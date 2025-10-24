import React, { useState, useEffect } from "react";
import { useParams, useNavigate } from "react-router-dom";
import { verifyPassword, getCurrentUser, signAgreement } from "../api/apiSignature";

export default function SignerEntentePage() {
    const { id } = useParams();
    const navigate = useNavigate();
    const [password, setPassword] = useState("");
    const [loading, setLoading] = useState(false);
    const [error, setError] = useState("");
    const [success, setSuccess] = useState("");
    const [userInfo, setUserInfo] = useState(null);

    useEffect(() => {
        const loadCurrentUser = async () => {
            const token = sessionStorage.getItem("accessToken");
            if (!token) {
                console.log("No token found in sessionStorage");
                return;
            }

            try {
                const userData = await getCurrentUser(token);
                setUserInfo(userData);

                if (userData.email) {
                    sessionStorage.setItem('email', userData.email);
                }
            } catch (error) {
                setError("Session expirée. Veuillez vous reconnecter.");
            }
        };

        loadCurrentUser();
    }, []);

    const handleSign = async (e) => {
        e.preventDefault();
        setError("");
        setSuccess("");
        setLoading(true);

        try {
            const email = userInfo?.email || sessionStorage.getItem("email");
            if (!email) {
                throw new Error("Impossible de trouver votre email. Veuillez vous reconnecter.");
            }


            const loginData = await verifyPassword(email, password);

            if (!loginData.accessToken) {
                throw new Error("Aucun token reçu après la connexion");
            }

            const newToken = loginData.accessToken;

            await getCurrentUser(newToken);

            sessionStorage.setItem('accessToken', newToken);


            await signAgreement(id, newToken);

            setSuccess("Entente signée avec succès !");
            setPassword("");

            setTimeout(() => {
                navigate("/dashboard/employeur");
            }, 2000);

        } catch (error) {
            console.error("💥 Signature process failed:", error);

            if (error.status === 401) {
                setError("Mot de passe incorrect. Veuillez réessayer.");
            } else if (error.status === 403) {
                setError("Accès refusé. Seuls les employeurs peuvent signer les ententes.");
            } else if (error.status === 404) {
                setError("Entente non trouvée.");
            } else if (error.message.includes("Mot de passe incorrect")) {
                setError("Mot de passe incorrect. Veuillez réessayer.");
            } else {
                setError(error.message || "Une erreur est survenue lors de la signature.");
            }
        } finally {
            setLoading(false);
        }
    };

    return (
        <div className="min-h-screen flex items-center justify-center bg-gray-50 px-4">
            <div className="max-w-md w-full bg-white p-6 rounded-lg shadow-md">
                <h2 className="text-2xl font-semibold text-gray-800 mb-4 text-center">
                    Signature de l'entente
                </h2>

                <form onSubmit={handleSign}>
                    <div className="mb-4">
                        <label className="block text-gray-700 text-sm font-medium mb-2">
                            Confirmez votre mot de passe pour signer cette entente:
                        </label>
                        <input
                            type="password"
                            value={password}
                            onChange={(e) => setPassword(e.target.value)}
                            className="w-full px-3 py-2 border border-gray-300 rounded-md shadow-sm focus:outline-none focus:ring-2 focus:ring-indigo-500 focus:border-indigo-500"
                            placeholder="Votre mot de passe"
                            required
                            disabled={loading}
                        />
                    </div>

                    {error && (
                        <div className="mb-4 p-3 bg-red-50 border border-red-200 rounded-md">
                            <p className="text-red-700 text-sm text-center">{error}</p>
                        </div>
                    )}

                    {success && (
                        <div className="mb-4 p-3 bg-green-50 border border-green-200 rounded-md">
                            <p className="text-green-700 text-sm text-center">{success}</p>
                        </div>
                    )}

                    <button
                        type="submit"
                        disabled={loading || !password}
                        className={`w-full py-2 px-4 border border-transparent rounded-md shadow-sm text-sm font-medium text-white focus:outline-none focus:ring-2 focus:ring-offset-2 focus:ring-indigo-500 ${
                            loading || !password
                                ? 'bg-gray-400 cursor-not-allowed'
                                : 'bg-indigo-600 hover:bg-indigo-700'
                        }`}
                    >
                        {loading ? (
                            <span className="flex items-center justify-center">
                                <svg className="animate-spin -ml-1 mr-2 h-4 w-4 text-white" xmlns="http://www.w3.org/2000/svg" fill="none" viewBox="0 0 24 24">
                                    <circle className="opacity-25" cx="12" cy="12" r="10" stroke="currentColor" strokeWidth="4"></circle>
                                    <path className="opacity-75" fill="currentColor" d="M4 12a8 8 0 018-8V0C5.373 0 0 5.373 0 12h4zm2 5.291A7.962 7.962 0 014 12H0c0 3.042 1.135 5.824 3 7.938l3-2.647z"></path>
                                </svg>
                                Signature en cours...
                            </span>
                        ) : (
                            "Signer l'entente"
                        )}
                    </button>
                </form>

                <div className="mt-4 text-center">
                    <button
                        type="button"
                        onClick={() => navigate("/dashboard/employeur")}
                        className="text-sm text-gray-600 hover:text-gray-800 underline"
                        disabled={loading}
                    >
                        Annuler et retourner au tableau de bord
                    </button>
                </div>
            </div>
        </div>
    );
}