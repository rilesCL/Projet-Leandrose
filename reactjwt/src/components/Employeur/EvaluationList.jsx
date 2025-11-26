import React, {useEffect, useState} from 'react';
import {Link} from 'react-router-dom';
import {useTranslation} from 'react-i18next';
import {
    checkExistingEvaluation,
    checkTeacherAssigned,
    getEligibleEvaluations,
    previewEvaluationPdf
} from '../../api/apiEmployeur';
import PdfViewer from '../PdfViewer.jsx';

export default function EvaluationsList({selectedTerm}) {
    const [eligibleAgreements, setEligibleAgreements] = useState([]);
    const [filteredAgreements, setFilteredAgreements] = useState([]);
    const [loading, setLoading] = useState(true);
    const [showPdfViewer, setShowPdfViewer] = useState(false);
    const [currentPdfBlob, setCurrentPdfBlob] = useState(null);
    const [evaluationStatus, setEvaluationStatus] = useState({});
    const [teacherAssignmentStatus, setTeacherAssignmentStatus] = useState({})
    const {t} = useTranslation()

    useEffect(() => {
        if (!selectedTerm || !eligibleAgreements.length) {
            setFilteredAgreements(eligibleAgreements);
            return;
        }

        const filtered = eligibleAgreements.filter(agreement => {
            if (!agreement.internshipTerm) return false;

            if (typeof agreement.internshipTerm === 'object') {
                return agreement.internshipTerm.season === selectedTerm.season &&
                    agreement.internshipTerm.year === selectedTerm.year;
            }

            if (typeof agreement.internshipTerm === 'string') {
                const termParts = agreement.internshipTerm.trim().split(/\s+/);
                const agreementSeason = termParts[0]?.toUpperCase();
                const agreementYear = parseInt(termParts[1]);
                return agreementSeason === selectedTerm.season && agreementYear === selectedTerm.year;
            }

            return false;
        });

        setFilteredAgreements(filtered);
    }, [selectedTerm, eligibleAgreements]);

    useEffect(() => {
        const fetchEligibleAgreements = async () => {
            try {
                const agreements = await getEligibleEvaluations();
                setEligibleAgreements(agreements);

                const statusMap = {};
                const teacherAssignmentMap = {}
                for (const agreement of agreements) {
                    const key = `${agreement.studentId}-${agreement.offerId}`
                    try {
                        const existingCheck = await checkExistingEvaluation(agreement.studentId, agreement.offerId);
                        statusMap[key] = existingCheck;
                        const teacherCheck = await checkTeacherAssigned(agreement.studentId, agreement.offerId)
                        teacherAssignmentMap[key] = teacherCheck.teacherAssigned;
                    } catch (error) {
                        console.error(t("evaluationList.errors.checking_evaluation") + `${agreement.studentId}`, error)
                        statusMap[key] = {exists: false};
                        teacherAssignmentMap[key] = false;
                    }
                }
                setEvaluationStatus(statusMap);
                setTeacherAssignmentStatus(teacherAssignmentMap)

            } catch (error) {
                console.error(t("evaluationList.errors.fetching_evaluations"), error);
            } finally {
                setLoading(false);
            }
        };
        fetchEligibleAgreements();
    }, []);

    const handleViewPdf = async (studentId, offerId) => {
        try {
            const statusKey = `${studentId}-${offerId}`;
            const status = evaluationStatus[statusKey];

            if (status && status.exists && status.evaluation) {
                const pdfBlob = await previewEvaluationPdf(status.evaluation.id);
                setCurrentPdfBlob(pdfBlob);
                setShowPdfViewer(true);
            }
        } catch (error) {
            console.error(t("evaluationList.errors.pdf_loading"), error);
        }
    };

    const handleClosePdfViewer = () => {
        setShowPdfViewer(false);
        setCurrentPdfBlob(null);
    };

    if (loading) return (
        <div className="flex justify-center items-center min-h-64">
            <div className="animate-spin rounded-full h-8 w-8 border-b-2 border-blue-600"></div>
        </div>
    );

    return (
        <div className="w-full max-w-7xl mx-auto px-4 sm:px-6 lg:px-8">
            <div className="mb-8">
                <h1 className="text-3xl font-bold text-gray-900">{t("evaluationList.title")}</h1>
                <p className="text-gray-600 mt-2">
                    {t("evaluationList.subtitle")}
                </p>
            </div>

            {eligibleAgreements.length === 0 ? (
                <div className="bg-white rounded-lg shadow p-8 text-center">
                    <p className="text-gray-500 text-lg">{t("evaluationList.no_evaluations")}</p>
                </div>
            ) : filteredAgreements.length === 0 ? (
                <div className="bg-white rounded-lg shadow p-8 text-center">
                    <p className="text-gray-500 text-lg">
                        {t("evaluationList.no_evaluations_for_term", {
                            term: selectedTerm ? `${t(`terms.${selectedTerm.season}`)} ${selectedTerm.year}` : ''
                        })}
                    </p>
                </div>
            ) : (
                <div className="space-y-5">
                    {filteredAgreements.map(agreement => {
                        const statusKey = `${agreement.studentId}-${agreement.offerId}`;
                        const status = evaluationStatus[statusKey];
                        const hasEvaluation = status?.exists;
                        const evaluation = status?.evaluation;
                        const isTeacherAssigned = teacherAssignmentStatus[statusKey]
                        return (

                            <div
                                key={agreement.id}
                                className="bg-white border border-gray-200 rounded-xl p-6 shadow-sm hover:shadow-md transition-shadow"
                            >
                                <div className="flex flex-col md:flex-row md:items-start md:justify-between gap-6">
                                    <div className="flex-1 space-y-3">
                                        <div className="flex items-center gap-3 flex-wrap">
                                            <h3 className="text-xl font-semibold text-gray-900">
                                                {agreement.studentFirstName} {agreement.studentLastName}
                                            </h3>
                                            <span
                                                className="text-xs font-medium uppercase tracking-wide bg-slate-100 text-slate-600 px-2.5 py-1 rounded-full">
                                                {t(agreement.studentProgram)}
                                            </span>
                                        </div>
                                        <p className="text-gray-700 leading-relaxed">
                                            {agreement.internshipDescription}
                                        </p>
                                        <div className="flex flex-wrap items-center gap-4 text-sm text-gray-500">
                                            <span className="flex items-center gap-1">
                                                <span
                                                    className="font-medium text-gray-700">{t("ententeStage.student")}:</span>
                                                {agreement.companyName}
                                            </span>
                                        </div>

                                        {hasEvaluation && evaluation && (
                                            <div className="mt-2">
                                                <div
                                                    className="inline-flex items-center gap-2 px-3 py-1.5 rounded-full bg-green-50 text-green-700 text-sm font-medium">
                                                    <svg className="w-4 h-4 mr-1" fill="currentColor"
                                                         viewBox="0 0 20 20">
                                                        <path fillRule="evenodd"
                                                              d="M10 18a8 8 0 100-16 8 8 0 000 16zm3.707-9.293a1 1 0 00-1.414-1.414L9 10.586 7.707 9.293a1 1 0 00-1.414 1.414l2 2a1 1 0 001.414 0l4-4z"
                                                              clipRule="evenodd"/>
                                                    </svg>
                                                    <span>{t("evaluationList.evaluation")} {evaluation.submitted ? t('evaluationList.submitted')
                                                        : t('evaluationList.draft_created')} - {new Date(evaluation.dateEvaluation).toLocaleDateString()}</span>
                                                </div>
                                            </div>
                                        )}
                                    </div>

                                    <div
                                        className="flex flex-col items-start justify-center md:items-end md:justify-center gap-3">
                                        {hasEvaluation ? (
                                            <button
                                                onClick={() => handleViewPdf(agreement.studentId, agreement.offerId)}
                                                className="inline-flex items-center justify-center px-4 py-2 rounded-md bg-emerald-600 text-white text-sm font-medium shadow-sm hover:bg-emerald-700 transition"
                                            >
                                                {t("evaluationList.view_pdf")}
                                            </button>
                                        ) : (
                                            <Link
                                                to={isTeacherAssigned ? `/dashboard/employeur/evaluation/${agreement.studentId}/${agreement.offerId}` : '#'}
                                                className={`inline-flex items-center justify-center px-4 py-2 rounded-md text-white text-sm font-medium shadow-sm transition ${
                                                    isTeacherAssigned
                                                        ? 'bg-blue-600 hover:bg-blue-700'
                                                        : 'bg-gray-400 cursor-not-allowed'
                                                }`}

                                            >
                                                {isTeacherAssigned
                                                    ? t("evaluationList.create_evaluation")
                                                    : t("evaluationList.awaiting_teacher")
                                                }
                                            </Link>
                                        )}
                                    </div>
                                </div>
                            </div>
                        );
                    })}
                </div>
            )}

            {showPdfViewer && currentPdfBlob && (
                <PdfViewer
                    file={currentPdfBlob}
                    onClose={handleClosePdfViewer}
                />
            )}
        </div>
    );
}