package com.kaldi.resource;

import com.kaldi.entity.*;
import com.kaldi.enums.ChatStatus;
import com.kaldi.enums.Room;
import com.kaldi.service.TokenService;
import io.quarkus.elytron.security.common.BcryptUtil;
import io.quarkus.test.common.http.TestHTTPEndpoint;
import io.quarkus.test.junit.QuarkusTest;
import io.restassured.http.ContentType;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.*;

@QuarkusTest
@TestHTTPEndpoint(OperatorResource.class) // adds /operator prefix to all requests
public class OperatorResourceTest {

    @Inject
    TokenService tokenService;

    String token;
    Long chatId;
    Long operatorId;

    @BeforeEach
    @Transactional
    public void setup() {
        Message.deleteAll();
        Chat.deleteAll();
        Operator.deleteAll();
        User.deleteAll();

        Operator op = new Operator();
        op.username = "op1";
        op.passwordHash = BcryptUtil.bcryptHash("pass");
        op.persist();
        operatorId = op.idOperator;

        User user = new User();
        user.username = "client1";
        user.persist();

        Chat chat = new Chat();
        chat.user = user;
        chat.room = Room.TEHNIKA;
        chat.status = ChatStatus.WAITING;
        chat.persist();
        chatId = chat.idChat;

        token = tokenService.generateToken("op1", "operator");
    }

    @Test
    public void testGetChats() {
        given()
            .header("Authorization", "Bearer " + token)
        .when()
            .get("/chats")
        .then()
            .statusCode(200)
            .body("size()", greaterThanOrEqualTo(1));
    }

    @Test
    public void testGetChatsUnauthorized() {
        given()
        .when()
            .get("/chats")
        .then()
            .statusCode(401);
    }

    @Test
    public void testAcquireChat() {
        given()
            .header("Authorization", "Bearer " + token)
            .contentType(ContentType.JSON)
        .when()
            .post("/chats/" + chatId + "/acquire")
        .then()
            .statusCode(200)
            .body("status", equalTo("ACTIVE"));
    }

    @Test
    public void testAcquireChatNotFound() {
        given()
            .header("Authorization", "Bearer " + token)
            .contentType(ContentType.JSON)
        .when()
            .post("/chats/99999/acquire")
        .then()
            .statusCode(404);
    }

    @Test
    @Transactional
    public void testAcquireChatAlreadyActive() {
        // Acquire it first
        given()
            .header("Authorization", "Bearer " + token)
            .contentType(ContentType.JSON)
            .post("/chats/" + chatId + "/acquire");

        // Try to acquire again
        given()
            .header("Authorization", "Bearer " + token)
            .contentType(ContentType.JSON)
        .when()
            .post("/chats/" + chatId + "/acquire")
        .then()
            .statusCode(409);
    }

    @Test
    @Transactional
    public void testSendMessage() {
        // Acquire chat first
        given()
            .header("Authorization", "Bearer " + token)
            .contentType(ContentType.JSON)
            .post("/chats/" + chatId + "/acquire");

        given()
            .header("Authorization", "Bearer " + token)
            .contentType(ContentType.JSON)
            .body("""
                {"message": "Hello, how can I help you?"}
                """)
        .when()
            .post("/chats/" + chatId + "/message")
        .then()
            .statusCode(200)
            .body("content", equalTo("Hello, how can I help you?"));
    }

    @Test
    public void testSendMessageOnWaitingChat() {
        given()
            .header("Authorization", "Bearer " + token)
            .contentType(ContentType.JSON)
            .body("""
                {"message": "This should fail"}
                """)
        .when()
            .post("/chats/" + chatId + "/message")
        .then()
            .statusCode(409);
    }

    @Test
    public void testGetMessages() {
        given()
            .header("Authorization", "Bearer " + token)
        .when()
            .get("/chats/" + chatId + "/messages")
        .then()
            .statusCode(200)
            .body("$", isA(java.util.List.class));
    }

    @Test
    public void testGetMessagesNotFound() {
        given()
            .header("Authorization", "Bearer " + token)
        .when()
            .get("/chats/99999/messages")
        .then()
            .statusCode(404);
    }
}
