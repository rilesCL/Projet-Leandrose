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
    if (!pdfFile) {
        throw { response: { data: 'Fichier PDF manquant' } };
    }

    const url = `${API_BASE}/employeur/offers`;
    const formData = new FormData();
    const offerBlob = new Blob([JSON.stringify(offer)], { type: 'application/json' });
    formData.append('offer', offerBlob);
    formData.append('pdfFile', pdfFile);

    const headers = {};
    const accessToken = token || localStorage.getItem('accessToken');
    const tokenType = (localStorage.getItem('tokenType') || 'BEARER').toUpperCase();
    if (accessToken) {
        headers['Authorization'] = tokenType.startsWith('BEARER') ? `Bearer ${accessToken}` : accessToken;
    }

    try {
        const res = await handleFetch(url, {
            method: 'POST',
            headers,
            body: formData
        });

        // handleFetch peut retourner soit un Response, soit le body déjà parsé.
        if (res && typeof res.json === 'function') {
            // res est un Response -> retourner le JSON
            return await res.json();
        }

        // res est probablement déjà le body parsé -> le renvoyer tel quel
        return res;
    } catch (err) {
        // err a normalement la forme { response: { data: ... } }
        // Log pour debug
        console.error('uploadStageEmployeur error', err);

        // Rejeter une erreur structurée pour l'appelant (comportement similaire à axios)
        const message = err?.response?.data ?? err?.message ?? 'Erreur inconnue';
        throw { response: { data: message } };
    }
}