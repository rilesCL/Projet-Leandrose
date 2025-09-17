async function handleFetch(url, options) {
    try {
        const res = await fetch(url, options);
        if (!res.ok) {
            const errorText = await res.text();
            throw { response: { data: errorText || `Erreur ${res.status}: ${res.statusText}` } };
        }
        return res;
    } catch (error) {
        if (error.response) {
            throw error;
        }
        throw {
            response: {
                data: error.message || "Impossible de se connecter au serveur"
            }
        };
    }
}

const BASE_URL = 'http://localhost:8080/api/register';

export async function registerEmployeur(employeur) {
    const res = await handleFetch(`${BASE_URL}/employeur`, {
        method: 'POST',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify(employeur)
    });
    return await res.text();
}

