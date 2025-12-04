import React, {useEffect, useState} from 'react';
import {
    acceptCandidatureByStudent,
    getMyCandidatures,
    getMyConvocations,
    rejectCandidatureByStudent
} from '../../api/apiStudent.jsx';
import {useNavigate} from 'react-router-dom';
import {useTranslation} from 'react-i18next';

export default function StudentApplicationsList() {
    const {t} = useTranslation();
    const navigate = useNavigate();
    const [candidatures, setCandidatures] = useState([]);
    const [convocations, setConvocations] = useState([]);
    const [loading, setLoading] = useState(true);
    const [error, setError] = useState(null);
    const [showConvocationModal, setShowConvocationModal] = useState(false);
    const [selectedConvocation, setSelectedConvocation] = useState(null);
    const [showRejectModal, setShowRejectModal] = useState(false);
    const [selectedCandidatureForReject, setSelectedCandidatureForReject] = useState(null);
    const [toast, setToast] = useState({show: false, message: '', type: 'success'});
    const [activeTab, setActiveTab] = useState('ALL');

    const closeToast = () => {
        setToast({show: false, message: '', type: 'success'});
    };

    const showToast = (message, type = 'success') => {
        setToast({show: true, message, type});
    };

    const loadData = async () => {
        setLoading(true);
        setError(null);
        try {
            const data = await getMyCandidatures();
            setCandidatures(Array.isArray(data) ? data : []);

            try {
                const convocationsData = await getMyConvocations();
                setConvocations(Array.isArray(convocationsData) ? convocationsData : []);
            } catch (convError) {
                console.error('Error loading convocations:', convError);
            }
        } catch (e) {
            setError(t("studentApplicationsList.loadError"));
        } finally {
            setLoading(false);
        }
    };

    useEffect(() => {
        let cancelled = false;

        async function fetchData() {
            await loadData();
        }

        if (!cancelled) fetchData();
        return () => {
            cancelled = true;
        };
    }, [t]);

    const handleAcceptCandidature = async (candidatureId) => {
        try {
            await acceptCandidatureByStudent(candidatureId);
            showToast(t("studentApplicationsList.acceptSuccess"), 'success');
            await loadData();
        } catch (error) {
            showToast(error.message || t("studentApplicationsList.acceptError"), 'error');
        }
    };

    const openRejectModal = (candidature) => {
        setSelectedCandidatureForReject(candidature);
        setShowRejectModal(true);
    };

    const closeRejectModal = () => {
        setShowRejectModal(false);
        setSelectedCandidatureForReject(null);
    };

    const confirmRejectCandidature = async () => {
        if (!selectedCandidatureForReject) return;

        try {
            await rejectCandidatureByStudent(selectedCandidatureForReject.id);
            showToast(t("studentApplicationsList.rejectSuccess"), 'success');
            closeRejectModal();
            await loadData();
        } catch (error) {
            showToast(error.message || t("studentApplicationsList.rejectError"), 'error');
            closeRejectModal();
        }
    };

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
                return <span
                    className={`${base} bg-yellow-100 text-yellow-800 border-yellow-200`}>{t("studentApplicationsList.status.PENDING")}</span>;
            case 'CONVENED':
                return (
                    <button
                        onClick={() => openConvocationModal(candidature)}
                        className={`${base} bg-blue-100 text-blue-800 border-blue-200 hover:bg-blue-200 cursor-pointer transition-colors`}
                    >
                        {t("studentApplicationsList.status.CONVENED")}
                    </button>
                );
            case 'ACCEPTEDBYEMPLOYEUR':
                return <span
                    className={`${base} bg-purple-100 text-purple-800 border-purple-200`}>{t("studentApplicationsList.status.ACCEPTEDBYEMPLOYEUR")}</span>;
            case 'ACCEPTED':
                return <span
                    className={`${base} bg-green-100 text-green-800 border-green-200`}>{t("studentApplicationsList.status.ACCEPTED")}</span>;
            case 'REJECTED':
                return <span
                    className={`${base} bg-red-100 text-red-800 border-red-200`}>{t("studentApplicationsList.status.REJECTED")}</span>;
            default:
                return <span className={`${base} bg-gray-100 text-gray-800 border-gray-200`}>{s}</span>;
        }
    };

    const canAcceptOrReject = (status) => {
        return status && status.toUpperCase() === 'ACCEPTEDBYEMPLOYEUR';
    };

    const getFilteredCandidatures = () => {
        if (activeTab === 'ALL') return candidatures;
        return candidatures.filter(c => c.status?.toUpperCase() === activeTab);
    };

    const filteredCandidatures = getFilteredCandidatures();

    const getCounts = () => {
        return {
            ALL: candidatures.length,
            PENDING: candidatures.filter(c => c.status?.toUpperCase() === 'PENDING').length,
            ACCEPTEDBYEMPLOYEUR: candidatures.filter(c => c.status?.toUpperCase() === 'ACCEPTEDBYEMPLOYEUR').length,
            CONVENED: candidatures.filter(c => c.status?.toUpperCase() === 'CONVENED').length,
            ACCEPTED: candidatures.filter(c => c.status?.toUpperCase() === 'ACCEPTED').length,
            REJECTED: candidatures.filter(c => c.status?.toUpperCase() === 'REJECTED').length,
        };
    };

    const counts = getCounts();

    const tabs = [
        {key: 'ALL', label: t("studentApplicationsList.tabs.all"), count: counts.ALL, color: 'text-gray-700'},
        {
            key: 'ACCEPTEDBYEMPLOYEUR',
            label: t("studentApplicationsList.tabs.awaiting"),
            count: counts.ACCEPTEDBYEMPLOYEUR,
            color: 'text-purple-700'
        },
        {
            key: 'PENDING',
            label: t("studentApplicationsList.tabs.pending"),
            count: counts.PENDING,
            color: 'text-yellow-700'
        },
        {
            key: 'CONVENED',
            label: t("studentApplicationsList.tabs.convened"),
            count: counts.CONVENED,
            color: 'text-blue-700'
        },
        {
            key: 'ACCEPTED',
            label: t("studentApplicationsList.tabs.accepted"),
            count: counts.ACCEPTED,
            color: 'text-green-700'
        },
        {
            key: 'REJECTED',
            label: t("studentApplicationsList.tabs.rejected"),
            count: counts.REJECTED,
            color: 'text-red-700'
        },
    ];

    if (loading) {
        return (
            <div className="bg-white shadow rounded-lg p-6">
                <div className="animate-pulse h-4 bg-gray-200 w-1/3 mb-4 rounded"/>
                <div className="space-y-2">
                    <div className="h-3 bg-gray-200 rounded"/>
                    <div className="h-3 bg-gray-200 rounded w-5/6"/>
                    <div className="h-3 bg-gray-200 rounded w-4/6"/>
                </div>
            </div>
        );
    }

    if (error) {
        return (
            <div className="bg-white shadow rounded-lg p-6 text-center">
                <p className="text-red-600 mb-4">{error}</p>
                <button onClick={() => loadData()} className="px-4 py-2 bg-red-600 text-white rounded">
                    {t("studentApplicationsList.retry")}
                </button>
            </div>
        );
    }

    if (!candidatures.length) {
        return (
            <div className="bg-white shadow rounded-lg p-6 text-center">
                <div
                    className="mx-auto mb-4 h-14 w-14 rounded-full bg-gray-100 flex items-center justify-center text-2xl">📄
                </div>
                <h3 className="text-lg font-medium text-gray-900 mb-2">{t("studentApplicationsList.noApplicationsTitle")}</h3>
                <p className="text-gray-600 mb-4">{t("studentApplicationsList.noApplicationsText")}</p>
            </div>
        );
    }

    return (
        <>
            <div className="bg-white shadow rounded-lg overflow-hidden">
                <div className="px-6 py-4 border-b border-gray-200">
                    <h3 className="text-lg font-medium text-gray-900">{t("studentApplicationsList.title")}</h3>
                    <p className="text-sm text-gray-600">
                        {t("studentApplicationsList.count", {count: candidatures.length})}
                    </p>
                </div>

                <div className="px-6 py-4 border-b border-gray-200 bg-gray-50">
                    <div className="flex items-center justify-between">
                        <label htmlFor="status-filter" className="text-sm font-medium text-gray-700">
                            {t("studentApplicationsList.filterBy")}
                        </label>
                        <div className="relative inline-block w-64">
                            <select
                                id="status-filter"
                                value={activeTab}
                                onChange={(e) => setActiveTab(e.target.value)}
                                className="block w-full pl-3 pr-10 py-2 text-base border border-gray-300 focus:outline-none focus:ring-indigo-500 focus:border-indigo-500 sm:text-sm rounded-lg bg-white shadow-sm appearance-none cursor-pointer"
                            >
                                {tabs.map(tab => (
                                    <option key={tab.key} value={tab.key}>
                                        {tab.label} ({tab.count})
                                    </option>
                                ))}
                            </select>
                            <div
                                className="pointer-events-none absolute inset-y-0 right-0 flex items-center px-2 text-gray-700">
                                <svg className="fill-current h-4 w-4" xmlns="http://www.w3.org/2000/svg"
                                     viewBox="0 0 20 20">
                                    <path
                                        d="M9.293 12.95l.707.707L15.657 8l-1.414-1.414L10 10.828 5.757 6.586 4.343 8z"/>
                                </svg>
                            </div>
                        </div>
                    </div>
                </div>

                {filteredCandidatures.length === 0 ? (
                    <div className="px-6 py-12 text-center">
                        <div
                            className="mx-auto h-12 w-12 rounded-full bg-gray-100 flex items-center justify-center mb-4">
                            <svg className="h-6 w-6 text-gray-400" fill="none" stroke="currentColor"
                                 viewBox="0 0 24 24">
                                <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2}
                                      d="M9 12h6m-6 4h6m2 5H7a2 2 0 01-2-2V5a2 2 0 012-2h5.586a1 1 0 01.707.293l5.414 5.414a1 1 0 01.293.707V19a2 2 0 01-2 2z"/>
                            </svg>
                        </div>
                        <h3 className="text-sm font-medium text-gray-900 mb-1">
                            {t("studentApplicationsList.noFilteredResults")}
                        </h3>
                        <p className="text-sm text-gray-500">
                            {t("studentApplicationsList.noFilteredResultsDesc")}
                        </p>
                    </div>
                ) : (
                    <>
                        <div className="hidden lg:block overflow-x-auto">
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
                                {filteredCandidatures.map(c => (
                                    <tr key={c.id} className="hover:bg-gray-50">
                                        <td className="px-6 py-4 text-sm font-medium text-gray-900">{c.internshipOffer?.description}</td>
                                        <td className="px-6 py-4 text-sm text-gray-900">{c.internshipOffer?.companyName}</td>
                                        <td className="px-6 py-4 text-sm text-gray-900 whitespace-nowrap">{formatDate(c.applicationDate)}</td>
                                        <td className="px-6 py-4 whitespace-nowrap">{getStatusBadge(c.status, c)}</td>
                                        <td className="px-6 py-4 whitespace-nowrap text-sm space-x-2">
                                            <button
                                                onClick={() => navigate(`/dashboard/student/offers/${c.internshipOffer?.id}`)}
                                                className="text-indigo-600 hover:text-indigo-900"
                                            >
                                                {t("studentApplicationsList.viewOffer")}
                                            </button>

                                            {canAcceptOrReject(c.status) && (
                                                <>
                                                    <button
                                                        onClick={() => handleAcceptCandidature(c.id)}
                                                        className="px-3 py-1 text-xs bg-green-100 text-green-700 rounded hover:bg-green-200 font-medium"
                                                    >
                                                        {t("studentApplicationsList.actions.accept")}
                                                    </button>
                                                    <button
                                                        onClick={() => openRejectModal(c)}
                                                        className="px-3 py-1 text-xs bg-red-100 text-red-700 rounded hover:bg-red-200 font-medium"
                                                    >
                                                        {t("studentApplicationsList.actions.reject")}
                                                    </button>
                                                </>
                                            )}
                                        </td>
                                    </tr>
                                ))}
                                </tbody>
                            </table>
                        </div>

                        <div className="lg:hidden divide-y divide-gray-200">
                            {filteredCandidatures.map(c => (
                                <div key={c.id} className="p-4 hover:bg-gray-50">
                                    <div className="flex items-start justify-between mb-3">
                                        <div className="flex-1 min-w-0">
                                            <h4 className="text-sm font-semibold text-gray-900 truncate">
                                                {c.internshipOffer?.description}
                                            </h4>
                                            <p className="text-xs text-gray-600 mt-1">
                                                {c.internshipOffer?.companyName}
                                            </p>
                                            <p className="text-xs text-gray-500 mt-1">
                                                {formatDate(c.applicationDate)}
                                            </p>
                                        </div>
                                    </div>

                                    <div className="flex items-center justify-center mb-3">
                                        {getStatusBadge(c.status, c)}
                                    </div>

                                    <div className="flex flex-col gap-2">
                                        <button
                                            onClick={() => navigate(`/dashboard/student/offers/${c.internshipOffer?.id}`)}
                                            className="w-full inline-flex justify-center px-3 py-2 text-sm text-indigo-600 bg-indigo-50 rounded-md hover:bg-indigo-100"
                                        >
                                            {t("studentApplicationsList.viewOffer")}
                                        </button>

                                        {canAcceptOrReject(c.status) && (
                                            <>
                                                <button
                                                    onClick={() => handleAcceptCandidature(c.id)}
                                                    className="w-full px-3 py-2 text-xs bg-green-100 text-green-700 rounded hover:bg-green-200 font-medium"
                                                >
                                                    {t("studentApplicationsList.actions.accept")}
                                                </button>
                                                <button
                                                    onClick={() => openRejectModal(c)}
                                                    className="w-full px-3 py-2 text-xs bg-red-100 text-red-700 rounded hover:bg-red-200 font-medium"
                                                >
                                                    {t("studentApplicationsList.actions.reject")}
                                                </button>
                                            </>
                                        )}
                                    </div>
                                </div>
                            ))}
                        </div>
                    </>
                )}
            </div>

            {showRejectModal && selectedCandidatureForReject && (
                <div className="fixed inset-0 bg-black bg-opacity-50 flex items-center justify-center z-50 p-4"
                     onClick={closeRejectModal}>
                    <div className="bg-white rounded-lg shadow-xl max-w-md w-full relative"
                         onClick={(e) => e.stopPropagation()}>
                        <div className="bg-red-50 rounded-t-lg px-6 py-4 border-b border-red-100">
                            <div className="flex items-center gap-3">
                                <div
                                    className="flex-shrink-0 w-10 h-10 bg-red-100 rounded-full flex items-center justify-center">
                                    <svg className="w-6 h-6 text-red-600" fill="none" stroke="currentColor"
                                         viewBox="0 0 24 24">
                                        <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2}
                                              d="M12 9v2m0 4h.01m-6.938 4h13.856c1.54 0 2.502-1.667 1.732-3L13.732 4c-.77-1.333-2.694-1.333-3.464 0L3.34 16c-.77 1.333.192 3 1.732 3z"/>
                                    </svg>
                                </div>
                                <div>
                                    <h3 className="text-lg font-semibold text-gray-900">
                                        {t("studentApplicationsList.rejectModal.title")}
                                    </h3>
                                </div>
                            </div>
                        </div>

                        <div className="px-6 py-4">
                            <p className="text-gray-700 mb-4">
                                {t("studentApplicationsList.rejectModal.message")}
                            </p>

                            <div className="bg-gray-50 rounded-lg p-4 mb-4">
                                <div className="space-y-2">
                                    <div className="flex items-start">
                                        <p className="text-xs text-gray-500 font-medium">{t("studentApplicationsList.table.offer")}</p>
                                        <p className="text-sm font-semibold text-gray-900">{selectedCandidatureForReject.internshipOffer?.description}</p>
                                    </div>
                                    <div className="flex items-start">
                                        <p className="text-xs text-gray-500 font-medium">{t("studentApplicationsList.table.company")}</p>
                                        <p className="text-sm font-semibold text-gray-900">{selectedCandidatureForReject.internshipOffer?.companyName}</p>
                                    </div>
                                </div>
                            </div>

                            <div className="flex justify-end space-x-2">
                                <button onClick={closeRejectModal}
                                        className="px-4 py-2 rounded bg-gray-200 hover:bg-gray-300">{t("studentApplicationsList.rejectModal.cancel")}</button>
                                <button onClick={confirmRejectCandidature}
                                        className="px-4 py-2 rounded bg-red-600 text-white hover:bg-red-700">{t("studentApplicationsList.actions.reject")}</button>
                            </div>
                        </div>
                    </div>
                </div>
            )}
            {showConvocationModal && (
                <div className='fixed inset-0 bg-black bg-opacity-50 flex items-center justify-center z-50'
                     onClick={closeConvocationModal}>
                    <div className='bg-white rounded-lg shadow-xl p-6 w-full max-w-md relative'
                         onClick={(e) => e.stopPropagation()}>
                        <button
                            type='button'
                            onClick={closeConvocationModal}
                            className='absolute top-4 right-4 text-gray-400 hover:text-gray-600 transition-colors'
                        >
                            <svg className='w-6 h-6' fill='none' stroke='currentColor' viewBox='0 0 24 24'>
                                <path strokeLinecap='round' strokeLinejoin='round' strokeWidth={2}
                                      d='M6 18L18 6M6 6l12 12'/>
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
                                        <svg className='w-5 h-5 text-gray-400 mt-0.5 mr-3 flex-shrink-0' fill='none'
                                             stroke='currentColor' viewBox='0 0 24 24'>
                                            <path strokeLinecap='round' strokeLinejoin='round' strokeWidth={2}
                                                  d='M8 7V3m8 4V3m-9 8h10M5 21h14a2 2 0 002-2V7a2 2 0 00-2-2H5a2 2 0 00-2 2v12a2 2 0 002 2z'/>
                                        </svg>
                                        <div className='flex-1'>
                                            <p className='text-xs text-gray-500 uppercase font-medium'>{t('studentApplicationsList.convocationModal.date')}</p>
                                            <p className='text-sm font-semibold text-gray-900'>{formatDateTime(selectedConvocation.convocationDate)}</p>
                                        </div>
                                    </div>

                                    <div className='flex items-start'>
                                        <svg className='w-5 h-5 text-gray-400 mt-0.5 mr-3 flex-shrink-0' fill='none'
                                             stroke='currentColor' viewBox='0 0 24 24'>
                                            <path strokeLinecap='round' strokeLinejoin='round' strokeWidth={2}
                                                  d='M17.657 16.657L13.414 20.9a1.998 1.998 0 01-2.827 0l-4.244-4.243a8 8 0 1111.314 0z'/>
                                            <path strokeLinecap='round' strokeLinejoin='round' strokeWidth={2}
                                                  d='M15 11a3 3 0 11-6 0 3 3 0 016 0z'/>
                                        </svg>
                                        <div className='flex-1'>
                                            <p className='text-xs text-gray-500 uppercase font-medium'>{t('studentApplicationsList.convocationModal.location')}</p>
                                            <p className='text-sm font-semibold text-gray-900'>{selectedConvocation.location}</p>
                                        </div>
                                    </div>

                                    {selectedConvocation.message && (
                                        <div className='flex items-start'>
                                            <svg className='w-5 h-5 text-gray-400 mt-0.5 mr-3 flex-shrink-0' fill='none'
                                                 stroke='currentColor' viewBox='0 0 24 24'>
                                                <path strokeLinecap='round' strokeLinejoin='round' strokeWidth={2}
                                                      d='M7 8h10M7 12h4m1 8l-4-4H5a2 2 0 01-2-2V6a2 2 0 012-2h14a2 2 0 012 2v8a2 2 0 01-2 2h-3l-4 4z'/>
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

            {toast.show && (
                <div
                    className={`fixed bottom-6 right-6 px-6 py-4 rounded-lg shadow-lg z-50 flex items-center gap-3 transition-all duration-300 ${toast.type === 'success' ? 'bg-green-500 text-white' : 'bg-red-500 text-white'}`}>
                    {toast.type === 'success' ? (
                        <svg className="w-6 h-6" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                            <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M5 13l4 4L19 7"/>
                        </svg>
                    ) : (
                        <svg className="w-6 h-6" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                            <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2}
                                  d="M6 18L18 6M6 6l12 12"/>
                        </svg>
                    )}
                    <span className="font-medium">{toast.message}</span>
                    <button
                        onClick={closeToast}
                        className={`ml-2 rounded-full p-1 transition-colors ${toast.type === 'success' ? 'bg-green-600 hover:bg-green-700' : 'bg-red-600 hover:bg-red-700'}`}
                        aria-label="Close"
                    >
                        <svg className="w-4 h-4 text-white" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                            <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2}
                                  d="M6 18L18 6M6 6l12 12"/>
                        </svg>
                    </button>
                </div>
            )}
        </>
    );
}
