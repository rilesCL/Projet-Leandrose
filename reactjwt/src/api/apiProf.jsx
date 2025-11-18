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
            throw { response: { data: msg } };
        }
        return data ?? [];
    } catch (err) {
        if (err?.response) throw err;
        throw { response: { data: err?.message || "Impossible de se connecter au serveur" } };
    }
}

function authHeaders() {
    const token = sessionStorage.getItem("accessToken");
    const h = { Accept: "application/json" };
    if (token) h.Authorization = `Bearer ${token}`;
    return h;
}

export async function fetchProfStudents(profId, params = {}) {
    const q = new URLSearchParams();
    if (params.name) q.set("nom", params.name);
    if (params.company) q.set("entreprise", params.company);
    if (params.dateFrom) q.set("dateFrom", params.dateFrom);           // yyyy-mm-dd
    if (params.dateTo) q.set("dateTo", params.dateTo);                 // yyyy-mm-dd
    if (params.evaluationStatus) q.set("evaluationStatus", params.evaluationStatus); // A_FAIRE|EN_COURS|TERMINEE
    q.set("sortBy", params.sortBy || "name");                          // name|date|company
    q.set("asc", String(params.asc ?? true));

    const url = `${API_BASE}/prof/${profId}/etudiants?${q.toString()}`;
    return handleFetch(url, { method: "GET", headers: authHeaders() });
}

export async function getEligibleEvaluations(token = null) {
    const res = await handleFetch(`${API_BASE}/prof/evaluations/eligible`, {
        headers: authHeaders(token)
    });
    return await res.json();
}
export async function checkTeacherAssigned(studentId, offerId, token = null){
    const res = await handleFetch(
        `${API_BASE}/employeur/evaluations/check-teacher-assigned?studentId=${studentId}&offerId=${offerId}`, {
            method: 'GET',
            headers: {
                ...authHeaders(token)
            }
        })
    return await res.json();
}
export async function checkExistingEvaluation(studentId, offerId, token = null) {
    const res = await handleFetch(`${API_BASE}/prof/evaluations/check-existing?studentId=${studentId}&offerId=${offerId}`, {
        headers: authHeaders(token)
    });
    return await res.json();
}
export async function previewEvaluationPdf (evaluationId, formData = null, token = null) {
    if (formData) {
        const res = await handleFetch(`${API_BASE}/prof/evaluations/${evaluationId}/preview-pdf`, {
            method: 'POST',
            headers: {
                'Content-Type': 'application/json',
                ...authHeaders(token)
            },
            body: JSON.stringify(formData)
        });
        return await res.blob();
    } else {
        const res = await handleFetch(`${API_BASE}/prof/evaluations/${evaluationId}/pdf`, {
            headers: authHeaders(token)
        });
        return await res.blob();
    }
}
