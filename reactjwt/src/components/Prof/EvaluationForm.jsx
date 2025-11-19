import React, { useEffect, useState } from "react";
import { useParams, useNavigate } from "react-router-dom";
import { getEvaluationInfo, createEvaluation, generateEvaluationPdfWithId } from "../../api/apiProf";

const EvaluationForm = () => {


    const { studentId, offerId } = useParams();
    const navigate = useNavigate();

    const [info, setInfo] = useState(null);
    const [loading, setLoading] = useState(true);
    const [error, setError] = useState(null);
    const [submitting, setSubmitting] = useState(false);


    const teacherEvaluationTemplate = {
        conformity: [
            "Les tâches confiées au stagiaire sont conformes aux tâches annoncées.",
            "Des mesures d’accueil facilitent l’intégration du stagiaire.",
            "Le temps consacré à l’encadrement est suffisant."
        ],
        environment: [
            "L’environnement respecte les normes d’hygiène et de sécurité.",
            "Le climat de travail est agréable."
        ],
        general: [
            "Le milieu de stage est accessible en transport en commun.",
            "Le salaire offert est intéressant pour le stagiaire.",
            "La communication avec le superviseur facilite le bon déroulement.",
            "L’équipement fourni est adéquat.",
            "Le volume de travail est acceptable."
        ]
    };

    const [formData, setFormData] = useState({
        stageNumber: "",
        hoursMonth1: "",
        hoursMonth2: "",
        hoursMonth3: "",
        salary: "",
        questions: {},
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

                // Initialize questions object
                const initialQuestions = {};
                Object.entries(teacherEvaluationTemplate).forEach(([groupKey, questions]) => {
                    initialQuestions[groupKey] = questions.map(() => "");
                });

                setFormData(prev => ({
                    ...prev,
                    questions: initialQuestions
                }));

            } catch (err) {
                console.error(err);
                setError("Erreur de chargement du formulaire");
            } finally {
                setLoading(false);
            }
        };

        loadInfo();
    }, [studentId, offerId]);

    // --------------------
    // 🔹 Handlers
    // --------------------
    const handleChange = (field, value) => {
        setFormData(prev => ({ ...prev, [field]: value }));
    };

    const handleQuestionChange = (group, index, value) => {
        setFormData(prev => ({
            ...prev,
            questions: {
                ...prev.questions,
                [group]: prev.questions[group].map((v, i) => (i === index ? value : v))
            }
        }));
    };

    const handleShiftChange = (row, field, value) => {
        const updated = [...formData.workShifts];
        updated[row][field] = value;
        setFormData(prev => ({ ...prev, workShifts: updated }));
    };

    // --------------------
    // 🔹 Submit Form
    // --------------------
    const handleSubmit = async () => {
        try {
            setSubmitting(true);

            const createRes = await createEvaluation(studentId, offerId);
            const evalId = createRes.evaluationId;

            await generateEvaluationPdfWithId(evalId, formData);

            navigate("/dashboard/prof/evaluations");

        } catch (err) {
            console.error(err);
            setError("Erreur lors de la soumission.");
        } finally {
            setSubmitting(false);
        }
    };

    // --------------------
    // 🔹 Rendering
    // --------------------
    if (loading) return <p>Chargement...</p>;
    if (error && !info) return <p className="text-red-500">{error}</p>;


    return (
        <div className="max-w-4xl mx-auto p-6">

            <h1 className="text-3xl font-bold text-center mb-8">
                ÉVALUATION DU MILIEU DE STAGE
            </h1>

            {/* ------------------------------
                Identification Entreprise
            --------------------------------*/}
            <section className="mb-8 p-4 border rounded-lg bg-gray-50">
                <h2 className="text-xl font-semibold mb-4">Identification de l'entreprise</h2>

                <p><strong>Nom :</strong> {info?.entrepriseTeacherDto.companyName}</p>
                <p><strong>Personne contact :</strong> {info?.entrepriseTeacherDto.contactName}</p>
                <p><strong>Adresse :</strong> {info?.entrepriseTeacherDto.address}</p>
                <p><strong>Email :</strong> {info?.entrepriseTeacherDto.email}</p>
            </section>

            {/* ------------------------------
                IDENTIFICATION DU STAGIAIRE
            --------------------------------*/}
            <section className="mb-8 p-4 border rounded-lg bg-gray-50">
                <h2 className="text-xl font-semibold mb-4">Identification du stagiaire</h2>
                <p><strong>Nom :</strong> {info?.studentTeacherDto.fullname}</p>
                <p><strong>Date du stage :</strong> {info?.studentTeacherDto.internshipStartDate}</p>

                <div className="mt-3">
                    <label className="font-medium block mb-2 text-center">Stage :</label>
                        <div className="flex justify-center gap-4">
                            {[
                            {
                                value: "1",
                                label: "1",
                                baseClasses: "bg-blue-200 border-2 border-blue-400 text-blue-900 font-semibold hover:border-blue-500 hover:bg-blue-300/80",
                                selectedClasses: "border-[3px] border-blue-600 ring-2 ring-blue-400 ring-offset-2 shadow-md",
                                inputRing: "focus:ring-blue-500"
                            },
                            {
                                value: "2",
                                label: "2",
                                baseClasses: "bg-blue-200 border-2 border-blue-400 text-blue-900 font-semibold hover:border-blue-500 hover:bg-blue-300/80",
                                selectedClasses: "border-[3px] border-blue-600 ring-2 ring-blue-400 ring-offset-2 shadow-md",
                                inputRing: "focus:ring-blue-500"
                            }
                        ].map(option => {
                            const isSelected = formData.stageNumber === option.value;

                            return (
                                <label
                                    key={option.value}
                                    className={`flex items-center px-4 py-2 rounded-lg cursor-pointer transition-all
                        ${option.baseClasses}
                        ${isSelected ? option.selectedClasses : ""}
                    `}
                                >
                                    <input
                                        type="radio"
                                        name="stageNumber"
                                        value={option.value}
                                        checked={isSelected}
                                        onChange={e => handleChange("stageNumber", e.target.value)}
                                        className={`mr-2 focus:ring-2 ${option.inputRing}`}
                                    />
                                    <span className="text-sm font-medium">{option.label}</span>
                                </label>
                            );
                        })}
                    </div>
                </div>
            </section>

            {Object.entries(teacherEvaluationTemplate).map(([groupKey, questions]) => (
                <section key={groupKey} className="mb-8 p-4 border rounded-lg">

                    <h3 className="text-lg font-semibold mb-4">
                        {groupKey.toUpperCase()}
                    </h3>

                    {questions.map((q, index) => (
                        <div key={index} className="mb-6">

                            <p className="mb-3">{q}</p>

                            {/* Rating Buttons */}
                            <div className="flex flex-wrap justify-center gap-3">
                                {[
                                    { value: "EXCELLENT", label: "Totalement en accord" },
                                    { value: "TRES_BIEN", label: "Plutôt en accord" },
                                    { value: "SATISFAISANT", label: "Plutôt en désaccord" },
                                    { value: "A_AMELIORER", label: "Totalement en désaccord" }
                                ].map(option => {
                                    const isSelected = formData.questions[groupKey][index] === option.value;

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

                            {/* Salary input if needed */}
                            {groupKey === "general" && index === 1 && (
                                <div className="mt-3 flex justify-center">
                                    <input
                                        type="text"
                                        placeholder="Préciser le salaire /h"
                                        className="border px-2 py-1 rounded"
                                        value={formData.salary}
                                        onChange={e => handleChange("salary", e.target.value)}
                                    />
                                </div>
                            )}

                        </div>
                    ))}

                    {/* Hours inputs only for conformity group */}
                    {groupKey === "conformity" && (
                        <div className="mt-6 flex flex-col gap-3 items-center">
                            <label className="font-medium">Préciser le nombre d’heures / semaine :</label>
                            <div className="flex gap-4">
                                <input className="border p-1 rounded" placeholder="Premier mois"
                                       value={formData.hoursMonth1}
                                       onChange={e => handleChange("hoursMonth1", e.target.value)} />
                                <input className="border p-1 rounded" placeholder="Deuxième mois"
                                       value={formData.hoursMonth2}
                                       onChange={e => handleChange("hoursMonth2", e.target.value)} />
                                <input className="border p-1 rounded" placeholder="Troisième mois"
                                       value={formData.hoursMonth3}
                                       onChange={e => handleChange("hoursMonth3", e.target.value)} />
                            </div>
                        </div>
                    )}

                </section>
            ))}

            <section className="mb-8 p-4 border rounded-lg bg-gray-50 ">
                <h3 className="text-lg font-semibold mb-4">Observations générales</h3>
                <div className="mb-6">
                    <p className="font-medium mb-2">Ce milieu est à privilégier pour le :</p>
                    <div className="flex flex-wrap justify-center gap-3">
                        {[
                            { value: "1", label: "Premier stage" },
                            { value: "2", label: "Deuxième stage" }
                        ].map(opt => {
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
                        })}
                    </div>
                </div>

                <div className="mb-6">
                    <p className="font-medium mb-2">Ce milieu est ouvert à accueillir :</p>
                    <div className="flex flex-wrap justify-center gap-3">
                        {[
                            { value: "1", label: "Un stagiaire" },
                            { value: "2", label: "Deux stagiaires" },
                            { value: "3", label: "Trois stagiaires" },
                            { value: "4", label: "Plus de trois" }
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

                <div className="mb-6">
                    <p className="font-medium mb-2">
                        Ce milieu désire accueillir le même stagiaire pour un prochain stage :
                    </p>
                    <div className="flex justify-center gap-3">
                        {[
                            { value: "YES", label: "Oui" },
                            { value: "NO", label: "Non" }
                        ].map(opt => {
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

                <div className="mb-6">
                    <p className="font-medium mb-2">
                        Ce milieu offre des quarts de travail variables :
                    </p>
                    <div className="flex gap-3 justify-center">
                        {[
                            { value: "YES", label: "Oui" },
                            { value: "NO", label: "Non" }
                        ].map(opt => {
                            const isSelected = formData.workShifts === opt.value;
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

                    {formData.workShifts === "YES" && (
                        <div className="mt-4 space-y-3">
                            {[1, 2, 3].map(n => (
                                <div key={n} className="flex gap-3 items-center">
                                    <span>De</span>
                                    <input
                                        type="time"
                                        className="p-2 border rounded"
                                        value={formData[`obs_shift${n}From`]}
                                        onChange={e =>
                                            handleChange(`obs_shift${n}From`, e.target.value)
                                        }
                                    />
                                    <span>à</span>
                                    <input
                                        type="time"
                                        className="p-2 border rounded"
                                        value={formData[`obs_shift${n}To`]}
                                        onChange={e =>
                                            handleChange(`obs_shift${n}To`, e.target.value)
                                        }
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