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
    const [errors, setErrors] = useState({})
    const [evaluationId, setEvaluationId] = useState(null);
    const [submitting, setSubmitting] = useState(false);
    const [submitted, setSubmitted] = useState(false);
    const [successMessage, setSuccessMessage] = useState(null);

    const teacherEvaluationTemplate = {
        conformity: {
            description: t('evaluation.conformity.description'),
            questions: [
                t('evaluation.conformity.q1'),
                t('evaluation.conformity.q2'),
                t('evaluation.conformity.q3'),
            ]
        },
        environment: {
            description: t('evaluation.environment.description'),
            questions: [
                t('evaluation.environment.q1'),
                t('evaluation.environment.q2'),
            ]
        },
        general: {
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
        stageNumber: "",
        comments: "",
        preferredStage: "",
        capacity: "",
        sameTraineeNextStage: null,
        workShiftYesNo: null,
        workShifts: [
            { start: "", end: "" },
            { start: "", end: "" },
            { start: "", end: "" }
        ]
    });

    function normalizeYesNo(value) {
        if (value === "YES") return true;
        if (value === "NO") return false;
        return value;
    }

    const validateForm = () => {
        const newErrors = {}

        for (const [categoryKey, questions] of Object.entries(formData.categories)){
            for(let i= 0; i <questions.length; i++){
                const q = questions[i]

                if (!q.rating) {
                    if (!newErrors.categories) newErrors.categories = {};
                    if (!newErrors.categories[categoryKey]) newErrors.categories[categoryKey] = {};

                    newErrors.categories[categoryKey][i] =
                        `${t('evaluation.validation.missingRating')} - ${t(`evaluation.${categoryKey}.title`)} Q${i + 1}`;
                }
            }
        }

        if (!formData.stageNumber)
            newErrors.stageNumber = t('evaluation.validation.missingRating');

        if (!formData.preferredStage)
            newErrors.preferredStage = t('evaluation.validation.selectOption');

        if (!formData.capacity)
            newErrors.capacity = t('evaluation.validation.selectOption');

        if (!formData.sameTraineeNextStage)
            newErrors.sameTraineeNextStage = t('evaluation.validation.selectOption');

        if (!formData.workShiftYesNo)
            newErrors.workShiftYesNo = t('evaluation.validation.selectOption');

        if (formData.workShiftYesNo === "YES") {
            for (let i = 0; i < 3; i++) {
                if (!formData.workShifts[i].start || !formData.workShifts[i].end) {
                    if (!newErrors.workShifts) newErrors.workShifts = [];
                    newErrors.workShifts[i] = t('evaluation.validation.missingFields');
                }
            }
        }

        return newErrors;
    }

    useEffect(() => {
        const loadInfo = async () => {
            try {
                setLoading(true);
                const existingCheck = await checkExistingEvaluation(studentId, offerId)
                let evalId = null
                if (existingCheck.exists && existingCheck.evaluation) {
                    evalId = existingCheck.evaluation.id
                    console.log("Evaluation Id: ", evalId)
                    setEvaluationId(evalId)
                }
                const data = await getEvaluationInfo(studentId, offerId);
                setInfo(data);

                const initialCategories = {};
                Object.entries(teacherEvaluationTemplate).forEach(([key, group]) => {
                    initialCategories[key] =
                        Array.isArray(group.questions)
                            ? group.questions.map(() => "")
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
        clearFieldError(field)
    };

    const handleQuestionChange = (category, index, value) => {
        setFormData(prev => ({
            ...prev,
            categories: {
                ...prev.categories,
                [category]: prev.categories[category].map((q, i) =>
                    i === index ? {...q, rating: value}: q
                )
            }
        }));
        clearCategoryQuestionError(category, index)
    };

    const handleShiftChange = (row, field, value) => {
        const updated = [...formData.workShifts];
        updated[row][field] = value;
        setFormData(prev => ({ ...prev, workShifts: updated }));
    };

    const handleSubmit = async () => {
        setError(null)
        const normalizedForm = {
            ...formData,
            sameTraineeNextStage: normalizeYesNo(formData.sameTraineeNextStage),
            workShiftYesNo: normalizeYesNo(formData.workShiftYesNo),
            technicalTrainingSufficient: normalizeYesNo(formData.technicalTrainingSufficient),
            discussedWithTrainee: normalizeYesNo(formData.discussedWithTrainee),
        };
        const validationsErrors = validateForm();
        if(Object.keys(validationsErrors).length > 0){
            setErrors(validationsErrors)

            setTimeout(() => {
                const el = document.querySelector(".validation-error")
                if (el && typeof el.scrollIntoView === "function"){
                    el.scrollIntoView({behavior: "smooth", block: "center"})
                }
            }, 50)
            return
        }
        setErrors({})
        setSubmitting(true);

        try {
            const createRes = await createEvaluation(studentId, offerId);
            const evalId = createRes.id;
            console.log("Create response: ", createRes)

            console.log("Form Data: ", formData)
            await generateEvaluationPdfWithId(evalId, normalizedForm);
            setSuccessMessage(t("evaluation.submittedSuccess"));
            setSubmitted(true);
            setTimeout(() => {
                navigate("/dashboard/prof/evaluations")
            }, 2000)

        } catch (err) {
            console.error(err);
            setError(t('evaluation.errors.submit'));
        } finally {
            setSubmitting(false);
        }
    };


    if (loading) return <p>{t('evaluation.loading')}</p>;
    if (error && !info) return <p className="text-red-500">{error}</p>;


    return (
        <div className="max-w-4xl mx-auto p-6">

            <h1 className="text-3xl font-bold text-center mb-8">
                {t('evaluation.title')}
            </h1>

            {/* Company Info */}
            <section className="mb-8 p-4 border rounded-lg bg-gray-50 dark:bg-gray-800">
                <h2 className="text-xl font-semibold mb-4 dark:text-gray-100">{t('evaluation.companyInfo')}</h2>

                <p className="dark:text-gray-200"><strong>{t('evaluation.name')}</strong> {info?.entrepriseTeacherDto.companyName}</p>
                <p className="dark:text-gray-200"><strong>{t('evaluation.contact_person')}</strong> {info?.entrepriseTeacherDto.contactName}</p>
                <p className="dark:text-gray-200"><strong>{t('evaluation.address')}</strong> {info?.entrepriseTeacherDto.address}</p>
                <p className="dark:text-gray-200"><strong>{t('evaluation.email')}</strong> {info?.entrepriseTeacherDto.email}</p>
            </section>

            {/* Student Info */}
            <section className="mb-8 p-4 border rounded-lg bg-gray-50 dark:bg-gray-800 dark:border-gray-700">
                <h2 className="text-xl font-semibold mb-4 dark:text-gray-100">{t('evaluation.studentInfo')}</h2>
                <p className="dark:text-gray-200"><strong>{t('evaluation.name')}</strong> {info?.studentTeacherDto.fullname}</p>
                <p className="dark:text-gray-200"><strong>{t('evaluation.internStartDate')}</strong> {info?.studentTeacherDto.internshipStartDate}</p>

                {/* Stage number selection */}
                <div className="mt-3">
                    <label className="font-medium block mb-2 text-center dark:text-gray-100">{t("evaluation.intern")}</label>
                    {errors?.stageNumber && (
                        <p className="validation-error text-sm text-red-600 mb-2 text-center">
                            {errors.stageNumber}
                        </p>
                    )}

                    <div className="flex justify-center gap-4">
                        {[1, 2].map(val => {
                            const strVal = val.toString();
                            const isSelected = formData.stageNumber === strVal;
                            return (
                                <label
                                    key={strVal}
                                    className={`flex items-center px-4 py-2 rounded-lg cursor-pointer transition-all
                                        bg-blue-200 dark:bg-blue-700 border-2 border-blue-400 dark:border-blue-500 text-blue-900 dark:text-blue-100 font-semibold
                                        ${isSelected ? "border-blue-600 dark:border-blue-300 ring-2 ring-blue-400 dark:ring-blue-500 ring-offset-2 shadow-md" : ""}
                                    `}
                                >
                                    <input
                                        type="radio"
                                        name="stageNumber"
                                        value={strVal}
                                        checked={isSelected}
                                        onChange={e => handleChange("stageNumber", e.target.value)}
                                        className="mr-2"
                                    />
                                    {strVal}
                                </label>
                            );
                        })}
                    </div>
                </div>
            </section>

            {/* Question Groups */}
            {Object.entries(teacherEvaluationTemplate).map(([groupKey, group]) => (
                <section key={groupKey} className="mb-8 p-4 border rounded-lg dark:bg-gray-800 dark:border-gray-700">

                    <h3 className="text-lg font-semibold mb-4 dark:text-gray-100">{t(`evaluation.${groupKey}.title`)}</h3>

                    {group.questions.map((q, index) => {
                        const ratingErrors = errors?.categories?.[groupKey]?.[index]

                        return (
                            <div key={index} className="mb-6">

                                <p className="mb-3 dark:text-gray-200">{q}</p>

                                {ratingErrors && (
                                    <p className="validation-error text-sm text-red-600 mb-2">{ratingErrors}</p>
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
                                                    ${option.value === "EXCELLENT" ? "bg-green-400 dark:bg-green-600 border-green-600 dark:border-green-500 dark:text-white" : ""}
                                                    ${option.value === "TRES_BIEN" ? "bg-green-200 dark:bg-green-700 border-green-400 dark:border-green-600 dark:text-white" : ""}
                                                    ${option.value === "SATISFAISANT" ? "bg-orange-200 dark:bg-orange-700 border-orange-400 dark:border-orange-600 dark:text-white" : ""}
                                                    ${option.value === "A_AMELIORER" ? "bg-red-400 dark:bg-red-600 border-red-600 dark:border-red-500 dark:text-white" : ""}
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
            <section className="mb-8 p-4 border rounded-lg bg-gray-50 dark:bg-gray-800 dark:border-gray-700">
                <h3 className="text-lg font-semibold mb-4 dark:text-gray-100">{t("evaluation.observations.title")}</h3>

                {/* Preferred stage */}
                <div className="mb-6">
                    {errors?.preferredStage && (
                        <p className="validation-error text-sm text-red-600 mb-2 text-center">
                            {errors.preferredStage}
                        </p>
                    )}
                    <p className="font-medium mb-2 dark:text-gray-200">{t("evaluation.observations.q1")}</p>
                    <div className="flex flex-wrap justify-center gap-3">
                        {[{ value: "1", label: t('evaluation.observations.first_intern')},
                            { value: "2", label: t('evaluation.observations.second_intern') }].map(
                            opt => {
                                const isSelected = formData.preferredStage === opt.value;
                                return (
                                    <label
                                        key={opt.value}
                                        className={`flex items-center px-4 py-2 rounded-lg cursor-pointer transition-all
                                            bg-blue-100 dark:bg-blue-800 border-2 border-blue-300 dark:border-blue-600 text-blue-900 dark:text-blue-100 font-medium
                                            ${isSelected ? "border-blue-600 dark:border-blue-400 ring-2 ring-blue-400 dark:ring-blue-500 ring-offset-2 shadow-md" : ""}
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
                        <p className="validation-error text-sm text-red-600 mb-2 text-center">
                            {errors.capacity}
                        </p>
                    )}
                    <p className="font-medium mb-2 dark:text-gray-200">{t("evaluation.observations.q2")}</p>
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
                                        bg-purple-100 dark:bg-purple-800 border-2 border-purple-300 dark:border-purple-600 text-purple-900 dark:text-purple-100 font-medium
                                        ${isSelected ? "border-purple-700 dark:border-purple-400 ring-2 ring-purple-400 dark:ring-purple-500 ring-offset-2 shadow-md" : ""}
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
                        <p className="validation-error text-sm text-red-600 mb-2 text-center">
                            {errors.sameTraineeNextStage}
                        </p>
                    )}
                    <p className="font-medium mb-2 dark:text-gray-200">{t("evaluation.observations.q3")}</p>
                    <div className="flex justify-center gap-3">
                        {[{ value: "YES", label: t('evaluation.observations.yes') }, { value: "NO", label: t('evaluation.observations.no') }].map(opt => {
                            const isSelected = formData.sameTraineeNextStage === opt.value;
                            return (
                                <label
                                    key={opt.value}
                                    className={`flex items-center px-4 py-2 rounded-lg cursor-pointer transition-all
                                        bg-green-100 dark:bg-green-800 border-2 border-green-300 dark:border-green-600 text-green-900 dark:text-green-100 font-medium
                                        ${isSelected ? "border-green-700 dark:border-green-400 ring-2 ring-green-400 dark:ring-green-500 ring-offset-2 shadow-md" : ""}
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

                {/* Variable shifts */}
                <div className="mb-6">
                    {errors?.workShiftYesNo && (
                        <p className="validation-error text-sm text-red-600 mb-2 text-center">
                            {errors.workShiftYesNo}
                        </p>
                    )}
                    <p className="font-medium mb-2 dark:text-gray-200">{t("evaluation.observations.q4")}</p>
                    <div className="flex gap-3 justify-center">
                        {[{ value: "YES", label: t('evaluation.observations.yes') }, { value: "NO", label: t('evaluation.observations.no') }].map(opt => {
                            const isSelected = formData.workShiftYesNo === opt.value;
                            return (
                                <label
                                    key={opt.value}
                                    className={`flex items-center px-4 py-2 rounded-lg cursor-pointer transition-all
                                        bg-orange-100 dark:bg-orange-800 border-2 border-orange-300 dark:border-orange-600 text-orange-900 dark:text-orange-100 font-medium
                                        ${isSelected ? "border-orange-700 dark:border-orange-400 ring-2 ring-orange-400 dark:ring-orange-500 ring-offset-2 shadow-md" : ""}
                                    `}
                                >
                                    <input
                                        type="radio"
                                        name="obs_variableShifts"
                                        value={opt.value}
                                        checked={isSelected}
                                        onChange={e => handleChange("workShiftYesNo", e.target.value)}
                                        className="mr-2"
                                    />
                                    {opt.label}
                                </label>
                            );
                        })}
                    </div>

                    {formData.workShiftYesNo === "YES" && (
                        <div className="validation-error mt-4 space-y-3">
                            {errors?.workShiftYesNo && (
                                <p className="text-sm text-red-600 mb-2 text-center">
                                    {errors.workShiftYesNo}
                                </p>
                            )}
                            {[1, 2, 3].map(n => {
                                const errorMsg = errors?.workShifts?.[n - 1];
                                return (
                                    <div key={n} className="flex flex-col items-center gap-1">

                                        <div className="flex gap-3 items-center justify-center">
                                            <span className="dark:text-gray-200">{t('evaluation.observations.from')}</span>
                                            <input
                                                type="time"
                                                className="p-2 border rounded dark:bg-gray-700 dark:border-gray-600 dark:text-gray-100"
                                                value={formData[`obs_shift${n}From`] || ""}
                                                onChange={e => handleChange(`obs_shift${n}From`, e.target.value)}
                                            />
                                            <span className="dark:text-gray-200">{t('evaluation.observations.to')}</span>
                                            <input
                                                type="time"
                                                className="p-2 border rounded dark:bg-gray-700 dark:border-gray-600 dark:text-gray-100"
                                                value={formData[`obs_shift${n}To`] || ""}
                                                onChange={e => handleChange(`obs_shift${n}To`, e.target.value)}
                                            />
                                        </div>

                                        {errorMsg && (
                                            <p className="text-sm text-red-600 text-center">{errorMsg}</p>
                                        )}

                                    </div>
                                );
                            })}
                        </div>
                    )}
                </div>
            </section>

            <div className="flex justify-end gap-3 pt-6 border-t border-gray-200 dark:border-gray-700">
                <button
                    type="button"
                    onClick={handleSubmit}
                    disabled={submitting || submitted}
                    className="px-6 py-2.5 bg-blue-600 dark:bg-blue-700 text-white font-medium rounded-md hover:bg-blue-700 dark:hover:bg-blue-600 focus:outline-none focus:ring-2 focus:ring-blue-500 focus:ring-offset-2 disabled:opacity-50 disabled:cursor-not-allowed transition-colors"
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