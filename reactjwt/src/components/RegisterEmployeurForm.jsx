import React, { useState } from "react";
import { registerEmployeur } from "../api/apiRegister.jsx";
import { useNavigate } from "react-router";
import { useTranslation } from "react-i18next";

const initialState = {
    firstName: "",
    lastName: "",
    email: "",
    password: "",
    companyName: "",
    field: ""
};

function validate(values, t) {
    const errors = {};
    if (!values.firstName.trim()) errors.firstName = t("registerEmployeur.errors.firstName");
    if (!values.lastName.trim()) errors.lastName = t("registerEmployeur.errors.lastName");
    if (!values.companyName.trim()) errors.companyName = t("registerEmployeur.errors.companyName");
    if (!values.field.trim()) errors.field = t("registerEmployeur.errors.field");
    if (!values.email.trim()) errors.email = t("registerEmployeur.errors.email");
    else if (!/^[^\s@]+@[^\s@]+\.[^\s@]+$/.test(values.email))
        errors.email = t("registerEmployeur.errors.emailInvalid");
    if (!values.password) errors.password = t("registerEmployeur.errors.password");
    else if (values.password.length < 8)
        errors.password = t("registerEmployeur.errors.passwordLength");
    return errors;
}

export default function RegisterEmployeur() {
    const navigate = useNavigate();
    const { t, i18n } = useTranslation();
    const [form, setForm] = useState(initialState);
    const [errors, setErrors] = useState({});
    const [globalError, setGlobalError] = useState("");
    const [successMessage, setSuccessMessage] = useState("");
    const [isSubmitting, setIsSubmitting] = useState(false);

    const handleChange = (e) => {
        const { id, value } = e.target;
        setForm(prev => ({ ...prev, [id]: value }));
        setErrors(prev => {
            const clone = { ...prev };
            delete clone[id];
            return clone;
        });
        setGlobalError("");
    };

    const handleSubmit = async (e) => {
        e.preventDefault();
        setGlobalError("");
        setSuccessMessage("");
        const v = validate(form, t);
        setErrors(v);
        if (Object.keys(v).length > 0) return;

        setIsSubmitting(true);
        try {
            await registerEmployeur(form);
            setSuccessMessage(t("registerEmployeur.success"));
            setForm(initialState);
            setErrors({});

            // Récupérer le token pour redirection
            const loginResponse = await fetch('http://localhost:8080/user/login', {
                method: 'POST',
                headers: { 'Content-Type': 'application/json' },
                body: JSON.stringify({ email: form.email, password: form.password })
            });

            if (loginResponse.ok) {
                const data = await loginResponse.json();
                sessionStorage.setItem('accessToken', data.accessToken);
                sessionStorage.setItem('tokenType', data.tokenType || 'BEARER');

                const userInfoResponse = await fetch('http://localhost:8080/user/me', {
                    method: 'GET',
                    headers: { Authorization: `Bearer ${data.accessToken}` }
                });

                if (userInfoResponse.ok) {
                    const userData = await userInfoResponse.json();
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
                }
            }

        } catch (err) {
            if (err && err.response && err.response.data) {
                const data = err.response.data;
                setGlobalError(typeof data === "string" ? data : t("registerEmployeur.errors.unknown"));
            } else {
                setGlobalError(t("registerEmployeur.errors.serverError"));
            }
        } finally {
            setIsSubmitting(false);
        }
    };


    const handleReset = () => {
        setForm(initialState);
        setErrors({});
        setGlobalError("");
        setSuccessMessage("");
    };

    const inputClass = (field) =>
        `mt-1 block w-full rounded-md shadow-sm border ${errors[field] ? "border-red-500" : "border-gray-300"} focus:ring-indigo-500 focus:border-indigo-500`;

    return (
        <div className="min-h-screen bg-gray-50 flex items-center justify-center p-4">
            <div className="max-w-3xl w-full">
                <header className="text-center mb-6">
                    <h1 className="text-3xl font-bold text-indigo-600">{t("appName")}</h1>
                </header>

                <div className="bg-white rounded-lg shadow-md p-6 md:p-10">
                    <div className="flex justify-between items-center mb-4">
                        <h2 className="text-2xl font-semibold text-gray-800">{t("registerEmployeur.title")}</h2>
                        <div className="w-32">
                            <select
                                value={i18n.language}
                                onChange={(e) => i18n.changeLanguage(e.target.value)}
                                className="block w-full bg-white border border-gray-300 text-gray-700 py-2 px-3 rounded shadow-sm focus:outline-none focus:ring-2 focus:ring-indigo-500 focus:border-indigo-500"
                            >
                                <option value="en">English</option>
                                <option value="fr">Français</option>
                            </select>
                        </div>
                    </div>

                    <p className="text-sm text-gray-500 mb-6">{t("registerEmployeur.subtitle")}</p>

                    {globalError && (
                        <div role="alert" className="mb-4 text-sm text-red-700 bg-red-50 border border-red-100 p-3 rounded">
                            {globalError}
                        </div>
                    )}

                    {successMessage && (
                        <div role="status" className="mb-4 text-sm text-green-700 bg-green-50 border border-green-100 p-3 rounded">
                            {successMessage}
                        </div>
                    )}

                    <form onSubmit={handleSubmit} noValidate>
                        <div className="grid grid-cols-1 md:grid-cols-2 gap-4">
                            {/* First Name */}
                            <div>
                                <label htmlFor="firstName" className="block text-sm font-medium text-gray-700">
                                    {t("registerEmployeur.firstName")}
                                </label>
                                <input
                                    id="firstName"
                                    type="text"
                                    value={form.firstName}
                                    onChange={handleChange}
                                    className={inputClass("firstName")}
                                    placeholder={t("registerEmployeur.placeholders.firstName")}
                                />
                                {errors.firstName && <p className="mt-1 text-xs text-red-600">{errors.firstName}</p>}
                            </div>

                            {/* Last Name */}
                            <div>
                                <label htmlFor="lastName" className="block text-sm font-medium text-gray-700">
                                    {t("registerEmployeur.lastName")}
                                </label>
                                <input
                                    id="lastName"
                                    type="text"
                                    value={form.lastName}
                                    onChange={handleChange}
                                    className={inputClass("lastName")}
                                    placeholder={t("registerEmployeur.placeholders.lastName")}
                                />
                                {errors.lastName && <p className="mt-1 text-xs text-red-600">{errors.lastName}</p>}
                            </div>

                            {/* Company Name */}
                            <div>
                                <label htmlFor="companyName" className="block text-sm font-medium text-gray-700">
                                    {t("registerEmployeur.companyName")}
                                </label>
                                <input
                                    id="companyName"
                                    type="text"
                                    value={form.companyName}
                                    onChange={handleChange}
                                    className={inputClass("companyName")}
                                    placeholder={t("registerEmployeur.placeholders.companyName")}
                                />
                                {errors.companyName && <p className="mt-1 text-xs text-red-600">{errors.companyName}</p>}
                            </div>

                            {/* Field */}
                            <div>
                                <label htmlFor="field" className="block text-sm font-medium text-gray-700">
                                    {t("registerEmployeur.field")}
                                </label>
                                <input
                                    id="field"
                                    type="text"
                                    value={form.field}
                                    onChange={handleChange}
                                    className={inputClass("field")}
                                    placeholder={t("registerEmployeur.placeholders.field")}
                                />
                                {errors.field && <p className="mt-1 text-xs text-red-600">{errors.field}</p>}
                            </div>

                            {/* Email */}
                            <div>
                                <label htmlFor="email" className="block text-sm font-medium text-gray-700">
                                    {t("registerEmployeur.email")}
                                </label>
                                <input
                                    id="email"
                                    type="email"
                                    value={form.email}
                                    onChange={handleChange}
                                    className={inputClass("email")}
                                    placeholder={t("registerEmployeur.placeholders.email")}
                                />
                                {errors.email && <p className="mt-1 text-xs text-red-600">{errors.email}</p>}
                            </div>

                            {/* Password */}
                            <div>
                                <label htmlFor="password" className="block text-sm font-medium text-gray-700">
                                    {t("registerEmployeur.password")}
                                </label>
                                <input
                                    id="password"
                                    type="password"
                                    value={form.password}
                                    onChange={handleChange}
                                    className={inputClass("password")}
                                    placeholder={t("registerEmployeur.placeholders.password")}
                                />
                                {errors.password && <p className="mt-1 text-xs text-red-600">{errors.password}</p>}
                            </div>
                        </div>

                        {/* Buttons & Links */}
                        <div className="mt-6 flex flex-col md:flex-row md:items-center md:justify-between gap-4">
                            <div className="flex gap-2">
                                <button
                                    type="submit"
                                    disabled={isSubmitting}
                                    className={`inline-flex items-center px-4 py-2 border border-transparent text-sm font-medium rounded-md text-white ${
                                        isSubmitting
                                            ? "bg-indigo-300"
                                            : "bg-indigo-600 hover:bg-indigo-700"
                                    }`}
                                >
                                    {isSubmitting ? t("registerEmployeur.submitting") : t("registerEmployeur.submit")}
                                </button>

                                <button
                                    type="button"
                                    onClick={handleReset}
                                    disabled={isSubmitting}
                                    className="inline-flex items-center px-4 py-2 border border-gray-300 text-sm font-medium rounded-md bg-white text-gray-700 hover:bg-gray-50"
                                >
                                    {t("registerEmployeur.reset")}
                                </button>
                            </div>

                            <div className="text-sm text-gray-500">
                                {t("registerEmployeur.alreadyAccount")}{" "}
                                <button
                                    type="button"
                                    onClick={() => navigate("/login")}
                                    className="text-indigo-600 hover:underline"
                                >
                                    {t("registerEmployeur.login")}
                                </button>
                            </div>
                            <div className="text-sm text-gray-500">
                                {t("registerEmployeur.studentAccount")}{" "}
                                <button
                                    type="button"
                                    onClick={() => navigate("/register/etudiant")}
                                    className="text-indigo-600 hover:underline"
                                >
                                    {t("registerEmployeur.createStudent")}
                                </button>
                            </div>
                        </div>
                    </form>
                </div>
            </div>
        </div>
    );
}
