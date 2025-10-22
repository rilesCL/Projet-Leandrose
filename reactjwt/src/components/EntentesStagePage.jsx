// src/components/EntentesStagePage.jsx
import React, { useEffect, useState } from "react";
import { useNavigate } from "react-router-dom";
import { getCandidaturesAcceptees, previewCv } from "../api/apiGestionnaire.jsx";
import PdfViewer from "./PdfViewer";

export default function EntentesStagePage() {
    const [candidatures, setCandidatures] = useState([]);
    const [loading, setLoading] = useState(true);
    const [error, setError] = useState(null);
    const [cvToPreview, setCvToPreview] = useState(null); // Blob PDF pour PdfViewer
    const navigate = useNavigate();

    useEffect(() => {
        const token = sessionStorage.getItem("accessToken");
        if (!token) {
            navigate("/login");
            return;
        }

        const fetchData = async () => {
            try {
                setLoading(true);
                const data = await getCandidaturesAcceptees();
                setCandidatures(Array.isArray(data) ? data : []);
            } catch (err) {
                console.error("Erreur fetching candidatures:", err);
                setError(err.message || "Erreur serveur");
            } finally {
                setLoading(false);
            }
        };

        fetchData();
    }, [navigate]);

    const handlePreviewCv = async (cvId) => {
        try {
            const blob = await previewCv(cvId);
            setCvToPreview(blob); // on ouvre dans PdfViewer
        } catch (err) {
            console.error("Erreur preview CV:", err);
            alert("Impossible d'ouvrir le CV : " + (err.message || ""));
        }
    };

    if (loading)
        return (
            <div className="bg-white shadow rounded p-4 text-center">
                Chargement des candidatures acceptées...
            </div>
        );

    if (error)
        return (
            <div className="bg-white shadow rounded p-4 text-red-600 text-center">
                Erreur : {error}
            </div>
        );

    return (
        <div className="bg-white shadow rounded p-6">
            <h2 className="text-2xl font-semibold mb-6 text-gray-900">Candidatures acceptées</h2>

            {candidatures.length === 0 ? (
                <p className="text-sm text-gray-600">Aucune candidature acceptée trouvée.</p>
            ) : (
                <div className="space-y-6">
                    {candidatures.map((c) => {
                        const student = c.student || {};
                        const offer = c.internshipOffer || {};
                        const cv = c.cv || null;
                        const entente = c.entente || null;

                        return (
                            <div
                                key={c.id}
                                className="border rounded-lg p-4 flex flex-col md:flex-row justify-between items-start md:items-center gap-4 md:gap-0 shadow-sm hover:shadow-md transition-shadow"
                            >
                                <div className="flex-1 space-y-1">
                                    <div className="text-lg font-medium text-gray-900">
                                        {student.fullName || `${student.firstName ?? ""} ${student.lastName ?? ""}`}
                                    </div>
                                    <div className="text-sm text-gray-700">Offre : {offer.description ?? "—"}</div>
                                    <div className="text-sm text-gray-700">Entreprise : {offer.companyName ?? "—"}</div>
                                    <div className="text-sm text-gray-500">
                                        Date début : {offer.startDate ? new Date(offer.startDate).toLocaleDateString("fr-FR") : "—"}
                                    </div>
                                    <div className="text-sm text-gray-500">
                                        Durée : {offer.durationInWeeks ? `${offer.durationInWeeks} semaine(s)` : "—"}
                                    </div>
                                    <div className="text-sm text-gray-500">Adresse : {offer.address ?? "—"}</div>
                                    <div className="text-sm text-gray-500">
                                        Rémunération : {offer.remuneration ? `${offer.remuneration} $/h` : "—"}
                                    </div>
                                    <div className="text-sm text-gray-500">Statut candidature : {c.status}</div>
                                    {entente && (
                                        <div className="text-sm text-green-700 font-medium">
                                            Entente créée le : {new Date(entente.creationDate).toLocaleDateString("fr-FR")}
                                        </div>
                                    )}
                                </div>

                                <div className="flex flex-col md:flex-row items-start md:items-center space-y-2 md:space-y-0 md:space-x-2">
                                    {cv && (
                                        <button
                                            onClick={() => handlePreviewCv(cv.id)}
                                            className="px-3 py-1 text-sm bg-indigo-50 text-indigo-700 rounded hover:bg-indigo-100"
                                        >
                                            Voir CV
                                        </button>
                                    )}
                                    {!entente && (
                                        <button
                                            onClick={() =>
                                                navigate(`/dashboard/gestionnaire/ententes/create?candidatureId=${c.id}`)
                                            }
                                            className="px-3 py-1 text-sm bg-green-50 text-green-700 rounded hover:bg-green-100"
                                        >
                                            Créer entente
                                        </button>
                                    )}
                                    {entente && (
                                        <span className="px-3 py-1 text-sm bg-gray-100 text-gray-600 rounded">
                                            Entente déjà créée
                                        </span>
                                    )}
                                </div>
                            </div>
                        );
                    })}
                </div>
            )}

            {cvToPreview && (
                <PdfViewer
                    file={cvToPreview}
                    onClose={() => setCvToPreview(null)}
                />
            )}
        </div>
    );
}
