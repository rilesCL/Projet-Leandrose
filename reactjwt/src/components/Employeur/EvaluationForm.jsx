import React, { useState, useEffect } from 'react';
import { useTranslation } from 'react-i18next';
import { useParams, useNavigate } from 'react-router-dom';
import {
    createEvaluation,
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
    const [successMessage, setSuccessMessage] = useState(null);
    const [submitted, setSubmitted] = useState(false);

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

    const validateForm = () => {
        const newErrors = [];

        for (const [categoryKey, questions] of Object.entries(formData.categories)) {
            // Find all questions without rating
            const missingIndexes = questions
                .map((q, i) => (!q.rating ? i + 1 : null))
                .filter(i => i !== null);

            if (missingIndexes.length > 0) {
                newErrors.push(
                    `${t('evaluation.validation.missingRating')} - ${evaluationTemplate[categoryKey].title}, ${t('evaluation.questions')} ${missingIndexes.join(', ')}`
                );
            }
        }

        if (formData.globalAssessment === null)
            newErrors.push(t('evaluation.validation.globalAssessmentRequired'));

        if (!formData.globalAppreciation.trim())
            newErrors.push(t('evaluation.validation.globalAppreciationRequired'));

        if (formData.discussedWithTrainee === null)
            newErrors.push(t('evaluation.validation.discussedRequired'));

        if (formData.supervisionHours === '' || isNaN(formData.supervisionHours))
            newErrors.push(t('evaluation.validation.supervisionHoursRequired'));

        if (!formData.welcomeNextInternship)
            newErrors.push(t('evaluation.validation.welcomeNextInternshipRequired'));

        if (formData.technicalTrainingSufficient === null)
            newErrors.push(t('evaluation.validation.technicalTrainingRequired'));

        return newErrors;
    };



    useEffect(() => {
        const initializeForm = async () => {
            try {
                setLoading(true);
                setError(null);

                if (!studentId || !offerId) {
                    throw new Error(t("evaluation.errors.missing_studentId_Or_offerId"));
                }

                const existingCheck = await checkExistingEvaluation(studentId, offerId);

                if (existingCheck.exists) {
                    setError(t('evaluation.errors.evaluation_exists'));
                    setTimeout(() => {
                        navigate('/dashboard/employeur/evaluations');
                    }, 3000);
                    return;
                }

                const evaluationInfo = await getEvaluationInfo(studentId, offerId);

                setStudent({
                    firstName: evaluationInfo.studentInfo.firstName,
                    lastName: evaluationInfo.studentInfo.lastName,
                    program: evaluationInfo.studentInfo.program
                });
                setInternship({
                    description: evaluationInfo.internshipInfo.description,
                    companyName: evaluationInfo.internshipInfo.companyName
                });

                const initialFormData = {
                    categories: {},
                    generalComment: '',
                    globalAssessment: null,
                    globalAppreciation: '',
                    discussedWithTrainee: null,
                    supervisionHours: '',
                    welcomeNextInternship: '',
                    technicalTrainingSufficient: null
                };

                Object.keys(evaluationTemplate).forEach(categoryKey => {
                    initialFormData.categories[categoryKey] = evaluationTemplate[categoryKey].questions.map(() => ({
                        checked: false,
                        comment: '',
                        rating: null
                    }));
                });

                setFormData(initialFormData);

            } catch (err) {
                const errorMessage = err?.response?.data?.message || err?.message || t('evaluation.initializationError');
                setError(errorMessage);
            } finally {
                setLoading(false);
            }
        };

        initializeForm();
    }, [studentId, offerId, t, navigate]);

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

    const handleSubmitEvaluation = async () => {
        setError(null);
        setSuccessMessage(null);

        const validationErrors = validateForm();
        if (validationErrors.length > 0) {
            setError(validationErrors.join('\n'));
            return;
        }

        setSubmitting(true);

        try {
            const createResponse = await createEvaluation(studentId, offerId);
            const evalId = createResponse.evaluationId || createResponse.id || createResponse;

            if (!evalId) {
                throw new Error("Evaluation ID not received from server.");
            }

            setEvaluationId(evalId);

            await new Promise(resolve => setTimeout(resolve, 500));

            await generateEvaluationPdfWithId(evalId, formData);

            setSubmitted(true);
            setSuccessMessage("Evaluation submitted successfully!");


        } catch (err) {
            console.error("Error in evaluation submission:", err);
            const errorMessage =
                err?.response?.data?.message || err?.message || "An error occurred while submitting the evaluation.";
            setError(errorMessage);
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

    if (error && !student) {
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
                        {t('evaluation.backToEvaluationsList')}
                    </button>
                </div>
            </div>
        );
    }

    return (
        <div className="max-w-6xl mx-auto px-4 py-8">
            {/* Header Section */}
            <div className="mb-8">
                <div className="mb-4">
                    <button
                        type="button"
                        onClick={() => navigate('/dashboard/employeur/evaluations')}
                        className="inline-flex items-center gap-2 rounded-md border border-blue-100 bg-white px-4 py-2 text-sm font-medium text-blue-600 shadow-sm transition hover:border-blue-300 hover:bg-blue-50 hover:text-blue-700"
                    >
                        <svg className="h-4 w-4" viewBox="0 0 20 20" fill="none">
                            <line x1="11" y1="5" x2="6" y2="10" stroke="currentColor" strokeWidth="1.6" strokeLinecap="round" />
                            <line x1="6" y1="10" x2="11" y2="15" stroke="currentColor" strokeWidth="1.6" strokeLinecap="round" />
                            <line x1="6" y1="10" x2="16" y2="10" stroke="currentColor" strokeWidth="1.6" strokeLinecap="round" />
                        </svg>
                        {t('evaluation.backToEvaluationsList')}
                    </button>
                </div>
                <h1 className="text-3xl font-bold text-gray-900 text-center mb-6">
                    {t('evaluation.title')}
                </h1>

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
                        <div className="mb-4">
                            <h2 className="text-xl font-semibold text-gray-900 mb-2">
                                {category.title}
                            </h2>
                            <p className="text-gray-600 italic text-sm">
                                {category.description}
                            </p>
                        </div>

                        <div className="space-y-6">
                            {category.questions.map((question, questionIndex) => (
                                <div key={questionIndex} className="border-b border-gray-100 pb-6 last:border-b-0">
                                    <p className="text-gray-800 font-medium mb-3 leading-relaxed">
                                        {question}
                                    </p>

                                    <div className="flex flex-wrap justify-center gap-3 mb-3">
                                        {[
                                            {
                                                value: 'EXCELLENT',
                                                label: t('evaluation.rating.totally_agree'),
                                                baseClasses: 'bg-green-400 border-2 border-green-600 text-green-900 font-semibold hover:border-green-700 hover:bg-green-500/80',
                                                selectedClasses: 'border-[3px] border-green-800 ring-2 ring-green-600 ring-offset-2 shadow-md',
                                                inputRing: 'focus:ring-green-600 text-green-700'
                                            },
                                            {
                                                value: 'TRES_BIEN',
                                                label: t('evaluation.rating.mostly_agree'),
                                                baseClasses: 'bg-green-200 border-2 border-green-400 text-green-900 font-semibold hover:border-green-500 hover:bg-green-300/80',
                                                selectedClasses: 'border-[3px] border-green-600 ring-2 ring-green-400 ring-offset-2 shadow-md',
                                                inputRing: 'focus:ring-green-500 text-green-600'
                                            },
                                            {
                                                value: 'SATISFAISANT',
                                                label: t('evaluation.rating.mostly_disagree'),
                                                baseClasses: 'bg-orange-200 border-2 border-orange-400 text-orange-900 font-semibold hover:border-orange-500 hover:bg-orange-300/80',
                                                selectedClasses: 'border-[3px] border-orange-600 ring-2 ring-orange-400 ring-offset-2 shadow-md',
                                                inputRing: 'focus:ring-orange-500 text-orange-600'
                                            },
                                            {
                                                value: 'A_AMELIORER',
                                                label: t('evaluation.rating.totally_disagree'),
                                                baseClasses: 'bg-red-400 border-2 border-red-600 text-red-900 font-semibold hover:border-red-700 hover:bg-red-500/80',
                                                selectedClasses: 'border-[3px] border-red-800 ring-2 ring-red-600 ring-offset-2 shadow-md',
                                                inputRing: 'focus:ring-red-600 text-red-700'
                                            }
                                        ].map((option) => {
                                            const isSelected = formData.categories[categoryKey]?.[questionIndex]?.rating === option.value;

                                            return (
                                                <label
                                                    key={option.value}
                                                    className={`flex items-center px-4 py-2 rounded-lg cursor-pointer transition-all ${option.baseClasses} ${
                                                        isSelected ? option.selectedClasses : ''
                                                    }`}
                                                >
                                                    <input
                                                        type="radio"
                                                        name={`rating-${categoryKey}-${questionIndex}`}
                                                        value={option.value}
                                                        checked={isSelected}
                                                        onChange={(e) =>
                                                            handleQuestionChange(categoryKey, questionIndex, 'rating', e.target.value)
                                                        }
                                                        className={`mr-2 focus:ring-2 ${option.inputRing}`}
                                                    />
                                                    <span className="text-sm font-medium">{option.label}</span>
                                                </label>
                                            );
                                        })}
                                    </div>

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

                {/* Global Assessment Section with Unified Radio Buttons */}
                <div className="bg-white border border-gray-200 rounded-lg p-6 shadow-sm">
                    <h2 className="text-xl font-semibold text-gray-900 mb-4">
                        {t('evaluation.globalAssessment.title')}
                    </h2>

                    {/* Global Assessment Options with Same Radio Button Style */}
                    <div className="mb-6">
                        <p className="text-gray-800 font-medium mb-3 leading-relaxed">
                            {t('evaluation.globalAssessment.question')}
                        </p>
                        <div className="flex flex-wrap justify-center gap-3 mb-3">
                            {[
                                {
                                    value: 0,
                                    label: t('evaluation.rating.totally_agree'),
                                    baseClasses: 'bg-green-400 border-2 border-green-600 text-green-900 font-semibold hover:border-green-700 hover:bg-green-500/80',
                                    selectedClasses: 'border-[3px] border-green-800 ring-2 ring-green-600 ring-offset-2 shadow-md',
                                    inputRing: 'focus:ring-green-600 text-green-700'
                                },
                                {
                                    value: 1,
                                    label: t('evaluation.rating.mostly_agree'),
                                    baseClasses: 'bg-green-200 border-2 border-green-400 text-green-900 font-semibold hover:border-green-500 hover:bg-green-300/80',
                                    selectedClasses: 'border-[3px] border-green-600 ring-2 ring-green-400 ring-offset-2 shadow-md',
                                    inputRing: 'focus:ring-green-500 text-green-600'
                                },
                                {
                                    value: 3,
                                    label: t('evaluation.rating.mostly_disagree'),
                                    baseClasses: 'bg-orange-200 border-2 border-orange-400 text-orange-900 font-semibold hover:border-orange-500 hover:bg-orange-300/80',
                                    selectedClasses: 'border-[3px] border-orange-600 ring-2 ring-orange-400 ring-offset-2 shadow-md',
                                    inputRing: 'focus:ring-orange-500 text-orange-600'
                                },
                                {
                                    value: 4,
                                    label: t('evaluation.rating.totally_disagree'),
                                    baseClasses: 'bg-red-400 border-2 border-red-600 text-red-900 font-semibold hover:border-red-700 hover:bg-red-500/80',
                                    selectedClasses: 'border-[3px] border-red-800 ring-2 ring-red-600 ring-offset-2 shadow-md',
                                    inputRing: 'focus:ring-red-600 text-red-700'
                                }
                            ].map((option) => {
                                const isSelected = formData.globalAssessment === option.value;

                                return (
                                    <label
                                        key={option.value}
                                        className={`flex items-center px-4 py-2 rounded-lg cursor-pointer transition-all ${option.baseClasses} ${
                                            isSelected ? option.selectedClasses : ''
                                        }`}
                                    >
                                        <input
                                            type="radio"
                                            name="globalAssessment"
                                            value={option.value}
                                            checked={isSelected}
                                            onChange={(e) => handleFieldChange('globalAssessment', parseInt(e.target.value))}
                                            className={`mr-2 focus:ring-2 ${option.inputRing}`}
                                        />
                                        <span className="text-sm font-medium">{option.label}</span>
                                    </label>
                                );
                            })}
                        </div>
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
                                    onChange={() => handleFieldChange('discussedWithTrainee', true)}
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
                                    onChange={() => handleFieldChange('discussedWithTrainee', false)}
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
                            {['YES', 'NO', 'MAYBE'].map((option) => (
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
                                    onChange={() => handleFieldChange('technicalTrainingSufficient', true)}
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
                                    onChange={() => handleFieldChange('technicalTrainingSufficient', false)}
                                    className="text-blue-600 focus:ring-blue-500"
                                />
                                <span className="text-gray-700">{t('evaluation.globalAssessment.no')}</span>
                            </label>
                        </div>
                    </div>
                </div>

                {successMessage && (
                    <div className="bg-green-50 border border-green-200 rounded-lg p-4">
                        <div className="flex items-center">
                            <div className="flex-shrink-0">
                                <svg className="h-5 w-5 text-green-400" viewBox="0 0 20 20" fill="currentColor">
                                    <circle cx="10" cy="10" r="8" />
                                    <polyline points="7,10 9,12 13,8" fill="none" stroke="white" strokeWidth="2" strokeLinecap="round" strokeLinejoin="round" />
                                </svg>
                            </div>
                            <div className="ml-3">
                                <p className="text-sm font-medium text-green-800">
                                    {successMessage}
                                </p>
                            </div>
                        </div>
                    </div>
                )}

                {error && student && (
                    <div className="bg-red-50 border border-red-300 rounded-xl p-4 shadow-sm">
                        <div className="flex items-start">
                            <div className="flex-shrink-0 mt-1">
                                <svg className="h-6 w-6 text-red-500" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2">
                                    <circle cx="12" cy="12" r="10" />
                                    <line x1="15" y1="9" x2="9" y2="15" />
                                    <line x1="9" y1="9" x2="15" y2="15" />
                                </svg>
                            </div>

                            <div className="ml-3 flex-1">
                                <h3 className="text-sm font-semibold text-red-800 mb-1">
                                    ⚠️ {t('evaluation.errors.submit') || 'Veuillez corriger les erreurs suivantes :'}
                                </h3>
                                <ul className="list-disc list-inside space-y-1 text-sm text-red-700">
                                    {error.split('\n').map((msg, index) => (
                                        <li key={index}>{msg}</li>
                                    ))}
                                </ul>
                            </div>

                            {/* Close button */}
                            <button
                                onClick={() => setError(null)}
                                className="ml-4 text-red-400 hover:text-red-600 transition-colors"
                                aria-label="Fermer le message d’erreur"
                            >
                                <svg className="h-5 w-5" viewBox="0 0 20 20" fill="none">
                                    <line x1="5" y1="5" x2="15" y2="15" stroke="currentColor" strokeWidth="2" strokeLinecap="round" />
                                    <line x1="15" y1="5" x2="5" y2="15" stroke="currentColor" strokeWidth="2" strokeLinecap="round" />
                                </svg>
                            </button>
                        </div>
                    </div>
                )}


                {/* Action Buttons */}
                <div className="flex justify-end gap-3 pt-6 border-t border-gray-200">
                    <button
                        type="button"
                        onClick={handleSubmitEvaluation}
                        disabled={submitting || submitted}
                        className="px-6 py-2.5 bg-blue-600 text-white font-medium rounded-md hover:bg-blue-700 focus:outline-none focus:ring-2 focus:ring-blue-500 focus:ring-offset-2 disabled:opacity-50 disabled:cursor-not-allowed transition-colors"
                    >
                        {submitting ? (
                            <span className="flex items-center gap-2">
                                <div className="animate-spin rounded-full h-4 w-4 border-b-2 border-white"></div>
                                {t('evaluation.submitting')}
                            </span>
                        ) : submitted ? (
                            t('evaluation.submitted')
                        ) : (
                            t('evaluation.submitEvaluation')
                        )}
                    </button>
                </div>
            </div>
        </div>
    );
};

export default EvaluationForm;