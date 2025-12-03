import React, {useEffect, useState} from "react";
import {Link} from "react-router-dom";
import {useTranslation} from "react-i18next";
import {
    FaCheck,
    FaClock,
    FaEye,
    FaFileAlt,
    FaSignature,
    FaSort,
    FaSortDown,
    FaSortUp,
    FaTimes,
    FaUser
} from "react-icons/fa";
import PdfViewer from "../PdfViewer.jsx";
import {getCurrentUser} from "../../api/apiSignature.jsx";
import {getEmployeurEntentes, previewEmployeurEntentePdf} from "../../api/apiEmployeur.jsx";

export default function EmployeurListeStages({selectedTerm}) {
    const [ententes, setEntentes] = useState([]);
    const [filteredEntentes, setFilteredEntentes] = useState([]);
    const [loading, setLoading] = useState(true);
    const [sortField, setSortField] = useState("dateCreation");
    const {t} = useTranslation();
    const [sortDirection, setSortDirection] = useState("desc");
    const [pdfToPreview, setPdfToPreview] = useState(null);
    const [toast, setToast] = useState({show: false, message: '', type: ''});
    const [signatureModal, setSignatureModal] = useState({show: false, entente: null});

    useEffect(() => {
        if (!selectedTerm || !ententes.length) {
            setFilteredEntentes(ententes);
            return;
        }

        const filtered = ententes.filter(entente => {
            const offer = entente.internshipOffer;
            if (!offer || !offer.schoolTerm) return false;

            const termParts = offer.schoolTerm.trim().split(/\s+/);
            const offerSeason = termParts[0]?.toUpperCase();
            const offerYear = parseInt(termParts[1]);

            return offerSeason === selectedTerm.season && offerYear === selectedTerm.year;
        });

        setFilteredEntentes(filtered);
    }, [selectedTerm, ententes]);

    useEffect(() => {
        fetchAgreements();
    }, []);

    const showToast = (message, type = 'error') => {
        setToast({show: true, message, type});
    };

    const fetchAgreements = async () => {
        try {
            const token = sessionStorage.getItem("accessToken");
            const userData = await getCurrentUser(token);
            const allEntentes = await getEmployeurEntentes(token);
            const employerEntentes = allEntentes.filter(entente => {
                    const employeurEmail = entente.internshipOffer?.employeurDto?.email;
                    return employeurEmail === userData.email;
                }
            );
            setEntentes(employerEntentes);
        } catch (error) {
            console.error(t("ententeStage.errors_fetching_agreements"), error);
            showToast(t("ententeStage.errors.loading_agreements"));
        } finally {
            setLoading(false);
        }
    };

    const handleViewPdf = async (ententeId) => {
        try {
            const token = sessionStorage.getItem("accessToken");
            const blob = await previewEmployeurEntentePdf(ententeId, token);
            setPdfToPreview(blob);
        } catch (error) {
            console.error(t("ententeStage.errors.pdf_viewing"), error);
            if (error.message && error.message.includes("404")) {
                showToast(t("ententeStage.errors.pdf_agreementNotFound"));
            } else if (error.message && error.message.includes("403")) {
                showToast(t("ententeStage.errors.pdf_unauthorized"));
            } else {
                showToast(t("ententeStage.errors.pdf_unable_view"));
            }
        }
    };

    const handleStatusClick = (entente) => {
        setSignatureModal({show: true, entente});
    };

    const getSignatureStatus = (entente) => {
        return {
            employeur: {
                signed: entente.dateSignatureEmployeur !== null,
                date: entente.dateSignatureEmployeur,
                name: entente.internshipOffer?.employeurDto?.companyName || 'Employeur'
            },
            etudiant: {
                signed: entente.dateSignatureEtudiant !== null,
                date: entente.dateSignatureEtudiant,
                name: `${entente.student?.firstName} ${entente.student?.lastName}` || 'Étudiant'
            },
            gestionnaire: {
                signed: entente.dateSignatureGestionnaire !== null,
                date: entente.dateSignatureGestionnaire,
                name: 'Gestionnaire'
            }
        };
    };

    const handleSort = (field) => {
        if (sortField === field) {
            setSortDirection(sortDirection === "asc" ? "desc" : "asc");
        } else {
            setSortField(field);
            setSortDirection("asc");
        }
    };

    const hasEmployerSigned = (entente) => {
        return entente.employeurASigne || entente.dateSignatureEmployeur !== null;
    };

    const getSortedEntentes = () => {
        return [...filteredEntentes].sort((a, b) => {
            let aValue = a[sortField];
            let bValue = b[sortField];

            if (sortField.includes("date")) {
                aValue = new Date(aValue);
                bValue = new Date(bValue);
            }

            if (aValue < bValue) return sortDirection === "asc" ? -1 : 1;
            if (aValue > bValue) return sortDirection === "asc" ? 1 : -1;
            return 0;
        });
    };

    const getStatusBadge = (entente) => {
        const employerSigned = entente.employeurASigne || entente.dateSignatureEmployeur !== null;

        if (entente.statut === 'VALIDEE') {
            return (
                <button
                    onClick={() => handleStatusClick(entente)}
                    className="px-2 py-1 rounded-full text-xs font-medium bg-green-100 text-green-800 hover:bg-green-200 transition-colors cursor-pointer inline-flex items-center"
                >
                    <span className="w-2 h-2 bg-green-500 rounded-full mr-1"></span>
                    <span className="hidden sm:inline">{t("ententeStage.status.validation")}</span>
                    <span className="sm:hidden">✓</span>
                </button>
            );
        }

        if (employerSigned && entente.statut === 'EN_ATTENTE_SIGNATURE') {
            return (
                <button
                    onClick={() => handleStatusClick(entente)}
                    className="px-2 py-1 rounded-full text-xs font-medium bg-blue-100 text-blue-800 hover:bg-blue-200 transition-colors cursor-pointer inline-flex items-center"
                >
                    <span className="w-2 h-2 bg-blue-500 rounded-full mr-1"></span>
                    <span className="hidden sm:inline">{t("ententeStage.status.waiting_other_signatures")}</span>
                    <span className="sm:hidden">⏳</span>
                </button>
            );
        }

        if (entente.statut === 'EN_ATTENTE_SIGNATURE') {
            return (
                <button
                    onClick={() => handleStatusClick(entente)}
                    className="px-2 py-1 rounded-full text-xs font-medium bg-orange-100 text-orange-800 hover:bg-orange-200 transition-colors cursor-pointer inline-flex items-center"
                >
                    <span className="w-2 h-2 bg-orange-500 rounded-full mr-1"></span>
                    <span className="hidden sm:inline">{t("ententeStage.status.waiting_your_signature")}</span>
                    <span className="sm:hidden">✍️</span>
                </button>
            );
        }

        if (entente.statut === 'BROUILLON') {
            return (
                <button
                    onClick={() => handleStatusClick(entente)}
                    className="px-2 py-1 rounded-full text-xs font-medium bg-gray-100 text-gray-800 hover:bg-gray-200 transition-colors cursor-pointer inline-flex items-center"
                >
                    <span className="w-2 h-2 bg-gray-500 rounded-full mr-1"></span>
                    <span className="hidden sm:inline">{t("ententeStage.status.draft")}</span>
                    <span className="sm:hidden">📝</span>
                </button>
            );
        }

        return (
            <span className="px-2 py-1 rounded-full text-xs font-medium bg-gray-100 text-gray-800 inline-flex items-center">
                <span className="w-2 h-2 bg-gray-500 rounded-full mr-1"></span>
                {entente.statut}
            </span>
        );
    };

    const getSortIcon = (field) => {
        if (sortField !== field) return <FaSort className="text-gray-400"/>;
        return sortDirection === "asc" ? <FaSortUp className="text-indigo-600"/> :
            <FaSortDown className="text-indigo-600"/>;
    };

    const formatDate = (dateString) => {
        return new Date(dateString).toLocaleDateString('fr-FR');
    };

    const formatDateTime = (dateString) => {
        if (!dateString) return null;
        return new Date(dateString).toLocaleString('fr-FR', {
            day: '2-digit',
            month: '2-digit',
            year: 'numeric',
            hour: '2-digit',
            minute: '2-digit'
        });
    };

    const calculateDateFin = (dateDebut, dureeSemaines) => {
        if (!dateDebut || !dureeSemaines) return null;
        const date = new Date(dateDebut);
        date.setDate(date.getDate() + (dureeSemaines * 7));
        return date.toISOString().split('T')[0];
    };

    if (loading) {
        return (
            <div className="bg-gray-50 flex items-center justify-center py-8">
                <div className="text-center">
                    <div className="animate-spin rounded-full h-12 w-12 border-b-2 border-indigo-600 mx-auto"></div>
                    <p className="mt-4 text-gray-600">{t("ententeStage.loading")}</p>
                </div>
            </div>
        );
    }

    const sortedEntentes = getSortedEntentes();

    const waitingForEmployerSignature = sortedEntentes.filter(e =>
        e.statut === 'EN_ATTENTE_SIGNATURE' && !hasEmployerSigned(e)
    ).length;

    return (
        <div className="bg-gray-50">
            <div className="w-full px-4 sm:px-6 lg:px-8">
                {toast.show && (
                    <div
                        className={`fixed top-4 right-4 z-50 flex items-center gap-3 px-4 py-3 rounded-lg shadow-lg transition-all duration-300 ${
                            toast.type === 'error' ? 'bg-red-500 text-white' : 'bg-green-500 text-white'
                        }`}>
                        <span>{toast.message}</span>
                        <button
                            onClick={() => setToast({show: false, message: '', type: ''})}
                            className="text-white hover:text-gray-200 transition-colors"
                        >
                            <FaTimes className="text-lg"/>
                        </button>
                    </div>
                )}

                {signatureModal.show && signatureModal.entente && (
                    <div className="fixed inset-0 bg-black bg-opacity-50 flex items-center justify-center z-50 p-4">
                        <div className="bg-white rounded-lg shadow-xl max-w-md w-full p-6">
                            <div className="flex justify-between items-center mb-4">
                                <h3 className="text-lg font-semibold text-gray-900">
                                    {t("ententeStage.model.stage_signatures")}
                                </h3>
                                <button
                                    onClick={() => setSignatureModal({show: false, entente: null})}
                                    className="text-gray-400 hover:text-gray-600 transition-colors"
                                >
                                    <FaTimes className="text-xl"/>
                                </button>
                            </div>

                            <div className="space-y-4">
                                {(() => {
                                    const signatures = getSignatureStatus(signatureModal.entente);
                                    return (
                                        <>
                                            <div className="flex items-start space-x-3 p-3 bg-gray-50 rounded-lg">
                                                <div
                                                    className={`mt-1 ${signatures.employeur.signed ? 'text-green-600' : 'text-orange-500'}`}>
                                                    {signatures.employeur.signed ? (
                                                        <FaCheck className="text-xl"/>
                                                    ) : (
                                                        <FaClock className="text-xl"/>
                                                    )}
                                                </div>
                                                <div className="flex-1">
                                                    <div className="flex items-center space-x-2">
                                                        <FaUser className="text-gray-400"/>
                                                        <span
                                                            className="font-medium text-gray-900">{t("ententeStage.model.employer")}</span>
                                                    </div>
                                                    <p className="text-sm text-gray-600 mt-1">{signatures.employeur.name}</p>
                                                    {signatures.employeur.signed ? (
                                                        <p className="text-xs text-green-600 mt-1">
                                                            {t("ententeStage.model.sign_the")} {formatDateTime(signatures.employeur.date)}
                                                        </p>
                                                    ) : (
                                                        <p className="text-xs text-orange-600 mt-1">{t("ententeStage.model.waiting_signature")}</p>
                                                    )}
                                                </div>
                                            </div>

                                            <div className="flex items-start space-x-3 p-3 bg-gray-50 rounded-lg">
                                                <div
                                                    className={`mt-1 ${signatures.etudiant.signed ? 'text-green-600' : 'text-orange-500'}`}>
                                                    {signatures.etudiant.signed ? (
                                                        <FaCheck className="text-xl"/>
                                                    ) : (
                                                        <FaClock className="text-xl"/>
                                                    )}
                                                </div>
                                                <div className="flex-1">
                                                    <div className="flex items-center space-x-2">
                                                        <FaUser className="text-gray-400"/>
                                                        <span
                                                            className="font-medium text-gray-900">{t("ententeStage.model.student")}</span>
                                                    </div>
                                                    <p className="text-sm text-gray-600 mt-1">{signatures.etudiant.name}</p>
                                                    {signatures.etudiant.signed ? (
                                                        <p className="text-xs text-green-600 mt-1">
                                                            {t("ententeStage.model.sign_the")} {formatDateTime(signatures.etudiant.date)}
                                                        </p>
                                                    ) : (
                                                        <p className="text-xs text-orange-600 mt-1">{t("ententeStage.model.waiting_signature")}</p>
                                                    )}
                                                </div>
                                            </div>

                                            <div className="flex items-start space-x-3 p-3 bg-gray-50 rounded-lg">
                                                <div
                                                    className={`mt-1 ${signatures.gestionnaire.signed ? 'text-green-600' : 'text-orange-500'}`}>
                                                    {signatures.gestionnaire.signed ? (
                                                        <FaCheck className="text-xl"/>
                                                    ) : (
                                                        <FaClock className="text-xl"/>
                                                    )}
                                                </div>
                                                <div className="flex-1">
                                                    <div className="flex items-center space-x-2">
                                                        <FaUser className="text-gray-400"/>
                                                        <span
                                                            className="font-medium text-gray-900">{t("ententeStage.model.manager")}</span>
                                                    </div>
                                                    <p className="text-sm text-gray-600 mt-1">{signatures.gestionnaire.name}</p>
                                                    {signatures.gestionnaire.signed ? (
                                                        <p className="text-xs text-green-600 mt-1">
                                                            {t("ententeStage.model.sign_the")} {formatDateTime(signatures.gestionnaire.date)}
                                                        </p>
                                                    ) : (
                                                        <p className="text-xs text-orange-600 mt-1">{t("ententeStage.model.waiting_signature")}</p>
                                                    )}
                                                </div>
                                            </div>
                                        </>
                                    );
                                })()}
                            </div>

                            <div className="mt-6 flex justify-end">
                                <button
                                    onClick={() => setSignatureModal({show: false, entente: null})}
                                    className="px-4 py-2 bg-indigo-600 text-white rounded-lg hover:bg-indigo-700 transition-colors"
                                >
                                    {t("ententeStage.model.close")}
                                </button>
                            </div>
                        </div>
                    </div>
                )}

                <div className="mb-8">
                    <div>
                        <h1 className="text-2xl font-bold text-gray-900">{t("ententeStage.title")}</h1>
                        <p className="text-gray-600 mt-2">
                            {t("ententeStage.description")}
                        </p>
                    </div>
                </div>

                {sortedEntentes.length === 0 ? (
                    <div className="bg-white rounded-lg shadow p-8 text-center">
                        <div className="max-w-md mx-auto">
                            <div
                                className="w-16 h-16 bg-gray-100 rounded-full flex items-center justify-center mx-auto mb-4">
                                <FaFileAlt className="text-2xl text-gray-400"/>
                            </div>
                            <h3 className="text-lg font-medium text-gray-900 mb-2">
                                {selectedTerm ? t("ententeStage.noEntenteForTerm") : t("ententeStage.noneStagetitle")}
                            </h3>
                            <p className="text-gray-500 mb-6">
                                {selectedTerm
                                    ? t("ententeStage.noEntenteForTermDescription", {
                                        term: `${t(`terms.${selectedTerm.season}`)} ${selectedTerm.year}`
                                    })
                                    : t("ententeStage.noneStagedescription")
                                }
                            </p>
                        </div>
                    </div>
                ) : (
                    <>
                        <div className="bg-white shadow rounded-lg overflow-hidden">
                            <div className="hidden lg:block overflow-x-auto">
                                <table className="min-w-full divide-y divide-gray-200">
                                    <thead className="bg-gray-50">
                                    <tr>
                                        <th
                                            className="px-4 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider cursor-pointer hover:bg-gray-100"
                                            onClick={() => handleSort("studentNom")}
                                        >
                                            <div className="flex items-center space-x-1">
                                                <span>{t("ententeStage.student")}</span>
                                                {getSortIcon("studentNom")}
                                            </div>
                                        </th>
                                        <th className="px-4 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">
                                            {t("ententeStage.stage")}
                                        </th>
                                        <th className="px-4 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">
                                            {t("ententeStage.period")}
                                        </th>
                                        <th
                                            className="px-4 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider cursor-pointer hover:bg-gray-100"
                                            onClick={() => handleSort("statut")}
                                        >
                                            <div className="flex items-center space-x-1">
                                                <span>{t("ententeStage.status.title")}</span>
                                                {getSortIcon("statut")}
                                            </div>
                                        </th>
                                        <th className="px-4 py-3 text-center text-xs font-medium text-gray-500 uppercase tracking-wider">
                                            {t("ententeStage.actions.title")}
                                        </th>
                                    </tr>
                                    </thead>
                                    <tbody className="bg-white divide-y divide-gray-200">
                                    {sortedEntentes.map((entente) => (
                                        <tr key={entente.id} className="hover:bg-gray-50">
                                            <td className="px-4 py-4 whitespace-nowrap">
                                                <div className="text-sm font-medium text-gray-900">
                                                    {entente.student?.firstName} {entente.student?.lastName}
                                                </div>
                                            </td>
                                            <td className="px-4 py-4">
                                                <div className="text-sm text-gray-900 max-w-xs truncate">
                                                    {entente.internshipOffer?.description}
                                                </div>
                                            </td>
                                            <td className="px-4 py-4 whitespace-nowrap">
                                                <div className="text-sm text-gray-900">
                                                    {formatDate(entente.dateDebut)} - {formatDate(calculateDateFin(entente.dateDebut, entente.duree))}
                                                </div>
                                            </td>
                                            <td className="px-4 py-4 whitespace-nowrap">
                                                {getStatusBadge(entente)}
                                            </td>
                                            <td className="px-4 py-4 whitespace-nowrap text-sm font-medium">
                                                <div className="flex items-center justify-center gap-2">
                                                    <button
                                                        onClick={() => handleViewPdf(entente.id)}
                                                        className="inline-flex items-center px-3 py-2 text-indigo-600 hover:text-indigo-900 bg-indigo-50 hover:bg-indigo-100 rounded-md transition-colors"
                                                    >
                                                        <FaEye className="mr-1"/>
                                                        <span className="hidden xl:inline">{t("ententeStage.actions.look")}</span>
                                                    </button>
                                                    {entente.statut === 'EN_ATTENTE_SIGNATURE' && !hasEmployerSigned(entente) && (
                                                        <Link
                                                            to={`/dashboard/employeur/ententes/${entente.id}/signer`}
                                                            className="inline-flex items-center px-3 py-2 text-green-600 hover:text-green-900 bg-green-50 hover:bg-green-100 rounded-md transition-colors"
                                                        >
                                                            <FaSignature className="mr-1"/>
                                                            <span className="hidden xl:inline">{t("ententeStage.actions.sign")}</span>
                                                        </Link>
                                                    )}
                                                </div>
                                            </td>
                                        </tr>
                                    ))}
                                    </tbody>
                                </table>
                            </div>

                            <div className="lg:hidden divide-y divide-gray-200">
                                {sortedEntentes.map((entente) => (
                                    <div key={entente.id} className="p-4 hover:bg-gray-50">
                                        <div className="flex items-start justify-between mb-3">
                                            <div className="flex-1 min-w-0">
                                                <h4 className="text-sm font-semibold text-gray-900">
                                                    {entente.student?.firstName} {entente.student?.lastName}
                                                </h4>
                                                <p className="text-xs text-gray-600 mt-1 truncate">
                                                    {entente.internshipOffer?.description}
                                                </p>
                                                <p className="text-xs text-gray-500 mt-1">
                                                    {formatDate(entente.dateDebut)} - {formatDate(calculateDateFin(entente.dateDebut, entente.duree))}
                                                </p>
                                            </div>
                                        </div>

                                        <div className="flex items-center justify-center mb-3">
                                            {getStatusBadge(entente)}
                                        </div>

                                        <div className="flex items-center gap-2">
                                            <button
                                                onClick={() => handleViewPdf(entente.id)}
                                                className="flex-1 inline-flex items-center justify-center px-3 py-2 text-indigo-600 bg-indigo-50 hover:bg-indigo-100 rounded-md transition-colors"
                                            >
                                                <FaEye className="mr-1"/>
                                                <span className="text-sm">{t("ententeStage.actions.look")}</span>
                                            </button>
                                            {entente.statut === 'EN_ATTENTE_SIGNATURE' && !hasEmployerSigned(entente) && (
                                                <Link
                                                    to={`/dashboard/employeur/ententes/${entente.id}/signer`}
                                                    className="flex-1 inline-flex items-center justify-center px-3 py-2 text-green-600 bg-green-50 hover:bg-green-100 rounded-md transition-colors"
                                                >
                                                    <FaSignature className="mr-1"/>
                                                    <span className="text-sm">{t("ententeStage.actions.sign")}</span>
                                                </Link>
                                            )}
                                        </div>
                                    </div>
                                ))}
                            </div>
                        </div>
                    </>
                )}

                {sortedEntentes.length > 0 && (
                    <div className="mt-6 grid grid-cols-1 gap-4 sm:grid-cols-3">
                        <div className="bg-white overflow-hidden shadow rounded-lg">
                            <div className="px-4 py-5 sm:p-6">
                                <dt className="text-sm font-medium text-gray-500 truncate">
                                    {t("ententeStage.stats.totalEntentes")}
                                </dt>
                                <dd className="mt-1 text-3xl font-semibold text-gray-900">
                                    {sortedEntentes.length}
                                </dd>
                            </div>
                        </div>
                        <div className="bg-white overflow-hidden shadow rounded-lg">
                            <div className="px-4 py-5 sm:p-6">
                                <dt className="text-sm font-medium text-gray-500 truncate">
                                    {t("ententeStage.stats.waitingSignature")}
                                </dt>
                                <dd className="mt-1 text-3xl font-semibold text-orange-600">
                                    {waitingForEmployerSignature}
                                </dd>
                            </div>
                        </div>
                        <div className="bg-white overflow-hidden shadow rounded-lg">
                            <div className="px-4 py-5 sm:p-6">
                                <dt className="text-sm font-medium text-gray-500 truncate">
                                    {t("ententeStage.stats.agreementValidated")}
                                </dt>
                                <dd className="mt-1 text-3xl font-semibold text-green-600">
                                    {sortedEntentes.filter(e => e.statut === 'VALIDEE').length}
                                </dd>
                            </div>
                        </div>
                    </div>
                )}
                {pdfToPreview && (
                    <PdfViewer
                        file={pdfToPreview}
                        onClose={() => setPdfToPreview(null)}
                    />
                )}
            </div>
        </div>
    );
}