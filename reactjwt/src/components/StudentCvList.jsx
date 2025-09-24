import React, { useEffect, useState } from "react";
import { getStudentCv } from "../api/apiStudent";

export default function StudentCvList() {
    const [cv, setCv] = useState(null);
    const [loading, setLoading] = useState(true);
    const [error, setError] = useState(null);

    useEffect(() => {
        async function fetchCv() {
            setLoading(true);
            setError(null);
            try {
                const token = localStorage.getItem("accessToken");
                const data = await getStudentCv(token);
                setCv(data || null);
            } catch (err) {
                setError(err.response?.data || "Impossible de charger votre CV.");
            } finally {
                setLoading(false);
            }
        }
        fetchCv();
    }, []);

    if (loading) return <p className="text-gray-600">Chargement...</p>;
    if (error) return <p className="text-red-600">{error}</p>;
    if (!cv) return <p className="text-gray-600">Vous n’avez pas encore téléversé de CV.</p>;

    const status = (cv.status || "").toString().toUpperCase();
    const statusLabel = status === "PENDING" || status === "PENDING_VALIDATION" ? (
        <span className="px-2 py-1 text-xs rounded bg-yellow-100 text-yellow-800">En attente</span>
    ) : status === "APPROVED" || status === "APPROUVED" ? (
        <span className="px-2 py-1 text-xs rounded bg-green-100 text-green-800">Approuvé</span>
    ) : status === "REJECTED" ? (
        <span className="px-2 py-1 text-xs rounded bg-red-100 text-red-800">Rejeté</span>
    ) : (
        <span className="px-2 py-1 text-xs rounded bg-gray-100 text-gray-800">{status}</span>
    );

    return (
        <div className="overflow-x-auto bg-white shadow rounded-lg">
            <table className="min-w-full divide-y divide-gray-200">
                <thead className="bg-gray-50">
                <tr>
                    <th className="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase">Nom du fichier</th>
                    <th className="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase">Statut</th>
                    <th className="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase">Action</th>
                </tr>
                </thead>
                <tbody className="divide-y divide-gray-200">
                <tr>
                    <td className="px-6 py-4 text-sm text-gray-800 truncate max-w-xs">{cv.pdfPath}</td>
                    <td className="px-6 py-4 text-sm">{statusLabel}</td>
                    <td className="px-6 py-4 text-sm">
                        <a href={cv.pdfPath} target="_blank" rel="noopener noreferrer" className="text-indigo-600 hover:underline">Voir / Télécharger</a>
                    </td>
                </tr>
                </tbody>
            </table>
        </div>
    );
}