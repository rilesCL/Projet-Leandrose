import React, { useEffect, useState } from "react";
import { useParams, useNavigate } from "react-router-dom";
import { getEvaluationInfo, createEvaluation, generateEvaluationPdfWithId } from "../../api/apiProf";
import { useTranslation } from 'react-i18next';

const EvaluationForm = () => {

    const { t } = useTranslation();
    const { studentId, offerId } = useParams();
    const navigate = useNavigate();

    const [info, setInfo] = useState(null);
    const [loading, setLoading] = useState(true);
    const [error, setError] = useState(null);
    const [submitting, setSubmitting] = useState(false);

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
        hoursMonth1: "",
        hoursMonth2: "",
        hoursMonth3: "",
        salary: "",
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


    const handleChange = (field, value) => {
        setFormData(prev => ({ ...prev, [field]: value }));
    };

    const handleQuestionChange = (category, index, value) => {
        setFormData(prev => ({
            ...prev,
            categories: {
                ...prev.categories,
                [category]: prev.categories[category].map((q, i) =>
                    i === index ? value : q
                )
            }
        }));
    };

    const handleShiftChange = (row, field, value) => {
        const updated = [...formData.workShifts];
        updated[row][field] = value;
        setFormData(prev => ({ ...prev, workShifts: updated }));
    };

    const handleSubmit = async () => {
        try {
            setSubmitting(true);

            const createRes = await createEvaluation(studentId, offerId);
            const evalId = createRes.evaluationId;

            await generateEvaluationPdfWithId(evalId, formData);

            navigate("/dashboard/prof/evaluations");

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
            <section className="mb-8 p-4 border rounded-lg bg-gray-50">
                <h2 className="text-xl font-semibold mb-4">{t('evaluation.companyInfo')}</h2>

                <p><strong>{t('evaluation.name')}</strong> {info?.entrepriseTeacherDto.companyName}</p>
                <p><strong>{t('evaluation.contact_person')}</strong> {info?.entrepriseTeacherDto.contactName}</p>
                <p><strong>{t('evaluation.address')}</strong> {info?.entrepriseTeacherDto.address}</p>
                <p><strong>{t('evaluation.email')}</strong> {info?.entrepriseTeacherDto.email}</p>
            </section>

            {/* Student Info */}
            <section className="mb-8 p-4 border rounded-lg bg-gray-50">
                <h2 className="text-xl font-semibold mb-4">{t('evaluation.studentInfo')}</h2>
                <p><strong>{t('evaluation.name')}</strong> {info?.studentTeacherDto.fullname}</p>
                <p><strong>{t('evaluation.internStartDate')}</strong> {info?.studentTeacherDto.internshipStartDate}</p>

                {/* Stage number selection */}
                <div className="mt-3">
                    <label className="font-medium block mb-2 text-center">{t("evaluation.intern")}</label>
                    <div className="flex justify-center gap-4">
                        {[1, 2].map(val => {
                            const strVal = val.toString();
                            const isSelected = formData.stageNumber === strVal;
                            return (
                                <label
                                    key={strVal}
                                    className={`flex items-center px-4 py-2 rounded-lg cursor-pointer transition-all
                                        bg-blue-200 border-2 border-blue-400 text-blue-900 font-semibold
                                        ${isSelected ? "border-blue-600 ring-2 ring-blue-400 ring-offset-2 shadow-md" : ""}
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
                <section key={groupKey} className="mb-8 p-4 border rounded-lg">

                    <h3 className="text-lg font-semibold mb-4">{t(`evaluation.${groupKey}.title`)}</h3>

                    {group.questions.map((q, index) => {
                        const isAnswered = formData.categories[groupKey]?.[index] || "";

                        return (
                            <div key={index} className="mb-6">

                                <p className="mb-3">{q}</p>

                                {/* Rating Buttons */}
                                <div className="flex flex-wrap justify-center gap-3">
                                    {[
                                        { value: "EXCELLENT", label: t('evaluation.rating.totally_agree') },
                                        { value: "TRES_BIEN", label: t('evaluation.rating.mostly_agree') },
                                        { value: "SATISFAISANT", label: t('evaluation.rating.mostly_disagree') },
                                        { value: "A_AMELIORER", label: t('evaluation.rating.totally_disagree') }
                                    ].map(option => {
                                        const isSelected =
                                            formData.categories[groupKey]?.[index] === option.value;

                                        return (
                                            <label
                                                key={option.value}
                                                className={`
                                                    flex items-center px-4 py-2 rounded-lg cursor-pointer transition-all
                                                    border-2 font-semibold
                                                    ${option.value === "EXCELLENT" ? "bg-green-400 border-green-600" : ""}
                                                    ${option.value === "TRES_BIEN" ? "bg-green-200 border-green-400" : ""}
                                                    ${option.value === "SATISFAISANT" ? "bg-orange-200 border-orange-400" : ""}
                                                    ${option.value === "A_AMELIORER" ? "bg-red-400 border-red-600" : ""}
                                                    ${isSelected ? "ring-2 ring-black shadow-lg" : ""}
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

                                {/* Salary field (general, question 2) */}
                                {groupKey === "general" && index === 1 && (
                                    <div className="mt-3 flex justify-center">
                                        <input
                                            type="text"
                                            placeholder={t('evaluation.placeholders.salary')}
                                            className="border px-2 py-1 rounded"
                                            value={formData.salary}
                                            onChange={e => handleChange("salary", e.target.value)}
                                        />
                                    </div>
                                )}

                            </div>
                        );
                    })}

                    {/* Hours for conformity */}
                    {groupKey === "conformity" && (
                        <div className="mt-6 flex flex-col gap-3 items-center">
                            <label className="font-medium">{t('evaluation.placeholders.nbHoursWeek')}</label>
                            <div className="flex gap-4">
                                <input className="border p-1 rounded" placeholder={t('evaluation.placeholders.first_month')}
                                       value={formData.hoursMonth1}
                                       onChange={e => handleChange("hoursMonth1", e.target.value)} />
                                <input className="border p-1 rounded" placeholder={t('evaluation.placeholders.second_month')}
                                       value={formData.hoursMonth2}
                                       onChange={e => handleChange("hoursMonth2", e.target.value)} />
                                <input className="border p-1 rounded" placeholder={t('evaluation.placeholders.third_month')}
                                       value={formData.hoursMonth3}
                                       onChange={e => handleChange("hoursMonth3", e.target.value)} />
                            </div>
                        </div>
                    )}

                </section>
            ))}

            {/* OBSERVATIONS GÉNÉRALES */}
            <section className="mb-8 p-4 border rounded-lg bg-gray-50">
                <h3 className="text-lg font-semibold mb-4">{t("evaluation.observations.title")}</h3>

                {/* Preferred stage */}
                <div className="mb-6">
                    <p className="font-medium mb-2">{t("evaluation.observations.q1")}</p>
                    <div className="flex flex-wrap justify-center gap-3">
                        {[{ value: "1", label: t('evaluation.observations.first_intern')},
                            { value: "2", label: t('evaluation.observations.second_intern') }].map(
                            opt => {
                                const isSelected = formData.preferredStage === opt.value;
                                return (
                                    <label
                                        key={opt.value}
                                        className={`flex items-center px-4 py-2 rounded-lg cursor-pointer transition-all
                                            bg-blue-100 border-2 border-blue-300 text-blue-900 font-medium
                                            ${isSelected ? "border-blue-600 ring-2 ring-blue-400 ring-offset-2 shadow-md" : ""}
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
                    <p className="font-medium mb-2">{t("evaluation.observations.q2")}</p>
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
                                        bg-purple-100 border-2 border-purple-300 text-purple-900 font-medium
                                        ${isSelected ? "border-purple-700 ring-2 ring-purple-400 ring-offset-2 shadow-md" : ""}
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
                    <p className="font-medium mb-2">{t("evaluation.observations.q3")}</p>
                    <div className="flex justify-center gap-3">
                        {[{ value: "YES", label: t('evaluation.observations.yes') }, { value: "NO", label: t('evaluation.observations.no') }].map(opt => {
                            const isSelected = formData.sameTraineeNextStage === opt.value;
                            return (
                                <label
                                    key={opt.value}
                                    className={`flex items-center px-4 py-2 rounded-lg cursor-pointer transition-all
                                        bg-green-100 border-2 border-green-300 text-green-900 font-medium
                                        ${isSelected ? "border-green-700 ring-2 ring-green-400 ring-offset-2 shadow-md" : ""}
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
                    <p className="font-medium mb-2">{t("evaluation.observations.q3")}</p>
                    <div className="flex gap-3 justify-center">
                        {[{ value: "YES", label: t('evaluation.observations.yes') }, { value: "NO", label: t('evaluation.observations.no') }].map(opt => {
                            const isSelected = formData.workShiftYesNo === opt.value;
                            return (
                                <label
                                    key={opt.value}
                                    className={`flex items-center px-4 py-2 rounded-lg cursor-pointer transition-all
                                        bg-orange-100 border-2 border-orange-300 text-orange-900 font-medium
                                        ${isSelected ? "border-orange-700 ring-2 ring-orange-400 ring-offset-2 shadow-md" : ""}
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
                        <div className="mt-4 space-y-3">
                            {[1, 2, 3].map(n => (
                                <div key={n} className="flex gap-3 items-center justify-center">
                                    <span>{t('evaluation.observations.from')}</span>
                                    <input
                                        type="time"
                                        className="p-2 border rounded"
                                        value={formData[`obs_shift${n}From`] || ""}
                                        onChange={e => handleChange(`obs_shift${n}From`, e.target.value)}
                                    />
                                    <span>{t('evaluation.observations.to')}</span>
                                    <input
                                        type="time"
                                        className="p-2 border rounded"
                                        value={formData[`obs_shift${n}To`] || ""}
                                        onChange={e => handleChange(`obs_shift${n}To`, e.target.value)}
                                    />
                                </div>
                            ))}
                        </div>
                    )}
                </div>
            </section>

            {/* Submit */}
            <button
                onClick={handleSubmit}
                disabled={submitting}
                className="px-6 py-3 bg-blue-600 text-white rounded-lg hover:bg-blue-700"
            >
                {submitting ? "Soumission..." : "Soumettre l’évaluation"}
            </button>

            {error && <p className="text-red-500 mt-4">{error}</p>}

        </div>
    );
};

export default EvaluationForm;
