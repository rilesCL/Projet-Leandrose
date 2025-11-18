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
    const [student, setStudent] = useState(null)
    const [company, setCompany] = useState(null)
    const [evaluationId, setEvaluationId] = useState(null)

    const [formData, setFormData] = useState({
        stageNumber: "",
        hoursMonth1: "",
        hoursMonth2: "",
        hoursMonth3: "",
        comments: "",
        observations: "",
        sameInternNextStage: "",
        nbInternsAccepted: "",
        schedule1Start: "",
        schedule1End: "",
        schedule2Start: "",
        schedule2End: "",
        schedule3Start: "",
        schedule3End: "",
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
                // setStudent(info.studentInfo)
                // setCompany(info.companyInfo)

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
    if(error && !student) return <p className="text-red-500">{error}</p>;

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
                    <label className="font-medium">Stage :</label>
                    <div className="flex gap-4 mt-2">
                        <label><input type="radio" value="1"
                                      checked={formData.stageNumber === "1"}
                                      onChange={e => handleChange("stageNumber", e.target.value)} /> 1</label>

                        <label><input type="radio" value="2"
                                      checked={formData.stageNumber === "2"}
                                      onChange={e => handleChange("stageNumber", e.target.value)} /> 2</label>
                    </div>
                </div>
            </section>

            {/* Questions */}
            {Object.entries(questionGroups).map(([groupKey, questions]) => (
                <section key={groupKey} className="mb-8 p-4 border rounded-lg">
                    <h3 className="text-lg font-semibold mb-4">{groupKey.toUpperCase()}</h3>

                    {questions.map((q, index) => (
                        <div key={index} className="mb-4">
                            <p className="mb-2">{q}</p>

                            <div className="flex flex-wrap gap-4">
                                {ratingOptions.map(option => (
                                    <label key={option} className="flex items-center gap-2">
                                        <input
                                            type="radio"
                                            name={`${groupKey}-${index}`}
                                            value={option}
                                            checked={formData.questions[groupKey][index] === option}
                                            onChange={() => handleQuestionChange(groupKey, index, option)}
                                        />
                                        {option}
                                    </label>
                                ))}
                            </div>
                        </div>
                    ))}
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
            <section className="mb-6">
                <label className="block font-medium mb-2">Observations générales :</label>
                <textarea
                    className="w-full p-2 border rounded"
                    rows="4"
                    value={formData.observations}
                    onChange={e => handleChange("observations", e.target.value)}
                />
            </section>

            {/* Submit */}
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
    )
}