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

export async function downloadStudentCv(token = null) {
    try {
        const accessToken = token || sessionStorage.getItem('accessToken');
        const headers = {};

        if (accessToken) {
            headers['Authorization'] = `Bearer ${accessToken}`;
        }

        const response = await fetch(`${API_BASE}/student/cv/download`, {
            method: 'GET',
            headers
        });

        await handleApiResponse(response);
        return response;
    } catch (error) {
        throw new Error(error.message || 'Erreur lors du téléchargement du CV');
    }
}