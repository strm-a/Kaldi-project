package com.kaldi.dto;

import com.kaldi.enums.Room;

public class NewChatRequest {
    public Long userId;
    public Room room;
    public String message; // first message from the user
}