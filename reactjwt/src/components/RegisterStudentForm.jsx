import React, { useState, useEffect } from "react";
import { registerStudent, fetchPrograms } from "../api/apiRegister.jsx";
import { useNavigate } from "react-router";
import { useTranslation } from "react-i18next";
import { FaArrowLeft } from "react-icons/fa";
import LanguageSelector from "./LanguageSelector.jsx";

const initialState = {
    firstName: "",
    lastName: "",
    email: "",
    password: "",
    confirmPassword: "",
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

    if (!values.confirmPassword) errors.confirmPassword = t("registerEtudiant.errors.confirmPassword");
    else if (values.confirmPassword !== values.password)
        errors.confirmPassword = t("registerEtudiant.errors.passwordMismatch");

    return errors;
}

export default function RegisterEtudiant() {
    const navigate = useNavigate();
    const { t } = useTranslation();
    const [form, setForm] = useState(initialState);
    const [errors, setErrors] = useState({});
    const [globalError, setGlobalError] = useState("");
    const [successMessage, setSuccessMessage] = useState("");
    const [isSubmitting, setIsSubmitting] = useState(false);
    const [programs, setPrograms] = useState([]);
    const [loadingPrograms, setLoadingPrograms] = useState(true);

    useEffect(() => {
        const loadPrograms = async () => {
            try {
                const data = await fetchPrograms();
                setPrograms(data);
            } catch (error) {
                console.error("Error fetching programs:", error);
                setGlobalError(t("registerEtudiant.errors.programsLoadError"));
            } finally {
                setLoadingPrograms(false);
            }
        };
        loadPrograms();
    }, [t]);

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

            setTimeout(async () => {
                const loginRes = await fetch("http://localhost:8080/user/login", {
                    method: "POST",
                    headers: { "Content-Type": "application/json" },
                    body: JSON.stringify({ email: form.email, password: form.password })
                });
                const loginData = await loginRes.json();
                sessionStorage.setItem("accessToken", loginData.accessToken);
                sessionStorage.setItem("tokenType", loginData.tokenType || "BEARER");

                const meRes = await fetch("http://localhost:8080/user/me", {
                    headers: { Authorization: `Bearer ${loginData.accessToken}` }
                });
                const userData = await meRes.json();

                switch (userData.role) {
                    case "STUDENT":
                        navigate("/dashboard/student");
                        break;
                    case "EMPLOYEUR":
                        navigate("/dashboard/employeur");
                        break;
                    default:
                        navigate("/dashboard");
                }
            }, 1500);
        } catch (err) {
            console.error(err);
            const message = err.response?.data;
            const errorText = typeof message === "object" ? message.error : message;

            if (errorText?.includes("déjà utilisé")) {
                setGlobalError(t("registerEtudiant.errors.emailUsed"));
            } else {
                setGlobalError(errorText || t("registerEtudiant.errors.serverError"));
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

    return (
        <div className="min-h-screen bg-gray-50 flex items-center justify-center p-4">
            <div className="max-w-3xl w-full">
                <header className="text-center mb-6">
                    <h1 className="text-3xl font-bold text-indigo-600">{t("appName")}</h1>
                </header>

                <div className="bg-white rounded-lg shadow-md p-6 md:p-10">
                    <div className="flex justify-between items-center mb-4">
                        <h2 className="text-2xl font-semibold text-gray-800">{t("registerEtudiant.title")}</h2>
                        <LanguageSelector />
                    </div>

                    <p className="text-sm text-gray-500 mb-6">{t("registerEtudiant.subtitle")}</p>

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
                            <InputField
                                id="firstName"
                                label={t("registerEtudiant.firstName")}
                                placeholder={t("registerEtudiant.placeholders.firstName")}
                                value={form.firstName}
                                onChange={handleChange}
                                error={errors.firstName}
                            />

                            <InputField
                                id="lastName"
                                label={t("registerEtudiant.lastName")}
                                placeholder={t("registerEtudiant.placeholders.lastName")}
                                value={form.lastName}
                                onChange={handleChange}
                                error={errors.lastName}
                            />

                            <InputField
                                id="studentNumber"
                                label={t("registerEtudiant.studentNumber")}
                                placeholder={t("registerEtudiant.placeholders.studentNumber")}
                                value={form.studentNumber}
                                onChange={handleChange}
                                error={errors.studentNumber}
                            />

                            <InputField
                                id="email"
                                label={t("registerEtudiant.email")}
                                placeholder={t("registerEtudiant.placeholders.email")}
                                value={form.email}
                                onChange={handleChange}
                                error={errors.email}
                                type="email"
                            />

                            <SelectField
                                id="program"
                                className="md:col-span-2"
                                label={t("registerEtudiant.program")}
                                value={form.program}
                                onChange={handleChange}
                                error={errors.program}
                                options={programs}
                                loading={loadingPrograms}
                                placeholder={t("registerEtudiant.placeholders.program")}
                                t={t}
                            />

                            <InputField
                                id="password"
                                label={t("registerEtudiant.password")}
                                placeholder={t("registerEtudiant.placeholders.password")}
                                value={form.password}
                                onChange={handleChange}
                                error={errors.password}
                                type="password"
                            />

                            <InputField
                                id="confirmPassword"
                                label={t("registerEtudiant.confirmPassword")}
                                placeholder={t("registerEtudiant.placeholders.confirmPassword")}
                                value={form.confirmPassword}
                                onChange={handleChange}
                                error={errors.confirmPassword}
                                type="password"
                            />
                        </div>

                        <div className="mt-6 flex flex-col md:flex-row md:items-center md:justify-between gap-4">
                            <div className="flex gap-2">
                                <button
                                    type="submit"
                                    disabled={isSubmitting}
                                    className={`inline-flex items-center px-4 py-2 border border-transparent text-sm font-medium rounded-md text-white ${isSubmitting ? "bg-indigo-300" : "bg-indigo-600 hover:bg-indigo-700"}`}
                                >
                                    {isSubmitting ? t("registerEtudiant.submitting") : t("registerEtudiant.submit")}
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
                                <button type="button" onClick={() => navigate("/register")} className="text-indigo-600 hover:underline">
                                    <FaArrowLeft />
                                </button>
                            </div>
                        </div>
                    </form>
                </div>
            </div>
        </div>
    );
}

function InputField({ id, label, placeholder, value, onChange, error, type = "text", className = "" }) {
    const inputClass = `mt-1 block w-full rounded-md shadow-sm border ${error ? "border-red-500" : "border-gray-300"} focus:ring-indigo-500 focus:border-indigo-500 px-3 py-2`;
    return (
        <div className={className}>
            <label htmlFor={id} className="block text-sm font-medium text-gray-700">{label}</label>
            <input
                id={id}
                type={type}
                value={value}
                onChange={onChange}
                className={inputClass}
                placeholder={placeholder}
                aria-invalid={!!error}
                aria-describedby={error ? `${id}-error` : undefined}
            />
            {error && <p id={`${id}-error`} role="alert" className="mt-1 text-xs text-red-600">{error}</p>}
        </div>
    );
}

function SelectField({ id, label, value, onChange, error, options, loading, placeholder, className = "", t }) {
    const selectClass = `mt-1 block w-full rounded-md shadow-sm border ${error ? "border-red-500" : "border-gray-300"} focus:ring-indigo-500 focus:border-indigo-500 px-3 py-2`;
    return (
        <div className={className}>
            <label htmlFor={id} className="block text-sm font-medium text-gray-700">{label}</label>
            <select
                id={id}
                value={value}
                onChange={onChange}
                className={selectClass}
                aria-invalid={!!error}
                aria-describedby={error ? `${id}-error` : undefined}
                disabled={loading}
            >
                <option value="">{loading ? t("registerEtudiant.loadingPrograms") : placeholder}</option>
                {options.map((program) => (
                    <option key={program.code} value={program.translationKey}>
                        {t(program.translationKey)}
                    </option>
                ))}
            </select>
            {error && <p id={`${id}-error`} role="alert" className="mt-1 text-xs text-red-600">{error}</p>}
        </div>
    );
}