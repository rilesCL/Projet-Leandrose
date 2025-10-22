import React, { useState, useEffect } from "react";
import { useNavigate, useSearchParams } from "react-router-dom";
import { creerEntente, getCandidaturesAcceptees } from "../api/apiGestionnaire";

export default function CreateEntenteForm() {
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
                setError("Impossible de charger les candidatures");
            }
        };

        fetchCandidatures();
    }, [navigate]);

    const handleChange = (e) => {
        const { name, value } = e.target;
        setFormData(prev => ({ ...prev, [name]: value }));
        if (errors[name]) setErrors(prev => ({ ...prev, [name]: null }));
    };

    const validateForm = () => {
        const newErrors = {};
        if (!formData.candidatureId) newErrors.candidatureId = "La candidature est obligatoire";
        if (!formData.missionsObjectifs || formData.missionsObjectifs.trim().length === 0)
            newErrors.missionsObjectifs = "Les missions et objectifs sont obligatoires";

        setErrors(newErrors);
        return Object.keys(newErrors).length === 0;
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

            const payload = {
                candidatureId: parseInt(formData.candidatureId),
                dateDebut: selectedCandidature.internshipOffer?.startDate,
                duree: selectedCandidature.internshipOffer?.durationInWeeks,
                lieu: selectedCandidature.internshipOffer?.address,
                remuneration: selectedCandidature.internshipOffer?.remuneration,
                missionsObjectifs: formData.missionsObjectifs.trim()
            };

            const result = await creerEntente(payload);

            if (result.error) {
                setError(result.error.message || "Erreur lors de la création de l'entente");
            } else {
                setSuccess(true);
                setTimeout(() => navigate("/dashboard/gestionnaire/ententes"), 1500);
            }
        } catch (err) {
            console.error("Erreur création entente:", err);
            setError(err.message || "Erreur lors de la création de l'entente");
        } finally {
            setLoading(false);
        }
    };

    const handleCancel = () => navigate("/dashboard/gestionnaire/ententes");

    const selectedCandidature = candidatures.find(
        c => c.id === parseInt(formData.candidatureId)
    );

    const formatDate = (dateString) => {
        if (!dateString) return "—";
        const date = new Date(dateString);
        return date.toLocaleDateString('fr-CA');
    };

    return (
        <div className="max-w-5xl mx-auto bg-white shadow rounded p-6">
            <h2 className="text-2xl font-semibold mb-6">Créer une entente de stage</h2>

            {error && <div className="mb-4 p-3 bg-red-50 border border-red-200 text-red-700 rounded">{error}</div>}
            {success && <div className="mb-4 p-3 bg-green-50 border border-green-200 text-green-700 rounded">Entente créée avec succès ! Redirection...</div>}

            <form onSubmit={handleSubmit} className="space-y-6">
                {/* Sélection de la candidature */}
                <div>
                    <label className="block text-sm font-medium text-gray-700 mb-1">Candidature acceptée *</label>
                    <select
                        name="candidatureId"
                        value={formData.candidatureId}
                        onChange={handleChange}
                        disabled={!!candidatureIdFromUrl}
                        className={`w-full px-3 py-2 border rounded-md shadow-sm focus:ring-indigo-500 focus:border-indigo-500 ${
                            errors.candidatureId ? "border-red-500" : "border-gray-300"
                        } ${candidatureIdFromUrl ? "bg-gray-100" : ""}`}
                    >
                        <option value="">-- Sélectionner une candidature --</option>
                        {candidatures.map(c => (
                            <option key={c.id} value={c.id}>
                                {c.student.firstName} {c.student.lastName} - {c.internshipOffer.description}
                            </option>
                        ))}
                    </select>
                    {errors.candidatureId && <p className="mt-1 text-sm text-red-600">{errors.candidatureId}</p>}
                </div>

                {/* Informations de la candidature sélectionnée */}
                {selectedCandidature && (
                    <div className="space-y-4">
                        {/* Étudiant */}
                        <div className="p-4 bg-blue-50 rounded-lg border border-blue-200">
                            <h3 className="font-semibold text-blue-900 mb-3">Informations de l'étudiant</h3>
                            <div className="grid grid-cols-1 md:grid-cols-2 gap-3 text-sm">
                                <div>
                                    <span className="font-medium text-gray-700">Nom complet :</span>
                                    <p className="text-gray-900">{selectedCandidature.student.firstName} {selectedCandidature.student.lastName}</p>
                                </div>
                                <div>
                                    <span className="font-medium text-gray-700">Email :</span>
                                    <p className="text-gray-900">{selectedCandidature.student.email}</p>
                                </div>
                                <div>
                                    <span className="font-medium text-gray-700">Numéro étudiant :</span>
                                    <p className="text-gray-900">{selectedCandidature.student.studentNumber}</p>
                                </div>
                                <div>
                                    <span className="font-medium text-gray-700">Programme :</span>
                                    <p className="text-gray-900">{selectedCandidature.student.program}</p>
                                </div>
                            </div>
                        </div>

                        {/* Entreprise / Offre */}
                        <div className="p-4 bg-purple-50 rounded-lg border border-purple-200">
                            <h3 className="font-semibold text-purple-900 mb-3">Informations de l'entreprise</h3>
                            <div className="grid grid-cols-1 md:grid-cols-2 gap-3 text-sm">
                                <div>
                                    <span className="font-medium text-gray-700">Nom de l'entreprise :</span>
                                    <p className="text-gray-900">{selectedCandidature.internshipOffer.companyName}</p>
                                </div>
                                <div>
                                    <span className="font-medium text-gray-700">Description de l'offre :</span>
                                    <p className="text-gray-900">{selectedCandidature.internshipOffer.description}</p>
                                </div>
                                <div>
                                    <span className="font-medium text-gray-700">Date de début :</span>
                                    <p className="text-gray-900">{formatDate(selectedCandidature.internshipOffer.startDate)}</p>
                                </div>
                                <div>
                                    <span className="font-medium text-gray-700">Durée :</span>
                                    <p className="text-gray-900">{selectedCandidature.internshipOffer.durationInWeeks} semaines</p>
                                </div>
                                <div>
                                    <span className="font-medium text-gray-700">Lieu :</span>
                                    <p className="text-gray-900">{selectedCandidature.internshipOffer.address}</p>
                                </div>
                                <div>
                                    <span className="font-medium text-gray-700">Rémunération :</span>
                                    <p className="text-gray-900">
                                        {selectedCandidature.internshipOffer.remuneration
                                            ? `${selectedCandidature.internshipOffer.remuneration} $/h`
                                            : "Non rémunéré"}
                                    </p>
                                </div>
                            </div>
                        </div>
                    </div>
                )}

                {/* Missions et objectifs */}
                <div>
                    <label className="block text-sm font-medium text-gray-700 mb-1">Missions et objectifs du stage *</label>
                    <textarea
                        name="missionsObjectifs"
                        value={formData.missionsObjectifs}
                        onChange={handleChange}
                        rows="8"
                        placeholder="Décrivez les missions et objectifs..."
                        className={`w-full px-3 py-2 border rounded-md shadow-sm focus:ring-indigo-500 focus:border-indigo-500 ${
                            errors.missionsObjectifs ? "border-red-500" : "border-gray-300"
                        }`}
                    />
                    {errors.missionsObjectifs && <p className="mt-1 text-sm text-red-600">{errors.missionsObjectifs}</p>}
                </div>

                <div className="flex items-center justify-end space-x-3 pt-4 border-t">
                    <button type="button" onClick={handleCancel} disabled={loading} className="px-4 py-2 text-sm font-medium text-gray-700 bg-white border border-gray-300 rounded-md hover:bg-gray-50 disabled:opacity-50">
                        Annuler
                    </button>
                    <button type="submit" disabled={loading || success} className="px-4 py-2 text-sm font-medium text-white bg-indigo-600 border border-transparent rounded-md hover:bg-indigo-700 disabled:opacity-50">
                        {loading ? "Création en cours..." : "Créer l'entente"}
                    </button>
                </div>
            </form>
        </div>
    );
}
