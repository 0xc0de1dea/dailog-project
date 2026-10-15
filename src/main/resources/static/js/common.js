const API = 'http://localhost:8080';

function getToken() {
    return localStorage.getItem('dailyJwt') || '';
}

function getRefreshToken() {
    return localStorage.getItem('dailyRefreshToken') || '';
}

function clearTokens() {
    localStorage.removeItem('dailyJwt');
    localStorage.removeItem('dailyRefreshToken');
    localStorage.removeItem('dailyUserName');
}

function requireLogin() {
    if (!getToken()) {
        location.href = 'login.html';
        return false;
    }
    return true;
}

function dataOf(response) {
    return response?.data ?? response;
}

// API 요청 공통 함수
async function api(path, opt = {}) {
    const {
        auth = true,
        retry = true,
        ...fetchOptions
    } = opt;

    const headers = {
        'Content-Type': 'application/json',
        ...(fetchOptions.headers || {})
    };

    if (auth && getToken()) {
        // 백엔드 토큰에 "Bearer "가 이미 포함되어 있음
        headers.Authorization = getToken();
    }

    const response = await fetch(API + path, {
        ...fetchOptions,
        headers
    });

    let body = null;
    try {
        body = await response.json();
    } catch {
        // 응답 본문이 없는 경우
    }

    const unauthorized =
        response.status === 401 ||
        (
            response.status === 403 &&
            body?.error === 'Unauthorized'
        );

    // Access Token이 만료/무효라면 한 번 재발급 시도
    if (auth && retry && unauthorized && getRefreshToken()) {
        try {
            await reissueAccessToken();

            // 새 Access Token으로 원래 요청을 한 번 재시도
            return await api(path, {
                ...opt,
                retry: false
            });
        } catch (err) {
            clearTokens();
            location.href = 'login.html';
            throw new Error('로그인이 만료되었습니다. 다시 로그인해 주세요.');
        }
    }

    if (response.status === 401 || unauthorized) {
        if (auth) {
            clearTokens();
            location.href = 'login.html';
        }
        throw new Error(body?.message || body?.error || '인증에 실패했습니다.');
    }

    if (!response.ok) {
        throw new Error(
            body?.message || body?.error || `요청 실패 (${response.status})`
        );
    }

    return body;
}

// Refresh Token으로 Access Token 재발급
async function reissueAccessToken() {
    const refreshToken = getRefreshToken();

    if (!refreshToken) {
        throw new Error('Refresh Token이 없습니다.');
    }

    // 재발급 요청 자체에는 기존 Access Token을 넣지 않음
    const response = await fetch(API + '/api/auth/reissue', {
        method: 'POST',
        headers: {
            'Content-Type': 'application/json'
        },
        body: JSON.stringify({ refreshToken })
    });

    let body = null;
    try {
        body = await response.json();
    } catch {
        // 응답 본문이 없는 경우
    }

    if (!response.ok) {
        throw new Error(
            body?.message || body?.error || '토큰 재발급에 실패했습니다.'
        );
    }

    const result = dataOf(body);
    const newAccessToken = result?.accessToken;
    const newRefreshToken = result?.refreshToken;

    if (!newAccessToken || !newRefreshToken) {
        throw new Error('재발급 응답에 토큰이 없습니다.');
    }

    // 백엔드는 Refresh Token도 새 토큰으로 교체함
    localStorage.setItem('dailyJwt', newAccessToken);
    localStorage.setItem('dailyRefreshToken', newRefreshToken);
}

function esc(value = '') {
    return String(value).replace(/[&<>'"]/g, c => ({
        '&': '&amp;',
        '<': '&lt;',
        '>': '&gt;',
        "'": '&#39;',
        '"': '&quot;'
    }[c]));
}

function authorName(author) {
    if (!author) return '-';
    if (typeof author === 'string') return author;
    return author.name || author.email || author.username || '-';
}

function fmtDate(value) {
    if (!value) return '';
    const date = new Date(value);
    return Number.isNaN(date.getTime())
        ? value
        : date.toLocaleString('ko-KR');
}

function currentUserName() {
    return localStorage.getItem('dailyUserName') || '';
}

function initHeader() {
    const userName = document.getElementById('userName');
    if (userName) userName.textContent = currentUserName();

    document.getElementById('logoutBtn')?.addEventListener('click', async () => {
        const refreshToken = getRefreshToken();

        try {
            if (refreshToken) {
                await api('/api/auth/logout', {
                    method: 'POST',
                    auth: false,
                    body: JSON.stringify({ refreshToken })
                });
            }
        } catch (err) {
            console.error('서버 로그아웃 처리 실패:', err);
        } finally {
            // 서버 요청 실패 여부와 관계없이 브라우저 토큰 삭제
            clearTokens();
            location.href = 'login.html';
        }
    });
}