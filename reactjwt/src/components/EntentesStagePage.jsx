// src/components/EntentesStagePage.jsx
import React, { useEffect, useState } from "react";
import { useNavigate } from "react-router-dom";
import { getCandidaturesAcceptees, previewCv } from "../api/apiGestionnaire.jsx";

export default function EntentesStagePage() {
    const [candidatures, setCandidatures] = useState([]);
    const [loading, setLoading] = useState(true);
    const [error, setError] = useState(null);
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
            downloadBlobInNewTab(blob);
        } catch (err) {
            console.error("Erreur preview CV:", err);
            alert("Impossible d'ouvrir le CV : " + (err.message || ""));
        }
    };

    function downloadBlobInNewTab(blob) {
        const url = window.URL.createObjectURL(blob);
        window.open(url, "_blank");
        setTimeout(() => window.URL.revokeObjectURL(url), 60_000);
    }

    if (loading) return <div className="bg-white shadow rounded p-4">Chargement des candidatures acceptées...</div>;
    if (error) return <div className="bg-white shadow rounded p-4 text-red-600">Erreur : {error}</div>;

    return (
        <div className="bg-white shadow rounded p-4">
            <h2 className="text-lg font-medium mb-3">Candidatures acceptées</h2>
            {candidatures.length === 0 ? (
                <p className="text-sm text-gray-600">Aucune candidature acceptée trouvée.</p>
            ) : (
                <div className="space-y-3">
                    {candidatures.map((c) => (
                        <div key={c.id} className="border rounded p-3 flex justify-between items-center">
                            <div>
                                <div className="font-medium">{c.studentName ?? `${c.studentPrenom ?? ""} ${c.studentNom ?? ""}`}</div>
                                <div className="text-sm text-gray-600">Offre : {c.offerDescription ?? c.internshipOfferDescription ?? "—"}</div>
                                <div className="text-sm text-gray-500">Candidature ID: {c.id}</div>
                            </div>
                            <div className="flex items-center space-x-2">
                                {c.cvId && (
                                    <button onClick={() => handlePreviewCv(c.cvId)} className="px-3 py-1 text-sm bg-indigo-50 text-indigo-700 rounded hover:bg-indigo-100">
                                        Voir CV
                                    </button>
                                )}
                                <button onClick={() => navigate(`/dashboard/gestionnaire/ententes/create?candidatureId=${c.id}`)} className="px-3 py-1 text-sm bg-green-50 text-green-700 rounded hover:bg-green-100">
                                    Créer entente
                                </button>
                            </div>
                        </div>
                    ))}
                </div>
            )}
        </div>
    );
}