import React, { useEffect, useState, useCallback } from 'react';
import { getOfferCandidatures, downloadCandidateCv, createConvocation, rejectCandidature } from '../api/apiEmployeur';
import { useParams, Link } from 'react-router-dom';
import { useTranslation } from 'react-i18next';

export default function OfferCandidaturesList() {
    const { offerId } = useParams();
    const { t } = useTranslation();
    const [candidatures, setCandidatures] = useState([]);
    const [loading, setLoading] = useState(true);
    const [error, setError] = useState(null);
    const [showConvocationModal, setShowConvocationModal] = useState(false);
    const [selectedCandidature, setSelectedCandidature] = useState(null);
    const [convocationForm, setConvocationForm] = useState({
        convocationDate: '',
        location: '',
        message: ''
    });

    const load = useCallback(async () => {
        setLoading(true); setError(null);
        try {
            const data = await getOfferCandidatures(offerId);
            setCandidatures(Array.isArray(data) ? data : []);
        } catch (e) {
            setError(e?.response?.data || 'Error');
        } finally { setLoading(false); }
    }, [offerId]);

    useEffect(() => { load(); }, [load]);

    const downloadCvHandler = async (candidature) => {
        try {
            const baseName = [candidature.studentFirstName, candidature.studentLastName].filter(Boolean).join(' ') || 'etudiant';
            const blob = await downloadCandidateCv(candidature.id);
            const url = window.URL.createObjectURL(blob);
            const a = document.createElement('a');
            a.href = url;
            a.download = `CV-${baseName}.pdf`;
            document.body.appendChild(a); a.click();
            document.body.removeChild(a); window.URL.revokeObjectURL(url);
        } catch (e) { alert(t('employerCandidatures.downloadError')); }
    };

    const openConvocationModal = (candidature) => {
        setSelectedCandidature(candidature);
        setConvocationForm({
            convocationDate: '',
            location: '',
            message: ''
        });
        setShowConvocationModal(true);
    };

    const closeConvocationModal = () => {
        setShowConvocationModal(false);
        setSelectedCandidature(null);
        setConvocationForm({ convocationDate: '', location: '', message: '' });
    };

    const handleConvocationSubmit = async (e) => {
        e.preventDefault();
        if (!selectedCandidature) return;

        try {
            await createConvocation(selectedCandidature.id, convocationForm);
            alert(t('employerCandidatures.convocationSuccess'));
            closeConvocationModal();
            load();
        } catch (error) {
            alert(error?.response?.data || t('employerCandidatures.convocationError'));
        }
    };

    const handleReject = async (candidature) => {
        const displayName = [candidature.studentFirstName, candidature.studentLastName].filter(Boolean).join(' ') || 'cet étudiant';
        if (!window.confirm(t('employerCandidatures.confirmReject', { name: displayName }))) {
            return;
        }

        try {
            await rejectCandidature(candidature.id);
            alert(t('employerCandidatures.rejectSuccess'));
            load(); // Recharger les candidatures
        } catch (error) {
            alert(error?.response?.data || t('employerCandidatures.rejectError'));
        }
    };

    const renderStatus = (status) => (
        <span className='text-xs font-medium text-gray-700'>{t(`employerCandidatures.status.${status}`)}</span>
    );

    const canConvoke = (status) => status === 'EN_ATTENTE' || status === 'PENDING';
    const canReject = (status) => status === 'EN_ATTENTE' || status === 'PENDING' || status === 'CONVOQUE';

    if (loading) return <div className='bg-white p-6 shadow rounded text-center'>{t('internshipOffersList.loading')}</div>;
    if (error) return <div className='bg-white p-6 shadow rounded text-center text-red-600'>
        <p className='mb-4'>{t('internshipOffersList.errorTitle')}: {error}</p>
        <button onClick={load} className='px-4 py-2 bg-red-600 text-white rounded'>{t('internshipOffersList.retry')}</button>
    </div>;
    if (candidatures.length === 0) return <div className='bg-white p-6 shadow rounded text-center'>
        <h3 className='text-lg font-semibold mb-2'>{t('employerCandidatures.emptyTitle')}</h3>
        <p className='text-gray-600 mb-4'>{t('employerCandidatures.emptyDescription')}</p>
        <div className='flex gap-4 justify-center'>
            <Link to='/dashboard/employeur' className='text-indigo-600 hover:text-indigo-800 text-sm'>{t('employerCandidatures.backToOffers')}</Link>
            <Link to='/dashboard/employeur/candidatures' className='text-sm text-gray-600 hover:text-gray-800'>{t('employerCandidatures.navLink')}</Link>
        </div>
    </div>;

    return (
        <>
            <div className='bg-white shadow rounded-lg overflow-hidden'>
                <div className='px-6 py-4 border-b border-gray-200 flex flex-col sm:flex-row sm:items-center sm:justify-between gap-2'>
                    <div>
                        <h2 className='text-lg font-semibold'>{t('employerCandidatures.titleOffer')} #{offerId}</h2>
                        <p className='text-sm text-gray-500'>{t('employerCandidatures.candidaturesCount', { count: candidatures.length })}</p>
                    </div>
                    <div className='flex gap-4'>
                        <Link to='/dashboard/employeur' className='text-sm text-indigo-600 hover:text-indigo-800'>{t('employerCandidatures.backToOffers')}</Link>
                        <Link to='/dashboard/employeur/candidatures' className='text-sm text-gray-600 hover:text-gray-800'>{t('employerCandidatures.navLink')}</Link>
                    </div>
                </div>
                <div className='overflow-x-auto'>
                    <table className='min-w-full divide-y divide-gray-200'>
                        <thead className='bg-gray-50'>
                        <tr>
                            <th className='px-4 py-2 text-left text-xs font-medium text-gray-500 uppercase tracking-wider'>{t('employerCandidatures.table.student')}</th>
                            <th className='px-4 py-2 text-left text-xs font-medium text-gray-500 uppercase tracking-wider'>{t('employerCandidatures.table.program')}</th>
                            <th className='px-4 py-2 text-left text-xs font-medium text-gray-500 uppercase tracking-wider'>{t('employerCandidatures.table.date')}</th>
                            <th className='px-4 py-2 text-left text-xs font-medium text-gray-500 uppercase tracking-wider'>{t('employerCandidatures.table.status')}</th>
                            <th className='px-4 py-2 text-left text-xs font-medium text-gray-500 uppercase tracking-wider'>{t('employerCandidatures.table.actions')}</th>
                        </tr>
                        </thead>
                        <tbody className='bg-white divide-y divide-gray-100'>
                        {candidatures.map(c => {
                            const date = c.applicationDate ? new Date(c.applicationDate).toLocaleDateString() : '-';
                            const displayName = [c.studentFirstName, c.studentLastName].filter(Boolean).join(' ') || '-';
                            return (
                                <tr key={c.id} className='hover:bg-gray-50'>
                                    <td className='px-4 py-2 text-sm'>{displayName}</td>
                                    <td className='px-4 py-2 text-sm text-gray-600'>{c.studentProgram || '-'}</td>
                                    <td className='px-4 py-2 text-sm text-gray-600'>{date}</td>
                                    <td className='px-4 py-2 text-sm'>{renderStatus(c.status)}</td>
                                    <td className='px-4 py-2 text-sm space-x-2'>
                                        <button onClick={() => downloadCvHandler(c)} className='px-2 py-1 text-xs bg-indigo-100 text-indigo-700 rounded hover:bg-indigo-200'>
                                            {t('employerCandidatures.actions.downloadCv')}
                                        </button>
                                        {canConvoke(c.status) && (
                                            <button onClick={() => openConvocationModal(c)} className='px-2 py-1 text-xs bg-green-100 text-green-700 rounded hover:bg-green-200'>
                                                {t('employerCandidatures.actions.retain')}
                                            </button>
                                        )}
                                        {canReject(c.status) && (
                                            <button onClick={() => handleReject(c)} className='px-2 py-1 text-xs bg-red-100 text-red-700 rounded hover:bg-red-200'>
                                                {t('employerCandidatures.actions.reject')}
                                            </button>
                                        )}
                                    </td>
                                </tr>
                            );
                        })}
                        </tbody>
                    </table>
                </div>
            </div>

            {/* Modal de convocation */}
            {showConvocationModal && selectedCandidature && (
                <div className='fixed inset-0 bg-black bg-opacity-50 flex items-center justify-center z-50'>
                    <div className='bg-white rounded-lg shadow-xl p-6 w-full max-w-md'>
                        <h3 className='text-lg font-semibold mb-4'>
                            {t('employerCandidatures.convocationModal.title')} - {[selectedCandidature.studentFirstName, selectedCandidature.studentLastName].filter(Boolean).join(' ')}
                        </h3>
                        <form onSubmit={handleConvocationSubmit}>
                            <div className='mb-4'>
                                <label className='block text-sm font-medium text-gray-700 mb-1'>
                                    {t('employerCandidatures.convocationModal.date')} *
                                </label>
                                <input
                                    type='datetime-local'
                                    required
                                    value={convocationForm.convocationDate}
                                    onChange={(e) => setConvocationForm({ ...convocationForm, convocationDate: e.target.value })}
                                    className='w-full px-3 py-2 border border-gray-300 rounded focus:outline-none focus:ring-2 focus:ring-indigo-500'
                                />
                            </div>
                            <div className='mb-4'>
                                <label className='block text-sm font-medium text-gray-700 mb-1'>
                                    {t('employerCandidatures.convocationModal.location')} *
                                </label>
                                <input
                                    type='text'
                                    required
                                    value={convocationForm.location}
                                    onChange={(e) => setConvocationForm({ ...convocationForm, location: e.target.value })}
                                    className='w-full px-3 py-2 border border-gray-300 rounded focus:outline-none focus:ring-2 focus:ring-indigo-500'
                                />
                            </div>
                            <div className='mb-4'>
                                <label className='block text-sm font-medium text-gray-700 mb-1'>
                                    {t('employerCandidatures.convocationModal.message')}
                                </label>
                                <textarea
                                    value={convocationForm.message}
                                    onChange={(e) => setConvocationForm({ ...convocationForm, message: e.target.value })}
                                    rows='4'
                                    className='w-full px-3 py-2 border border-gray-300 rounded focus:outline-none focus:ring-2 focus:ring-indigo-500'
                                />
                            </div>
                            <div className='flex gap-3 justify-end'>
                                <button
                                    type='button'
                                    onClick={closeConvocationModal}
                                    className='px-4 py-2 border border-gray-300 rounded text-gray-700 hover:bg-gray-50'
                                >
                                    {t('employerCandidatures.convocationModal.cancel')}
                                </button>
                                <button
                                    type='submit'
                                    className='px-4 py-2 bg-indigo-600 text-white rounded hover:bg-indigo-700'
                                >
                                    {t('employerCandidatures.convocationModal.submit')}
                                </button>
                            </div>
                        </form>
                    </div>
                </div>
            )}
        </>
    );
}