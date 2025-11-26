import React, {useEffect, useState} from "react";
import {useNavigate} from "react-router-dom";
import {useTranslation} from "react-i18next";
import {getCandidaturesAcceptees, previewCv} from "../../api/apiGestionnaire.jsx";
import PdfViewer from "../PdfViewer.jsx";

export default function EntentesStagePage({selectedTerm}) {
    const {t} = useTranslation();
    const [candidatures, setCandidatures] = useState([]);
    const [filteredCandidatures, setFilteredCandidatures] = useState([]);
    const [loading, setLoading] = useState(true);
    const [error, setError] = useState(null);
    const [cvToPreview, setCvToPreview] = useState(null);
    const navigate = useNavigate();

    useEffect(() => {
        const token = sessionStorage.getItem("accessToken");
        if (!token) {
            navigate("/login");
            return;
        }

        const fetchData = async () => {
            try {
                setLoading(true);
                const data = await getCandidaturesAcceptees();
                setCandidatures(Array.isArray(data) ? data : []);
            } catch (err) {
                console.error("Erreur fetching candidatures:", err);
                setError(err.message || "Erreur serveur");
            } finally {
                setLoading(false);
            }
        };

        fetchData();
    }, [navigate]);

    useEffect(() => {
        if (!selectedTerm || !candidatures.length) {
            setFilteredCandidatures(candidatures);
            return;
        }

        const filtered = candidatures.filter(candidature => {
            const offer = candidature.internshipOffer;
            if (!offer || !offer.schoolTerm) return false;

            const termParts = offer.schoolTerm.trim().split(/\s+/);
            const offerSeason = termParts[0]?.toUpperCase();
            const offerYear = parseInt(termParts[1]);

            return offerSeason === selectedTerm.season && offerYear === selectedTerm.year;
        });

        setFilteredCandidatures(filtered);
    }, [selectedTerm, candidatures]);

    const handlePreviewCv = async (cvId) => {
        try {
            const blob = await previewCv(cvId);
            setCvToPreview(blob);
        } catch (err) {
            console.error("Erreur preview CV:", err);
            alert(`${t("ententesStagePage.previewError")} : ${err.message || ""}`);
        }
    };

    if (loading)
        return (
            <div className="bg-white shadow rounded p-4 text-center">
                {t("ententesStagePage.loading")}
            </div>
        );

    if (error)
        return (
            <div className="bg-white shadow rounded p-4 text-red-600 text-center">
                {t("ententesStagePage.error")} : {error}
            </div>
        );

    return (
        <div className="bg-white shadow rounded p-6">

            <h2 className="text-2xl font-semibold mb-6 text-gray-900">
                {t("ententesStagePage.title")}
            </h2>

            {filteredCandidatures.length === 0 ? (
                <p className="text-sm text-gray-600">
                    {selectedTerm
                        ? t("ententesStagePage.noApplicationsForTerm", {
                            term: `${t(`terms.${selectedTerm.season}`)} ${selectedTerm.year}`
                        })
                        : t("ententesStagePage.noApplications")
                    }
                </p>
            ) : (
                <div className="space-y-6">
                    {filteredCandidatures.map((c) => {
                        const student = c.student || {};
                        const offer = c.internshipOffer || {};
                        const cv = c.cv || null;
                        const entente = c.entente || null;

                        return (
                            <div
                                key={c.id}
                                className="border rounded-lg p-4 flex flex-col md:flex-row justify-between items-start md:items-center gap-4 md:gap-0 shadow-sm hover:shadow-md transition-shadow"
                            >
                                <div className="flex-1 space-y-1">
                                    <div className="text-lg font-medium text-gray-900">
                                        {student.fullName || `${student.firstName ?? ""} ${student.lastName ?? ""}`}
                                    </div>
                                    <div className="text-sm text-gray-700">
                                        {t("ententesStagePage.offer")} : {offer.description ?? t("ententesStagePage.notDefined")}
                                    </div>
                                    <div className="text-sm text-gray-700">
                                        {t("ententesStagePage.company")} : {offer.companyName ?? t("ententesStagePage.notDefined")}
                                    </div>
                                    <div className="text-sm text-gray-500">
                                        {t("ententesStagePage.startDate")} : {offer.startDate ? new Date(offer.startDate).toLocaleDateString() : t("ententesStagePage.notDefined")}
                                    </div>
                                    <div className="text-sm text-gray-500">
                                        {t("ententesStagePage.duration")} : {offer.durationInWeeks ? `${offer.durationInWeeks} ${t("ententesStagePage.weeks")}` : t("ententesStagePage.notDefined")}
                                    </div>
                                    <div className="text-sm text-gray-500">
                                        {t("ententesStagePage.address")} : {offer.address ?? t("ententesStagePage.notDefined")}
                                    </div>
                                    <div className="text-sm text-gray-500">
                                        {t("ententesStagePage.remuneration")} : {offer.remuneration ? `${offer.remuneration} $/h` : t("ententesStagePage.notDefined")}
                                    </div>
                                    {entente && (
                                        <div className="text-sm text-green-700 font-medium">
                                            {t("ententesStagePage.ententeCreated")} : {new Date(entente.creationDate).toLocaleDateString()}
                                        </div>
                                    )}
                                </div>

                                <div
                                    className="flex flex-col md:flex-row items-start md:items-center space-y-2 md:space-y-0 md:space-x-2">
                                    {cv && (
                                        <button
                                            onClick={() => handlePreviewCv(cv.id)}
                                            className="px-3 py-1 text-sm bg-indigo-50 text-indigo-700 rounded hover:bg-indigo-100 transition-colors"
                                        >
                                            {t("ententesStagePage.viewCv")}
                                        </button>
                                    )}
                                    {!entente && (
                                        <button
                                            onClick={() =>
                                                navigate(`/dashboard/gestionnaire/ententes/create?candidatureId=${c.id}`)
                                            }
                                            className="px-3 py-1 text-sm bg-green-50 text-green-700 rounded hover:bg-green-100 transition-colors"
                                        >
                                            {t("ententesStagePage.createEntente")}
                                        </button>
                                    )}
                                    {entente && (
                                        <span className="px-3 py-1 text-sm bg-gray-100 text-gray-600 rounded">
                                            {t("ententesStagePage.ententeAlreadyCreated")}
                                        </span>
                                    )}
                                </div>
                            </div>
                        );
                    })}
                </div>
            )}

            {cvToPreview && (
                <PdfViewer
                    file={cvToPreview}
                    onClose={() => setCvToPreview(null)}
                />
            )}
        </div>
    );
}