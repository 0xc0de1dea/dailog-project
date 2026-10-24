
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
        if (request == null) {
            throw new IllegalArgumentException(
                    "AI 글쓰기 요청이 없습니다."
            );
        }

        if (request.mode() == null || request.mode().isBlank()) {
            throw new IllegalArgumentException(
                    "글쓰기 모드를 선택해 주세요."
            );
        }

        if (request.content() == null || request.content().isBlank()) {
            throw new IllegalArgumentException(
                    "Daily 본문을 입력해 주세요."
            );
        }

        AiWritingMode mode;

        try {
            mode = AiWritingMode.valueOf(
                    request.mode().trim().toUpperCase()
            );
        } catch (IllegalArgumentException e) {
            throw new IllegalArgumentException(
                    "지원하지 않는 글쓰기 모드입니다: "
                            + request.mode(),
                    e
            );
        }

        String title = request.title() == null
                ? ""
                : request.title().trim();

        String content = request.content().trim();

        double temperature = switch (mode) {
            case CORRECT -> 0.1;
            case TITLE -> 0.3;
            case EMOTIONAL -> 0.4;
            case SUMMARIZE -> 0.2;
        };

        int maxTokens = switch (mode) {
            case CORRECT -> 512;
            case TITLE -> 128;
            case EMOTIONAL -> 768;
            case SUMMARIZE -> 256;
        };

        long start = System.currentTimeMillis();

        System.out.println(
                "[AI] 요청 시작 - mode: " + mode
        );

        String result;

        try {
            result = chatClient.prompt()
                    .options(OllamaChatOptions.builder()
                            .model("qwen3:4b-instruct-2507-q4_K_M")
                            .temperature(temperature)
                            .numPredict(maxTokens)
                            .disableThinking())
                    .system(writingPrompt.getSystemPrompt(mode))
                    .user(user -> user.text("""
        다음은 사용자가 작성한 Daily 글이다.

        지정된 글쓰기 작업만 수행하라.
        작업 과정이나 분석은 출력하지 말고 완성된 결과물만 반환하라.

        중요:
        - <daily_title>, <daily_content> 같은 XML 태그를 출력하지 마라.
        - 제목이나 본문을 구분하는 불필요한 라벨을 출력하지 마라.
        - 결과물 외의 설명이나 인사말을 출력하지 마라.
        - 제목 추천 모드에서는 추천 제목만 출력하라.
        - 나머지 모드에서는 개선된 본문만 출력하라.

        기존 제목:
        {title}

        본문:
        {content}
        """)
                            .param("title", title)
                            .param("content", content))
                    .call()
                    .content();

            if (result == null || result.isBlank()) {
                throw new IllegalStateException(
                        "AI가 빈 응답을 반환했습니다."
                );
            }

            result = result.trim();

            System.out.println(
                    "[AI] 응답 완료: "
                            + (System.currentTimeMillis() - start)
                            + "ms"
            );

            System.out.println(
                    "[AI] mode: " + mode
            );

            System.out.println(
                    "[AI] 응답 길이: " + result.length()
            );

        } catch (Exception e) {
            System.err.println(
                    "[AI] 오류 발생: "
                            + (System.currentTimeMillis() - start)
                            + "ms"
            );

            throw e;
        }

        return new DailyImproveResponse(
                request.title(),
                result,
                mode.name()
        );
    }
}
