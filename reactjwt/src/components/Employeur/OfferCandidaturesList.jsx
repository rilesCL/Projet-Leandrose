import React, {useCallback, useEffect, useState} from 'react';
import {
    acceptCandidature,
    createConvocation,
    getOfferCandidatures,
    previewCandidateCv,
    rejectCandidature
} from '../../api/apiEmployeur.jsx';
import {Link, useParams} from 'react-router-dom';
import {useTranslation} from 'react-i18next';
import PdfViewer from '../PdfViewer.jsx';

export default function OfferCandidaturesList() {
    const {offerId} = useParams();
    const {t} = useTranslation();
    const [candidatures, setCandidatures] = useState([]);
    const [loading, setLoading] = useState(true);
    const [error, setError] = useState(null);
    const [showConvocationModal, setShowConvocationModal] = useState(false);
    const [selectedCandidature, setSelectedCandidature] = useState(null);
    const [convocationForm, setConvocationForm] = useState({convocationDate: '', location: '', message: ''});
    const [toast, setToast] = useState({show: false, message: '', type: 'success', persistent: false});
    const [selectedPdfUrl, setSelectedPdfUrl] = useState(null);

    const showToast = (message, type = 'success', persistent = false) => {
        setToast({show: true, message, type, persistent});
    };

    const closeToast = () => {
        setToast({show: false, message: '', type: 'success', persistent: false});
    };

    const load = useCallback(async () => {
        setLoading(true);
        setError(null);
        try {
            const data = await getOfferCandidatures(offerId);
            setCandidatures(Array.isArray(data) ? data : []);
        } catch (e) {
            setError(e?.response?.data || 'Error');
        } finally {
            setLoading(false);
        }
    }, [offerId]);

    useEffect(() => {
        load();
    }, [load]);

    const previewCvHandler = async (candidature) => {
        try {
            const blob = await previewCandidateCv(candidature.id);
            const url = URL.createObjectURL(blob);
            setSelectedPdfUrl(url);
        } catch (e) {
            showToast(t('employerCandidatures.downloadError'), 'error');
        }
    };

    const openConvocationModal = (candidature) => {
        setSelectedCandidature(candidature);
        setConvocationForm({convocationDate: '', location: '', message: ''});
        setShowConvocationModal(true);
    };

    const closeConvocationModal = () => {
        setShowConvocationModal(false);
        setSelectedCandidature(null);
        setConvocationForm({convocationDate: '', location: '', message: ''});
    };

    const handleConvocationSubmit = async (e) => {
        e.preventDefault();
        if (!selectedCandidature) return;
        try {
            await createConvocation(selectedCandidature.id, convocationForm);
            showToast(t('employerCandidatures.convocationSuccess'), 'success', true);
            closeConvocationModal();
            setCandidatures(prev =>
                prev.map(c => c.id === selectedCandidature.id ? {...c, status: 'CONVENED'} : c)
            );
        } catch (error) {
            showToast(error?.response?.data || t('employerCandidatures.convocationError'), 'error');
        }
    };

    const handleReject = async (candidature) => {
        try {
            await rejectCandidature(candidature.id);
            showToast(t('employerCandidatures.rejectSuccess'), 'error', true);
            load();
        } catch (error) {
            showToast(error?.response?.data || t('employerCandidatures.rejectError'), 'error');
        }
    };

    const handleAccept = async (candidature) => {
        try {
            await acceptCandidature(candidature.id);
            showToast(t('employerCandidatures.acceptSuccess'), 'success', true);
            load();
        } catch (error) {
            showToast(error?.response?.data || t('employerCandidatures.acceptError'), 'error');
        }
    };

    const renderStatus = (status) => <span
        className="text-xs font-medium text-gray-700">{t(`studentApplicationsList.status.${status}`)}</span>;
    const canRetain = (status) => status === 'PENDING';
    const canAcceptOrReject = (status) => status === 'CONVENED';

    if (loading) return <div
        className="bg-white p-6 shadow rounded text-center">{t('internshipOffersList.loading')}</div>;
    if (error) return (
        <div className="bg-white p-6 shadow rounded text-center text-red-600">
            <p className="mb-4">{t('internshipOffersList.errorTitle')}: {error}</p>
            <button onClick={load}
                    className="px-4 py-2 bg-red-600 text-white rounded">{t('internshipOffersList.retry')}</button>
        </div>
    );
    if (!candidatures.length) return (
        <div className="bg-white p-6 shadow rounded text-center">
            <h3 className="text-lg font-semibold mb-2">{t('employerCandidatures.emptyTitle')}</h3>
            <p className="text-gray-600 mb-4">{t('employerCandidatures.emptyDescription')}</p>
            <div className="flex gap-4 justify-center">
                <Link to="/dashboard/employeur"
                      className="text-indigo-600 hover:text-indigo-800 text-sm">{t('employerCandidatures.backToOffers')}</Link>
            </div>
        </div>
    );

    return (
        <>
            <div className="bg-white shadow rounded-lg overflow-hidden">
                <div
                    className="px-6 py-4 border-b border-gray-200 flex flex-col sm:flex-row sm:items-center sm:justify-between gap-2">
                    <div>
                        <h2 className="text-lg font-semibold">{t('employerCandidatures.titleOffer')} #{offerId}</h2>
                        <p className="text-sm text-gray-500">{t('employerCandidatures.candidaturesCount', {count: candidatures.length})}</p>
                    </div>
                    <div className="flex gap-4">
                        <Link to="/dashboard/employeur"
                              className="text-sm text-indigo-600 hover:text-indigo-800">{t('employerCandidatures.backToOffers')}</Link>
                    </div>
                </div>
                <div className="overflow-x-auto">
                    <table className="min-w-full divide-y divide-gray-200">
                        <thead className="bg-gray-50">
                        <tr>
                            <th className="px-4 py-2 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">{t('employerCandidatures.table.student')}</th>
                            <th className="px-4 py-2 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">{t('employerCandidatures.table.program')}</th>
                            <th className="px-4 py-2 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">{t('employerCandidatures.table.date')}</th>
                            <th className="px-4 py-2 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">{t('employerCandidatures.table.status')}</th>
                            <th className="px-4 py-2 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">{t('employerCandidatures.table.actions')}</th>
                        </tr>
                        </thead>
                        <tbody className="bg-white divide-y divide-gray-100">
                        {candidatures.map(c => {
                            const date = c.applicationDate ? new Date(c.applicationDate).toLocaleDateString() : '-';
                            const displayName = [c.studentFirstName, c.studentLastName].filter(Boolean).join(' ') || '-';
                            const programDisplay = c.studentProgram ? t(c.studentProgram) : '-';

                            return (
                                <tr key={c.id} className="hover:bg-gray-50">
                                    <td className="px-4 py-2 text-sm">{displayName}</td>
                                    <td className="px-4 py-2 text-sm text-gray-600">{programDisplay}</td>
                                    <td className="px-4 py-2 text-sm text-gray-600">{date}</td>
                                    <td className="px-4 py-2 text-sm">{renderStatus(c.status)}</td>
                                    <td className="px-4 py-2 text-sm space-x-2">
                                        <button
                                            onClick={() => previewCvHandler(c)}
                                            className="px-2 py-1 text-xs bg-indigo-100 text-indigo-700 rounded hover:bg-indigo-200"
                                        >
                                            {t('previewPdf.preview')}
                                        </button>

                                        {canRetain(c.status) && (
                                            <button
                                                onClick={() => openConvocationModal(c)}
                                                className="px-2 py-1 text-xs bg-green-100 text-green-700 rounded hover:bg-green-200"
                                            >
                                                {t('employerCandidatures.actions.retain')}
                                            </button>
                                        )}

                                        {canAcceptOrReject(c.status) && (
                                            <>
                                                <button onClick={() => handleAccept(c)}
                                                        className="px-2 py-1 text-xs bg-blue-100 text-blue-700 rounded hover:bg-blue-200">{t('employerCandidatures.actions.accept')}</button>
                                                <button onClick={() => handleReject(c)}
                                                        className="px-2 py-1 text-xs bg-red-100 text-red-700 rounded hover:bg-red-200">{t('employerCandidatures.actions.reject')}</button>
                                            </>
                                        )}
                                    </td>
                                </tr>
                            );
                        })}
                        </tbody>
                    </table>
                </div>
            </div>

            {showConvocationModal && selectedCandidature && (
                <div className="fixed inset-0 bg-black bg-opacity-50 flex items-center justify-center z-50"
                     onClick={closeConvocationModal}>
                    <div className="bg-white rounded-lg shadow-xl p-6 w-full max-w-md relative"
                         onClick={e => e.stopPropagation()}>
                        <button type="button" onClick={closeConvocationModal}
                                className="absolute top-4 right-4 text-gray-400 hover:text-gray-600 transition-colors"
                                aria-label="Close">
                            <svg className="w-6 h-6" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                                <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2}
                                      d="M6 18L18 6M6 6l12 12"/>
                            </svg>
                        </button>
                        <h3 className="text-lg font-semibold mb-4 pr-8">
                            {t('employerCandidatures.convocationModal.title')} - {[selectedCandidature.studentFirstName, selectedCandidature.studentLastName].filter(Boolean).join(' ')}
                        </h3>
                        <form onSubmit={handleConvocationSubmit}>
                            <div className="mb-4">
                                <label
                                    className="block text-sm font-medium text-gray-700 mb-1">{t('employerCandidatures.convocationModal.date')} *</label>
                                <input type="datetime-local" required value={convocationForm.convocationDate}
                                       onChange={e => setConvocationForm({
                                           ...convocationForm,
                                           convocationDate: e.target.value
                                       })}
                                       className="w-full px-3 py-2 border border-gray-300 rounded focus:outline-none focus:ring-2 focus:ring-indigo-500"/>
                            </div>
                            <div className="mb-4">
                                <label
                                    className="block text-sm font-medium text-gray-700 mb-1">{t('employerCandidatures.convocationModal.location')} *</label>
                                <input type="text" required value={convocationForm.location}
                                       onChange={e => setConvocationForm({
                                           ...convocationForm,
                                           location: e.target.value
                                       })}
                                       className="w-full px-3 py-2 border border-gray-300 rounded focus:outline-none focus:ring-2 focus:ring-indigo-500"/>
                            </div>
                            <div className="mb-4">
                                <label
                                    className="block text-sm font-medium text-gray-700 mb-1">{t('employerCandidatures.convocationModal.message')}</label>
                                <textarea
                                    value={convocationForm.message}
                                    onChange={e => setConvocationForm({...convocationForm, message: e.target.value})}
                                    maxLength={100}
                                    rows="4"
                                    className="w-full px-3 py-2 border border-gray-300 rounded focus:outline-none focus:ring-2 focus:ring-indigo-500"
                                />
                                <p className="text-xs text-gray-500 mt-1 text-right">
                                    {convocationForm.message.length}/100
                                </p>
                            </div>
                            <div className="flex gap-3 justify-end">
                                <button type="button" onClick={closeConvocationModal}
                                        className="px-4 py-2 border border-gray-300 rounded text-gray-700 hover:bg-gray-50">{t('employerCandidatures.convocationModal.cancel')}</button>
                                <button type="submit"
                                        className="px-4 py-2 bg-indigo-600 text-white rounded hover:bg-indigo-700">{t('employerCandidatures.convocationModal.submit')}</button>
                            </div>
                        </form>
                    </div>
                </div>
            )}

            {selectedPdfUrl && <PdfViewer file={selectedPdfUrl} onClose={() => {
                URL.revokeObjectURL(selectedPdfUrl);
                setSelectedPdfUrl(null);
            }}/>}

            {toast.show && (
                <div
                    className={`fixed bottom-6 right-6 px-6 py-4 rounded-lg shadow-lg z-50 flex items-center gap-3 transition-all duration-300 ${toast.type === 'success' ? 'bg-green-500 text-white' : 'bg-red-500 text-white'}`}>
                    {toast.type === 'success' ? (
                        <svg className="w-6 h-6" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                            <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M5 13l4 4L19 7"/>
                        </svg>
                    ) : (
                        <svg className="w-6 h-6" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                            <circle cx="12" cy="12" r="10" strokeWidth={2}/>
                            <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M12 8v4m0 4h.01"/>
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