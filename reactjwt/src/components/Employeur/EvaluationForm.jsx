import React, { useState, useEffect } from 'react';
import { useTranslation } from 'react-i18next';
import { useParams, useNavigate } from 'react-router-dom';
import { createEvaluation, generateEvaluationPdf, previewEvaluationPdf, getStudentInfo, getInternshipInfo } from "../../api/apiEmployeur.jsx";

const EvaluationForm = () => {
    const { t } = useTranslation();
    const { studentId, offerId } = useParams();
    const navigate = useNavigate();

    const [loading, setLoading] = useState(true);
    const [submitting, setSubmitting] = useState(false);
    const [evaluationId, setEvaluationId] = useState(null);
    const [student, setStudent] = useState(null);
    const [internship, setInternship] = useState(null);

    // Form state
    const [formData, setFormData] = useState({
        categories: {},
        generalComment: ''
    });

    // Evaluation categories and questions structure
    const evaluationTemplate = {
        productivity: {
            title: t('evaluation.productivity.title'),
            description: t('evaluation.productivity.description'),
            questions: [
                t('evaluation.productivity.q1'),
                t('evaluation.productivity.q2'),
                t('evaluation.productivity.q3'),
                t('evaluation.productivity.q4'),
                t('evaluation.productivity.q5')
            ]
        },
        quality: {
            title: t('evaluation.quality.title'),
            description: t('evaluation.quality.description'),
            questions: [
                t('evaluation.quality.q1'),
                t('evaluation.quality.q2'),
                t('evaluation.quality.q3')
            ]
        },
        autonomy: {
            title: t('evaluation.autonomy.title'),
            description: t('evaluation.autonomy.description'),
            questions: [
                t('evaluation.autonomy.q1'),
                t('evaluation.autonomy.q2'),
                t('evaluation.autonomy.q3'),
                t('evaluation.autonomy.q4')
            ]
        },
        relationships: {
            title: t('evaluation.relationships.title'),
            description: t('evaluation.relationships.description'),
            questions: [
                t('evaluation.relationships.q1'),
                t('evaluation.relationships.q2'),
                t('evaluation.relationships.q3'),
                t('evaluation.relationships.q4')
            ]
        },
        professionalism: {
            title: t('evaluation.professionalism.title'),
            description: t('evaluation.professionalism.description'),
            questions: [
                t('evaluation.professionalism.q1'),
                t('evaluation.professionalism.q2'),
                t('evaluation.professionalism.q3'),
                t('evaluation.professionalism.q4'),
                t('evaluation.professionalism.q5')
            ]
        }
    };

    // Initialize evaluation on component mount
    useEffect(() => {
        const initializeEvaluation = async () => {
            try {
                setLoading(true);

                const evaluationResponse = await createEvaluation(studentId, offerId);
                setEvaluationId(evaluationResponse.evaluationId);

                const [studentData, internshipData] = await Promise.all([
                    getStudentInfo(studentId),
                    getInternshipInfo(offerId)
                ]);

                setStudent(studentData);
                setInternship(internshipData);

                const initialFormData = {
                    categories: {},
                    generalComment: ''
                };

                Object.keys(evaluationTemplate).forEach(categoryKey => {
                    initialFormData.categories[categoryKey] = evaluationTemplate[categoryKey].questions.map(() => ({
                        checked: false,
                        comment: '',
                        rating: 'NON_APPLICABLE'
                    }));
                });

                setFormData(initialFormData);

            } catch (error) {
                console.error('Error initializing evaluation:', error);
                alert(t('evaluation.initializationError'));
            } finally {
                setLoading(false);
            }
        };

        initializeEvaluation();
    }, [studentId, offerId, t]);

    // Handle question field changes
    const handleQuestionChange = (categoryKey, questionIndex, field, value) => {
        setFormData(prev => ({
            ...prev,
            categories: {
                ...prev.categories,
                [categoryKey]: prev.categories[categoryKey].map((question, idx) =>
                    idx === questionIndex ? { ...question, [field]: value } : question
                )
            }
        }));
    };

    // Handle general comment change
    const handleGeneralCommentChange = (comment) => {
        setFormData(prev => ({
            ...prev,
            generalComment: comment
        }));
    };

    // Generate and download PDF
    const handleGeneratePdf = async () => {
        if (!evaluationId) return;

        setSubmitting(true);
        try {
            await generateEvaluationPdf(evaluationId, formData);

            // Download the generated PDF
            const pdfBlob = await previewEvaluationPdf(evaluationId);
            const url = window.URL.createObjectURL(pdfBlob);
            const link = document.createElement('a');
            link.href = url;
            link.download = `evaluation_${student?.firstName}_${student?.lastName}.pdf`;
            document.body.appendChild(link);
            link.click();
            document.body.removeChild(link);
            window.URL.revokeObjectURL(url);

            alert(t('evaluation.pdfGenerated'));
            navigate('/employer/evaluations');

        } catch (error) {
            console.error('Error generating PDF:', error);
            alert(t('evaluation.pdfGenerationError'));
        } finally {
            setSubmitting(false);
        }
    };

    // Preview PDF without saving
    const handlePreviewPdf = async () => {
        if (!evaluationId) return;

        try {
            const pdfBlob = await previewEvaluationPdf(evaluationId, formData);
            const url = window.URL.createObjectURL(pdfBlob);
            window.open(url, '_blank');
        } catch (error) {
            console.error('Error previewing PDF:', error);
            alert(t('evaluation.previewError'));
        }
    };

    if (loading) {
        return (
            <div className="flex flex-col items-center justify-center min-h-64 py-12">
                <div className="animate-spin rounded-full h-12 w-12 border-b-2 border-blue-600 mb-4"></div>
                <p className="text-gray-600">{t('evaluation.loading')}</p>
            </div>
        );
    }

    return (
        <div className="max-w-6xl mx-auto px-4 py-8">
            {/* Header Section */}
            <div className="mb-8">
                <h1 className="text-3xl font-bold text-gray-900 text-center mb-6">
                    {t('evaluation.title')}
                </h1>

                {/* Student and Company Information */}
                <div className="bg-gray-50 border border-gray-200 rounded-lg p-6 shadow-sm">
                    {/* Student Information */}
                    <div className="mb-6">
                        <h3 className="text-lg font-semibold text-gray-700 mb-4 pb-2 border-b border-gray-200">
                            {t('evaluation.studentInfo')}
                        </h3>
                        <div className="grid grid-cols-1 md:grid-cols-2 gap-4">
                            <div>
                                <label className="block text-sm font-medium text-gray-600 mb-1">
                                    {t('evaluation.studentName')}
                                </label>
                                <p className="text-gray-900 font-medium">
                                    {student?.firstName} {student?.lastName}
                                </p>
                            </div>
                            <div>
                                <label className="block text-sm font-medium text-gray-600 mb-1">
                                    {t('evaluation.program')}
                                </label>
                                <p className="text-gray-900">{student?.program}</p>
                            </div>
                        </div>
                    </div>

                    {/* Company Information */}
                    <div>
                        <h3 className="text-lg font-semibold text-gray-700 mb-4 pb-2 border-b border-gray-200">
                            {t('evaluation.companyInfo')}
                        </h3>
                        <div className="grid grid-cols-1 md:grid-cols-3 gap-4">
                            <div>
                                <label className="block text-sm font-medium text-gray-600 mb-1">
                                    {t('evaluation.company')}
                                </label>
                                <p className="text-gray-900">{internship?.companyName}</p>
                            </div>
                            <div>
                                <label className="block text-sm font-medium text-gray-600 mb-1">
                                    {t('evaluation.internship')}
                                </label>
                                <p className="text-gray-900">{internship?.description}</p>
                            </div>
                            <div>
                                <label className="block text-sm font-medium text-gray-600 mb-1">
                                    {t('evaluation.evaluationDate')}
                                </label>
                                <p className="text-gray-900">{new Date().toLocaleDateString()}</p>
                            </div>
                        </div>
                    </div>
                </div>
            </div>

            {/* Instructions */}
            <div className="bg-blue-50 border border-blue-200 rounded-lg p-4 mb-6">
                <p className="text-blue-700 italic text-sm">
                    {t('evaluation.instructions')}
                </p>
            </div>

            {/* Evaluation Form */}
            <div className="space-y-6">
                {Object.entries(evaluationTemplate).map(([categoryKey, category]) => (
                    <div key={categoryKey} className="bg-white border border-gray-200 rounded-lg p-6 shadow-sm">
                        {/* Category Header */}
                        <div className="mb-4">
                            <h2 className="text-xl font-semibold text-gray-900 mb-2">
                                {category.title}
                            </h2>
                            <p className="text-gray-600 italic text-sm">
                                {category.description}
                            </p>
                        </div>

                        {/* Questions List */}
                        <div className="space-y-4">
                            {category.questions.map((question, questionIndex) => (
                                <div key={questionIndex} className="border-b border-gray-100 pb-4 last:border-b-0">
                                    {/* Question Row */}
                                    <div className="flex flex-col md:flex-row md:items-start md:justify-between gap-3 mb-3">
                                        <p className="text-gray-800 font-medium flex-1 leading-relaxed">
                                            {question}
                                        </p>

                                        <div className="flex items-center gap-4 flex-shrink-0">
                                            {/* Checkbox */}
                                            <label className="flex items-center gap-2 text-sm text-gray-600 whitespace-nowrap">
                                                <input
                                                    type="checkbox"
                                                    checked={formData.categories[categoryKey]?.[questionIndex]?.checked || false}
                                                    onChange={(e) =>
                                                        handleQuestionChange(categoryKey, questionIndex, 'checked', e.target.checked)
                                                    }
                                                    className="w-4 h-4 text-blue-600 border-gray-300 rounded focus:ring-blue-500"
                                                />
                                                {t('evaluation.observed')}
                                            </label>

                                            {/* Rating Select */}
                                            <select
                                                value={formData.categories[categoryKey]?.[questionIndex]?.rating || 'NON_APPLICABLE'}
                                                onChange={(e) =>
                                                    handleQuestionChange(categoryKey, questionIndex, 'rating', e.target.value)
                                                }
                                                className="px-3 py-1.5 border border-gray-300 rounded-md text-sm focus:outline-none focus:ring-2 focus:ring-blue-500 focus:border-blue-500 min-w-[120px]"
                                            >
                                                <option value="NON_APPLICABLE">{t('rating.NON_APPLICABLE')}</option>
                                                <option value="EXCELLENT">{t('rating.EXCELLENT')}</option>
                                                <option value="TRES_BIEN">{t('rating.TRES_BIEN')}</option>
                                                <option value="SATISFAISANT">{t('rating.SATISFAISANT')}</option>
                                                <option value="A_AMELIORER">{t('rating.A_AMELIORER')}</option>
                                            </select>
                                        </div>
                                    </div>

                                    {/* Comment Textarea */}
                                    <textarea
                                        placeholder={t('evaluation.commentsPlaceholder')}
                                        value={formData.categories[categoryKey]?.[questionIndex]?.comment || ''}
                                        onChange={(e) =>
                                            handleQuestionChange(categoryKey, questionIndex, 'comment', e.target.value)
                                        }
                                        className="w-full px-3 py-2 border border-gray-300 rounded-md text-sm resize-y focus:outline-none focus:ring-2 focus:ring-blue-500 focus:border-blue-500"
                                        rows={2}
                                    />
                                </div>
                            ))}
                        </div>
                    </div>
                ))}

                {/* General Comments Section */}
                <div className="bg-white border border-gray-200 rounded-lg p-6 shadow-sm">
                    <h2 className="text-xl font-semibold text-gray-900 mb-4">
                        {t('evaluation.generalComments')}
                    </h2>
                    <textarea
                        placeholder={t('evaluation.generalCommentsPlaceholder')}
                        value={formData.generalComment}
                        onChange={(e) => handleGeneralCommentChange(e.target.value)}
                        className="w-full px-3 py-2 border border-gray-300 rounded-md text-sm resize-y focus:outline-none focus:ring-2 focus:ring-blue-500 focus:border-blue-500"
                        rows={4}
                    />
                </div>

                {/* Action Buttons */}
                <div className="flex flex-col sm:flex-row justify-end gap-3 pt-6 border-t border-gray-200">
                    <button
                        type="button"
                        onClick={handlePreviewPdf}
                        disabled={submitting}
                        className="px-6 py-2.5 bg-gray-600 text-white font-medium rounded-md hover:bg-gray-700 focus:outline-none focus:ring-2 focus:ring-gray-500 focus:ring-offset-2 disabled:opacity-50 disabled:cursor-not-allowed transition-colors"
                    >
                        {t('evaluation.preview')}
                    </button>

                    <button
                        type="button"
                        onClick={handleGeneratePdf}
                        disabled={submitting}
                        className="px-6 py-2.5 bg-blue-600 text-white font-medium rounded-md hover:bg-blue-700 focus:outline-none focus:ring-2 focus:ring-blue-500 focus:ring-offset-2 disabled:opacity-50 disabled:cursor-not-allowed transition-colors"
                    >
                        {submitting ? (
                            <span className="flex items-center gap-2">
                <div className="animate-spin rounded-full h-4 w-4 border-b-2 border-white"></div>
                                {t('evaluation.generating')}
              </span>
                        ) : (
                            t('evaluation.generatePdf')
                        )}
                    </button>
                </div>
            </div>
        </div>
    );
};

export default EvaluationForm;