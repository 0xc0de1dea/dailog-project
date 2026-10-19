package com.example.dailog.domain.ai.service;

import com.example.dailog.domain.ai.dto.DailyImproveRequest;
import com.example.dailog.domain.ai.dto.DailyImproveResponse;
import com.example.dailog.domain.ai.enums.AiWritingMode;
import com.example.dailog.domain.ai.prompt.DailyWritingPrompt;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.ollama.api.OllamaChatOptions;
import org.springframework.stereotype.Service;

@Service
public class AiWritingService {

    private final ChatClient chatClient;
    private final DailyWritingPrompt writingPrompt;

    public AiWritingService(
            ChatClient.Builder chatClientBuilder,
            DailyWritingPrompt writingPrompt
    ) {
        this.chatClient = chatClientBuilder.build();
        this.writingPrompt = writingPrompt;
    }

    public DailyImproveResponse improve(
            DailyImproveRequest request
    ) {
        AiWritingMode mode = AiWritingMode.valueOf(
                request.mode().trim().toUpperCase()
        );

        String result = chatClient.prompt()
                .options(OllamaChatOptions.builder()
                        .model("qwen3:4b")
                        .temperature(0.3)
                        .numPredict(128)
                        .disableThinking())
                .system(writingPrompt.getSystemPrompt(mode))
                .user(user -> user.text("""
            다음 Daily 글을 작업하라.

            제목:
            {title}

            본문:
            {content}
            """)
                        .param("title",
                                request.title() == null ? "" : request.title())
                        .param("content", request.content()))
                .call()
                .content();

        if (result == null || result.isBlank()) {
            throw new IllegalStateException(
                    "AI가 빈 응답을 반환했습니다."
            );
        }

        return new DailyImproveResponse(
                request.title(),
                result.trim(),
                mode.name()
        );
    }
}