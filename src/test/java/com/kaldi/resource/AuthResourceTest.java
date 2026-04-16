package com.kaldi.resource;

import com.kaldi.entity.Chat;
import com.kaldi.entity.Message;
import com.kaldi.entity.Operator;
import com.kaldi.entity.User;
import io.quarkus.test.junit.QuarkusTest;
import io.quarkus.test.security.TestSecurity;
import jakarta.transaction.Transactional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static io.restassured.RestAssured.given;

@QuarkusTest
public class AuthResourceTest {

    @BeforeEach
    @Transactional
    public void setup() {
        Message.deleteAll();
        Chat.deleteAll();
        User.deleteAll();
        Operator.deleteAll();

        Operator op = new Operator();
        op.username = "testoperator";
        op.persist();
    }

    @Test
    public void unauthenticatedRequestIsRejected() {
        given()
        .when()
            .get("/operator/chats")
        .then()
            .statusCode(401);
    }

    @Test
    @TestSecurity(user = "testoperator", roles = {"user"})
    public void wrongRoleIsForbidden() {
        given()
        .when()
            .get("/operator/chats")
        .then()
            .statusCode(403);
    }

    @Test
    @TestSecurity(user = "testoperator", roles = {"operator"})
    public void operatorRoleCanListChats() {
        given()
        .when()
            .get("/operator/chats")
        .then()
            .statusCode(200);
    }
}
