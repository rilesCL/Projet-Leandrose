import React, { useState } from "react";
import { useNavigate } from "react-router";
import { useTranslation } from "react-i18next";
import { registerEmployeur } from "../api/apiRegister.jsx";

const initialState = {
    firstName: "",
    lastName: "",
    email: "",
    password: "",
    confirmPassword: "",
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
    if (!values.confirmPassword) errors.confirmPassword = t("registerEmployeur.errors.confirmPassword");
    else if (values.confirmPassword !== values.password)
        errors.confirmPassword = t("registerEmployeur.errors.passwordMismatch");
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
        const formData = { ...form };

        try {
            await registerEmployeur(formData);
            setSuccessMessage(t("registerEmployeur.success"));
            setForm(initialState);
            setErrors({});
            setTimeout(() => {
                setSuccessMessage("");
                navigate("/login");
            }, 1500);
        } catch (err) {
            console.error(err);
            const message = err.response?.data;

            const errorText = typeof message === 'object' ? message.error : message;

            if (errorText?.includes("déjà utilisé")) {
                setGlobalError(t("registerEmployeur.errors.emailUsed"));
            } else {
                setGlobalError(errorText || t("registerEmployeur.errors.serverError"));
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
                            <InputField id="firstName" label={t("registerEmployeur.firstName")} placeholder={t("registerEmployeur.placeholders.firstName")} value={form.firstName} onChange={handleChange} error={errors.firstName} />
                            <InputField id="lastName" label={t("registerEmployeur.lastName")} placeholder={t("registerEmployeur.placeholders.lastName")} value={form.lastName} onChange={handleChange} error={errors.lastName} />
                            <InputField id="companyName" label={t("registerEmployeur.companyName")} placeholder={t("registerEmployeur.placeholders.companyName")} value={form.companyName} onChange={handleChange} error={errors.companyName} />
                            <InputField id="email" label={t("registerEmployeur.email")} placeholder={t("registerEmployeur.placeholders.email")} value={form.email} onChange={handleChange} error={errors.email} type="email" />
                            <InputField id="field" className="md:col-span-2" label={t("registerEmployeur.field")} placeholder={t("registerEmployeur.placeholders.field")} value={form.field} onChange={handleChange} error={errors.field} />
                            <InputField id="password" label={t("registerEmployeur.password")} placeholder={t("registerEmployeur.placeholders.password")} value={form.password} onChange={handleChange} error={errors.password} type="password" />
                            <InputField id="confirmPassword" label={t("registerEmployeur.confirmPassword")} placeholder={t("registerEmployeur.placeholders.confirmPassword")} value={form.confirmPassword} onChange={handleChange} error={errors.confirmPassword} type="password" />
                        </div>

                        <div className="mt-6 flex flex-col md:flex-row md:items-center md:justify-between gap-4">
                            <div className="flex gap-2">
                                <button type="submit" disabled={isSubmitting} className={`inline-flex items-center px-4 py-2 border border-transparent text-sm font-medium rounded-md text-white ${isSubmitting ? "bg-indigo-300" : "bg-indigo-600 hover:bg-indigo-700"}`}>
                                    {isSubmitting ? t("registerEmployeur.submitting") : t("registerEmployeur.submit")}
                                </button>
                                <button type="button" onClick={handleReset} disabled={isSubmitting} className="inline-flex items-center px-4 py-2 border border-gray-300 text-sm font-medium rounded-md bg-white text-gray-700 hover:bg-gray-50">
                                    {t("registerEmployeur.reset")}
                                </button>
                            </div>

                            <div className="text-sm text-gray-500">
                                {t("registerEmployeur.alreadyAccount")}{" "}
                                <button type="button" onClick={() => navigate("/login")} className="text-indigo-600 hover:underline">{t("registerEmployeur.login")}</button>
                            </div>
                            <div className="text-sm text-gray-500">
                                {t("registerEmployeur.studentAccount")}{" "}
                                <button type="button" onClick={() => navigate("/register/etudiant")} className="text-indigo-600 hover:underline">{t("registerEmployeur.createStudent")}</button>
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