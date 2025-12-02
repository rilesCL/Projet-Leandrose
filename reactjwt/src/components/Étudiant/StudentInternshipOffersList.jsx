import React, {useEffect, useState} from "react";
import {getPublishedOffers} from "../../api/apiStudent.jsx";
import {useNavigate} from "react-router-dom";
import {useTranslation} from "react-i18next";

export default function StudentInternshipOffersList({studentInfo, onReregisterClick}) {
    const {t} = useTranslation();
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

    const formatDate = (dateString) =>
        dateString
            ? new Date(dateString).toLocaleDateString("fr-FR")
            : t("StudentInternshipOffersList.startDate");

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

    // --- gestion erreurs, loading etc. ---

    if (loading) {
        return (
            <div className="bg-white shadow rounded-lg p-8 text-center">
                <h3 className="text-lg font-medium text-gray-900 mb-2">
                    {t("StudentInternshipOffersList.loading")}
                </h3>
            </div>
        );
    }

    if (error) {
        return (
            <div className="bg-white shadow rounded-lg p-8 text-center">
                <h3 className="text-lg font-medium text-red-600 mb-2">
                    {t("StudentInternshipOffersList.error")}
                </h3>
                <p className="text-gray-600">{error}</p>
            </div>
        );
    }

    if (!offers.length) {
        return (
            <div className="bg-white shadow rounded-lg p-8 text-center">
                <div className="text-gray-400 mb-4">
                    <div className="mx-auto h-16 w-16 rounded bg-gray-100 flex items-center justify-center">
                        <span className="text-gray-500 text-3xl">📋</span>
                    </div>
                </div>
                <h3 className="text-lg font-medium text-gray-900 mb-2">
                    {t("StudentInternshipOffersList.noOffersTitle")}
                </h3>
                <p className="text-gray-600">
                    {t("StudentInternshipOffersList.noOffersText")}
                </p>
            </div>
        );
    }

    return (
        <div className="bg-white shadow rounded-lg overflow-hidden">

            {/* HEADER */}
            <div className="px-6 py-4 border-b border-gray-200">
                <h3 className="text-lg font-medium text-gray-900">
                    {t("StudentInternshipOffersList.availableOffers")}
                </h3>
                <p className="text-sm text-gray-600">
                    {offers.length} offre{offers.length > 1 ? "s" : ""} disponible
                    {offers.length > 1 ? "s" : ""}
                </p>
            </div>

            {/* DESKTOP TABLE */}
            <div className="hidden lg:block overflow-x-auto">
                <table className="min-w-full divide-y divide-gray-200">
                    <thead className="bg-gray-50">
                    <tr>
                        <th className="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">
                            {t("StudentInternshipOffersList.description")}
                        </th>
                        <th className="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">
                            {t("ApplicationsPage.company")}
                        </th>
                        <th className="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">
                            {t("StudentInternshipOffersList.place")}
                        </th>
                        <th className="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">
                            {t("StudentInternshipOffersList.startDate")}
                        </th>
                        {/* ❌ COLUMN TERM REMOVED */}
                        <th className="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">
                            {t("StudentInternshipOffersList.duration")}
                        </th>
                        <th className="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">
                            {t("StudentInternshipOffersList.actions")}
                        </th>
                    </tr>
                    </thead>

                    <tbody className="bg-white divide-y divide-gray-200">
                    {offers.map((offer) => (
                        <tr key={offer.id} className="hover:bg-gray-50">

                            <td className="px-6 py-4">
                                <div className="flex items-center">
                                    <div className="h-8 w-8 bg-blue-500 rounded flex items-center justify-center">
                                        <span className="text-white text-sm">📋</span>
                                    </div>
                                    <div className="ml-3 max-w-xs truncate">
                                        <div className="text-sm font-medium text-gray-900 truncate">
                                            {offer.description}
                                        </div>
                                        <div className="text-sm text-gray-500">{offer.address}</div>
                                    </div>
                                </div>
                            </td>

                            <td className="px-6 py-4 text-sm text-gray-900">{offer.companyName || "N/A"}</td>

                            <td className="px-6 py-4 text-sm text-gray-900">{offer.address}</td>

                            <td className="px-6 py-4 whitespace-nowrap text-sm text-gray-900">
                                {formatDate(offer.startDate)}
                            </td>

                            {/* ❌ TERM REMOVED */}

                            <td className="px-6 py-4 whitespace-nowrap text-sm text-gray-900">
                                {offer.durationInWeeks} semaine{offer.durationInWeeks > 1 ? "s" : ""}
                            </td>

                            <td className="px-6 py-4 whitespace-nowrap text-sm font-medium">
                                <button
                                    onClick={() => navigate(`/dashboard/student/offers/${offer.id}`)}
                                    className="text-indigo-600 hover:text-indigo-900"
                                >
                                    {t("StudentInternshipOffersList.viewDetails")}
                                </button>
                            </td>
                        </tr>
                    ))}
                    </tbody>
                </table>
            </div>

            {/* MOBILE VERSION */}
            <div className="lg:hidden divide-y divide-gray-200">
                {offers.map((offer) => (
                    <div key={offer.id} className="p-4 hover:bg-gray-50">

                        <h4 className="text-sm font-semibold text-gray-900 truncate">
                            {offer.description}
                        </h4>

                        <p className="text-xs text-gray-600">{offer.companyName}</p>
                        <p className="text-xs text-gray-500">{offer.address}</p>

                        <div className="mt-3 text-xs text-gray-700">
                            <p>
                                <strong>{t("StudentInternshipOffersList.startDate")}:</strong>{" "}
                                {formatDate(offer.startDate)}
                            </p>
                            <p>
                                <strong>{t("StudentInternshipOffersList.duration")}:</strong>{" "}
                                {offer.durationInWeeks} sem.
                            </p>
                        </div>

                        <button
                            onClick={() => navigate(`/dashboard/student/offers/${offer.id}`)}
                            className="mt-4 w-full inline-flex justify-center px-3 py-2 text-indigo-600 bg-indigo-50 rounded-md hover:bg-indigo-100"
                        >
                            {t("StudentInternshipOffersList.viewDetails")}
                        </button>

                    </div>
                ))}
            </div>

        </div>
    );
}
