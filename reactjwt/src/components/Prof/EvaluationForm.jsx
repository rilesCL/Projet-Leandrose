import React, { useEffect, useState } from "react";
import { useParams, useNavigate } from "react-router-dom";
import {
    getEvaluationInfo,
    createEvaluation,
    generateEvaluationPdfWithId,
    checkExistingEvaluation
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
    const [evaluationId, setEvaluationId] = useState(null);
    const [submitting, setSubmitting] = useState(false);
    const [submitted, setSubmitted] = useState(false);
    const [successMessage, setSuccessMessage] = useState(null);

    const teacherEvaluationTemplate = {
        conformity: {
            title: t('evaluation.conformity.title'),
            description: t('evaluation.conformity.description'),
            questions: [
                t('evaluation.conformity.q1'),
                t('evaluation.conformity.q2'),
                t('evaluation.conformity.q3'),
            ]
        },
        environment: {
            title: t('evaluation.environment.title'),
            description: t('evaluation.environment.description'),
            questions: [
                t('evaluation.environment.q1'),
                t('evaluation.environment.q2'),
            ]
        },
        general: {
            title: t('evaluation.general.title'),
            description: t('evaluation.general.description'),
            questions: [
                t('evaluation.general.q1'),
                t('evaluation.general.q2'),
                t('evaluation.general.q3'),
                t('evaluation.general.q4'),
                t('evaluation.general.q5'),
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
                        `${t('evaluation.validation.missingRating')} - ${t(`evaluation.${categoryKey}.title`)} Q${i + 1}`;
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

                const existingCheck = await checkExistingEvaluation(studentId, offerId);
                if (existingCheck.exists && existingCheck.evaluation) {
                    setEvaluationId(existingCheck.evaluation.id);
                }

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
            setSuccessMessage(t("evaluation.submittedSuccess"));

            setTimeout(() => {
                navigate("/dashboard/prof?tab=evaluations");
            }, 2000);

        } catch (err) {
            console.error(err);
            setError(t('evaluation.errors.submit'));
        } finally {
            setSubmitting(false);
        }
    };

    if (loading) return <p className="text-gray-900 dark:text-gray-100">{t('evaluation.loading')}</p>;
    if (error && !info) return <p className="text-red-500 dark:text-red-400">{error}</p>;

    return (
        <div className="max-w-4xl mx-auto p-6">

            <h1 className="text-3xl font-bold text-center mb-8 text-gray-900 dark:text-gray-100">
                {t('evaluation.title')}
            </h1>

            {/* Company Info */}
            <section className="mb-8 p-4 border dark:border-gray-700 rounded-lg bg-gray-50 dark:bg-gray-800">
                <h2 className="text-xl font-semibold mb-4 text-gray-900 dark:text-gray-100">{t('evaluation.companyInfo')}</h2>

                <p className="text-gray-700 dark:text-gray-300"><strong className="text-gray-900 dark:text-gray-100">{t('evaluation.name')}</strong> {info?.entrepriseTeacherDto.companyName}</p>
                <p className="text-gray-700 dark:text-gray-300"><strong className="text-gray-900 dark:text-gray-100">{t('evaluation.contact_person')}</strong> {info?.entrepriseTeacherDto.contactName}</p>
                <p className="text-gray-700 dark:text-gray-300"><strong className="text-gray-900 dark:text-gray-100">{t('evaluation.address')}</strong> {info?.entrepriseTeacherDto.address}</p>
                <p className="text-gray-700 dark:text-gray-300"><strong className="text-gray-900 dark:text-gray-100">{t('evaluation.email')}</strong> {info?.entrepriseTeacherDto.email}</p>
            </section>

            {/* Student Info */}
            <section className="mb-8 p-4 border dark:border-gray-700 rounded-lg bg-gray-50 dark:bg-gray-800">
                <h2 className="text-xl font-semibold mb-4 text-gray-900 dark:text-gray-100">{t('evaluation.studentInfo')}</h2>
                <p className="text-gray-700 dark:text-gray-300"><strong className="text-gray-900 dark:text-gray-100">{t('evaluation.name')}</strong> {info?.studentTeacherDto.fullname}</p>
                <p className="text-gray-700 dark:text-gray-300"><strong className="text-gray-900 dark:text-gray-100">{t('evaluation.internStartDate')}</strong> {info?.studentTeacherDto.internshipStartDate}</p>
            </section>

            {/* Question Groups */}
            {Object.entries(teacherEvaluationTemplate).map(([groupKey, group]) => (
                <section key={groupKey} className="mb-8 p-4 border dark:border-gray-700 rounded-lg bg-white dark:bg-gray-800">

                    <h3 className="text-lg font-semibold mb-4 text-gray-900 dark:text-gray-100">{t(`evaluation.${groupKey}.title`)}</h3>

                    {group.questions.map((q, index) => {
                        const ratingErrors = errors?.categories?.[groupKey]?.[index]

                        return (
                            <div key={index} className="mb-6">

                                <p className="mb-3 text-gray-700 dark:text-gray-300">{q}</p>

                                {ratingErrors && (
                                    <p className="validation-error text-sm text-red-600 dark:text-red-400 mb-2">{ratingErrors}</p>
                                )}
                                {/* Rating Buttons */}
                                <div className="flex flex-wrap justify-center gap-3">
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
                                                className={`
                                                    flex items-center px-4 py-2 rounded-lg cursor-pointer transition-all
                                                    border-2 font-semibold
                                                    ${option.value === "EXCELLENT" ? "bg-green-400 dark:bg-green-600 border-green-600 dark:border-green-500 text-gray-900 dark:text-gray-100" : ""}
                                                    ${option.value === "TRES_BIEN" ? "bg-green-200 dark:bg-green-700 border-green-400 dark:border-green-500 text-gray-900 dark:text-gray-100" : ""}
                                                    ${option.value === "SATISFAISANT" ? "bg-orange-200 dark:bg-orange-700 border-orange-400 dark:border-orange-500 text-gray-900 dark:text-gray-100" : ""}
                                                    ${option.value === "A_AMELIORER" ? "bg-red-400 dark:bg-red-600 border-red-600 dark:border-red-500 text-gray-900 dark:text-gray-100" : ""}
                                                    ${isSelected ? "ring-2 ring-black dark:ring-white shadow-lg" : ""}
                                                `}
                                            >
                                                <input
                                                    type="radio"
                                                    name={`${groupKey}-${index}`}
                                                    value={option.value}
                                                    checked={isSelected}
                                                    onChange={() =>
                                                        handleQuestionChange(groupKey, index, option.value)
                                                    }
                                                    className="mr-2"
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

            {/* OBSERVATIONS GÉNÉRALES */}
            <section className="mb-8 p-4 border dark:border-gray-700 rounded-lg bg-gray-50 dark:bg-gray-800">
                <h3 className="text-lg font-semibold mb-4 text-gray-900 dark:text-gray-100">{t("evaluation.observations.title")}</h3>

                {/* Preferred stage */}
                <div className="mb-6">
                    {errors?.preferredStage && (
                        <p className="validation-error text-sm text-red-600 dark:text-red-400 mb-2 text-center">
                            {errors.preferredStage}
                        </p>
                    )}
                    <p className="font-medium mb-2 text-gray-700 dark:text-gray-300">{t("evaluation.observations.q1")}</p>
                    <div className="flex flex-wrap justify-center gap-3">
                        {[{ value: "1", label: t('evaluation.observations.first_intern')},
                            { value: "2", label: t('evaluation.observations.second_intern') }].map(
                            opt => {
                                const isSelected = formData.preferredStage === opt.value;
                                return (
                                    <label
                                        key={opt.value}
                                        className={`flex items-center px-4 py-2 rounded-lg cursor-pointer transition-all
                                            bg-blue-100 dark:bg-blue-900/30 border-2 border-blue-300 dark:border-blue-600 text-blue-900 dark:text-blue-200 font-medium
                                            ${isSelected ? "border-blue-600 dark:border-blue-400 ring-2 ring-blue-400 dark:ring-blue-500 ring-offset-2 dark:ring-offset-gray-800 shadow-md" : ""}
                                        `}
                                    >
                                        <input
                                            type="radio"
                                            name="obs_stagePreference"
                                            value={opt.value}
                                            checked={isSelected}
                                            onChange={e => handleChange("preferredStage", e.target.value)}
                                            className="mr-2"
                                        />
                                        {opt.label}
                                    </label>
                                );
                            }
                        )}
                    </div>
                </div>

                {/* Capacity */}
                <div className="mb-6">
                    {errors?.capacity && (
                        <p className="validation-error text-sm text-red-600 dark:text-red-400 mb-2 text-center">
                            {errors.capacity}
                        </p>
                    )}
                    <p className="font-medium mb-2 text-gray-700 dark:text-gray-300">{t("evaluation.observations.q2")}</p>
                    <div className="flex flex-wrap justify-center gap-3">
                        {[
                            { value: "1", label: t('evaluation.observations.stage1') },
                            { value: "2", label: t('evaluation.observations.stage2') },
                            { value: "3", label: t('evaluation.observations.stage3') },
                            { value: "4", label: t('evaluation.observations.stage4') }
                        ].map(opt => {
                            const isSelected = formData.capacity === opt.value;
                            return (
                                <label
                                    key={opt.value}
                                    className={`flex items-center px-4 py-2 rounded-lg cursor-pointer transition-all
                                        bg-purple-100 dark:bg-purple-900/30 border-2 border-purple-300 dark:border-purple-600 text-purple-900 dark:text-purple-200 font-medium
                                        ${isSelected ? "border-purple-700 dark:border-purple-400 ring-2 ring-purple-400 dark:ring-purple-500 ring-offset-2 dark:ring-offset-gray-800 shadow-md" : ""}
                                    `}
                                >
                                    <input
                                        type="radio"
                                        name="obs_openToHosting"
                                        value={opt.value}
                                        checked={isSelected}
                                        onChange={e => handleChange("capacity", e.target.value)}
                                        className="mr-2"
                                    />
                                    {opt.label}
                                </label>
                            );
                        })}
                    </div>
                </div>

                {/* Same trainee again */}
                <div className="mb-6">
                    {errors?.sameTraineeNextStage && (
                        <p className="validation-error text-sm text-red-600 dark:text-red-400 mb-2 text-center">
                            {errors.sameTraineeNextStage}
                        </p>
                    )}
                    <p className="font-medium mb-2 text-gray-700 dark:text-gray-300">{t("evaluation.observations.q3")}</p>
                    <div className="flex justify-center gap-3">
                        {[{ value: "YES", label: t('evaluation.observations.yes') }, { value: "NO", label: t('evaluation.observations.no') }].map(opt => {
                            const isSelected = formData.sameTraineeNextStage === opt.value;
                            return (
                                <label
                                    key={opt.value}
                                    className={`flex items-center px-4 py-2 rounded-lg cursor-pointer transition-all
                                        bg-green-100 dark:bg-green-900/30 border-2 border-green-300 dark:border-green-600 text-green-900 dark:text-green-200 font-medium
                                        ${isSelected ? "border-green-700 dark:border-green-400 ring-2 ring-green-400 dark:ring-green-500 ring-offset-2 dark:ring-offset-gray-800 shadow-md" : ""}
                                    `}
                                >
                                    <input
                                        type="radio"
                                        name="obs_sameInternAgain"
                                        value={opt.value}
                                        checked={isSelected}
                                        onChange={e => handleChange("sameTraineeNextStage", e.target.value)}
                                        className="mr-2"
                                    />
                                    {opt.label}
                                </label>
                            );
                        })}
                    </div>
                </div>

            </section>

            {successMessage && (
                <div className="bg-green-50 dark:bg-green-900/30 border border-green-200 dark:border-green-700 rounded-lg p-4">
                    <div className="flex items-center">
                        <div className="flex-shrink-0">
                            <svg className="h-5 w-5 text-green-400 dark:text-green-500" viewBox="0 0 20 20" fill="currentColor">
                                <circle cx="10" cy="10" r="8" />
                                <polyline points="7,10 9,12 13,8" fill="none" stroke="white" strokeWidth="2" strokeLinecap="round" strokeLinejoin="round" />
                            </svg>
                        </div>
                        <div className="ml-3">
                            <p className="text-sm font-medium text-green-800 dark:text-green-200">
                                {successMessage}
                            </p>
                        </div>
                    </div>
                </div>
            )}

            <div className="flex justify-end gap-3 pt-6 border-t border-gray-200 dark:border-gray-700">
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

        </div>
    );
};

export default EvaluationForm;