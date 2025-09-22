const API_BASE = 'http://localhost:8080';

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

export async function uploadStageEmployeur(offer, pdfFile, token = null) {
    if (!pdfFile) throw { response: { data: 'Fichier PDF manquant' } };

    const url = `${API_BASE}/employeur/offers`;
    const formData = new FormData();
    const offerBlob = new Blob([JSON.stringify(offer)], { type: 'application/json' });
    formData.append('offer', offerBlob);
    formData.append('pdfFile', pdfFile);

    const headers = {};
    const accessToken = token || localStorage.getItem('accessToken');
    const tokenType = (localStorage.getItem('tokenType') || 'BEARER').toUpperCase();

    console.log('uploadStageEmployeur - accessToken:', accessToken, 'tokenType:', tokenType);

    if (accessToken) {
        const headerValue = tokenType.startsWith('BEARER') ? `Bearer ${accessToken}` : accessToken;
        console.log(headerValue);
        headers['Authorization'] = headerValue;
    } else {
        console.warn('Aucun token disponible pour uploadStageEmployeur');
    }

    const res = await handleFetch(url, {
        method: 'POST',
        headers,
        body: formData
    });

    return await res.json();
}