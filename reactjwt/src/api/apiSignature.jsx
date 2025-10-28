const BASE_URL = "http://localhost:8080";

async function handleFetch(url, options) {
    try {
        console.log("🔍 API Call:", url);
        console.log("📦 Options:", {
            method: options.method,
            headers: options.headers,
            body: options.body ? JSON.parse(options.body) : null
        });

        const res = await fetch(url, options);
        console.log("📥 Response status:", res.status);

        let data;
        const text = await res.text();
        console.log("📥 Response text:", text);

        if (text && text.trim().length > 0) {
            try {
                data = JSON.parse(text);
                console.log("✅ Parsed JSON data:", data);
            } catch (parseError) {
                console.log("📝 Response is not JSON, using raw text");
                data = text;
            }
        } else {
            console.log("📭 Empty response received");
            data = null;
        }

        if (!res.ok) {
            console.log("❌ Request failed with status:", res.status);
            throw {
                status: res.status,
                message: data?.error || data?.message || `Erreur ${res.status}`,
                data: data
            };
        }

        console.log("✅ Request successful");
        return data;
    } catch (error) {
        console.error("💥 Fetch error:", error);
        if (error.status) throw error;
        throw {
            status: 0,
            message: error.message || "Erreur de connexion"
        };
    }
}

// Verify password and get token
export async function verifyPassword(email, password) {
    return handleFetch(`${BASE_URL}/user/login`, {
        method: "POST",
        headers: { "Content-Type": "application/json" },
        body: JSON.stringify({ email: email.toLowerCase(), password }),
    });
}

// Get current user info
export async function getCurrentUser(token) {
    return handleFetch(`${BASE_URL}/user/me`, {
        method: "GET",
        headers: {
            "Authorization": `Bearer ${token}`,
            "Content-Type": "application/json"
        },
    });
}

// Sign agreement - matches your backend endpoint exactly
export async function signAgreement(ententeId, token) {
    return handleFetch(`${BASE_URL}/employeur/ententes/${ententeId}/signer`, {
        method: "POST",
        headers: {
            "Authorization": `Bearer ${token}`,
            "Content-Type": "application/json"
        },
    });
}