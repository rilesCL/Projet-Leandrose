import React, {useEffect, useState} from "react";
import {useNavigate, useSearchParams} from "react-router-dom";
import {useTranslation} from "react-i18next";
import {FaArrowLeft} from "react-icons/fa";
import {creerEntente, getCandidaturesAcceptees} from "../../api/apiGestionnaire.jsx";

export default function CreateEntenteForm() {
    const {t} = useTranslation();
    const navigate = useNavigate();
    const [searchParams] = useSearchParams();
    const candidatureIdFromUrl = searchParams.get("candidatureId");

    const [candidatures, setCandidatures] = useState([]);
    const [loading, setLoading] = useState(false);
    const [error, setError] = useState(null);
    const [success, setSuccess] = useState(false);
    const [toast, setToast] = useState({
        show: false,
        message: '',
        type: 'success'
    })

    const [formData, setFormData] = useState({
        candidatureId: candidatureIdFromUrl || "",
        missionsObjectifs: ""
    });

    const [errors, setErrors] = useState({});

    useEffect(() => {
        const token = sessionStorage.getItem("accessToken");
        if (!token) {
            navigate("/login");
            return;
        }

        const fetchCandidatures = async () => {
            try {
                const data = await getCandidaturesAcceptees();
                setCandidatures(Array.isArray(data) ? data : []);
            } catch (err) {
                console.error("Erreur lors du chargement des candidatures:", err);
                setError(t("createEntenteForm.loadError"));
            }
        };

        fetchCandidatures();
    }, [navigate, t]);

    const handleChange = (e) => {
        const {name, value} = e.target;
        setFormData(prev => ({...prev, [name]: value}));
        if (errors[name]) setErrors(prev => ({...prev, [name]: null}));
    };

    const showToast = (message, type = 'success') => {
        setToast({
            show: true,
            message,
            type
        });
        setTimeout(() => {
            setToast(prev => ({...prev, show: false}));
        }, 5000)
    }
    const closeToast = () => {
        setToast({show: false, message: '', type: 'success'})

        if (success) {
            navigate("/dashboard/gestionnaire");
        }

    }

    const validateForm = () => {
        const newErrors = {};
        if (!formData.candidatureId)
            newErrors.candidatureId = t("createEntenteForm.candidatureRequired");
        if (!formData.missionsObjectifs || formData.missionsObjectifs.trim().length === 0)
            newErrors.missionsObjectifs = t("createEntenteForm.missionsRequired");

        setErrors(newErrors);
        return Object.keys(newErrors).length === 0;
    };

    const formatDateForBackend = (dateString) => {
        if (!dateString) return null;

        if (typeof dateString === 'string' && dateString.match(/^\d{4}-\d{2}-\d{2}$/)) {
            return dateString;
        }

        try {
            const date = new Date(dateString);
            if (isNaN(date.getTime())) return null;

            const year = date.getFullYear();
            const month = String(date.getMonth() + 1).padStart(2, '0');
            const day = String(date.getDate()).padStart(2, '0');
            return `${year}-${month}-${day}`;
        } catch (e) {
            console.error("Erreur formatage date:", e);
            return null;
        }
    };

    const handleSubmit = async (e) => {
        e.preventDefault();
        setToast({show: false, message: '', type: 'success'});
        if (!validateForm()) return;

        setLoading(true);
        setError(null);
        setSuccess(false);

        try {
            const selectedCandidature = candidatures.find(
                c => c.id === parseInt(formData.candidatureId)
            );

            if (!selectedCandidature) {
                throw new Error("Candidature non trouvée");
            }

            const payload = {
                candidatureId: parseInt(formData.candidatureId),
                dateDebut: formatDateForBackend(selectedCandidature.internshipOffer?.startDate),
                duree: selectedCandidature.internshipOffer?.durationInWeeks || 0,
                lieu: selectedCandidature.internshipOffer?.address || "",
                remuneration: selectedCandidature.internshipOffer?.remuneration || 0,
                missionsObjectifs: formData.missionsObjectifs.trim()
            };

            const result = await creerEntente(payload);


            if (result.error) {
                showToast(result.error.message || t("createEntenteForm.createError"), 'error');
            } else {
                showToast(t("createEntenteForm.success"), 'success');
                setSuccess(true);
            }
        } catch (err) {
            console.error("❌ Erreur création entente:", err);
            showToast(err.message || t("createEntenteForm.createError"), 'error');
        } finally {
            setLoading(false);
        }
    };

    const handleCancel = () => navigate("/dashboard/gestionnaire");

    const selectedCandidature = candidatures.find(
        c => c.id === parseInt(formData.candidatureId)
    );

    const formatDate = (dateString) => {
        if (!dateString) return t("createEntenteForm.notDefined");
        const date = new Date(dateString);
        return date.toLocaleDateString();
    };

    return (
        <div className="max-w-5xl mx-auto bg-white shadow rounded p-6">
            <button
                onClick={handleCancel}
                className="mb-4 flex items-center text-gray-600 hover:text-indigo-600 transition-colors"
            >
                <FaArrowLeft className="mr-2"/>
                <span>{t("createEntenteForm.back")}</span>
            </button>

            <h2 className="text-2xl font-semibold mb-6">{t("createEntenteForm.title")}</h2>

            {toast.show && (
                <div
                    className={`fixed bottom-6 right-6 px-6 py-4 rounded-lg shadow-lg z-50 flex items-center gap-3 transition-all duration-300 ${
                        toast.type === 'success'
                            ? 'bg-green-500 text-white'
                            : 'bg-red-500 text-white'
                    }`}
                >
                    {toast.type === 'success' ? (
                        <svg className="w-6 h-6" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                            <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M5 13l4 4L19 7"/>
                        </svg>
                    ) : (
                        <svg className="w-6 h-6" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                            <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2}
                                  d="M6 18L18 6M6 6l12 12"/>
                        </svg>
                    )}
                    <span className="font-medium">{toast.message}</span>
                    <button
                        onClick={closeToast}
                        className={`ml-2 rounded-full p-1 transition-colors ${
                            toast.type === 'success'
                                ? 'bg-green-600 hover:bg-green-700'
                                : 'bg-red-600 hover:bg-red-700'
                        }`}
                        aria-label="Close"
                    >
                        <svg className="w-4 h-4 text-white" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                            <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2}
                                  d="M6 18L18 6M6 6l12 12"/>
                        </svg>
                    </button>
                </div>
            )}

            <form onSubmit={handleSubmit} className="space-y-6">
                <div>
                    <label className="block text-sm font-medium text-gray-700 mb-1">
                        {t("createEntenteForm.candidatureLabel")}
                    </label>
                    <select
                        name="candidatureId"
                        value={formData.candidatureId}
                        onChange={handleChange}
                        disabled={!!candidatureIdFromUrl}
                        className={`w-full px-3 py-2 border rounded-md shadow-sm focus:ring-indigo-500 focus:border-indigo-500 ${
                            errors.candidatureId ? "border-red-500" : "border-gray-300"
                        } ${candidatureIdFromUrl ? "bg-gray-100" : ""}`}
                    >
                        <option value="">{t("createEntenteForm.candidaturePlaceholder")}</option>
                        {candidatures.map(c => (
                            <option key={c.id} value={c.id}>
                                {c.student.firstName} {c.student.lastName} - {c.internshipOffer.description}
                            </option>
                        ))}
                    </select>
                    {errors.candidatureId && (
                        <p className="validation-error mt-1 text-sm text-red-600">{errors.candidatureId}</p>
                    )}
                </div>

                {selectedCandidature && (
                    <div className="space-y-4">
                        <div className="p-4 bg-blue-50 rounded-lg border border-blue-200">
                            <h3 className="font-semibold text-blue-900 mb-3">
                                {t("createEntenteForm.studentInfo")}
                            </h3>
                            <div className="grid grid-cols-1 md:grid-cols-2 gap-3 text-sm">
                                <div>
                                    <span className="font-medium text-gray-700">
                                        {t("createEntenteForm.fullName")} :
                                    </span>
                                    <p className="text-gray-900">
                                        {selectedCandidature.student.firstName} {selectedCandidature.student.lastName}
                                    </p>
                                </div>
                                <div>
                                    <span className="font-medium text-gray-700">
                                        {t("createEntenteForm.email")} :
                                    </span>
                                    <p className="text-gray-900">{selectedCandidature.student.email}</p>
                                </div>
                                <div>
                                    <span className="font-medium text-gray-700">
                                        {t("createEntenteForm.studentNumber")} :
                                    </span>
                                    <p className="text-gray-900">{selectedCandidature.student.studentNumber}</p>
                                </div>
                                <div>
                                    <span className="font-medium text-gray-700">
                                        {t("createEntenteForm.program")} :
                                    </span>
                                    <p className="text-gray-900">{t(selectedCandidature.student.program)}</p>
                                </div>
                            </div>
                        </div>
                        <div className="p-4 bg-purple-50 rounded-lg border border-purple-200">
                            <h3 className="font-semibold text-purple-900 mb-3">
                                {t("createEntenteForm.companyInfo")}
                            </h3>
                            <div className="grid grid-cols-1 md:grid-cols-2 gap-3 text-sm">
                                <div>
                                    <span className="font-medium text-gray-700">
                                        {t("createEntenteForm.companyName")} :
                                    </span>
                                    <p className="text-gray-900">
                                        {selectedCandidature.internshipOffer.companyName}
                                    </p>
                                </div>
                                <div>
                                    <span className="font-medium text-gray-700">
                                        {t("createEntenteForm.offerDescription")} :
                                    </span>
                                    <p className="text-gray-900">
                                        {selectedCandidature.internshipOffer.description}
                                    </p>
                                </div>
                                <div>
                                    <span className="font-medium text-gray-700">
                                        {t("createEntenteForm.startDate")} :
                                    </span>
                                    <p className="text-gray-900">
                                        {formatDate(selectedCandidature.internshipOffer.startDate)}
                                    </p>
                                </div>
                                <div>
                                    <span className="font-medium text-gray-700">
                                        {t("createEntenteForm.duration")} :
                                    </span>
                                    <p className="text-gray-900">
                                        {selectedCandidature.internshipOffer.durationInWeeks} {t("createEntenteForm.weeks")}
                                    </p>
                                </div>
                                <div>
                                    <span className="font-medium text-gray-700">
                                        {t("createEntenteForm.location")} :
                                    </span>
                                    <p className="text-gray-900">
                                        {selectedCandidature.internshipOffer.address}
                                    </p>
                                </div>
                                <div>
                                    <span className="font-medium text-gray-700">
                                        {t("createEntenteForm.remuneration")} :
                                    </span>
                                    <p className="text-gray-900">
                                        {selectedCandidature.internshipOffer.remuneration
                                            ? `${selectedCandidature.internshipOffer.remuneration} $/h`
                                            : t("createEntenteForm.notPaid")}
                                    </p>
                                </div>
                            </div>
                        </div>
                    </div>
                )}

                <div>
                    <label className="block text-sm font-medium text-gray-700 mb-1">
                        {t("createEntenteForm.missionsLabel")}
                    </label>
                    <textarea
                        name="missionsObjectifs"
                        value={formData.missionsObjectifs}
                        onChange={handleChange}
                        rows="8"
                        placeholder={t("createEntenteForm.missionsPlaceholder")}
                        className={`w-full px-3 py-2 border rounded-md shadow-sm focus:ring-indigo-500 focus:border-indigo-500 ${
                            errors.missionsObjectifs ? "border-red-500" : "border-gray-300"
                        }`}
                    />
                    {errors.missionsObjectifs && (
                        <p className="validation-error mt-1 text-sm text-red-600">{errors.missionsObjectifs}</p>
                    )}
                </div>

                <div className="flex items-center justify-end space-x-3 pt-4 border-t">
                    <button
                        type="button"
                        onClick={handleCancel}
                        disabled={loading}
                        className="px-4 py-2 text-sm font-medium text-gray-700 bg-white border border-gray-300 rounded-md hover:bg-gray-50 disabled:opacity-50 transition-colors"
                    >
                        {t("createEntenteForm.cancelButton")}
                    </button>
                    <button
                        type="submit"
                        disabled={loading}
                        className="px-4 py-2 text-sm font-medium text-white bg-indigo-600 border border-transparent rounded-md hover:bg-indigo-700 disabled:opacity-50 transition-colors"
                    >
                        {loading ? t("createEntenteForm.submitting") : t("createEntenteForm.submitButton")}
                    </button>
                </div>
            </form>
        </div>
    );
}