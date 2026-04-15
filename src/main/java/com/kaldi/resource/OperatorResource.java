package com.kaldi.resource;

import com.kaldi.dto.ChatDetailDTO;
import com.kaldi.dto.ChatSummaryDTO;
import com.kaldi.dto.SendMessageRequest;
import com.kaldi.entity.Chat;
import com.kaldi.entity.Message;
import com.kaldi.entity.Operator;
import com.kaldi.enums.ChatStatus;
import com.kaldi.enums.SenderType;
import jakarta.annotation.security.RolesAllowed;
import jakarta.transaction.Transactional;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.Context;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.core.SecurityContext;
import org.eclipse.microprofile.openapi.annotations.Operation;
import org.eclipse.microprofile.openapi.annotations.security.SecurityRequirement;
import org.eclipse.microprofile.openapi.annotations.tags.Tag;
import java.time.LocalDateTime;
import java.util.List;

@Path("/operator")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
@RolesAllowed("operator")
@SecurityRequirement(name = "jwt")
@Tag(name = "Operator", description = "Operator endpoints (JWT required)")
public class OperatorResource {

    // GET /operator/chats — all waiting + active chats
    @GET
    @Path("/conversations")
    @Operation(summary = "Get all open conversations (waiting and active)")
    public Response getConversations() {
        List<ChatSummaryDTO> chats = Chat.findAllOpen()
                .stream()
                .map(ChatSummaryDTO::new)
                .toList();
        return Response.ok(chats).build();
    }

    // POST /operator/chats/{chatId}/acquire — take over a waiting chat
    @POST
    @Path("/conversations/{chatId}/acquire")
    @Transactional
    @Operation(summary = "Acquire a waiting conversation")
    public Response acquireChat(
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

        chat.operator = operator;
        chat.status = ChatStatus.ACTIVE;
        chat.acquiredAt = LocalDateTime.now();
        chat.persist();

        // Return full detail including user, room and messages
        List<Message> messages = Message.findByChat(chat);
        return Response.ok(new ChatDetailDTO(chat, messages)).build();
    }

    // POST /operator/chats/{chatId}/message — send a reply
    @POST
    @Path("/chats/{chatId}/message")
    @Transactional
    @Operation(summary = "Send a message in a chat")
    public Response sendMessage(
            @PathParam("chatId") Long chatId,
            SendMessageRequest request,
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
        Operator operator = Operator.findByUsername(username);

        Message message = new Message();
        message.chat = chat;
        message.senderType = SenderType.OPERATOR;
        message.senderId = operator.idOperator;
        message.content = request.message;
        message.persist();

        return Response.ok(message).build();
    }

    // GET /operator/chats/{chatId}/messages — full message history
    @GET
    @Path("/chats/{chatId}/messages")
    @Operation(summary = "Get all messages in a chat")
    public Response getMessages(@PathParam("chatId") Long chatId) {
        Chat chat = Chat.findById(chatId);
        if (chat == null) {
            return Response.status(Response.Status.NOT_FOUND)
                    .entity("Chat not found")
                    .build();
        }

        List<Message> messages = Message.findByChat(chat);
        return Response.ok(messages).build();
    }
}