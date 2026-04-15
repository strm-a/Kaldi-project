package com.kaldi.resource;

import com.kaldi.dto.NewChatRequest;
import com.kaldi.entity.Chat;
import com.kaldi.entity.Message;
import com.kaldi.entity.User;
import com.kaldi.enums.ChatStatus;
import com.kaldi.enums.SenderType;
import jakarta.transaction.Transactional;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import org.eclipse.microprofile.openapi.annotations.Operation;
import org.eclipse.microprofile.openapi.annotations.tags.Tag;

import java.util.List;

@Path("/user")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
@Tag(name = "User", description = "Mobile user endpoints")
public class UserResource {

    @POST
    @Path("/chat")
    @Transactional
    @Operation(summary = "Start a new chat")
    public Response newChat(NewChatRequest request) {
        User user = User.findById(request.userId);
        if (user == null) {
            return Response.status(Response.Status.NOT_FOUND)
                    .entity("User not found")
                    .build();
        }

        // Create chat
        Chat chat = new Chat();
        chat.user = user;
        chat.room = request.room;
        chat.status = ChatStatus.WAITING;
        chat.persist();

        // Save the first message
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
    @Operation(summary = "Send a message in a chat")
    public Response sendMessage(@PathParam("chatId") Long chatId, NewChatRequest request) {
        Chat chat = Chat.findById(chatId);
        if (chat == null) {
            return Response.status(Response.Status.NOT_FOUND)
                    .entity("Chat not found")
                    .build();
        }

        Message message = new Message();
        message.chat = chat;
        message.senderType = SenderType.USER;
        message.senderId = request.userId;
        message.content = request.message;
        message.persist();

        return Response.ok(message).build();
    }

    @GET
    @Path("/chat/{chatId}/messages")
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