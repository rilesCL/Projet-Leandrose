import React, { useState, useEffect } from "react";
import { useNavigate, useSearchParams } from "react-router-dom";
import { useTranslation } from "react-i18next";
import { FaArrowLeft } from "react-icons/fa";
import { creerEntente, getCandidaturesAcceptees } from "../api/apiGestionnaire";

export default function CreateEntenteForm() {
    const { t } = useTranslation();
    const navigate = useNavigate();
    const [searchParams] = useSearchParams();
    const candidatureIdFromUrl = searchParams.get("candidatureId");

    const [candidatures, setCandidatures] = useState([]);
    const [loading, setLoading] = useState(false);
    const [error, setError] = useState(null);
    const [success, setSuccess] = useState(false);

    const [formData, setFormData] = useState({
        candidatureId: candidatureIdFromUrl || "",
        missionsObjectifs: ""
    });

    const [errors, setErrors] = useState({});

    useEffect(() => {
        const token = sessionStorage.getItem("accessToken");
        if (!token) {
            navigate("/login");
            return;
        }

        const fetchCandidatures = async () => {
            try {
                const data = await getCandidaturesAcceptees();
                setCandidatures(Array.isArray(data) ? data : []);
            } catch (err) {
                console.error("Erreur lors du chargement des candidatures:", err);
                setError(t("createEntenteForm.loadError"));
            }
        };

        fetchCandidatures();
    }, [navigate, t]);

    const handleChange = (e) => {
        const { name, value } = e.target;
        setFormData(prev => ({ ...prev, [name]: value }));
        if (errors[name]) setErrors(prev => ({ ...prev, [name]: null }));
    };

    const validateForm = () => {
        const newErrors = {};
        if (!formData.candidatureId)
            newErrors.candidatureId = t("createEntenteForm.candidatureRequired");
        if (!formData.missionsObjectifs || formData.missionsObjectifs.trim().length === 0)
            newErrors.missionsObjectifs = t("createEntenteForm.missionsRequired");

        setErrors(newErrors);
        return Object.keys(newErrors).length === 0;
    };

    // 🔧 Fonction pour formater la date au format ISO (YYYY-MM-DD)
    const formatDateForBackend = (dateString) => {
        if (!dateString) return null;

        // Si c'est déjà au format ISO (YYYY-MM-DD), le garder
        if (typeof dateString === 'string' && dateString.match(/^\d{4}-\d{2}-\d{2}$/)) {
            return dateString;
        }

        // Si c'est un timestamp ou un format avec heure, parser et formater
        try {
            const date = new Date(dateString);
            if (isNaN(date.getTime())) return null;

            // Format YYYY-MM-DD
            const year = date.getFullYear();
            const month = String(date.getMonth() + 1).padStart(2, '0');
            const day = String(date.getDate()).padStart(2, '0');
            return `${year}-${month}-${day}`;
        } catch (e) {
            console.error("Erreur formatage date:", e);
            return null;
        }
    };

    const handleSubmit = async (e) => {
        e.preventDefault();
        if (!validateForm()) return;

        setLoading(true);
        setError(null);
        setSuccess(false);

        try {
            const selectedCandidature = candidatures.find(
                c => c.id === parseInt(formData.candidatureId)
            );

            if (!selectedCandidature) {
                throw new Error("Candidature non trouvée");
            }

            const payload = {
                candidatureId: parseInt(formData.candidatureId),
                dateDebut: formatDateForBackend(selectedCandidature.internshipOffer?.startDate),
                duree: selectedCandidature.internshipOffer?.durationInWeeks || 0,
                lieu: selectedCandidature.internshipOffer?.address || "",
                remuneration: selectedCandidature.internshipOffer?.remuneration || 0,
                missionsObjectifs: formData.missionsObjectifs.trim()
            };

            console.log("📤 Payload envoyé au backend:", JSON.stringify(payload, null, 2));

            const result = await creerEntente(payload);

            console.log("📥 Réponse du backend:", result);

            if (result.error) {
                setError(result.error.message || t("createEntenteForm.createError"));
            } else {
                setSuccess(true);
            }
        } catch (err) {
            console.error("❌ Erreur création entente:", err);
            setError(err.message || t("createEntenteForm.createError"));
        } finally {
            setLoading(false);
        }
    };

    const handleCancel = () => navigate("/dashboard/gestionnaire/ententes");

    const selectedCandidature = candidatures.find(
        c => c.id === parseInt(formData.candidatureId)
    );

    const formatDate = (dateString) => {
        if (!dateString) return t("createEntenteForm.notDefined");
        const date = new Date(dateString);
        return date.toLocaleDateString();
    };

    return (
        <div className="max-w-5xl mx-auto bg-white shadow rounded p-6">
            {/* Bouton retour */}
            <button
                onClick={handleCancel}
                className="mb-4 flex items-center text-gray-600 hover:text-indigo-600 transition-colors"
            >
                <FaArrowLeft className="mr-2" />
                <span>{t("createEntenteForm.back")}</span>
            </button>

            <h2 className="text-2xl font-semibold mb-6">{t("createEntenteForm.title")}</h2>

            {error && (
                <div className="mb-4 p-3 bg-red-50 border border-red-200 text-red-700 rounded">
                    {error}
                </div>
            )}
            {success && (
                <div className="mb-4 p-3 bg-green-50 border border-green-200 text-green-700 rounded">
                    {t("createEntenteForm.success")}
                </div>
            )}

            <form onSubmit={handleSubmit} className="space-y-6">
                {/* Sélection de la candidature */}
                <div>
                    <label className="block text-sm font-medium text-gray-700 mb-1">
                        {t("createEntenteForm.candidatureLabel")}
                    </label>
                    <select
                        name="candidatureId"
                        value={formData.candidatureId}
                        onChange={handleChange}
                        disabled={!!candidatureIdFromUrl}
                        className={`w-full px-3 py-2 border rounded-md shadow-sm focus:ring-indigo-500 focus:border-indigo-500 ${
                            errors.candidatureId ? "border-red-500" : "border-gray-300"
                        } ${candidatureIdFromUrl ? "bg-gray-100" : ""}`}
                    >
                        <option value="">{t("createEntenteForm.candidaturePlaceholder")}</option>
                        {candidatures.map(c => (
                            <option key={c.id} value={c.id}>
                                {c.student.firstName} {c.student.lastName} - {c.internshipOffer.description}
                            </option>
                        ))}
                    </select>
                    {errors.candidatureId && (
                        <p className="mt-1 text-sm text-red-600">{errors.candidatureId}</p>
                    )}
                </div>

                {/* Informations de la candidature sélectionnée */}
                {selectedCandidature && (
                    <div className="space-y-4">
                        {/* Étudiant */}
                        <div className="p-4 bg-blue-50 rounded-lg border border-blue-200">
                            <h3 className="font-semibold text-blue-900 mb-3">
                                {t("createEntenteForm.studentInfo")}
                            </h3>
                            <div className="grid grid-cols-1 md:grid-cols-2 gap-3 text-sm">
                                <div>
                                    <span className="font-medium text-gray-700">
                                        {t("createEntenteForm.fullName")} :
                                    </span>
                                    <p className="text-gray-900">
                                        {selectedCandidature.student.firstName} {selectedCandidature.student.lastName}
                                    </p>
                                </div>
                                <div>
                                    <span className="font-medium text-gray-700">
                                        {t("createEntenteForm.email")} :
                                    </span>
                                    <p className="text-gray-900">{selectedCandidature.student.email}</p>
                                </div>
                                <div>
                                    <span className="font-medium text-gray-700">
                                        {t("createEntenteForm.studentNumber")} :
                                    </span>
                                    <p className="text-gray-900">{selectedCandidature.student.studentNumber}</p>
                                </div>
                                <div>
                                    <span className="font-medium text-gray-700">
                                        {t("createEntenteForm.program")} :
                                    </span>
                                    <p className="text-gray-900">{selectedCandidature.student.program}</p>
                                </div>
                            </div>
                        </div>

                        {/* Entreprise / Offre */}
                        <div className="p-4 bg-purple-50 rounded-lg border border-purple-200">
                            <h3 className="font-semibold text-purple-900 mb-3">
                                {t("createEntenteForm.companyInfo")}
                            </h3>
                            <div className="grid grid-cols-1 md:grid-cols-2 gap-3 text-sm">
                                <div>
                                    <span className="font-medium text-gray-700">
                                        {t("createEntenteForm.companyName")} :
                                    </span>
                                    <p className="text-gray-900">
                                        {selectedCandidature.internshipOffer.companyName}
                                    </p>
                                </div>
                                <div>
                                    <span className="font-medium text-gray-700">
                                        {t("createEntenteForm.offerDescription")} :
                                    </span>
                                    <p className="text-gray-900">
                                        {selectedCandidature.internshipOffer.description}
                                    </p>
                                </div>
                                <div>
                                    <span className="font-medium text-gray-700">
                                        {t("createEntenteForm.startDate")} :
                                    </span>
                                    <p className="text-gray-900">
                                        {formatDate(selectedCandidature.internshipOffer.startDate)}
                                    </p>
                                </div>
                                <div>
                                    <span className="font-medium text-gray-700">
                                        {t("createEntenteForm.duration")} :
                                    </span>
                                    <p className="text-gray-900">
                                        {selectedCandidature.internshipOffer.durationInWeeks} {t("createEntenteForm.weeks")}
                                    </p>
                                </div>
                                <div>
                                    <span className="font-medium text-gray-700">
                                        {t("createEntenteForm.location")} :
                                    </span>
                                    <p className="text-gray-900">
                                        {selectedCandidature.internshipOffer.address}
                                    </p>
                                </div>
                                <div>
                                    <span className="font-medium text-gray-700">
                                        {t("createEntenteForm.remuneration")} :
                                    </span>
                                    <p className="text-gray-900">
                                        {selectedCandidature.internshipOffer.remuneration
                                            ? `${selectedCandidature.internshipOffer.remuneration} $/h`
                                            : t("createEntenteForm.notPaid")}
                                    </p>
                                </div>
                            </div>
                        </div>
                    </div>
                )}

                {/* Missions et objectifs */}
                <div>
                    <label className="block text-sm font-medium text-gray-700 mb-1">
                        {t("createEntenteForm.missionsLabel")}
                    </label>
                    <textarea
                        name="missionsObjectifs"
                        value={formData.missionsObjectifs}
                        onChange={handleChange}
                        rows="8"
                        placeholder={t("createEntenteForm.missionsPlaceholder")}
                        className={`w-full px-3 py-2 border rounded-md shadow-sm focus:ring-indigo-500 focus:border-indigo-500 ${
                            errors.missionsObjectifs ? "border-red-500" : "border-gray-300"
                        }`}
                    />
                    {errors.missionsObjectifs && (
                        <p className="mt-1 text-sm text-red-600">{errors.missionsObjectifs}</p>
                    )}
                </div>

                <div className="flex items-center justify-end space-x-3 pt-4 border-t">
                    <button
                        type="button"
                        onClick={handleCancel}
                        disabled={loading}
                        className="px-4 py-2 text-sm font-medium text-gray-700 bg-white border border-gray-300 rounded-md hover:bg-gray-50 disabled:opacity-50 transition-colors"
                    >
                        {t("createEntenteForm.cancelButton")}
                    </button>
                    <button
                        type="submit"
                        disabled={loading || success}
                        className="px-4 py-2 text-sm font-medium text-white bg-indigo-600 border border-transparent rounded-md hover:bg-indigo-700 disabled:opacity-50 transition-colors"
                    >
                        {loading ? t("createEntenteForm.submitting") : t("createEntenteForm.submitButton")}
                    </button>
                </div>
            </form>
        </div>
    );
}