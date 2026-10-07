const API = 'http://localhost:8080';

function getToken() {
    return localStorage.getItem('dailyJwt') || '';
}

function requireLogin() {
    if (!getToken()) {
        location.href = 'login.html';
        return false;
    }

    return true;
}

async function api(path, opt = {}) {

    const headers = {
        'Content-Type': 'application/json',
        ...(opt.headers || {})
    };

    const token = getToken();

    // 로그인 / 회원가입은 JWT를 보내지 않는다.
    const isAuthRequest =
        path === '/api/users/login' ||
        path === '/api/users/register';

    if (token && !isAuthRequest) {
        headers.Authorization = token;
    }

    const res = await fetch(API + path, {
        ...opt,
        headers
    });

    let data = null;

    try {
        data = await res.json();
    } catch (e) {
        // 응답 body가 없는 경우
    }

    /*
     * 현재 JwtFilter는 잘못된 JWT에 대해
     * 401이 아니라 403을 반환한다.
     */
    if (res.status === 401 || res.status === 403) {

        if (!isAuthRequest) {
            localStorage.removeItem('dailyJwt');
            localStorage.removeItem('dailyUserName');
            location.href = 'login.html';
        }

        throw new Error(
            data?.message ||
            data?.error ||
            `인증 실패 (${res.status})`
        );
    }

    if (!res.ok) {
        throw new Error(
            data?.message ||
            data?.error ||
            `요청 실패 (${res.status})`
        );
    }

    return data;
}

function dataOf(res) {
    return res?.data ?? res;
}

function esc(v = '') {
    return String(v).replace(
        /[&<>'"]/g,
        c => ({
            '&': '&amp;',
            '<': '&lt;',
            '>': '&gt;',
            "'": '&#39;',
            '"': '&quot;'
        }[c])
    );
}

function authorName(a) {
    if (!a) return '-';

    if (typeof a === 'string') {
        return a;
    }

    return a.name || a.email || a.username || '-';
}

function fmtDate(v) {
    if (!v) return '';

    const d = new Date(v);

    return Number.isNaN(d.getTime())
        ? v
        : d.toLocaleString('ko-KR');
}

function currentUserName() {
    return localStorage.getItem('dailyUserName') || '';
}

function initHeader() {

    const el = document.getElementById('userName');

    if (el) {
        el.textContent = currentUserName();
    }

    document
        .getElementById('logoutBtn')
        ?.addEventListener('click', () => {

            localStorage.removeItem('dailyJwt');
            localStorage.removeItem('dailyUserName');

            location.href = 'login.html';
        });
}