import React, {useEffect, useState} from "react";
import {getMyCandidatures} from "../../api/apiStudent.jsx";
import {useNavigate} from "react-router-dom";
import {useTranslation} from "react-i18next";

export default function ApplicationsPage() {
    const navigate = useNavigate();
    const {t} = useTranslation();

    const [candidatures, setCandidatures] = useState([]);
    const [loading, setLoading] = useState(true);
    const [error, setError] = useState(null);

    useEffect(() => {
        async function fetchCandidatures() {
            setLoading(true);
            setError(null);

            try {
                const data = await getMyCandidatures();
                setCandidatures(Array.isArray(data) ? data : []);
            } catch (err) {
                console.error(err);
                setError(t("ApplicationsPage.error"));
            } finally {
                setLoading(false);
            }
        }

        fetchCandidatures();
    }, [t]);

    const formatDate = (dateString) => {
        if (!dateString) return t("ApplicationsPage.applicationDate");
        return new Date(dateString).toLocaleDateString("fr-FR");
    };

    const getStatusBadge = (status) => {
        const statusUpper = (status || "").toUpperCase();
        const base = "px-3 py-1 text-xs font-medium rounded-full border";

        if (statusUpper === "PENDING") return <span
            className={`${base} bg-yellow-100 text-yellow-800 border-yellow-200`}>{t("ApplicationsPage.statusPending")}</span>;
        if (statusUpper === "ACCEPTED") return <span
            className={`${base} bg-green-100 text-green-800 border-green-200`}>{t("ApplicationsPage.statusAccepted")}</span>;
        if (statusUpper === "REJECTED") return <span
            className={`${base} bg-red-100 text-red-800 border-red-200`}>{t("ApplicationsPage.statusRejected")}</span>;
        return <span className={`${base} bg-gray-100 text-gray-800 border-gray-200`}>{statusUpper}</span>;
    };

    if (loading) {
        return (
            <div className="bg-white shadow rounded-lg p-8 text-center">
                <div className="animate-pulse">{t("ApplicationsPage.loading")}</div>
            </div>
        );
    }

    if (error) {
        return (
            <div className="bg-white shadow rounded-lg p-8 text-center">
                <p className="text-red-600">{error}</p>
                <button onClick={() => window.location.reload()}
                        className="mt-4 px-4 py-2 bg-red-600 text-white rounded">{t("ApplicationsPage.retry")}</button>
            </div>
        );
    }

    if (!candidatures || candidatures.length === 0) {
        return (
            <div className="bg-white shadow rounded-lg p-8 text-center">
                <div className="text-gray-400 mb-4">
                    <div className="mx-auto h-16 w-16 rounded bg-gray-100 flex items-center justify-center">
                        <span className="text-gray-500 text-3xl">📄</span>
                    </div>
                </div>
                <h3 className="text-lg font-medium text-gray-900 mb-2">{t("ApplicationsPage.noApplicationsTitle")}</h3>
                <p className="text-gray-600 mb-4">{t("ApplicationsPage.noApplicationsText")}</p>
                <button onClick={() => navigate('/dashboard/student')}
                        className="px-4 py-2 bg-indigo-600 text-white rounded hover:bg-indigo-700">{t("ApplicationsPage.viewOffers")}</button>
            </div>
        );
    }

    return (
        <div className="bg-white shadow rounded-lg overflow-hidden">
            <div className="px-6 py-4 border-b border-gray-200">
                <h3 className="text-lg font-medium text-gray-900">{t("ApplicationsPage.myApplications")}</h3>
                <p className="text-sm text-gray-600">{t("ApplicationsPage.applicationsCount", {count: candidatures.length})}</p>
            </div>
            <div className="overflow-x-auto">
                <table className="min-w-full divide-y divide-gray-200">
                    <thead className="bg-gray-50">
                    <tr>
                        <th className="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">{t("ApplicationsPage.offer")}</th>
                        <th className="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">{t("ApplicationsPage.company")}</th>
                        <th className="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">{t("ApplicationsPage.applicationDate")}</th>
                        <th className="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">{t("ApplicationsPage.status")}</th>
                        <th className="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">{t("ApplicationsPage.actions")}</th>
                    </tr>
                    </thead>
                    <tbody className="bg-white divide-y divide-gray-200">
                    {candidatures.map((candidature) => (
                        <tr key={candidature.id} className="hover:bg-gray-50">
                            <td className="px-6 py-4 text-sm font-medium text-gray-900">{candidature.offerDescription}</td>
                            <td className="px-6 py-4 text-sm text-gray-900">{candidature.companyName}</td>
                            <td className="px-6 py-4 whitespace-nowrap text-sm text-gray-900">{formatDate(candidature.applicationDate)}</td>
                            <td className="px-6 py-4 whitespace-nowrap">{getStatusBadge(candidature.status)}</td>
                            <td className="px-6 py-4 whitespace-nowrap text-sm font-medium">
                                <button onClick={() => navigate(`/dashboard/student/offers/${candidature.offerId}`)}
                                        className="text-indigo-600 hover:text-indigo-900">{t("ApplicationsPage.viewOffer")}</button>
                            </td>
                        </tr>
                    ))}
                    </tbody>
                </table>
            </div>
        </div>
    );
}
