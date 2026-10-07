const COMMENT = {
    list: id => `/api/comments/${id}`,
    one: id => `/api/comments/${id}`,
    like: id => `/api/comments/${id}/likes`
};


async function loadComments() {

    const box = document.getElementById('commentList');

    if (!box) return;

    const dailyId = new URLSearchParams(location.search).get('id');

    if (!dailyId) return;

    try {

        const r = await api(COMMENT.list(dailyId));

        const data = dataOf(r);

        // 현재 백엔드 GET /api/comments/{id}는 List를 바로 반환하므로
        // 일반적으로 data 자체가 배열입니다.
        const arr = Array.isArray(data)
            ? data
            : Array.isArray(data?.content)
                ? data.content
                : [];

        if (!arr.length) {

            box.innerHTML = `
                <div class="empty">
                    아직 댓글이 없습니다.
                </div>
            `;

            return;
        }


        box.innerHTML = arr.map(c => `

            <article class="comment">

                <div class="comment-head">

                    <strong>
                        ${esc(authorName(c.author))}
                    </strong>

                    <span class="meta">
                        ${esc(fmtDate(c.createdAt || c.modifiedAt))}
                    </span>

                </div>


                <div class="comment-content">
                    ${esc(c.content || '')}
                </div>


                <div class="meta">
                    ♥ ${c.likes ?? 0}
                </div>


                <div class="comment-actions">

                    <button
                        type="button"
                        data-like="${c.commentId}"
                    >
                        좋아요
                    </button>

                    <button
                        type="button"
                        data-edit="${c.commentId}"
                    >
                        수정
                    </button>

                    <button
                        type="button"
                        data-delete="${c.commentId}"
                    >
                        삭제
                    </button>

                </div>

            </article>

        `).join('');


        // 좋아요
        box.querySelectorAll('[data-like]').forEach(button => {

            button.onclick = async () => {

                try {

                    await api(
                        COMMENT.like(button.dataset.like),
                        {
                            method: 'POST'
                        }
                    );

                    await loadComments();

                } catch (e) {

                    alert(e.message);

                }

            };

        });


        // 수정
        box.querySelectorAll('[data-edit]').forEach(button => {

            button.onclick = async () => {

                const content = prompt(
                    '댓글 내용을 수정하세요.'
                );

                if (content === null) return;

                const trimmedContent = content.trim();

                if (!trimmedContent) {

                    alert('댓글 내용을 입력해주세요.');

                    return;

                }

                if (trimmedContent.length > 100) {

                    alert('댓글은 최대 100자까지 입력할 수 있습니다.');

                    return;

                }


                const password = prompt(
                    '댓글 비밀번호를 입력하세요.'
                );

                if (password === null) return;

                if (!password) {

                    alert('비밀번호를 입력해주세요.');

                    return;

                }


                try {

                    await api(
                        COMMENT.one(button.dataset.edit),
                        {
                            method: 'PATCH',
                            headers: {
                                'Content-Type': 'application/json'
                            },
                            body: JSON.stringify({
                                content: trimmedContent,
                                password: password
                            })
                        }
                    );

                    await loadComments();

                } catch (e) {

                    alert(e.message);

                }

            };

        });


        // 삭제
        box.querySelectorAll('[data-delete]').forEach(button => {

            button.onclick = async () => {

                const password = prompt(
                    '댓글 비밀번호를 입력하세요.'
                );

                if (password === null) return;

                if (!password) {

                    alert('비밀번호를 입력해주세요.');

                    return;

                }


                if (!confirm('댓글을 삭제하시겠습니까?')) {
                    return;
                }


                try {

                    await api(
                        COMMENT.one(button.dataset.delete),
                        {
                            method: 'DELETE',
                            headers: {
                                'Content-Type': 'application/json'
                            },
                            body: JSON.stringify({
                                password: password
                            })
                        }
                    );

                    await loadComments();

                } catch (e) {

                    alert(e.message);

                }

            };

        });


    } catch (e) {

        box.innerHTML = `
            <div class="empty">
                ${esc(e.message)}
            </div>
        `;

    }

}


document.addEventListener('DOMContentLoaded', () => {

    const form = document.getElementById('commentForm');

    if (!form) return;


    loadComments();


    form.addEventListener('submit', async e => {

        e.preventDefault();


        const dailyId =
            new URLSearchParams(location.search).get('id');

        if (!dailyId) {

            alert('Daily ID를 찾을 수 없습니다.');

            return;

        }


        const contentEl =
            document.getElementById('commentContent');

        const passwordEl =
            document.getElementById('commentPassword');


        const content =
            contentEl.value.trim();

        const password =
            passwordEl.value;


        if (!content) {

            alert('댓글 내용을 입력해주세요.');

            contentEl.focus();

            return;

        }


        if (content.length > 100) {

            alert('댓글은 최대 100자까지 입력할 수 있습니다.');

            contentEl.focus();

            return;

        }


        if (!password) {

            alert('댓글 비밀번호를 입력해주세요.');

            passwordEl.focus();

            return;

        }


        try {

            await api(
                `/api/comments/${dailyId}`,
                {
                    method: 'POST',

                    headers: {
                        'Content-Type': 'application/json'
                    },

                    body: JSON.stringify({
                        content: content,
                        password: password
                    })
                }
            );


            // 입력창 초기화
            contentEl.value = '';
            passwordEl.value = '';


            // 댓글 다시 불러오기
            await loadComments();


        } catch (err) {

            alert(err.message);

        }

    });

});