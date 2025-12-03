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
import {getStudentEntentes, previewStudentEntentePdf} from "../../api/apiStudent.jsx";

export default function StudentEntentesListe() {
    const {t} = useTranslation();
    const [ententes, setEntentes] = useState([]);
    const [loading, setLoading] = useState(true);
    const [sortField, setSortField] = useState("dateCreation");
    const [sortDirection, setSortDirection] = useState("desc");
    const [pdfToPreview, setPdfToPreview] = useState(null);
    const [toast, setToast] = useState({show: false, message: '', type: ''});
    const [signatureModal, setSignatureModal] = useState({show: false, entente: null});

    useEffect(() => {
        fetchAgreements();
    }, []);

    const closeToast = () => {
        setToast({show: false, message: '', type: ''});
    };

    const showToast = (message, type = 'error') => {
        setToast({show: true, message, type});
    };

    const fetchAgreements = async () => {
        try {
            const token = sessionStorage.getItem("accessToken");
            const userData = await getCurrentUser(token);
            const allEntentes = await getStudentEntentes(token);
            const studentEntentes = allEntentes.filter(entente => {
                const studentEmail = entente.student?.email;
                return studentEmail === userData.email;
            });
            setEntentes(studentEntentes);
        } catch (error) {
            console.error("Error fetching agreements:", error);
            showToast(t("studentEntentes.error"));
        } finally {
            setLoading(false);
        }
    };

    const handleViewPdf = async (ententeId) => {
        try {
            const token = sessionStorage.getItem("accessToken");
            const blob = await previewStudentEntentePdf(ententeId, token);
            setPdfToPreview(blob);
        } catch (error) {
            console.error("Erreur lors de la visualisation du PDF:", error);
            if (error.message && error.message.includes("404")) {
                showToast(t("studentEntentes.pdfNotFound"));
            } else if (error.message && error.message.includes("403")) {
                showToast(t("studentEntentes.accessDenied"));
            } else {
                showToast(t("studentEntentes.pdfViewError"));
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
                name: entente.internshipOffer?.employeurDto?.companyName || t("studentEntentes.employer")
            },
            etudiant: {
                signed: entente.dateSignatureEtudiant !== null,
                date: entente.dateSignatureEtudiant,
                name: `${entente.student?.firstName} ${entente.student?.lastName}` || t("studentEntentes.student")
            },
            gestionnaire: {
                signed: entente.dateSignatureGestionnaire !== null,
                date: entente.dateSignatureGestionnaire,
                name: t("studentEntentes.manager")
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

    const hasStudentSigned = (entente) => {
        return entente.etudiantASigne || entente.dateSignatureEtudiant !== null;
    };

    const getSortedEntentes = () => {
        return [...ententes].sort((a, b) => {
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
        const studentSigned = entente.etudiantASigne || entente.dateSignatureEtudiant !== null;

        if (entente.statut === 'VALIDEE') {
            return (
                <button
                    onClick={() => handleStatusClick(entente)}
                    className="px-2 py-1 rounded-full text-xs font-medium bg-green-100 text-green-800 hover:bg-green-200 transition-colors cursor-pointer"
                >
                    {t("studentEntentes.statusValidated")}
                </button>
            );
        }

        if (studentSigned && entente.statut === 'EN_ATTENTE_SIGNATURE') {
            return (
                <button
                    onClick={() => handleStatusClick(entente)}
                    className="px-2 py-1 rounded-full text-xs font-medium bg-blue-100 text-blue-800 hover:bg-blue-200 transition-colors cursor-pointer"
                >
                    {t("studentEntentes.statusAwaitingOthers")}
                </button>
            );
        }

        if (entente.statut === 'EN_ATTENTE_SIGNATURE') {
            return (
                <button
                    onClick={() => handleStatusClick(entente)}
                    className="px-2 py-1 rounded-full text-xs font-medium bg-orange-100 text-orange-800 hover:bg-orange-200 transition-colors cursor-pointer"
                >
                    {t("studentEntentes.statusAwaitingYours")}
                </button>
            );
        }

        if (entente.statut === 'BROUILLON') {
            return (
                <button
                    onClick={() => handleStatusClick(entente)}
                    className="px-2 py-1 rounded-full text-xs font-medium bg-gray-100 text-gray-800 hover:bg-gray-200 transition-colors cursor-pointer"
                >
                    {t("studentEntentes.statusDraft")}
                </button>
            );
        }

        return (
            <button
                onClick={() => handleStatusClick(entente)}
                className="px-2 py-1 rounded-full text-xs font-medium bg-gray-100 text-gray-800 hover:bg-gray-200 transition-colors cursor-pointer"
            >
                {entente.statut}
            </button>
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
                    <p className="mt-4 text-gray-600">{t("studentEntentes.loading")}</p>
                </div>
            </div>
        );
    }

    const sortedEntentes = getSortedEntentes();

    const waitingForStudentSignature = sortedEntentes.filter(e =>
        e.statut === 'EN_ATTENTE_SIGNATURE' && !hasStudentSigned(e)
    ).length;

    return (
        <>
            {signatureModal.show && signatureModal.entente && (
                    <div className="fixed inset-0 bg-black bg-opacity-50 flex items-center justify-center z-50 p-4">
                        <div className="bg-white rounded-lg shadow-xl max-w-md w-full p-6">
                            <div className="flex justify-between items-center mb-4">
                                <h3 className="text-lg font-semibold text-gray-900">
                                    {t("studentEntentes.signatureStatus")}
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
                                                            className="font-medium text-gray-900">{t("studentEntentes.employer")}</span>
                                                    </div>
                                                    <p className="text-sm text-gray-600 mt-1">{signatures.employeur.name}</p>
                                                    {signatures.employeur.signed ? (
                                                        <p className="text-xs text-green-600 mt-1">
                                                            {t("studentEntentes.signedOn")} {formatDateTime(signatures.employeur.date)}
                                                        </p>
                                                    ) : (
                                                        <p className="text-xs text-orange-600 mt-1">{t("studentEntentes.awaitingSignature")}</p>
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
                                                            className="font-medium text-gray-900">{t("studentEntentes.student")}</span>
                                                    </div>
                                                    <p className="text-sm text-gray-600 mt-1">{signatures.etudiant.name}</p>
                                                    {signatures.etudiant.signed ? (
                                                        <p className="text-xs text-green-600 mt-1">
                                                            {t("studentEntentes.signedOn")} {formatDateTime(signatures.etudiant.date)}
                                                        </p>
                                                    ) : (
                                                        <p className="text-xs text-orange-600 mt-1">{t("studentEntentes.awaitingSignature")}</p>
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
                                                            className="font-medium text-gray-900">{t("studentEntentes.manager")}</span>
                                                    </div>
                                                    <p className="text-sm text-gray-600 mt-1">{signatures.gestionnaire.name}</p>
                                                    {signatures.gestionnaire.signed ? (
                                                        <p className="text-xs text-green-600 mt-1">
                                                            {t("studentEntentes.signedOn")} {formatDateTime(signatures.gestionnaire.date)}
                                                        </p>
                                                    ) : (
                                                        <p className="text-xs text-orange-600 mt-1">{t("studentEntentes.awaitingSignature")}</p>
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
                                    {t("studentEntentes.close")}
                                </button>
                            </div>
                        </div>
                    </div>
                )}

            <div className="bg-white shadow rounded-lg overflow-hidden">
                <div className="px-4 sm:px-6 py-4 border-b border-gray-200">
                    <h3 className="text-lg font-medium text-gray-900">{t("studentEntentes.title")}</h3>
                    <p className="text-sm text-gray-600">
                        {t("studentEntentes.subtitle")}
                    </p>
                </div>

                {sortedEntentes.length === 0 ? (
                    <div className="p-8 text-center">
                        <div className="max-w-md mx-auto">
                            <div
                                className="w-16 h-16 bg-gray-100 rounded-full flex items-center justify-center mx-auto mb-4">
                                <FaFileAlt className="text-2xl text-gray-400"/>
                            </div>
                            <h3 className="text-lg font-medium text-gray-900 mb-2">
                                {t("studentEntentes.noEntentes")}
                            </h3>
                            <p className="text-gray-500 mb-6">
                                {t("studentEntentes.noEntentesDescription")}
                            </p>
                        </div>
                    </div>
                ) : (
                    <>
                        <div className="hidden lg:block overflow-x-auto">
                            <table className="min-w-full divide-y divide-gray-200">
                                <thead className="bg-gray-50">
                                <tr>
                                    <th className="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">
                                        {t("studentEntentes.company")}
                                    </th>
                                    <th className="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">
                                        {t("studentEntentes.internship")}
                                    </th>
                                    <th className="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">
                                        {t("studentEntentes.period")}
                                    </th>
                                    <th className="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">
                                        {t("studentEntentes.professor")}
                                    </th>
                                    <th
                                        className="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider cursor-pointer hover:bg-gray-100"
                                        onClick={() => handleSort("statut")}
                                    >
                                        <div className="flex items-center space-x-1">
                                            <span>{t("studentEntentes.status")}</span>
                                            {getSortIcon("statut")}
                                        </div>
                                    </th>
                                    <th
                                        className="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider cursor-pointer hover:bg-gray-100"
                                        onClick={() => handleSort("dateCreation")}
                                    >
                                        <div className="flex items-center space-x-1">
                                            <span>{t("studentEntentes.createdOn")}</span>
                                            {getSortIcon("dateCreation")}
                                        </div>
                                    </th>
                                    <th className="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">
                                        {t("studentEntentes.actions")}
                                    </th>
                                </tr>
                                </thead>
                                <tbody className="bg-white divide-y divide-gray-200">
                                {sortedEntentes.map((entente) => (
                                    <tr key={entente.id} className="hover:bg-gray-50">
                                        <td className="px-6 py-4 whitespace-nowrap">
                                            <div className="text-sm font-medium text-gray-900">
                                                {entente.internshipOffer?.employeurDto?.companyName}
                                            </div>
                                        </td>
                                        <td className="px-6 py-4">
                                            <div className="text-sm text-gray-900 max-w-xs truncate">
                                                {entente.internshipOffer?.description}
                                            </div>
                                        </td>
                                        <td className="px-6 py-4 whitespace-nowrap">
                                            <div className="text-sm text-gray-900">
                                                {formatDate(entente.dateDebut)} - {formatDate(calculateDateFin(entente.dateDebut, entente.duree))}
                                            </div>
                                        </td>
                                        <td className="px-6 py-4 whitespace-nowrap">
                                            {entente.prof ? (
                                                <div className="flex items-center space-x-2">
                                                    <FaUser className="text-indigo-600"/>
                                                    <div>
                                                        <div className="text-sm font-medium text-gray-900">
                                                            {entente.prof.firstName} {entente.prof.lastName}
                                                        </div>
                                                        {entente.prof.department && (
                                                            <div className="text-xs text-gray-500">
                                                                {entente.prof.department}
                                                            </div>
                                                        )}
                                                    </div>
                                                </div>
                                            ) : (
                                                <span className="text-sm text-gray-400 italic">
                                                    {t("studentEntentes.noProfessor")}
                                                </span>
                                            )}
                                        </td>
                                        <td className="px-6 py-4 whitespace-nowrap">
                                            {getStatusBadge(entente)}
                                        </td>
                                        <td className="px-6 py-4 whitespace-nowrap text-sm text-gray-500">
                                            {formatDate(entente.dateCreation)}
                                        </td>
                                        <td className="px-6 py-4 whitespace-nowrap text-sm font-medium">
                                            <div className="flex items-center space-x-2">
                                                <button
                                                    onClick={() => handleViewPdf(entente.id)}
                                                    className="text-indigo-600 hover:text-indigo-900 flex items-center space-x-1"
                                                >
                                                    <FaEye className="text-sm"/>
                                                    <span>{t("studentEntentes.viewPdf")}</span>
                                                </button>
                                                {entente.statut === 'EN_ATTENTE_SIGNATURE' && !hasStudentSigned(entente) && (
                                                    <Link
                                                        to={`/dashboard/student/ententes/${entente.id}/signer`}
                                                        className="text-green-600 hover:text-green-900 flex items-center space-x-1"
                                                    >
                                                        <FaSignature className="text-sm"/>
                                                        <span>{t("studentEntentes.sign")}</span>
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
                                                {entente.internshipOffer?.employeurDto?.companyName}
                                            </h4>
                                            <p className="text-xs text-gray-600 mt-1 truncate">
                                                {entente.internshipOffer?.description}
                                            </p>
                                            <p className="text-xs text-gray-500 mt-1">
                                                {formatDate(entente.dateDebut)} - {formatDate(calculateDateFin(entente.dateDebut, entente.duree))}
                                            </p>
                                            {entente.prof && (
                                                <p className="text-xs text-gray-500 mt-1">
                                                    {t("studentEntentes.professor")}: {entente.prof.firstName} {entente.prof.lastName}
                                                </p>
                                            )}
                                            <p className="text-xs text-gray-500 mt-1">
                                                {formatDate(entente.dateCreation)}
                                            </p>
                                        </div>
                                    </div>

                                    <div className="flex items-center justify-center mb-3">
                                        {getStatusBadge(entente)}
                                    </div>

                                    <div className="flex flex-col gap-2">
                                        <button
                                            onClick={() => handleViewPdf(entente.id)}
                                            className="w-full inline-flex justify-center items-center px-3 py-2 text-sm text-indigo-600 bg-indigo-50 rounded-md hover:bg-indigo-100"
                                        >
                                            <FaEye className="mr-1"/>
                                            {t("studentEntentes.viewPdf")}
                                        </button>
                                        {entente.statut === 'EN_ATTENTE_SIGNATURE' && !hasStudentSigned(entente) && (
                                            <Link
                                                to={`/dashboard/student/ententes/${entente.id}/signer`}
                                                className="w-full inline-flex justify-center items-center px-3 py-2 text-sm text-green-600 bg-green-50 rounded-md hover:bg-green-100"
                                            >
                                                <FaSignature className="mr-1"/>
                                                {t("studentEntentes.sign")}
                                            </Link>
                                        )}
                                    </div>
                                </div>
                            ))}
                        </div>
                    </>
                )}
            </div>

            {sortedEntentes.length > 0 && (
                <div className="mt-6 grid grid-cols-1 gap-4 sm:grid-cols-3">
                        <div className="bg-white overflow-hidden shadow rounded-lg">
                            <div className="px-4 py-5 sm:p-6">
                                <dt className="text-sm font-medium text-gray-500 truncate">
                                    {t("studentEntentes.stats.total")}
                                </dt>
                                <dd className="mt-1 text-3xl font-semibold text-gray-900">
                                    {sortedEntentes.length}
                                </dd>
                            </div>
                        </div>
                        <div className="bg-white overflow-hidden shadow rounded-lg">
                            <div className="px-4 py-5 sm:p-6">
                                <dt className="text-sm font-medium text-gray-500 truncate">
                                    {t("studentEntentes.stats.awaitingSignature")}
                                </dt>
                                <dd className="mt-1 text-3xl font-semibold text-orange-600">
                                    {waitingForStudentSignature}
                                </dd>
                            </div>
                        </div>
                        <div className="bg-white overflow-hidden shadow rounded-lg">
                            <div className="px-4 py-5 sm:p-6">
                                <dt className="text-sm font-medium text-gray-500 truncate">
                                    {t("studentEntentes.stats.validated")}
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

            {toast.show && (
                <div
                    className={`fixed bottom-6 right-6 px-6 py-4 rounded-lg shadow-lg z-50 flex items-center gap-3 transition-all duration-300 ${toast.type === 'success' ? 'bg-green-500 text-white' : 'bg-red-500 text-white'}`}>
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
                        className={`ml-2 rounded-full p-1 transition-colors ${toast.type === 'success' ? 'bg-green-600 hover:bg-green-700' : 'bg-red-600 hover:bg-red-700'}`}
                        aria-label="Close"
                    >
                        <svg className="w-4 h-4 text-white" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                            <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2}
                                  d="M6 18L18 6M6 6l12 12"/>
                        </svg>
                    </button>
                </div>
            )}
        </>
    );
}