package com.kaldi.dto;

import jakarta.validation.constraints.NotBlank;
import org.eclipse.microprofile.openapi.annotations.media.Schema;

@Schema(description = "Payload for sending a message in an existing chat.")
public class SendMessageRequest {

    @Schema(description = "Message body.", examples = {"So I didn't try turning it off and on again but I will say that I did, okay?"}, required = true)
    @NotBlank
    public String message;
}
