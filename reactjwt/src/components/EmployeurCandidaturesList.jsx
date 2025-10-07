import React, { useEffect, useState, useCallback } from 'react';
import { getEmployerCandidatures, downloadCandidateCv } from '../api/apiEmployeur';
import { useTranslation } from 'react-i18next';
import { Link } from 'react-router-dom';

export default function EmployeurCandidaturesList() {
    const { t } = useTranslation();
    const [candidatures, setCandidatures] = useState([]);
    const [loading, setLoading] = useState(true);
    const [error, setError] = useState(null);

    const load = useCallback(async () => {
        setLoading(true); setError(null);
        try {
            const data = await getEmployerCandidatures();
            setCandidatures(Array.isArray(data) ? data : []);
        } catch (e) {
            setError(e?.response?.data || 'Error');
        } finally { setLoading(false); }
    }, []);

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
        } catch (e) {
            alert(t('employerCandidatures.downloadError'));
        }
    };

    const renderStatus = (status) => (
        <span className='text-xs font-medium text-gray-700'>{t(`employerCandidatures.status.${status}`)}</span>
    );

    if (loading) {
        return <div className='bg-white p-6 shadow rounded text-center'>{t('internshipOffersList.loading')}</div>;
    }
    if (error) {
        return <div className='bg-white p-6 shadow rounded text-center text-red-600'>
            <p className='mb-4'>{t('internshipOffersList.errorTitle')}: {error}</p>
            <button onClick={load} className='px-4 py-2 bg-red-600 text-white rounded'>{t('internshipOffersList.retry')}</button>
        </div>;
    }
    if (candidatures.length === 0) {
        return <div className='bg-white p-6 shadow rounded text-center'>
            <h3 className='text-lg font-semibold mb-2'>{t('employerCandidatures.emptyTitle')}</h3>
            <p className='text-gray-600 mb-4'>{t('employerCandidatures.emptyDescription')}</p>
            <Link to='/dashboard/employeur' className='text-indigo-600 hover:text-indigo-800 text-sm'>
                {t('employerCandidatures.backToOffers')}
            </Link>
        </div>;
    }

    return (
        <div className='bg-white shadow rounded-lg overflow-hidden'>
            <div className='px-6 py-4 border-b border-gray-200 flex flex-col sm:flex-row sm:items-center sm:justify-between gap-2'>
                <div>
                    <h2 className='text-lg font-semibold'>{t('employerCandidatures.titleAll')}</h2>
                    <p className='text-sm text-gray-500'>{candidatures.length} {t('internshipOffersList.offers')}</p>
                </div>
                <Link to='/dashboard/employeur' className='text-sm text-indigo-600 hover:text-indigo-800'>
                    {t('employerCandidatures.backToOffers')}
                </Link>
            </div>
            <div className='overflow-x-auto'>
                <table className='min-w-full divide-y divide-gray-200'>
                    <thead className='bg-gray-50'>
                        <tr>
                            <th className='px-4 py-2 text-left text-xs font-medium text-gray-500 uppercase tracking-wider'>{t('employerCandidatures.table.student')}</th>
                            <th className='px-4 py-2 text-left text-xs font-medium text-gray-500 uppercase tracking-wider'>{t('employerCandidatures.table.program')}</th>
                            <th className='px-4 py-2 text-left text-xs font-medium text-gray-500 uppercase tracking-wider'>{t('employerCandidatures.table.offer')}</th>
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
                                    <td className='px-4 py-2 text-sm'>
                                        {displayName}
                                    </td>
                                    <td className='px-4 py-2 text-sm text-gray-600'>{c.studentProgram || '-'}</td>
                                    <td className='px-4 py-2 text-sm text-gray-600'>{c.offerDescription || '-'}</td>
                                    <td className='px-4 py-2 text-sm text-gray-600'>{date}</td>
                                    <td className='px-4 py-2 text-sm'>{renderStatus(c.status)}</td>
                                    <td className='px-4 py-2 text-sm space-x-2'>
                                        <button onClick={() => downloadCvHandler(c)} className='px-2 py-1 text-xs bg-indigo-100 text-indigo-700 rounded hover:bg-indigo-200'>
                                            {t('employerCandidatures.actions.downloadCv')}
                                        </button>
                                    </td>
                                </tr>
                            );
                        })}
                    </tbody>
                </table>
            </div>
        </div>
    );
}
