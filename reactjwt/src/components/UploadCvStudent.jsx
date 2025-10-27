import React, { useState } from "react";
import { useTranslation } from "react-i18next";
import { uploadStudentCv } from "../api/apiStudent";

const MAX_FILE_SIZE_MB = 5;
const BYTES_IN_MB = 1024 * 1024;
const MAX_FILE_SIZE_BYTES = MAX_FILE_SIZE_MB * BYTES_IN_MB;

export default function UploadCvStudent() {
    const { t } = useTranslation();
    const [pdfFile, setPdfFile] = useState(null);
    const [errors, setErrors] = useState({});
    const [submitting, setSubmitting] = useState(false);
    const [serverMessage, setServerMessage] = useState(null);
    const [serverMessageType, setServerMessageType] = useState(null);

    const navigateToDashboard = () => {
        window.location.href = "/dashboard/student";
    };

    function validateFile() {
        const e = {};
        if (!pdfFile) {
            e.pdfFile = t("uploadCvStudent.errors.required");
        } else if (pdfFile.type !== "application/pdf" && !pdfFile.name.toLowerCase().endsWith(".pdf")) {
            e.pdfFile = t("uploadCvStudent.errors.format");
        } else if (pdfFile.size > MAX_FILE_SIZE_BYTES) {
            e.pdfFile = t("uploadCvStudent.errors.size", { max: MAX_FILE_SIZE_MB });
        }
        setErrors(e);
        return Object.keys(e).length === 0;
    }

    function handleFileChange(e) {
        const file = e.target.files?.[0] || null;
        if (!file) {
            setPdfFile(null);
            setErrors(prev => ({ ...prev, pdfFile: undefined }));
            return;
        }
        setPdfFile(file);
        setErrors(prev => ({ ...prev, pdfFile: undefined }));
    }

    async function handleSubmit(evt) {
        evt.preventDefault();
        setServerMessage(null);
        setServerMessageType(null);

        if (!validateFile()) return;

        setSubmitting(true);
        try {
            const token = sessionStorage.getItem("accessToken");
            await uploadStudentCv(pdfFile, token);

            setServerMessage(t("uploadCvStudent.successMessage"));
            setServerMessageType("success");
            setPdfFile(null);
            setErrors({});

            const fileInput = document.getElementById("pdfFile");
            if (fileInput) fileInput.value = "";
        } catch (err) {
            setServerMessage(`${t("uploadCvStudent.errors.serverError")}: ${err.message}`);
            setServerMessageType("error");
        } finally {
            setSubmitting(false);
        }
    }

    return (
        <div className="max-w-2xl mx-auto my-8">
            <div className="bg-white shadow-lg rounded-lg p-8">
                <h2 className="text-2xl font-semibold mb-4 text-gray-800">{t("uploadCvStudent.title")}</h2>

                <form onSubmit={handleSubmit} noValidate>
                    <div className="grid gap-6">
                        <div>
                            <label className="block text-sm font-medium text-gray-700">
                                {t("uploadCvStudent.fileLabel")}
                            </label>
                            <div className="mt-1 flex items-center gap-3">
                                <label
                                    htmlFor="pdfFile"
                                    className="inline-flex items-center px-4 py-2 border border-transparent text-sm font-medium rounded-md shadow-sm text-white bg-indigo-600 hover:bg-indigo-700 cursor-pointer"
                                >
                                    {t("uploadCvStudent.choosePdf")}
                                </label>
                                <input
                                    id="pdfFile"
                                    type="file"
                                    accept="application/pdf,.pdf"
                                    onChange={handleFileChange}
                                    className="hidden"
                                />
                                <div className="flex-1 text-sm text-gray-700">
                                    {pdfFile ? (
                                        <div className="flex items-center justify-between gap-3">
                                            <div className="truncate">
                                                <span className="font-medium">{pdfFile.name}</span>
                                                <span className="ml-2 text-gray-500 text-xs">
                                                    ({(pdfFile.size / BYTES_IN_MB).toFixed(2)} MB)
                                                </span>
                                            </div>
                                            <button
                                                type="button"
                                                onClick={() => {
                                                    setPdfFile(null);
                                                    const fileInput = document.getElementById("pdfFile");
                                                    if (fileInput) fileInput.value = "";
                                                }}
                                                className="text-sm text-red-600 hover:underline"
                                            >
                                                {t("uploadCvStudent.remove")}
                                            </button>
                                        </div>
                                    ) : (
                                        <div className="text-gray-500">
                                            {t("uploadCvStudent.noFileSelected", { max: MAX_FILE_SIZE_MB })}
                                        </div>
                                    )}
                                </div>
                            </div>
                            {errors.pdfFile && <p className="mt-1 text-sm text-red-600">{errors.pdfFile}</p>}
                        </div>

                        <div className="flex flex-col md:flex-row gap-4 pt-4">
                            <button
                                type="submit"
                                disabled={submitting}
                                className={`inline-flex items-center justify-center gap-2 px-6 py-3 border border-transparent text-sm font-medium rounded-md shadow-sm text-white ${
                                    submitting
                                        ? "bg-indigo-400 cursor-not-allowed"
                                        : "bg-indigo-600 hover:bg-indigo-700"
                                }`}
                            >
                                {submitting ? (
                                    <>
                                        <div className="w-4 h-4 border-2 border-white border-t-transparent rounded-full animate-spin"></div>
                                        {t("uploadCvStudent.submitting")}
                                    </>
                                ) : (
                                    t("uploadCvStudent.submit")
                                )}
                            </button>
                            <button
                                type="button"
                                onClick={navigateToDashboard}
                                className="inline-flex items-center justify-center gap-2 px-6 py-3 border border-gray-300 text-sm font-medium rounded-md shadow-sm text-gray-700 bg-white hover:bg-gray-50"
                            >
                                {t("uploadCvStudent.backToDashboard")}
                            </button>
                        </div>

                        {serverMessage && (
                            <div
                                className={`mt-2 p-3 rounded-md text-sm ${
                                    serverMessageType === "success"
                                        ? "bg-green-50 border border-green-200 text-green-800"
                                        : "bg-red-50 border border-red-200 text-red-800"
                                }`}
                                role="status"
                                aria-live="polite"
                            >
                                <div className="flex items-center">
                                    {serverMessageType === "success" ? (
                                        <div className="h-4 w-4 mr-2 rounded-full bg-green-600 flex items-center justify-center">
                                            <span className="text-white text-xs font-bold">✓</span>
                                        </div>
                                    ) : (
                                        <div className="h-4 w-4 mr-2 rounded-full bg-red-600 flex items-center justify-center">
                                            <span className="text-white text-xs font-bold">!</span>
                                        </div>
                                    )}
                                    <span>{serverMessage}</span>
                                </div>
                            </div>
                        )}
                    </div>
                </form>
            </div>
        </div>
    );
}