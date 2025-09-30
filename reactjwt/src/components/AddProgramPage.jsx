import React, { useState } from "react";
import { useNavigate } from "react-router-dom";
import { useTranslation } from "react-i18next";
import { FaPlus } from "react-icons/fa";
import ProgramsListPage from "./ProgramListPage.jsx";

export default function AddProgramPage() {
    const navigate = useNavigate();
    const { t } = useTranslation();
    const [programName, setProgramName] = useState("");
    const [isSubmitting, setIsSubmitting] = useState(false);
    const [successMessage, setSuccessMessage] = useState("");
    const [errorMessage, setErrorMessage] = useState("");
    const [refreshKey, setRefreshKey] = useState(0);

    const handleBack = () => {
        navigate(-1);
    };

    const handleSubmit = async (e) => {
        e.preventDefault();

        if (!programName.trim()) {
            setErrorMessage(t("addProgram.errorEmptyName"));
            return;
        }

        setIsSubmitting(true);
        setErrorMessage("");
        setSuccessMessage("");

        const token = sessionStorage.getItem("accessToken");

        try {
            const response = await fetch("http://localhost:8080/gestionnaire/addProgram", {
                method: "POST",
                headers: {
                    Authorization: `Bearer ${token}`,
                    "Content-Type": "text/plain"  // Changé de application/json à text/plain
                },
                body: programName  // Envoi direct sans JSON.stringify
            });

            if (response.ok) {
                setSuccessMessage(t("addProgram.successMessage"));
                setProgramName("");
                setRefreshKey(prev => prev + 1);
                setTimeout(() => setSuccessMessage(""), 3000);
            } else {
                const data = await response.json();
                setErrorMessage(data.name || t("addProgram.errorAddProgram"));
            }
        } catch (err) {
            console.error("Error adding program:", err);
            setErrorMessage(t("addProgram.errorServer"));
        } finally {
            setIsSubmitting(false);
        }
    };

    return (
        <div className="min-h-screen bg-gray-50 py-8">
            <div className="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8">
                {/* Header */}
                <div className="mb-6">
                    <button
                        onClick={handleBack}
                        className="text-indigo-600 hover:text-indigo-800 font-medium mb-4"
                    >
                        ← {t("addProgram.back")}
                    </button>
                    <h1 className="text-3xl font-bold text-gray-900">
                        {t("addProgram.pageTitle")}
                    </h1>
                </div>

                {/* Section: Ajouter un nouveau programme */}
                <div className="bg-white rounded-lg shadow-md p-8 mb-8">
                    <div className="text-center mb-6">
                        <div className="inline-flex items-center justify-center w-16 h-16 bg-indigo-100 rounded-full mb-4">
                            <FaPlus className="text-3xl text-indigo-600" />
                        </div>
                        <h2 className="text-2xl font-semibold text-gray-900 mb-2">
                            {t("addProgram.newProgramTitle")}
                        </h2>
                        <p className="text-gray-600">
                            {t("addProgram.newProgramDescription")}
                        </p>
                    </div>

                    {/* Messages de succès et d'erreur */}
                    {successMessage && (
                        <div className="mb-4 bg-green-50 border border-green-200 rounded-lg p-4 text-center">
                            <p className="text-green-800 font-medium">{successMessage}</p>
                        </div>
                    )}

                    {errorMessage && (
                        <div className="mb-4 bg-red-50 border border-red-200 rounded-lg p-4 text-center">
                            <p className="text-red-800 font-medium">{errorMessage}</p>
                        </div>
                    )}

                    {/* Formulaire d'ajout */}
                    <form onSubmit={handleSubmit} className="max-w-2xl mx-auto">
                        <div className="mb-4">
                            <label htmlFor="programName" className="block text-sm font-medium text-gray-700 mb-2">
                                {t("addProgram.programNameLabel")}
                            </label>
                            <input
                                id="programName"
                                type="text"
                                value={programName}
                                onChange={(e) => setProgramName(e.target.value)}
                                placeholder={t("addProgram.programNamePlaceholder")}
                                className="w-full px-4 py-2 border border-gray-300 rounded-md focus:ring-2 focus:ring-indigo-500 focus:border-indigo-500"
                                disabled={isSubmitting}
                            />
                        </div>

                        <div className="flex justify-center">
                            <button
                                type="submit"
                                disabled={isSubmitting}
                                className={`flex items-center px-6 py-3 text-white font-medium rounded-md ${
                                    isSubmitting
                                        ? "bg-indigo-400 cursor-not-allowed"
                                        : "bg-indigo-600 hover:bg-indigo-700"
                                }`}
                            >
                                <FaPlus className="mr-2" />
                                {isSubmitting ? t("addProgram.submitting") : t("addProgram.submitButton")}
                            </button>
                        </div>
                    </form>
                </div>

                {/* Divider */}
                <div className="my-12 border-t-2 border-gray-300"></div>

                {/* Section: Liste des programmes existants */}
                <div className="mb-8">
                   <ProgramsListPage key={refreshKey} />
                </div>
            </div>
        </div>
    );
}