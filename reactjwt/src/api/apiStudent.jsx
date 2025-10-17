const API_BASE = 'http://localhost:8080';

async function handleApiResponse(response) {
    if (!response.ok) {
        const errorText = await response.text();
        throw new Error(errorText || `Erreur ${response.status}: ${response.statusText}`);
    }
    return response;
}

function getAuthHeaders(token = null) {
    const accessToken = token || sessionStorage.getItem('accessToken');
    const headers = {
        'Content-Type': 'application/json'
    };

    if (accessToken) {
        headers['Authorization'] = `Bearer ${accessToken}`;
    }

    return headers;
}

export async function uploadCvStudent(pdfFile, token = null) {
    if (!pdfFile) {
        throw new Error('Fichier PDF manquant');
    }

    const formData = new FormData();
    formData.append('pdfFile', pdfFile);

    const headers = {};
    const accessToken = token || sessionStorage.getItem('accessToken');
    if (accessToken) {
        headers['Authorization'] = `Bearer ${accessToken}`;
    }

    try {
        const response = await fetch(`${API_BASE}/student/cv`, {
            method: 'POST',
            headers,
            body: formData
        });

        await handleApiResponse(response);
        return await response.json();
    } catch (error) {
        throw new Error(error.message || 'Erreur lors du téléversement du CV');
    }
}

export async function getStudentCv(token = null) {
    try {
        const response = await fetch(`${API_BASE}/student/cv`, {
            method: 'GET',
            headers: getAuthHeaders(token)
        });

        if (response.status === 404) {
            return null;
        }

        await handleApiResponse(response);
        return await response.json();
    } catch (error) {
        throw new Error(error.message || 'Erreur lors de la récupération du CV');
    }
}

export async function getPublishedOffers(token = null) {
    try {
        const response = await fetch(`${API_BASE}/student/offers`, {
            method: 'GET',
            headers: getAuthHeaders(token)
        });

        await handleApiResponse(response);
        return await response.json();
    } catch (error) {
        throw new Error(error.message || 'Erreur lors de la récupération des offres');
    }
}


export async function getOfferDetails(offerId, token = null) {
    try {
        const response = await fetch(`${API_BASE}/student/offers/${offerId}`, {
            method: 'GET',
            headers: getAuthHeaders(token)
        });

        await handleApiResponse(response);
        return await response.json();
    } catch (error) {
        throw new Error(error.message || 'Erreur lors de la récupération des détails');
    }
}

    export async function applyToOffer(offerId, cvId, token = null) {
        try {
            const response = await fetch(`${API_BASE}/student/offers/${offerId}/apply?cvId=${cvId}`, {
                method: 'POST',
                headers: getAuthHeaders(token)
            });

            await handleApiResponse(response);
            return await response.json();
        } catch (error) {
            throw new Error(error.message || 'Erreur lors de la candidature');
        }
    }

export async function getMyConvocations(token = null) {
    const accessToken = token || sessionStorage.getItem('accessToken');
    const tokenType = (sessionStorage.getItem('tokenType') || 'BEARER').toUpperCase();
    const headers = {};

    if (accessToken) {
        headers['Authorization'] = tokenType.startsWith('BEARER') ? `Bearer ${accessToken}` : accessToken;
    }

    try {
        const response = await fetch(`${API_BASE}/student/convocations`, {
            method: 'GET',
            headers
        });

        if (!response.ok) {
            const errorText = await response.text();
            throw new Error(errorText || `Erreur ${response.status}: ${response.statusText}`);
        }

        return await response.json();
    } catch (error) {
        throw new Error(error.message || 'Erreur lors de la récupération des convocations');
    }
}


export async function getMyCandidatures(token = null) {
    try {
        const response = await fetch(`${API_BASE}/student/applications`, {
            method: 'GET',
            headers: getAuthHeaders(token)
        });

        await handleApiResponse(response);
        return await response.json();
    } catch (error) {
        throw new Error(error.message || 'Erreur lors de la récupération des candidatures');
    }
}

export async function previewOfferPdfStudent(offerId, token = null) {
    try {
        const accessToken = token || sessionStorage.getItem("accessToken");
        const headers = {};
        if (accessToken) headers["Authorization"] = `Bearer ${accessToken}`;

        const response = await fetch(`${API_BASE}/student/offers/${offerId}/pdf`, {
            method: "GET",
            headers,
        });

        await handleApiResponse(response);
        return await response.blob();
    } catch (error) {
        throw new Error(error.message || "Erreur lors du chargement du PDF");
    }
}

export async function previewStudentCv(token = null) {
    try {
        const accessToken = token || sessionStorage.getItem("accessToken");
        const headers = {};
        if (accessToken) headers["Authorization"] = `Bearer ${accessToken}`;

        const response = await fetch(`${API_BASE}/student/cv/download`, {
            method: "GET",
            headers,
        });

        await handleApiResponse(response);
        return await response.blob();
    } catch (error) {
        throw new Error(error.message || "Erreur lors du chargement du CV");
    }
}
