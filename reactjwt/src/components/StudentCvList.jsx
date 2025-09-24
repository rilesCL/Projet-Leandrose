import React, { useEffect, useState } from "react";

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
                const response = await fetch('http://localhost:8080/student/cv', {
                    method: 'GET',
                    headers: {
                        'Authorization': `Bearer ${token}`,
                        'Content-Type': 'application/json'
                    }
                });

                if (response.status === 404) {
                    // No CV uploaded - this is normal
                    setCv(null);
                } else if (response.status === 401) {
                    setError("Vous devez être connecté pour voir votre CV.");
                } else if (response.status === 403) {
                    setError("Vous n'avez pas les permissions nécessaires.");
                } else if (!response.ok) {
                    const errorText = await response.text();
                    setError(errorText || `Erreur ${response.status}: ${response.statusText}`);
                } else {
                    const data = await response.json();
                    setCv(data || null);
                }
            } catch (err) {
                setError("Impossible de se connecter au serveur. Vérifiez votre connexion internet.");
            } finally {
                setLoading(false);
            }
        }

        fetchCv();
    }, []);

    if (loading) {
        return (
            <div className="bg-white shadow rounded-lg p-8 text-center">
                <div className="animate-pulse">
                    <div className="h-4 bg-gray-200 rounded w-1/4 mx-auto mb-4"></div>
                    <div className="h-4 bg-gray-200 rounded w-1/2 mx-auto"></div>
                </div>
                <p className="text-gray-600 mt-4">Chargement de vos CVs...</p>
            </div>
        );
    }

    if (error) {
        return (
            <div className="bg-white shadow rounded-lg p-8 text-center">
                <div className="text-red-600 mb-4">
                    <svg className="mx-auto h-12 w-12" fill="none" viewBox="0 0 24 24" stroke="currentColor">
                        <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M12 8v4m0 4h.01M21 12a9 9 0 11-18 0 9 9 0 0118 0z" />
                    </svg>
                </div>
                <h3 className="text-lg font-medium text-gray-900 mb-2">Erreur de chargement</h3>
                <p className="text-red-600">{error}</p>
                <button
                    onClick={() => window.location.reload()}
                    className="mt-4 px-4 py-2 bg-red-600 text-white rounded hover:bg-red-700"
                >
                    Réessayer
                </button>
            </div>
        );
    }

    if (!cv) {
        return (
            <div className="bg-white shadow rounded-lg p-8 text-center">
                <div className="text-gray-400 mb-4">
                    <svg className="mx-auto h-16 w-16" fill="none" viewBox="0 0 24 24" stroke="currentColor">
                        <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={1} d="M9 12h6m-6 4h6m2 5H7a2 2 0 01-2-2V5a2 2 0 012-2h5.586a1 1 0 01.707.293l5.414 5.414a1 1 0 01.293.707V19a2 2 0 01-2 2z" />
                    </svg>
                </div>
                <h3 className="text-lg font-medium text-gray-900 mb-2">Aucun CV téléversé</h3>
                <p className="text-gray-600 mb-4">
                    Vous n'avez pas encore téléversé de CV. Téléversez votre CV pour postuler aux offres de stage.
                </p>
                <a
                    href="/dashboard/student/uploadCv"
                    className="inline-flex items-center px-4 py-2 bg-indigo-600 text-white rounded-md hover:bg-indigo-700 transition-colors"
                >
                    <svg className="mr-2 h-4 w-4" fill="none" viewBox="0 0 24 24" stroke="currentColor">
                        <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M12 4v16m8-8H4" />
                    </svg>
                    Téléverser mon CV
                </a>
            </div>
        );
    }

    const status = (cv.status || "").toString().toUpperCase();
    const statusLabel = status === "PENDING" || status === "PENDING_VALIDATION" ? (
        <span className="px-3 py-1 text-xs font-medium rounded-full bg-yellow-100 text-yellow-800 border border-yellow-200">
            <span className="w-2 h-2 bg-yellow-500 rounded-full inline-block mr-2"></span>
            En attente
        </span>
    ) : status === "APPROVED" || status === "APPROUVED" ? (
        <span className="px-3 py-1 text-xs font-medium rounded-full bg-green-100 text-green-800 border border-green-200">
            <span className="w-2 h-2 bg-green-500 rounded-full inline-block mr-2"></span>
            Approuvé
        </span>
    ) : status === "REJECTED" ? (
        <span className="px-3 py-1 text-xs font-medium rounded-full bg-red-100 text-red-800 border border-red-200">
            <span className="w-2 h-2 bg-red-500 rounded-full inline-block mr-2"></span>
            Rejeté
        </span>
    ) : (
        <span className="px-3 py-1 text-xs font-medium rounded-full bg-gray-100 text-gray-800 border border-gray-200">
            <span className="w-2 h-2 bg-gray-500 rounded-full inline-block mr-2"></span>
            {status || 'Inconnu'}
        </span>
    );

    // Extract filename from path
    const getFileName = (path) => {
        if (!path) return 'CV.pdf';
        const fileName = path.split('/').pop() || path.split('\\').pop();
        // If it's a generated filename with UUID, show a user-friendly name
        if (fileName.includes('_') && fileName.includes('-')) {
            return 'Mon_CV.pdf';
        }
        return fileName;
    };

    const handleDownload = async () => {
        try {
            const token = localStorage.getItem("accessToken");
            const response = await fetch('http://localhost:8080/student/cv/download', {
                method: 'GET',
                headers: {
                    'Authorization': `Bearer ${token}`
                }
            });

            if (response.ok) {
                const blob = await response.blob();
                const url = window.URL.createObjectURL(blob);
                const a = document.createElement('a');
                a.href = url;
                a.download = getFileName(cv.pdfPath);
                document.body.appendChild(a);
                a.click();
                window.URL.revokeObjectURL(url);
                document.body.removeChild(a);
            } else {
                alert('Erreur lors du téléchargement du CV');
            }
        } catch (error) {
            console.error('Download error:', error);
            alert('Erreur lors du téléchargement du CV');
        }
    };

    const handlePreview = async () => {
        try {
            const token = localStorage.getItem("accessToken");
            const response = await fetch('http://localhost:8080/student/cv/download', {
                method: 'GET',
                headers: {
                    'Authorization': `Bearer ${token}`
                }
            });

            if (response.ok) {
                const blob = await response.blob();
                const url = window.URL.createObjectURL(blob);
                window.open(url, '_blank');
                // Don't revoke URL immediately to allow the browser to load it
                setTimeout(() => window.URL.revokeObjectURL(url), 10000);
            } else {
                alert('Erreur lors de l\'ouverture du CV');
            }
        } catch (error) {
            console.error('Preview error:', error);
            alert('Erreur lors de l\'ouverture du CV');
        }
    };

    return (
        <div className="bg-white shadow rounded-lg overflow-hidden">
            <div className="px-6 py-4 border-b border-gray-200">
                <h3 className="text-lg font-medium text-gray-900">Mon CV</h3>
                <p className="text-sm text-gray-600">Gérez votre CV téléversé</p>
            </div>

            <div className="overflow-x-auto">
                <table className="min-w-full divide-y divide-gray-200">
                    <thead className="bg-gray-50">
                    <tr>
                        <th className="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">
                            Fichier
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
                    <tr className="hover:bg-gray-50">
                        <td className="px-6 py-4 whitespace-nowrap">
                            <div className="flex items-center">
                                <div className="flex-shrink-0">
                                    <svg className="h-8 w-8 text-red-500" fill="currentColor" viewBox="0 0 20 20">
                                        <path fillRule="evenodd" d="M4 4a2 2 0 012-2h4.586A2 2 0 0112 2.586L15.414 6A2 2 0 0116 7.414V16a2 2 0 01-2 2H6a2 2 0 01-2-2V4zm2 6a1 1 0 011-1h6a1 1 0 110 2H7a1 1 0 01-1-1zm1 3a1 1 0 100 2h6a1 1 0 100-2H7z" clipRule="evenodd" />
                                    </svg>
                                </div>
                                <div className="ml-3">
                                    <div className="text-sm font-medium text-gray-900">
                                        {getFileName(cv.pdfPath)}
                                    </div>
                                    <div className="text-sm text-gray-500">PDF</div>
                                </div>
                            </div>
                        </td>
                        <td className="px-6 py-4 whitespace-nowrap">
                            {statusLabel}
                        </td>
                        <td className="px-6 py-4 whitespace-nowrap text-sm font-medium space-x-2">
                            <button
                                onClick={handlePreview}
                                className="inline-flex items-center px-3 py-1 border border-transparent text-sm leading-4 font-medium rounded-md text-indigo-700 bg-indigo-100 hover:bg-indigo-200 focus:outline-none focus:ring-2 focus:ring-offset-2 focus:ring-indigo-500"
                            >
                                <svg className="mr-1 h-4 w-4" fill="none" viewBox="0 0 24 24" stroke="currentColor">
                                    <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M15 12a3 3 0 11-6 0 3 3 0 016 0z" />
                                    <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M2.458 12C3.732 7.943 7.523 5 12 5c4.478 0 8.268 2.943 9.542 7-1.274 4.057-5.064 7-9.542 7-4.477 0-8.268-2.943-9.542-7z" />
                                </svg>
                                Aperçu
                            </button>
                            <button
                                onClick={handleDownload}
                                className="inline-flex items-center px-3 py-1 border border-transparent text-sm leading-4 font-medium rounded-md text-green-700 bg-green-100 hover:bg-green-200 focus:outline-none focus:ring-2 focus:ring-offset-2 focus:ring-green-500"
                            >
                                <svg className="mr-1 h-4 w-4" fill="none" viewBox="0 0 24 24" stroke="currentColor">
                                    <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M12 10v6m0 0l-3-3m3 3l3-3m2 8H7a2 2 0 01-2-2V5a2 2 0 012-2h5.586a1 1 0 01.707.293l5.414 5.414a1 1 0 01.293.707V19a2 2 0 01-2 2z" />
                                </svg>
                                Télécharger
                            </button>
                        </td>
                    </tr>
                    </tbody>
                </table>
            </div>

            <div className="px-6 py-3 bg-gray-50 text-right">
                <a
                    href="/dashboard/student/uploadCv"
                    className="inline-flex items-center px-4 py-2 text-sm font-medium text-indigo-600 hover:text-indigo-500"
                >
                    Téléverser un nouveau CV
                    <svg className="ml-1 h-4 w-4" fill="none" viewBox="0 0 24 24" stroke="currentColor">
                        <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M4 16v1a3 3 0 003 3h10a3 3 0 003-3v-1m-4-8l-4-4m0 0L8 8m4-4v12" />
                    </svg>
                </a>
            </div>
        </div>
    );
}