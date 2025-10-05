import React, { useEffect, useState } from "react";
import { useParams, useNavigate } from "react-router-dom";
import { getOfferDetails, downloadOfferPdf, applyToOffer } from "../api/apiStudent";
import { getStudentCv, getMyCandidatures } from "../api/apiStudent";

export default function OfferDetailPage() {
    const { offerId } = useParams();
    const navigate = useNavigate();

    const [offer, setOffer] = useState(null);
    const [cv, setCv] = useState(null);
    const [loading, setLoading] = useState(true);
    const [error, setError] = useState(null);
    const [applying, setApplying] = useState(false);
    const [applyError, setApplyError] = useState(null);
    const [applySuccess, setApplySuccess] = useState(false);
    const [alreadyApplied, setAlreadyApplied] = useState(false);

    useEffect(() => {
        async function fetchData() {
            setLoading(true);
            setError(null);

            try {
                const offerData = await getOfferDetails(offerId);
                setOffer(offerData);

                const cvData = await getStudentCv();
                setCv(cvData);

                try {
                    const candidatures = await getMyCandidatures();
                    if (Array.isArray(candidatures)) {
                        setAlreadyApplied(candidatures.some(c => String(c.offerId) === String(offerId)));
                    }
                } catch (e) {
                }
            } catch (err) {
                console.error(err);
                setError("Impossible de charger les détails de l'offre");
            } finally {
                setLoading(false);
            }
        }

        fetchData();
    }, [offerId]);

    const formatDate = (dateString) => {
        if (!dateString) return "Non définie";
        return new Date(dateString).toLocaleDateString('fr-FR');
    };

    const handleDownloadPdf = async () => {
        try {
            await downloadOfferPdf(offerId);
        } catch (err) {
            alert("Erreur lors du téléchargement du PDF");
        }
    };

    const handleApply = async () => {
        if (alreadyApplied) return;

        if (!cv) {
            setApplyError("Vous devez avoir un CV approuvé pour postuler");
            return;
        }

        if (cv.status !== "APPROVED") {
            setApplyError("Votre CV doit être approuvé par le gestionnaire avant de postuler");
            return;
        }

        setApplying(true);
        setApplyError(null);

        try {
            await applyToOffer(offerId, cv.id);
            setApplySuccess(true);
            setAlreadyApplied(true);
        } catch (err) {
            console.error(err);
            setApplyError(err.message || "Erreur lors de la candidature. Vous avez peut-être déjà postulé.");
        } finally {
            setApplying(false);
        }
    };

    if (loading) {
        return (
            <div className="bg-white shadow rounded-lg p-8 text-center">
                <div className="animate-pulse">Chargement...</div>
            </div>
        );
    }

    if (error) {
        return (
            <div className="bg-white shadow rounded-lg p-8 text-center">
                <p className="text-red-600">{error}</p>
                <button
                    onClick={() => navigate('/dashboard/student')}
                    className="mt-4 px-4 py-2 bg-indigo-600 text-white rounded"
                >
                    Retour aux offres
                </button>
            </div>
        );
    }

    if (!offer) {
        return (
            <div className="bg-white shadow rounded-lg p-8 text-center">
                <p>Offre introuvable</p>
            </div>
        );
    }

    return (
        <div className="max-w-4xl mx-auto">

            <div className="bg-white shadow rounded-lg mb-6">
                <div className="px-6 py-4 border-b border-gray-200">
                    <button
                        onClick={() => navigate('/dashboard/student')}
                        className="text-indigo-600 hover:text-indigo-900 mb-2"
                    >
                        ← Retour aux offres
                    </button>
                    <h1 className="text-2xl font-bold text-gray-900">{offer.description}</h1>
                    <p className="text-gray-600 mt-1">{offer.companyName}</p>
                </div>


                <div className="px-6 py-4 grid grid-cols-1 md:grid-cols-2 gap-4">
                    <div>
                        <h3 className="text-sm font-medium text-gray-500">Lieu</h3>
                        <p className="mt-1 text-gray-900">{offer.address}</p>
                    </div>

                    <div>
                        <h3 className="text-sm font-medium text-gray-500">Date de début</h3>
                        <p className="mt-1 text-gray-900">{formatDate(offer.startDate)}</p>
                    </div>

                    <div>
                        <h3 className="text-sm font-medium text-gray-500">Durée</h3>
                        <p className="mt-1 text-gray-900">{offer.durationInWeeks} semaines</p>
                    </div>

                    {offer.remuneration && (
                        <div>
                            <h3 className="text-sm font-medium text-gray-500">Rémunération</h3>
                            <p className="mt-1 text-gray-900">{offer.remuneration} $ / semaine</p>
                        </div>
                    )}
                </div>

                <div className="px-6 py-4 bg-gray-50">
                    <h3 className="text-sm font-medium text-gray-500 mb-2">Contact de l'employeur</h3>
                    <p className="text-gray-900">
                        {offer.employeurFirstName} {offer.employeurLastName}
                    </p>
                    <p className="text-gray-600">{offer.employeurEmail}</p>
                </div>


                <div className="px-6 py-4 border-t border-gray-200">
                    <button
                        onClick={handleDownloadPdf}
                        className="inline-flex items-center px-4 py-2 border border-gray-300 rounded-md shadow-sm text-sm font-medium text-gray-700 bg-white hover:bg-gray-50"
                    >
                        <span className="mr-2">📄</span>
                        Télécharger le document PDF
                    </button>
                </div>
            </div>

            <div className="bg-white shadow rounded-lg p-6">
                <h2 className="text-lg font-medium text-gray-900 mb-4">Postuler à cette offre</h2>

                {applySuccess ? (
                    <div className="bg-green-50 border border-green-200 rounded-md p-4">
                        <p className="text-green-800">
                            Candidature envoyée avec succès ! L'employeur examinera votre dossier.
                        </p>
                    </div>
                ) : (
                    <>
                        {!cv && (
                            <div className="bg-yellow-50 border border-yellow-200 rounded-md p-4 mb-4">
                                <p className="text-yellow-800">
                                    Vous devez téléverser un CV approuvé avant de postuler.
                                </p>
                            </div>
                        )}

                        {cv && cv.status !== "APPROVED" && (
                            <div className="bg-yellow-50 border border-yellow-200 rounded-md p-4 mb-4">
                                <p className="text-yellow-800">
                                    Votre CV est en attente d'approbation. Vous pourrez postuler une fois qu'il sera approuvé.
                                </p>
                            </div>
                        )}

                        {applyError && (
                            <div className="bg-red-50 border border-red-200 rounded-md p-4 mb-4">
                                <p className="text-red-800">{applyError}</p>
                            </div>
                        )}

                        <button
                            onClick={handleApply}
                            disabled={applying || !cv || cv.status !== "APPROVED" || alreadyApplied || applySuccess}
                            className={`w-full px-4 py-2 rounded-md font-medium ${
                                applying || !cv || cv.status !== "APPROVED" || alreadyApplied || applySuccess
                                    ? 'bg-gray-300 text-gray-500 cursor-not-allowed'
                                    : 'bg-indigo-600 text-white hover:bg-indigo-700'
                            }`}
                        >
                            {applySuccess ? 'Candidature envoyée' : alreadyApplied ? 'Déjà postulée' : applying ? 'Envoi en cours...' : 'Postuler à cette offre'}
                        </button>
                    </>
                )}
            </div>
        </div>
    );
}