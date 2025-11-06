import React, { useState, useEffect } from 'react';
import { Link } from 'react-router-dom';
import { useTranslation } from 'react-i18next';
import { getEligibleEvaluations, checkExistingEvaluation, previewEvaluationPdf } from '../../api/apiEmployeur';
import PdfViewer from '../PdfViewer.jsx';

export default function EvaluationsList() {
    const [eligibleAgreements, setEligibleAgreements] = useState([]);
    const [loading, setLoading] = useState(true);
    const [showPdfViewer, setShowPdfViewer] = useState(false);
    const [currentPdfBlob, setCurrentPdfBlob] = useState(null);
    const [evaluationStatus, setEvaluationStatus] = useState({});
    const {t} = useTranslation()

    useEffect(() => {
        const fetchEligibleAgreements = async () => {
            try {
                const agreements = await getEligibleEvaluations();
                setEligibleAgreements(agreements);

                // Check evaluation status for each agreement
                const statusMap = {};
                for (const agreement of agreements) {
                    try {
                        const existingCheck = await checkExistingEvaluation(agreement.studentId, agreement.offerId);
                        statusMap[`${agreement.studentId}-${agreement.offerId}`] = existingCheck;
                    } catch (error) {
                        console.error(t("evaluationList.errors.checking_evaluation") + `${agreement.studentId}`, error)
                        statusMap[`${agreement.studentId}-${agreement.offerId}`] = { exists: false };
                    }
                }
                setEvaluationStatus(statusMap);

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
            alert(t("evaluationList.errors.pdf_loading") + (error?.response?.data?.message || error?.message));
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
        <div className="max-w-4xl mx-auto p-6">
            <h1 className="text-2xl font-bold mb-6">{t("evaluationList.title")}</h1>

            {eligibleAgreements.length === 0 ? (
                <div className="text-center py-8">
                    <p className="text-gray-500">{t("evaluationList.no_evaluations")}</p>
                </div>
            ) : (
                <div className="space-y-4">
                    {eligibleAgreements.map(agreement => {
                        const statusKey = `${agreement.studentId}-${agreement.offerId}`;
                        const status = evaluationStatus[statusKey];
                        const hasEvaluation = status?.exists;
                        const evaluation = status?.evaluation;

                        return (
                            <div key={agreement.id} className="border border-gray-200 rounded-lg p-6 shadow-sm">
                                <div className="flex justify-between items-start">
                                    <div className="flex-1">
                                        <h3 className="text-lg font-semibold text-gray-900">
                                            {agreement.studentFirstName} {agreement.studentLastName}
                                        </h3>
                                        <p className="text-gray-600 mt-1">{t(agreement.studentProgram)}</p>
                                        <p className="text-gray-700 mt-2">{agreement.internshipDescription}</p>
                                        <p className="text-sm text-gray-500 mt-1">{agreement.companyName}</p>

                                        {hasEvaluation && evaluation && (
                                            <div className="mt-3">
                                                <div className="flex items-center text-sm text-green-600">
                                                    <svg className="w-4 h-4 mr-1" fill="currentColor" viewBox="0 0 20 20">
                                                        <path fillRule="evenodd" d="M10 18a8 8 0 100-16 8 8 0 000 16zm3.707-9.293a1 1 0 00-1.414-1.414L9 10.586 7.707 9.293a1 1 0 00-1.414 1.414l2 2a1 1 0 001.414 0l4-4z" clipRule="evenodd" />
                                                    </svg>
                                                    <span>{t("evaluationList.evaluation")} {evaluation.submitted ? t('evaluationList.submitted')
                                                        : t('evaluationList.draft_created')} - {new Date(evaluation.dateEvaluation).toLocaleDateString()}</span>
                                                </div>
                                            </div>
                                        )}
                                    </div>

                                    <div className="flex flex-col gap-2 ml-4">
                                        {hasEvaluation ? (
                                            <>
                                                <button
                                                    onClick={() => handleViewPdf(agreement.studentId, agreement.offerId)}
                                                    className="bg-green-600 text-white px-4 py-2 rounded hover:bg-green-700 transition-colors text-sm"
                                                >
                                                    View PDF
                                                </button>
                                            </>
                                        ) : (
                                            <Link
                                                to={`/dashboard/employeur/evaluation/${agreement.studentId}/${agreement.offerId}`}
                                                className="bg-blue-600 text-white px-4 py-2 rounded hover:bg-blue-700 transition-colors text-sm"
                                            >
                                                {t("evaluationList.create_evaluation")}
                                            </Link>
                                        )}
                                    </div>
                                </div>
                            </div>
                        );
                    })}
                </div>
            )}

            {/* PDF Viewer Modal */}
            {showPdfViewer && currentPdfBlob && (
                <PdfViewer
                    file={currentPdfBlob}
                    onClose={handleClosePdfViewer}
                />
            )}
        </div>
    );
}