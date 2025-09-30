import React, { useState } from "react";
import { registerStudent } from "../api/apiRegister.jsx";
import { useNavigate } from "react-router";
import { useTranslation } from "react-i18next";

const initialState = {
    firstName: "",
    lastName: "",
    email: "",
    password: "",
    studentNumber: "",
    program: "",
};

function validate(values, t) {
    const errors = {};
    if (!values.firstName.trim()) errors.firstName = t("registerEtudiant.errors.firstName");
    if (!values.lastName.trim()) errors.lastName = t("registerEtudiant.errors.lastName");
    if (!values.studentNumber.trim()) errors.studentNumber = t("registerEtudiant.errors.studentNumber");
    if (!values.program.trim()) errors.program = t("registerEtudiant.errors.program");
    if (!values.email.trim()) errors.email = t("registerEtudiant.errors.email");
    else if (!/^[^\s@]+@[^\s@]+\.[^\s@]+$/.test(values.email))
        errors.email = t("registerEtudiant.errors.emailInvalid");
    if (!values.password) errors.password = t("registerEtudiant.errors.password");
    else if (values.password.length < 8)
        errors.password = t("registerEtudiant.errors.passwordLength");
    return errors;
}

export default function RegisterEtudiant() {
    const navigate = useNavigate();
    const { t, i18n } = useTranslation();
    const [form, setForm] = useState(initialState);
    const [errors, setErrors] = useState({});
    const [globalError, setGlobalError] = useState("");
    const [successMessage, setSuccessMessage] = useState("");
    const [isSubmitting, setIsSubmitting] = useState(false);

    const handleChange = (e) => {
        const { id, value } = e.target;
        setForm((prev) => ({ ...prev, [id]: value }));
        setErrors((prev) => {
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
        if (Object.keys(v).length > 0) {
            const firstKey = Object.keys(v)[0];
            const el = document.getElementById(firstKey);
            if (el) el.focus();
            return;
        }

        setIsSubmitting(true);
        try {
            await registerStudent(form);
            setSuccessMessage(t("registerEtudiant.success"));
            setForm(initialState);
            setErrors({});
            setTimeout(() => {
                setSuccessMessage("");
                navigate("/");
            }, 1500);
        } catch (err) {
            if (err && err.response && err.response.data) {
                const data = err.response.data;
                if (typeof data === "string") {
                    setGlobalError(data);
                } else if (typeof data === "object") {
                    setErrors(data);
                    const firstKey = Object.keys(data)[0];
                    const el = document.getElementById(firstKey);
                    if (el) el.focus();
                    const parts = [];
                    for (const key of Object.keys(data)) {
                        parts.push(
                            `${key}: ${
                                typeof data[key] === "string"
                                    ? data[key]
                                    : JSON.stringify(data[key])
                            }`
                        );
                    }
                    setGlobalError(parts.join(" · "));
                } else {
                    setGlobalError(t("registerEtudiant.errors.unknown"));
                }
            } else {
                setGlobalError(t("registerEtudiant.errors.serverError"));
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
        `mt-1 block w-full rounded-md shadow-sm border ${
            errors[field] ? "border-red-500" : "border-gray-300"
        } focus:ring-indigo-500 focus:border-indigo-500`;

    return (
        <div className="min-h-screen bg-gray-50 flex items-center justify-center p-4">
            <div className="max-w-3xl w-full">
                <header className="text-center mb-6">
                    <h1 className="text-3xl font-bold text-indigo-600">{t("appName")}</h1>
                </header>

                <div className="bg-white rounded-lg shadow-md p-6 md:p-10">
                    <div className="flex justify-between items-center mb-4">
                        <h2 className="text-2xl font-semibold text-gray-800">{t("registerEtudiant.title")}</h2>
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

                    <p className="text-sm text-gray-500 mb-6">{t("registerEtudiant.subtitle")}</p>

                    {globalError && (
                        <div
                            role="alert"
                            className="mb-4 text-sm text-red-700 bg-red-50 border border-red-100 p-3 rounded"
                        >
                            {globalError}
                        </div>
                    )}

                    {successMessage && (
                        <div
                            role="status"
                            className="mb-4 text-sm text-green-700 bg-green-50 border border-green-100 p-3 rounded"
                        >
                            {successMessage}
                        </div>
                    )}

                    <form onSubmit={handleSubmit} noValidate>
                        <div className="grid grid-cols-1 md:grid-cols-2 gap-4">
                            <div>
                                <label
                                    htmlFor="firstName"
                                    className="block text-sm font-medium text-gray-700"
                                >
                                    {t("registerEtudiant.firstName")}
                                </label>
                                <input
                                    id="firstName"
                                    type="text"
                                    value={form.firstName}
                                    onChange={handleChange}
                                    className={inputClass("firstName")}
                                    placeholder={t("registerEtudiant.placeholders.firstName")}
                                    aria-invalid={!!errors.firstName}
                                    aria-describedby={
                                        errors.firstName ? "firstName-error" : undefined
                                    }
                                />
                                {errors.firstName && (
                                    <p
                                        id="firstName-error"
                                        role="alert"
                                        className="mt-1 text-xs text-red-600"
                                    >
                                        {errors.firstName}
                                    </p>
                                )}
                            </div>

                            <div>
                                <label
                                    htmlFor="lastName"
                                    className="block text-sm font-medium text-gray-700"
                                >
                                    {t("registerEtudiant.lastName")}
                                </label>
                                <input
                                    id="lastName"
                                    type="text"
                                    value={form.lastName}
                                    onChange={handleChange}
                                    className={inputClass("lastName")}
                                    placeholder={t("registerEtudiant.placeholders.lastName")}
                                    aria-invalid={!!errors.lastName}
                                    aria-describedby={errors.lastName ? "lastName-error" : undefined}
                                />
                                {errors.lastName && (
                                    <p
                                        id="lastName-error"
                                        role="alert"
                                        className="mt-1 text-xs text-red-600"
                                    >
                                        {errors.lastName}
                                    </p>
                                )}
                            </div>

                            <div>
                                <label
                                    htmlFor="studentNumber"
                                    className="block text-sm font-medium text-gray-700"
                                >
                                    {t("registerEtudiant.studentNumber")}
                                </label>
                                <input
                                    id="studentNumber"
                                    type="text"
                                    value={form.studentNumber}
                                    onChange={handleChange}
                                    className={inputClass("studentNumber")}
                                    placeholder={t("registerEtudiant.placeholders.studentNumber")}
                                    aria-invalid={!!errors.studentNumber}
                                    aria-describedby={
                                        errors.studentNumber ? "studentNumber-error" : undefined
                                    }
                                />
                                {errors.studentNumber && (
                                    <p
                                        id="studentNumber-error"
                                        role="alert"
                                        className="mt-1 text-xs text-red-600"
                                    >
                                        {errors.studentNumber}
                                    </p>
                                )}
                            </div>

                            <div>
                                <label
                                    htmlFor="program"
                                    className="block text-sm font-medium text-gray-700"
                                >
                                    {t("registerEtudiant.program")}
                                </label>
                                <input
                                    id="program"
                                    type="text"
                                    value={form.program}
                                    onChange={handleChange}
                                    className={inputClass("program")}
                                    placeholder={t("registerEtudiant.placeholders.program")}
                                    aria-invalid={!!errors.program}
                                    aria-describedby={errors.program ? "program-error" : undefined}
                                />
                                {errors.program && (
                                    <p
                                        id="program-error"
                                        role="alert"
                                        className="mt-1 text-xs text-red-600"
                                    >
                                        {errors.program}
                                    </p>
                                )}
                            </div>

                            <div>
                                <label
                                    htmlFor="email"
                                    className="block text-sm font-medium text-gray-700"
                                >
                                    {t("registerEtudiant.email")}
                                </label>
                                <input
                                    id="email"
                                    type="email"
                                    value={form.email}
                                    onChange={handleChange}
                                    className={inputClass("email")}
                                    placeholder={t("registerEtudiant.placeholders.email")}
                                    aria-invalid={!!errors.email}
                                    aria-describedby={errors.email ? "email-error" : undefined}
                                />
                                {errors.email && (
                                    <p
                                        id="email-error"
                                        role="alert"
                                        className="mt-1 text-xs text-red-600"
                                    >
                                        {errors.email}
                                    </p>
                                )}
                            </div>

                            <div>
                                <label
                                    htmlFor="password"
                                    className="block text-sm font-medium text-gray-700"
                                >
                                    {t("registerEtudiant.password")}
                                </label>
                                <input
                                    id="password"
                                    type="password"
                                    value={form.password}
                                    onChange={handleChange}
                                    className={inputClass("password")}
                                    placeholder={t("registerEtudiant.placeholders.password")}
                                    aria-invalid={!!errors.password}
                                    aria-describedby={errors.password ? "password-error" : undefined}
                                />
                                {errors.password && (
                                    <p
                                        id="password-error"
                                        role="alert"
                                        className="mt-1 text-xs text-red-600"
                                    >
                                        {errors.password}
                                    </p>
                                )}
                            </div>
                        </div>

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
                                    {isSubmitting ? (
                                        <>
                                            <svg
                                                className="animate-spin -ml-1 mr-2 h-5 w-5 text-white"
                                                xmlns="http://www.w3.org/2000/svg"
                                                fill="none"
                                                viewBox="0 0 24 24"
                                            >
                                                <circle
                                                    className="opacity-25"
                                                    cx="12"
                                                    cy="12"
                                                    r="10"
                                                    stroke="currentColor"
                                                    strokeWidth="4"
                                                ></circle>
                                                <path
                                                    className="opacity-75"
                                                    fill="currentColor"
                                                    d="M4 12a8 8 0 018-8v4a4 4 0 00-4 4H4z"
                                                ></path>
                                            </svg>
                                            {t("registerEtudiant.submitting")}
                                        </>
                                    ) : (
                                        t("registerEtudiant.submit")
                                    )}
                                </button>

                                <button
                                    type="button"
                                    onClick={handleReset}
                                    disabled={isSubmitting}
                                    className="inline-flex items-center px-4 py-2 border border-gray-300 text-sm font-medium rounded-md bg-white text-gray-700 hover:bg-gray-50"
                                >
                                    {t("registerEtudiant.reset")}
                                </button>
                            </div>

                            <div className="text-sm text-gray-500">
                                {t("registerEtudiant.alreadyAccount")}{" "}
                                <button
                                    type="button"
                                    onClick={() => navigate("/login")}
                                    className="text-indigo-600 hover:underline"
                                >
                                    {t("registerEtudiant.login")}
                                </button>
                            </div>
                            <div className="text-sm text-gray-500">
                                {t("registerEtudiant.employerAccount")}{" "}
                                <button
                                    type="button"
                                    onClick={() => navigate("/register/employeur")}
                                    className="text-indigo-600 hover:underline"
                                >
                                    {t("registerEtudiant.createEmployer")}
                                </button>
                            </div>
                        </div>
                    </form>
                </div>
            </div>
        </div>
    );
}