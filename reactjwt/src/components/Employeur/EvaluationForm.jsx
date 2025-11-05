import React, { useState, useEffect } from 'react';
import { useTranslation } from 'react-i18next';
import { useParams, useNavigate } from 'react-router-dom';
import PdfViewer from '../PdfViewer.jsx';
import {
    createEvaluation,
    previewEvaluationPdf,
    getEvaluationInfo,
    generateEvaluationPdfWithId,
    checkExistingEvaluation
} from '../../api/apiEmployeur';

const EvaluationForm = () => {
    const { t } = useTranslation();
    const { studentId, offerId } = useParams();
    const navigate = useNavigate();

    const [loading, setLoading] = useState(true);
    const [submitting, setSubmitting] = useState(false);
    const [evaluationId, setEvaluationId] = useState(null);
    const [student, setStudent] = useState(null);
    const [internship, setInternship] = useState(null);
    const [error, setError] = useState(null);
    const [submitted, setSubmitted] = useState(false); // New state for submission status

    const [formData, setFormData] = useState({
        categories: {},
        generalComment: '',
        globalAssessment: null,
        globalAppreciation: '',
        discussedWithTrainee: null,
        supervisionHours: '',
        welcomeNextInternship: '',
        technicalTrainingSufficient: null
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
                t('evaluation.quality.q3'),
                t('evaluation.quality.q4'),
                t('evaluation.quality.q5')
            ]
        },

        relationships: {
            title: t('evaluation.relationships.title'),
            description: t('evaluation.relationships.description'),
            questions: [
                t('evaluation.relationships.q1'),
                t('evaluation.relationships.q2'),
                t('evaluation.relationships.q3'),
                t('evaluation.relationships.q4'),
                t('evaluation.relationships.q5'),
                t('evaluation.relationships.q6')
            ]
        },
        skills: {
            title: t('evaluation.skills.title'),
            description: t('evaluation.skills.description'),
            questions: [
                t('evaluation.skills.q1'),
                t('evaluation.skills.q2'),
                t('evaluation.skills.q3'),
                t('evaluation.skills.q4'),
                t('evaluation.skills.q5'),
                t('evaluation.skills.q6')
            ]
        }
    };


    useEffect(() => {
        const initializeForm = async () => {
            try {
                setLoading(true);
                setError(null);

                console.log('Initializing form with:', { studentId, offerId });

                if (!studentId || !offerId) {
                    throw new Error('Missing student ID or offer ID');
                }

                // First, check if an evaluation already exists
                console.log('Checking for existing evaluation...');
                const existingCheck = await checkExistingEvaluation(studentId, offerId);
                console.log('Existing evaluation check:', existingCheck);

                if (existingCheck.exists) {
                    // Evaluation already exists - redirect to evaluations list
                    console.log('Evaluation already exists, redirecting...');
                    alert('Une évaluation existe déjà pour ce stagiaire. Vous allez être redirigé vers la liste des évaluations.');
                    navigate('/dashboard/employeur/evaluations');
                    return;
                }

                // Get evaluation info (student + internship data)
                console.log('Fetching evaluation info...');
                const evaluationInfo = await getEvaluationInfo(studentId, offerId);
                console.log('Evaluation info:', evaluationInfo);

                setStudent({
                    firstName: evaluationInfo.studentInfo.firstName,
                    lastName: evaluationInfo.studentInfo.lastName,
                    program: evaluationInfo.studentInfo.program
                });
                setInternship({
                    description: evaluationInfo.internshipInfo.description,
                    companyName: evaluationInfo.internshipInfo.companyName
                });

                // Initialize empty form data
                console.log('Initializing form data...');
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
                console.log('Form initialization completed successfully');

            } catch (error) {
                console.error('Error initializing form:', error);
                const errorMessage = error?.response?.data?.message || error?.message || t('evaluation.initializationError');
                setError(errorMessage);
            } finally {
                setLoading(false);
            }
        };

        initializeForm();
    }, [studentId, offerId, t, navigate]);

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

    const handleFieldChange = (field, value) => {
        setFormData(prev => ({
            ...prev,
            [field]: value
        }));
    };

    // Generate and download PDF, then redirect
    const handleGeneratePdf = async () => {
        setSubmitting(true);
        try {
            console.log('Starting PDF generation process...');

            // 1. Create evaluation and get evaluationId
            console.log('Step 1: Creating evaluation...');
            const createResponse = await createEvaluation(studentId, offerId);
            console.log('Create evaluation response:', createResponse);

            const evalId = createResponse.evaluationId || createResponse.id || createResponse;
            console.log('Evaluation ID:', evalId);

            if (!evalId) {
                throw new Error('No evaluation ID received from server');
            }

            setEvaluationId(evalId);

            // Small delay to ensure evaluation is saved
            await new Promise(resolve => setTimeout(resolve, 500));

            // 2. Generate PDF using evaluationId
            console.log('Step 2: Generating PDF for evaluation:', evalId);
            console.log('Form data:', formData);

            const generateResponse = await generateEvaluationPdfWithId(evalId, formData);
            console.log('PDF generation response:', generateResponse);

            // 3. Download using evaluationId
            console.log('Step 3: Downloading PDF for evaluation:', evalId);
            const pdfBlob = await previewEvaluationPdf(evalId);
            console.log('PDF blob received, size:', pdfBlob.size);

            // Create download
            const url = window.URL.createObjectURL(pdfBlob);
            const link = document.createElement('a');
            link.href = url;
            link.download = `evaluation_${student?.firstName}_${student?.lastName}.pdf`;
            document.body.appendChild(link);
            link.click();
            document.body.removeChild(link);
            window.URL.revokeObjectURL(url);

            // Show success message and redirect
            alert(t('evaluation.pdfGenerated'));

            // Redirect to evaluations list
            navigate('/dashboard/employeur/evaluations');

        } catch (error) {
            console.error('Error in PDF generation process:', error);
            const errorMessage = error?.response?.data?.message || error?.message || t('evaluation.pdfGenerationError');
            alert(`${t('evaluation.pdfGenerationError')}: ${errorMessage}`);
        } finally {
            setSubmitting(false);
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

    if (error) {
        return (
            <div className="max-w-4xl mx-auto px-4 py-8">
                <div className="bg-red-50 border border-red-200 rounded-lg p-6">
                    <h2 className="text-xl font-semibold text-red-800 mb-2">
                        {t('evaluation.initializationError')}
                    </h2>
                    <p className="text-red-700 mb-4">{error}</p>
                    <button
                        onClick={() => navigate('/dashboard/employeur/evaluations')}
                        className="px-4 py-2 bg-red-600 text-white rounded-md hover:bg-red-700"
                    >
                        Back to Evaluations List
                    </button>
                </div>
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

                {submitted && (
                    <div className="bg-green-50 border border-green-200 rounded-lg p-4 mb-6">
                        <div className="flex items-center">
                            <div className="flex-shrink-0">
                                <svg className="h-5 w-5 text-green-400" viewBox="0 0 20 20" fill="currentColor">
                                    <path fillRule="evenodd" d="M10 18a8 8 0 100-16 8 8 0 000 16zm3.707-9.293a1 1 0 00-1.414-1.414L9 10.586 7.707 9.293a1 1 0 00-1.414 1.414l2 2a1 1 0 001.414 0l4-4z" clipRule="evenodd" />
                                </svg>
                            </div>
                            <div className="ml-3">
                                <p className="text-sm font-medium text-green-800">
                                    {t('evaluation.submittedSuccess')}
                                </p>
                            </div>
                        </div>
                    </div>
                )}

                <div className="bg-gray-50 border border-gray-200 rounded-lg p-6 shadow-sm">
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
                                <p className="text-gray-900">{t(student?.program) || 'N/A'}</p>
                            </div>
                        </div>
                    </div>

                    <div>
                        <h3 className="text-lg font-semibold text-gray-700 mb-4 pb-2 border-b border-gray-200">
                            {t('evaluation.companyInfo')}
                        </h3>
                        <div className="grid grid-cols-1 md:grid-cols-3 gap-4">
                            <div>
                                <label className="block text-sm font-medium text-gray-600 mb-1">
                                    {t('evaluation.company')}
                                </label>
                                <p className="text-gray-900">{internship?.companyName || 'N/A'}</p>
                            </div>
                            <div>
                                <label className="block text-sm font-medium text-gray-600 mb-1">
                                    {t('evaluation.internship')}
                                </label>
                                <p className="text-gray-900">{internship?.description || 'N/A'}</p>
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


                                            {/* Rating Select */}
                                            <select
                                                value={formData.categories[categoryKey]?.[questionIndex]?.rating || 'NON_APPLICABLE'}
                                                onChange={(e) =>
                                                    handleQuestionChange(categoryKey, questionIndex, 'rating', e.target.value)
                                                }
                                                className="px-3 py-1.5 border border-gray-300 rounded-md text-sm focus:outline-none focus:ring-2 focus:ring-blue-500 focus:border-blue-500 min-w-[120px]"
                                            >
                                                <option value="NON_APPLICABLE">{t('evaluation.rating.non_applicable')}</option>
                                                <option value="EXCELLENT">{t('evaluation.rating.totally_agree')}</option>
                                                <option value="TRES_BIEN">{t('evaluation.rating.mostly_agree')}</option>
                                                <option value="SATISFAISANT">{t('evaluation.rating.mostly_disagree')}</option>
                                                <option value="A_AMELIORER">{t('evaluation.rating.totally_disagree')}</option>
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

                {/* NEW: Global Assessment Section */}
                <div className="bg-white border border-gray-200 rounded-lg p-6 shadow-sm">
                    <h2 className="text-xl font-semibold text-gray-900 mb-4">
                        {t('evaluation.globalAssessment.title')}
                    </h2>

                    {/* Global Assessment Options */}
                    <div className="space-y-3 mb-6">
                        {[0, 1, 2, 3, 4].map((index) => (
                            <label key={index} className="flex items-start space-x-3 cursor-pointer">
                                <input
                                    type="radio"
                                    name="globalAssessment"
                                    value={index}
                                    checked={formData.globalAssessment === index}
                                    onChange={(e) => handleFieldChange('globalAssessment', parseInt(e.target.value))}
                                    className="mt-1 text-blue-600 focus:ring-blue-500"
                                />
                                <span className="text-gray-700 text-sm">
                  {t(`evaluation.globalAssessment.option${index}`)}
                </span>
                            </label>
                        ))}
                    </div>

                    {/* Global Appreciation */}
                    <div className="mb-6">
                        <label className="block text-sm font-medium text-gray-700 mb-2">
                            {t('evaluation.globalAssessment.specify')}
                        </label>
                        <textarea
                            value={formData.globalAppreciation}
                            onChange={(e) => handleFieldChange('globalAppreciation', e.target.value)}
                            className="w-full px-3 py-2 border border-gray-300 rounded-md text-sm resize-y focus:outline-none focus:ring-2 focus:ring-blue-500 focus:border-blue-500"
                            rows={3}
                        />
                    </div>

                    {/* Discussion with Trainee */}
                    <div className="mb-6">
                        <label className="block text-sm font-medium text-gray-700 mb-2">
                            {t('evaluation.globalAssessment.discussed')}
                        </label>
                        <div className="flex space-x-6">
                            <label className="flex items-center space-x-2 cursor-pointer">
                                <input
                                    type="radio"
                                    name="discussedWithTrainee"
                                    value="true"
                                    checked={formData.discussedWithTrainee === true}
                                    onChange={(e) => handleFieldChange('discussedWithTrainee', true)}
                                    className="text-blue-600 focus:ring-blue-500"
                                />
                                <span className="text-gray-700">{t('evaluation.globalAssessment.yes')}</span>
                            </label>
                            <label className="flex items-center space-x-2 cursor-pointer">
                                <input
                                    type="radio"
                                    name="discussedWithTrainee"
                                    value="false"
                                    checked={formData.discussedWithTrainee === false}
                                    onChange={(e) => handleFieldChange('discussedWithTrainee', false)}
                                    className="text-blue-600 focus:ring-blue-500"
                                />
                                <span className="text-gray-700">{t('evaluation.globalAssessment.no')}</span>
                            </label>
                        </div>
                    </div>

                    {/* Supervision Hours */}
                    <div className="mb-6">
                        <label className="block text-sm font-medium text-gray-700 mb-2">
                            {t('evaluation.globalAssessment.supervision_hours')}
                        </label>
                        <input
                            type="number"
                            value={formData.supervisionHours}
                            onChange={(e) => handleFieldChange('supervisionHours', e.target.value)}
                            className="w-32 px-3 py-2 border border-gray-300 rounded-md text-sm focus:outline-none focus:ring-2 focus:ring-blue-500 focus:border-blue-500"
                            min="0"
                            max="40"
                        />
                    </div>

                    {/* Welcome Next Internship */}
                    <div className="mb-6">
                        <label className="block text-sm font-medium text-gray-700 mb-2">
                            {t('evaluation.globalAssessment.welcome_nextInternship')}
                        </label>
                        <div className="flex space-x-6">
                            {['YES', 'NO', t("welcome_maybe")].map((option) => (
                                <label key={option} className="flex items-center space-x-2 cursor-pointer">
                                    <input
                                        type="radio"
                                        name="welcomeNextInternship"
                                        value={option}
                                        checked={formData.welcomeNextInternship === option}
                                        onChange={(e) => handleFieldChange('welcomeNextInternship', e.target.value)}
                                        className="text-blue-600 focus:ring-blue-500"
                                    />
                                    <span className="text-gray-700">{t(`evaluation.globalAssessment.${option.toLowerCase()}`)}</span>
                                </label>
                            ))}
                        </div>
                    </div>

                    {/* Technical Training Sufficient */}
                    <div>
                        <label className="block text-sm font-medium text-gray-700 mb-2">
                            {t('evaluation.globalAssessment.technical_training')}
                        </label>
                        <div className="flex space-x-6">
                            <label className="flex items-center space-x-2 cursor-pointer">
                                <input
                                    type="radio"
                                    name="technicalTrainingSufficient"
                                    value="true"
                                    checked={formData.technicalTrainingSufficient === true}
                                    onChange={(e) => handleFieldChange('technicalTrainingSufficient', true)}
                                    className="text-blue-600 focus:ring-blue-500"
                                />
                                <span className="text-gray-700">{t('evaluation.globalAssessment.yes')}</span>
                            </label>
                            <label className="flex items-center space-x-2 cursor-pointer">
                                <input
                                    type="radio"
                                    name="technicalTrainingSufficient"
                                    value="false"
                                    checked={formData.technicalTrainingSufficient === false}
                                    onChange={(e) => handleFieldChange('technicalTrainingSufficient', false)}
                                    className="text-blue-600 focus:ring-blue-500"
                                />
                                <span className="text-gray-700">{t('evaluation.globalAssessment.no')}</span>
                            </label>
                        </div>
                    </div>
                </div>

                {/* Action Buttons */}
                <div className="flex justify-end gap-3 pt-6 border-t border-gray-200">
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