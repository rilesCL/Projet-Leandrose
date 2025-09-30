import React, { useEffect, useState } from "react";
import { approveCv, rejectCv, getPendingCvs, downloadCv } from "../api/apiGestionnaire.jsx";

export default function PendingCvPage() {
    const [pendingCvs, setPendingCvs] = useState([]);
    const [comments, setComments] = useState({});
    const [loading, setLoading] = useState(true);
    const [error, setError] = useState(null);

    useEffect(() => {
        async function fetchCvs() {
            setLoading(true);
            setError(null);
            try {
                const cvs = await getPendingCvs();
                setPendingCvs(Array.isArray(cvs) ? cvs : []);
            } catch (err) {
                console.error(err);
                setError("Erreur lors du chargement des CVs");
            } finally {
                setLoading(false);
            }
        }

        fetchCvs();
    }, []);

    const handleApprove = async (cvId) => {
        try {
            await approveCv(cvId);
            setPendingCvs(pendingCvs.filter(cv => cv.id !== cvId));
        } catch (err) {
            console.error(err);
        }
    };

    const handleReject = async (cvId) => {
        const comment = comments[cvId] || "";
        if (!comment.trim()) {
            setError("Il faut écrire un commentaire avant de rejeter un CV");
            return;
        }
        try {
            await rejectCv(cvId, comment);
            setPendingCvs(pendingCvs.filter(cv => cv.id !== cvId));
        } catch (err) {
            console.error(err);
        }
    };

    const handleCommentChange = (cvId, value) => {
        setComments(prev => ({ ...prev, [cvId]: value }));
    };

    const handleDownload = async (cvId) => {
        try {
            await downloadCv(cvId);
        } catch (err) {
            console.error(err);
            alert("Erreur lors du téléchargement du CV");
        }
    };

    if (loading) return <div className="p-8 text-center">Chargement des CVs en attente...</div>;
    if (error) return <div className="p-8 text-red-600 text-center">{error}</div>;
    if (!pendingCvs.length) return <div className="p-8 text-center">Aucun CV en attente</div>;

    return (
        <div className="bg-white shadow rounded-lg overflow-hidden">
            <div className="px-6 py-4 border-b border-gray-200">
                <h3 className="text-lg font-medium text-gray-900">CVs en attente</h3>
                <p className="text-sm text-gray-600">Gérez les CVs soumis ({pendingCvs.length})</p>
            </div>

            <div className="overflow-x-auto">
                <table className="min-w-full divide-y divide-gray-200">
                    <thead className="bg-gray-50">
                    <tr>
                        <th className="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">Étudiant</th>
                        <th className="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">CV</th>
                        <th className="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">Commentaire</th>
                        <th className="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">Actions</th>
                    </tr>
                    </thead>
                    <tbody className="bg-white divide-y divide-gray-200">
                    {pendingCvs.map(cv => (
                        <tr key={cv.id} className="hover:bg-gray-50">
                            <td className="px-6 py-4 text-sm text-gray-900 min-w-[150px]">
                                {cv.studentName || "Étudiant inconnu"}
                            </td>
                            <td className="px-6 py-4 min-w-[120px]">
                                <button
                                    onClick={() => handleDownload(cv.id)}
                                    className="text-blue-600 hover:underline"
                                >
                                    Télécharger CV
                                </button>
                            </td>
                            <td className="px-6 py-4">
                                    <textarea
                                        value={comments[cv.id] || ""}
                                        onChange={e => handleCommentChange(cv.id, e.target.value)}
                                        className="w-full h-20 border rounded p-2 resize-none"
                                        placeholder="Commentaire (requis pour rejet)"
                                    />
                            </td>
                            <td className="px-6 py-4 flex flex-col md:flex-row items-start md:items-center space-y-2 md:space-y-0 md:space-x-2">
                                <button onClick={() => handleApprove(cv.id)} className="bg-green-500 text-white px-3 py-1 rounded w-full md:w-auto">Approuver</button>
                                <button onClick={() => handleReject(cv.id)} className="bg-red-500 text-white px-3 py-1 rounded w-full md:w-auto">Rejeter</button>
                            </td>
                        </tr>
                    ))}
                    </tbody>
                </table>
            </div>
        </div>
    );
}
