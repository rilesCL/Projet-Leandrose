import React, { useEffect, useState } from 'react';
import { getMyCandidatures } from '../api/apiStudent';
import { useNavigate } from 'react-router-dom';
import { useTranslation } from 'react-i18next';

export default function StudentApplicationsList() {
    const { t } = useTranslation();
    const navigate = useNavigate();
    const [candidatures, setCandidatures] = useState([]);
    const [loading, setLoading] = useState(true);
    const [error, setError] = useState(null);

    useEffect(() => {
        let cancelled = false;
        async function fetchData() {
            setLoading(true);
            setError(null);
            try {
                const data = await getMyCandidatures();
                if (!cancelled) {
                    setCandidatures(Array.isArray(data) ? data : []);
                }
            } catch (e) {
                if (!cancelled) setError(t("StudentApplicationsList.loadError"));
            } finally {
                if (!cancelled) setLoading(false);
            }
        }
        fetchData();
        return () => { cancelled = true; };
    }, [t]);

    const formatDate = (dateString) => {
        if (!dateString) return t("StudentApplicationsList.noDate");
        return new Date(dateString).toLocaleDateString();
    };

    const getStatusBadge = (status) => {
        const s = (status || '').toUpperCase();
        const base = 'px-3 py-1 text-xs font-medium rounded-full border';
        switch (s) {
            case 'PENDING':
                return <span className={`${base} bg-yellow-100 text-yellow-800 border-yellow-200`}>{t("StudentApplicationsList.status.pending")}</span>;
            case 'ACCEPTED':
                return <span className={`${base} bg-green-100 text-green-800 border-green-200`}>{t("StudentApplicationsList.status.accepted")}</span>;
            case 'REJECTED':
                return <span className={`${base} bg-red-100 text-red-800 border-red-200`}>{t("StudentApplicationsList.status.rejected")}</span>;
            default:
                return <span className={`${base} bg-gray-100 text-gray-800 border-gray-200`}>{s}</span>;
        }
    };

    if (loading) {
        return (
            <div className="bg-white shadow rounded-lg p-6">
                <div className="animate-pulse h-4 bg-gray-200 w-1/3 mb-4 rounded" />
                <div className="space-y-2">
                    <div className="h-3 bg-gray-200 rounded" />
                    <div className="h-3 bg-gray-200 rounded w-5/6" />
                    <div className="h-3 bg-gray-200 rounded w-4/6" />
                </div>
            </div>
        );
    }

    if (error) {
        return (
            <div className="bg-white shadow rounded-lg p-6 text-center">
                <p className="text-red-600 mb-4">{error}</p>
                <button onClick={() => window.location.reload()} className="px-4 py-2 bg-red-600 text-white rounded">
                    {t("StudentApplicationsList.retry")}
                </button>
            </div>
        );
    }

    if (!candidatures.length) {
        return (
            <div className="bg-white shadow rounded-lg p-6 text-center">
                <div className="mx-auto mb-4 h-14 w-14 rounded-full bg-gray-100 flex items-center justify-center text-2xl">📄</div>
                <h3 className="text-lg font-medium text-gray-900 mb-2">{t("StudentApplicationsList.noApplicationsTitle")}</h3>
                <p className="text-gray-600 mb-4">{t("StudentApplicationsList.noApplicationsText")}</p>
                <button
                    onClick={() => navigate('/dashboard/student')}
                    className="px-4 py-2 bg-indigo-600 text-white rounded hover:bg-indigo-700"
                >
                    {t("StudentApplicationsList.viewOffers")}
                </button>
            </div>
        );
    }

    return (
        <div className="bg-white shadow rounded-lg overflow-hidden">
            <div className="px-6 py-4 border-b border-gray-200 flex items-center justify-between">
                <div>
                    <h3 className="text-lg font-medium text-gray-900">{t("StudentApplicationsList.title")}</h3>
                    <p className="text-sm text-gray-600">
                        {t("StudentApplicationsList.applicationCount", { count: candidatures.length })}
                    </p>
                </div>
            </div>
            <div className="overflow-x-auto">
                <table className="min-w-full divide-y divide-gray-200">
                    <thead className="bg-gray-50">
                    <tr>
                        <th className="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">{t("StudentApplicationsList.table.offer")}</th>
                        <th className="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">{t("StudentApplicationsList.table.company")}</th>
                        <th className="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">{t("StudentApplicationsList.table.date")}</th>
                        <th className="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">{t("StudentApplicationsList.table.status")}</th>
                        <th className="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">{t("StudentApplicationsList.table.actions")}</th>
                    </tr>
                    </thead>
                    <tbody className="bg-white divide-y divide-gray-200">
                    {candidatures.map(c => (
                        <tr key={c.id} className="hover:bg-gray-50">
                            <td className="px-6 py-4 text-sm font-medium text-gray-900">{c.offerDescription}</td>
                            <td className="px-6 py-4 text-sm text-gray-900">{c.companyName}</td>
                            <td className="px-6 py-4 text-sm text-gray-900 whitespace-nowrap">{formatDate(c.applicationDate)}</td>
                            <td className="px-6 py-4 whitespace-nowrap">{getStatusBadge(c.status)}</td>
                            <td className="px-6 py-4 whitespace-nowrap text-sm">
                                <button
                                    onClick={() => navigate(`/dashboard/student/offers/${c.offerId}`)}
                                    className="text-indigo-600 hover:text-indigo-900"
                                >
                                    {t("StudentApplicationsList.viewOffer")}
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
