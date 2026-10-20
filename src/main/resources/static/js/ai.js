
(() => {
    "use strict";

    const API_URL = "http://localhost:8080/api/ai/daily/improve";

    const MODE_LABELS = {
        CORRECT: "문장 교정",
        TITLE: "제목 추천",
        EMOTIONAL: "감성 표현",
        SUMMARIZE: "요약하기"
    };

    const titleInput = document.getElementById("dailyTitle");
    const contentInput = document.getElementById("dailyContent");
    const statusEl = document.getElementById("aiStatus");
    const resultBox = document.getElementById("aiResultBox");
    const resultModeEl = document.getElementById("aiResultMode");
    const resultContentEl = document.getElementById("aiResultContent");
    const applyBtn = document.getElementById("aiApplyBtn");
    const retryBtn = document.getElementById("aiRetryBtn");
    const modeButtons = document.querySelectorAll("[data-ai-mode]");

    if (
        !titleInput ||
        !contentInput ||
        !statusEl ||
        !resultBox ||
        !resultModeEl ||
        !resultContentEl ||
        !applyBtn ||
        !retryBtn
    ) {
        console.warn("[AI] Daily 작성 폼 또는 AI 요소를 찾을 수 없습니다.");
        return;
    }

    let currentMode = null;
    let currentResult = "";
    let isLoading = false;

    function setStatus(message, type = "") {
        statusEl.textContent = message;
        statusEl.className = "ai-status";

        if (type) {
            statusEl.classList.add(type);
        }
    }

    function setLoading(loading) {
        isLoading = loading;

        modeButtons.forEach(button => {
            button.disabled = loading;
        });

        retryBtn.disabled = loading;
        applyBtn.disabled = loading;
    }

    function unwrapResponse(body) {
        return body?.data !== undefined ? body.data : body;
    }

    // XML 형태의 태그가 포함된 모델 응답도 처리한다.
    function parseAiResult(rawContent, mode) {
        const raw = String(rawContent ?? "").trim();

        if (!raw) {
            return "";
        }

        const titleMatch = raw.match(
            /<daily_title\b[^>]*>([\s\S]*?)<\/daily_title>/i
        );

        const contentMatch = raw.match(
            /<daily_content\b[^>]*>([\s\S]*?)<\/daily_content>/i
        );

        const extractText = value => {
            return String(value ?? "")
                .replace(/<\/?daily_title\b[^>]*>/gi, "")
                .replace(/<\/?daily_content\b[^>]*>/gi, "")
                .trim();
        };

        // 제목 추천은 제목 태그 안의 내용만 사용한다.
        if (mode === "TITLE" && titleMatch) {
            return extractText(titleMatch[1]);
        }

        // 본문 관련 작업은 본문 태그 안의 내용만 사용한다.
        if (mode !== "TITLE" && contentMatch) {
            return extractText(contentMatch[1]);
        }

        // 태그가 없는 일반 응답 또는 일부 태그만 있는 응답도 처리한다.
        return extractText(raw);
    }

    async function improveDaily(mode) {
        if (isLoading) return;

        if (!MODE_LABELS[mode]) {
            setStatus("지원하지 않는 AI 작업입니다.", "error");
            return;
        }

        const title = titleInput.value.trim();
        const content = contentInput.value.trim();

        if (!content) {
            setStatus("먼저 Daily 본문을 작성해 주세요.", "error");
            contentInput.focus();
            return;
        }

        const storedToken = localStorage.getItem("dailyJwt");
        const token = storedToken
            ? storedToken.replace(/^Bearer\s+/i, "").trim()
            : null;

        if (!token) {
            setStatus(
                "로그인이 필요합니다. 로그인 후 다시 시도해 주세요.",
                "error"
            );
            return;
        }

        currentMode = mode;
        currentResult = "";
        resultBox.hidden = true;

        setStatus(
            `${MODE_LABELS[mode]} 작업 중입니다. 모델 응답까지 잠시 기다려 주세요.`
        );
        setLoading(true);

        const controller = new AbortController();
        const timeoutId = setTimeout(() => controller.abort(), 120000);

        try {
            const response = await fetch(API_URL, {
                method: "POST",
                headers: {
                    "Content-Type": "application/json",
                    "Authorization": `Bearer ${token}`
                },
                body: JSON.stringify({
                    title,
                    content,
                    mode
                }),
                signal: controller.signal
            });

            const rawBody = await response.json().catch(() => null);

            if (response.status === 401 || response.status === 403) {
                throw new Error(
                    "인증에 실패했습니다. 로그인 상태와 JWT 토큰을 확인해 주세요."
                );
            }

            if (!response.ok) {
                throw new Error(
                    rawBody?.message ||
                    rawBody?.error ||
                    `AI 요청에 실패했습니다. (HTTP ${response.status})`
                );
            }

            const data = unwrapResponse(rawBody);

            if (typeof data?.content !== "string") {
                throw new Error(
                    "AI 응답 형식이 예상과 다릅니다. 백엔드 응답을 확인해 주세요."
                );
            }

            currentResult = parseAiResult(data.content, mode);

            if (!currentResult) {
                throw new Error("AI가 비어 있는 결과를 반환했습니다.");
            }

            resultModeEl.textContent = MODE_LABELS[mode];
            resultContentEl.value = currentResult;
            resultBox.hidden = false;

            setStatus(
                "AI 결과가 생성되었습니다. 확인 후 적용해 주세요.",
                "success"
            );
        } catch (error) {
            if (error.name === "AbortError") {
                setStatus(
                    "응답 시간이 2분을 초과했습니다. Ollama 실행 상태와 모델 로딩을 확인해 주세요.",
                    "error"
                );
            } else {
                setStatus(
                    error.message || "AI 요청 중 오류가 발생했습니다.",
                    "error"
                );
            }
        } finally {
            clearTimeout(timeoutId);
            setLoading(false);
        }
    }

    modeButtons.forEach(button => {
        button.addEventListener("click", () => {
            improveDaily(button.dataset.aiMode);
        });
    });

    retryBtn.addEventListener("click", () => {
        if (currentMode) {
            improveDaily(currentMode);
        }
    });

    applyBtn.addEventListener("click", () => {
        const result = resultContentEl.value.trim();

        if (!result) {
            setStatus("적용할 AI 결과가 없습니다.", "error");
            return;
        }

        if (currentMode === "TITLE") {
            titleInput.value = result;
            titleInput.dispatchEvent(new Event("input", { bubbles: true }));
        } else {
            contentInput.value = result;
            contentInput.dispatchEvent(new Event("input", { bubbles: true }));
        }

        resultBox.hidden = true;
        setStatus("AI 결과를 작성창에 적용했습니다.", "success");
    });
})();
