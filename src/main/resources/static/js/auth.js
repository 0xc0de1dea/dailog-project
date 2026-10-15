document.addEventListener('DOMContentLoaded', () => {
 const loginForm = document.getElementById('loginForm');

 // 로그인
 if (loginForm) {
  loginForm.addEventListener('submit', async (e) => {
   e.preventDefault();

   const msg = document.getElementById('message');

   try {
    // 로그인 요청에는 기존 JWT를 보내지 않음
    const response = await api('/api/users/login', {
     method: 'POST',
     auth: false,
     body: JSON.stringify({
      email: document.getElementById('email').value,
      password: document.getElementById('password').value
     })
    });

    const result = dataOf(response);
    const accessToken = result?.accessToken;
    const refreshToken = result?.refreshToken;

    if (!accessToken || !refreshToken) {
     throw new Error(
         '로그인 응답에 Access Token 또는 Refresh Token이 없습니다.'
     );
    }

    localStorage.setItem('dailyJwt', accessToken);
    localStorage.setItem('dailyRefreshToken', refreshToken);

    location.href = 'daily.html';
   } catch (err) {
    if (msg) msg.textContent = err.message;
   }
  });
 }

 // 회원가입
 const signupForm = document.getElementById('signupForm');

 if (signupForm) {
  signupForm.addEventListener('submit', async (e) => {
   e.preventDefault();

   const msg = document.getElementById('message');

   try {
    await api('/api/users/register', {
     method: 'POST',
     auth: false,
     body: JSON.stringify({
      name: document.getElementById('name').value,
      email: document.getElementById('email').value,
      password: document.getElementById('password').value
     })
    });

    alert('회원가입이 완료되었습니다.');
    location.href = 'login.html';
   } catch (err) {
    if (msg) msg.textContent = err.message;
   }
  });
 }
});