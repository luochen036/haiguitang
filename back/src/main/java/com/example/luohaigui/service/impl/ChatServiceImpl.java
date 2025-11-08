package com.example.luohaigui.service.impl;

import com.example.luohaigui.manager.AiManager;
import com.example.luohaigui.model.ChatRoom;
import com.example.luohaigui.service.ChatService;
import com.volcengine.ark.runtime.model.completion.chat.ChatMessage;
import com.volcengine.ark.runtime.model.completion.chat.ChatMessageRole;

import javax.annotation.Resource;
import java.util.*;
import org.springframework.stereotype.Service;

@Service
public class ChatServiceImpl implements ChatService {


    @Resource
    private AiManager aiManager;

    final Map<Long, List<ChatMessage>> globalMessagesMap = new HashMap<>();

    @Override
    public String doChat(long roomId,String message) {
        final String systemPrompt = "你将成为一名专业的海龟汤游戏主持人，需严格遵循以下要求开展游戏：\n" +
                "角色与任务\n" +
                "负责出题（提供 “汤面”，即故事表面描述）和引导玩家推理。\n" +
                "根据玩家提问，仅能回答 “是”“否” 或 “与此无关”。\n" +
                "在特定情况下结束游戏并揭示 “汤底”（故事真相）。\n" +
                "游戏流程\n" +
                "当玩家输入 “开始” 时，立即提供一道简短、有趣且逻辑严密的海龟汤谜题 “汤面”（答案需出人意料但合理）。\n" +
                "玩家依次提问时，严格按以下规则回应：\n" +
                "“是”：玩家的猜测与真相相符。\n" +
                "“否”：玩家的猜测与真相不符。\n" +
                "“与此无关”：玩家的猜测与真相无直接关联。\n" +
                "出现以下情况时，主动结束游戏并揭示 “汤底”：\n" +
                "玩家明确表示 “不想玩了”“想要答案” 或类似表达。\n" +
                "玩家几乎已还原故事真相，或所有关键问题都已询问完毕。\n" +
                "玩家输入 “退出”。\n" +
                "玩家连续提问 10 次仍未触及关键信息，或表现出完全无头绪的状态。\n" +
                "核心注意事项\n" +
                "汤面设计要简短、有趣且逻辑严密，汤底需 “出人意料但合理”。\n" +
                "回答时严格遵守 “是”“否”“与此无关” 的规则，不得提供额外提示。\n" +
                "符合结束条件时，及时揭示 “汤底”，避免玩家陷入无效推理。\n" +
                "当你决定游戏结束时，必须在结束消息中包含【游戏已结束】。\n" +
                "出题时做到随机出题，确保每次出题都不一样。";
        final ChatMessage streamSystemMessage = ChatMessage.builder().role(ChatMessageRole.SYSTEM).content(systemPrompt).build();
        final ChatMessage streamUserMessage = ChatMessage.builder().role(ChatMessageRole.USER).content(message).build();
        List<ChatMessage> messages = new ArrayList<>();
        // 首次开始
        if(!message.equals("开始") && globalMessagesMap.isEmpty())
        {
            throw new RuntimeException("请先输入“开始”");
        }
        if(message.equals("开始") && !globalMessagesMap.containsKey(roomId)){
            globalMessagesMap.put(roomId,messages);
            messages.add(streamSystemMessage);
            messages.add(streamUserMessage);
        }
        else
        {
            messages = globalMessagesMap.get(roomId);
        }
        messages.add(streamUserMessage);
        String answer = aiManager.doChat(messages);
        final ChatMessage assistantMessage = ChatMessage.builder().role(ChatMessageRole.ASSISTANT).content(answer).build();
        messages.add(assistantMessage);
        if(answer.contains("【游戏已结束】"))
        {
            globalMessagesMap.remove(roomId);
        }
        return answer;
    }

    @Override
    public List<ChatRoom> getChatRoomList() {
        List<ChatRoom> chatRoomList = new ArrayList<>();
        for (Map.Entry<Long, List<ChatMessage>> roomIdMessageListEntry : globalMessagesMap.entrySet()){
                ChatRoom chatRoom = new ChatRoom();
                chatRoom.setRoomId(roomIdMessageListEntry.getKey());
                chatRoom.setChatMessagesList(roomIdMessageListEntry.getValue());
                chatRoomList.add(chatRoom);
        }
        return chatRoomList;
    }
}
