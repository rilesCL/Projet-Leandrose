import React, { useState, useEffect } from "react";
import { Link, useNavigate } from "react-router-dom";
import { FaEye, FaSignature, FaSort, FaSortUp, FaSortDown, FaFileAlt } from "react-icons/fa";

export default function EmployeurListeStages() {
    const [ententes, setEntentes] = useState([]);
    const [loading, setLoading] = useState(true);
    const [sortField, setSortField] = useState("dateCreation");
    const [sortDirection, setSortDirection] = useState("desc");
    const navigate = useNavigate();

    useEffect(() => {
        fetchAgreements();
    }, []);

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
            console.error("Error fetching agreements:", error);
        } finally {
            setLoading(false);
        }
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
        const employerSigned = entente.employeurASigne || entente.dateSignatureEmployeur !== null;

        if (entente.statut === 'VALIDEE') {
            return <span className="px-2 py-1 rounded-full text-xs font-medium bg-green-100 text-green-800">Validée</span>;
        }

        if (employerSigned && entente.statut === 'EN_ATTENTE_SIGNATURE') {
            return <span className="px-2 py-1 rounded-full text-xs font-medium bg-blue-100 text-blue-800">En attente des autres signatures</span>;
        }

        if (entente.statut === 'EN_ATTENTE_SIGNATURE') {
            return <span className="px-2 py-1 rounded-full text-xs font-medium bg-orange-100 text-orange-800">En attente de votre signature</span>;
        }

        if (entente.statut === 'BROUILLON') {
            return <span className="px-2 py-1 rounded-full text-xs font-medium bg-gray-100 text-gray-800">Brouillon</span>;
        }

        return <span className="px-2 py-1 rounded-full text-xs font-medium bg-gray-100 text-gray-800">{entente.statut}</span>;
    };

    const getSortIcon = (field) => {
        if (sortField !== field) return <FaSort className="text-gray-400" />;
        return sortDirection === "asc" ? <FaSortUp className="text-indigo-600" /> : <FaSortDown className="text-indigo-600" />;
    };

    const formatDate = (dateString) => {
        return new Date(dateString).toLocaleDateString('fr-FR');
    };

    const calculateDateFin = (dateDebut, dureeSemaines) => {
        if (!dateDebut || !dureeSemaines) return null;
        const date = new Date(dateDebut);
        date.setDate(date.getDate() + (dureeSemaines * 7)); // Convert weeks to days
        return date.toISOString().split('T')[0]; // Return as YYYY-MM-DD
    };

    if (loading) {
        return (
            <div className="min-h-screen bg-gray-50 flex items-center justify-center">
                <div className="text-center">
                    <div className="animate-spin rounded-full h-12 w-12 border-b-2 border-indigo-600 mx-auto"></div>
                    <p className="mt-4 text-gray-600">Chargement des ententes...</p>
                </div>
            </div>
        );
    }

    const sortedEntentes = getSortedEntentes();

    const waitingForEmployerSignature = sortedEntentes.filter(e =>
        e.statut === 'EN_ATTENTE_SIGNATURE' && !hasEmployerSigned(e)
    ).length;

    return (
        <div className="min-h-screen bg-gray-50 py-8">
            <div className="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8">
                {/* Header */}
                <div className="mb-8">
                    <div className="flex justify-between items-center">
                        <div>
                            <h1 className="text-2xl font-bold text-gray-900">Ententes de stage</h1>
                            <p className="text-gray-600 mt-2">
                                Gérez et signez les ententes de stage avec vos stagiaires
                            </p>
                        </div>
                        <Link
                            to="/dashboard/employeur"
                            className="px-4 py-2 text-gray-600 hover:text-gray-800 transition"
                        >
                            ← Retour au tableau de bord
                        </Link>
                    </div>
                </div>

                {/* Agreements Table */}
                {sortedEntentes.length === 0 ? (
                    <div className="bg-white rounded-lg shadow p-8 text-center">
                        <div className="max-w-md mx-auto">
                            <div className="w-16 h-16 bg-gray-100 rounded-full flex items-center justify-center mx-auto mb-4">
                                <FaFileAlt className="text-2xl text-gray-400" />
                            </div>
                            <h3 className="text-lg font-medium text-gray-900 mb-2">
                                Aucune entente de stage
                            </h3>
                            <p className="text-gray-500 mb-6">
                                Vous n'avez aucune entente de stage en cours pour le moment.
                            </p>
                        </div>
                    </div>
                ) : (
                    <div className="bg-white shadow rounded-lg overflow-hidden">
                        <div className="overflow-x-auto">
                            <table className="min-w-full divide-y divide-gray-200">
                                <thead className="bg-gray-50">
                                <tr>
                                    <th
                                        className="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider cursor-pointer hover:bg-gray-100"
                                        onClick={() => handleSort("studentNom")}
                                    >
                                        <div className="flex items-center space-x-1">
                                            <span>Étudiant</span>
                                            {getSortIcon("studentNom")}
                                        </div>
                                    </th>
                                    <th className="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">
                                        Stage
                                    </th>
                                    <th className="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">
                                        Période
                                    </th>
                                    <th
                                        className="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider cursor-pointer hover:bg-gray-100"
                                        onClick={() => handleSort("statut")}
                                    >
                                        <div className="flex items-center space-x-1">
                                            <span>Statut</span>
                                            {getSortIcon("statut")}
                                        </div>
                                    </th>
                                    <th
                                        className="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider cursor-pointer hover:bg-gray-100"
                                        onClick={() => handleSort("dateCreation")}
                                    >
                                        <div className="flex items-center space-x-1">
                                            <span>Créée le</span>
                                            {getSortIcon("dateCreation")}
                                        </div>
                                    </th>
                                    <th className="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">
                                        Actions
                                    </th>
                                </tr>
                                </thead>
                                <tbody className="bg-white divide-y divide-gray-200">
                                {sortedEntentes.map((entente) => (
                                    <tr key={entente.id} className="hover:bg-gray-50">
                                        <td className="px-6 py-4 whitespace-nowrap">
                                            <div>
                                                <div className="text-sm font-medium text-gray-900">
                                                    {entente.student?.firstName} {entente.student?.lastName}
                                                </div>
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
                                            {getStatusBadge(entente)}
                                        </td>
                                        <td className="px-6 py-4 whitespace-nowrap text-sm text-gray-500">
                                            {formatDate(entente.dateCreation)}
                                        </td>
                                        <td className="px-6 py-4 whitespace-nowrap text-sm font-medium">
                                            <div className="flex items-center space-x-2">
                                                <Link
                                                    to={`/dashboard/employeur/ententes/${entente.id}`}
                                                    className="text-indigo-600 hover:text-indigo-900 flex items-center space-x-1"
                                                >
                                                    <FaEye className="text-sm" />
                                                    <span>Voir</span>
                                                </Link>
                                                {entente.statut === 'EN_ATTENTE_SIGNATURE' && !hasEmployerSigned(entente) && (
                                                    <Link
                                                        to={`/dashboard/employeur/ententes/${entente.id}/signer`}
                                                        className="text-green-600 hover:text-green-900 flex items-center space-x-1"
                                                    >
                                                        <FaSignature className="text-sm" />
                                                        <span>Signer</span>
                                                    </Link>
                                                )}
                                            </div>
                                        </td>
                                    </tr>
                                ))}
                                </tbody>
                            </table>
                        </div>
                    </div>
                )}

                {/* Stats */}
                {sortedEntentes.length > 0 && (
                    <div className="mt-6 grid grid-cols-1 gap-4 sm:grid-cols-3">
                        <div className="bg-white overflow-hidden shadow rounded-lg">
                            <div className="px-4 py-5 sm:p-6">
                                <dt className="text-sm font-medium text-gray-500 truncate">
                                    Total des ententes
                                </dt>
                                <dd className="mt-1 text-3xl font-semibold text-gray-900">
                                    {sortedEntentes.length}
                                </dd>
                            </div>
                        </div>
                        <div className="bg-white overflow-hidden shadow rounded-lg">
                            <div className="px-4 py-5 sm:p-6">
                                <dt className="text-sm font-medium text-gray-500 truncate">
                                    En attente de signature
                                </dt>
                                <dd className="mt-1 text-3xl font-semibold text-orange-600">
                                    {waitingForEmployerSignature}
                                </dd>
                            </div>
                        </div>
                        <div className="bg-white overflow-hidden shadow rounded-lg">
                            <div className="px-4 py-5 sm:p-6">
                                <dt className="text-sm font-medium text-gray-500 truncate">
                                    Ententes validées
                                </dt>
                                <dd className="mt-1 text-3xl font-semibold text-green-600">
                                    {sortedEntentes.filter(e => e.statut === 'VALIDEE').length}
                                </dd>
                            </div>
                        </div>
                    </div>
                )}
            </div>
        </div>
    );
}