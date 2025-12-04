

import React, {useEffect, useState} from 'react';
import {useTranslation} from 'react-i18next';
import {useNavigate, useParams} from 'react-router-dom';
import {
    checkExistingEvaluation,
    createEvaluation,
    generateEvaluationPdfWithId,
    getEvaluationInfo
} from '../../api/apiEmployeur';

const EvaluationForm = () => {
    const {t} = useTranslation();
    const {studentId, offerId} = useParams();
    const navigate = useNavigate();
    const [loading, setLoading] = useState(true);
    const [submitting, setSubmitting] = useState(false);
    const [evaluationId, setEvaluationId] = useState(null);
    const [student, setStudent] = useState(null);
    const [internship, setInternship] = useState(null);

    const [error, setError] = useState(null);
    const [toast, setToast] = useState({show: false, message: '', type: 'success'});
    const [submitted, setSubmitted] = useState(false);

    const [errors, setErrors] = useState({});

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
        const newErrors = {};

        for (const [categoryKey, questions] of Object.entries(formData.categories)) {
            for (let i = 0; i < questions.length; i++) {
                const q = questions[i];
                if (!q.rating) {
                    if (!newErrors.categories) newErrors.categories = {};
                    if (!newErrors.categories[categoryKey]) newErrors.categories[categoryKey] = {};

                    newErrors.categories[categoryKey][i] = `${t('evaluation.validation.missingRating')} - ${evaluationTemplate[categoryKey].title}, ${t('evaluation.questions')} ${i + 1}`;
                }
            }
        }


        if (formData.globalAssessment === null) {
            newErrors.globalAssessment = t('evaluation.validation.globalAssessmentRequired');
        }
        if (!formData.globalAppreciation || !formData.globalAppreciation.trim()) {
            newErrors.globalAppreciation = t('evaluation.validation.globalAppreciationRequired');
        }
        if (formData.discussedWithTrainee === null) {
            newErrors.discussedWithTrainee = t('evaluation.validation.discussedRequired');
        }
        if (formData.supervisionHours === '' || isNaN(Number(formData.supervisionHours))) {
            newErrors.supervisionHours = t('evaluation.validation.supervisionHoursRequired');
        }
        if (!formData.welcomeNextInternship) {
            newErrors.welcomeNextInternship = t('evaluation.validation.welcomeNextInternshipRequired');
        }
        if (formData.technicalTrainingSufficient === null) {
            newErrors.technicalTrainingSufficient = t('evaluation.validation.technicalTrainingRequired');
        }

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
                let evalId =null
                if (existingCheck.exists && existingCheck.evaluation) {
                    evalId = existingCheck.evaluation.id
                    setEvaluationId(evalId)
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
                const errorMessage = err?.response?.data?.message || err?.message || t('evaluation.errors.initializationError');
                setError(errorMessage);
            } finally {
                setLoading(false);
            }
        };
        initializeForm();
    }, [studentId, offerId, t, navigate]);


    const clearCategoryQuestionError = (categoryKey, questionIndex) => {
        setErrors(prev => {
            if (!prev?.categories || !prev.categories[categoryKey]) return prev;
            const catCopy = {...prev.categories[categoryKey]};
            delete catCopy[questionIndex];
            const categoriesCopy = {...prev.categories, [categoryKey]: catCopy};

            if (Object.keys(catCopy).length === 0) delete categoriesCopy[categoryKey];
            const newPrev = {...prev, categories: categoriesCopy};
            if (!newPrev.categories || Object.keys(newPrev.categories).length === 0) {
                delete newPrev.categories;
            }
            return Object.keys(newPrev).length === 0 ? {} : newPrev;
        });
    };


    const handleQuestionChange = (categoryKey, questionIndex, field, value) => {
        setFormData(prev => ({
            ...prev,
            categories: {
                ...prev.categories,
                [categoryKey]: prev.categories[categoryKey].map((question, idx) =>
                    idx === questionIndex ? {...question, [field]: value} : question
                )
            }
        }));

        if (field === 'rating') {
            clearCategoryQuestionError(categoryKey, questionIndex);
        }
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
        setErrors(prev => {
            if (!prev) return prev;
            const copy = {...prev};
            if (copy[field]) delete copy[field];
            if (Object.keys(copy).length === 0) return {};
            return copy;
        });
    };

    const hasErrors = (obj) => {
        if (!obj) return false;

        for (const k of Object.keys(obj)) {
            if (k === 'categories') {
                if (Object.keys(obj.categories).length > 0) return true;
            } else {
                if (obj[k]) return true;
            }
        }
        return false;
    };

    const closeToast = () => {
        setToast({show: false, message: '', type: 'success'});
        if (submitted) {
            navigate("/dashboard/employeur?tab=evaluations");
        }
    };

    const handleSubmitEvaluation = async () => {
        setError(null);
        setToast({show: false, message: '', type: 'success'});

        const validationErrors = validateForm();
        if (hasErrors(validationErrors)) {
            setErrors(validationErrors);


            setTimeout(() => {
                const el = document.querySelector('.validation-error');
                if (el && typeof el.scrollIntoView === 'function') {
                    el.scrollIntoView({behavior: 'smooth', block: 'center'});
                }
            }, 50);
            return;
        }


        setErrors({});
        setSubmitting(true);
        try {
            let evalId
            const existingCheck = await checkExistingEvaluation(studentId, offerId)

            if (existingCheck.exists && existingCheck.evaluation) {
                evalId = existingCheck.evaluation.id;
            } else {
                const createResponse = await createEvaluation(studentId, offerId);
                evalId = createResponse.evaluationId || createResponse.id || createResponse;
            }
            if (!evalId) {
                throw new Error(t("evaluation.errors.evaluationId_received"));
            }
            setEvaluationId(evalId);
            await new Promise(resolve => setTimeout(resolve, 500));
            console.log("EvaluationId: ", evalId)
            await generateEvaluationPdfWithId(evalId, formData);
            setSubmitted(true);
            setToast({
                show: true,
                message: t("evaluation.submittedSuccess"),
                type: 'success'
            });
        } catch (err) {
            console.error(t("evaluation.errors.submit"), err);
            const errorMessage =
                err?.response?.data?.message || err?.message || t("evaluation.errors.submit");
            setError(errorMessage);
        } finally {
            setSubmitting(false);
        }
    };

    const isFormDisabled = toast.show && toast.type === 'success';

    if (loading) {
        return (
            <div className="flex flex-col items-center justify-center min-h-64 py-12">
                <div className="animate-spin rounded-full h-12 w-12 border-b-2 border-blue-600 mb-4"></div>
                <p className="text-gray-600">{t('evaluation.loading')}</p>
            </div>
        );
    }


    const optionBase = "flex items-center justify-center px-4 py-2 rounded-lg cursor-pointer transition-all border font-semibold text-sm";
    const optionDefault = "bg-gray-100 border-gray-300 text-gray-800";


    const getRatingButtonClasses = (value, isSelected) => {
        if (!isSelected) return optionDefault;

        switch (value) {
            case "EXCELLENT":
            case 0:
                return "bg-green-800 border-green-900 text-white shadow";
            case "TRES_BIEN":
            case 1:
                return "bg-green-200 border-green-300 text-green-900 shadow";
            case "SATISFAISANT":
            case 3:
                return "bg-red-200 border-red-300 text-red-900 shadow";
            case "A_AMELIORER":
            case 4:
                return "bg-red-800 border-red-900 text-white shadow";
            default:
                return optionDefault;
        }
    };

    if (error && !student) {
        return (
            <div className="max-w-4xl mx-auto px-4 py-8">
                <div className="bg-red-50 border border-red-200 rounded-lg p-6">
                    <h2 className="text-xl font-semibold text-red-800 mb-2">
                        {t('evaluation.initializationError')}
                    </h2>
                    <p className="text-red-700 mb-4">{error}</p>
                    <button
                        onClick={() => navigate('/dashboard/employeur')}
                        className="px-4 py-2 bg-red-600 text-white rounded-md hover:bg-red-700"
                    >
                        {t('evaluation.backToEvaluationsList')}
                    </button>
                </div>
            </div>
        );
    }

    return (
        <div className="max-w-6xl mx-auto px-4 py-8 relative">
            {isFormDisabled && (
                <div className="absolute inset-0 bg-white bg-opacity-75 z-10 rounded-lg"></div>
            )}
            <div className="mb-8">
                <div className="mb-4">
                    <button
                        type="button"
                        onClick={() => navigate('/dashboard/employeur')}
                        className="inline-flex items-center gap-2 rounded-md border border-blue-100 bg-white px-4 py-2 text-sm font-medium text-blue-600 shadow-sm transition hover:border-blue-300 hover:bg-blue-50 hover:text-blue-700"
                    >
                        <svg className="h-4 w-4" viewBox="0 0 20 20" fill="none">
                            <line x1="11" y1="5" x2="6" y2="10" stroke="currentColor" strokeWidth="1.6"
                                  strokeLinecap="round"/>
                            <line x1="6" y1="10" x2="11" y2="15" stroke="currentColor" strokeWidth="1.6"
                                  strokeLinecap="round"/>
                            <line x1="6" y1="10" x2="16" y2="10" stroke="currentColor" strokeWidth="1.6"
                                  strokeLinecap="round"/>
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

            <div className="bg-blue-50 border border-blue-200 rounded-lg p-4 mb-6">
                <p className="text-blue-700 italic text-sm">
                    {t('evaluation.instructions')}
                </p>
            </div>

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
                            {category.questions.map((question, questionIndex) => {
                                const ratingError = errors?.categories?.[categoryKey]?.[questionIndex];
                                return (
                                    <div key={questionIndex} className="border-b border-gray-100 pb-6 last:border-b-0">
                                        <p className="text-gray-800 font-medium mb-3 leading-relaxed">
                                            {question}
                                        </p>
                                        
                                        <div className="flex flex-col sm:flex-row gap-3 mb-3">
                                            {[
                                                { value: 'EXCELLENT', label: t('evaluation.rating.totally_agree') },
                                                { value: 'TRES_BIEN', label: t('evaluation.rating.mostly_agree') },
                                                { value: 'SATISFAISANT', label: t('evaluation.rating.mostly_disagree') },
                                                { value: 'A_AMELIORER', label: t('evaluation.rating.totally_disagree') }
                                            ].map((option) => {
                                                const isSelected = formData.categories[categoryKey]?.[questionIndex]?.rating === option.value;
                                                return (
                                                    <label
                                                        key={option.value}
                                                        className={`${optionBase} ${getRatingButtonClasses(option.value, isSelected)} flex-1`}
                                                    >
                                                        <input
                                                            type="radio"
                                                            name={`rating-${categoryKey}-${questionIndex}`}
                                                            value={option.value}
                                                            checked={isSelected}
                                                            onChange={(e) =>
                                                                handleQuestionChange(categoryKey, questionIndex, 'rating', e.target.value)
                                                            }
                                                            className="sr-only"
                                                        />
                                                        <span>{option.label}</span>
                                                    </label>
                                                );
                                            })}
                                        </div>

                                        {ratingError && (
                                            <p className="validation-error text-sm text-red-600 mb-2">{ratingError}</p>
                                        )}

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
                                );
                            })}
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

                <div className="bg-white border border-gray-200 rounded-lg p-6 shadow-sm">
                    <h2 className="text-xl font-semibold text-gray-900 mb-4">
                        {t('evaluation.globalAssessment.title')}
                    </h2>

                    <div className="mb-6">
                        <p className="text-gray-800 font-medium mb-3 leading-relaxed">
                            {t('evaluation.globalAssessment.question')}
                        </p>
                        
                        <div className="flex flex-col sm:flex-row gap-3 mb-3">
                            {[
                                { value: 0, label: t('evaluation.rating.totally_agree') },
                                { value: 1, label: t('evaluation.rating.mostly_agree') },
                                { value: 3, label: t('evaluation.rating.mostly_disagree') },
                                { value: 4, label: t('evaluation.rating.totally_disagree') }
                            ].map((option) => {
                                const isSelected = formData.globalAssessment === option.value;
                                return (
                                    <label
                                        key={option.value}
                                        className={`${optionBase} ${getRatingButtonClasses(option.value, isSelected)} flex-1`}
                                    >
                                        <input
                                            type="radio"
                                            name="globalAssessment"
                                            value={option.value}
                                            checked={isSelected}
                                            onChange={(e) => handleFieldChange('globalAssessment', parseInt(e.target.value))}
                                            className="sr-only"
                                        />
                                        <span>{option.label}</span>
                                    </label>
                                );
                            })}
                        </div>

                        {errors.globalAssessment && (
                            <p className="validation-error text-sm text-red-600 mb-2">{errors.globalAssessment}</p>
                        )}
                    </div>

                    <div className="mb-6">
                        <label className="block text-sm font-medium text-gray-700 mb-2">
                            {t('evaluation.globalAssessment.discussed')}
                        </label>
                        <textarea
                            value={formData.globalAppreciation}
                            onChange={(e) => handleFieldChange('globalAppreciation', e.target.value)}
                            className="w-full px-3 py-2 border border-gray-300 rounded-md text-sm resize-y focus:outline-none focus:ring-2 focus:ring-blue-500 focus:border-blue-500"
                            rows={3}
                        />
                        {errors.globalAppreciation && (
                            <p className="validation-error text-sm text-red-600 mt-1">{errors.globalAppreciation}</p>
                        )}
                    </div>

                    <div className="mb-6">
                        <p className="text-gray-800 font-medium mb-3 leading-relaxed">
                            {t('evaluation.globalAssessment.technical_training')}
                        </p>
                        
                        <div className="flex flex-col sm:flex-row gap-3 mb-3">
                            {[
                                { value: true, label: t('evaluation.globalAssessment.yes') },
                                { value: false, label: t('evaluation.globalAssessment.no') }
                            ].map((option) => {
                                const isSelected = formData.discussedWithTrainee === option.value;
                                const optionSelected = "bg-blue-600 border-blue-700 text-white shadow";
                                return (
                                    <label
                                        key={String(option.value)}
                                        className={`${optionBase} ${isSelected ? optionSelected : optionDefault} flex-1`}
                                    >
                                        <input
                                            type="radio"
                                            name="discussedWithTrainee"
                                            value={option.value}
                                            checked={isSelected}
                                            onChange={(e) => handleFieldChange('discussedWithTrainee', option.value)}
                                            className="sr-only"
                                        />
                                        <span>{option.label}</span>
                                    </label>
                                );
                            })}
                        </div>

                        {errors.discussedWithTrainee && (
                            <p className="validation-error text-sm text-red-600 mt-1">{errors.discussedWithTrainee}</p>
                        )}
                    </div>
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
                        {errors.supervisionHours && (
                            <p className="validation-error text-sm text-red-600 mt-1">{errors.supervisionHours}</p>
                        )}
                    </div>

                    <div className="mb-6">
                        <p className="text-gray-800 font-medium mb-3 leading-relaxed">
                            {t('evaluation.globalAssessment.welcome_nextInternship')}
                        </p>
                        
                        <div className="flex flex-col sm:flex-row gap-3 mb-3">
                            {[
                                { value: 'YES', label: t('evaluation.globalAssessment.yes') },
                                { value: 'NO', label: t('evaluation.globalAssessment.no') },
                                { value: 'MAYBE', label: t('evaluation.globalAssessment.maybe') }
                            ].map((option) => {
                                const isSelected = formData.welcomeNextInternship === option.value;
                                const optionSelected = "bg-blue-600 border-blue-700 text-white shadow";
                                return (
                                    <label
                                        key={option.value}
                                        className={`${optionBase} ${isSelected ? optionSelected : optionDefault} flex-1`}
                                    >
                                        <input
                                            type="radio"
                                            name="welcomeNextInternship"
                                            value={option.value}
                                            checked={isSelected}
                                            onChange={(e) => handleFieldChange('welcomeNextInternship', option.value)}
                                            className="sr-only"
                                        />
                                        <span>{option.label}</span>
                                    </label>
                                );
                            })}
                        </div>

                        {errors.welcomeNextInternship && (
                            <p className="validation-error text-sm text-red-600 mb-2">{errors.welcomeNextInternship}</p>
                        )}
                    </div>

                    <div className="mb-6">
                        <p className="text-gray-800 font-medium mb-3 leading-relaxed">
                            {t('evaluation.globalAssessment.technical_training')}
                        </p>
                        
                        <div className="flex flex-col sm:flex-row gap-3 mb-3">
                            {[
                                { value: true, label: t('evaluation.globalAssessment.yes') },
                                { value: false, label: t('evaluation.globalAssessment.no') }
                            ].map((option) => {
                                const isSelected = formData.technicalTrainingSufficient === option.value;
                                const optionSelected = "bg-blue-600 border-blue-700 text-white shadow";
                                return (
                                    <label
                                        key={String(option.value)}
                                        className={`${optionBase} ${isSelected ? optionSelected : optionDefault} flex-1`}
                                    >
                                        <input
                                            type="radio"
                                            name="technicalTrainingSufficient"
                                            value={option.value}
                                            checked={isSelected}
                                            onChange={(e) => handleFieldChange('technicalTrainingSufficient', option.value)}
                                            className="sr-only"
                                        />
                                        <span>{option.label}</span>
                                    </label>
                                );
                            })}
                        </div>

                        {errors.technicalTrainingSufficient && (
                            <p className="validation-error text-sm text-red-600 mt-1">{errors.technicalTrainingSufficient}</p>
                        )}
                    </div>
                </div>

                {error && student && (
                    <div className="bg-red-50 border border-red-300 rounded-xl p-4 shadow-sm">
                        <div className="flex items-start">
                            <div className="flex-shrink-0 mt-1">
                                <svg className="h-6 w-6 text-red-500" viewBox="0 0 24 24" fill="none"
                                     stroke="currentColor" strokeWidth="2">
                                    <circle cx="12" cy="12" r="10"/>
                                    <line x1="15" y1="9" x2="9" y2="15"/>
                                    <line x1="9" y1="9" x2="15" y2="15"/>
                                </svg>
                            </div>
                            <div className="ml-3 flex-1">
                                <h3 className="text-sm font-semibold text-red-800 mb-1">
                                    ⚠️ {t('evaluation.errors.submit') || 'Une erreur est survenue :'}
                                </h3>
                                <p className="text-sm text-red-700">{error}</p>
                            </div>
                            <button
                                onClick={() => setError(null)}
                                className="ml-4 text-red-400 hover:text-red-600 transition-colors"
                                aria-label="Fermer le message d'erreur"
                            >
                                <svg className="h-5 w-5" viewBox="0 0 20 20" fill="none">
                                    <line x1="5" y1="5" x2="15" y2="15" stroke="currentColor" strokeWidth="2"
                                          strokeLinecap="round"/>
                                    <line x1="15" y1="5" x2="5" y2="15" stroke="currentColor" strokeWidth="2"
                                          strokeLinecap="round"/>
                                </svg>
                            </button>
                        </div>
                    </div>
                )}

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
        </div>
    );
};

export default EvaluationForm;