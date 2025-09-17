import React, { useState } from "react";
import {registerStudent, registerEmployeur} from "../api/apiEmployeur.jsx";

export default function RegistrationForm() {
    const initialFormData = {
        firstName: "",
        lastName: "",
        email: "",
        password: "",
        companyName: "",
        field: "",
        programme: "",
        numero_matricule: "",
    };

    const [userType, setUserType] = useState("student");
    const [formData, setFormData] = useState(initialFormData);
    const [errors, setErrors] = useState({});
    const [loading, setLoading] = useState(false);
    const [success, setSuccess] = useState(null);

    const handleChange = (e) => {
        setFormData({
            ...formData,
            [e.target.name]: e.target.value,
        });
    };

    const validateForm = () => {
        const newErrors = {};

        if (!formData.firstName.trim())
            newErrors.firstName = "Le prénom est requis";
        if (!formData.lastName.trim())
            newErrors.lastName = "Le nom de famille est requis";
        if (!formData.email.trim()) newErrors.email = "L'email est requis";
        else if (!/^[^\s@]+@[^\s@]+\.[^\s@]+$/.test(formData.email))
            newErrors.email = "Email invalide";
        if (!formData.password) newErrors.password = "Mot de passe requis";

        if (userType === "student") {
            if (!formData.programme.trim())
                newErrors.programme = "Le nom du programme est requis";
            if (!formData.numero_matricule.trim())
                newErrors.numero_matricule = "Le numéro de matricule est requis";
        }

        if (userType === "employeur") {
            if (!formData.companyName.trim())
                newErrors.companyName = "Le nom de compagnie est requis";
            if (!formData.field.trim())
                newErrors.field = "Le secteur d'activité est requis";
        }

        setErrors(newErrors);
        return Object.keys(newErrors).length === 0;
    };

    const handleSubmit = async (e) => {
        e.preventDefault();
        setSuccess(null);

        if (!validateForm()) return;

        setLoading(true);
        try {
            if (userType === "student") {
                await registerStudent(formData);
                setSuccess("Étudiant inscrit avec succès !");
            } else {
                await registerEmployeur(formData);
                setSuccess("Employeur inscrit avec succès !");
            }

            setFormData(initialFormData);
            setErrors({});
        } catch (err) {
            setErrors({ global: err.response?.data || "Erreur serveur" });
        } finally {
            setLoading(false);
        }
    };

    const renderInput = (name, label, type = "text") => (
        <div>
            <label className="block font-medium">
                {label}
                <input
                    type={type}
                    name={name}
                    value={formData[name] ?? ""}
                    onChange={handleChange}
                    className={`border p-2 w-full mt-1 ${
                        errors[name] ? "border-red-500" : ""
                    }`}
                />
            </label>
            {errors[name] && (
                <p className="text-red-600 text-sm mt-1">{errors[name]}</p>
            )}
        </div>
    );

    return (
        <div className="max-w-md mx-auto p-4 border rounded">
            <h2 className="text-xl font-bold mb-4">Inscription {userType}</h2>

            {/* Switch user type */}
            <div className="mb-4">
                <label className="mr-4">
                    <input
                        type="radio"
                        value="student"
                        checked={userType === "student"}
                        onChange={() => {
                            setUserType("student");
                            setFormData(initialFormData);
                        }}
                    />
                    Étudiant
                </label>
                <label>
                    <input
                        type="radio"
                        value="employeur"
                        checked={userType === "employeur"}
                        onChange={() => {
                            setUserType("employeur");
                            setFormData(initialFormData);
                        }}
                    />
                    Employeur
                </label>
            </div>

            <form onSubmit={handleSubmit} className="space-y-4">
                {renderInput("firstName", "Prénom")}
                {renderInput("lastName", "Nom de famille")}
                {renderInput("email", "Email", "email")}
                {renderInput("password", "Mot de passe", "password")}

                {userType === "student" && (
                    <>
                        {renderInput("programme", "Programme")}
                        {renderInput("numero_matricule", "Numéro de matricule")}
                    </>
                )}

                {userType === "employeur" && (
                    <>
                        {renderInput("companyName", "Nom de la compagnie")}
                        {renderInput("field", "Secteur d'activité")}
                    </>
                )}

                <button
                    type="submit"
                    className="bg-blue-600 text-white px-4 py-2 rounded"
                    disabled={loading}
                >
                    {loading ? "Envoi..." : "S'inscrire"}
                </button>

                <div className="text-sm text-gray-500">
                    Déjà un compte ? <button type="button" onClick={() => navigate("/login")} className="text-indigo-600 hover:underline">Se connecter</button>
                </div>
            </form>

            {errors.global && <p className="text-red-600 mt-2">{errors.global}</p>}
            {success && <p className="text-green-600 mt-2">{success}</p>}
        </div>
    );
}
