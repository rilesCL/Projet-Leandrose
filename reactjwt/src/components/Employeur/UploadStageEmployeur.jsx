import React, { useState } from "react";
import { uploadStageEmployeur } from "../../api/apiEmployeur.jsx";
import { useTranslation } from "react-i18next";
import DatePicker from "react-datepicker";
import "react-datepicker/dist/react-datepicker.css";

const MAX_FILE_SIZE_MB = 5;
const BYTES_IN_KB = 1024;
const BYTES_IN_MB = BYTES_IN_KB * BYTES_IN_KB;
const MAX_FILE_SIZE_BYTES = MAX_FILE_SIZE_MB * BYTES_IN_MB;
const MIN_DURATION_WEEKS = 1;
const MAX_DURATION_WEEKS = 52;
const MIN_REMUNERATION = 0;
const MAX_DESCRIPTION_LENGTH = 100;
const DATE_REGEX = /^(0[1-9]|[12][0-9]|3[01])-(0[1-9]|1[0-2])-(\d{4})$/;

export default function UploadStageEmployeur({ employeurId }) {
    const { t } = useTranslation();
    const [description, setDescription] = useState("");
    const [startDate, setStartDate] = useState("");
    const [duration, setDuration] = useState("");
    const [address, setAddress] = useState("");
    const [remuneration, setRemuneration] = useState("");
    const [pdfFile, setPdfFile] = useState(null);
    const [errors, setErrors] = useState({});
    const [submitting, setSubmitting] = useState(false);
    const [toast, setToast] = useState({ show: false, message: '', type: 'success' });

    const navigateToDashboard = () => {
        window.location.href = '/dashboard/employeur';
    };

    const closeToast = () => {
        setToast({ show: false, message: '', type: 'success' });
    };

    function validateFields() {
        const e = {};
        if (!description.trim()) {
            e.description = t("uploadStageEmployeur.errors.description");
        } else if (description.trim().length > MAX_DESCRIPTION_LENGTH) {
            e.description = t("uploadStageEmployeur.errors.descriptionTooLong", { max: MAX_DESCRIPTION_LENGTH });
        }

        if (!startDate.trim()) {
            e.startDate = t("uploadStageEmployeur.errors.startDate");
        } else if (!DATE_REGEX.test(startDate.trim())) {
            e.startDate = t("uploadStageEmployeur.errors.startDateFormat");
        }

        if (!duration.toString().trim()) {
            e.duration = t("uploadStageEmployeur.errors.duration");
        } else if (!Number.isInteger(Number(duration)) || Number(duration) < MIN_DURATION_WEEKS) {
            e.duration = t("uploadStageEmployeur.errors.durationInvalid");
        } else if (Number(duration) > MAX_DURATION_WEEKS) {
            e.duration = t("uploadStageEmployeur.errors.durationInvalidMax");
        }

        if (!address.trim()) {
            e.address = t("uploadStageEmployeur.errors.address");
        }

        if (!pdfFile) {
            e.pdfFile = t("uploadStageEmployeur.errors.pdfRequired");
        } else {
            const fileName = pdfFile.name.toLowerCase();
            if (pdfFile.type !== "application/pdf" && !fileName.endsWith(".pdf")) {
                e.pdfFile = t("uploadStageEmployeur.errors.pdfFormat");
            } else if (pdfFile.size > MAX_FILE_SIZE_BYTES) {
                e.pdfFile = t("uploadStageEmployeur.errors.pdfSize");
            }
        }

        if (remuneration !== "" && remuneration < MIN_REMUNERATION) {
            e.remuneration = t("uploadStageEmployeur.errors.remunerationMin");
        }

        setErrors(e);
        return Object.keys(e).length === 0;
    }

    function convertDateToIso(ddmmyyyy) {
        const parts = ddmmyyyy.split("-");
        if (parts.length !== 3) return null;
        const [dd, mm, yyyy] = parts;
        return `${yyyy}-${mm.padStart(2, "0")}-${dd.padStart(2, "0")}`;
    }

    function handleFileChange(e) {
        const file = e.target.files?.[0] || null;
        if (!file) {
            setPdfFile(null);
            setErrors(prev => ({ ...prev, pdfFile: undefined }));
            return;
        }
        const fileName = file.name.toLowerCase();
        if (file.type !== "application/pdf" && !fileName.endsWith(".pdf")) {
            setErrors(prev => ({ ...prev, pdfFile: t("uploadStageEmployeur.errors.pdfFormat") }));
            setPdfFile(null);
            return;
        }
        if (file.size > MAX_FILE_SIZE_BYTES) {
            setErrors(prev => ({ ...prev, pdfFile: t("uploadStageEmployeur.errors.pdfSize") }));
            setPdfFile(null);
            return;
        }
        setPdfFile(file);
        setErrors(prev => ({ ...prev, pdfFile: undefined }));
    }

    function clearFile() {
        setPdfFile(null);
    }
    async function handleSubmit(evt) {
        evt.preventDefault();
        if (!validateFields()) return;

        setSubmitting(true);
        try {
            const isoDate = convertDateToIso(startDate.trim());
            const offer = {
                description: description.trim(),
                startDate: isoDate,
                durationInWeeks: Number(duration),
                address: address.trim(),
                remuneration: remuneration === "" ? null : Number(remuneration)
            };
            const token = sessionStorage.getItem("accessToken");
            const created = await uploadStageEmployeur(offer, pdfFile, token);
            const status = created.status || created.statut || "UNKNOWN";
            const statusUpper = status.toUpperCase();

            let statusText = "";
            if (statusUpper === "PENDING" || statusUpper === "PENDING_VALIDATION") {
                statusText = t("pendingOffers.status.pendingapproval");
            } else if (statusUpper === "APPROVED" || statusUpper === "PUBLISHED") {
                statusText = t("pendingOffers.status.approved");
            } else if (statusUpper === "REJECTED") {
                statusText = t("pendingOffers.status.rejected");
            } else {
                statusText = t("pendingOffers.status.unknown");
            }

            setToast({
                show: true,
                message: `${t("uploadStageEmployeur.successMessage")} ${statusText}`,
                type: 'success'
            });

            setDescription("");
            setStartDate("");
            setDuration("");
            setAddress("");
            setRemuneration("");
            setPdfFile(null);
            setErrors({});
        } catch (err) {
            const msg = err.response?.data?.message || err.response?.data || err.message || t("uploadStageEmployeur.errors.serverError");
            setToast({
                show: true,
                message: `${t("uploadStageEmployeur.errors.serverError")} ${msg}`,
                type: 'error'
            });
        } finally {
            setSubmitting(false);
        }
    }
    const isFormDisabled = toast.show && toast.type === 'success';

    return (
        <>
            <div className="max-w-3xl mx-auto my-8">
                <div className="bg-white shadow-lg rounded-lg p-8 relative">
                    {isFormDisabled && (
                        <div className="absolute inset-0 bg-white bg-opacity-75 z-10 rounded-lg"></div>
                    )}

                    <h2 className="text-2xl font-semibold text-gray-800 mb-4">{t("uploadStageEmployeur.title")}</h2>

                    <form onSubmit={handleSubmit} noValidate>
                        <div className="grid grid-cols-1 gap-6">
                            <div>
                                <label htmlFor="description" className="block text-sm font-medium text-gray-700">
                                    {t("uploadStageEmployeur.description")}
                                    <span className="text-gray-500 text-xs ml-2">
                                        ({description.length}/{MAX_DESCRIPTION_LENGTH})
                                    </span>
                                </label>
                                <textarea
                                    id="description"
                                    value={description}
                                    onChange={e => setDescription(e.target.value)}
                                    rows={3}
                                    maxLength={MAX_DESCRIPTION_LENGTH}
                                    placeholder={t("uploadStageEmployeur.descriptionPlaceholder")}
                                    disabled={isFormDisabled}
                                    className={`mt-1 block w-full rounded-md border px-3 py-2 shadow-sm focus:outline-none focus:ring-2 focus:ring-indigo-500 transition resize-none ${errors.description ? "border-red-400 bg-red-50" : "border-gray-300 bg-white"} ${isFormDisabled ? "opacity-50 cursor-not-allowed" : ""}`}
                                />
                                {errors.description && <p className="mt-1 text-sm text-red-600">{errors.description}</p>}
                            </div>

                            <div className="grid grid-cols-1 md:grid-cols-2 gap-4">
                                <div>
                                    <label htmlFor="startDate" className="block text-sm font-medium text-gray-700">
                                        {t("uploadStageEmployeur.startDate")}
                                    </label>
                                    <DatePicker
                                        id="startDate"
                                        selected={startDate ? (() => {
                                            try {
                                                const [day, month, year] = startDate.split('-');
                                                const dateObj = new Date(Number(year), Number(month) - 1, Number(day));
                                                return isNaN(dateObj.getTime()) ? null : dateObj;
                                            } catch {
                                                return null;
                                            }
                                        })() : null}
                                        onChange={(date) => {
                                            if (date) {
                                                const day = String(date.getDate()).padStart(2, '0');
                                                const month = String(date.getMonth() + 1).padStart(2, '0');
                                                const year = date.getFullYear();
                                                setStartDate(`${day}-${month}-${year}`);
                                            } else {
                                                setStartDate('');
                                            }
                                        }}
                                        minDate={new Date()}
                                        dateFormat="dd-MM-yyyy"
                                        placeholderText={t("uploadStageEmployeur.startDatePlaceholder")}
                                        showMonthDropdown
                                        showYearDropdown
                                        dropdownMode="select"
                                        disabled={isFormDisabled}
                                        className={`mt-1 block w-full rounded-md border px-3 py-2 shadow-sm focus:outline-none focus:ring-2 focus:ring-indigo-500 transition ${errors.startDate ? "border-red-400 bg-red-50" : "border-gray-300 bg-white"} ${isFormDisabled ? "opacity-50 cursor-not-allowed" : ""}`}
                                        wrapperClassName="w-full"
                                        calendarClassName="shadow-lg"
                                    />
                                    {errors.startDate && <p className="mt-1 text-sm text-red-600">{errors.startDate}</p>}
                                </div>
                                <div>
                                    <label htmlFor="duration" className="block text-sm font-medium text-gray-700">
                                        {t("uploadStageEmployeur.duration")}
                                    </label>
                                    <input
                                        id="duration"
                                        type="number"
                                        value={duration}
                                        onChange={e => setDuration(e.target.value)}
                                        min={MIN_DURATION_WEEKS}
                                        max={MAX_DURATION_WEEKS}
                                        disabled={isFormDisabled}
                                        className={`mt-1 block w-full rounded-md border px-3 py-2 shadow-sm focus:outline-none focus:ring-2 focus:ring-indigo-500 transition ${errors.duration ? "border-red-400 bg-red-50" : "border-gray-300 bg-white"} ${isFormDisabled ? "opacity-50 cursor-not-allowed" : ""}`}
                                    />
                                    {errors.duration && <p className="mt-1 text-sm text-red-600">{errors.duration}</p>}
                                </div>
                            </div>

                            <div>
                                <label htmlFor="address" className="block text-sm font-medium text-gray-700">
                                    {t("uploadStageEmployeur.address")}
                                </label>
                                <input
                                    id="address"
                                    type="text"
                                    value={address}
                                    onChange={e => setAddress(e.target.value)}
                                    placeholder={t("uploadStageEmployeur.addressPlaceholder")}
                                    disabled={isFormDisabled}
                                    className={`mt-1 block w-full rounded-md border px-3 py-2 shadow-sm focus:outline-none focus:ring-2 focus:ring-indigo-500 transition ${errors.address ? "border-red-400 bg-red-50" : "border-gray-300 bg-white"} ${isFormDisabled ? "opacity-50 cursor-not-allowed" : ""}`}
                                />
                                {errors.address && <p className="mt-1 text-sm text-red-600">{errors.address}</p>}
                            </div>

                            <div className="grid grid-cols-1 md:grid-cols-2 gap-4">
                                <div>
                                    <label htmlFor="remuneration" className="block text-sm font-medium text-gray-700">
                                        {t("uploadStageEmployeur.remuneration")}
                                    </label>
                                    <input
                                        id="remuneration"
                                        type="number"
                                        value={remuneration}
                                        onChange={e => setRemuneration(e.target.value)}
                                        min={MIN_REMUNERATION}
                                        step="0.01"
                                        disabled={isFormDisabled}
                                        className={`mt-1 block w-full rounded-md border px-3 py-2 shadow-sm focus:outline-none focus:ring-2 focus:ring-indigo-500 transition ${errors.remuneration ? "border-red-400 bg-red-50" : "border-gray-300 bg-white"} ${isFormDisabled ? "opacity-50 cursor-not-allowed" : ""}`}
                                    />
                                    {errors.remuneration && <p className="mt-1 text-sm text-red-600">{errors.remuneration}</p>}
                                </div>
                            </div>

                            <div>
                                <label className="block text-sm font-medium text-gray-700">
                                    {t("uploadStageEmployeur.pdfDocument")}
                                </label>
                                <div className="mt-1 flex items-center gap-3">
                                    <label htmlFor="pdfFile" className={`inline-flex items-center px-4 py-2 border border-transparent text-sm font-medium rounded-md shadow-sm text-white ${isFormDisabled ? "bg-indigo-400 cursor-not-allowed" : "bg-indigo-600 hover:bg-indigo-700 cursor-pointer"}`}>
                                        {t("uploadStageEmployeur.choosePdf")}
                                    </label>
                                    <input id="pdfFile" type="file" accept="application/pdf" onChange={handleFileChange} disabled={isFormDisabled} className="hidden" />
                                    <div className="flex-1 text-sm text-gray-700">
                                        {pdfFile ? (
                                            <div className="flex items-center justify-between gap-3">
                                                <div className="truncate">
                                                    <span className="font-medium">{pdfFile.name}</span>
                                                    <span className="ml-2 text-gray-500 text-xs">({(pdfFile.size / BYTES_IN_MB).toFixed(2)} MB)</span>
                                                </div>
                                                <button type="button" onClick={clearFile} disabled={isFormDisabled} className={`text-sm text-red-600 ${isFormDisabled ? "opacity-50 cursor-not-allowed" : "hover:underline"}`}>
                                                    {t("uploadStageEmployeur.remove")}
                                                </button>
                                            </div>
                                        ) : (
                                            <div className="text-gray-500">{t("uploadStageEmployeur.noFileSelected")}</div>
                                        )}
                                    </div>
                                </div>
                                {errors.pdfFile && <p className="mt-1 text-sm text-red-600">{errors.pdfFile}</p>}
                            </div>

                            <div className="flex flex-row gap-4 pt-4">
                                <button
                                    type="submit"
                                    disabled={submitting || isFormDisabled}
                                    className={`inline-flex items-center justify-center gap-2 w-full md:w-auto px-6 py-3 border border-transparent text-sm font-medium rounded-md shadow-sm text-white ${(submitting || isFormDisabled) ? "bg-indigo-400 cursor-not-allowed" : "bg-indigo-600 hover:bg-indigo-700"} focus:outline-none focus:ring-2 focus:ring-offset-2 focus:ring-indigo-500 transition`}
                                >
                                    {submitting && <div className="w-4 h-4 border-2 border-white border-t-transparent rounded-full animate-spin"></div>}
                                    {submitting ? t("uploadStageEmployeur.submitting") : t("uploadStageEmployeur.submit")}
                                </button>

                                <button
                                    type="button"
                                    onClick={navigateToDashboard}
                                    className="inline-flex items-center justify-center gap-2 px-6 py-3 border border-gray-300 text-sm font-medium rounded-md shadow-sm text-gray-700 bg-white hover:bg-gray-50"
                                >
                                    {t("uploadStageEmployeur.backToDashboard")}
                                </button>
                            </div>
                        </div>
                    </form>
                </div>
            </div>

            {toast.show && (
                <div className={`fixed bottom-6 right-6 px-6 py-4 rounded-lg shadow-lg z-50 flex items-center gap-3 transition-all duration-300 ${toast.type === 'success' ? 'bg-green-500 text-white' : 'bg-red-500 text-white'}`}>
                    {toast.type === 'success' ? (
                        <svg className="w-6 h-6" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                            <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M5 13l4 4L19 7"/>
                        </svg>
                    ) : (
                        <svg className="w-6 h-6" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                            <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M6 18L18 6M6 6l12 12"/>
                        </svg>
                    )}
                    <span className="font-medium">{toast.message}</span>
                    <button
                        onClick={closeToast}
                        className={`ml-2 rounded-full p-1 transition-colors ${toast.type === 'success' ? 'bg-green-600 hover:bg-green-700' : 'bg-red-600 hover:bg-red-700'}`}
                        aria-label="Close"
                    >
                        <svg className="w-4 h-4 text-black" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                            <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M6 18L18 6M6 6l12 12"/>
                        </svg>
                    </button>
                </div>
            )}
        </>
    );
}