package com.kaldi;

import jakarta.ws.rs.core.Application;
import org.eclipse.microprofile.openapi.annotations.OpenAPIDefinition;
import org.eclipse.microprofile.openapi.annotations.enums.SecuritySchemeType;
import org.eclipse.microprofile.openapi.annotations.info.Info;
import org.eclipse.microprofile.openapi.annotations.security.SecurityScheme;
import org.eclipse.microprofile.openapi.annotations.tags.Tag;

@OpenAPIDefinition(
    info = @Info(
        title = "Kaldi Support API",
        version = "1.0.0",
        description = """
            Backend for the Kaldi customer-support chat.

            **Endpoints under `/user/*`** are anonymous - the mobile app identifies the user \
            by `userId` in the request body. No token required.

            **Endpoints under `/operator/*`** require a Keycloak-issued JWT with the \
            `operator` role. In dev mode, the seeded operator accounts are \
            `aliceOperator` / `alice`, `mikeOperator` / `mike`, and `lucyOperator` / `lucy`.

            Swagger UI exposes this OpenAPI definition as live reference when the app is running.
            Use Bruno or another HTTP client for request testing.
            """
    ),
    tags = {
        @Tag(name = "Operator", description = "Operator dashboard — requires JWT with role `operator`."),
        @Tag(name = "User", description = "Endpoints consumed by the mobile app (anonymous).")
    }
)
@SecurityScheme(
    securitySchemeName = "jwt",
    type = SecuritySchemeType.HTTP,
    scheme = "bearer",
    bearerFormat = "JWT"
)
public class KaldiApplication extends Application {
}
