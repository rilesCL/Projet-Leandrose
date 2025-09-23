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

export async function uploadCvStudent(pdfFile, token = null) {
    if (!pdfFile) {
        throw { response: { data: 'Fichier PDF manquant' } };
    }

    const url = `${API_BASE}/student/cv`;
    const formData = new FormData();
    formData.append('pdfFile', pdfFile);

    const headers = {};
    const accessToken = token || localStorage.getItem('accessToken');
    if (accessToken) {
        headers['Authorization'] = `Bearer ${accessToken}`;
    }

    const res = await handleFetch(url, { method: 'POST', headers, body: formData });
    return res.json ? await res.json() : res;
}

export async function getStudentCvs(token = null) {
    const url = `${API_BASE}/student/cv`;
    const headers = {};
    const accessToken = token || localStorage.getItem('accessToken');
    if (accessToken) {
        headers['Authorization'] = `Bearer ${accessToken}`;
    }

    const res = await handleFetch(url, { method: 'GET', headers });
    return res.json ? await res.json() : res;
}
