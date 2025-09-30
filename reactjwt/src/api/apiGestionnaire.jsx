const API_BASE = 'http://localhost:8080/gestionnaire';

function getAuthHeaders() {
    const accessToken = sessionStorage.getItem('accessToken');
    const tokenType = (sessionStorage.getItem('tokenType') || 'BEARER').toUpperCase();
    if (!accessToken) return {};
    return {
        "Content-Type": "application/json",
        "Authorization": tokenType.startsWith("BEARER") ? `Bearer ${accessToken}` : accessToken,
    };
}

export async function downloadCv(cvId) {
    try {
        const response = await fetch(`${API_BASE}/cv/${cvId}/download`, {
            method: "GET",
            headers: getAuthHeaders(),
        });

        if (!response.ok) {
            throw new Error(`HTTP erreur! Status ${response.status}`);
        }

        const blob = await response.blob();
        const url = window.URL.createObjectURL(blob);
        const a = document.createElement("a");
        a.href = url;
        a.download = `cv_${cvId}.pdf`;
        document.body.appendChild(a);
        a.click();
        a.remove();
        window.URL.revokeObjectURL(url);
    } catch (err) {
        console.error("Erreur téléchargement PDF", err);
        throw err;
    }
}

export async function getPendingCvs() {
    try {
        const response = await fetch(`${API_BASE}/cvs/pending`, {
            method: "GET",
            headers: getAuthHeaders(),
        });

        if (!response.ok) throw new Error(`HTTP erreur! Status ${response.status}`);
        return await response.json();
    } catch (error) {
        console.error("Erreur fetching Cvs: ", error);
        throw error;
    }
}

export async function approveCv(cvId) {
    const response = await fetch(`${API_BASE}/cv/${cvId}/approve`, {
        method: 'POST',
        headers: getAuthHeaders(),
    });
    return response.json();
}

export async function rejectCv(cvId, comment) {
    const response = await fetch(`${API_BASE}/cv/${cvId}/reject`, {
        method: "POST",
        headers: getAuthHeaders(),
        body: JSON.stringify(comment),
    });
    return response.json();
}

export async function getPendingOffers() {
    try {
        const response = await fetch(`${API_BASE}/offers/pending`, {
            method: "GET",
            headers: getAuthHeaders(),
        });

        if (!response.ok) throw new Error(`HTTP erreur! Status ${response.status}`);
        return await response.json();
    } catch (error) {
        console.error("Erreur fetching offers: ", error);
        throw error;
    }
}

export async function approveOffer(offerId) {
    const response = await fetch(`${API_BASE}/offers/${offerId}/approve`, {
        method: 'POST',
        headers: getAuthHeaders(),
    });
    return response.json();
}

export async function rejectOffer(offerId, comment) {
    const response = await fetch(`${API_BASE}/offers/${offerId}/reject`, {
        method: "POST",
        headers: getAuthHeaders(),
        body: JSON.stringify({ comment }),
    });
    return response.json();
}

export async function downloadOfferPdf(offerId) {
    try {
        const response = await fetch(`${API_BASE}/offers/${offerId}/pdf`, {
            method: "GET",
            headers: getAuthHeaders(),
        });

        if (!response.ok) throw new Error(`Erreur téléchargement offre ${offerId}`);

        const blob = await response.blob();
        const url = window.URL.createObjectURL(blob);
        const a = document.createElement("a");
        a.href = url;
        a.download = `offer_${offerId}.pdf`;
        document.body.appendChild(a);
        a.click();
        a.remove();
        window.URL.revokeObjectURL(url);
    } catch (err) {
        console.error(err);
        throw err;
    }
}
