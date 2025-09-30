import React, { useEffect, useState } from "react";
import { useNavigate } from "react-router-dom";
import { useTranslation } from "react-i18next";
import { FaGraduationCap, FaSpinner, FaExclamationTriangle } from "react-icons/fa";

export default function ProgramsListPage() {
    const navigate = useNavigate();
    const { t } = useTranslation();
    const [programs, setPrograms] = useState([]);
    const [loading, setLoading] = useState(true);
    const [error, setError] = useState("");

    useEffect(() => {
        fetchPrograms();
    }, []);

    const fetchPrograms = async () => {
        setLoading(true);
        setError("");
        const token = sessionStorage.getItem("accessToken");

        if (!token) {
            navigate("/login");
            return;
        }

        try {
            const response = await fetch("http://localhost:8080/gestionnaire/programs", {
                headers: {
                    Authorization: `Bearer ${token}`,
                    "Content-Type": "application/json"
                }
            });

            if (response.ok) {
                const data = await response.json();
                setPrograms(data);
            } else if (response.status === 401) {
                navigate("/login");
            } else {
                setError(t("programsList.errorFetch"));
            }
        } catch (err) {
            console.error("Error fetching programs:", err);
            setError(t("programsList.errorServer"));
        } finally {
            setLoading(false);
        }
    };

    if (loading) {
        return (
            <div className="bg-gray-50 flex items-center justify-center py-12">
                <div className="text-center">
                    <FaSpinner className="animate-spin text-4xl text-indigo-600 mx-auto mb-4" />
                    <p className="text-gray-600">{t("programsList.loading")}</p>
                </div>
            </div>
        );
    }

    return (
        <>
            <div className="bg-white rounded-lg shadow-md p-6 mb-6">
                <h2 className="text-2xl font-semibold text-gray-900 mb-2">
                    {t("programsList.title")}
                </h2>
                <p className="text-gray-600">
                    {t("programsList.description")}
                </p>
            </div>

            {error && (
                <div className="mb-6 bg-red-50 border border-red-200 rounded-lg p-4 flex items-start">
                    <FaExclamationTriangle className="text-red-600 mt-1 mr-3 flex-shrink-0" />
                    <div>
                        <p className="text-red-800 font-medium">{t("programsList.error")}</p>
                        <p className="text-red-600 text-sm">{error}</p>
                    </div>
                </div>
            )}

            {programs.length === 0 && !error ? (
                <div className="bg-white rounded-lg shadow-md p-12 text-center">
                    <FaGraduationCap className="text-6xl text-gray-300 mx-auto mb-4" />
                    <h3 className="text-xl font-semibold text-gray-700 mb-2">
                        {t("programsList.noPrograms")}
                    </h3>
                    <p className="text-gray-500">
                        {t("programsList.noProgramsDescription")}
                    </p>
                </div>
            ) : (
                <>
                    <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-3 gap-6">
                        {programs.map((program) => (
                            <div
                                key={program.id}
                                className="bg-white rounded-lg shadow-md hover:shadow-lg transition-shadow p-6 border border-gray-200"
                            >
                                <div className="flex items-start mb-4">
                                    <div className="flex-shrink-0 w-12 h-12 bg-indigo-100 rounded-lg flex items-center justify-center">
                                        <FaGraduationCap className="text-2xl text-indigo-600" />
                                    </div>
                                    <div className="ml-4 flex-1">
                                        <h3 className="text-lg font-semibold text-gray-900 mb-1">
                                            {program.name}
                                        </h3>
                                        {program.description && (
                                            <p className="text-sm text-gray-600 line-clamp-2">
                                                {program.description}
                                            </p>
                                        )}
                                    </div>
                                </div>

                                <div className="border-t border-gray-200 pt-4">
                                    {program.department && (
                                        <div className="flex items-center justify-between text-sm">
                                            <span className="text-gray-500">
                                                {t("programsList.department")}:
                                            </span>
                                            <span className="font-medium text-gray-900">
                                                {program.department}
                                            </span>
                                        </div>
                                    )}
                                </div>
                            </div>
                        ))}
                    </div>

                    <div className="mt-6 text-center">
                        <p className="text-gray-500 text-sm">
                            {t("programsList.totalPrograms")}: <span className="font-semibold text-gray-700">{programs.length}</span>
                        </p>
                    </div>
                </>
            )}
        </>
    );
}