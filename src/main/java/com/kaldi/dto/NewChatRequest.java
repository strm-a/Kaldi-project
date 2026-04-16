package com.kaldi.dto;

import com.kaldi.enums.Room;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import org.eclipse.microprofile.openapi.annotations.media.Schema;

@Schema(description = "Payload for a mobile user starting a new chat.")
public class NewChatRequest {

    @Schema(description = "ID of the user starting the chat.", examples = {"1"}, required = true)
    @NotNull
    @Positive
    public Long userId;

    @Schema(description = "Support room / category the chat belongs to.", examples = {"TEHNIKA"}, required = true)
    @NotNull
    public Room room;

    @Schema(description = "First message from the user.", examples = {"Hi, my espresso machine won't turn on."}, required = true)
    @NotBlank
    public String message;
}
