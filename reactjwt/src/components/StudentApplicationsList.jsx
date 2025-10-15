import React, { useEffect, useState } from 'react';
import { getMyCandidatures, getMyConvocations } from '../api/apiStudent';
import { useNavigate } from 'react-router-dom';
import { useTranslation } from 'react-i18next';

export default function StudentApplicationsList() {
    const { t } = useTranslation();
    const navigate = useNavigate();
    const [candidatures, setCandidatures] = useState([]);
    const [convocations, setConvocations] = useState([]);
    const [loading, setLoading] = useState(true);
    const [error, setError] = useState(null);
    const [showConvocationModal, setShowConvocationModal] = useState(false);
    const [selectedConvocation, setSelectedConvocation] = useState(null);

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

                try {
                    const convocationsData = await getMyConvocations();
                    if (!cancelled) {
                        setConvocations(Array.isArray(convocationsData) ? convocationsData : []);
                    }
                } catch (convError) {
                    console.error('Error loading convocations:', convError);
                }
            } catch (e) {
                if (!cancelled) setError(t("studentApplicationsList.loadError"));
            } finally {
                if (!cancelled) setLoading(false);
            }
        }
        fetchData();
        return () => { cancelled = true; };
    }, [t]);

    const formatDate = (dateString) => {
        if (!dateString) return t("studentApplicationsList.noDate");
        return new Date(dateString).toLocaleDateString();
    };

    const formatDateTime = (dateTimeString) => {
        if (!dateTimeString) return t("studentApplicationsList.noDate");
        const date = new Date(dateTimeString);
        return date.toLocaleString('fr-FR', {
            day: '2-digit',
            month: '2-digit',
            year: 'numeric',
            hour: '2-digit',
            minute: '2-digit'
        });
    };

    const openConvocationModal = (candidature) => {
        const convocation = convocations.find(conv => conv.candidatureId === candidature.id);
        setSelectedConvocation(convocation);
        setShowConvocationModal(true);
    };

    const closeConvocationModal = () => {
        setShowConvocationModal(false);
        setSelectedConvocation(null);
    };

    const getStatusBadge = (status, candidature) => {
        const s = (status || '').toUpperCase();
        const base = 'px-3 py-1 text-xs font-medium rounded-full border';
        switch (s) {
            case 'PENDING':
                return <span className={`${base} bg-yellow-100 text-yellow-800 border-yellow-200`}>{t("studentApplicationsList.status.pending")}</span>;
            case 'CONVENED':
                return (
                    <button
                        onClick={() => openConvocationModal(candidature)}
                        className={`${base} bg-blue-100 text-blue-800 border-blue-200 hover:bg-blue-200 cursor-pointer transition-colors`}
                    >
                        {t("studentApplicationsList.status.CONVENED")}
                    </button>
                );
            case 'ACCEPTED':
                return <span className={`${base} bg-green-100 text-green-800 border-green-200`}>{t("studentApplicationsList.status.accepted")}</span>;
            case 'REJECTED':
                return <span className={`${base} bg-red-100 text-red-800 border-red-200`}>{t("studentApplicationsList.status.rejected")}</span>;
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
                    {t("studentApplicationsList.retry")}
                </button>
            </div>
        );
    }

    if (!candidatures.length) {
        return (
            <div className="bg-white shadow rounded-lg p-6 text-center">
                <div className="mx-auto mb-4 h-14 w-14 rounded-full bg-gray-100 flex items-center justify-center text-2xl">📄</div>
                <h3 className="text-lg font-medium text-gray-900 mb-2">{t("studentApplicationsList.noApplicationsTitle")}</h3>
                <p className="text-gray-600 mb-4">{t("studentApplicationsList.noApplicationsText")}</p>
            </div>
        );
    }

    return (
        <>
            <div className="bg-white shadow rounded-lg overflow-hidden">
                <div className="px-6 py-4 border-b border-gray-200 flex items-center justify-between">
                    <div>
                        <h3 className="text-lg font-medium text-gray-900">{t("studentApplicationsList.title")}</h3>
                        <p className="text-sm text-gray-600">
                            {t("studentApplicationsList.count", { count: candidatures.length })}
                        </p>
                    </div>
                </div>
                <div className="overflow-x-auto">
                    <table className="min-w-full divide-y divide-gray-200">
                        <thead className="bg-gray-50">
                        <tr>
                            <th className="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">{t("studentApplicationsList.table.offer")}</th>
                            <th className="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">{t("studentApplicationsList.table.company")}</th>
                            <th className="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">{t("studentApplicationsList.table.date")}</th>
                            <th className="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">{t("studentApplicationsList.table.status")}</th>
                            <th className="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">{t("studentApplicationsList.table.actions")}</th>
                        </tr>
                        </thead>
                        <tbody className="bg-white divide-y divide-gray-200">
                        {candidatures.map(c => (
                            <tr key={c.id} className="hover:bg-gray-50">
                                <td className="px-6 py-4 text-sm font-medium text-gray-900">{c.offerDescription}</td>
                                <td className="px-6 py-4 text-sm text-gray-900">{c.companyName}</td>
                                <td className="px-6 py-4 text-sm text-gray-900 whitespace-nowrap">{formatDate(c.applicationDate)}</td>
                                <td className="px-6 py-4 whitespace-nowrap">{getStatusBadge(c.status, c)}</td>
                                <td className="px-6 py-4 whitespace-nowrap text-sm">
                                    <button
                                        onClick={() => navigate(`/dashboard/student/offers/${c.offerId}`)}
                                        className="text-indigo-600 hover:text-indigo-900"
                                    >
                                        {t("studentApplicationsList.viewOffer")}
                                    </button>
                                </td>
                            </tr>
                        ))}
                        </tbody>
                    </table>
                </div>
            </div>

            {showConvocationModal && (
                <div className='fixed inset-0 bg-black bg-opacity-50 flex items-center justify-center z-50' onClick={closeConvocationModal}>
                    <div className='bg-white rounded-lg shadow-xl p-6 w-full max-w-md relative' onClick={(e) => e.stopPropagation()}>
                        <button
                            type='button'
                            onClick={closeConvocationModal}
                            className='absolute top-4 right-4 text-gray-400 hover:text-gray-600 transition-colors'
                        >
                            <svg className='w-6 h-6' fill='none' stroke='currentColor' viewBox='0 0 24 24'>
                                <path strokeLinecap='round' strokeLinejoin='round' strokeWidth={2} d='M6 18L18 6M6 6l12 12' />
                            </svg>
                        </button>

                        <h3 className='text-lg font-semibold mb-4 pr-8'>
                            {t('studentApplicationsList.convocationModal.title')}
                        </h3>

                        {selectedConvocation ? (
                            <div className='space-y-4'>
                                <div className='bg-blue-50 border-l-4 border-blue-500 p-4 rounded'>
                                    <p className='text-sm text-blue-800 font-medium'>
                                        {t('studentApplicationsList.convocationModal.description')}
                                    </p>
                                </div>

                                <div className='space-y-3'>
                                    <div className='flex items-start'>
                                        <svg className='w-5 h-5 text-gray-400 mt-0.5 mr-3' fill='none' stroke='currentColor' viewBox='0 0 24 24'>
                                            <path strokeLinecap='round' strokeLinejoin='round' strokeWidth={2} d='M8 7V3m8 4V3m-9 8h10M5 21h14a2 2 0 002-2V7a2 2 0 00-2-2H5a2 2 0 00-2 2v12a2 2 0 002 2z' />
                                        </svg>
                                        <div>
                                            <p className='text-xs text-gray-500 uppercase font-medium'>{t('studentApplicationsList.convocationModal.date')}</p>
                                            <p className='text-sm font-semibold text-gray-900'>{formatDateTime(selectedConvocation.convocationDate)}</p>
                                        </div>
                                    </div>

                                    <div className='flex items-start'>
                                        <svg className='w-5 h-5 text-gray-400 mt-0.5 mr-3' fill='none' stroke='currentColor' viewBox='0 0 24 24'>
                                            <path strokeLinecap='round' strokeLinejoin='round' strokeWidth={2} d='M17.657 16.657L13.414 20.9a1.998 1.998 0 01-2.827 0l-4.244-4.243a8 8 0 1111.314 0z' />
                                            <path strokeLinecap='round' strokeLinejoin='round' strokeWidth={2} d='M15 11a3 3 0 11-6 0 3 3 0 016 0z' />
                                        </svg>
                                        <div>
                                            <p className='text-xs text-gray-500 uppercase font-medium'>{t('studentApplicationsList.convocationModal.location')}</p>
                                            <p className='text-sm font-semibold text-gray-900'>{selectedConvocation.location}</p>
                                        </div>
                                    </div>

                                    {selectedConvocation.message && (
                                        <div className='flex items-start'>
                                            <svg className='w-5 h-5 text-gray-400 mt-0.5 mr-3' fill='none' stroke='currentColor' viewBox='0 0 24 24'>
                                                <path strokeLinecap='round' strokeLinejoin='round' strokeWidth={2} d='M7 8h10M7 12h4m1 8l-4-4H5a2 2 0 01-2-2V6a2 2 0 012-2h14a2 2 0 012 2v8a2 2 0 01-2 2h-3l-4 4z' />
                                            </svg>
                                            <div className='flex-1'>
                                                <p className='text-xs text-gray-500 uppercase font-medium mb-2'>{t('studentApplicationsList.convocationModal.message')}</p>
                                                {selectedConvocation.message ? (
                                                    <div className='bg-gray-50 border border-gray-200 rounded-lg p-3'>
                                                        <p className='text-sm text-gray-800 leading-relaxed whitespace-pre-wrap'>{selectedConvocation.message}</p>
                                                    </div>
                                                ) : (
                                                    <div className='bg-gray-50 border border-gray-200 rounded-lg p-3'>
                                                        <p className='text-sm text-gray-500 italic'>{t('studentApplicationsList.convocationModal.noMessage', 'Aucun message additionnel')}</p>
                                                    </div>
                                                )}
                                            </div>
                                        </div>
                                    )}
                                </div>
                            </div>
                        ) : (
                            <div className='bg-gray-50 p-4 rounded-lg'>
                                <p className='text-sm text-gray-500 text-center'>
                                    {t('studentApplicationsList.convocationModal.noInfo')}
                                </p>
                            </div>
                        )}

                        <div className='mt-6 flex justify-end'>
                            <button
                                onClick={closeConvocationModal}
                                className='px-4 py-2 bg-indigo-600 text-white rounded hover:bg-indigo-700'
                            >
                                {t('studentApplicationsList.convocationModal.close')}
                            </button>
                        </div>
                    </div>
                </div>
            )}
        </>
    );
}