// Pure React API functions using only built-in fetch API
const API_BASE = 'http://localhost:8080';

// Helper function to handle fetch responses
async function handleApiResponse(response) {
    if (!response.ok) {
        const errorText = await response.text();
        throw new Error(errorText || `Erreur ${response.status}: ${response.statusText}`);
    }
    return response;
}

// Get authorization headers
function getAuthHeaders(token = null) {
    const accessToken = token || localStorage.getItem('accessToken');
    const headers = {
        'Content-Type': 'application/json'
    };

    if (accessToken) {
        headers['Authorization'] = `Bearer ${accessToken}`;
    }

    return headers;
}

// Upload CV function
export async function uploadCvStudent(pdfFile, token = null) {
    if (!pdfFile) {
        throw new Error('Fichier PDF manquant');
    }

    const formData = new FormData();
    formData.append('pdfFile', pdfFile);

    const headers = {};
    const accessToken = token || localStorage.getItem('accessToken');
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

// Get student CV function
export async function getStudentCv(token = null) {
    try {
        const response = await fetch(`${API_BASE}/student/cv`, {
            method: 'GET',
            headers: getAuthHeaders(token)
        });

        if (response.status === 404) {
            return null; // No CV found
        }

        await handleApiResponse(response);
        return await response.json();
    } catch (error) {
        throw new Error(error.message || 'Erreur lors de la récupération du CV');
    }
}

// Download CV function
export async function downloadStudentCv(token = null) {
    try {
        const accessToken = token || localStorage.getItem('accessToken');
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