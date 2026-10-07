document.addEventListener('DOMContentLoaded', () => {

 const login = document.getElementById('loginForm');

 if (login) {
  login.addEventListener('submit', async e => {
   e.preventDefault();

   const msg = document.getElementById('message');

   try {
    const r = await api('/api/users/login', {
     method: 'POST',
     body: JSON.stringify({
      email: document.getElementById('email').value,
      password: document.getElementById('password').value
     })
    });

    /*
     * 백엔드:
     *
     * ResponseEntity<ApiResponse<String>>
     *
     * userService.login()에서 JWT 문자열을 반환하므로
     * dataOf(r)는 JWT 문자열이 된다.
     */
    const token = dataOf(r);

    if (!token || typeof token !== 'string') {
     throw new Error(
         '로그인 응답에서 JWT를 찾을 수 없습니다.'
     );
    }

    localStorage.setItem('dailyJwt', token);

    location.href = 'daily.html';

   } catch (err) {
    msg.textContent = err.message;
   }
  });
 }


 const signup = document.getElementById('signupForm');

 if (signup) {
  signup.addEventListener('submit', async e => {
   e.preventDefault();

   const msg = document.getElementById('message');

   try {
    await api('/api/users/register', {
     method: 'POST',
     body: JSON.stringify({
      name: document.getElementById('name').value,
      email: document.getElementById('email').value,
      password: document.getElementById('password').value
     })
    });

    alert('회원가입이 완료되었습니다.');

    location.href = 'login.html';

   } catch (err) {
    msg.textContent = err.message;
   }
  });
 }

});