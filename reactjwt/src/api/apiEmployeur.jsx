const API_BASE = "http://localhost:8080";

async function handleFetch(url, options = {}) {
    try {
        const res = await fetch(url, options);
        if (!res.ok) {
            const errorText = await res.text();
            throw {response: {data: errorText || `Erreur ${res.status}: ${res.statusText}`}};
        }
        return res;
    } catch (error) {
        if (error && error.response) throw error;
        throw {response: {data: error?.message || "Impossible de se connecter au serveur"}};
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
    if (!pdfFile) throw {response: {data: "Fichier PDF manquant"}};

    const url = `${API_BASE}/employeur/offers`;
    const formData = new FormData();
    const offerBlob = new Blob([JSON.stringify(offer)], {type: "application/json"});
    formData.append("offer", offerBlob);
    formData.append("pdfFile", pdfFile);

    const headers = {};
    const accessToken = token || sessionStorage.getItem("accessToken");
    const tokenType = (sessionStorage.getItem("tokenType") || "BEARER").toUpperCase();
    if (accessToken) headers["Authorization"] = tokenType.startsWith("BEARER") ? `Bearer ${accessToken}` : accessToken;

    const res = await handleFetch(url, {method: "POST", headers, body: formData});
    return res.json();
}

export async function getOfferCandidatures(offerId, token = null) {
    const res = await handleFetch(`${API_BASE}/employeur/offers/${offerId}/candidatures`, {headers: authHeaders(token)});
    return res.json();
}

export async function previewCandidateCv(candidatureId, token = null) {
    const res = await handleFetch(`${API_BASE}/employeur/candidatures/${candidatureId}/cv`, {
        headers: authHeaders(token),
    });
    return await res.blob();
}

export async function createConvocation(candidatureId, convocationData, token = null) {
    const headers = {"Content-Type": "application/json", ...authHeaders(token)};
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

export async function previewOfferPdf(offerId, token = null) {
    const res = await handleFetch(`${API_BASE}/employeur/offers/${offerId}/download`, {
        headers: authHeaders(token)
    });
    return await res.blob();
}

export async function createEvaluation(studentId, offerId, token = null) {
    const url = `${API_BASE}/employeur/evaluations`;
    console.log('Calling URL:', url);
    console.log('With data:', {studentId, internshipOfferId: offerId});


    const res = await handleFetch(url, {
        method: 'POST',
        headers: {
            'Content-Type': 'application/json',
            ...authHeaders(token)
        },
        body: JSON.stringify({studentId, internshipOfferId: offerId})
    });
    const responseData = await res.json()
    console.log("Create evaluation Response", responseData)
    return responseData
}

export async function generateEvaluationPdf(studentId, offerId, formData, token = null) {
    const res = await handleFetch(`${API_BASE}/employeur/evaluations/generate-pdf`, {
        method: 'POST',
        headers: {
            'Content-Type': 'application/json',
            ...authHeaders(token)
        },
        body: JSON.stringify({
            studentId: studentId,
            internshipOfferId: offerId,
            formData: formData
        })
    })
    return await res.json()
}

export async function previewEvaluationPdf(evaluationId, formData = null, token = null) {
    if (formData) {
        // Preview with current form data (not saved)
        const res = await handleFetch(`${API_BASE}/employeur/evaluations/${evaluationId}/preview-pdf`, {
            method: 'POST',
            headers: {
                'Content-Type': 'application/json',
                ...authHeaders(token)
            },
            body: JSON.stringify(formData)
        });
        return await res.blob();
    } else {
        const res = await handleFetch(`${API_BASE}/employeur/evaluations/${evaluationId}/pdf`, {
            headers: authHeaders(token)
        });
        return await res.blob();
    }
}

export async function getEligibleEvaluations(token = null) {
    const res = await handleFetch(`${API_BASE}/employeur/evaluations/eligible`, {
        headers: authHeaders(token)
    });
    return await res.json();
}

export async function getEvaluationInfo(studentId, offerId, token = null) {
    const res = await handleFetch(`${API_BASE}/employeur/evaluations/info?studentId=${studentId}&offerId=${offerId}`, {
        headers: authHeaders(token)
    });
    return await res.json();
}

export async function checkTeacherAssigned(studentId, offerId, token = null) {
    const res = await handleFetch(
        `${API_BASE}/employeur/evaluations/check-teacher-assigned?studentId=${studentId}&offerId=${offerId}`, {
            method: 'GET',
            headers: {
                ...authHeaders(token)
            }
        })
    return await res.json();
}

export async function generateEvaluationPdfWithId(evaluationId, formData, token = null) {
    const currentLanguage = localStorage.getItem("i18nextLng")
    const res = await handleFetch(`${API_BASE}/employeur/evaluations/${evaluationId}/generate-pdf`, {
        method: 'POST',
        headers: {
            'Content-Type': 'application/json',
            "Accept-Language": currentLanguage,
            ...authHeaders(token)
        },
        body: JSON.stringify(formData)
    });
    return await res.json();
}

export async function checkExistingEvaluation(studentId, offerId, token = null) {
    const res = await handleFetch(`${API_BASE}/employeur/evaluations/check-existing?studentId=${studentId}&offerId=${offerId}`, {
        headers: authHeaders(token)
    });
    return await res.json();
}

export async function getEmployeurMe(token = null) {
    const accessToken = token || sessionStorage.getItem("accessToken");
    const headers = {
        'Content-Type': 'application/json'
    };
    if (accessToken) {
        headers['Authorization'] = `Bearer ${accessToken}`;
    }
    const res = await handleFetch(`${API_BASE}/user/me`, {
        method: 'GET',
        headers
    });
    return await res.json();
}

export async function getEmployeurEntentes(token = null) {
    const res = await handleFetch(`${API_BASE}/employeur/ententes`, {
        method: 'GET',
        headers: authHeaders(token)
    });
    return await res.json();
}

export async function previewEmployeurEntentePdf(ententeId, token = null) {
    const accessToken = token || sessionStorage.getItem("accessToken");
    const headers = {};
    if (accessToken) headers['Authorization'] = `Bearer ${accessToken}`;
    const res = await handleFetch(`${API_BASE}/employeur/ententes/${ententeId}/pdf`, {
        method: 'GET',
        headers
    });
    return await res.blob();
}

export async function getEmployeurOffers(token = null) {
    const res = await handleFetch(`${API_BASE}/employeur/offers`, {
        method: 'GET',
        headers: authHeaders(token)
    });
    return await res.json();
}

export async function disableOffer(offerId, token){
    const res = await handleFetch(`${API_BASE}/employeur/offers/${offerId}/disable`, {
        method: 'PUT',
        headers: {
            "Content-Type": "application/json",
            ...authHeaders(token)
        }
    })
    return res.json();
}
export async function enableOffer(offerId, token){
    const res = await handleFetch(`${API_BASE}/employeur/offers/${offerId}/enable`, {
        method: 'PUT',
        headers: {
            "Content-Type": "application/json",
            ...authHeaders(token)
        }
    })
    return res.json();
}

