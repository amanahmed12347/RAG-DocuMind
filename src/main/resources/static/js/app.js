requireAuth();

const messages = document.getElementById('messages');
const questionForm = document.getElementById('questionForm');
const questionInput = document.getElementById('questionInput');
const sendBtn = document.getElementById('sendBtn');
const fileInput = document.getElementById('fileInput');
const uploadZone = document.getElementById('uploadZone');
const uploadStatus = document.getElementById('uploadStatus');
const statusFileName = document.getElementById('statusFileName');
const statusDetails = document.getElementById('statusDetails');
const documentList = document.getElementById('documentList');
const userName = document.getElementById('userName');
const userAvatar = document.getElementById('userAvatar');

const user = getUser();
if (user) {
    userName.textContent = user.name;
    userAvatar.textContent = user.name.charAt(0).toUpperCase();
}

let documents = [];

// Upload handlers
uploadZone.addEventListener('click', () => fileInput.click());

uploadZone.addEventListener('dragover', (e) => {
    e.preventDefault();
    uploadZone.classList.add('dragover');
});

uploadZone.addEventListener('dragleave', () => {
    uploadZone.classList.remove('dragover');
});

uploadZone.addEventListener('drop', (e) => {
    e.preventDefault();
    uploadZone.classList.remove('dragover');
    const files = e.dataTransfer.files;
    if (files.length > 0) uploadFile(files[0]);
});

fileInput.addEventListener('change', (e) => {
    if (e.target.files.length > 0) uploadFile(e.target.files[0]);
});

async function uploadFile(file) {
    if (!file.name.toLowerCase().endsWith('.pdf')) {
        alert('Only PDF files are supported.');
        return;
    }

    const formData = new FormData();
    formData.append('file', file);

    uploadStatus.hidden = true;
    const originalText = uploadZone.querySelector('p').textContent;
    uploadZone.querySelector('p').textContent = 'Uploading...';

    try {
        const res = await authFetch('/upload', {
            method: 'POST',
            body: formData
        });

        if (!res.ok) {
            const err = await res.text();
            throw new Error(err || 'Upload failed');
        }

        const data = await res.json();

        statusFileName.textContent = data.fileName;
        statusDetails.textContent = `${data.chunksStored} chunk${data.chunksStored !== 1 ? 's' : ''} stored`;
        uploadStatus.hidden = false;

        addDocument(data.fileName, data.chunksStored);

        if (documents.length === 1) {
            questionInput.disabled = false;
            sendBtn.disabled = false;
            questionInput.focus();
        }
    } catch (err) {
        alert('Upload failed: ' + err.message);
    } finally {
        uploadZone.querySelector('p').textContent = originalText;
        fileInput.value = '';
    }
}

function addDocument(name, chunks) {
    if (documents.includes(name)) return;
    documents.push(name);

    const empty = documentList.querySelector('.empty-state');
    if (empty) empty.remove();

    const item = document.createElement('div');
    item.className = 'document-item';
    item.innerHTML = `
        <span class="doc-icon">
            <svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
                <path d="M14 2H6a2 2 0 0 0-2 2v16a2 2 0 0 0 2 2h12a2 2 0 0 0 2-2V8z"/>
                <polyline points="14 2 14 8 20 8"/>
            </svg>
        </span>
        <span class="doc-name">${name}</span>
        <span class="doc-chunks">${chunks} chunks</span>
    `;
    documentList.appendChild(item);
}

// Messaging
questionForm.addEventListener('submit', async (e) => {
    e.preventDefault();
    const question = questionInput.value.trim();
    if (!question) return;

    addMessage(question, 'user');
    questionInput.value = '';
    questionInput.disabled = true;
    sendBtn.disabled = true;

    const loadingId = addLoadingMessage();

    try {
        const res = await authFetch('/ask', {
            method: 'POST',
            headers: { 'Content-Type': 'application/json' },
            body: JSON.stringify({ question, topK: 5 })
        });

        removeMessage(loadingId);

        if (!res.ok) {
            const err = await res.text();
            throw new Error(err || 'Request failed');
        }

        const data = await res.json();
        addBotMessage(data.answer, data.sources);
    } catch (err) {
        removeMessage(loadingId);
        addMessage('Error: ' + err.message, 'user');
    } finally {
        questionInput.disabled = false;
        sendBtn.disabled = false;
        questionInput.focus();
    }
});

function addMessage(text, sender) {
    const msg = document.createElement('div');
    msg.className = `message ${sender}`;
    msg.innerHTML = `
        <div class="avatar ${sender}">
            ${sender === 'user'
                ? `<svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
                    <path d="M20 21v-2a4 4 0 00-4-4H8a4 4 0 00-4 4v2"/>
                    <circle cx="12" cy="7" r="4"/>
                   </svg>`
                : `<svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
                    <path d="M12 2a4 4 0 014 4v2a4 4 0 01-8 0V6a4 4 0 014-4z"/>
                    <path d="M20 18v2a4 4 0 01-4 4H8a4 4 0 01-4-4v-2"/>
                    <circle cx="12" cy="12" r="3"/>
                   </svg>`
            }
        </div>
        <div class="bubble"><p>${text}</p></div>
    `;
    messages.appendChild(msg);
    scrollToBottom();
    return msg;
}

function addBotMessage(answer, sources) {
    const msg = document.createElement('div');
    msg.className = 'message bot';

    let sourcesHtml = '';
    if (sources && sources.length > 0) {
        sourcesHtml = `<details class="sources">
            <summary>Sources (${sources.length})</summary>
            ${sources.map(s => `
                <div class="source-item">
                    <strong>${s.documentName}</strong> (chunk ${s.chunkIndex})
                    <span class="source-score">relevance: ${(s.score * 100).toFixed(0)}%</span>
                </div>
            `).join('')}
        </details>`;
    }

    msg.innerHTML = `
        <div class="avatar bot">
            <svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
                <path d="M12 2a4 4 0 014 4v2a4 4 0 01-8 0V6a4 4 0 014-4z"/>
                <path d="M20 18v2a4 4 0 01-4 4H8a4 4 0 01-4-4v-2"/>
                <circle cx="12" cy="12" r="3"/>
            </svg>
        </div>
        <div class="bubble">
            <p>${answer}</p>
            ${sourcesHtml}
        </div>
    `;
    messages.appendChild(msg);
    scrollToBottom();
}

function addLoadingMessage() {
    const msg = document.createElement('div');
    msg.className = 'message bot';
    msg.id = 'loading-' + Date.now();
    msg.innerHTML = `
        <div class="avatar bot">
            <svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
                <path d="M12 2a4 4 0 014 4v2a4 4 0 01-8 0V6a4 4 0 014-4z"/>
                <path d="M20 18v2a4 4 0 01-4 4H8a4 4 0 01-4-4v-2"/>
                <circle cx="12" cy="12" r="3"/>
            </svg>
        </div>
        <div class="bubble">
            <div class="thinking">
                <span></span><span></span><span></span>
            </div>
        </div>
    `;
    messages.appendChild(msg);
    scrollToBottom();
    return msg.id;
}

function removeMessage(id) {
    const el = document.getElementById(id);
    if (el) el.remove();
}

function scrollToBottom() {
    messages.scrollTop = messages.scrollHeight;
}
