
import React, { useState } from "react";
import { registerEmployeur } from "../api/apiRegister.jsx";
import { useNavigate} from "react-router";


const initialState = {
    firstName: "",
    lastName: "",
    email: "",
    password: "",
    companyName: "",
    field: ""
};

function validate(values) {
    const errors = {};
    if (!values.firstName.trim()) errors.firstName = "Prénom requis";
    if (!values.lastName.trim()) errors.lastName = "Nom requis";
    if (!values.companyName.trim()) errors.companyName = "Nom de l'entreprise requis";
    if (!values.field.trim()) errors.field = "Secteur d'activité requis";
    if (!values.email.trim()) errors.email = "Email requis";
    else if (!/^[^\s@]+@[^\s@]+\.[^\s@]+$/.test(values.email)) errors.email = "Email invalide";
    if (!values.password) errors.password = "Mot de passe requis";
    else if (values.password.length < 8) errors.password = "Le mot de passe doit contenir au moins 8 caractères";
    return errors;
}

export default function RegisterEmployeur() {
    const navigate = useNavigate();
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
        const v = validate(form);
        setErrors(v);
        if (Object.keys(v).length > 0) {
            const firstKey = Object.keys(v)[0];
            const el = document.getElementById(firstKey);
            if (el) el.focus();
            return;
        }

        setIsSubmitting(true);
        try {
            await registerEmployeur(form);
            setSuccessMessage("Inscription réussie !");
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
                        parts.push(`${key}: ${typeof data[key] === "string" ? data[key] : JSON.stringify(data[key])}`);
                    }
                    setGlobalError(parts.join(" · "));
                } else {
                    setGlobalError("Une erreur est survenue.");
                }
            } else {
                setGlobalError("Impossible de se connecter au serveur.");
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
                    <h1 className="text-3xl font-bold text-indigo-600">LeandrOSE</h1>
                </header>

                <div className="bg-white rounded-lg shadow-md p-6 md:p-10">
                    <h2 className="text-2xl font-semibold text-gray-800 mb-2">Inscription Employeur</h2>
                    <p className="text-sm text-gray-500 mb-6">
                        Créez un compte employeur pour publier des offres et gérer vos candidats.
                    </p>

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
                            <div>
                                <label htmlFor="firstName" className="block text-sm font-medium text-gray-700">Prénom</label>
                                <input
                                    id="firstName"
                                    type="text"
                                    value={form.firstName}
                                    onChange={handleChange}
                                    className={inputClass("firstName")}
                                    placeholder="Jean"
                                    aria-invalid={!!errors.firstName}
                                    aria-describedby={errors.firstName ? "firstName-error" : undefined}
                                />
                                {errors.firstName && <p id="firstName-error" role="alert" className="mt-1 text-xs text-red-600">{errors.firstName}</p>}
                            </div>

                            <div>
                                <label htmlFor="lastName" className="block text-sm font-medium text-gray-700">Nom</label>
                                <input
                                    id="lastName"
                                    type="text"
                                    value={form.lastName}
                                    onChange={handleChange}
                                    className={inputClass("lastName")}
                                    placeholder="Dupont"
                                    aria-invalid={!!errors.lastName}
                                    aria-describedby={errors.lastName ? "lastName-error" : undefined}
                                />
                                {errors.lastName && <p id="lastName-error" role="alert" className="mt-1 text-xs text-red-600">{errors.lastName}</p>}
                            </div>

                            <div>
                                <label htmlFor="companyName" className="block text-sm font-medium text-gray-700">Nom de l'entreprise</label>
                                <input
                                    id="companyName"
                                    type="text"
                                    value={form.companyName}
                                    onChange={handleChange}
                                    className={inputClass("companyName")}
                                    placeholder="ACME SARL"
                                    aria-invalid={!!errors.companyName}
                                    aria-describedby={errors.companyName ? "companyName-error" : undefined}
                                />
                                {errors.companyName && <p id="companyName-error" role="alert" className="mt-1 text-xs text-red-600">{errors.companyName}</p>}
                            </div>

                            <div>
                                <label htmlFor="field" className="block text-sm font-medium text-gray-700">Secteur d'activité</label>
                                <input
                                    id="field"
                                    type="text"
                                    value={form.field}
                                    onChange={handleChange}
                                    className={inputClass("field")}
                                    placeholder="Informatique, Santé..."
                                    aria-invalid={!!errors.field}
                                    aria-describedby={errors.field ? "field-error" : undefined}
                                />
                                {errors.field && <p id="field-error" role="alert" className="mt-1 text-xs text-red-600">{errors.field}</p>}
                            </div>

                            <div>
                                <label htmlFor="email" className="block text-sm font-medium text-gray-700">Email professionnel</label>
                                <input
                                    id="email"
                                    type="email"
                                    value={form.email}
                                    onChange={handleChange}
                                    className={inputClass("email")}
                                    placeholder="contact@entreprise.com"
                                    aria-invalid={!!errors.email}
                                    aria-describedby={errors.email ? "email-error" : undefined}
                                />
                                {errors.email && <p id="email-error" role="alert" className="mt-1 text-xs text-red-600">{errors.email}</p>}
                            </div>

                            <div>
                                <label htmlFor="password" className="block text-sm font-medium text-gray-700">Mot de passe</label>
                                <input
                                    id="password"
                                    type="password"
                                    value={form.password}
                                    onChange={handleChange}
                                    className={inputClass("password")}
                                    placeholder="Au moins 8 caractères"
                                    aria-invalid={!!errors.password}
                                    aria-describedby={errors.password ? "password-error" : undefined}
                                />
                                {errors.password && <p id="password-error" role="alert" className="mt-1 text-xs text-red-600">{errors.password}</p>}
                            </div>
                        </div>

                        <div className="mt-6 flex items-center justify-between gap-4">
                            <div className="flex gap-2">
                                <button
                                    type="submit"
                                    disabled={isSubmitting}
                                    className={`inline-flex items-center px-4 py-2 border border-transparent text-sm font-medium rounded-md text-white ${isSubmitting ? "bg-indigo-300" : "bg-indigo-600 hover:bg-indigo-700"}`}
                                >
                                    {isSubmitting ? (
                                        <>
                                            <svg className="animate-spin -ml-1 mr-2 h-5 w-5 text-white" xmlns="http://www.w3.org/2000/svg" fill="none" viewBox="0 0 24 24">
                                                <circle className="opacity-25" cx="12" cy="12" r="10" stroke="currentColor" strokeWidth="4"></circle>
                                                <path className="opacity-75" fill="currentColor" d="M4 12a8 8 0 018-8v4a4 4 0 00-4 4H4z"></path>
                                            </svg>
                                            En cours...
                                        </>
                                    ) : "S'inscrire"}
                                </button>

                                <button
                                    type="button"
                                    onClick={handleReset}
                                    disabled={isSubmitting}
                                    className="inline-flex items-center px-4 py-2 border border-gray-300 text-sm font-medium rounded-md bg-white text-gray-700 hover:bg-gray-50"
                                >
                                    Réinitialiser
                                </button>
                            </div>

                            <div className="text-sm text-gray-500">
                                Déjà un compte ? <button type="button" onClick={() => navigate("/login")} className="text-indigo-600 hover:underline">Se connecter</button>
                            </div>
                            <div className="text-sm text-gray-500">
                                Vous êtes étudiant ?{" "}
                                <button
                                    type="button"
                                    onClick={() => navigate("/register/etudiant")}
                                    className="text-indigo-600 hover:underline"
                                >
                                    Créez un compte étudiant
                                </button>
                            </div>

                        </div>
                    </form>
                </div>
            </div>
        </div>
    );
}