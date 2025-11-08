package com.example.luohaigui.manager;


import cn.hutool.core.collection.CollUtil;
import com.volcengine.ark.runtime.model.completion.chat.ChatCompletionChoice;
import com.volcengine.ark.runtime.model.completion.chat.ChatCompletionRequest;
import com.volcengine.ark.runtime.model.completion.chat.ChatMessage;
import com.volcengine.ark.runtime.model.completion.chat.ChatMessageRole;
import com.volcengine.ark.runtime.service.ArkService;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.List;

@Service
public class AiManager {

    @Resource
    private ArkService arkService;

    public String doChat(String systemPrompt,String userPrompt) {
        System.out.println("\n----- standard request -----");
        final List<ChatMessage> messages = new ArrayList<>();
        final ChatMessage systemMessage = ChatMessage.builder().role(ChatMessageRole.SYSTEM).content(systemPrompt).build();
        final ChatMessage userMessage = ChatMessage.builder().role(ChatMessageRole.USER).content(userPrompt).build();
        messages.add(systemMessage);
        messages.add(userMessage);
        return doChat(messages);
    }
    public String doChat(List<ChatMessage> chatMessageList) {

        ChatCompletionRequest chatCompletionRequest = ChatCompletionRequest.builder()
                // 指定您创建的方舟推理接入点 ID，此处已帮您修改为您的推理接入点 ID
                .model("deepseek-v3-1-terminus")
                .messages(chatMessageList)
                .build();

        List<ChatCompletionChoice> choiceList = arkService.createChatCompletion(chatCompletionRequest).
                getChoices();
        if(CollUtil.isEmpty(choiceList)){
            throw new RuntimeException("ai没有返回结果");
        }
        String content = (String) choiceList.get(0).getMessage().getContent();
        System.out.println("ai返回结果：" + content);
        return content;
    }
}
