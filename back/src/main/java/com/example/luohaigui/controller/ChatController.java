package com.example.luohaigui.controller;

import com.example.luohaigui.model.ChatRoom;
import com.example.luohaigui.service.ChatService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import java.util.List;

@RestController
@RequestMapping("/chat") // 基础路径，统一管理聊天相关接口
public class ChatController {
    @Resource
    private final ChatService chatService;

    // 构造方法注入Service（推荐的注入方式）
    @Autowired
    public ChatController(ChatService chatService) {
        this.chatService = chatService;
    }

    /**
     * 发送聊天消息接口
     * 用于向指定聊天室发送消息
     * @param roomId 聊天室ID
     * @param message 消息内容
     * @return 处理结果（如消息发送成功标识或返回的消息内容）
     */
    @PostMapping("/message") // POST请求适合提交数据（发送消息）
    public String sendChatMessage(
            @RequestParam("roomId") long roomId, // 从请求参数获取聊天室ID
            @RequestParam("message") String message) { // 从请求参数获取消息内容
        return chatService.doChat(roomId, message);
    }

    /**
     * 获取聊天室列表接口
     * 用于查询所有可用的聊天室
     * @return 聊天室列表（自动序列化为JSON）
     */
    @GetMapping("/rooms") // GET请求适合查询资源（获取列表）
    public List<ChatRoom> getChatRooms() {
        return chatService.getChatRoomList();
    }
}