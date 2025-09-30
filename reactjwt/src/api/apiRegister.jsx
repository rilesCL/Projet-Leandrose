const BASE_URL = "http://localhost:8080/api/register";

async function handleFetch(url, options) {
    try {
        console.log("Fetching URL:", url);
        console.log("Options:", options);

        const res = await fetch(url, options);
        console.log("Raw response:", res);

        let data;
        const contentType = res.headers.get("content-type");
        console.log("Content-Type:", contentType);

        if (contentType && contentType.includes("application/json")) {
            data = await res.json();
            console.log("Parsed JSON data:", data);
        } else {
            data = await res.text();
            console.log("Text data:", data);
        }

        if (!res.ok) {
            let errorMessage = typeof data === "string" ? data : data.error || JSON.stringify(data);
            console.error("Error message:", errorMessage);
            throw { response: { data: errorMessage || `Erreur ${res.status}: ${res.statusText}` } };
        }

        console.log("Successful response data:", data);
        return data;
    } catch (error) {
        console.error("Fetch caught error:", error);
        if (error.response) throw error;
        throw { response: { data: error.message || "Impossible de se connecter au serveur" } };
    }
}

export async function registerEmployeur(employeur) {
    return handleFetch(`${BASE_URL}/employeur`, {
        method: "POST",
        headers: { "Content-Type": "application/json" },
        body: JSON.stringify(employeur),
    });
}

export async function registerStudent(student) {
    return handleFetch(`${BASE_URL}/student`, {
        method: "POST",
        headers: { "Content-Type": "application/json" },
        body: JSON.stringify(student),
    });
}
