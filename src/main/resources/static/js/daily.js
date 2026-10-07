const DAILY = {
    list: '/api/dailies',

    one: id =>
        `/api/dailies/${id}`,

    like: id =>
        `/api/dailies/${id}/likes`
};


/* ==================================================
   Daily 목록
================================================== */

async function loadDailies() {

    const box =
        document.getElementById('dailyList');

    if (!box) return;


    box.innerHTML =
        '<div class="loading">불러오는 중...</div>';


    try {

        const r =
            await api(DAILY.list);

        const data =
            dataOf(r);


        /*
          서버 응답

          {
            code: 200,
            data: {
              content: [...],
              pageInfo: {...}
            },
            success: true
          }
        */

        const arr =
            Array.isArray(data)
                ? data
                : Array.isArray(data?.content)
                    ? data.content
                    : [];


        if (arr.length === 0) {

            box.innerHTML =
                '<div class="empty">작성된 Daily가 없습니다.</div>';

            return;
        }


        box.innerHTML =
            arr.map(d => {

                const id =
                    d.id ?? d.dailyId;


                return `
          <a
            class="daily-item"
            href="daily-detail.html?id=${encodeURIComponent(id)}">

            <h3>
              ${esc(d.title || '제목 없음')}
            </h3>

            <p>
              ${esc(d.content || '')}
            </p>

            <div class="meta">
              ${esc(authorName(d.author))}
              · ${esc(fmtDate(d.createdAt))}
              · ♥ ${d.likes ?? 0}
            </div>

          </a>
        `;

            }).join('');


    } catch (e) {

        console.error(
            'Daily 목록 불러오기 오류:',
            e
        );


        box.innerHTML = `
      <div class="empty">
        ${esc(
            e.message ||
            'Daily를 불러오지 못했습니다.'
        )}
      </div>
    `;
    }
}


/* ==================================================
   Daily 상세
================================================== */

async function loadDailyDetail() {

    const box =
        document.getElementById('dailyDetail');

    if (!box) return;


    const id =
        new URLSearchParams(
            location.search
        ).get('id');


    if (!id) {

        box.innerHTML =
            '<div class="empty">Daily ID가 없습니다.</div>';

        return;
    }


    try {

        const r =
            await api(DAILY.one(id));

        const d =
            dataOf(r);


        box.innerHTML = `

        <h1>
            ${esc(d.title || '제목 없음')}
        </h1>

        <div class="meta">

            ${esc(authorName(d.author))}

            ·

            ${esc(fmtDate(d.createdAt))}

            ·

            ♥ ${d.likes ?? 0}

        </div>


        <div class="detail-content">
            ${esc(d.content || '')}
        </div>


        <div class="detail-actions">

            <button
                class="secondary"
                id="likeDailyBtn">
                ♥ 좋아요
            </button>


            <button
                class="secondary"
                id="editDailyBtn">
                수정
            </button>


            <button
                class="secondary"
                id="deleteDailyBtn">
                삭제
            </button>

        </div>
    `;


        /* =========================
           좋아요
        ========================= */

        const likeBtn =
            document.getElementById('likeDailyBtn');

        likeBtn?.addEventListener('click', async () => {

            try {

                await api(DAILY.like(id), {
                    method: 'POST'
                });

                await loadDailyDetail();

            } catch (e) {

                console.error(
                    'Daily 좋아요 오류:',
                    e
                );

                alert(
                    e.message ||
                    '좋아요 처리에 실패했습니다.'
                );
            }
        });


        /* =========================
           수정
        ========================= */

        const editBtn =
            document.getElementById('editDailyBtn');

        editBtn?.addEventListener('click', () => {

            openEditDailyDialog(d);

        });


        /* =========================
           삭제
        ========================= */

        const deleteBtn =
            document.getElementById('deleteDailyBtn');

        deleteBtn?.addEventListener('click', async () => {

            if (!confirm(
                '정말 이 Daily를 삭제하시겠습니까?'
            )) {
                return;
            }


            const password =
                prompt(
                    'Daily 삭제 비밀번호를 입력해주세요.'
                );


            // 취소
            if (password === null) {
                return;
            }


            // 빈 비밀번호
            if (!password.trim()) {

                alert(
                    '비밀번호를 입력해주세요.'
                );

                return;
            }


            try {

                await api(DAILY.one(id), {

                    method: 'DELETE',

                    headers: {
                        'Content-Type':
                            'application/json'
                    },

                    body: JSON.stringify({
                        password: password
                    })

                });


                alert(
                    'Daily가 삭제되었습니다.'
                );


                // 삭제 후 목록으로 이동
                location.href = 'daily.html';


            } catch (e) {

                console.error(
                    'Daily 삭제 오류:',
                    e
                );

                alert(
                    e.message ||
                    'Daily 삭제에 실패했습니다.'
                );
            }

        });


    } catch (e) {

        console.error(
            'Daily 상세 조회 오류:',
            e
        );

        box.innerHTML =
            '<div class="empty">Daily를 불러오지 못했습니다.</div>';
    }
}


/* ==================================================
   Daily 작성
================================================== */

async function createDaily() {

    const titleEl =
        document.getElementById('dailyTitle');

    const contentEl =
        document.getElementById('dailyContent');

    const passwordEl =
        document.getElementById('dailyPassword');

    const saveBtn =
        document.getElementById('saveDailyBtn');

    const dialog =
        document.getElementById('dailyDialog');


    if (
        !titleEl ||
        !contentEl ||
        !passwordEl
    ) {

        alert(
            'Daily 작성 입력칸을 찾을 수 없습니다.'
        );

        return;
    }


    const title =
        titleEl.value.trim();

    const content =
        contentEl.value.trim();

    const password =
        passwordEl.value;


    if (!title) {

        alert('제목을 입력해주세요.');

        titleEl.focus();

        return;
    }


    if (!content) {

        alert('내용을 입력해주세요.');

        contentEl.focus();

        return;
    }


    if (!password) {

        alert('비밀번호를 입력해주세요.');

        passwordEl.focus();

        return;
    }


    try {

        if (saveBtn) {

            saveBtn.disabled = true;

            saveBtn.textContent =
                '작성 중...';
        }


        await api(
            DAILY.list,
            {
                method: 'POST',

                headers: {
                    'Content-Type':
                        'application/json'
                },

                body: JSON.stringify({
                    title,
                    content,
                    password
                })
            }
        );


        titleEl.value = '';

        contentEl.value = '';

        passwordEl.value = '';


        if (dialog?.open) {

            dialog.close();
        }


        await loadDailies();


        alert(
            'Daily가 작성되었습니다.'
        );


    } catch (e) {

        console.error(
            'Daily 작성 오류:',
            e
        );


        alert(
            e.message ||
            'Daily 작성에 실패했습니다.'
        );


    } finally {

        if (saveBtn) {

            saveBtn.disabled = false;

            saveBtn.textContent =
                '작성';
        }
    }
}


/* ==================================================
   Daily 작성 Dialog
================================================== */

function initDailyDialog() {

    const dialog =
        document.getElementById(
            'dailyDialog'
        );

    const form =
        document.getElementById(
            'dailyForm'
        );

    const newBtn =
        document.getElementById(
            'newDailyBtn'
        );

    const closeBtn =
        document.getElementById(
            'closeDailyDialog'
        );

    const cancelBtn =
        document.getElementById(
            'cancelDailyBtn'
        );


    if (!dialog) return;


    newBtn?.addEventListener(
        'click',
        () => {

            dialog.showModal();

        }
    );


    closeBtn?.addEventListener(
        'click',
        () => {

            dialog.close();

        }
    );


    cancelBtn?.addEventListener(
        'click',
        () => {

            dialog.close();

        }
    );


    form?.addEventListener(
        'submit',
        async e => {

            e.preventDefault();

            await createDaily();

        }
    );


    dialog.addEventListener(
        'cancel',
        e => {

            e.preventDefault();

            dialog.close();

        }
    );
}


/* ==================================================
   Daily 수정 Dialog 열기
================================================== */

function openEditDailyDialog(d) {

    const dialog =
        document.getElementById(
            'editDailyDialog'
        );

    const titleEl =
        document.getElementById(
            'editDailyTitle'
        );

    const contentEl =
        document.getElementById(
            'editDailyContent'
        );

    const passwordEl =
        document.getElementById(
            'editDailyPassword'
        );


    if (
        !dialog ||
        !titleEl ||
        !contentEl ||
        !passwordEl
    ) {

        alert(
            'Daily 수정 창을 찾을 수 없습니다.'
        );

        return;
    }


    titleEl.value =
        d.title || '';

    contentEl.value =
        d.content || '';

    passwordEl.value =
        '';


    dialog.showModal();
}


/* ==================================================
   Daily 수정 Dialog 초기화
================================================== */

function initEditDailyDialog() {

    const dialog =
        document.getElementById(
            'editDailyDialog'
        );

    const form =
        document.getElementById(
            'editDailyForm'
        );

    const closeBtn =
        document.getElementById(
            'closeEditDailyDialog'
        );

    const cancelBtn =
        document.getElementById(
            'cancelEditDailyBtn'
        );


    if (!dialog) return;


    closeBtn?.addEventListener(
        'click',
        () => {

            dialog.close();

        }
    );


    cancelBtn?.addEventListener(
        'click',
        () => {

            dialog.close();

        }
    );


    dialog.addEventListener(
        'cancel',
        e => {

            e.preventDefault();

            dialog.close();

        }
    );


    /*
      여기까지 UI는 완성.

      실제 PUT/PATCH 요청은
      백엔드의 Daily 수정 API가 확인되면
      아래 submit 부분에 정확히 연결한다.
    */

    form?.addEventListener(
        'submit',
        async e => {

            e.preventDefault();

            const id =
                new URLSearchParams(location.search).get('id');

            const titleEl =
                document.getElementById('editDailyTitle');

            const contentEl =
                document.getElementById('editDailyContent');

            const passwordEl =
                document.getElementById('editDailyPassword');

            const title =
                titleEl?.value.trim();

            const content =
                contentEl?.value.trim();

            const password =
                passwordEl?.value;


            if (!id) {
                alert('Daily ID가 없습니다.');
                return;
            }

            if (!title) {
                alert('제목을 입력해주세요.');
                titleEl?.focus();
                return;
            }

            if (!content) {
                alert('내용을 입력해주세요.');
                contentEl?.focus();
                return;
            }

            if (!password) {
                alert('비밀번호를 입력해주세요.');
                passwordEl?.focus();
                return;
            }


            try {

                await api(
                    DAILY.one(id),
                    {
                        method: 'PATCH',

                        headers: {
                            'Content-Type': 'application/json'
                        },

                        body: JSON.stringify({
                            title,
                            content,
                            password
                        })
                    }
                );


                dialog.close();

                await loadDailyDetail();

                alert('Daily가 수정되었습니다.');


            } catch (e) {

                console.error(
                    'Daily 수정 오류:',
                    e
                );

                alert(
                    e.message ||
                    'Daily 수정에 실패했습니다.'
                );
            }
        }
    );
}


/* ==================================================
   초기화
================================================== */

document.addEventListener(
    'DOMContentLoaded',
    () => {

        if (!requireLogin()) {
            return;
        }


        initHeader();


        loadDailies();


        loadDailyDetail();


        initDailyDialog();


        initEditDailyDialog();

    }
);