/* =========================================================
   Daily Chat
========================================================= */

const CHAT = {

    rooms: '/api/chat/rooms',

    messages: id =>
        `/api/chat/rooms/${id}/messages`,

    ws: '/ws',

    subscribe: id =>
        `/sub/chat/${id}`,

    send:
        '/pub/chat.send'
};


let stompClient = null;
let currentRoomId = null;


/* =========================================================
   공통
========================================================= */

function getRoomId(room) {

    return (
        room?.roomId ??
        room?.id
    );
}


function getRoomName(room) {

    return (
        room?.name ||
        room?.title ||
        '채팅방'
    );
}


function getCurrentUserName() {

    return (
        localStorage.getItem('dailyUserName') ||
        ''
    );
}


/* =========================================================
   채팅방 목록
========================================================= */

async function loadRooms() {

    const box =
        document.getElementById('roomList');

    if (!box) {
        return;
    }

    box.innerHTML = `
        <div class="chat-room-loading">
            채팅방을 불러오는 중...
        </div>
    `;

    try {

        const response =
            await api(CHAT.rooms);

        const rooms =
            dataOf(response) || [];

        if (!Array.isArray(rooms) ||
            rooms.length === 0) {

            box.innerHTML = `
                <div class="chat-room-empty">
                    <div class="chat-room-empty-icon">
                        💬
                    </div>

                    <p>
                        아직 채팅방이 없습니다.
                    </p>

                    <span>
                        + 버튼으로<br>
                        새로운 채팅방을 만들어보세요.
                    </span>
                </div>
            `;

            return;
        }

        box.innerHTML =
            rooms
                .map(room => {

                    const roomId =
                        getRoomId(room);

                    if (!roomId) {
                        return '';
                    }

                    const roomName =
                        getRoomName(room);

                    const active =
                        String(roomId) ===
                        String(currentRoomId);

                    return `
                        <a
                            class="chat-room-item ${active ? 'active' : ''}"
                            href="chat.html?roomId=${encodeURIComponent(roomId)}"
                        >

                            <div class="chat-room-item-avatar">
                                #
                            </div>

                            <div class="chat-room-item-info">

                                <div class="chat-room-item-name">
                                    ${esc(roomName)}
                                </div>

                                <div class="chat-room-item-message">
                                    ${esc(
                        room.lastMessage ||
                        '새로운 대화를 시작해보세요.'
                    )}
                                </div>

                            </div>

                        </a>
                    `;
                })
                .join('');

    } catch (error) {

        console.error(
            '[CHAT] 채팅방 목록 조회 실패:',
            error
        );

        box.innerHTML = `
            <div class="chat-room-error">

                채팅방을 불러오지 못했습니다.

                <small>
                    ${esc(error.message || '')}
                </small>

            </div>
        `;
    }
}


/* =========================================================
   메시지 조회
========================================================= */

async function loadMessages(roomId) {

    const box =
        document.getElementById(
            'chatMessages'
        );

    if (!box) {
        return;
    }

    box.innerHTML = `
        <div class="chat-message-loading">
            메시지를 불러오는 중...
        </div>
    `;

    try {

        const response =
            await api(
                CHAT.messages(roomId)
            );

        const messages =
            dataOf(response) || [];

        if (!Array.isArray(messages) ||
            messages.length === 0) {

            box.innerHTML = `
                <div class="chat-no-message">

                    <div class="chat-no-message-icon">
                        👋
                    </div>

                    <strong>
                        아직 메시지가 없습니다.
                    </strong>

                    <span>
                        첫 번째 메시지를 보내보세요.
                    </span>

                </div>
            `;

            return;
        }

        box.innerHTML =
            messages
                .map(chatHtml)
                .join('');

        box.scrollTop =
            box.scrollHeight;

    } catch (error) {

        console.error(
            '[CHAT] 메시지 조회 실패:',
            error
        );

        box.innerHTML = `
            <div class="chat-message-error">

                메시지를 불러오지 못했습니다.

                <small>
                    ${esc(error.message || '')}
                </small>

            </div>
        `;
    }
}


/* =========================================================
   메시지 HTML
========================================================= */

function chatHtml(message) {

    const senderName =
        message?.senderName ||
        '사용자';

    const content =
        message?.content ||
        '';

    const currentName =
        getCurrentUserName();

    const mine =
        String(senderName) ===
        String(currentName);

    const createdAt =
        message?.createdAt
            ? formatChatTime(message.createdAt)
            : '';

    const initial =
        senderName
            .charAt(0)
            .toUpperCase();

    return `
        <div
            class="chat-message-row ${mine ? 'mine' : 'other'}"
        >

            ${
        mine
            ? ''
            : `
                        <div class="chat-message-avatar">
                            ${esc(initial)}
                        </div>
                    `
    }

            <div class="chat-message-content">

                ${
        mine
            ? ''
            : `
                            <div class="chat-message-sender">
                                ${esc(senderName)}
                            </div>
                        `
    }

                <div class="chat-message-line">

                    <div class="chat-bubble">
                        ${esc(content)}
                    </div>

                    ${
        createdAt
            ? `
                                <span class="chat-message-time">
                                    ${esc(createdAt)}
                                </span>
                            `
            : ''
    }

                </div>

            </div>

        </div>
    `;
}


/* =========================================================
   시간
========================================================= */

function formatChatTime(value) {

    const date =
        new Date(value);

    if (
        Number.isNaN(
            date.getTime()
        )
    ) {
        return '';
    }

    return date.toLocaleTimeString(
        'ko-KR',
        {
            hour: '2-digit',
            minute: '2-digit'
        }
    );
}


/* =========================================================
   채팅 화면
========================================================= */

function showChatWorkspace(room) {

    const empty =
        document.getElementById(
            'chatEmpty'
        );

    const workspace =
        document.getElementById(
            'chatWorkspace'
        );


    /*
     * 빈 화면 숨기기
     */
    if (empty) {

        empty.hidden = true;

        empty.style.display =
            'none';
    }


    /*
     * 실제 채팅 화면 표시
     */
    if (workspace) {

        workspace.hidden = false;

        workspace.style.display =
            'flex';
    }


    /*
     * 채팅방 이름
     */
    const title =
        document.getElementById(
            'roomTitle'
        );


    if (title) {

        title.textContent =
            getRoomName(room);
    }


    /*
     * 채팅방 아이콘
     */
    const avatar =
        document.getElementById(
            'roomAvatar'
        );


    if (avatar) {

        avatar.textContent =
            '#';
    }


    console.log(
        '[CHAT] 채팅방 화면 활성화:',
        getRoomId(room),
        getRoomName(room)
    );
}


/* =========================================================
   STOMP 연결 종료
========================================================= */

function disconnectChat() {

    if (
        stompClient &&
        stompClient.connected
    ) {

        try {
            stompClient.disconnect();
        } catch (e) {
            console.warn(
                '[CHAT] STOMP 종료 오류:',
                e
            );
        }
    }

    stompClient = null;
}


/* =========================================================
   STOMP 연결
========================================================= */

function connectChat(roomId) {

    const state =
        document.getElementById(
            'connectionState'
        );

    if (!window.SockJS ||
        !window.Stomp) {

        console.error(
            '[CHAT] SockJS/STOMP가 없습니다.'
        );

        if (state) {
            state.textContent =
                '채팅 라이브러리 오류';
        }

        return;
    }

    const token =
        getToken();

    if (!token) {

        if (state) {
            state.textContent =
                '로그인이 필요합니다.';
        }

        return;
    }

    /*
     * 기존 연결이 있다면 종료
     */
    disconnectChat();

    let jwt =
        token.trim();

    /*
     * localStorage의 JWT에는
     * Bearer가 포함되어 있을 수 있다.
     */
    if (
        jwt.startsWith('Bearer ')
    ) {

        jwt =
            jwt
                .substring(7)
                .trim();
    }

    if (!jwt) {

        if (state) {
            state.textContent =
                'JWT가 없습니다.';
        }

        return;
    }

    if (state) {

        state.textContent =
            '● 연결 중...';

        state.classList.remove(
            'connected'
        );
    }

    try {

        const socket =
            new SockJS(
                API + CHAT.ws
            );

        stompClient =
            Stomp.over(socket);

        /*
         * 개발 중에는 로그를 유지한다.
         */
        stompClient.debug =
            message => {
                console.log(
                    '[STOMP]',
                    message
                );
            };

        stompClient.connect(

            {
                Authorization:
                    `Bearer ${jwt}`
            },

            frame => {

                console.log(
                    '[CHAT] STOMP 연결 성공',
                    frame
                );

                if (state) {

                    state.textContent =
                        '● 연결됨';

                    state.classList.add(
                        'connected'
                    );
                }

                stompClient.subscribe(

                    CHAT.subscribe(roomId),

                    message => {

                        try {

                            const data =
                                JSON.parse(
                                    message.body
                                );

                            console.log(
                                '[CHAT] 수신:',
                                data
                            );

                            appendChatMessage(
                                data
                            );

                        } catch (error) {

                            console.error(
                                '[CHAT] 메시지 파싱 실패:',
                                error,
                                message.body
                            );
                        }
                    }
                );

            },

            error => {

                console.error(
                    '[CHAT] STOMP 연결 실패:',
                    error
                );

                if (state) {

                    state.textContent =
                        '● 연결 실패';

                    state.classList.remove(
                        'connected'
                    );
                }
            }
        );

    } catch (error) {

        console.error(
            '[CHAT] WebSocket 오류:',
            error
        );

        if (state) {
            state.textContent =
                '● 연결 실패';
        }
    }
}


/* =========================================================
   새 메시지 추가
========================================================= */

function appendChatMessage(message) {

    const box =
        document.getElementById(
            'chatMessages'
        );

    if (!box) {
        return;
    }

    /*
     * "아직 메시지가 없습니다."
     * 화면 제거
     */
    box
        .querySelector(
            '.chat-no-message'
        )
        ?.remove();

    /*
     * 로딩 화면 제거
     */
    box
        .querySelector(
            '.chat-message-loading'
        )
        ?.remove();

    /*
     * 오류 화면 제거
     */
    box
        .querySelector(
            '.chat-message-error'
        )
        ?.remove();

    /*
     * 실제 메시지 추가
     */
    box.insertAdjacentHTML(
        'beforeend',
        chatHtml(message)
    );

    /*
     * 가장 아래로 이동
     */
    box.scrollTop =
        box.scrollHeight;
}


/* =========================================================
   채팅방 생성
========================================================= */

async function createChatRoom() {

    const name =
        prompt(
            '새 채팅방 이름을 입력하세요.'
        );

    if (name === null) {
        return;
    }

    const trimmed =
        name.trim();

    if (!trimmed) {

        alert(
            '채팅방 이름을 입력해주세요.'
        );

        return;
    }

    try {

        const response =
            await api(
                `${CHAT.rooms}?name=${encodeURIComponent(trimmed)}`,
                {
                    method: 'POST'
                }
            );

        const room =
            dataOf(response);

        const roomId =
            getRoomId(room);

        if (!roomId) {

            throw new Error(
                '채팅방 ID를 받지 못했습니다.'
            );
        }

        /*
         * 생성 후 해당 채팅방으로 이동
         */
        location.href =
            `chat.html?roomId=${encodeURIComponent(roomId)}`;

    } catch (error) {

        console.error(
            '[CHAT] 채팅방 생성 실패:',
            error
        );

        alert(
            error.message ||
            '채팅방을 만들지 못했습니다.'
        );
    }
}


/* =========================================================
   메시지 전송
========================================================= */

function sendChatMessage() {

    const input =
        document.getElementById(
            'chatInput'
        );

    if (!input) {
        return;
    }

    const content =
        input.value.trim();

    if (!content) {
        return;
    }

    if (
        !stompClient ||
        !stompClient.connected
    ) {

        alert(
            '채팅 서버에 연결되지 않았습니다.'
        );

        return;
    }

    if (!currentRoomId) {

        alert(
            '채팅방을 선택해주세요.'
        );

        return;
    }

    const roomId =
        Number(currentRoomId);

    if (!Number.isFinite(roomId)) {

        alert(
            '잘못된 채팅방입니다.'
        );

        return;
    }

    try {

        stompClient.send(

            CHAT.send,

            {},

            JSON.stringify({
                roomId: roomId,
                content: content
            })
        );

        input.value = '';

        input.focus();

    } catch (error) {

        console.error(
            '[CHAT] 메시지 전송 실패:',
            error
        );

        alert(
            '메시지를 전송하지 못했습니다.'
        );
    }
}


/* =========================================================
   초기화
========================================================= */

document.addEventListener(
    'DOMContentLoaded',
    async () => {

        if (!requireLogin()) {
            return;
        }

        initHeader();

        const params =
            new URLSearchParams(
                location.search
            );

        const roomId =
            params.get('roomId');

        currentRoomId =
            roomId;

        /*
         * 채팅방 목록
         */
        await loadRooms();

        /*
         * 채팅방이 선택된 경우
         */
        if (roomId) {

            /*
             * URL에 roomId가 있다는 것은
             * 이미 특정 채팅방으로 진입했다는 뜻이다.
             *
             * 따라서 API 조회보다 먼저
             * 채팅 화면을 활성화한다.
             */
            showChatWorkspace({
                id: roomId,
                name: '채팅방'
            });


            /*
             * 실제 채팅방 이름 조회
             */
            try {

                const response =
                    await api(
                        CHAT.rooms
                    );

                const rooms =
                    dataOf(response) || [];


                const room =
                    rooms.find(
                        item =>
                            String(
                                getRoomId(item)
                            ) ===
                            String(roomId)
                    );


                if (room) {

                    showChatWorkspace(
                        room
                    );
                }

            } catch (error) {

                console.error(
                    '[CHAT] 채팅방 정보 조회 실패:',
                    error
                );

                /*
                 * 이미 화면은 열려 있으므로
                 * 여기서는 아무것도 하지 않는다.
                 */
            }


            /*
             * 기존 메시지 조회
             */
            await loadMessages(
                roomId
            );


            /*
             * Redis Pub/Sub을 거쳐오는
             * STOMP 구독 연결
             */
            connectChat(
                roomId
            );
        }

        /*
         * 새 채팅방 버튼
         */
        document
            .getElementById(
                'newRoomBtn'
            )
            ?.addEventListener(
                'click',
                createChatRoom
            );

        document
            .getElementById(
                'emptyNewRoomBtn'
            )
            ?.addEventListener(
                'click',
                createChatRoom
            );

        /*
         * 메시지 전송
         */
        document
            .getElementById(
                'chatForm'
            )
            ?.addEventListener(
                'submit',
                event => {

                    event.preventDefault();

                    sendChatMessage();
                }
            );
    }
);


/* =========================================================
   페이지 종료
========================================================= */

window.addEventListener(
    'beforeunload',
    () => {
        disconnectChat();
    }
);