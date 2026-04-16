package com.kaldi.resource;

import com.kaldi.dto.ChatDetailDTO;
import com.kaldi.dto.ChatSummaryDTO;
import com.kaldi.dto.SendMessageRequest;
import com.kaldi.entity.Chat;
import com.kaldi.entity.Message;
import com.kaldi.entity.Operator;
import com.kaldi.enums.ChatStatus;
import com.kaldi.enums.SenderType;
import jakarta.inject.Inject;
import jakarta.persistence.EntityManager;
import jakarta.annotation.security.RolesAllowed;
import jakarta.transaction.Transactional;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.Context;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.core.SecurityContext;
import org.eclipse.microprofile.openapi.annotations.Operation;
import org.eclipse.microprofile.openapi.annotations.media.Content;
import org.eclipse.microprofile.openapi.annotations.media.ExampleObject;
import org.eclipse.microprofile.openapi.annotations.media.Schema;
import org.eclipse.microprofile.openapi.annotations.parameters.Parameter;
import org.eclipse.microprofile.openapi.annotations.parameters.RequestBody;
import org.eclipse.microprofile.openapi.annotations.responses.APIResponse;
import org.eclipse.microprofile.openapi.annotations.responses.APIResponses;
import org.eclipse.microprofile.openapi.annotations.security.SecurityRequirement;
import org.eclipse.microprofile.openapi.annotations.tags.Tag;
import java.time.LocalDateTime;
import java.util.List;

@Path("/operator")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
@RolesAllowed("operator")
@SecurityRequirement(name = "jwt")
@Tag(name = "Operator", description = "Operator endpoints (JWT with role `operator` required)")
public class OperatorResource {

    @Inject
    EntityManager entityManager;

    @GET
    @Path("/chats")
    @Operation(summary = "List all open chats", description = "Returns every chat in WAITING or ACTIVE status, across all operators.")
    @APIResponses({
        @APIResponse(responseCode = "200", description = "Open chats returned.",
            content = @Content(schema = @Schema(implementation = ChatSummaryDTO[].class))),
        @APIResponse(responseCode = "401", description = "Missing or invalid JWT."),
        @APIResponse(responseCode = "403", description = "Token lacks the `operator` role.")
    })
    public Response getChats() {
        List<ChatSummaryDTO> chats = Chat.findAllOpen()
                .stream()
                .map(ChatSummaryDTO::new)
                .toList();
        return Response.ok(chats).build();
    }

    @POST
    @Path("/chats/{chatId}/acquire")
    @Transactional
    @Operation(summary = "Acquire a waiting chat", description = "Assigns the chat to the calling operator and transitions it from WAITING to ACTIVE.")
    @APIResponses({
        @APIResponse(responseCode = "200", description = "Chat acquired. Full chat detail returned.",
            content = @Content(schema = @Schema(implementation = ChatDetailDTO.class))),
        @APIResponse(responseCode = "401", description = "Missing or invalid JWT."),
        @APIResponse(responseCode = "403", description = "Token lacks the `operator` role."),
        @APIResponse(responseCode = "404", description = "Chat not found."),
        @APIResponse(responseCode = "409", description = "Chat is not in WAITING status.")
    })
    public Response acquireChat(
            @Parameter(description = "ID of the chat to acquire.", example = "42")
            @PathParam("chatId") Long chatId,
            @Context SecurityContext securityContext) {

        Chat chat = Chat.findById(chatId);
        if (chat == null) {
            return Response.status(Response.Status.NOT_FOUND)
                    .entity("Chat not found")
                    .build();
        }

        if (chat.status != ChatStatus.WAITING) {
            return Response.status(Response.Status.CONFLICT)
                    .entity("Chat is not in WAITING status")
                    .build();
        }

        String username = securityContext.getUserPrincipal().getName();
        Operator operator = Operator.findByUsername(username);
        if (operator == null) {
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                    .entity("No operator row found for principal '" + username + "'. "
                          + "Either the JWT principal claim isn't `preferred_username`, "
                          + "or the operators table is missing a row for this user.")
                    .build();
        }

        LocalDateTime acquiredAt = LocalDateTime.now();
        long updated = Chat.update(
                "operator = ?1, status = ?2, acquiredAt = ?3, version = version + 1 where idChat = ?4 and status = ?5 and version = ?6",
                operator, ChatStatus.ACTIVE, acquiredAt, chatId, ChatStatus.WAITING, chat.version);

        if (updated == 0) {
            return Response.status(Response.Status.CONFLICT)
                    .entity("Chat is not in WAITING status")
                    .build();
        }

        entityManager.refresh(chat);

        List<Message> messages = Message.findByChat(chat);
        return Response.ok(new ChatDetailDTO(chat, messages)).build();
    }

    @POST
    @Path("/chats/{chatId}/message")
    @Transactional
    @Operation(summary = "Send a reply in a chat", description = "Posts a message from the calling operator into an ACTIVE chat. Only the operator who acquired the chat can reply.")
    @APIResponses({
        @APIResponse(responseCode = "200", description = "Message saved.",
            content = @Content(schema = @Schema(implementation = Message.class))),
        @APIResponse(responseCode = "401", description = "Missing or invalid JWT."),
        @APIResponse(responseCode = "403", description = "Token lacks the `operator` role, or the caller is not the operator who acquired this chat."),
        @APIResponse(responseCode = "404", description = "Chat not found."),
        @APIResponse(responseCode = "409", description = "Chat is not ACTIVE.")
    })
    public Response sendMessage(
            @Parameter(description = "ID of the chat to post into.", example = "42")
            @PathParam("chatId") Long chatId,
            @RequestBody(
                description = "Operator's reply to the chat.",
                required = true,
                content = @Content(
                    schema = @Schema(implementation = SendMessageRequest.class),
                    examples = @ExampleObject(
                        name = "operator-reply",
                        value = "{\"message\": \"Hi how can I help you today?\"}"
                    )
                )
            )
            @Valid @NotNull SendMessageRequest request,
            @Context SecurityContext securityContext) {

        Chat chat = Chat.findById(chatId);
        if (chat == null) {
            return Response.status(Response.Status.NOT_FOUND)
                    .entity("Chat not found")
                    .build();
        }

        if (chat.status != ChatStatus.ACTIVE) {
            return Response.status(Response.Status.CONFLICT)
                    .entity("Chat is not active")
                    .build();
        }

        String username = securityContext.getUserPrincipal().getName();
        if (chat.operator == null || !chat.operator.username.equalsIgnoreCase(username)) {
            return Response.status(Response.Status.FORBIDDEN)
                    .entity("You are not the operator assigned to this chat")
                    .build();
        }

        Message message = new Message();
        message.chat = chat;
        message.senderType = SenderType.OPERATOR;
        message.senderId = chat.operator.idOperator;
        message.content = request.message;
        message.persist();

        return Response.ok(message).build();
    }

    @GET
    @Path("/chats/{chatId}/messages")
    @Operation(summary = "Get full message history for a chat", description = "Returns all messages for the given chat, ordered oldest to newest. Only the operator who acquired the chat can view its messages.")
    @APIResponses({
        @APIResponse(responseCode = "200", description = "Messages returned.",
            content = @Content(schema = @Schema(implementation = Message[].class))),
        @APIResponse(responseCode = "401", description = "Missing or invalid JWT."),
        @APIResponse(responseCode = "403", description = "Token lacks the `operator` role, or the caller is not the operator who acquired this chat."),
        @APIResponse(responseCode = "404", description = "Chat not found.")
    })
    public Response getMessages(
            @Parameter(description = "ID of the chat.", example = "42")
            @PathParam("chatId") Long chatId,
            @Context SecurityContext securityContext) {
        Chat chat = Chat.findById(chatId);
        if (chat == null) {
            return Response.status(Response.Status.NOT_FOUND)
                    .entity("Chat not found")
                    .build();
        }

        String username = securityContext.getUserPrincipal().getName();
        if (chat.operator == null || !chat.operator.username.equalsIgnoreCase(username)) {
            return Response.status(Response.Status.FORBIDDEN)
                    .entity("You are not the operator assigned to this chat")
                    .build();
        }

        List<Message> messages = Message.findByChat(chat);
        return Response.ok(messages).build();
    }
}
