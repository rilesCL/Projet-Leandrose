import React, { useEffect, useState } from "react";
import { useTranslation } from "react-i18next";
import {
    getPendingOffers,
    approveOffer,
    rejectOffer,
    previewOfferPdf
} from "../api/apiGestionnaire.jsx";
import PdfViewer from "../components/PdfViewer.jsx";

export default function PendingOffersPage() {
    const { t } = useTranslation();
    const [pendingOffers, setPendingOffers] = useState([]);
    const [loading, setLoading] = useState(true);
    const [error, setError] = useState(null);
    const [selectedPdfUrl, setSelectedPdfUrl] = useState(null);


    const [rejectModal, setRejectModal] = useState({ open: false, id: null, comment: "", error: null });

    useEffect(() => {
        async function fetchOffers() {
            setLoading(true);
            setError(null);
            try {
                const offers = await getPendingOffers();
                setPendingOffers(Array.isArray(offers) ? offers : []);
            } catch (err) {
                console.error(err);
                setError("serverError");
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
                    <span className="w-2 h-2 bg-yellow-500 rounded-full mr-2"></span>
                    {t("pendingOffers.status.pendingapproval")}
                </span>
            );
        } else if (statusUpper === "APPROVED" || statusUpper === "PUBLISHED") {
            return (
                <span className="px-2 py-1 text-xs font-medium rounded-full bg-green-100 text-green-800 border border-green-200 inline-flex items-center">
                    <span className="w-2 h-2 bg-green-500 rounded-full mr-2"></span>
                    {t("pendingOffers.status.approved")}
                </span>
            );
        } else if (statusUpper === "REJECTED") {
            return (
                <span className="px-2 py-1 text-xs font-medium rounded-full bg-red-100 text-red-800 border border-red-200 inline-flex items-center">
                    <span className="w-2 h-2 bg-red-500 rounded-full mr-2"></span>
                    {t("pendingOffers.status.rejected")}
                </span>
            );
        } else {
            return (
                <span className="px-2 py-1 text-xs font-medium rounded-full bg-gray-100 text-gray-800 border border-gray-200 inline-flex items-center">
                    <span className="w-2 h-2 bg-gray-500 rounded-full mr-2"></span>
                    {t("pendingOffers.status.unknown")}
                </span>
            );
        }
    };

    const handleApprove = async (offerId) => {
        try {
            await approveOffer(offerId);
            setPendingOffers((prev) => prev.filter((o) => o.id !== offerId));
        } catch (err) {
            console.error(err);
        }
    };

    // Ouvrir la modale de rejet
    const openRejectModal = (offerId) => {
        setRejectModal({ open: true, id: offerId, comment: "", error: null });
    };

    // Fermer la modale
    const closeRejectModal = () => {
        setRejectModal({ open: false, id: null, comment: "", error: null });
    };

    // Confirmer le rejet via la modale
    const confirmReject = async () => {
        const { id, comment } = rejectModal;
        const trimmed = (comment || "").trim();
        if (!trimmed) {
            setRejectModal((prev) => ({ ...prev, error: "commentRequired" }));
            return;
        }
        try {
            await rejectOffer(id, trimmed);
            setPendingOffers((prev) => prev.filter((o) => o.id !== id));
            closeRejectModal();
        } catch (err) {
            console.error(err);
        }
    };

    const handleViewPdf = async (offerId) => {
        try {
            const blob = await previewOfferPdf(offerId);
            const url = URL.createObjectURL(blob);
            setSelectedPdfUrl(url);
        } catch (err) {
            console.error(err);
            alert(t("pendingOffers.errors.download"));
        }
    };

    if (loading) return <div className="p-8 text-center">{t("pendingOffers.loading")}</div>;
    if (error) return <div className="p-8 text-red-600 text-center">{t(`pendingOffers.errors.${error}`)}</div>;
    if (!pendingOffers.length) return <div className="p-8 text-center">{t("pendingOffers.noPending")}</div>;

    return (
        <div className="bg-white shadow rounded-lg overflow-hidden">
            <div className="px-6 py-4 border-b border-gray-200">
                <h3 className="text-lg font-medium text-gray-900">{t("pendingOffers.title")}</h3>
                <p className="text-sm text-gray-600">{t("pendingOffers.subtitle", { count: pendingOffers.length })}</p>
            </div>

            <div className="overflow-x-auto">
                <table className="min-w-full divide-y divide-gray-200">
                    <thead className="bg-gray-50">
                    <tr>
                        <th className="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">
                            {t("pendingOffers.table.title")}
                        </th>
                        <th className="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">
                            {t("pendingOffers.table.company")}
                        </th>
                        <th className="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">
                            {t("pendingOffers.table.status")}
                        </th>
                        {/* Colonne Comment supprimée */}
                        <th className="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">
                            {t("pendingOffers.table.actions")}
                        </th>
                    </tr>
                    </thead>
                    <tbody className="bg-white divide-y divide-gray-200">
                    {pendingOffers.map((offer) => (
                        <tr key={offer.id} className="hover:bg-gray-50">
                            <td className="px-6 py-4">
                                <div className="flex items-center space-x-3 min-w-[200px]">
                                    <div className="h-8 w-8 bg-blue-500 rounded flex items-center justify-center flex-shrink-0">
                                        <span className="text-white text-sm font-bold">📋</span>
                                    </div>
                                    <div className="text-sm font-medium text-gray-900 truncate">
                                        {offer.description || t("pendingOffers.unknownTitle")}
                                    </div>
                                </div>
                            </td>
                            <td className="px-6 py-4 text-sm text-gray-900 min-w-[150px]">
                                {offer.companyName || t("pendingOffers.unknownCompany")}
                            </td>
                            <td className="px-6 py-4">{getStatusLabel(offer.status)}</td>
                            <td className="px-6 py-4 flex flex-col md:flex-row items-start md:items-center space-y-2 md:space-y-0 md:space-x-2">
                                <button
                                    onClick={() => handleApprove(offer.id)}
                                    className="bg-green-500 text-white px-3 py-1 rounded w-full md:w-auto hover:bg-green-600"
                                >
                                    {t("pendingOffers.actions.approve")}
                                </button>
                                <button
                                    onClick={() => openRejectModal(offer.id)}
                                    className="bg-red-500 text-white px-3 py-1 rounded w-full md:w-auto hover:bg-red-600"
                                >
                                    {t("pendingOffers.actions.reject")}
                                </button>
                                <button
                                    onClick={() => handleViewPdf(offer.id)}
                                    className="inline-flex items-center px-3 py-1 border border-transparent text-sm leading-4 font-medium rounded-md text-blue-700 bg-blue-100 hover:bg-blue-200 w-full md:w-auto"
                                >
                                    👁{t("previewPdf.preview")}
                                </button>
                            </td>
                        </tr>
                    ))}
                    </tbody>
                </table>
            </div>

            {selectedPdfUrl && (
                <PdfViewer
                    file={selectedPdfUrl}
                    onClose={() => {
                        URL.revokeObjectURL(selectedPdfUrl);
                        setSelectedPdfUrl(null);
                    }}
                />
            )}


            {rejectModal.open && (
                <div className="fixed inset-0 z-50 flex items-center justify-center bg-black bg-opacity-50">
                    <div className="bg-white rounded-lg shadow-lg w-full max-w-md mx-4">
                        <div className="px-6 py-4 border-b">
                            <h4 className="text-base font-semibold text-gray-900">
                                {t("pendingOffers.actions.reject")}
                            </h4>
                        </div>
                        <div className="px-6 py-4 space-y-3">
                            <p className="text-sm text-gray-700">{t("pendingOffers.commentPlaceholder")}</p>
                            <textarea
                                value={rejectModal.comment}
                                onChange={(e) => setRejectModal((prev) => ({ ...prev, comment: e.target.value }))}
                                className={`w-full h-28 border rounded p-2 resize-none ${rejectModal.error ? "border-red-500" : ""}`}
                            />
                            {rejectModal.error && (
                                <p className="text-xs text-red-600">{t(`pendingOffers.errors.${rejectModal.error}`, { defaultValue: "Commentaire requis" })}</p>
                            )}
                        </div>
                        <div className="px-6 py-4 border-t flex justify-end space-x-2">
                            <button
                                onClick={closeRejectModal}
                                className="px-3 py-1 rounded border text-gray-700 hover:bg-gray-50"
                            >
                                {t("common.cancel")}
                            </button>
                            <button
                                onClick={confirmReject}
                                className="px-3 py-1 rounded bg-red-600 text-white hover:bg-red-700"
                            >
                                {t("pendingOffers.actions.reject")}
                            </button>
                        </div>
                    </div>
                </div>
            )}
        </div>
    );
}
