import React, { useEffect, useState } from "react";
import { getPendingOffers, approveOffer, rejectOffer, downloadOfferPdf } from "../api/apiGestionnaire.jsx";

export default function PendingOffersPage() {
    const [pendingOffers, setPendingOffers] = useState([]);
    const [comments, setComments] = useState({});
    const [loading, setLoading] = useState(true);
    const [error, setError] = useState(null);

    useEffect(() => {
        async function fetchOffers() {
            setLoading(true);
            setError(null);
            try {
                const offers = await getPendingOffers();
                setPendingOffers(Array.isArray(offers) ? offers : []);
            } catch (err) {
                console.error(err);
                setError("Erreur lors du chargement des offres");
            } finally {
                setLoading(false);
            }
        }

        fetchOffers();
    }, []);

    const getStatusLabel = (status) => {
        const statusUpper = (status || "").toUpperCase();
        if (statusUpper === "PENDING" || statusUpper === "PENDING_VALIDATION") {
            return (
                <span className="px-2 py-1 text-xs font-medium rounded-full bg-yellow-100 text-yellow-800 border border-yellow-200 inline-flex items-center">
                    <span className="w-2 h-2 bg-yellow-500 rounded-full mr-2"></span>En attente
                </span>
            );
        } else if (statusUpper === "APPROVED" || statusUpper === "PUBLISHED") {
            return (
                <span className="px-2 py-1 text-xs font-medium rounded-full bg-green-100 text-green-800 border border-green-200 inline-flex items-center">
                    <span className="w-2 h-2 bg-green-500 rounded-full mr-2"></span>Publiée
                </span>
            );
        } else if (statusUpper === "REJECTED") {
            return (
                <span className="px-2 py-1 text-xs font-medium rounded-full bg-red-100 text-red-800 border border-red-200 inline-flex items-center">
                    <span className="w-2 h-2 bg-red-500 rounded-full mr-2"></span>Rejetée
                </span>
            );
        } else {
            return (
                <span className="px-2 py-1 text-xs font-medium rounded-full bg-gray-100 text-gray-800 border border-gray-200 inline-flex items-center">
                    <span className="w-2 h-2 bg-gray-500 rounded-full mr-2"></span>Inconnu
                </span>
            );
        }
    };

    const formatDate = (dateString) => {
        if (!dateString) return "Non définie";
        try {
            return new Date(dateString).toLocaleDateString("fr-FR");
        } catch {
            return dateString;
        }
    };

    const handleDownload = async (offerId) => {
        try {
            await downloadOfferPdf(offerId);
        } catch (err) {
            console.error(err);
            alert("Erreur lors du téléchargement de l'offre");
        }
    };

    const handleApprove = async (offerId) => {
        try {
            await approveOffer(offerId);
            setPendingOffers(pendingOffers.filter(o => o.id !== offerId));
        } catch (err) {
            console.error(err);
        }
    };

    const handleReject = async (offerId) => {
        const comment = comments[offerId] || "";
        if (!comment.trim()) {
            setError("Il faut écrire un commentaire avant de rejeter l'offre");
            return;
        }
        try {
            await rejectOffer(offerId, comment);
            setPendingOffers(pendingOffers.filter(o => o.id !== offerId));
        } catch (err) {
            console.error(err);
        }
    };

    const handleCommentChange = (offerId, value) => {
        setComments(prev => ({ ...prev, [offerId]: value }));
    };

    if (loading) return <div className="p-8 text-center">Chargement des offres en attente...</div>;
    if (error) return <div className="p-8 text-red-600 text-center">{error}</div>;
    if (!pendingOffers.length) return <div className="p-8 text-center">Aucune offre en attente</div>;

    return (
        <div className="bg-white shadow rounded-lg overflow-hidden">
            <div className="px-6 py-4 border-b border-gray-200">
                <h3 className="text-lg font-medium text-gray-900">Offres en attente</h3>
                <p className="text-sm text-gray-600">Gérez les offres en attente ({pendingOffers.length})</p>
            </div>

            <div className="overflow-x-auto">
                <table className="min-w-full divide-y divide-gray-200">
                    <thead className="bg-gray-50">
                    <tr>
                        <th className="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">Titre</th>
                        <th className="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">Entreprise</th>
                        <th className="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">Statut</th>
                        <th className="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">Commentaire</th>
                        <th className="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">Actions</th>
                    </tr>
                    </thead>
                    <tbody className="bg-white divide-y divide-gray-200">
                    {pendingOffers.map(offer => (
                        <tr key={offer.id} className="hover:bg-gray-50">
                            <td className="px-6 py-4">
                                <div className="flex items-center space-x-3 min-w-[200px]">
                                    <div className="h-8 w-8 bg-blue-500 rounded flex items-center justify-center flex-shrink-0">
                                        <span className="text-white text-sm font-bold">📋</span>
                                    </div>
                                    <div className="text-sm font-medium text-gray-900 truncate">{offer.description || "Titre non disponible"}</div>
                                </div>
                            </td>
                            <td className="px-6 py-4 text-sm text-gray-900 min-w-[150px]">{offer.companyName || "Entreprise inconnue"}</td>
                            <td className="px-6 py-4">{getStatusLabel(offer.status)}</td>
                            <td className="px-6 py-4">
                                    <textarea
                                        value={comments[offer.id] || ""}
                                        onChange={e => handleCommentChange(offer.id, e.target.value)}
                                        className="w-full h-20 border rounded p-2 resize-none"
                                        placeholder="Commentaire (requis pour rejet)"
                                    />
                            </td>
                            <td className="px-6 py-4 flex flex-col md:flex-row items-start md:items-center space-y-2 md:space-y-0 md:space-x-2">
                                <button onClick={() => handleApprove(offer.id)} className="bg-green-500 text-white px-3 py-1 rounded w-full md:w-auto">Approuver</button>
                                <button onClick={() => handleReject(offer.id)} className="bg-red-500 text-white px-3 py-1 rounded w-full md:w-auto">Rejeter</button>
                                <button onClick={() => handleDownload(offer.id)} className="inline-flex items-center px-3 py-1 border border-transparent text-sm leading-4 font-medium rounded-md text-blue-700 bg-blue-100 hover:bg-blue-200 w-full md:w-auto">
                                    ⬇ PDF
                                </button>
                            </td>
                        </tr>
                    ))}
                    </tbody>
                </table>
            </div>
        </div>
    );
}
