import React, { useState, useEffect } from "react";
import { useParams, Link, useNavigate } from "react-router-dom";
import { useTranslation } from "react-i18next";
import { FaArrowLeft, FaCalendarAlt, FaMapMarkerAlt, FaMoneyBillWave, FaSignature, FaCheck, FaClock}
    from "react-icons/fa"


export default function StageDetails(){
    const {id} = useParams()
    const navigate = useNavigate()
    const {t, i18n} = useTranslation();
    const [ententes, setEntentes] = useState([]);
    const [loading, setLoading] = useState(true);
    const [error, setError] = useState("")

    useEffect(() => {
        fetchAgreements();
    }, [id]);

    const fetchAgreements = async () => {
        try {
            const token = sessionStorage.getItem("accessToken");

            const userResponse = await fetch('http://localhost:8080/user/me', {
                headers: {
                    'Authorization': `Bearer ${token}`,
                    'Content-Type': 'application/json'
                }
            });

            if (userResponse.ok) {
                const userData = await userResponse.json();

                const ententesResponse = await fetch('http://localhost:8080/employeur/ententes', {
                    headers: {
                        'Authorization': `Bearer ${token}`,
                        'Content-Type': 'application/json'
                    }
                });

                if (ententesResponse.ok) {
                    const allEntentes = await ententesResponse.json();
                    const employerEntentes = allEntentes.filter(entente =>{
                            const employeurEmail = entente.internshipOffer?.employeurDto?.email;
                            return employeurEmail === userData.email;
                        }
                    );
                    setEntentes(employerEntentes);


                }
            }
        } catch (error) {
            console.error(t("stageDetails.errors.fetching_agreements"), error);
        } finally {
            setLoading(false);
        }
    };

    const entente = ententes.find(e => e.id === parseInt(id))

    const formatDate = (dateString) => {
        if (!dateString) return t("stageDetails.errors.not_defined");
        try {
            return new Date(dateString).toLocaleDateString('fr-FR');
        } catch {
            return t("stageDetails.errors.date_invalide");
        }
    };

    const calculateDateFin = (dateDebut, dureeSemaines) => {
        if (!dateDebut || !dureeSemaines) return null;
        try {
            const date = new Date(dateDebut);
            date.setDate(date.getDate() + (parseInt(dureeSemaines) * 7));
            return date.toISOString().split('T')[0];
        } catch {
            return null;
        }
    };

    const getSignatureStatus = (signatureDate, role) => {
        if (signatureDate) {
            return (
                <div className="flex items-center text-green-600">
                    <FaCheck className="mr-2" />
                    <span>{t("stageDetails.sign_date")} {formatDate(signatureDate)}</span>
                </div>
            );
        }
        return (
            <div className="flex items-center text-orange-500">
                <FaClock className="mr-2" />
                <span>{t("stageDetails.attente_signature")}{role}</span>
            </div>
        );
    };

    if (loading) {
        return (
            <div className="min-h-screen bg-gray-50 flex items-center justify-center">
                <div className="text-center">
                    <div className="animate-spin rounded-full h-12 w-12 border-b-2 border-indigo-600 mx-auto"></div>
                    <p className="mt-4 text-gray-600">{t("stageDetails.loading")}</p>
                </div>
            </div>
        );
    }

    if (error || !entente) {
        return (
            <div className="min-h-screen bg-gray-50 flex items-center justify-center">
                <div className="text-center">
                    <div className="w-16 h-16 bg-red-100 rounded-full flex items-center justify-center mx-auto mb-4">
                        <FaSignature className="text-2xl text-red-600" />
                    </div>
                    <h3 className="text-lg font-medium text-gray-900 mb-2">
                        {error || t("stageDetails.errors.agreement_notFound")}
                    </h3>
                    <Link
                        to="/dashboard/employeur/ententes"
                        className="inline-flex items-center px-4 py-2 bg-indigo-600 text-white rounded-lg hover:bg-indigo-700 transition"
                    >
                        <FaArrowLeft className="mr-2" />
                        {t("stageDetails.back")}
                    </Link>
                </div>
            </div>
        );
    }

    return (
        <div className="min-h-screen bg-gray-50 py-8">
            <div className="max-w-4xl mx-auto px-4 sm:px-6 lg:px-8">
                <div className="mb-8">
                    <div className="flex items-center justify-between">
                        <div>
                            <Link
                                to="/dashboard/employeur/ententes"
                                className="inline-flex items-center text-gray-600 hover:text-gray-800 mb-4"
                            >
                                <FaArrowLeft className="mr-2" />
                                {t("stageDetails.back")}
                            </Link>
                            <h1 className="text-3xl font-bold text-gray-900">{t("stageDetails.title")}</h1>
                            <p className="text-gray-600 mt-2">
                                {t("stageDetails.description")}
                            </p>
                        </div>

                        {/* Sign button if employer hasn't signed yet */}
                        {entente.statut === 'EN_ATTENTE_SIGNATURE' && !entente.employeurASigne && (
                            <Link
                                to={`/dashboard/employeur/ententes/${id}/signer`}
                                className="inline-flex items-center px-6 py-3 bg-green-600 text-white rounded-lg hover:bg-green-700 transition font-medium"
                            >
                                <FaSignature className="mr-2" />
                                {t("stageDetails.sign_agreement")}
                            </Link>
                        )}
                    </div>
                </div>

                <div className="bg-white shadow rounded-lg overflow-hidden">
                    {/* Student and Company Information */}
                    <div className="px-6 py-8 border-b border-gray-200">
                        <h2 className="text-xl font-semibold text-gray-900 mb-6">{t("stageDetails.general_informations")}</h2>
                        <div className="grid grid-cols-1 md:grid-cols-2 gap-6">
                            <div>
                                <h3 className="text-lg font-medium text-gray-900 mb-4">{t("stageDetails.student")}</h3>
                                <div className="space-y-2">
                                    <p className="text-gray-700">
                                        <span className="font-medium">{t("stageDetails.student_name")}</span> {entente.student?.firstName} {entente.student?.lastName}
                                    </p>
                                    <p className="text-gray-700">
                                        <span className="font-medium">{t("stageDetails.email")}</span> {entente.student?.email || t("stageDetails.email_notAvailable")}
                                    </p>
                                </div>
                            </div>
                            <div>
                                <h3 className="text-lg font-medium text-gray-900 mb-4">{t("stageDetails.company")}</h3>
                                <div className="space-y-2">
                                    <p className="text-gray-700">
                                        <span className="font-medium">{t("stageDetails.company_name")}</span> {entente.internshipOffer?.companyName}
                                    </p>
                                    <p className="text-gray-700">
                                        <span className="font-medium">{t("stageDetails.company_contact")}</span> {entente.internshipOffer?.employeurDto?.email}
                                    </p>
                                </div>
                            </div>
                        </div>
                    </div>

                    {/* Stage Details */}
                    <div className="px-6 py-8 border-b border-gray-200">
                        <h2 className="text-xl font-semibold text-gray-900 mb-6">{t("stageDetails.internship_details")}</h2>
                        <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-4 gap-6">
                            <div className="flex items-center">
                                <FaCalendarAlt className="text-gray-400 mr-3" />
                                <div>
                                    <p className="text-sm font-medium text-gray-500">{t("stageDetails.date_begin")}</p>
                                    <p className="text-gray-900">{formatDate(entente.dateDebut)}</p>
                                </div>
                            </div>
                            <div className="flex items-center">
                                <FaCalendarAlt className="text-gray-400 mr-3" />
                                <div>
                                    <p className="text-sm font-medium text-gray-500">{t("stageDetails.date_end")}</p>
                                    <p className="text-gray-900">{formatDate(calculateDateFin(entente.dateDebut, entente.duree))}</p>
                                </div>
                            </div>
                            <div className="flex items-center">
                                <FaCalendarAlt className="text-gray-400 mr-3" />
                                <div>
                                    <p className="text-sm font-medium text-gray-500">{t("stageDetails.duration")}</p>
                                    <p className="text-gray-900">{entente.duree} {t("stageDetails.weeks")}</p>
                                </div>
                            </div>
                            <div className="flex items-center">
                                <FaMapMarkerAlt className="text-gray-400 mr-3" />
                                <div>
                                    <p className="text-sm font-medium text-gray-500">{t("stageDetails.place")}</p>
                                    <p className="text-gray-900">{entente.lieu || t("StageDetails.place_notSpecified")}</p>
                                </div>
                            </div>
                        </div>

                        {/* Title */}
                        <div className="mt-6">
                            <h3 className="text-lg font-medium text-gray-900 mb-3">{t("stageDetails.title_internship")}</h3>
                            <p className="text-gray-700 bg-gray-50 p-4 rounded-lg">
                                {entente.internshipOffer?.description || t("stage_details.internship_description_notSpecified")}
                            </p>
                        </div>
                    </div>

                    {/* Missions and Objectives */}
                    <div className="px-6 py-8 border-b border-gray-200">
                        <h2 className="text-xl font-semibold text-gray-900 mb-6">{t("stageDetails.missions_objectifs")}</h2>
                        <div className="bg-gray-50 p-6 rounded-lg">
                            <p className="text-gray-700 whitespace-pre-wrap">
                                {entente.missionsObjectifs || t("stageDetails.internship_missions_notDefined")}
                            </p>
                        </div>
                    </div>

                    {/* Remuneration */}
                    <div className="px-6 py-8 border-b border-gray-200">
                        <h2 className="text-xl font-semibold text-gray-900 mb-6">{t("stageDetails.remuneration")}</h2>
                        <div className="flex flex-col items-center text-center mb-2">
                            <div className="flex items-center mb-2">
                                <FaMoneyBillWave className="text-gray-400 mr-3 text-xl"/>
                            </div>
                            <div>
                                <p className="text-gray-700">
                                    {entente.remuneration ? (
                                        <span className="text-lg font-semibold text-green-600">
                                            {entente.remuneration} {t("stageDetails.hourly")}
                                        </span>
                                        ) : (
                                        <span className="text-gray-500">{t("stageDetails.notRemunerated")}</span>
                                    )}
                                </p>
                            </div>
                        </div>
                    </div>

                    {/* Signatures */}
                    <div className="px-6 py-8">
                        <h2 className="text-xl font-semibold text-gray-900 mb-6">{t("stageDetails.signatures")}</h2>
                        <div className="space-y-4">
                            <div className="flex justify-between items-center p-4 bg-gray-50 rounded-lg">
                                <div>
                                    <h3 className="font-medium text-gray-900">{t("stageDetails.employer")}</h3>
                                    <p className="text-sm text-gray-500">{entente.internshipOffer?.companyName}</p>
                                </div>
                                {getSignatureStatus(entente.dateSignatureEmployeur, t("stageDetails.employer_role"))}
                            </div>

                            <div className="flex justify-between items-center p-4 bg-gray-50 rounded-lg">
                                <div>
                                    <h3 className="font-medium text-gray-900">{t("stageDetails.student")}</h3>
                                    <p className="text-sm text-gray-500">{entente.student?.firstName} {entente.student?.lastName}</p>
                                </div>
                                {getSignatureStatus(entente.dateSignatureEtudiant, t("stageDetails.student_role"))}
                            </div>

                            <div className="flex justify-between items-center p-4 bg-gray-50 rounded-lg">
                                <div>
                                    <h3 className="font-medium text-gray-900">{t("stageDetails.manager")}</h3>
                                    <p className="text-sm text-gray-500">{t("stageDetails.admin")}</p>
                                </div>
                                {getSignatureStatus(entente.dateSignatureGestionnaire, t("stageDetails.manager_role"))}
                            </div>
                        </div>

                        <div className="mt-6 p-4 bg-blue-50 rounded-lg">
                            <div className="flex flex-col items-center text-center">
                                <div className="flex items-center mb-3">
                                    <FaSignature className="text-blue-600 mr-3" />
                                    <h3 className="font-medium text-blue-900  ">{t("stageDetails.status.title")} </h3>
                                </div>
                                <p className="text-blue-700">
                                    {entente.statut === 'VALIDEE' ? t("stageDetails.status.validated") :
                                        entente.statut === 'EN_ATTENTE_SIGNATURE' ? t("stageDetails.status.waiting_signatures") :
                                            t("stageDetails.status.draft")}
                                </p>
                            </div>
                        </div>
                    </div>
                </div>
            </div>
        </div>
    );
}