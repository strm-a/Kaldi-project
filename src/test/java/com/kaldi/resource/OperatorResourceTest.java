package com.kaldi.resource;

import com.kaldi.entity.*;
import com.kaldi.enums.ChatStatus;
import com.kaldi.enums.Room;
import io.quarkus.test.common.http.TestHTTPEndpoint;
import io.quarkus.test.junit.QuarkusTest;
import io.quarkus.test.security.TestSecurity;
import io.restassured.http.ContentType;
import jakarta.transaction.Transactional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.*;

@QuarkusTest
@TestHTTPEndpoint(OperatorResource.class) // adds /operator prefix to all requests
public class OperatorResourceTest {

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
    }

    @Test
    @TestSecurity(user = "op1", roles = {"operator"})
    public void testGetChats() {
        given()
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
    @TestSecurity(user = "op1", roles = {"operator"})
    public void testAcquireChat() {
        given()
            .contentType(ContentType.JSON)
        .when()
            .post("/chats/" + chatId + "/acquire")
        .then()
            .statusCode(200)
            .body("status", equalTo("ACTIVE"));
    }

    @Test
    @TestSecurity(user = "op1", roles = {"operator"})
    public void testAcquireChatNotFound() {
        given()
            .contentType(ContentType.JSON)
        .when()
            .post("/chats/99999/acquire")
        .then()
            .statusCode(404);
    }

    @Test
    @TestSecurity(user = "op1", roles = {"operator"})
    @Transactional
    public void testAcquireChatAlreadyActive() {
        given()
            .contentType(ContentType.JSON)
            .post("/chats/" + chatId + "/acquire");

        given()
            .contentType(ContentType.JSON)
        .when()
            .post("/chats/" + chatId + "/acquire")
        .then()
            .statusCode(409);
    }

    @Test
    @TestSecurity(user = "op1", roles = {"operator"})
    @Transactional
    public void testSendMessage() {
        given()
            .contentType(ContentType.JSON)
            .post("/chats/" + chatId + "/acquire");

        given()
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
    @TestSecurity(user = "op1", roles = {"operator"})
    public void testSendMessageOnWaitingChat() {
        given()
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
    @TestSecurity(user = "op1", roles = {"operator"})
    public void testGetMessages() {
        given()
            .contentType(ContentType.JSON)
            .post("/chats/" + chatId + "/acquire");

        given()
        .when()
            .get("/chats/" + chatId + "/messages")
        .then()
            .statusCode(200)
            .body("$", isA(java.util.List.class));
    }

    @Test
    @TestSecurity(user = "op1", roles = {"operator"})
    public void testSendMessageValidationFailure() {
        given()
            .contentType(ContentType.JSON)
            .post("/chats/" + chatId + "/acquire");

        given()
            .contentType(ContentType.JSON)
            .body("""
                {"message": "   "}
                """)
        .when()
            .post("/chats/" + chatId + "/message")
        .then()
            .statusCode(400);
    }

    @Test
    @TestSecurity(user = "op1", roles = {"operator"})
    public void testGetMessagesNotFound() {
        given()
        .when()
            .get("/chats/99999/messages")
        .then()
            .statusCode(404);
    }
}
