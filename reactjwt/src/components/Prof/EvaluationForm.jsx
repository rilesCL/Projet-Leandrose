import React, { useEffect, useState } from "react";
import { useParams, useNavigate } from "react-router-dom";
import {
    getEvaluationInfo,
    createEvaluation,
        generateEvaluationPdfWithId
} from "../../api/apiProf";
import { useTranslation } from 'react-i18next';

const EvaluationForm = () => {

    const { t } = useTranslation();
    const { studentId, offerId } = useParams();
    const navigate = useNavigate();

    const [info, setInfo] = useState(null);
    const [loading, setLoading] = useState(true);
    const [error, setError] = useState(null);
    const [errors, setErrors] = useState({});
    const [submitting, setSubmitting] = useState(false);
    const [submitted, setSubmitted] = useState(false);
    const [toast, setToast] = useState({show: false, message: '', type: 'success'});

    const teacherEvaluationTemplate = {
        conformity: {
            title: t('studentProfile.conformity.title'),
            description: t('studentProfile.conformity.description'),
            questions: [
                t('studentProfile.conformity.q1'),
                t('studentProfile.conformity.q2'),
                t('studentProfile.conformity.q3'),
            ]
        },
        environment: {
            title: t('studentProfile.environment.title'),
            description: t('studentProfile.environment.description'),
            questions: [
                t('studentProfile.environment.q1'),
                t('studentProfile.environment.q2'),
            ]
        },
        general: {
            title: t('studentProfile.general.title'),
            description: t('studentProfile.general.description'),
            questions: [
                t('studentProfile.general.q1'),
                t('studentProfile.general.q2'),
                t('studentProfile.general.q3'),
                t('studentProfile.general.q4'),
                t('studentProfile.general.q5'),
            ]
        }
    };

    const [formData, setFormData] = useState({
        categories: {},
        comments: "",
        preferredStage: "",
        capacity: "",
        sameTraineeNextStage: null
    });

    function normalizeYesNo(value) {
        if (value === "YES") return true;
        if (value === "NO") return false;
        return value;
    }

    const validateForm = () => {
        const newErrors = {};

        for (const [categoryKey, questions] of Object.entries(formData.categories)) {
            for (let i = 0; i < questions.length; i++) {
                const q = questions[i];

                if (!q.rating) {
                    if (!newErrors.categories) newErrors.categories = {};
                    if (!newErrors.categories[categoryKey]) newErrors.categories[categoryKey] = {};

                    newErrors.categories[categoryKey][i] =
                        `${t('evaluation.validation.missingRating')} - ${t(`studentProfile.${categoryKey}.title`)} Q${i + 1}`;
                }
            }
        }

        if (!formData.preferredStage)
            newErrors.preferredStage = t('evaluation.validation.selectOption');

        if (!formData.capacity)
            newErrors.capacity = t('evaluation.validation.selectOption');

        if (!formData.sameTraineeNextStage)
            newErrors.sameTraineeNextStage = t('evaluation.validation.selectOption');

        return newErrors;
    };

    useEffect(() => {
        const loadInfo = async () => {
            try {
                setLoading(true);

                const data = await getEvaluationInfo(studentId, offerId);
                setInfo(data);

                const initialCategories = {};
                Object.entries(teacherEvaluationTemplate).forEach(([key, group]) => {
                    initialCategories[key] =
                        Array.isArray(group.questions)
                            ? group.questions.map(() => ({ rating: "" }))
                            : [];
                });

                setFormData(prev => ({
                    ...prev,
                    categories: initialCategories
                }));

            } catch (err) {
                console.error(err);
                setError(t('evaluation.errors.initializationError'));
            } finally {
                setLoading(false);
            }
        };

        loadInfo();
    }, [studentId, offerId]);

    const clearCategoryQuestionError = (categoryKey, questionIndex) => {
        setErrors(prev => {
            if (!prev?.categories || !prev.categories[categoryKey]) return prev;

            const updatedCat = { ...prev.categories[categoryKey] };
            delete updatedCat[questionIndex];

            const updatedCategories = { ...prev.categories };
            if (Object.keys(updatedCat).length === 0) {
                delete updatedCategories[categoryKey];
            } else {
                updatedCategories[categoryKey] = updatedCat;
            }

            const newErrors = { ...prev, categories: updatedCategories };
            if (Object.keys(updatedCategories).length === 0) {
                delete newErrors.categories;
            }

            return newErrors;
        });
    };

    const clearFieldError = (field) => {
        setErrors(prev => {
            if (!prev || !prev[field]) return prev;
            const updated = { ...prev };
            delete updated[field];
            return updated;
        });
    };

    const handleChange = (field, value) => {
        setFormData(prev => ({ ...prev, [field]: value }));
        clearFieldError(field);
    };

    const handleQuestionChange = (category, index, value) => {
        setFormData(prev => ({
            ...prev,
            categories: {
                ...prev.categories,
                [category]: prev.categories[category].map((q, i) =>
                    i === index ? { ...q, rating: value } : q
                )
            }
        }));
        clearCategoryQuestionError(category, index);
    };

    const handleSubmit = async () => {
        setError(null);

        const normalizedForm = {
            ...formData,
            sameTraineeNextStage: normalizeYesNo(formData.sameTraineeNextStage)
        };

        const validationErrors = validateForm();
        if (Object.keys(validationErrors).length > 0) {
            setErrors(validationErrors);

            setTimeout(() => {
                const el = document.querySelector('.validation-error');
                if (el && typeof el.scrollIntoView === 'function') {
                    el.scrollIntoView({ behavior: 'smooth', block: 'center' });
                }
            }, 50);

            return;
        }

        setSubmitting(true);
        try {
            const createRes = await createEvaluation(studentId, offerId);
            const evalId = createRes.id;

            await generateEvaluationPdfWithId(evalId, normalizedForm);
            setSubmitted(true);
            setToast({
                show: true,
                message: t("evaluation.submittedSuccess"),
                type: 'success'
            });

        } catch (err) {
            console.error(err);
            setError(t('evaluation.errors.submit'));
        } finally {
            setSubmitting(false);
        }
    };

    if (loading) return <p className="text-gray-900">{t('evaluation.loading')}</p>;
    if (error && !info) return <p className="text-red-500">{error}</p>;

    const closeToast = () => {
        setToast({show: false, message: '', type: 'success'});
        if (submitted) {
            navigate("/dashboard/prof?tab=evaluations");
        }
    };

    const isFormDisabled = toast.show && toast.type === 'success';

    const optionBase = "flex items-center justify-center px-4 py-2 rounded-lg cursor-pointer transition-all border font-semibold text-sm";
    const optionDefault = "bg-gray-100 border-gray-300 text-gray-800";
    const optionSelected = "bg-blue-600 border-blue-700 text-white shadow";
    
    const getRatingButtonClasses = (value, isSelected) => {
        if (!isSelected) return optionDefault;
        
        switch (value) {
            case "EXCELLENT":
                return "bg-green-800 border-green-900 text-white shadow";
            case "TRES_BIEN":
                return "bg-green-200 border-green-300 text-green-900 shadow";
            case "SATISFAISANT":
                return "bg-red-200 border-red-300 text-red-900 shadow";
            case "A_AMELIORER":
                return "bg-red-800 border-red-900 text-white shadow";
            default:
                return optionDefault;
        }
    };

    return (
        <div className="max-w-4xl mx-auto p-6 relative">
            {isFormDisabled && (
                <div className="absolute inset-0 bg-white dark:bg-gray-800 bg-opacity-75 dark:bg-opacity-75 z-10 rounded-lg"></div>
            )}

            <div className="mb-4">
                <button
                    onClick={() => navigate(-1)}
                    className="flex items-center text-blue-600 hover:text-blue-800 focus:outline-none"
                    aria-label={t('common.back', { defaultValue: 'Retour' })}
                >
                    <svg className="w-5 h-5 mr-2" fill="none" stroke="currentColor" viewBox="0 0 24 24" xmlns="http://www.w3.org/2000/svg">
                        <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M15 19l-7-7 7-7" />
                    </svg>
                    {t('common.back', { defaultValue: 'Retour' })}
                </button>
            </div>

            <h1 className="text-3xl font-bold text-center mb-8 text-gray-900">
                {t('evaluation.title')}
            </h1>

            <section className="mb-8 p-4 border rounded-lg bg-gray-50">
                <h2 className="text-xl font-semibold mb-4 text-gray-900">{t('evaluation.companyInfo')}</h2>

                <p className="text-gray-700"><strong className="text-gray-900">{t('studentProfile.name')}</strong> {info?.entrepriseTeacherDto.companyName}</p>
                <p className="text-gray-700"><strong className="text-gray-900">{t('studentProfile.contact_person')}</strong> {info?.entrepriseTeacherDto.contactName}</p>
                <p className="text-gray-700"><strong className="text-gray-900">{t('studentProfile.address')}</strong> {info?.entrepriseTeacherDto.address}</p>
                <p className="text-gray-700"><strong className="text-gray-900">{t('studentProfile.email')}</strong> {info?.entrepriseTeacherDto.email}</p>
            </section>

            {/* Student Info */}
            <section className="mb-8 p-4 border rounded-lg bg-gray-50">
                <h2 className="text-xl font-semibold mb-4 text-gray-900">{t('evaluation.studentInfo')}</h2>
                <p className="text-gray-700"><strong className="text-gray-900">{t('studentProfile.name')}</strong> {info?.studentTeacherDto.fullname}</p>
                <p className="text-gray-700"><strong className="text-gray-900">{t('studentProfile.internStartDate')}</strong> {info?.studentTeacherDto.internshipStartDate}</p>
            </section>

            {/* Question Groups */}
            {Object.entries(teacherEvaluationTemplate).map(([groupKey, group]) => (
                <section key={groupKey} className="mb-8 p-4 border rounded-lg bg-white">

                    <h3 className="text-lg font-semibold mb-4 text-gray-900">{t(`studentProfile.${groupKey}.title`)}</h3>

                    {group.questions.map((q, index) => {
                        const ratingErrors = errors?.categories?.[groupKey]?.[index]

                        return (
                            <div key={index} className="mb-6">

                                <p className="mb-3 text-gray-700">{q}</p>

                                {ratingErrors && (
                                    <p className="validation-error text-sm text-red-600 mb-2">{ratingErrors}</p>
                                )}

                                {/* Rating Buttons: use grid for better layout - vertical on small screens */}
                                <div className="flex flex-col sm:flex-row gap-3">
                                    {[
                                        { value: "EXCELLENT", label: t('evaluation.rating.totally_agree') },
                                        { value: "TRES_BIEN", label: t('evaluation.rating.mostly_agree') },
                                        { value: "SATISFAISANT", label: t('evaluation.rating.mostly_disagree') },
                                        { value: "A_AMELIORER", label: t('evaluation.rating.totally_disagree') }
                                    ].map(option => {
                                        const isSelected =
                                            formData.categories[groupKey]?.[index]?.rating === option.value;

                                        return (
                                            <label
                                                key={option.value}
                                                className={`${optionBase} ${getRatingButtonClasses(option.value, isSelected)} flex-1`}
                                            >
                                                <input
                                                    type="radio"
                                                    name={`${groupKey}-${index}`}
                                                    value={option.value}
                                                    checked={isSelected}
                                                    onChange={() =>
                                                        handleQuestionChange(groupKey, index, option.value)
                                                    }
                                                    className="sr-only"
                                                />
                                                <span>{option.label}</span>
                                            </label>
                                        );
                                    })}
                                </div>

                            </div>
                        );
                    })}

                </section>
            ))}

            <section className="mb-8 p-4 border rounded-lg bg-gray-50">
                <h3 className="text-lg font-semibold mb-4 text-gray-900">{t("studentProfile.observations.title")}</h3>

                <div className="mb-6">
                    {errors?.preferredStage && (
                        <p className="validation-error text-sm text-red-600 mb-2 text-center">{errors.preferredStage}</p>
                    )}
                    <p className="font-medium mb-2 text-gray-700">{t("studentProfile.observations.q1")}</p>
                    <div className="flex flex-col sm:flex-row gap-3 justify-center">
                        {[{ value: "1", label: t('studentProfile.observations.first_intern')},
                            { value: "2", label: t('studentProfile.observations.second_intern') }].map(
                            opt => {
                                const isSelected = formData.preferredStage === opt.value;
                                return (
                                    <label
                                        key={opt.value}
                                        className={`${optionBase} ${isSelected ? optionSelected : optionDefault} flex-1`}
                                    >
                                        <input
                                            type="radio"
                                            name="obs_stagePreference"
                                            value={opt.value}
                                            checked={isSelected}
                                            onChange={e => handleChange("preferredStage", e.target.value)}
                                            className="sr-only"
                                        />
                                        {opt.label}
                                    </label>
                                );
                            }
                        )}
                    </div>
                </div>

                <div className="mb-6">
                    {errors?.capacity && (
                        <p className="validation-error text-sm text-red-600 mb-2 text-center">{errors.capacity}</p>
                    )}
                    <p className="font-medium mb-2 text-gray-700">{t("studentProfile.observations.q2")}</p>
                    <div className="flex flex-col sm:flex-row sm:flex-wrap gap-3 justify-center">
                        {[
                            { value: "1", label: t('studentProfile.observations.stage1') },
                            { value: "2", label: t('studentProfile.observations.stage2') },
                            { value: "3", label: t('studentProfile.observations.stage3') },
                            { value: "4", label: t('studentProfile.observations.stage4') }
                        ].map(opt => {
                            const isSelected = formData.capacity === opt.value;
                            return (
                                <label
                                    key={opt.value}
                                    className={`${optionBase} ${isSelected ? optionSelected : optionDefault}`}
                                >
                                    <input
                                        type="radio"
                                        name="obs_openToHosting"
                                        value={opt.value}
                                        checked={isSelected}
                                        onChange={e => handleChange("capacity", e.target.value)}
                                        className="sr-only"
                                    />
                                    {opt.label}
                                </label>
                            );
                        })}
                    </div>
                </div>

                <div className="mb-6">
                    {errors?.sameTraineeNextStage && (
                        <p className="validation-error text-sm text-red-600 mb-2 text-center">{errors.sameTraineeNextStage}</p>
                    )}
                    <p className="font-medium mb-2 text-gray-700">{t("studentProfile.observations.q3")}</p>
                    <div className="flex flex-col sm:flex-row gap-3 justify-center max-w-xs mx-auto">
                        {[{ value: "YES", label: t('studentProfile.observations.yes') }, { value: "NO", label: t('studentProfile.observations.no') }].map(opt => {
                            const isSelected = formData.sameTraineeNextStage === opt.value;
                            return (
                                <label
                                    key={opt.value}
                                    className={`${optionBase} ${isSelected ? optionSelected : optionDefault} flex-1`}
                                >
                                    <input
                                        type="radio"
                                        name="obs_sameInternAgain"
                                        value={opt.value}
                                        checked={isSelected}
                                        onChange={e => handleChange("sameTraineeNextStage", e.target.value)}
                                        className="sr-only"
                                    />
                                    {opt.label}
                                </label>
                            );
                        })}
                    </div>
                </div>

            </section>


            <div className="flex justify-end gap-3 pt-6 border-t border-gray-200">
                <button
                    type="button"
                    onClick={handleSubmit}
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

