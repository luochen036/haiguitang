package com.example.luohaigui.service;

import com.example.luohaigui.model.ChatRoom;
import com.volcengine.ark.runtime.model.completion.chat.ChatMessage;

import java.util.List;
import java.util.Map;

public interface ChatService {
    String doChat(long roomId,String message);

    List<ChatRoom> getChatRoomList();
}
