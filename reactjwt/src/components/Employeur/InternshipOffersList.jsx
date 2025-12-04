import React, {useEffect, useState} from "react";
import {useTranslation} from "react-i18next";
import {useNavigate} from 'react-router-dom';
import {
    disableOffer,
    enableOffer,
    getEmployeurOffers,
    getOfferCandidatures,
    previewOfferPdf
} from '../../api/apiEmployeur.jsx';
import PdfViewer from '../PdfViewer.jsx';

export default function InternshipOffersList({selectedTerm}) {
    const {t} = useTranslation();
    const navigate = useNavigate();
    const [offers, setOffers] = useState([]);
    const [filteredOffers, setFilteredOffers] = useState([]);
    const [loading, setLoading] = useState(true);
    const [error, setError] = useState(null);
    const [candidatureData, setCandidatureData] = useState({});
    const [loadingCounts, setLoadingCounts] = useState(false);
    const [showCommentModal, setShowCommentModal] = useState(false);
    const [currentComment, setCurrentComment] = useState("");
    const [showPdfModal, setShowPdfModal] = useState(false);
    const [pdfUrl, setPdfUrl] = useState(null);
    const [showConfirmModal, setShowConfirmModal] = useState(false);
    const [confirmMessage, setConfirmMessage] = useState("");
    const [onConfirm, setOnConfirm] = useState(null);
    const [infoMessage, setInfoMessage] = useState(null);

    useEffect(() => {
        if (!selectedTerm || !offers.length) {
            setFilteredOffers(offers);
            return;
        }

        const filtered = offers.filter(offer => {
            if (!offer.schoolTerm) return false;

            const termParts = offer.schoolTerm.trim().split(/\s+/);
            const offerSeason = termParts[0]?.toUpperCase();
            const offerYear = parseInt(termParts[1]);

            return offerSeason === selectedTerm.season && offerYear === selectedTerm.year;
        });

        setFilteredOffers(filtered);
    }, [selectedTerm, offers]);

    useEffect(() => {
        async function fetchOffers() {
            setLoading(true);
            setError(null);

            try {
                const token = sessionStorage.getItem("accessToken");
                const data = await getEmployeurOffers(token);
                setOffers(Array.isArray(data) ? data : []);
            } catch (err) {
                setError(t("internshipOffersList.errors.serverError"));
            } finally {
                setLoading(false);
            }
        }

        fetchOffers();
    }, [t]);

    useEffect(() => {
        if (!filteredOffers || filteredOffers.length === 0) return;
        let cancelled = false;

        async function loadCounts() {
            setLoadingCounts(true);
            const entries = await Promise.all(filteredOffers.map(async (o) => {
                try {
                    const list = await getOfferCandidatures(o.id);
                    const preview = list.slice(0, 3).map(c => [c.studentFirstName, c.studentLastName].filter(Boolean).join(' ') || c.studentName || '?');
                    return [o.id, {count: list.length, preview}];
                } catch {
                    return [o.id, {count: 0, preview: []}];
                }
            }));
            if (!cancelled) {
                setCandidatureData(Object.fromEntries(entries));
                setLoadingCounts(false);
            }
        }

        loadCounts();
        return () => {
            cancelled = true;
        };
    }, [filteredOffers]);

    const getStatusLabel = (status) => {
        const statusUpper = (status || "").toString().toUpperCase();

        if (statusUpper === "PENDING" || statusUpper === "PENDING_VALIDATION") {
            return (
                <span
                    className="px-2 py-1 text-xs font-medium rounded-full bg-yellow-100 text-yellow-800 border border-yellow-200 inline-flex items-center">
                    <span className="w-2 h-2 bg-yellow-500 rounded-full mr-1"></span>
                    <span className="hidden sm:inline">{t("internshipOffersList.status.pendingapproval")}</span>
                    <span className="sm:hidden">⏳</span>
                </span>
            );
        } else if (statusUpper === "APPROVED" || statusUpper === "PUBLISHED") {
            return (
                <span
                    className="px-2 py-1 text-xs font-medium rounded-full bg-green-100 text-green-800 border border-green-200 inline-flex items-center">
                    <span className="w-2 h-2 bg-green-500 rounded-full mr-1"></span>
                    <span className="hidden sm:inline">{t("internshipOffersList.status.published")}</span>
                    <span className="sm:hidden">✓</span>
                </span>
            );
        } else if (statusUpper === "REJECTED") {
            return (
                <span
                    className="px-2 py-1 text-xs font-medium rounded-full bg-red-100 text-red-800 border border-red-200 inline-flex items-center">
                    <span className="w-2 h-2 bg-red-500 rounded-full mr-1"></span>
                    <span className="hidden sm:inline">{t("internshipOffersList.status.rejected")}</span>
                    <span className="sm:hidden">✗</span>
                </span>
            );
        } else {
            return (
                <span
                    className="px-2 py-1 text-xs font-medium rounded-full bg-gray-100 text-gray-800 border border-gray-200 inline-flex items-center">
                    <span className="w-2 h-2 bg-gray-500 rounded-full mr-1"></span>
                    <span className="hidden sm:inline">{statusUpper || t("internshipOffersList.status.unknown")}</span>
                    <span className="sm:hidden">?</span>
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

    const handlePreviewOffer = async (offerId) => {
        try {
            const token = sessionStorage.getItem("accessToken");
            const blob = await previewOfferPdf(offerId, token);
            const url = window.URL.createObjectURL(blob);
            setPdfUrl(url);
            setShowPdfModal(true);
        } catch (error) {
            console.error('Preview error:', error);
            alert(t("internshipOffersList.previewError"));
        }
    };

    const handleCreateOffer = () => {
        navigate('/dashboard/employeur/createOffer');
    };

    const confirmAction = (message, callback) => {
        setConfirmMessage(message);
        setOnConfirm(() => callback);
        setShowConfirmModal(true);
    };

    const handleDisableOffer = async (offerId) => {
        confirmAction(
            t("internshipOffersList.confirmDisable"),
            async () => {
                try {
                    const token = sessionStorage.getItem("accessToken");
                    await disableOffer(offerId, token);
                    setOffers(prev =>
                        prev.map(o =>
                            o.id === offerId ? {...o, status: "DISABLED"} : o
                        )
                    );
                } catch (err) {
                    setInfoMessage(err.response?.data || t("internshipOffersList.errors.serverError"));
                }
            }
        );
    }

    const handleEnableOffer = async (offer) => {
        const startDate = new Date(offer.startDate)
        const today = new Date()
        today.setHours(0, 0, 0, 0)

        if (startDate < today) {
            setInfoMessage(
                t("internshipOffersList.enableNotAllowed")
            );
            return;
        }

        confirmAction(
            t("internshipOffersList.confirmEnable"),
            async () => {
                try {
                    const token = sessionStorage.getItem("accessToken");
                    await enableOffer(offer.id, token);
                    setOffers(prev =>
                        prev.map(o =>
                            o.id === offer.id ? {...o, status: "PUBLISHED"} : o
                        )
                    );
                } catch (err) {
                    setInfoMessage(err.response?.data || t("internshipOffersList.errors.serverError"));
                }
            }
        );
    }

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

    if (filteredOffers.length === 0 && selectedTerm) {
        return (
            <div className="bg-white shadow rounded-lg p-8 text-center">
                <div className="text-gray-400 mb-4">
                    <div className="mx-auto h-16 w-16 rounded bg-gray-100 flex items-center justify-center">
                        <span className="text-gray-500 text-3xl">🔍</span>
                    </div>
                </div>
                <h3 className="text-lg font-medium text-gray-900 mb-2">{t("internshipOffersList.noOffersForTerm")}</h3>
                <p className="text-gray-600 mb-4">
                    {t("internshipOffersList.noOffersForTermDescription", {
                        term: `${t(`terms.${selectedTerm.season}`)} ${selectedTerm.year}`
                    })}
                </p>
            </div>
        );
    }

    return (
        <>
            <div className="bg-white shadow rounded-lg overflow-hidden">
                <div className="px-4 sm:px-6 py-4 border-b border-gray-200">
                    <h3 className="text-lg font-medium text-gray-900">{t("internshipOffersList.title")}</h3>
                    <p className="text-sm text-gray-600">
                        {t("internshipOffersList.subtitle")} ({filteredOffers.length} {filteredOffers.length > 1 ? t("internshipOffersList.offers") : t("internshipOffersList.offer")})
                    </p>
                </div>

                <div className="hidden lg:block overflow-x-auto">
                    <table className="min-w-full divide-y divide-gray-200">
                        <thead className="bg-gray-50">
                        <tr>
                            <th className="px-4 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">
                                {t("internshipOffersList.table.description")}
                            </th>
                            <th className="px-4 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">
                                {t("internshipOffersList.table.startDate")}
                            </th>
                            <th className="px-4 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">
                                {t("internshipOffersList.table.status")}
                            </th>
                            <th className="px-4 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">
                                {t("employerCandidatures.table.candidatures", 'Candidatures')}
                            </th>
                            <th className="px-4 py-3 text-center text-xs font-medium text-gray-500 uppercase tracking-wider">
                                {t("internshipOffersList.table.actions")}
                            </th>
                        </tr>
                        </thead>
                        <tbody className="bg-white divide-y divide-gray-200">
                        {filteredOffers.map((offer) => {
                            const cData = candidatureData[offer.id];
                            const count = cData ? cData.count : (loadingCounts ? '…' : 0);
                            const preview = cData && cData.preview && cData.preview.length ? cData.preview.join(', ') : '';
                            const statusUpper = (offer.status || '').toUpperCase();
                            const isClickable = statusUpper === 'APPROVED' || statusUpper === 'PUBLISHED';
                            return (
                                <tr key={offer.id}
                                    className={`hover:bg-gray-50 ${isClickable ? 'cursor-pointer' : 'cursor-not-allowed opacity-50'}`}
                                    {...(isClickable ? {
                                        onClick: (e) => {
                                            if (!(e.target.closest('button'))) navigate(`/dashboard/employeur/offers/${offer.id}/candidatures`);
                                        }
                                    } : {})}
                                >
                                    <td className="px-4 py-4">
                                        <div className="flex items-center">
                                            <div className="flex-shrink-0">
                                                <div
                                                    className="h-8 w-8 bg-blue-500 rounded flex items-center justify-center">
                                                    <span className="text-white text-sm font-bold">📋</span>
                                                </div>
                                            </div>
                                            <div className="ml-3 min-w-0">
                                                <div className="text-sm font-medium text-gray-900 truncate">
                                                    {offer.description || t("internshipOffersList.notDefined")}
                                                </div>
                                                <div className="text-xs text-gray-500">
                                                    {offer.durationInWeeks || 0} {(offer.durationInWeeks || 0) > 1 ? t("internshipOffersList.weeks") : t("internshipOffersList.week")}
                                                </div>
                                            </div>
                                        </div>
                                    </td>
                                    <td className="px-4 py-4 whitespace-nowrap text-sm text-gray-900">
                                        {formatDate(offer.startDate)}
                                    </td>
                                    <td className="px-4 py-4 whitespace-nowrap">
                                        {getStatusLabel(offer.status)}
                                    </td>
                                    <td className="px-4 py-4 whitespace-nowrap text-sm">
                                        <span title={preview}
                                              className="inline-flex items-center px-2 py-1 rounded bg-indigo-50 text-indigo-700 text-xs font-medium border border-indigo-100">
                                            {loadingCounts && !cData ? t('internshipOffersList.loading') : count}
                                        </span>
                                    </td>
                                    <td className="px-4 py-4 whitespace-nowrap text-sm font-medium"
                                        onClick={(e) => e.stopPropagation()}>
                                        <div className="flex items-center justify-center gap-1">
                                            {statusUpper === "REJECTED" && (
                                                <button
                                                    onClick={() => {
                                                        setCurrentComment(offer.rejectionComment || t('internshipOffersList.noRejectionComment'));
                                                        setShowCommentModal(true);
                                                    }}
                                                    className="inline-flex items-center px-3 py-2 border border-transparent text-sm leading-4 font-medium rounded-md text-red-700 bg-red-100 hover:bg-red-200"
                                                    title={t('internshipOffersList.viewRejectionComment')}
                                                >
                                                    <span className="mr-1">💬</span>
                                                    <span
                                                        className="hidden xl:inline">{t('internshipOffersList.viewRejectionComment')}</span>
                                                </button>
                                            )}
                                            <button
                                                onClick={() => handlePreviewOffer(offer.id)}
                                                className="inline-flex items-center px-3 py-2 border border-transparent text-sm leading-4 font-medium rounded-md text-indigo-700 bg-indigo-100 hover:bg-indigo-200"
                                                title={t('internshipOffersList.preview')}
                                            >
                                                <span className="mr-1">👁</span>
                                                <span className="hidden xl:inline">{t("previewPdf.preview")}</span>
                                            </button>
                                            {statusUpper === "PUBLISHED" || statusUpper === "APPROVED" ? (
                                                <button
                                                    onClick={() => handleDisableOffer(offer.id)}
                                                    className="inline-flex items-center px-3 py-2 border text-sm rounded-md text-gray-700 bg-red-300 hover:bg-red-200"
                                                    title={t("internshipOffersList.disable")}
                                                >
                                                    <span className="mr-1">🔒</span>
                                                    <span
                                                        className="hidden xl:inline">{t("internshipOffersList.disable")}</span>
                                                </button>
                                            ) : (
                                                <button
                                                    onClick={() => handleEnableOffer(offer)}
                                                    className="inline-flex items-center px-3 py-2 border text-sm rounded-md text-green-700 bg-green-100 hover:bg-green-200"
                                                    title={t("internshipOffersList.enable")}
                                                >
                                                    <span className="mr-1">🔓</span>
                                                    <span
                                                        className="hidden xl:inline">{t("internshipOffersList.enable")}</span>
                                                </button>
                                            )}
                                        </div>
                                    </td>
                                </tr>
                            );
                        })}
                        </tbody>
                    </table>
                </div>

                <div className="lg:hidden divide-y divide-gray-200">
                    {filteredOffers.map((offer) => {
                        const cData = candidatureData[offer.id];
                        const count = cData ? cData.count : (loadingCounts ? '…' : 0);
                        const statusUpper = (offer.status || '').toUpperCase();
                        const isClickable = statusUpper === 'APPROVED' || statusUpper === 'PUBLISHED';

                        return (
                            <div
                                key={offer.id}
                                className={`p-4 ${isClickable ? 'cursor-pointer hover:bg-gray-50' : 'cursor-not-allowed opacity-50'}`}
                                {...(isClickable ? {
                                    onClick: (e) => {
                                        if (!(e.target.closest('button'))) navigate(`/dashboard/employeur/offers/${offer.id}/candidatures`);
                                    }
                                } : {})}
                            >
                                <div className="flex items-start justify-between mb-3">
                                    <div className="flex items-start flex-1 min-w-0">
                                        <div className="flex-shrink-0">
                                            <div
                                                className="h-10 w-10 bg-blue-500 rounded flex items-center justify-center">
                                                <span className="text-white text-lg">📋</span>
                                            </div>
                                        </div>
                                        <div className="ml-3 flex-1 min-w-0">
                                            <h4 className="text-sm font-semibold text-gray-900 truncate">
                                                {offer.description || t("internshipOffersList.notDefined")}
                                            </h4>
                                            <p className="text-xs text-gray-500 mt-1">
                                                {formatDate(offer.startDate)} • {offer.durationInWeeks || 0} {(offer.durationInWeeks || 0) > 1 ? t("internshipOffersList.weeks") : t("internshipOffersList.week")}
                                            </p>
                                        </div>
                                    </div>
                                </div>

                                <div className="flex items-center justify-center gap-2 mb-3">
                                    {getStatusLabel(offer.status)}
                                    <span
                                        className="inline-flex items-center px-2 py-1 rounded bg-indigo-50 text-indigo-700 text-xs font-medium border border-indigo-100">
                                        {loadingCounts && !cData ? '…' : count} 👥
                                    </span>
                                </div>

                                <div className="flex items-center gap-2" onClick={(e) => e.stopPropagation()}>
                                    {statusUpper === "REJECTED" && (
                                        <button
                                            onClick={() => {
                                                setCurrentComment(offer.rejectionComment || t('internshipOffersList.noRejectionComment'));
                                                setShowCommentModal(true);
                                            }}
                                            className="flex-1 inline-flex items-center justify-center px-3 py-2 border border-transparent text-sm font-medium rounded-md text-red-700 bg-red-100 hover:bg-red-200"
                                        >
                                            💬
                                        </button>
                                    )}
                                    <button
                                        onClick={() => handlePreviewOffer(offer.id)}
                                        className="flex-1 inline-flex items-center justify-center px-3 py-2 border border-transparent text-sm font-medium rounded-md text-indigo-700 bg-indigo-100 hover:bg-indigo-200"
                                    >
                                        👁
                                    </button>
                                    {statusUpper === "PUBLISHED" || statusUpper === "APPROVED" ? (
                                        <button
                                            onClick={() => handleDisableOffer(offer.id)}
                                            className="flex-1 inline-flex items-center justify-center px-3 py-2 border text-sm font-medium rounded-md text-gray-700 bg-red-300 hover:bg-red-200"
                                        >
                                            🔒
                                        </button>
                                    ) : (
                                        <button
                                            onClick={() => handleEnableOffer(offer)}
                                            className="flex-1 inline-flex items-center justify-center px-3 py-2 border text-sm font-medium rounded-md text-green-700 bg-green-100 hover:bg-green-200"
                                        >
                                            🔓
                                        </button>
                                    )}
                                </div>
                            </div>
                        );
                    })}
                </div>

                <div className="px-4 sm:px-6 py-3 bg-gray-50 text-right">
                    <button
                        onClick={handleCreateOffer}
                        className="inline-flex items-center px-4 py-2 text-sm font-medium text-indigo-600 hover:text-indigo-500"
                    >
                        {t("internshipOffersList.createNewOffer")}
                        <span className="ml-1">➕</span>
                    </button>
                </div>

                {showCommentModal && (
                    <div className="fixed inset-0 z-50 flex items-center justify-center bg-black bg-opacity-40 p-4">
                        <div className="bg-white rounded-lg shadow-lg p-6 max-w-xl w-full max-h-[80vh] overflow-hidden">
                            <h2 className="text-lg font-semibold mb-4 text-red-700">
                                {t('internshipOffersList.rejectionCommentTitle', 'Commentaire de rejet')}
                            </h2>

                            <div className="text-gray-800 overflow-auto max-h-[60vh]">
                                <pre className="whitespace-pre-wrap break-words text-sm" style={{margin: 0}}>
                                    {currentComment}
                                </pre>
                            </div>

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

            {showConfirmModal && (
                <div className="fixed inset-0 z-50 flex items-center justify-center bg-black bg-opacity-40 p-4">
                    <div className="bg-white rounded-lg shadow-lg p-6 max-w-md w-full">
                        <h2 className="text-lg font-semibold mb-4 text-gray-800">
                            {t("internshipOffersList.confirm", "Confirmation")}
                        </h2>

                        <p className="text-gray-700 mb-6 whitespace-pre-line">
                            {confirmMessage}
                        </p>

                        <div className="flex justify-end gap-3">
                            <button
                                onClick={() => setShowConfirmModal(false)}
                                className="px-4 py-2 bg-gray-300 rounded hover:bg-gray-400"
                            >
                                {t("common.cancel", "Cancel")}
                            </button>
                            <button
                                onClick={() => {
                                    setShowConfirmModal(false);
                                    onConfirm && onConfirm();
                                }}
                                className="px-4 py-2 bg-indigo-600 text-white rounded hover:bg-indigo-700"
                            >
                                {t("internshipOffersList.yes", "Yes")}
                            </button>
                        </div>
                    </div>
                </div>
            )}

            {infoMessage && (
                <div className="fixed inset-0 z-50 flex items-center justify-center bg-black bg-opacity-40 p-4">
                    <div className="bg-white rounded-lg shadow-lg p-6 max-w-md w-full">
                        <h2 className="text-lg font-semibold mb-4 text-indigo-700">
                            {t("internshipOffersList.info")}
                        </h2>

                        <p className="text-gray-700 mb-6 whitespace-pre-line">
                            {infoMessage}
                        </p>

                        <div className="flex justify-end">
                            <button
                                onClick={() => setInfoMessage(null)}
                                className="px-4 py-2 bg-indigo-600 text-white rounded hover:bg-indigo-700"
                            >
                                {t("internshipOffersList.ok")}
                            </button>
                        </div>
                    </div>
                </div>
            )}

            {showPdfModal && (
                <PdfViewer
                    file={pdfUrl}
                    onClose={() => {
                        setShowPdfModal(false);
                        if (pdfUrl) window.URL.revokeObjectURL(pdfUrl);
                    }}
                />
            )}
        </>
    );
}