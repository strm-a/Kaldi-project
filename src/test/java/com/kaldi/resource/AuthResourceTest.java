package com.kaldi.resource;

import io.quarkus.elytron.security.common.BcryptUtil;
import io.quarkus.test.common.http.TestHTTPEndpoint;
import io.quarkus.test.junit.QuarkusTest;
import io.restassured.http.ContentType;
import jakarta.transaction.Transactional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import com.kaldi.entity.Chat;
import com.kaldi.entity.User;
import com.kaldi.entity.Message;
import com.kaldi.entity.Operator;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.*;

@QuarkusTest
@TestHTTPEndpoint(OperatorResource.class) // adds /operator prefix to all requests
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
        op.passwordHash = BcryptUtil.bcryptHash("secret123");
        op.persist();
    }


    @Test
    public void testLoginSuccess() {
        given()
            .contentType(ContentType.JSON)
            .body("""
                {"username": "testoperator", "password": "secret123"}
                """)
        .when()
            .post("/login")
        .then()
            .statusCode(200)
            .body("token", notNullValue())
            .body("username", equalTo("testoperator"));
    }

    @Test
    public void testLoginWrongPassword() {
        given()
            .contentType(ContentType.JSON)
            .body("""
                {"username": "testoperator", "password": "wrongpassword"}
                """)
        .when()
            .post("/login")
        .then()
            .statusCode(401);
    }

    @Test
    public void testLoginUnknownUser() {
        given()
            .contentType(ContentType.JSON)
            .body("""
                {"username": "nobody", "password": "secret123"}
                """)
        .when()
            .post("/login")
        .then()
            .statusCode(401);
    }
}
