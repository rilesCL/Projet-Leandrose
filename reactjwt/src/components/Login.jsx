import React, { useState } from "react";
import { useNavigate } from "react-router-dom";

const Login = () => {
    const navigate = useNavigate();

    const [formData, setFormData] = useState({
        email: '',
        password: ''
    });

    const [warnings, setWarnings] = useState({
        email: '',
        password: ''
    });

    const [isSubmitting, setIsSubmitting] = useState(false);
    const [showForgotPassword, setShowForgotPassword] = useState(false);
    const [forgotPasswordEmail, setForgotPasswordEmail] = useState("");

    // Use Case 2: Validation des informations
    const validateUser = () => {
        let isValid = true;
        let updatedWarnings = { ...warnings };

        if (!validateEmail()) {
            updatedWarnings.email = "Email invalide";
            isValid = false;
        } else {
            updatedWarnings.email = "";
        }

        if (!validatePassword()) {
            updatedWarnings.password = "Mot de passe requis";
            isValid = false;
        } else {
            updatedWarnings.password = "";
        }

        setWarnings(updatedWarnings);
        return isValid;
    };

    const validateEmail = () => {
        const emailRegex = /^[A-Z0-9._%+-]+@[A-Z0-9.-]+\.[A-Z]{2,}$/i;
        return emailRegex.test(formData.email);
    };

    const validatePassword = () => {
        return formData.password.length > 0;
    };

    const handleChanges = (e) => {
        const { name, value } = e.target;
        setWarnings({ ...warnings, [name]: "" });
        setFormData({ ...formData, [name]: value.trim() });
    };

    const handleSubmit = (e) => {
        e.preventDefault();
        if (validateUser()) {
            fetchLogin();
        }
    };

    // Use Case 3: Validation via API
    const fetchLogin = async () => {
        setIsSubmitting(true);
        try {
            console.log("FormData:", formData);
            const response = await fetch('http://localhost:8080/user/login', {
                method: "POST",
                headers: {
                    Accept: "application/json",
                    "Content-Type": "application/json;charset=UTF-8",
                },
                body: JSON.stringify({
                    email: formData.email.toLowerCase(),
                    password: formData.password
                }),
            });

            if (!response.ok) {
                // Use Case 4: Gestion des erreurs
                switch (response.status) {
                    case 401:
                        setWarnings({
                            email: "Email ou mot de passe incorrect",
                            password: "Email ou mot de passe incorrect"
                        });
                        break;
                    case 404:
                        setWarnings({ email: "Utilisateur introuvable", password: "" });
                        break;
                    default:
                        setWarnings({ email: "Erreur de connexion", password: "" });
                }
                return;
            }

            const data = await response.json();
            console.log("Login response:", data);

            // Stocker le token
            sessionStorage.setItem('accessToken', data.accessToken);
            sessionStorage.setItem('tokenType', data.tokenType || 'BEARER');

            await fetchUserInfo(data.accessToken);

        } catch (error) {
            console.error("Login error:", error);
            setWarnings({
                email: "Impossible de se connecter au serveur",
                password: ""
            });
        } finally {
            setIsSubmitting(false);
        }
    };


    const fetchUserInfo = async (token) => {
        console.log("Token envoyé:", token); // DEBUG

        try {
            const response = await fetch('http://localhost:8080/user/me', {
                method: "GET",
                headers: {
                    Accept: "application/json",
                    Authorization: `Bearer ${token}`,
                },
            });

            console.log("Response status:", response.status); // DEBUG

            if (response.ok) {
                const userData = await response.json();
                console.log("User data:", userData); // DEBUG


                switch (userData.role) {
                    case 'STUDENT':
                        navigate("/dashboard/student");
                        break;
                    case 'EMPLOYEUR':
                        navigate("/dashboard/employeur");
                        break;
                    case 'GESTIONNAIRE':
                        navigate("/dashboard/gestionnaire");
                        break;
                    default:
                        navigate("/dashboard");
                }
            } else {
                console.error("Failed to fetch user info, status:", response.status);
                navigate("/dashboard");
            }
        } catch (error) {
            console.error("User info error:", error); // DEBUG
            navigate("/dashboard");
        }
    };

    // Use Case 6: Mot de passe oublié
    const handleForgotPassword = async (e) => {
        e.preventDefault();
        if (!forgotPasswordEmail.trim()) {
            setWarnings({ ...warnings, email: "Veuillez entrer votre email" });
            return;
        }

        setIsSubmitting(true);
        try {
            const response = await fetch('http://localhost:8080/user/forgot-password', {
                method: 'POST',
                headers: { 'Content-Type': 'application/json' },
                body: JSON.stringify({ email: forgotPasswordEmail })
            });

            if (response.ok) {
                alert("Un email de réinitialisation a été envoyé à votre adresse.");
                setShowForgotPassword(false);
                setForgotPasswordEmail("");
                setWarnings({ email: "", password: "" });
            } else {
                setWarnings({ ...warnings, email: "Email non trouvé" });
            }
        } catch (error) {
            setWarnings({ ...warnings, email: "Erreur lors de l'envoi de l'email" });
        } finally {
            setIsSubmitting(false);
        }
    };

    // Interface mot de passe oublié
    if (showForgotPassword) {
        return (
            <div className="min-h-screen bg-gray-50 flex items-center justify-center p-4">
                <div className="max-w-md w-full bg-white rounded-lg shadow-md p-6">
                    <h1 className="text-2xl font-semibold text-gray-800 mb-2">Mot de passe oublié</h1>
                    <p className="text-sm text-gray-500 mb-6">
                        Entrez votre email pour recevoir un lien de réinitialisation.
                    </p>

                    <form onSubmit={handleForgotPassword}>
                        <div className="mb-4">
                            <label htmlFor="forgotEmail" className="block text-sm font-medium text-gray-700">Email</label>
                            <input
                                id="forgotEmail"
                                type="email"
                                value={forgotPasswordEmail}
                                onChange={(e) => setForgotPasswordEmail(e.target.value)}
                                className={`mt-1 block w-full rounded-md shadow-sm border ${warnings.email ? "border-red-500" : "border-gray-300"} focus:ring-indigo-500 focus:border-indigo-500 px-3 py-2`}
                                placeholder="votre.email@exemple.com"
                            />
                            {warnings.email && <div className="mt-1 text-xs text-red-600">{warnings.email}</div>}
                        </div>

                        <div className="flex gap-2">
                            <button
                                type="submit"
                                disabled={isSubmitting}
                                className={`flex-1 px-4 py-2 border border-transparent text-sm font-medium rounded-md text-white ${isSubmitting ? "bg-indigo-300" : "bg-indigo-600 hover:bg-indigo-700"}`}
                            >
                                {isSubmitting ? "Envoi..." : "Envoyer"}
                            </button>
                            <button
                                type="button"
                                onClick={() => {
                                    setShowForgotPassword(false);
                                    setWarnings({ email: "", password: "" });
                                    setForgotPasswordEmail("");
                                }}
                                className="px-4 py-2 border border-gray-300 text-sm font-medium rounded-md bg-white text-gray-700 hover:bg-gray-50"
                            >
                                Retour
                            </button>
                        </div>
                    </form>
                </div>
            </div>
        );
    }

    // Use Case 1: Interface principale de connexion
    return (
        <div className="min-h-screen bg-gray-50 flex items-center justify-center p-4">
            <div className="max-w-md w-full">
                <header className="text-center mb-6">
                    <h1 className="text-3xl font-bold text-indigo-600">LeandrOSE</h1>
                </header>

                <div className="bg-white rounded-lg shadow-md p-6">
                    <h2 className="text-2xl font-semibold text-gray-800 mb-2">Connexion</h2>
                    <p className="text-sm text-gray-500 mb-6">
                        Connectez-vous à votre compte pour accéder à votre espace personnel.
                    </p>
                    <form onSubmit={handleSubmit}>
                        <div className="space-y-4">
                            <div>
                                <label htmlFor="email" className="block text-sm font-medium text-gray-700">Email</label>
                                <input
                                    id="email"
                                    type="email"
                                    name="email"
                                    value={formData.email}
                                    onChange={handleChanges}
                                    className={`mt-1 block w-full rounded-md shadow-sm border ${warnings.email ? "border-red-500" : "border-gray-300"} focus:ring-indigo-500 focus:border-indigo-500 px-3 py-2`}
                                    placeholder="votre.email@exemple.com"
                                    required
                                />
                                {warnings.email && <div className="mt-1 text-xs text-red-600">{warnings.email}</div>}
                            </div>

                            <div>
                                <label htmlFor="password" className="block text-sm font-medium text-gray-700">Mot de passe</label>
                                <input
                                    id="password"
                                    type="password"
                                    name="password"
                                    value={formData.password}
                                    onChange={handleChanges}
                                    className={`mt-1 block w-full rounded-md shadow-sm border ${warnings.password ? "border-red-500" : "border-gray-300"} focus:ring-indigo-500 focus:border-indigo-500 px-3 py-2`}
                                    placeholder="Votre mot de passe"
                                    required
                                />
                                {warnings.password && <div className="mt-1 text-xs text-red-600">{warnings.password}</div>}
                            </div>
                        </div>

                        <div className="mt-6">
                            <button
                                type="submit"
                                disabled={isSubmitting}
                                className={`w-full px-4 py-2 border border-transparent text-sm font-medium rounded-md text-white ${isSubmitting ? "bg-indigo-300" : "bg-indigo-600 hover:bg-indigo-700"}`}
                            >
                                {isSubmitting ? "Connexion..." : "Se connecter"}
                            </button>
                        </div>

                        <div className="mt-4 text-center space-y-2">
                            <button
                                type="button"
                                onClick={() => setShowForgotPassword(true)}
                                className="text-sm text-indigo-600 hover:underline"
                            >
                                Mot de passe oublié ?
                            </button>

                            <div className="text-sm text-gray-500">
                                Pas encore de compte ?{" "}
                                <button
                                    type="button"
                                    onClick={() => navigate("/register/etudiant")}
                                    className="text-indigo-600 hover:underline"
                                >
                                    S'inscrire
                                </button>
                            </div>
                        </div>
                    </form>
                </div>
            </div>
        </div>
    );
};

export default Login;