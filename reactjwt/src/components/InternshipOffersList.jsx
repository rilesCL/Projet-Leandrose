import React, { useEffect, useState } from "react";
import { useTranslation } from "react-i18next";
import { useNavigate } from 'react-router-dom';
import { getOfferCandidatures } from '../api/apiEmployeur';

const API_BASE = 'http://localhost:8080';

export default function InternshipOffersList() {
    const { t } = useTranslation();
    const navigate = useNavigate();
    const [offers, setOffers] = useState([]);
    const [loading, setLoading] = useState(true);
    const [error, setError] = useState(null);
    const [candidatureData, setCandidatureData] = useState({});
    const [loadingCounts, setLoadingCounts] = useState(false);
    const [showCommentModal, setShowCommentModal] = useState(false);
    const [currentComment, setCurrentComment] = useState("");

    useEffect(() => {
        async function fetchOffers() {
            setLoading(true);
            setError(null);

            try {
                const token = sessionStorage.getItem("accessToken");
                const response = await fetch(`${API_BASE}/employeur/offers`, {
                    method: 'GET',
                    headers: {
                        'Authorization': `Bearer ${token}`,
                        'Content-Type': 'application/json'
                    }
                });

                if (response.status === 401) {
                    setError(t("internshipOffersList.errors.unauthorized"));
                } else if (response.status === 403) {
                    setError(t("internshipOffersList.errors.forbidden"));
                } else if (!response.ok) {
                    const errorText = await response.text();
                    setError(errorText || `${t("internshipOffersList.errorTitle")} ${response.status}: ${response.statusText}`);
                } else {
                    const data = await response.json();
                    setOffers(Array.isArray(data) ? data : []);
                }
            } catch (err) {
                setError(t("internshipOffersList.errors.serverError"));
            } finally {
                setLoading(false);
            }
        }

        fetchOffers();
    }, [t]);

    useEffect(() => {
        if (!offers || offers.length === 0) return;
        let cancelled = false;
        async function loadCounts() {
            setLoadingCounts(true);
            const entries = await Promise.all(offers.map(async (o) => {
                try {
                    const list = await getOfferCandidatures(o.id);
                    const preview = list.slice(0, 3).map(c => [c.studentFirstName, c.studentLastName].filter(Boolean).join(' ') || c.studentName || '?');
                    return [o.id, { count: list.length, preview }];
                } catch {
                    return [o.id, { count: 0, preview: [] }];
                }
            }));
            if (!cancelled) {
                setCandidatureData(Object.fromEntries(entries));
                setLoadingCounts(false);
            }
        }
        loadCounts();
        return () => { cancelled = true; };
    }, [offers]);

    const getStatusLabel = (status) => {
        const statusUpper = (status || "").toString().toUpperCase();

        if (statusUpper === "PENDING" || statusUpper === "PENDING_VALIDATION") {
            return (
                <span className="px-3 py-1 text-xs font-medium rounded-full bg-yellow-100 text-yellow-800 border border-yellow-200">
                    <span className="w-2 h-2 bg-yellow-500 rounded-full inline-block mr-2"></span>
                    {t("internshipOffersList.status.pendingapproval")}
                </span>
            );
        } else if (statusUpper === "APPROVED" || statusUpper === "PUBLISHED") {
            return (
                <span className="px-3 py-1 text-xs font-medium rounded-full bg-green-100 text-green-800 border border-green-200">
                    <span className="w-2 h-2 bg-green-500 rounded-full inline-block mr-2"></span>
                    {t("internshipOffersList.status.published")}
                </span>
            );
        } else if (statusUpper === "REJECTED") {
            return (
                <span className="px-3 py-1 text-xs font-medium rounded-full bg-red-100 text-red-800 border border-red-200">
                    <span className="w-2 h-2 bg-red-500 rounded-full inline-block mr-2"></span>
                    {t("internshipOffersList.status.rejected")}
                </span>
            );
        } else {
            return (
                <span className="px-3 py-1 text-xs font-medium rounded-full bg-gray-100 text-gray-800 border border-gray-200">
                    <span className="w-2 h-2 bg-gray-500 rounded-full inline-block mr-2"></span>
                    {statusUpper || t("internshipOffersList.status.unknown")}
                </span>
            );
        }
    };

    const formatDate = (dateString) => {
        if (!dateString) return t("internshipOffersList.notDefined");
        try {
            const date = new Date(dateString);
            return date.toLocaleDateString(t === 'fr' ? 'fr-FR' : 'en-US');
        } catch {
            return dateString;
        }
    };

    const handleDownloadOffer = async (offerId, description) => {
        try {
            const token = sessionStorage.getItem("accessToken");
            const response = await fetch(`${API_BASE}/employeur/offers/${offerId}/download`, {
                method: 'GET',
                headers: {
                    'Authorization': `Bearer ${token}`
                }
            });

            if (response.ok) {
                const blob = await response.blob();
                const url = window.URL.createObjectURL(blob);
                const a = document.createElement('a');
                a.href = url;
                a.download = `Offre_${description.substring(0, 30).replace(/[^a-zA-Z0-9]/g, '_')}.pdf`;
                document.body.appendChild(a);
                a.click();
                window.URL.revokeObjectURL(url);
                document.body.removeChild(a);
            } else {
                alert(t("internshipOffersList.downloadError"));
            }
        } catch (error) {
            console.error('Download error:', error);
            alert(t("internshipOffersList.downloadError"));
        }
    };

    const handleCreateOffer = () => {
        navigate('/dashboard/employeur/createOffer');
    };

    if (loading) {
        return (
            <div className="bg-white shadow rounded-lg p-8 text-center">
                <div className="animate-pulse">
                    <div className="h-4 bg-gray-200 rounded w-1/4 mx-auto mb-4"></div>
                    <div className="h-4 bg-gray-200 rounded w-1/2 mx-auto"></div>
                </div>
                <p className="text-gray-600 mt-4">{t("internshipOffersList.loading")}</p>
            </div>
        );
    }

    if (error) {
        return (
            <div className="bg-white shadow rounded-lg p-8 text-center">
                <div className="text-red-600 mb-4">
                    <div className="mx-auto h-12 w-12 rounded-full bg-red-100 flex items-center justify-center">
                        <span className="text-red-600 text-2xl font-bold">!</span>
                    </div>
                </div>
                <h3 className="text-lg font-medium text-gray-900 mb-2">{t("internshipOffersList.errorTitle")}</h3>
                <p className="text-red-600">{error}</p>
                <button
                    onClick={() => window.location.reload()}
                    className="mt-4 px-4 py-2 bg-red-600 text-white rounded hover:bg-red-700"
                >
                    {t("internshipOffersList.retry")}
                </button>
            </div>
        );
    }

    if (!offers || offers.length === 0) {
        return (
            <div className="bg-white shadow rounded-lg p-8 text-center">
                <div className="text-gray-400 mb-4">
                    <div className="mx-auto h-16 w-16 rounded bg-gray-100 flex items-center justify-center">
                        <span className="text-gray-500 text-3xl">📋</span>
                    </div>
                </div>
                <h3 className="text-lg font-medium text-gray-900 mb-2">{t("internshipOffersList.noOffersTitle")}</h3>
                <p className="text-gray-600 mb-4">{t("internshipOffersList.noOffersDescription")}</p>
                <button
                    onClick={handleCreateOffer}
                    className="inline-flex items-center px-4 py-2 bg-indigo-600 text-white rounded-md hover:bg-indigo-700 transition-colors"
                >
                    <span className="mr-2">➕</span>
                    {t("internshipOffersList.createOffer")}
                </button>
            </div>
        );
    }

    return (
        <div className="bg-white shadow rounded-lg overflow-hidden">
            <div className="px-6 py-4 border-b border-gray-200">
                <h3 className="text-lg font-medium text-gray-900">{t("internshipOffersList.title")}</h3>
                <p className="text-sm text-gray-600">
                    {t("internshipOffersList.subtitle")} ({offers.length} {offers.length > 1 ? t("internshipOffersList.offers") : t("internshipOffersList.offer")})
                </p>
            </div>

            <div className="overflow-x-auto">
                <table className="min-w-full divide-y divide-gray-200">
                    <thead className="bg-gray-50">
                    <tr>
                        <th className="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">
                            {t("internshipOffersList.table.description")}
                        </th>
                        <th className="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">
                            {t("internshipOffersList.table.startDate")}
                        </th>
                        <th className="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">
                            {t("internshipOffersList.table.duration")}
                        </th>
                        <th className="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">
                            {t("internshipOffersList.table.status")}
                        </th>
                        <th className="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">
                            {t("employerCandidatures.table.candidatures", 'Candidatures')}
                        </th>
                        <th className="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">
                            {t("internshipOffersList.table.actions")}
                        </th>
                    </tr>
                    </thead>
                    <tbody className="bg-white divide-y divide-gray-200">
                    {offers.map((offer) => {
                        const cData = candidatureData[offer.id];
                        const count = cData ? cData.count : (loadingCounts ? '…' : 0);
                        const preview = cData && cData.preview && cData.preview.length ? cData.preview.join(', ') : '';
                        const statusUpper = (offer.status || '').toUpperCase();
                        const isClickable = statusUpper === 'APPROVED' || statusUpper === 'PUBLISHED';
                        return (
                            <tr key={offer.id}
                                className={`hover:bg-gray-50 ${isClickable ? 'cursor-pointer' : 'cursor-not-allowed opacity-50'}`}
                                {...(isClickable ? {onClick: (e) => { if (!(e.target.closest('button'))) navigate(`/dashboard/employeur/offers/${offer.id}/candidatures`); }} : {})}
                            >
                                <td className="px-6 py-4">
                                    <div className="flex items-center">
                                        <div className="flex-shrink-0">
                                            <div className="h-8 w-8 bg-blue-500 rounded flex items-center justify-center">
                                                <span className="text-white text-sm font-bold">📋</span>
                                            </div>
                                        </div>
                                        <div className="ml-3">
                                            <div className="text-sm font-medium text-gray-900 truncate max-w-xs">
                                                {offer.description || t("internshipOffersList.notDefined")}
                                            </div>
                                            <div className="text-xs text-gray-500">
                                                {offer.address || t("internshipOffersList.notSpecified")}
                                            </div>
                                        </div>
                                    </div>
                                </td>
                                <td className="px-6 py-4 whitespace-nowrap text-sm text-gray-900">
                                    {formatDate(offer.startDate)}
                                </td>
                                <td className="px-6 py-4 whitespace-nowrap text-sm text-gray-900">
                                    {offer.durationInWeeks || 0} {(offer.durationInWeeks || 0) > 1 ? t("internshipOffersList.weeks") : t("internshipOffersList.week")}
                                </td>
                                <td className="px-6 py-4 whitespace-nowrap">
                                    {getStatusLabel(offer.status)}
                                </td>
                                <td className="px-6 py-4 whitespace-nowrap text-sm">
                                    <span title={preview} className="inline-flex items-center px-2 py-1 rounded bg-indigo-50 text-indigo-700 text-xs font-medium border border-indigo-100">
                                        {loadingCounts && !cData ? t('internshipOffersList.loading') : count}
                                    </span>
                                </td>
                                <td className="px-6 py-4 whitespace-nowrap text-sm font-medium space-x-2" onClick={(e)=> e.stopPropagation()}>
                                    {statusUpper === "REJECTED" && (
                                        <button
                                            onClick={() => { setCurrentComment(offer.rejectionComment || t('internshipOffersList.noRejectionComment')); setShowCommentModal(true); }}
                                            className="inline-flex items-center px-3 py-1 border border-transparent text-sm leading-4 font-medium rounded-md text-red-700 bg-red-100 hover:bg-red-200"
                                            title={t('internshipOffersList.viewRejectionComment')}
                                        >
                                            <span className="mr-1">💬</span>
                                        </button>
                                    )}
                                    <button
                                        onClick={() => handleDownloadOffer(offer.id, offer.description)}
                                        className="inline-flex items-center px-3 py-1 border border-transparent text-sm leading-4 font-medium rounded-md text-blue-700 bg-blue-100 hover:bg-blue-200 focus:outline-none focus:ring-2 focus:ring-offset-2 focus:ring-blue-500"
                                    >
                                        <span className="mr-1">⬇</span>
                                        PDF
                                    </button>
                                </td>
                            </tr>
                        );
                    })}
                    </tbody>
                </table>
            </div>

            <div className="px-6 py-3 bg-gray-50 text-right">
                <button
                    onClick={handleCreateOffer}
                    className="inline-flex items-center px-4 py-2 text-sm font-medium text-indigo-600 hover:text-indigo-500"
                >
                    {t("internshipOffersList.createNewOffer")}
                    <span className="ml-1">➕</span>
                </button>
            </div>

            {showCommentModal && (
                <div className="fixed inset-0 z-50 flex items-center justify-center bg-black bg-opacity-40">
                    <div className="bg-white rounded-lg shadow-lg p-6 max-w-md w-full">
                        <h2 className="text-lg font-semibold mb-4 text-red-700">{t('internshipOffersList.rejectionCommentTitle', 'Commentaire de rejet')}</h2>
                        <p className="text-gray-800 whitespace-pre-line">{currentComment}</p>
                        <div className="mt-6 text-right">
                            <button
                                onClick={() => setShowCommentModal(false)}
                                className="px-4 py-2 bg-red-600 text-white rounded hover:bg-red-700"
                            >
                                {t('internshipOffersList.close', 'Fermer')}
                            </button>
                        </div>
                    </div>
                </div>
            )}
        </div>
    );
}