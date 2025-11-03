import React, { useEffect, useState } from "react";
import { getPublishedOffers } from "../../api/apiStudent.jsx";
import { useNavigate } from "react-router-dom";
import { useTranslation } from "react-i18next";

export default function StudentInternshipOffersList({ studentInfo, onReregisterClick }) {
    const { t } = useTranslation();
    const navigate = useNavigate();

    const [offers, setOffers] = useState([]);
    const [loading, setLoading] = useState(true);
    const [error, setError] = useState(null);

    const getProgramLabel = (program) => {
        if (!program) return "";
        if (typeof program === "string") return t(program);
        if (typeof program === "object") {
            return t(program.translationKey || "") || program.code || "";
        }
        return "";
    };

    const translateSchoolTerm = (termString) => {
        if (!termString || typeof termString !== 'string') return '';
        const parts = termString.trim().split(/\s+/);
        const seasonKey = parts.shift().toUpperCase();
        const rest = parts.join(' ');
        const translationKey = `terms.${seasonKey}`;
        const translated = t(translationKey);
        const seasonLabel = (translated === translationKey)
            ? (seasonKey.charAt(0).toUpperCase() + seasonKey.slice(1).toLowerCase())
            : translated;
        return rest ? `${seasonLabel} ${rest}` : seasonLabel;
    };

    useEffect(() => {
        async function fetchOffers() {
            setLoading(true);
            setError(null);
            try {
                const data = await getPublishedOffers();
                setOffers(Array.isArray(data) ? data : []);
            } catch (err) {
                console.error(err);
                setError(t("StudentInternshipOffersList.errorText"));
            } finally {
                setLoading(false);
            }
        }
        fetchOffers();
    }, [t]);

    const formatDate = (dateString) => dateString ? new Date(dateString).toLocaleDateString('fr-FR') : t("StudentInternshipOffersList.startDate");

    if (studentInfo?.expired) {
        return (
            <div className="bg-white shadow rounded-lg p-8 text-center">
                <div className="mx-auto mb-4 h-16 w-16 rounded-full bg-amber-100 flex items-center justify-center">
                    <span className="text-amber-600 text-3xl">⚠️</span>
                </div>
                <h3 className="text-xl font-semibold text-gray-900 mb-3">
                    {t("StudentInternshipOffersList.reregistrationRequired") || "Réinscription requise"}
                </h3>
                <p className="text-gray-700 mb-2">
                    {t("StudentInternshipOffersList.sessionExpired") ||
                        "Votre session d'inscription est expirée."}
                </p>
                <p className="text-gray-600 mb-6">
                    {t("StudentInternshipOffersList.reregisterToSeeOffers") ||
                        "Veuillez vous réinscrire pour voir les offres de la nouvelle session."}
                </p>
                <button
                    onClick={onReregisterClick}
                    className="inline-flex items-center px-6 py-3 bg-gradient-to-r from-indigo-600 to-indigo-700 text-white font-semibold rounded-lg hover:from-indigo-700 hover:to-indigo-800 transition-all shadow-lg shadow-indigo-500/50"
                >
                    {t("StudentInternshipOffersList.reregisterNow") || "Me réinscrire maintenant"}
                </button>
            </div>
        );
    }

    if (loading) return (
        <div className="bg-white shadow rounded-lg p-8 text-center">
            <div className="animate-pulse">
                <div className="h-4 bg-gray-200 rounded w-1/4 mx-auto mb-4"></div>
                <div className="h-4 bg-gray-200 rounded w-1/2 mx-auto"></div>
            </div>
            <p className="text-gray-600 mt-4">{t("StudentInternshipOffersList.loading")}</p>
        </div>
    );

    if (error) return (
        <div className="bg-white shadow rounded-lg p-8 text-center">
            <div className="text-red-600 mb-4">
                <div className="mx-auto h-12 w-12 rounded-full bg-red-100 flex items-center justify-center">
                    <span className="text-red-600 text-2xl font-bold">!</span>
                </div>
            </div>
            <h3 className="text-lg font-medium text-gray-900 mb-2">{t("StudentInternshipOffersList.noOffersTitle")}</h3>
            <p className="text-red-600">{error}</p>
            <button onClick={() => window.location.reload()} className="mt-4 px-4 py-2 bg-red-600 text-white rounded hover:bg-red-700">{t("ApplicationsPage.retry")}</button>
        </div>
    );

    if (!offers.length) return (
        <div className="bg-white shadow rounded-lg p-8 text-center">
            <div className="text-gray-400 mb-4">
                <div className="mx-auto h-16 w-16 rounded bg-gray-100 flex items-center justify-center">
                    <span className="text-gray-500 text-3xl">📋</span>
                </div>
            </div>
            <h3 className="text-lg font-medium text-gray-900 mb-2">{t("StudentInternshipOffersList.noOffersTitle")}</h3>
            <p className="text-gray-600">{t("StudentInternshipOffersList.noOffersText")}</p>
        </div>
    );

    return (
        <div className="bg-white shadow rounded-lg overflow-hidden">
            <div className="px-6 py-4 border-b border-gray-200">
                <h3 className="text-lg font-medium text-gray-900">{t("StudentInternshipOffersList.availableOffers")}</h3>
                <p className="text-sm text-gray-600">{offers.length} offre{offers.length > 1 ? 's' : ''} disponible{offers.length > 1 ? 's' : ''}</p>
            </div>
            <div className="overflow-x-auto">
                <table className="min-w-full divide-y divide-gray-200">
                    <thead className="bg-gray-50">
                    <tr>
                        <th className="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">{t("StudentInternshipOffersList.description")}</th>
                        <th className="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">{t("ApplicationsPage.company")}</th>
                        <th className="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">{t("StudentInternshipOffersList.place")}</th>
                        <th className="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">{t("StudentInternshipOffersList.startDate")}</th>
                        <th className="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">{t("StudentInternshipOffersList.duration")}</th>
                        <th className="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">{t("StudentInternshipOffersList.actions")}</th>
                    </tr>
                    </thead>
                    <tbody className="bg-white divide-y divide-gray-200">
                    {offers.map((offer) => (
                        <tr key={offer.id} className="hover:bg-gray-50">
                            <td className="px-6 py-4">
                                <div className="flex items-center">
                                    <div className="flex-shrink-0">
                                        <div className="h-8 w-8 bg-blue-500 rounded flex items-center justify-center">
                                            <span className="text-white text-sm font-bold">📋</span>
                                        </div>
                                    </div>
                                    <div className="ml-3">
                                        <div className="text-sm font-medium text-gray-900 truncate max-w-xs">{offer.description || t("StudentInternshipOffersList.description")}</div>
                                        <div className="text-sm text-gray-500">{offer.address || t("StudentInternshipOffersList.place")}</div>
                                    </div>
                                </div>
                            </td>
                            <td className="px-6 py-4 text-sm text-gray-900">{offer.companyName || "N/A"}</td>
                            <td className="px-6 py-4 text-sm text-gray-900">{offer.address || t("StudentInternshipOffersList.place")}</td>
                            <td className="px-6 py-4 whitespace-nowrap text-sm text-gray-900">{formatDate(offer.startDate)}</td>
                            <td className="px-6 py-4 whitespace-nowrap text-sm text-gray-900">{offer.durationInWeeks} semaine{offer.durationInWeeks > 1 ? 's' : ''}</td>
                            <td className="px-6 py-4 whitespace-nowrap text-sm font-medium">
                                <button onClick={() => navigate(`/dashboard/student/offers/${offer.id}`)} className="text-indigo-600 hover:text-indigo-900">{t("StudentInternshipOffersList.viewDetails")}</button>
                            </td>
                        </tr>
                    ))}
                    </tbody>
                </table>
            </div>
        </div>
    );
}