const API_BASE = "http://localhost:8080";

async function handleFetch(url, options = {}) {
    try {
        const res = await fetch(url, options);
        if (!res.ok) {
            const errorText = await res.text();
            throw { response: { data: errorText || `Erreur ${res.status}: ${res.statusText}` } };
        }
        return res;
    } catch (error) {
        if (error && error.response) throw error;
        throw { response: { data: error?.message || "Impossible de se connecter au serveur" } };
    }
}

function authHeaders(token = null) {
    const accessToken = token || sessionStorage.getItem("accessToken");
    const tokenType = (sessionStorage.getItem("tokenType") || "BEARER").toUpperCase();
    const headers = {};
    if (accessToken) headers["Authorization"] = tokenType.startsWith("BEARER") ? `Bearer ${accessToken}` : accessToken;
    return headers;
}

export async function uploadStageEmployeur(offer, pdfFile, token = null) {
    if (!pdfFile) throw { response: { data: "Fichier PDF manquant" } };

    const url = `${API_BASE}/employeur/offers`;
    const formData = new FormData();
    const offerBlob = new Blob([JSON.stringify(offer)], { type: "application/json" });
    formData.append("offer", offerBlob);
    formData.append("pdfFile", pdfFile);

    const headers = {};
    const accessToken = token || sessionStorage.getItem("accessToken");
    const tokenType = (sessionStorage.getItem("tokenType") || "BEARER").toUpperCase();
    if (accessToken) headers["Authorization"] = tokenType.startsWith("BEARER") ? `Bearer ${accessToken}` : accessToken;

    const res = await handleFetch(url, { method: "POST", headers, body: formData });
    return res.json();
}

export async function getEmployerCandidatures(token = null) {
    const res = await handleFetch(`${API_BASE}/employeur/candidatures`, { headers: authHeaders(token) });
    return res.json();
}

export async function getOfferCandidatures(offerId, token = null) {
    const res = await handleFetch(`${API_BASE}/employeur/offers/${offerId}/candidatures`, { headers: authHeaders(token) });
    return res.json();
}

export async function previewCandidateCv(candidatureId, token = null) {
    const res = await handleFetch(`${API_BASE}/employeur/candidatures/${candidatureId}/cv`, {
        headers: authHeaders(token),
    });
    return await res.blob();
}

export async function createConvocation(candidatureId, convocationData, token = null) {
    const headers = { "Content-Type": "application/json", ...authHeaders(token) };
    const res = await handleFetch(`${API_BASE}/employeur/candidatures/${candidatureId}/convocations`, {
        method: "POST",
        headers,
        body: JSON.stringify(convocationData),
    });
    return res.text();
}

export async function acceptCandidature(candidatureId, token = null) {
    const res = await handleFetch(`${API_BASE}/employeur/candidatures/${candidatureId}/accept`, {
        method: "POST",
        headers: authHeaders(token),
    });
    return res.json();
}

export async function rejectCandidature(candidatureId, token = null) {
    const res = await handleFetch(`${API_BASE}/employeur/candidatures/${candidatureId}/reject`, {
        method: "POST",
        headers: authHeaders(token),
    });
    return res.json();
}

// Preview the PDF of an offer
export async function previewOfferPdf(offerId, token = null) {
    const res = await handleFetch(`${API_BASE}/employeur/offers/${offerId}/download`, {
        headers: authHeaders(token)
    });
    return await res.blob();
}

