import React, { useEffect, useState } from "react";
import { useTranslation } from "react-i18next";
import PdfViewer from "./PdfViewer.jsx";

export default function StudentCvList() {
    const { t } = useTranslation();
    const [cv, setCv] = useState(null);
    const [loading, setLoading] = useState(true);
    const [error, setError] = useState(null);
    const [showCommentModal, setShowCommentModal] = useState(false);
    const [showPdfModal, setShowPdfModal] = useState(false);
    const [pdfUrl, setPdfUrl] = useState(null);

    useEffect(() => {
        const controller = new AbortController();
        const signal = controller.signal;

        async function fetchCv() {
            setLoading(true);
            setError(null);
            try {
                const token = sessionStorage.getItem("accessToken");
                const response = await fetch("http://localhost:8080/student/cv", {
                    method: "GET",
                    headers: {
                        Authorization: `Bearer ${token}`,
                        "Content-Type": "application/json",
                    },
                    signal,
                });

                if (response.status === 404) {
                    setCv(null);
                } else if (response.status === 401) {
                    setError(t("studentCvList.unauthorized"));
                } else if (response.status === 403) {
                    setError(t("studentCvList.forbidden"));
                } else if (!response.ok) {
                    const errorText = await response.text();
                    setError(errorText || t("studentCvList.serverError"));
                } else {
                    const data = await response.json();
                    setCv(data || null);
                }
            } catch (err) {
                if (err.name !== "AbortError") {
                    setError(t("studentCvList.serverError"));
                }
            } finally {
                setLoading(false);
            }
        }

        fetchCv();
        return () => controller.abort();
    }, [t]);

    // ----- Utility functions -----
    const getFileName = (path) => {
        if (!path) return "CV.pdf";
        const fileName = path.split("/").pop() || path.split("\\").pop();
        if (fileName.includes("_") && fileName.includes("-")) {
            return "Mon_CV.pdf";
        }
        return fileName;
    };

    // ----- PDF preview handler -----
    const handlePreview = async () => {
        try {
            const token = sessionStorage.getItem("accessToken");
            const response = await fetch("http://localhost:8080/student/cv/download", {
                method: "GET",
                headers: { Authorization: `Bearer ${token}` },
            });
            if (response.ok) {
                const blob = await response.blob();
                const url = window.URL.createObjectURL(blob);
                setPdfUrl(url);
                setShowPdfModal(true);
            } else {
                alert(t("studentCvList.previewError"));
            }
        } catch (err) {
            alert(t("studentCvList.previewError"));
        }
    };

    // ----- Render -----
    if (loading) {
        return (
            <div className="bg-white shadow rounded-lg p-8 text-center">
                <div className="animate-pulse">
                    <div className="h-4 bg-gray-200 rounded w-1/4 mx-auto mb-4"></div>
                    <div className="h-4 bg-gray-200 rounded w-1/2 mx-auto"></div>
                </div>
                <p className="text-gray-600 mt-4">{t("studentCvList.loading")}</p>
            </div>
        );
    }

    if (error) {
        return (
            <div className="bg-white shadow rounded-lg p-8 text-center">
                <div className="text-red-600 mb-4">
                    <div className="mx-auto h-12 w-12 rounded-full bg-red-100 flex items-center justify-center">
                        <span className="text-red-600 text-2xl font-bold">!</span>
                    </div>
                </div>
                <h3 className="text-lg font-medium text-gray-900 mb-2">
                    {t("studentCvList.errorTitle")}
                </h3>
                <p className="text-red-600">{error}</p>
                <button
                    onClick={() => window.location.reload()}
                    className="mt-4 px-4 py-2 bg-red-600 text-white rounded hover:bg-red-700"
                >
                    {t("studentCvList.retry")}
                </button>
            </div>
        );
    }

    if (!cv) {
        return (
            <div className="bg-white shadow rounded-lg p-8 text-center">
                <div className="text-gray-400 mb-4">
                    <div className="mx-auto h-16 w-16 rounded bg-gray-100 flex items-center justify-center">
                        <span className="text-gray-500 text-3xl">📄</span>
                    </div>
                </div>
                <h3 className="text-lg font-medium text-gray-900 mb-2">
                    {t("studentCvList.noCvTitle")}
                </h3>
                <p className="text-gray-600 mb-4">{t("studentCvList.noCvDescription")}</p>
                <a
                    href="/dashboard/student/uploadCv"
                    className="inline-flex items-center px-4 py-2 bg-indigo-600 text-white rounded-md hover:bg-indigo-700 transition-colors"
                >
                    <span className="mr-2">➕</span>
                    {t("studentCvList.uploadCv")}
                </a>
            </div>
        );
    }

    const status = (cv.status || "").toString().toUpperCase();
    const statusLabel =
        status === "PENDING" || status === "PENDING_VALIDATION" ? (
            <span className="px-3 py-1 text-xs font-medium rounded-full bg-yellow-100 text-yellow-800 border border-yellow-200">
                <span className="w-2 h-2 bg-yellow-500 rounded-full inline-block mr-2"></span>
                {t("studentCvList.status.pendingapproval")}
            </span>
        ) : status === "APPROVED" || status === "APPROUVED" ? (
            <span className="px-3 py-1 text-xs font-medium rounded-full bg-green-100 text-green-800 border border-green-200">
                <span className="w-2 h-2 bg-green-500 rounded-full inline-block mr-2"></span>
                {t("studentCvList.status.approved")}
            </span>
        ) : status === "REJECTED" ? (
            <span className="px-3 py-1 text-xs font-medium rounded-full bg-red-100 text-red-800 border border-red-200">
                <span className="w-2 h-2 bg-red-500 rounded-full inline-block mr-2"></span>
                {t("studentCvList.status.rejected")}
            </span>
        ) : (
            <span className="px-3 py-1 text-xs font-medium rounded-full bg-gray-100 text-gray-800 border border-gray-200">
                <span className="w-2 h-2 bg-gray-500 rounded-full inline-block mr-2"></span>
                {t("studentCvList.status.unknown")}
            </span>
        );

    return (
        <>
            <div className="bg-white shadow rounded-lg overflow-hidden">
                <div className="px-6 py-4 border-b border-gray-200">
                    <h3 className="text-lg font-medium text-gray-900">{t("studentCvList.title")}</h3>
                    <p className="text-sm text-gray-600">{t("studentCvList.subtitle")}</p>
                </div>

                <div className="overflow-x-auto">
                    <table className="min-w-full divide-y divide-gray-200">
                        <thead className="bg-gray-50">
                        <tr>
                            <th className="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">
                                {t("studentCvList.table.file")}
                            </th>
                            <th className="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">
                                {t("studentCvList.table.status")}
                            </th>
                            <th className="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">
                                {t("studentCvList.table.actions")}
                            </th>
                        </tr>
                        </thead>

                        <tbody className="bg-white divide-y divide-gray-200">
                        <tr className="hover:bg-gray-50">
                            <td className="px-6 py-4 whitespace-nowrap">
                                <div className="flex items-center">
                                    <div className="flex-shrink-0">
                                        <div className="h-8 w-8 bg-red-500 rounded flex items-center justify-center">
                                            <span className="text-white text-sm font-bold">PDF</span>
                                        </div>
                                    </div>
                                    <div className="ml-3">
                                        <div className="text-sm font-medium text-gray-900">{getFileName(cv.pdfPath)}</div>
                                        <div className="text-sm text-gray-500">PDF</div>
                                    </div>
                                </div>
                            </td>

                            <td className="px-6 py-4 whitespace-nowrap">{statusLabel}</td>

                            <td className="px-6 py-4 whitespace-nowrap text-sm font-medium space-x-2">
                                {status === "REJECTED" && (
                                    <button
                                        onClick={() => setShowCommentModal(true)}
                                        className="inline-flex items-center px-3 py-1 border border-transparent text-sm leading-4 font-medium rounded-md text-red-700 bg-red-100 hover:bg-red-200"
                                        title="Voir le commentaire de rejet"
                                    >
                                        <span className="mr-1">💬</span>
                                    </button>
                                )}

                                <button
                                    onClick={handlePreview}
                                    className="inline-flex items-center px-3 py-1 border border-transparent text-sm leading-4 font-medium rounded-md text-indigo-700 bg-indigo-100 hover:bg-indigo-200"
                                >
                                    <span className="mr-1">👁</span>
                                    {t("studentCvList.actions.preview")}
                                </button>
                            </td>
                        </tr>
                        </tbody>
                    </table>
                </div>

                <div className="px-6 py-3 bg-gray-50 text-right">
                    <a
                        href="/dashboard/student/uploadCv"
                        className="inline-flex items-center px-4 py-2 text-sm font-medium text-indigo-600 hover:text-indigo-500"
                    >
                        {t("studentCvList.actions.uploadNew")}
                        <span className="ml-1">⬆</span>
                    </a>
                </div>
            </div>

            {/* 🔹 Rejection Comment Modal */}
            {showCommentModal && (
                <div className="fixed inset-0 bg-black bg-opacity-50 flex items-center justify-center z-50 p-4">
                    <div className="bg-white rounded-lg shadow-xl max-w-md w-full">
                        <div className="px-6 py-4 border-b border-gray-200">
                            <div className="flex items-center justify-between">
                                <h3 className="text-lg font-medium text-gray-900">Commentaire de rejet</h3>
                                <button
                                    onClick={() => setShowCommentModal(false)}
                                    className="text-gray-400 hover:text-gray-600 text-2xl leading-none"
                                >
                                    ×
                                </button>
                            </div>
                        </div>

                        <div className="px-6 py-4">
                            <div className="bg-red-50 border-l-4 border-red-500 p-4 rounded">
                                <div className="flex items-start">
                                    <span className="text-red-500 text-xl mr-3">⚠️</span>
                                    <div>
                                        <p className="text-sm text-gray-700">
                                            {cv.rejectionComment || "Aucun commentaire fourni."}
                                        </p>
                                    </div>
                                </div>
                            </div>
                        </div>

                        <div className="px-6 py-4 bg-gray-50 text-right rounded-b-lg">
                            <button
                                onClick={() => setShowCommentModal(false)}
                                className="px-4 py-2 bg-indigo-600 text-white rounded-md hover:bg-indigo-700 transition-colors"
                            >
                                Fermer
                            </button>
                        </div>
                    </div>
                </div>
            )}

            {/* 🔹 PDF Viewer Modal */}
            {showPdfModal && (
                <PdfViewer
                    file={pdfUrl}
                    onClose={() => {
                        setShowPdfModal(false);
                        if (pdfUrl) window.URL.revokeObjectURL(pdfUrl);
                    }}
                />
            )}
        </>
    );
}
