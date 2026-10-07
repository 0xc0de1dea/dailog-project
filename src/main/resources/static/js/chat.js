/* =========================================================
   Chat API / STOMP
   ========================================================= */

const CHAT = {
    rooms: '/api/chat/rooms',
    messages: id => `/api/chat/rooms/${id}/messages`,
    ws: '/ws',
    subscribe: id => `/sub/chat/${id}`,
    send: '/pub/chat.send'
};

let stompClient = null;
let currentRoomId = null;


/* =========================================================
   채팅방 목록
   ========================================================= */

async function loadRooms() {

    const box =
        document.getElementById('roomList');

    if (!box) return;

    try {

        const r =
            await api(CHAT.rooms);

        const rooms =
            dataOf(r) || [];

        box.innerHTML =
            rooms.length
                ? rooms.map(x => {

                    const roomId =
                        x.roomId ?? x.id;

                    const roomName =
                        x.name ||
                        x.title ||
                        '채팅방';

                    return `
                        <a
                            class="room"
                            href="daily-detail.html?roomId=${encodeURIComponent(roomId)}"
                        >
                            <strong>
                                ${esc(roomName)}
                            </strong>

                            <span class="meta">
                                ${esc(x.lastMessage || '')}
                            </span>
                        </a>
                    `;

                }).join('')

                : `
                    <div class="empty">
                        채팅방이 없습니다.
                    </div>
                `;

    } catch (e) {

        console.error(
            '채팅방 목록 조회 오류:',
            e
        );

        box.innerHTML = `
            <div class="empty">
                ${esc(
            e.message ||
            '채팅방을 불러오지 못했습니다.'
        )}
            </div>
        `;
    }
}


/* =========================================================
   채팅 메시지 불러오기
   ========================================================= */

async function loadMessages(id) {

    const box =
        document.getElementById('chatMessages');

    if (!box) return;

    try {

        const r =
            await api(CHAT.messages(id));

        const msgs =
            dataOf(r) || [];

        box.innerHTML =
            msgs
                .map(m => chatHtml(m))
                .join('');

        box.scrollTop =
            box.scrollHeight;

    } catch (e) {

        console.error(
            '채팅 메시지 조회 오류:',
            e
        );

        box.innerHTML = `
            <div class="empty">
                ${esc(
            e.message ||
            '메시지를 불러오지 못했습니다.'
        )}
            </div>
        `;
    }
}


/* =========================================================
   채팅 메시지 HTML
   ========================================================= */

function chatHtml(m) {

    /*
     * ChatMessageResponse의 실제 필드:
     *
     * messageId
     * content
     * senderId
     * senderName
     * createdAt
     */

    const name =
        m.senderName ||
        m.sender ||
        m.author ||
        m.username ||
        '사용자';

    const mine =
        name === currentUserName();

    return `
        <div class="chat-message ${mine ? 'mine' : ''}">

            <div class="sender">
                ${esc(name)}
            </div>

            <div class="bubble">
                ${esc(
        m.content ||
        m.message ||
        ''
    )}
            </div>

        </div>
    `;
}


/* =========================================================
   STOMP 연결
   ========================================================= */

function connectChat(id) {

    const state = document.getElementById('connectionState');

    if (!window.SockJS || !window.Stomp) {

        console.error('[CHAT] SockJS/STOMP 라이브러리가 없습니다.');

        if (state) {
            state.textContent = '채팅 라이브러리 오류';
        }

        return;
    }

    if (!id) {
        console.error('[CHAT] roomId가 없습니다.');
        return;
    }

    currentRoomId = id;

    let token = getToken();

    console.log('[CHAT] 원본 JWT:', token);

    if (!token) {

        if (state) {
            state.textContent = '로그인이 필요합니다.';
        }

        return;
    }

    /*
     * dailyJwt가
     *
     * Bearer eyJ...
     *
     * 형태로 저장되어 있든
     *
     * eyJ...
     *
     * 형태로 저장되어 있든
     *
     * 최종적으로 순수 JWT만 남긴다.
     */
    token = token.trim();

    if (token.startsWith('Bearer ')) {
        token = token.substring(7).trim();
    }

    /*
     * 혹시 Bearer가 중복 저장되어 있다면 제거
     */
    while (token.startsWith('Bearer ')) {
        token = token.substring(7).trim();
    }

    console.log('[CHAT] 정리된 JWT 존재:', !!token);
    console.log(
        '[CHAT] JWT 앞부분:',
        token.substring(0, 20)
    );

    if (!token) {

        if (state) {
            state.textContent = 'JWT가 없습니다.';
        }

        return;
    }

    try {

        const socket =
            new SockJS(API + '/ws');

        socket.onopen = () => {
            console.log('[CHAT] SockJS 연결 성공');
        };

        socket.onclose = event => {
            console.log(
                '[CHAT] SockJS 연결 종료',
                event
            );
        };

        socket.onerror = error => {
            console.error(
                '[CHAT] SockJS 오류:',
                error
            );
        };

        stompClient = Stomp.over(socket);

        stompClient.debug = message => {
            console.log('[STOMP]', message);
        };

        stompClient.connect(

            {
                Authorization: 'Bearer ' + token
            },

            frame => {

                console.log(
                    '[CHAT] STOMP 연결 성공!',
                    frame
                );

                if (state) {
                    state.textContent = '연결됨';
                }

                stompClient.subscribe(
                    `/sub/chat/${id}`,
                    message => {

                        console.log(
                            '[CHAT] 메시지 수신:',
                            message.body
                        );

                        try {

                            const data =
                                JSON.parse(message.body);

                            const box =
                                document.getElementById(
                                    'chatMessages'
                                );

                            if (!box) {
                                return;
                            }

                            box.insertAdjacentHTML(
                                'beforeend',
                                chatHtml(data)
                            );

                            box.scrollTop =
                                box.scrollHeight;

                        } catch (e) {

                            console.error(
                                '[CHAT] 메시지 처리 오류:',
                                e
                            );
                        }
                    }
                );
            },

            error => {

                console.error(
                    '=============================='
                );

                console.error(
                    '[CHAT] STOMP 연결 실패'
                );

                console.error(
                    '[CHAT] 서버 응답:',
                    error
                );

                console.error(
                    '=============================='
                );

                if (state) {
                    state.textContent = '연결 실패';
                }
            }
        );

    } catch (e) {

        console.error(
            '[CHAT] WebSocket 생성 실패:',
            e
        );

        if (state) {
            state.textContent = '연결 실패';
        }
    }
}


/* =========================================================
   채팅방 생성
   ========================================================= */

async function createChatRoom() {

    const name =
        prompt(
            '채팅방 이름을 입력하세요.'
        );


    /*
     * 취소
     */
    if (name === null) {
        return;
    }


    /*
     * 빈 이름 방지
     */
    const trimmedName =
        name.trim();

    if (!trimmedName) {

        alert(
            '채팅방 이름을 입력해주세요.'
        );

        return;
    }


    try {

        /*
         * 백엔드가
         *
         * @PostMapping
         * public ... create(
         *     @RequestParam String name
         * )
         *
         * 형태이므로 JSON body가 아니라
         * ?name= 형태로 전송한다.
         */

        const r =
            await api(
                `${CHAT.rooms}?name=${encodeURIComponent(trimmedName)}`,
                {
                    method: 'POST'
                }
            );


        const room =
            dataOf(r);


        if (!room) {

            throw new Error(
                '채팅방 생성 결과를 받지 못했습니다.'
            );
        }


        const roomId =
            room.roomId ??
            room.id;


        if (!roomId) {

            throw new Error(
                '생성된 채팅방 ID가 없습니다.'
            );
        }


        /*
         * 생성된 채팅방으로 이동
         */

        location.href =
            `daily-detail.html?roomId=${encodeURIComponent(roomId)}`;

    } catch (e) {

        console.error(
            '채팅방 생성 오류:',
            e
        );

        alert(
            e.message ||
            '채팅방 생성에 실패했습니다.'
        );
    }
}


/* =========================================================
   채팅 메시지 전송
   ========================================================= */

function sendChatMessage() {

    const input =
        document.getElementById(
            'chatInput'
        );

    if (!input) return;


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
            '채팅방이 선택되지 않았습니다.'
        );

        return;
    }


    try {

        stompClient.send(

            CHAT.send,

            {},

            JSON.stringify({
                roomId: currentRoomId,
                content: content
            })

        );


        input.value = '';

        input.focus();

    } catch (e) {

        console.error(
            '메시지 전송 오류:',
            e
        );

        alert(
            '메시지 전송에 실패했습니다.'
        );
    }
}


/* =========================================================
   초기화
   ========================================================= */

document.addEventListener(
    'DOMContentLoaded',
    () => {

        /*
         * 로그인 확인
         */

        if (!requireLogin()) {
            return;
        }


        /*
         * 상단 헤더
         */

        initHeader();


        /*
         * 채팅방 목록
         */

        loadRooms();


        /*
         * 현재 채팅방 확인
         */

        const roomId =
            new URLSearchParams(
                location.search
            ).get('roomId');


        if (roomId) {

            loadMessages(roomId);

            connectChat(roomId);
        }


        /*
         * 채팅방 생성 버튼
         */

        document
            .getElementById('newRoomBtn')
            ?.addEventListener(
                'click',
                createChatRoom
            );


        /*
         * 채팅 메시지 전송
         */

        document
            .getElementById('chatForm')
            ?.addEventListener(
                'submit',
                e => {

                    e.preventDefault();

                    sendChatMessage();
                }
            );

    }
);