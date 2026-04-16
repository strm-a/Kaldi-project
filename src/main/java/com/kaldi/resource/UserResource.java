package com.kaldi.resource;

import com.kaldi.dto.NewChatRequest;
import com.kaldi.dto.SendMessageRequest;
import com.kaldi.entity.Chat;
import com.kaldi.entity.Message;
import com.kaldi.entity.User;
import com.kaldi.enums.ChatStatus;
import com.kaldi.enums.SenderType;
import jakarta.transaction.Transactional;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import org.eclipse.microprofile.openapi.annotations.Operation;
import org.eclipse.microprofile.openapi.annotations.media.Content;
import org.eclipse.microprofile.openapi.annotations.media.Schema;
import org.eclipse.microprofile.openapi.annotations.parameters.Parameter;
import org.eclipse.microprofile.openapi.annotations.responses.APIResponse;
import org.eclipse.microprofile.openapi.annotations.responses.APIResponses;
import org.eclipse.microprofile.openapi.annotations.tags.Tag;

import java.util.List;

@Path("/user")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
@Tag(name = "User", description = "Mobile user endpoints (anonymous — no JWT required)")
public class UserResource {

    @POST
    @Path("/chat")
    @Transactional
    @Operation(summary = "Start a new chat", description = "Creates a WAITING chat for the given user and stores the first message.")
    @APIResponses({
        @APIResponse(responseCode = "200", description = "Chat created.",
            content = @Content(schema = @Schema(implementation = Chat.class))),
        @APIResponse(responseCode = "404", description = "User not found.")
    })
    public Response newChat(@Valid @NotNull NewChatRequest request) {
        User user = User.findById(request.userId);
        if (user == null) {
            return Response.status(Response.Status.NOT_FOUND)
                    .entity("User not found")
                    .build();
        }

        Chat chat = new Chat();
        chat.user = user;
        chat.room = request.room;
        chat.status = ChatStatus.WAITING;
        chat.persist();

        Message message = new Message();
        message.chat = chat;
        message.senderType = SenderType.USER;
        message.senderId = user.idUser;
        message.content = request.message;
        message.persist();

        return Response.ok(chat).build();
    }

    @POST
    @Path("/chat/{chatId}/message")
    @Transactional
    @Operation(summary = "Send a message in a chat", description = "Posts a user-side message into an existing chat.")
    @APIResponses({
        @APIResponse(responseCode = "200", description = "Message saved.",
            content = @Content(schema = @Schema(implementation = Message.class))),
        @APIResponse(responseCode = "404", description = "Chat not found.")
    })
    public Response sendMessage(
            @Parameter(description = "ID of the chat to post into.", example = "42")
            @PathParam("chatId") Long chatId,
            @Valid @NotNull SendMessageRequest request) {
        Chat chat = Chat.findById(chatId);
        if (chat == null) {
            return Response.status(Response.Status.NOT_FOUND)
                    .entity("Chat not found")
                    .build();
        }

        Message message = new Message();
        message.chat = chat;
        message.senderType = SenderType.USER;
        message.senderId = chat.user.idUser;
        message.content = request.message;
        message.persist();

        return Response.ok(message).build();
    }

    @GET
    @Path("/chat/{chatId}/messages")
    @Operation(summary = "Get all messages in a chat", description = "Returns every message in the chat, ordered oldest to newest.")
    @APIResponses({
        @APIResponse(responseCode = "200", description = "Messages returned.",
            content = @Content(schema = @Schema(implementation = Message[].class))),
        @APIResponse(responseCode = "404", description = "Chat not found.")
    })
    public Response getMessages(
            @Parameter(description = "ID of the chat.", example = "42")
            @PathParam("chatId") Long chatId) {
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
