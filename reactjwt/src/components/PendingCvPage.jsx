import React, { useEffect, useState } from "react";
import { useTranslation } from "react-i18next";
import { approveCv, rejectCv, getPendingCvs, downloadCv } from "../api/apiGestionnaire.jsx";

export default function PendingCvPage() {
    const { t } = useTranslation();
    const [pendingCvs, setPendingCvs] = useState([]);
    const [comments, setComments] = useState({});
    const [loading, setLoading] = useState(true);
    const [error, setError] = useState(null);
    const [validationErrors, setValidationErrors] = useState({});

    useEffect(() => {
        async function fetchCvs() {
            setLoading(true);
            setError(null);
            try {
                const cvs = await getPendingCvs();
                setPendingCvs(Array.isArray(cvs) ? cvs : []);
            } catch (err) {
                console.error(err);
                setError("serverError");
            } finally {
                setLoading(false);
            }
        }

        fetchCvs();
    }, []);

    const handleApprove = async (cvId) => {
        try {
            await approveCv(cvId);
            setPendingCvs((prev) => prev.filter((cv) => cv.id !== cvId));
            setValidationErrors((prev) => {
                const updated = { ...prev };
                delete updated[cvId];
                return updated;
            });
        } catch (err) {
            console.error(err);
        }
    };

    const handleReject = async (cvId) => {
        const comment = comments[cvId] || "";
        if (!comment.trim()) {
            setValidationErrors((prev) => ({ ...prev, [cvId]: "commentRequired" }));
            return;
        }
        try {
            await rejectCv(cvId, comment);
            setPendingCvs((prev) => prev.filter((cv) => cv.id !== cvId));
            setValidationErrors((prev) => {
                const updated = { ...prev };
                delete updated[cvId];
                return updated;
            });
        } catch (err) {
            console.error(err);
        }
    };

    const handleCommentChange = (cvId, value) => {
        setComments((prev) => ({ ...prev, [cvId]: value }));
        if (validationErrors[cvId]) {
            setValidationErrors((prev) => {
                const updated = { ...prev };
                delete updated[cvId];
                return updated;
            });
        }
    };

    const handleDownload = async (cvId) => {
        try {
            await downloadCv(cvId);
        } catch (err) {
            console.error(err);
            alert(t("pendingCvList.errors.download"));
        }
    };

    if (loading) return <div className="p-8 text-center">{t("pendingCvList.loading")}</div>;
    if (error) return <div className="p-8 text-red-600 text-center">{t(`pendingCvList.errors.${error}`)}</div>;
    if (!pendingCvs.length) return <div className="p-8 text-center">{t("pendingCvList.noPending")}</div>;

    return (
        <div className="bg-white shadow rounded-lg overflow-hidden">
            <div className="px-6 py-4 border-b border-gray-200">
                <h3 className="text-lg font-medium text-gray-900">{t("pendingCvList.title")}</h3>
                <p className="text-sm text-gray-600">
                    {t("pendingCvList.subtitle", { count: pendingCvs.length })}
                </p>
            </div>

            <div className="overflow-x-auto">
                <table className="min-w-full divide-y divide-gray-200">
                    <thead className="bg-gray-50">
                    <tr>
                        <th className="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">
                            {t("pendingCvList.table.student")}
                        </th>
                        <th className="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">
                            {t("pendingCvList.table.cv")}
                        </th>
                        <th className="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">
                            {t("pendingCvList.table.comment")}
                        </th>
                        <th className="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">
                            {t("pendingCvList.table.actions")}
                        </th>
                    </tr>
                    </thead>
                    <tbody className="bg-white divide-y divide-gray-200">
                    {pendingCvs.map((cv) => (
                        <tr key={cv.id} className="hover:bg-gray-50">
                            <td className="px-6 py-4 text-sm text-gray-900 min-w-[150px]">
                                {cv.studentName || t("pendingCvList.unknownStudent")}
                            </td>
                            <td className="px-6 py-4 min-w-[120px]">
                                <button
                                    onClick={() => handleDownload(cv.id)}
                                    className="text-blue-600 hover:underline"
                                >
                                    {t("pendingCvList.actions.download")}
                                </button>
                            </td>
                            <td className="px-6 py-4">
                                <textarea
                                    value={comments[cv.id] || ""}
                                    onChange={(e) => handleCommentChange(cv.id, e.target.value)}
                                    className={`w-full h-20 border rounded p-2 resize-none ${
                                        validationErrors[cv.id] ? "border-red-500" : ""
                                    }`}
                                    placeholder={t("pendingCvList.commentPlaceholder")}
                                />
                                {validationErrors[cv.id] && (
                                    <p className="mt-1 text-xs text-red-600">
                                        {t(`pendingCvList.errors.${validationErrors[cv.id]}`)}
                                    </p>
                                )}
                            </td>
                            <td className="px-6 py-4 flex flex-col md:flex-row items-start md:items-center space-y-2 md:space-y-0 md:space-x-2">
                                <button
                                    onClick={() => handleApprove(cv.id)}
                                    className="bg-green-500 text-white px-3 py-1 rounded w-full md:w-auto hover:bg-green-600"
                                >
                                    {t("pendingCvList.actions.approve")}
                                </button>
                                <button
                                    onClick={() => handleReject(cv.id)}
                                    className="bg-red-500 text-white px-3 py-1 rounded w-full md:w-auto hover:bg-red-600"
                                >
                                    {t("pendingCvList.actions.reject")}
                                </button>
                            </td>
                        </tr>
                    ))}
                    </tbody>
                </table>
            </div>
        </div>
    );
}