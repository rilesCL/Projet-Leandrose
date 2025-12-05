import React, {useEffect, useState} from 'react';
import {Link} from 'react-router-dom';
import {useTranslation} from 'react-i18next';

import * as apiEmployer from '../../api/apiEmployeur';
import * as apiProf from '../../api/apiProf';
import {getMyRole} from '../../api/apiAuth';

import PdfViewer from '../PdfViewer.jsx';

export default function EvaluationsList() {
    const {t} = useTranslation();

    const [role, setRole] = useState(null);
    const [eligibleAgreements, setEligibleAgreements] = useState([]);
    const [evaluationStatus, setEvaluationStatus] = useState({});
    const [teacherAssignmentStatus, setTeacherAssignmentStatus] = useState({});
    const [loading, setLoading] = useState(true);

    const [showPdfViewer, setShowPdfViewer] = useState(false);
    const [currentPdfBlob, setCurrentPdfBlob] = useState(null);


    useEffect(() => {
        const loadRole = async () => {
            try {
                const res = await getMyRole();
                console.log("ROLE", res.role)
                setRole(res.role);
            } catch (err) {
                console.error("Error fetching role:", err);
            }
        };
        loadRole();
    }, []);

    const isEmployer = role === "EMPLOYEUR";
    const isTeacher = role === "PROF";

    const api = isEmployer ? apiEmployer : apiProf;


    useEffect(() => {
        if (!role) return
        const fetchEligible = async () => {
            try {
                const agreements = await api.getEligibleEvaluations();
                setEligibleAgreements(agreements);

                const evalMap = {};
                const teacherMap = {};

                for (const agreement of agreements) {
                    const key = `${agreement.studentId}-${agreement.offerId}`;

                    try {
                        const existing = await api.checkExistingEvaluation(
                            agreement.studentId,
                            agreement.offerId
                        );
                        evalMap[key] = existing;

                        if (role === "EMPLOYEUR") {
                            const teacherCheck =
                                await apiEmployer.checkTeacherAssigned(
                                    agreement.studentId,
                                    agreement.offerId
                                );
                            console.log("Teacher check", teacherCheck)
                            teacherMap[key] = teacherCheck.teacherAssigned;
                        } else {
                            teacherMap[key] = true;
                        }

                    } catch (err) {
                        evalMap[key] = {exists: false};
                        teacherMap[key] = false;
                    }
                }

                setEvaluationStatus(evalMap);
                setTeacherAssignmentStatus(teacherMap);
            } finally {
                setLoading(false);
            }
        };

        fetchEligible();
    }, [role]);


    const handleViewPdf = async (studentId, offerId) => {
        try {
            const key = `${studentId}-${offerId}`;
            const status = evaluationStatus[key];
            if (status?.exists && status.evaluation) {
                const blob = await api.previewEvaluationPdf(status.evaluation.id);
                setCurrentPdfBlob(blob);
                setShowPdfViewer(true);
            }
        } catch (err) {
            console.error("Error loading PDF:", err);
        }
    };

    const handleClosePdfViewer = () => {
        setShowPdfViewer(false);
        setCurrentPdfBlob(null);
    };

    if (!role || loading) {
        return (
            <div className="flex justify-center items-center min-h-64">
                <div className="animate-spin rounded-full h-8 w-8 border-b-2 border-blue-600"></div>
            </div>
        );
    }

    const formatEvaluationDate = (dateString) => {
        if (!dateString) return t("evaluationList.dateNotAvailable");

        if (dateString.match(/^\d{4}-\d{2}-\d{2}$/)) {
            const [year, month, day] = dateString.split('-');
            const date = new Date(parseInt(year), parseInt(month) - 1, parseInt(day));
            return date.toLocaleDateString();
        }

        try {
            const date = new Date(dateString);
            return isNaN(date.getTime())
                ? t("evaluationList.dateNotAvailable")
                : date.toLocaleDateString();
        } catch {
            return t("evaluationList.dateNotAvailable");
        }
    }

    return (
        <div className="w-full px-4 py-8">

            {eligibleAgreements.length === 0 ? (
                <div className="text-center py-8 text-gray-500">
                    {t("evaluationList.no_evaluations")}
                </div>
            ) : (
                <div className="space-y-5">
                    {eligibleAgreements.length === 0 ? (
                        <div className="text-center py-8 text-gray-500">
                            {t("evaluationList.no_evaluations")}
                        </div>
                    ) : (
                        <div className="space-y-5">
                            {eligibleAgreements.map((agreement) => {
                                const key = `${agreement.studentId}-${agreement.offerId}`;
                                const status = evaluationStatus[key] || {};
                                const evaluation = status?.evaluation;
                                const hasEval =
                                    evaluation &&
                                    ((isTeacher && evaluation.submittedByProfessor) ||
                                        (isEmployer && evaluation.submittedByEmployer))

                                const teacherAssigned = teacherAssignmentStatus[key];
                                const canCreate = teacherAssigned && !hasEval

                                return (
                                    <div
                                        key={agreement.id}
                                        className="bg-white border rounded-xl p-6 shadow-sm"
                                    >
                                        <div className="flex flex-col md:flex-row md:justify-between gap-6">

                                            <div className="flex-1 space-y-3">
                                                <h3 className="text-xl font-semibold">
                                                    {agreement.studentFirstName} {agreement.studentLastName}
                                                </h3>

                                                <p>{agreement.internshipDescription}</p>

                                                {hasEval && (
                                                    <div className="px-3 py-1.5 bg-green-50 text-green-700 rounded-full text-sm">
                                                        ✔ {t("evaluationList.evaluation")}{" "}
                                                        {isTeacher
                                                            ? evaluation.submittedByProfessor
                                                                ? t("evaluationList.submitted")
                                                                : t("evaluationList.draft_created")
                                                            : evaluation.submittedByEmployer
                                                                ? t("evaluationList.submitted")
                                                                : t("evaluationList.draft_created")}
                                                        {" - "}
                                                        {console.log("Raw dateEvaluation:", evaluation.dateEvaluation, "Type:", typeof evaluation.dateEvaluation)}

                                                        {formatEvaluationDate(evaluation.dateEvaluation)}
                                                    </div>
                                                )}
                                            </div>

                                            <div className="flex flex-col gap-3 items-center md:items-end">
                                                {hasEval ? (
                                                    <button
                                                        onClick={() =>
                                                            handleViewPdf(
                                                                agreement.studentId,
                                                                agreement.offerId
                                                            )
                                                        }
                                                        className="px-4 py-2 bg-emerald-600 text-white rounded-md w-full md:w-auto"
                                                    >
                                                        {t("evaluationList.view_pdf")}
                                                    </button>
                                                ) : (
                                                    <Link
                                                        to={
                                                            canCreate
                                                                ? isTeacher
                                                                    ? `/dashboard/prof/evaluation/${agreement.studentId}/${agreement.offerId}`
                                                                    : `/dashboard/employeur/evaluation/${agreement.studentId}/${agreement.offerId}`
                                                                : "#"
                                                        }
                                                        className={`px-4 py-2 rounded-md text-white text-center w-full md:w-auto ${
                                                            teacherAssigned
                                                                ? "bg-blue-600 hover:bg-blue-700"
                                                                : "bg-gray-400 cursor-not-allowed"
                                                        }`}
                                                    >
                                                        {canCreate
                                                            ? t("evaluationList.create_evaluation")
                                                            : t("evaluationList.awaiting_teacher")}
                                                    </Link>
                                                )}
                                            </div>
                                        </div>
                                    </div>
                                );
                            })}
                        </div>
                    )}
                </div>
            )}

            {showPdfViewer && currentPdfBlob && (
                <PdfViewer file={currentPdfBlob} onClose={handleClosePdfViewer}/>
            )}
        </div>
    );
}