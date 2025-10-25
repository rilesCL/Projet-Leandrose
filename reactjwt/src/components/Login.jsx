import React, { useState } from "react";
import { useNavigate } from "react-router-dom";
import { useTranslation } from "react-i18next";
import LanguageSelector from "./LanguageSelector.jsx";

const Login = () => {
    const navigate = useNavigate();
    const { t, i18n } = useTranslation();

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

    const validateUser = () => {
        let isValid = true;
        let updatedWarnings = { ...warnings };

        if (!validateEmail()) {
            updatedWarnings.email = t("login.errors.emailInvalid");
            isValid = false;
        } else {
            updatedWarnings.email = "";
        }

        if (!validatePassword()) {
            updatedWarnings.password = t("login.errors.passwordRequired");
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
                switch (response.status) {
                    case 401:
                        setWarnings({
                            email: t("login.errors.invalidCredentials"),
                            password: t("login.errors.invalidCredentials")
                        });
                        break;
                    case 404:
                        setWarnings({ email: t("login.errors.userNotFound"), password: "" });
                        break;
                    default:
                        setWarnings({ email: t("login.errors.connectionError"), password: "" });
                }
                return;
            }

            const data = await response.json();
            console.log("Login response:", data);

            sessionStorage.setItem('accessToken', data.accessToken);
            sessionStorage.setItem('tokenType', data.tokenType || 'BEARER');

            await fetchUserInfo(data.accessToken);

        } catch (error) {
            console.error("Login error:", error);
            setWarnings({
                email: t("login.errors.serverError"),
                password: ""
            });
        } finally {
            setIsSubmitting(false);
        }
    };

    const fetchUserInfo = async (token) => {
        console.log("Token envoyé:", token);

        try {
            const response = await fetch('http://localhost:8080/user/me', {
                method: "GET",
                headers: {
                    Accept: "application/json",
                    Authorization: `Bearer ${token}`,
                },
            });

            console.log("Response status:", response.status);

            if (response.ok) {
                const userData = await response.json();
                console.log("User data:", userData);

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
            console.error("User info error:", error);
            navigate("/dashboard");
        }
    };

    const handleForgotPassword = async (e) => {
        e.preventDefault();
        if (!forgotPasswordEmail.trim()) {
            setWarnings({ ...warnings, email: t("login.forgotPasswordModal.errors.emailRequired") });
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
                alert(t("login.forgotPasswordModal.success"));
                setShowForgotPassword(false);
                setForgotPasswordEmail("");
                setWarnings({ email: "", password: "" });
            } else {
                setWarnings({ ...warnings, email: t("login.forgotPasswordModal.errors.emailNotFound") });
            }
        } catch (error) {
            setWarnings({ ...warnings, email: t("login.forgotPasswordModal.errors.sendError") });
        } finally {
            setIsSubmitting(false);
        }
    };

    if (showForgotPassword) {
        return (
            <div className="min-h-screen bg-gray-50 flex items-center justify-center">
                <div className="max-w-xl w-full">
                    <header className="text-center mb-8">
                        <h1 className="text-4xl font-bold text-indigo-600">{t("appName")}</h1>
                    </header>

                    <div className="bg-white rounded-lg shadow-lg p-8">
                        <div className="flex justify-between items-center mb-3">
                            <h2 className="text-3xl font-semibold text-gray-800">{t("login.forgotPasswordModal.title")}</h2>
                            <LanguageSelector />
                        </div>
                        <p className="text-base text-gray-500 mb-8">{t("login.forgotPasswordModal.subtitle")}</p>

                        <form onSubmit={handleForgotPassword}>
                            <div className="mb-6">
                                <label htmlFor="forgotEmail" className="block text-base font-medium text-gray-700 mb-2">
                                    {t("login.forgotPasswordModal.email")}
                                </label>
                                <input
                                    id="forgotEmail"
                                    type="email"
                                    value={forgotPasswordEmail}
                                    onChange={(e) => setForgotPasswordEmail(e.target.value)}
                                    className={`mt-1 block w-full rounded-md shadow-sm border ${warnings.email ? "border-red-500" : "border-gray-300"} focus:ring-indigo-500 focus:border-indigo-500 px-4 py-3 text-base`}
                                    placeholder={t("login.placeholders.email")}
                                />
                                {warnings.email && <div className="mt-2 text-sm text-red-600">{warnings.email}</div>}
                            </div>

                            <div className="flex gap-3">
                                <button
                                    type="submit"
                                    disabled={isSubmitting}
                                    className={`flex-1 px-6 py-3 border border-transparent text-base font-medium rounded-md text-white ${isSubmitting ? "bg-indigo-300" : "bg-indigo-600 hover:bg-indigo-700"}`}
                                >
                                    {isSubmitting ? t("login.forgotPasswordModal.submitting") : t("login.forgotPasswordModal.submit")}
                                </button>
                                <button
                                    type="button"
                                    onClick={() => {
                                        setShowForgotPassword(false);
                                        setWarnings({ email: "", password: "" });
                                        setForgotPasswordEmail("");
                                    }}
                                    className="px-6 py-3 border border-gray-300 text-base font-medium rounded-md bg-white text-gray-700 hover:bg-gray-50"
                                >
                                    {t("login.forgotPasswordModal.back")}
                                </button>
                            </div>
                        </form>
                    </div>
                </div>
            </div>
        );
    }

    return (
        <div className="min-h-screen bg-gray-50 flex items-center justify-center">
            <div className="max-w-xl w-full">
                <header className="text-center mb-8">
                    <h1 className="text-4xl font-bold text-indigo-600">{t("appName")}</h1>
                </header>

                <div className="bg-white rounded-lg shadow-lg p-8">
                    <div className="flex justify-between items-center mb-3">
                        <h2 className="text-3xl font-semibold text-gray-800">{t("login.title")}</h2>
                        <div className="w-36">
                            <select
                                value={i18n.language}
                                onChange={(e) => i18n.changeLanguage(e.target.value)}
                                className="block w-full bg-white border border-gray-300 text-gray-700 py-2.5 px-4 rounded shadow-sm focus:outline-none focus:ring-2 focus:ring-indigo-500 focus:border-indigo-500"
                            >
                                <option value="en">English</option>
                                <option value="fr">Français</option>
                            </select>
                        </div>
                    </div>
                    <p className="text-base text-gray-500 mb-8">{t("login.subtitle")}</p>

                    <form onSubmit={handleSubmit}>
                        <div className="space-y-6">
                            <div>
                                <label htmlFor="email" className="block text-base font-medium text-gray-700 mb-2">
                                    {t("login.email")}
                                </label>
                                <input
                                    id="email"
                                    type="email"
                                    name="email"
                                    value={formData.email}
                                    onChange={handleChanges}
                                    className={`mt-1 block w-full rounded-md shadow-sm border ${warnings.email ? "border-red-500" : "border-gray-300"} focus:ring-indigo-500 focus:border-indigo-500 px-4 py-3 text-base`}
                                    placeholder={t("login.placeholders.email")}
                                    required
                                />
                                {warnings.email && <div className="mt-2 text-sm text-red-600">{warnings.email}</div>}
                            </div>

                            <div>
                                <label htmlFor="password" className="block text-base font-medium text-gray-700 mb-2">
                                    {t("login.password")}
                                </label>
                                <input
                                    id="password"
                                    type="password"
                                    name="password"
                                    value={formData.password}
                                    onChange={handleChanges}
                                    className={`mt-1 block w-full rounded-md shadow-sm border ${warnings.password ? "border-red-500" : "border-gray-300"} focus:ring-indigo-500 focus:border-indigo-500 px-4 py-3 text-base`}
                                    placeholder={t("login.placeholders.password")}
                                    required
                                />
                                {warnings.password && <div className="mt-2 text-sm text-red-600">{warnings.password}</div>}
                            </div>
                        </div>

                        <div className="mt-8">
                            <button
                                type="submit"
                                disabled={isSubmitting}
                                className={`w-full px-6 py-3 border border-transparent text-base font-medium rounded-md text-white ${isSubmitting ? "bg-indigo-300" : "bg-indigo-600 hover:bg-indigo-700"}`}
                            >
                                {isSubmitting ? t("login.submitting") : t("login.submit")}
                            </button>
                        </div>

                        <div className="mt-6 text-center space-y-3">
                            <div className="text-base text-gray-500">
                                {t("login.noAccount")}{" "}
                                <button
                                    type="button"
                                    onClick={() => navigate("/register/")}
                                    className="text-indigo-600 hover:underline"
                                >
                                    {t("login.signUp")}
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