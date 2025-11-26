const API_BASE = "http://localhost:8080";

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
function authHeaders(token = null) {
    const accessToken = token || sessionStorage.getItem("accessToken");
    const tokenType = (sessionStorage.getItem("tokenType") || "BEARER").toUpperCase();
    const headers = {};
    if (accessToken) headers["Authorization"] = tokenType.startsWith("BEARER") ? `Bearer ${accessToken}` : accessToken;
    return headers;
}

export async function getMyRole(token = null){
    const res = await handleFetch(`${API_BASE}/user/me/role`, {
        headers: authHeaders(token)
    });
    return await res.json();
}