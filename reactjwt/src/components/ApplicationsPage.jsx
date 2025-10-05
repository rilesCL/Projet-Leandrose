import React, { useEffect, useState } from "react";
import { getMyCandidatures } from "../api/apiStudent";
import { useNavigate } from "react-router-dom";

export default function ApplicationsPage() {
    const navigate = useNavigate();

    const [candidatures, setCandidatures] = useState([]);
    const [loading, setLoading] = useState(true);
    const [error, setError] = useState(null);

    useEffect(() => {
        async function fetchCandidatures() {
            setLoading(true);
            setError(null);

            try {
                const data = await getMyCandidatures();
                setCandidatures(Array.isArray(data) ? data : []);
            } catch (err) {
                console.error(err);
                setError("Impossible de charger vos candidatures");
            } finally {
                setLoading(false);
            }
        }

        fetchCandidatures();
    }, []);

    const formatDate = (dateString) => {
        if (!dateString) return "Non définie";
        return new Date(dateString).toLocaleDateString('fr-FR');
    };

    const getStatusBadge = (status) => {
        const statusUpper = (status || "").toUpperCase();

        if (statusUpper === "PENDING") {
            return (
                <span className="px-3 py-1 text-xs font-medium rounded-full bg-yellow-100 text-yellow-800 border border-yellow-200">
                    En attente
                </span>
            );
        } else if (statusUpper === "ACCEPTED") {
            return (
                <span className="px-3 py-1 text-xs font-medium rounded-full bg-green-100 text-green-800 border border-green-200">
                    Acceptée
                </span>
            );
        } else if (statusUpper === "REJECTED") {
            return (
                <span className="px-3 py-1 text-xs font-medium rounded-full bg-red-100 text-red-800 border border-red-200">
                    Refusée
                </span>
            );
        } else {
            return (
                <span className="px-3 py-1 text-xs font-medium rounded-full bg-gray-100 text-gray-800 border border-gray-200">
                    {statusUpper}
                </span>
            );
        }
    };

    if (loading) {
        return (
            <div className="bg-white shadow rounded-lg p-8 text-center">
                <div className="animate-pulse">Chargement...</div>
            </div>
        );
    }

    if (error) {
        return (
            <div className="bg-white shadow rounded-lg p-8 text-center">
                <p className="text-red-600">{error}</p>
                <button
                    onClick={() => window.location.reload()}
                    className="mt-4 px-4 py-2 bg-red-600 text-white rounded"
                >
                    Réessayer
                </button>
            </div>
        );
    }

    if (!candidatures || candidatures.length === 0) {
        return (
            <div className="bg-white shadow rounded-lg p-8 text-center">
                <div className="text-gray-400 mb-4">
                    <div className="mx-auto h-16 w-16 rounded bg-gray-100 flex items-center justify-center">
                        <span className="text-gray-500 text-3xl">📄</span>
                    </div>
                </div>
                <h3 className="text-lg font-medium text-gray-900 mb-2">Aucune candidature</h3>
                <p className="text-gray-600 mb-4">Vous n'avez pas encore postulé à d'offres de stage.</p>
                <button
                    onClick={() => navigate('/dashboard/student')}
                    className="px-4 py-2 bg-indigo-600 text-white rounded hover:bg-indigo-700"
                >
                    Voir les offres disponibles
                </button>
            </div>
        );
    }

    return (
        <div className="bg-white shadow rounded-lg overflow-hidden">
            <div className="px-6 py-4 border-b border-gray-200">
                <h3 className="text-lg font-medium text-gray-900">Mes candidatures</h3>
                <p className="text-sm text-gray-600">
                    {candidatures.length} candidature{candidatures.length > 1 ? 's' : ''}
                </p>
            </div>

            <div className="overflow-x-auto">
                <table className="min-w-full divide-y divide-gray-200">
                    <thead className="bg-gray-50">
                    <tr>
                        <th className="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">
                            Offre
                        </th>
                        <th className="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">
                            Entreprise
                        </th>
                        <th className="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">
                            Date de candidature
                        </th>
                        <th className="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">
                            Statut
                        </th>
                        <th className="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">
                            Actions
                        </th>
                    </tr>
                    </thead>
                    <tbody className="bg-white divide-y divide-gray-200">
                    {candidatures.map((candidature) => (
                        <tr key={candidature.id} className="hover:bg-gray-50">
                            <td className="px-6 py-4">
                                <div className="text-sm font-medium text-gray-900">
                                    {candidature.offerDescription}
                                </div>
                            </td>
                            <td className="px-6 py-4 text-sm text-gray-900">
                                {candidature.companyName}
                            </td>
                            <td className="px-6 py-4 whitespace-nowrap text-sm text-gray-900">
                                {formatDate(candidature.applicationDate)}
                            </td>
                            <td className="px-6 py-4 whitespace-nowrap">
                                {getStatusBadge(candidature.status)}
                            </td>
                            <td className="px-6 py-4 whitespace-nowrap text-sm font-medium">
                                <button
                                    onClick={() => navigate(`/dashboard/student/offers/${candidature.offerId}`)}
                                    className="text-indigo-600 hover:text-indigo-900"
                                >
                                    Voir l'offre
                                </button>
                            </td>
                        </tr>
                    ))}
                    </tbody>
                </table>
            </div>
        </div>
    );
}