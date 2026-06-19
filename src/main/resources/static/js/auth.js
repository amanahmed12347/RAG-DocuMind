const TOKEN_KEY = 'rag_chatbot_token';
const USER_KEY = 'rag_chatbot_user';

function getToken() {
    return localStorage.getItem(TOKEN_KEY);
}

function getUser() {
    const data = localStorage.getItem(USER_KEY);
    return data ? JSON.parse(data) : null;
}

function setAuth(token, email, name) {
    localStorage.setItem(TOKEN_KEY, token);
    localStorage.setItem(USER_KEY, JSON.stringify({ email, name }));
}

function clearAuth() {
    localStorage.removeItem(TOKEN_KEY);
    localStorage.removeItem(USER_KEY);
}

function isAuthenticated() {
    return !!getToken();
}

function requireAuth() {
    if (!isAuthenticated()) {
        window.location.href = '/login.html';
    }
}

async function authFetch(url, options = {}) {
    const token = getToken();
    const headers = { ...options.headers };

    if (token) {
        headers['Authorization'] = `Bearer ${token}`;
    }

    if (!headers['Content-Type'] && !(options.body instanceof FormData)) {
        headers['Content-Type'] = 'application/json';
    }

    const res = await fetch(url, { ...options, headers });

    if (res.status === 401 || res.status === 403) {
        clearAuth();
        window.location.href = '/login.html';
        throw new Error('Unauthorized');
    }

    return res;
}

function logout() {
    clearAuth();
    window.location.href = '/login.html';
}
