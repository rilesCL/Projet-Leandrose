import React, { useEffect, useState } from "react";
import { getStudentCvs } from "../api/apiStudent";

export default function StudentCvList() {
    const [cvs, setCvs] = useState([]);
    const [loading, setLoading] = useState(true);
    const [error, setError] = useState(null);

    useEffect(() => {
        async function fetchCvs() {
            setLoading(true);
            setError(null);
            try {
                const token = localStorage.getItem("accessToken");
                const data = await getStudentCvs(token);
                setCvs(data || []);
            } catch (err) {
                setError(err.response?.data || "Impossible de charger vos CV.");
            } finally {
                setLoading(false);
            }
        }
        fetchCvs();
    }, []);

    if (loading) return <p className="text-gray-600">Chargement...</p>;
    if (error) return <p className="text-red-600">{error}</p>;
    if (cvs.length === 0) return <p className="text-gray-600">Vous n’avez pas encore téléversé de CV.</p>;

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
                {cvs.map((cv, idx) => (
                    <tr key={idx}>
                        <td className="px-6 py-4 text-sm text-gray-800 truncate max-w-xs">{cv.pdfPath}</td>
                        <td className="px-6 py-4 text-sm">
                            {cv.status === "PENDING_VALIDATION" && <span className="px-2 py-1 text-xs rounded bg-yellow-100 text-yellow-800">En attente</span>}
                            {cv.status === "APPROUVED" && <span className="px-2 py-1 text-xs rounded bg-green-100 text-green-800">Approuvé</span>}
                            {cv.status === "REJECTED" && <span className="px-2 py-1 text-xs rounded bg-red-100 text-red-800">Rejeté</span>}
                        </td>
                        <td className="px-6 py-4 text-sm">
                            <a href={cv.pdfPath} target="_blank" rel="noopener noreferrer" className="text-indigo-600 hover:underline">Voir / Télécharger</a>
                        </td>
                    </tr>
                ))}
                </tbody>
            </table>
        </div>
    );
}
