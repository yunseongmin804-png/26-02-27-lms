(() => {
  const toggleBtn = document.getElementById('chatbot-toggle');
  const panel = document.getElementById('chatbot-panel');
  const closeBtn = document.getElementById('chatbot-close');
  const form = document.getElementById('chatbot-form');
  const input = document.getElementById('chatbot-input');
  const messages = document.getElementById('chatbot-messages');

  if (!toggleBtn || !panel || !form || !input || !messages) {
    return;
  }

  const appendMessage = (text, sender) => {
    const div = document.createElement('div');
    div.className = `chatbot-msg ${sender}`;
    div.textContent = text;
    messages.appendChild(div);
    messages.scrollTop = messages.scrollHeight;
  };

  const setOpen = (open) => {
    panel.classList.toggle('open', open);
    if (open) {
      input.focus();
    }
  };

  toggleBtn.addEventListener('click', () => setOpen(!panel.classList.contains('open')));
  if (closeBtn) {
    closeBtn.addEventListener('click', () => setOpen(false));
  }

  form.addEventListener('submit', async (event) => {
    event.preventDefault();
    const text = input.value.trim();
    if (!text) return;

    appendMessage(text, 'user');
    input.value = '';
    input.disabled = true;

    try {
      const response = await fetch('/api/chatbot/message', {
        method: 'POST',
        headers: {
          'Content-Type': 'application/json'
        },
        body: JSON.stringify({ message: text })
      });

      const payload = await response.json().catch(() => null);

      if (!response.ok) {
        const detail = payload?.error?.message || `HTTP ${response.status}`;
        throw new Error(detail);
      }

      const answer = payload?.data?.answer ?? '죄송해요. 답변을 받아오지 못했어요.';
      appendMessage(answer, 'bot');
    } catch (error) {
      console.error(error);
      appendMessage(`챗봇 연결 실패: ${error.message}`, 'bot');
    } finally {
      input.disabled = false;
      input.focus();
    }
  });
})();
