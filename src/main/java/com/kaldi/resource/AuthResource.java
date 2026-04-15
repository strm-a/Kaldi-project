package com.kaldi.resource;

import com.kaldi.dto.LoginRequest;
import com.kaldi.dto.LoginResponse;
import com.kaldi.entity.Operator;
import com.kaldi.service.TokenService;
import io.quarkus.elytron.security.common.BcryptUtil;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import org.eclipse.microprofile.openapi.annotations.Operation;
import org.eclipse.microprofile.openapi.annotations.tags.Tag;

@Path("/operator/login")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
@Tag(name = "Auth", description = "Operator authentication")
public class AuthResource {

    @Inject
    TokenService tokenService;

    @POST
    @Transactional
    @Operation(summary = "Operator login", description = "Returns a JWT token for authenticated operators")
    public Response login(LoginRequest request) {
        // Find operator by username
        Operator operator = Operator.findByUsername(request.username);

        if (operator == null) {
            return Response.status(Response.Status.UNAUTHORIZED)
                    .entity("Invalid username or password")
                    .build();
        }

        // Verify password against bcrypt hash
        if (!BcryptUtil.matches(request.password, operator.passwordHash)) {
            return Response.status(Response.Status.UNAUTHORIZED)
                    .entity("Invalid username or password")
                    .build();
        }

        // Generate JWT token
        String token = tokenService.generateToken(operator.username, "operator");

        return Response.ok(new LoginResponse(token, operator.username)).build();
    }
}