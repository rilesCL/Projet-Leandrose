// src/api/apiGestionnaire.js
// Client API pour les endpoints "gestionnaire"
const API_BASE = "http://localhost:8080/gestionnaire";

/**
 * Prépare les headers d'auth (Bearer token) si présent en sessionStorage.
 * Retourne un objet pouvant être spread dans fetch(..., { headers: ... }).
 */
function getAuthHeaders(contentType = "application/json") {
    const accessToken = sessionStorage.getItem("accessToken");
    const tokenType = (sessionStorage.getItem("tokenType") || "BEARER").toUpperCase();
    const headers = {};
    if (contentType) headers["Content-Type"] = contentType;
    if (!accessToken) return headers;
    headers["Authorization"] = tokenType.startsWith("BEARER") ? `Bearer ${accessToken}` : accessToken;
    return headers;
}

/**
 * Lit la réponse HTTP et parse le JSON. En cas d'erreur (status !ok) lève une Error
 * contenant le message si disponible (body JSON.message ou texte brut).
 */
async function handleJsonResponse(response) {
    const contentType = response.headers.get("content-type") || "";
    const text = await response.text();
    if (!response.ok) {
        // essayer d'extraire message JSON
        try {
            if (contentType.includes("application/json") && text) {
                const json = JSON.parse(text);
                throw new Error(json?.message || JSON.stringify(json));
            }
        } catch (e) {
            // fallback
        }
        throw new Error(text || `HTTP error ${response.status}`);
    }
    if (contentType.includes("application/json")) {
        return text ? JSON.parse(text) : null;
    }
    return text;
}

/**
 * Lit la réponse et retourne un Blob (utile pour les PDFs).
 * En cas d'erreur lève une Error.
 */
async function handleBlobResponse(response) {
    if (!response.ok) {
        const text = await response.text();
        throw new Error(text || `HTTP error ${response.status}`);
    }
    return await response.blob();
}

/* ------------------------
   Endpoints: Candidatures
   ------------------------ */

/**
 * Récupère les candidatures acceptées qui n'ont pas encore d'entente.
 * Endpoint backend attendu (exemple) : GET /gestionnaire/ententes/candidatures-acceptees
 */
export async function getCandidaturesAcceptees() {
    const url = `${API_BASE}/ententes/candidatures/accepted`;
    const res = await fetch(url, { method: "GET", headers: getAuthHeaders() });
    return await handleJsonResponse(res);
}

/* ------------------------
   Endpoints: CV (Gestionnaire)
   ------------------------ */

/**
 * Télécharge (preview) le CV (blob).
 * GET /gestionnaire/cv/{cvId}/download
 */
export async function previewCv(cvId) {
    const url = `${API_BASE}/cv/${cvId}/download`;
    const res = await fetch(url, { method: "GET", headers: getAuthHeaders() });
    return await handleBlobResponse(res);
}

/**
 * Récupère les CVs en attente
 * GET /gestionnaire/cvs/pending
 */
export async function getPendingCvs() {
    const url = `${API_BASE}/cvs/pending`;
    const res = await fetch(url, { method: "GET", headers: getAuthHeaders() });
    return await handleJsonResponse(res);
}

/**
 * Approuve un CV
 * POST /gestionnaire/cv/{cvId}/approve
 */
export async function approveCv(cvId) {
    const url = `${API_BASE}/cv/${cvId}/approve`;
    const res = await fetch(url, { method: "POST", headers: getAuthHeaders() });
    return await handleJsonResponse(res);
}

/**
 * Rejette un CV avec commentaire
 * POST /gestionnaire/cv/{cvId}/reject
 * body: string (comment) ou { comment: "..." } selon backend
 */
export async function rejectCv(cvId, comment) {
    const url = `${API_BASE}/cv/${cvId}/reject`;
    // si backend attend une chaîne brute, envoyer JSON.stringify(comment) ; si attend objet, envoyer { comment }
    const res = await fetch(url, {
        method: "POST",
        headers: getAuthHeaders(),
        body: JSON.stringify(comment),
    });
    return await handleJsonResponse(res);
}

/* ------------------------
   Endpoints: Offres (Gestionnaire)
   ------------------------ */

/**
 * Helper interne
 */
async function fetchWithAuthJson(url) {
    const res = await fetch(url, { method: "GET", headers: getAuthHeaders() });
    return await handleJsonResponse(res);
}

/**
 * GET /gestionnaire/offers/approved
 */
export async function getApprovedOffers() {
    return await fetchWithAuthJson(`${API_BASE}/offers/approved`);
}

/**
 * GET /gestionnaire/offers/reject
 */
export async function getRejectedOffers() {
    return await fetchWithAuthJson(`${API_BASE}/offers/reject`);
}

/**
 * GET /gestionnaire/offers/{id}
 */
export async function getOfferDetails(id) {
    return await fetchWithAuthJson(`${API_BASE}/offers/${id}`);
}

/**
 * GET /gestionnaire/offers/pending
 */
export async function getPendingOffers() {
    return await fetchWithAuthJson(`${API_BASE}/offers/pending`);
}

/**
 * POST /gestionnaire/offers/{offerId}/approve
 */
export async function approveOffer(offerId) {
    const url = `${API_BASE}/offers/${offerId}/approve`;
    const res = await fetch(url, { method: "POST", headers: getAuthHeaders() });
    return await handleJsonResponse(res);
}

/**
 * POST /gestionnaire/offers/{offerId}/reject
 * body: { comment: "..." }
 */
export async function rejectOffer(offerId, comment) {
    const url = `${API_BASE}/offers/${offerId}/reject`;
    const res = await fetch(url, {
        method: "POST",
        headers: getAuthHeaders(),
        body: JSON.stringify({ comment }),
    });
    return await handleJsonResponse(res);
}

/**
 * GET /gestionnaire/offers/{offerId}/pdf  -> retourne un blob pdf
 */
export async function previewOfferPdf(offerId) {
    const url = `${API_BASE}/offers/${offerId}/pdf`;
    const res = await fetch(url, { method: "GET", headers: getAuthHeaders() });
    return await handleBlobResponse(res);
}

/* ------------------------
   Endpoints: Ententes
   ------------------------ */

/**
 * POST /gestionnaire/ententes
 * body: EntenteStageDto (JSON)
 * Retourne l'entente créée.
 */
export async function creerEntente(ententeDto) {
    const url = `${API_BASE}/ententes`;
    const res = await fetch(url, {
        method: "POST",
        headers: getAuthHeaders(),
        body: JSON.stringify(ententeDto),
    });
    return await handleJsonResponse(res);
}

// Replace the fetchAgreements function in apiGestionnaire.jsx with this:

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