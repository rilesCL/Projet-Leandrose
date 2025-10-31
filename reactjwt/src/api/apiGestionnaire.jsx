const API_BASE = "http://localhost:8080/gestionnaire";

function getAuthHeaders(contentType = "application/json") {
    const accessToken = sessionStorage.getItem("accessToken");
    const tokenType = (sessionStorage.getItem("tokenType") || "BEARER").toUpperCase();
    const headers = {};
    if (contentType) headers["Content-Type"] = contentType;
    if (!accessToken) return headers;
    headers["Authorization"] = tokenType.startsWith("BEARER") ? `Bearer ${accessToken}` : accessToken;
    return headers;
}


async function handleJsonResponse(response) {
    const contentType = response.headers.get("content-type") || "";
    const text = await response.text();
    if (!response.ok) {
        try {
            if (contentType.includes("application/json") && text) {
                const json = JSON.parse(text);
                throw new Error(json?.message || JSON.stringify(json));
            }
        } catch (e) {
        }
        throw new Error(text || `HTTP error ${response.status}`);
    }
    if (contentType.includes("application/json")) {
        return text ? JSON.parse(text) : null;
    }
    return text;
}

async function handleBlobResponse(response) {
    if (!response.ok) {
        const text = await response.text();
        throw new Error(text || `HTTP error ${response.status}`);
    }
    return await response.blob();
}

export async function getCandidaturesAcceptees() {
    const url = `${API_BASE}/ententes/candidatures/accepted`;
    const res = await fetch(url, { method: "GET", headers: getAuthHeaders() });
    return await handleJsonResponse(res);
}

export async function previewCv(cvId) {
    const url = `${API_BASE}/cv/${cvId}/download`;
    const res = await fetch(url, { method: "GET", headers: getAuthHeaders() });
    return await handleBlobResponse(res);
}


export async function getPendingCvs() {
    const url = `${API_BASE}/cvs/pending`;
    const res = await fetch(url, { method: "GET", headers: getAuthHeaders() });
    return await handleJsonResponse(res);
}

export async function approveCv(cvId) {
    const url = `${API_BASE}/cv/${cvId}/approve`;
    const res = await fetch(url, { method: "POST", headers: getAuthHeaders() });
    return await handleJsonResponse(res);
}

export async function rejectCv(cvId, comment) {
    const url = `${API_BASE}/cv/${cvId}/reject`;

    const res = await fetch(url, {
        method: "POST",
        headers: getAuthHeaders(),
        body: JSON.stringify(comment),
    });
    return await handleJsonResponse(res);
}

async function fetchWithAuthJson(url) {
    const res = await fetch(url, { method: "GET", headers: getAuthHeaders() });
    return await handleJsonResponse(res);
}

export async function getApprovedOffers() {
    return await fetchWithAuthJson(`${API_BASE}/offers/approved`);
}

export async function getRejectedOffers() {
    return await fetchWithAuthJson(`${API_BASE}/offers/reject`);
}

export async function getOfferDetails(id) {
    return await fetchWithAuthJson(`${API_BASE}/offers/${id}`);
}

export async function getPendingOffers() {
    return await fetchWithAuthJson(`${API_BASE}/offers/pending`);
}

export async function approveOffer(offerId) {
    const url = `${API_BASE}/offers/${offerId}/approve`;
    const res = await fetch(url, { method: "POST", headers: getAuthHeaders() });
    return await handleJsonResponse(res);
}

export async function rejectOffer(offerId, comment) {
    const url = `${API_BASE}/offers/${offerId}/reject`;
    const res = await fetch(url, {
        method: "POST",
        headers: getAuthHeaders(),
        body: JSON.stringify({ comment }),
    });
    return await handleJsonResponse(res);
}

export async function previewOfferPdf(offerId) {
    const url = `${API_BASE}/offers/${offerId}/pdf`;
    const res = await fetch(url, { method: "GET", headers: getAuthHeaders() });
    return await handleBlobResponse(res);
}

export async function creerEntente(ententeDto) {
    const url = `${API_BASE}/ententes`;
    const res = await fetch(url, {
        method: "POST",
        headers: getAuthHeaders(),
        body: JSON.stringify(ententeDto),
    });
    return await handleJsonResponse(res);
}

export async function fetchAgreements(setEntentes, setLoading, showToast, t) {
    try {
        const token = sessionStorage.getItem("accessToken");

        const userResponse = await fetch('http://localhost:8080/user/me', {
            headers: {
                'Authorization': `Bearer ${token}`,
                'Content-Type': 'application/json'
            }
        });

        if (userResponse.ok) {
            const ententesResponse = await fetch('http://localhost:8080/gestionnaire/ententes', {
                headers: {
                    'Authorization': `Bearer ${token}`,
                    'Content-Type': 'application/json'
                }
            });

            if (ententesResponse.ok) {
                const allEntentes = await ententesResponse.json();
                setEntentes(allEntentes);
            }
        }
    } catch (error) {
        console.error("Error fetching agreements:", error);
        if (showToast && t) {
            showToast(t("ententeStage.errors.loading_agreements"));
        }
    } finally {
        setLoading(false);
    }
}

export async function previewEntentePdf(ententeId) {
    const url = `${API_BASE}/ententes/${ententeId}/telecharger`;
    const res = await fetch(url, { method: "GET", headers: getAuthHeaders() });
    return await handleBlobResponse(res);
}