import React, {useEffect, useState} from "react";
import {useNavigate, useParams} from "react-router-dom";
import {
    applyToOffer,
    getMyCandidatures,
    getOfferDetails,
    getStudentCv,
    previewOfferPdfStudent
} from "../../api/apiStudent.jsx";
import {useTranslation} from "react-i18next";
import PdfViewer from "../PdfViewer.jsx";

export default function OfferDetailPage() {
    const {t} = useTranslation();
    const {offerId} = useParams();
    const navigate = useNavigate();

    const [offer, setOffer] = useState(null);
    const [cv, setCv] = useState(null);
    const [loading, setLoading] = useState(true);
    const [error, setError] = useState(null);
    const [applying, setApplying] = useState(false);
    const [applyError, setApplyError] = useState(null);
    const [applySuccess, setApplySuccess] = useState(false);
    const [alreadyApplied, setAlreadyApplied] = useState(false);
    const [previewPdfFile, setPreviewPdfFile] = useState(null);


    const translateSchoolTerm = (termString, tFn) => {
        if (!termString || typeof termString !== 'string') return '';
        const parts = termString.trim().split(/\s+/);
        const seasonKey = parts.shift().toUpperCase();
        const rest = parts.join(' ');
        const translationKey = `terms.${seasonKey}`;
        const translated = tFn ? tFn(translationKey) : translationKey;
        const seasonLabel = (translated === translationKey)
            ? (seasonKey.charAt(0).toUpperCase() + seasonKey.slice(1).toLowerCase())
            : translated;
        return rest ? `${seasonLabel} ${rest}` : seasonLabel;
    };


    useEffect(() => {
        async function fetchData() {
            setLoading(true);
            setError(null);

            try {
                const offerData = await getOfferDetails(offerId);
                setOffer(offerData);

                const cvData = await getStudentCv();
                setCv(cvData);

                const candidatures = await getMyCandidatures();
                if (Array.isArray(candidatures))
                    setAlreadyApplied(
                        candidatures.some((c) => String(c.internshipOffer?.id) === String(offerId))
                    );

            } catch (err) {
                console.error(err);
                setError(t("OfferDetailPage.error"));
            } finally {
                setLoading(false);
            }
        }

        fetchData();
    }, [offerId, t]);

    const formatDate = (dateString) =>
        dateString ? new Date(dateString).toLocaleDateString("fr-FR") : t("OfferDetailPage.startDate");

    const handlePreviewPdf = async () => {
        try {
            const blob = await previewOfferPdfStudent(offerId);
            setPreviewPdfFile(blob);
        } catch {
            alert(t("OfferDetailPage.downloadError"));
        }
    };

    const handleApply = async () => {
        if (alreadyApplied) return;
        if (!cv) {
            setApplyError(t("OfferDetailPage.applyCvMissing"));
            return;
        }
        if (cv.status === "PENDING") {
            setApplyError(t("OfferDetailPage.applyCvPending"));
            return;
        }
        if (cv.status === "REJECTED") {
            setApplyError(t("OfferDetailPage.applyCvRejected"));
            return;
        }

        setApplying(true);
        setApplyError(null);
        try {
            await applyToOffer(offerId, cv.id);
            setApplySuccess(true);
            setAlreadyApplied(true);
        } catch (err) {
            console.error(err);
            setApplyError(err.message || t("OfferDetailPage.applyError"));
        } finally {
            setApplying(false);
        }
    };

    if (loading)
        return (
            <div className="bg-white shadow rounded-lg p-8 text-center">
                <div className="animate-pulse">{t("OfferDetailPage.loading")}</div>
            </div>
        );
    if (error)
        return (
            <div className="bg-white shadow rounded-lg p-8 text-center">
                <p className="text-red-600">{error}</p>
            </div>
        );
    if (!offer)
        return (
            <div className="bg-white shadow rounded-lg p-8 text-center">
                <p>{t("OfferDetailPage.notFound")}</p>
            </div>
        );

    return (
        <div className="max-w-4xl mx-auto">
            <div className="bg-white shadow rounded-lg mb-6">

                <div className="px-6 py-4 border-b border-gray-200">
                    <button
                        onClick={() => navigate("/dashboard/student")}
                        className="text-indigo-600 hover:text-indigo-900 mb-2"
                    >
                        {t("OfferDetailPage.backToOffers")}
                    </button>
                    <h1 className="text-2xl font-bold text-gray-900">{offer.description}</h1>
                    <p className="text-gray-600 mt-1">{offer.companyName}</p>
                </div>


                <div className="px-6 py-4">

                    <div className="mb-4">
                        <h3 className="text-sm font-medium text-gray-500">
                            {t("OfferDetailPage.location")}
                        </h3>
                        <p className="mt-1 text-gray-900">{offer.address}</p>
                    </div>

                    <div className="grid grid-cols-1 md:grid-cols-2 gap-4">
                        <div>
                            <h3 className="text-sm font-medium text-gray-500">
                                {t("OfferDetailPage.startDate")}
                            </h3>
                            <p className="mt-1 text-gray-900">{formatDate(offer.startDate)}</p>
                        </div>

                        <div>
                            <h3 className="text-sm font-medium text-gray-500">
                                {t("OfferDetailPage.duration")}
                            </h3>
                            <p className="mt-1 text-gray-900">
                                {offer.durationInWeeks}{" "}
                                {t("OfferDetailPage.week", {count: offer.durationInWeeks})}
                            </p>
                        </div>

                        <div>
                            <h3 className="text-sm font-medium text-gray-500">
                                {t("terms.term")}
                            </h3>
                            <p className="mt-1 text-gray-900">
                                {translateSchoolTerm(offer.schoolTerm, t) || "Non spécifié"}
                            </p>
                        </div>

                        <div>
                            <h3 className="text-sm font-medium text-gray-500">
                                {t("OfferDetailPage.remuneration")}
                            </h3>
                            <p className="mt-1 text-gray-900">
                                {offer.remuneration
                                    ? `${offer.remuneration} $ / ${t("OfferDetailPage.hour")}`
                                    : t("OfferDetailPage.noRemuneration")}
                            </p>
                        </div>
                    </div>
                </div>


                <div className="px-6 py-4 bg-gray-50">
                    <h3 className="text-sm font-medium text-gray-500 mb-2">
                        {t("OfferDetailPage.employerContact")}
                    </h3>
                    <p className="text-gray-900">
                        {offer.employeurDto.firstName} {offer.employeurDto.lastName}
                    </p>
                    <p className="text-gray-600">{offer.employeurDto.email}</p>
                </div>


                <div className="px-6 py-4 border-t border-gray-200">
                    <button
                        onClick={handlePreviewPdf}
                        className="inline-flex items-center px-4 py-2 border border-gray-300 rounded-md shadow-sm text-sm font-medium text-gray-700 bg-white hover:bg-gray-50"
                    >
                        <span className="mr-2">📄</span>
                        {t("previewPdf.preview")}
                    </button>
                </div>
            </div>

            <div className="bg-white shadow rounded-lg p-6">
                <h2 className="text-lg font-medium text-gray-900 mb-4">
                    {t("OfferDetailPage.applyTitle")}
                </h2>
                {applySuccess ? (
                    <div className="bg-green-50 border border-green-200 rounded-md p-4">
                        <p className="text-green-800">
                            {t("OfferDetailPage.applySuccess")}
                        </p>
                    </div>
                ) : (
                    <>
                        {!cv && (
                            <div className="bg-yellow-50 border border-yellow-200 rounded-md p-4 mb-4">
                                <p className="text-yellow-800">
                                    {t("OfferDetailPage.applyCvMissing")}
                                </p>
                            </div>
                        )}
                        {cv && cv.status === "PENDING" && (
                            <div className="bg-yellow-50 border border-yellow-200 rounded-md p-4 mb-4">
                                <p className="text-yellow-800">
                                    {t("OfferDetailPage.applyCvPending")}
                                </p>
                            </div>
                        )}
                        {cv && cv.status === "REJECTED" && (
                            <div className="bg-red-50 border border-red-200 rounded-md p-4 mb-4">
                                <p className="text-red-800">
                                    {t("OfferDetailPage.applyCvRejected")}
                                </p>
                            </div>
                        )}
                        {applyError && (
                            <div className="bg-red-50 border border-red-200 rounded-md p-4 mb-4">
                                <p className="text-red-800">{applyError}</p>
                            </div>
                        )}
                        <button
                            onClick={handleApply}
                            disabled={
                                applying ||
                                !cv ||
                                cv.status !== "APPROVED" ||
                                alreadyApplied ||
                                applySuccess
                            }
                            className={`w-full px-4 py-2 rounded-md font-medium ${
                                applying ||
                                !cv ||
                                cv.status !== "APPROVED" ||
                                alreadyApplied ||
                                applySuccess
                                    ? "bg-gray-300 text-gray-500 cursor-not-allowed"
                                    : "bg-indigo-600 text-white hover:bg-indigo-700"
                            }`}
                        >
                            {applySuccess
                                ? t("OfferDetailPage.applyButtonSuccess")
                                : alreadyApplied
                                    ? t("OfferDetailPage.applyButtonAlready")
                                    : applying
                                        ? t("OfferDetailPage.applyButtonLoading")
                                        : t("OfferDetailPage.applyButton")}
                        </button>
                    </>
                )}
            </div>

            {previewPdfFile && (
                <PdfViewer file={previewPdfFile} onClose={() => setPreviewPdfFile(null)}/>
            )}
        </div>
    );
}
