import React, {useEffect, useState} from "react"
import {useParams, useNavigate} from "react-router-dom"
import {
    createEvaluation,
    getEvaluationInfo,
    checkExistingEvaluation,
    generateEvaluationPdfWithId
} from "../../api/apiProf.jsx"

export default function EvaluationFormTeacher(){
    const {studentId, offerId} = useParams()
    const navigate = useNavigate();

    const [loading, setLoading] = useState(true);
    const [error, setError] = useState(null)
    const [submitting, setSubmitting] = useState(false)

    const [info, setInfo] = useState(null)
    const [evaluationId, setEvaluationId] = useState(null)

    const [formData, setFormData] = useState({
        stageNumber: "",
        hoursMonth1: "",
        hoursMonth2: "",
        hoursMonth3: "",
        salaryPerHour: "",
        comments: "",
        obs_stagePreference: "",
        obs_openToHosting: "",
        obs_sameInternAgain: "",
        obs_variableShifts: "",
        obs_shift1From: "",
        obs_shift1To: "",
        obs_shift2From: "",
        obs_shift2To: "",
        obs_shift3From: "",
        obs_shift3To: "",
        questions: {}
    });

    const questionGroups = {
        conformity: [
            "Les tâches confiées au stagiaire sont conformes aux tâches annoncées dans l’entente de stage.",
            "Des mesures d’accueil facilitent l’intégration du nouveau stagiaire.",
            "Le temps réel consacré à l’encadrement du stagiaire est suffisant."
        ],
        environment: [
            "L’environnement de travail respecte les normes d’hygiène et de sécurité au travail.",
            "Le climat de travail est agréable."
        ],
        general: [
            "Le milieu de stage est accessible par transport en commun.",
            "Le salaire offert est intéressant pour le stagiaire.",
            "La communication avec le superviseur de stage facilite le déroulement du stage.",
            "L’équipement fourni est adéquat pour réaliser les tâches confiées.",
            "Le volume de travail est acceptable."
        ]
    };
    const ratingOptions = [
        "TOTAL_AGREE",
        "MOSTLY_AGREE",
        "MOSTLY_DISAGREE",
        "TOTAL_DISAGREE",
        "NOT_APPLICABLE"
    ];

    useEffect(() => {
        async function initialize(){
            try{
                if (!studentId || !offerId){
                    setError("Paramatres invalides")
                    return
                }
                const evaluation = await checkExistingEvaluation(studentId, offerId)
                if(evaluation.exists){
                    setError("Une évaluation a déjà été complétée")
                    setTimeout(() => navigate("dashboard/prof"), 2000)

                }
                const data = await getEvaluationInfo(studentId, offerId)
                setInfo(data)

                const initialQuestions = {}
                Object.entries(questionGroups).forEach(([groupKey, questions]) => {
                    initialQuestions[groupKey] = questions.map(() => "");

                    setFormData(prev => ({
                        ...prev,
                        questions: initialQuestions
                    }));
                })
            } catch (err){
                console.log(err)
                setError("Erreur de chargement du formulaire: ", err)
            }
            finally{
                setLoading(false)
            }
        }
        initialize()
    }, [studentId, offerId, navigate])

    const handleQuestionChange = (group, index, value) => {
        setFormData(prev => ({
            ...prev,
            questions: {
                ...prev.questions,
                [group]: prev.questions[group].map((q, i) => i === index ? value: q)
            }
        }))
    }
    const handleChange = (field, value) => {
        setFormData(prev => ({...prev, [field]: value}))
    }
    const handleSubmit = async () => {
        setSubmitting(true)
        setError(null)

        try{
            const res = await createEvaluation(studentId, offerId)
            const evalId = res.id || res.evaluationId;
            setEvaluationId(evalId)

            await generateEvaluationPdfWithId(evalId, formData)
            setTimeout(() => navigate("dashboard/prof"), 2000)
        } catch(err){
            setError("Erreur lors de la soumission")
        }
        finally{
            setSubmitting(false)
        }
    }

    if (loading) return <p>Chargement...</p>
    if(error && !info) return <p className="text-red-500">{error}</p>;

    if (loading) return <p>Chargement...</p>
    if (error && !info) return <p className="text-red-500">{error}</p>;

    return (
        <div className="max-w-4xl mx-auto p-6">
            <h1 className="text-3xl font-bold text-center mb-8">
                ÉVALUATION DU MILIEU DE STAGE
            </h1>

            <section className="mb-8 p-4 border rounded-lg bg-gray-50">
                <h2 className="text-xl font-semibold mb-4">Identification de l'entreprise</h2>
                <p><strong>Nom: </strong>{info?.entrepriseTeacherDto.companyName}</p>
                <p><strong>Personne contact: </strong>{info?.entrepriseTeacherDto.contactName}</p>
                <p><strong>Adresse: </strong>{info?.entrepriseTeacherDto.address}</p>
                <p><strong>Email: </strong>{info?.entrepriseTeacherDto.email}</p>
            </section>

            {/* Identification Stagiaire */}
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

            {/* Questions */}
            {Object.entries(questionGroups).map(([groupKey, questions]) => (
                <section key={groupKey} className="mb-8 p-4 border rounded-lg">
                    <h3 className="text-lg font-semibold mb-4">{groupKey.toUpperCase()}</h3>

                    {questions.map((q, index) => (
                        <div key={index} className="mb-6">
                            <p className="mb-5">{q}</p>

                            <div className="flex flex-wrap justify-center gap-3 mb-3">
                                {[
                                    {
                                        value: 'EXCELLENT',
                                        label: 'Totalement en accord',
                                        baseClasses: 'bg-green-400 border-2 border-green-600 text-green-900 font-semibold hover:border-green-700 hover:bg-green-500/80',
                                        selectedClasses: 'border-[3px] border-green-800 ring-2 ring-green-600 ring-offset-2 shadow-md',
                                        inputRing: 'focus:ring-green-600 text-green-700'
                                    },
                                    {
                                        value: 'TRES_BIEN',
                                        label: 'Plutôt en accord',
                                        baseClasses: 'bg-green-200 border-2 border-green-400 text-green-900 font-semibold hover:border-green-500 hover:bg-green-300/80',
                                        selectedClasses: 'border-[3px] border-green-600 ring-2 ring-green-400 ring-offset-2 shadow-md',
                                        inputRing: 'focus:ring-green-500 text-green-600'
                                    },
                                    {
                                        value: 'SATISFAISANT',
                                        label: 'Plutôt en désaccord',
                                        baseClasses: 'bg-orange-200 border-2 border-orange-400 text-orange-900 font-semibold hover:border-orange-500 hover:bg-orange-300/80',
                                        selectedClasses: 'border-[3px] border-orange-600 ring-2 ring-orange-400 ring-offset-2 shadow-md',
                                        inputRing: 'focus:ring-orange-500 text-orange-600'
                                    },
                                    {
                                        value: 'A_AMELIORER',
                                        label: 'Totalement en désaccord',
                                        baseClasses: 'bg-red-400 border-2 border-red-600 text-red-900 font-semibold hover:border-red-700 hover:bg-red-500/80',
                                        selectedClasses: 'border-[3px] border-red-800 ring-2 ring-red-600 ring-offset-2 shadow-md',
                                        inputRing: 'focus:ring-red-600 text-red-700'
                                    }
                                ].map(option => {
                                    const isSelected =
                                        formData.questions[groupKey][index] === option.value;

                                    return (
                                        <label
                                            key={option.value}
                                            className={`flex items-center px-4 py-2 rounded-lg cursor-pointer transition-all
                                            ${option.baseClasses}
                                            ${isSelected ? option.selectedClasses : ''}`}
                                        >
                                            <input
                                                type="radio"
                                                name={`${groupKey}-${index}`}
                                                value={option.value}
                                                checked={isSelected}
                                                onChange={() =>
                                                    handleQuestionChange(groupKey, index, option.value)
                                                }
                                                className={`mr-2 focus:ring-2 ${option.inputRing}`}
                                            />
                                            <span className="text-sm font-medium">{option.label}</span>
                                        </label>
                                    );
                                })}
                            </div>
                        </div>
                    ))}

                    {groupKey === "conformity" && (
                        <div className="mt-6 p-4 border rounded-lg bg-gray-50">
                            <h4 className="font-semibold mb-3">
                                Préciser le nombre d’heures/semaine :
                            </h4>

                            <div className="grid grid-cols-1 sm:grid-cols-3 gap-4">
                                <div>
                                    <label className="block text-sm font-medium mb-1">Premier mois :</label>
                                    <input
                                        type="number"
                                        className="w-full p-2 border rounded"
                                        value={formData.hoursMonth1}
                                        onChange={e => handleChange("hoursMonth1", e.target.value)}
                                        min="0"
                                    />
                                </div>

                                <div>
                                    <label className="block text-sm font-medium mb-1">Deuxième mois :</label>
                                    <input
                                        type="number"
                                        className="w-full p-2 border rounded"
                                        value={formData.hoursMonth2}
                                        onChange={e => handleChange("hoursMonth2", e.target.value)}
                                        min="0"
                                    />
                                </div>

                                <div>
                                    <label className="block text-sm font-medium mb-1">Troisième mois :</label>
                                    <input
                                        type="number"
                                        className="w-full p-2 border rounded"
                                        value={formData.hoursMonth3}
                                        onChange={e => handleChange("hoursMonth3", e.target.value)}
                                        min="0"
                                    />
                                </div>
                            </div>
                        </div>
                    )}
                    {groupKey === "general"  && (
                            <div className="mt-4 ">
                                <label className="block text-sm font-medium mb-1">
                                    Préciser :
                                </label>
                                <div className="flex items-center justify-center gap-2">
                                    <input
                                        type="number"
                                        className="w-32 p-2 border rounded"
                                        placeholder="0.00"
                                        step="0.01"
                                        min="0"
                                        value={formData.salaryPerHour}
                                        onChange={e => handleChange("salaryPerHour", e.target.value)}
                                    />
                                    <span>/ l’heure</span>
                                </div>
                            </div>
                    )}
                </section>
            ))}

            {/* Comments */}
            <section className="mb-6">
                <label className="block font-medium mb-2">Commentaires :</label>
                <textarea
                    className="w-full p-2 border rounded"
                    rows="4"
                    value={formData.comments}
                    onChange={e => handleChange("comments", e.target.value)}
                />
            </section>

            {/* Observations */}
            <section className="mb-8 p-4 border rounded-lg bg-gray-50 ">
                <h3 className="text-lg font-semibold mb-4">Observations générales</h3>

                <div className="mb-6">
                    <p className="font-medium mb-2">Ce milieu est à privilégier pour le :</p>
                    <div className="flex flex-wrap justify-center gap-3">
                        {[
                            { value: "1", label: "Premier stage" },
                            { value: "2", label: "Deuxième stage" }
                        ].map(opt => {
                            const isSelected = formData.obs_stagePreference === opt.value;
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
                                        onChange={() => handleChange("obs_stagePreference", opt.value)}
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
                            const isSelected = formData.obs_openToHosting === opt.value;
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
                                        onChange={() => handleChange("obs_openToHosting", opt.value)}
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
                            const isSelected = formData.obs_sameInternAgain === opt.value;
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
                                        onChange={() => handleChange("obs_sameInternAgain", opt.value)}
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
                            const isSelected = formData.obs_variableShifts === opt.value;
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
                                        onChange={() => handleChange("obs_variableShifts", opt.value)}
                                        className="mr-2"
                                    />
                                    {opt.label}
                                </label>
                            );
                        })}
                    </div>

                    {formData.obs_variableShifts === "YES" && (
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

            <button
                onClick={handleSubmit}
                disabled={submitting}
                className="px-6 py-3 bg-blue-600 text-white rounded-lg hover:bg-blue-700"
            >
                {submitting ? "Soumission..." : "Soumettre l’évaluation"}
            </button>

            {error && (
                <p className="text-red-600 mt-4">{error}</p>
            )}
        </div>
    );

}