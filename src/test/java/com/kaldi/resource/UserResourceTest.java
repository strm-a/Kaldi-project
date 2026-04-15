package com.kaldi.resource;

import io.quarkus.test.common.http.TestHTTPEndpoint;
import io.quarkus.test.junit.QuarkusTest;
import io.restassured.http.ContentType;
import jakarta.transaction.Transactional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import com.kaldi.entity.User;
import com.kaldi.entity.Chat;
import com.kaldi.entity.Message;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.*;

@QuarkusTest
@TestHTTPEndpoint(UserResource.class) // adds /user prefix to all requests
public class UserResourceTest {

    Long userId;
    Long chatId;

    @BeforeEach
    @Transactional
    public void setup() {
        Message.deleteAll();
        Chat.deleteAll();
        User.deleteAll();

        User user = new User();
        user.username = "mobileuser";
        user.persist();
        userId = user.idUser;
    }

    @Test
    public void testStartNewChat() {
        given()
            .contentType(ContentType.JSON)
            .body(String.format("""
                {"userId": %d, "room": "TEHNIKA", "message": "Hello, I need help!"}
                """, userId))
        .when()
            .post("/chat")
        .then()
            .statusCode(200)
            .body("status", equalTo("WAITING"));
    }

    @Test
    public void testStartChatUserNotFound() {
        given()
            .contentType(ContentType.JSON)
            .body("""
                {"userId": 99999, "room": "TEHNIKA", "message": "Hello"}
                """)
        .when()
            .post("/chat")
        .then()
            .statusCode(404);
    }

    @Test
    @Transactional
    public void testSendMessageInChat() {
        chatId = given()
            .contentType(ContentType.JSON)
            .body(String.format("""
                {"userId": %d, "room": "TEHNIKA", "message": "First message"}
                """, userId))
            .post("/chat")
            .then()
            .statusCode(200)
            .extract()
            .jsonPath().getLong("idChat");

        given()
            .contentType(ContentType.JSON)
            .body(String.format("""
                {"userId": %d, "room": "TEHNIKA", "message": "Follow-up message"}
                """, userId))
        .when()
            .post("/chat/" + chatId + "/message")
        .then()
            .statusCode(200)
            .body("content", equalTo("Follow-up message"));
    }

    @Test
    public void testGetMessagesForUnknownChat() {
        given()
        .when()
            .get("/chat/99999/messages")
        .then()
            .statusCode(404);
    }

    @Test
    public void testGetMessages() {
        Long createdChatId = given()
            .contentType(ContentType.JSON)
            .body(String.format("""
                {"userId": %d, "room": "TEHNIKA", "message": "Hey!"}
                """, userId))
            .post("/chat")
            .then()
            .statusCode(200)
            .extract()
            .jsonPath().getLong("idChat");

        given()
        .when()
            .get("/chat/" + createdChatId + "/messages")
        .then()
            .statusCode(200)
            .body("size()", greaterThanOrEqualTo(1));
    }
}
