import React, {useEffect, useState} from "react";
import {useTranslation} from "react-i18next";
import {approveCv, getPendingCvs, previewCv, rejectCv} from "../../api/apiGestionnaire.jsx";
import PdfViewer from "../PdfViewer.jsx";

export default function PendingCvPage() {
    const {t} = useTranslation();
    const [pendingCvs, setPendingCvs] = useState([]);
    const [loading, setLoading] = useState(true);
    const [error, setError] = useState(null);
    const [selectedPdfUrl, setSelectedPdfUrl] = useState(null);

    const [rejectModal, setRejectModal] = useState({open: false, id: null, comment: "", error: null});

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
        } catch (err) {
            console.error(err);
        }
    };

    const openRejectModal = (cvId) => {
        setRejectModal({open: true, id: cvId, comment: "", error: null});
    };

    const closeRejectModal = () => {
        setRejectModal({open: false, id: null, comment: "", error: null});
    };

    const confirmReject = async () => {
        const {id, comment} = rejectModal;
        const trimmed = (comment || "").trim();
        if (!trimmed) {
            setRejectModal((prev) => ({...prev, error: "commentRequired"}));
            return;
        }
        try {
            await rejectCv(id, trimmed);
            setPendingCvs((prev) => prev.filter((cv) => cv.id !== id));
            closeRejectModal();
        } catch (err) {
            console.error(err);
        }
    };

    const handleView = async (cvId) => {
        try {
            const blob = await previewCv(cvId);
            const url = URL.createObjectURL(blob);
            setSelectedPdfUrl(url);
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
                    {t("pendingCvList.subtitle", {count: pendingCvs.length})}
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
                                    onClick={() => handleView(cv.id)}
                                    className="text-blue-600 hover:underline"
                                >
                                    {t("pendingCvList.actions.view")}
                                </button>
                            </td>
                            <td className="px-6 py-4 flex flex-col md:flex-row items-start md:items-center space-y-2 md:space-y-0 md:space-x-2">
                                <button
                                    onClick={() => handleApprove(cv.id)}
                                    className="bg-green-500 text-white px-3 py-1 rounded w-full md:w-auto hover:bg-green-600"
                                >
                                    {t("pendingCvList.actions.approve")}
                                </button>
                                <button
                                    onClick={() => openRejectModal(cv.id)}
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

            {selectedPdfUrl && (
                <PdfViewer
                    file={selectedPdfUrl}
                    onClose={() => {
                        URL.revokeObjectURL(selectedPdfUrl);
                        setSelectedPdfUrl(null);
                    }}
                />
            )}


            {rejectModal.open && (
                <div className="fixed inset-0 z-50 flex items-center justify-center bg-black bg-opacity-50">
                    <div className="bg-white rounded-lg shadow-lg w-full max-w-md mx-4">
                        <div className="px-6 py-4 border-b">
                            <h4 className="text-base font-semibold text-gray-900">
                                {t("pendingCvList.actions.reject")}
                            </h4>
                        </div>
                        <div className="px-6 py-4 space-y-3">
                            <p className="text-sm text-gray-700">{t("pendingCvList.commentPlaceholder")}</p>
                            <textarea
                                value={rejectModal.comment}
                                onChange={(e) => setRejectModal((prev) => ({...prev, comment: e.target.value}))}
                                maxLength={250}
                                className={`w-full h-28 border rounded p-2 resize-none ${rejectModal.error ? "border-red-500" : ""}`}
                            />
                            {rejectModal.error && (
                                <p className="text-xs text-red-600">{t(`pendingCvList.errors.${rejectModal.error}`, {defaultValue: "Commentaire requis"})}</p>
                            )}
                            <p className="text-xs text-gray-500 ml-auto">
                                {rejectModal.comment.length}/100
                            </p>
                        </div>
                        <div className="px-6 py-4 border-t flex justify-end space-x-2">
                            <button
                                onClick={closeRejectModal}
                                className="px-3 py-1 rounded border text-gray-700 hover:bg-gray-50"
                            >
                                {t("common.cancel")}
                            </button>
                            <button
                                onClick={confirmReject}
                                className="px-3 py-1 rounded bg-red-600 text-white hover:bg-red-700"
                            >
                                {t("pendingCvList.actions.reject")}
                            </button>
                        </div>
                    </div>
                </div>
            )}
        </div>
    );
}
