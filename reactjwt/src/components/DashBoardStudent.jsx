import React from "react";
import { Link, useNavigate } from "react-router-dom";
import { useTranslation } from "react-i18next";
import StudentCvList from "./StudentCvList";
import { FaSignOutAlt } from "react-icons/fa";

export default function DashBoardStudent() {
    const navigate = useNavigate();
    const { t, i18n } = useTranslation();

    const handleLogout = () => {
        sessionStorage.clear();
        localStorage.clear();
        navigate("/login", { replace: true });
    };

    return (
        <div className="min-h-screen bg-gray-50">
            <header className="bg-white shadow">
                <div className="w-full px-4 sm:px-6 lg:px-8">
                    <div className="flex justify-between items-center h-16">
                        <div className="flex items-center">
                            <span className="text-xl font-bold text-indigo-600">{t("appName")}</span>
                        </div>
                        <nav className="flex items-center space-x-4">
                            <div className="w-32">
                                <select
                                    value={i18n.language}
                                    onChange={(e) => i18n.changeLanguage(e.target.value)}
                                    className="block w-full bg-white border border-gray-300 text-gray-700 py-2 px-3 rounded shadow-sm focus:outline-none focus:ring-2 focus:ring-indigo-500 focus:border-indigo-500 text-sm"
                                >
                                    <option value="en">English</option>
                                    <option value="fr">Français</option>
                                </select>
                            </div>

                            <button
                                onClick={handleLogout}
                                className="flex items-center text-gray-600 hover:text-red-600 transition"
                            >
                                <FaSignOutAlt className="mr-1" />
                                <span className="hidden sm:inline">{t("dashboardStudent.logout")}</span>
                            </button>
                        </nav>
                    </div>
                </div>
            </header>

            <main className="py-10">
                <div className="w-full px-4 sm:px-6 lg:px-8">
                    <h1 className="text-2xl font-semibold text-gray-900 mb-4">{t("dashboardStudent.welcome")}</h1>
                    <p className="text-gray-600 mb-6">
                        {t("dashboardStudent.description")}
                    </p>

                    <StudentCvList />
                </div>
            </main>
        </div>
    );
}