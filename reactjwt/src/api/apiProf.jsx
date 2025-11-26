// src/api/profApi.js
const API_BASE = "http://localhost:8080"; // <— sans slash final

async function handleFetch(url, options = {}) {
    try {
        const res = await fetch(url, options);
        const ct = res.headers.get("content-type") || "";
        const text = await res.text();
        const data = text && ct.includes("application/json") ? JSON.parse(text) : text || null;
        if (!res.ok) {
            const msg = typeof data === "string" ? data : data?.error || `Erreur ${res.status}`;
            throw {response: {data: msg}};
        }
        return data ?? [];
    } catch (err) {
        if (err?.response) throw err;
        throw {response: {data: err?.message || "Impossible de se connecter au serveur"}};
    }
}

function authHeaders() {
    const token = sessionStorage.getItem("accessToken");
    const h = {Accept: "application/json"};
    if (token) h.Authorization = `Bearer ${token}`;
    return h;
}

export async function fetchProfStudents(profId, params = {}) {
    const q = new URLSearchParams();
    if (params.name) q.set("nom", params.name);
    if (params.company) q.set("entreprise", params.company);
    if (params.dateFrom) q.set("dateFrom", params.dateFrom);
    if (params.dateTo) q.set("dateTo", params.dateTo);
    if (params.evaluationStatus) q.set("evaluationStatus", params.evaluationStatus);
    q.set("sortBy", params.sortBy || "name");
    q.set("asc", String(params.asc ?? true));

    const url = `${API_BASE}/prof/${profId}/etudiants?${q.toString()}`;
    return handleFetch(url, {method: "GET", headers: authHeaders()});
}

export async function getProfMe(token = null) {
    const accessToken = token || sessionStorage.getItem("accessToken");
    const headers = {
        'Content-Type': 'application/json'
    };
    if (accessToken) {
        headers['Authorization'] = `Bearer ${accessToken}`;
    }
    const res = await fetch(`${API_BASE}/user/me`, {
        method: 'GET',
        headers
    });
    const ct = res.headers.get("content-type") || "";
    const text = await res.text();
    const data = text && ct.includes("application/json") ? JSON.parse(text) : text || null;
    if (!res.ok) {
        const msg = typeof data === "string" ? data : data?.error || `Erreur ${res.status}`;
        throw {response: {data: msg}};
    }
    return data ?? null;
}
