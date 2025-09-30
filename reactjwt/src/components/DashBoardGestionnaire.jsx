import React, { useEffect, useState } from "react";
import { FaSignOutAlt } from "react-icons/fa";
import { useNavigate } from "react-router-dom";
import { useTranslation } from "react-i18next";
import PendingCvPage from "./PendingCvPage";
import PendingOffersPage from "./PendingOffersPage.jsx";
import LanguageSelector from "./LanguageSelector.jsx";

export default function DashBoardGestionnaire() {
    const navigate = useNavigate();
    const { t, i18n } = useTranslation();
    const [userName, setUserName] = useState("");

    const handleLogout = () => {
        sessionStorage.clear();
        localStorage.clear();
        navigate("/login", { replace: true });
    };

    useEffect(() => {
        const fetchUserInfo = async () => {
            const token = sessionStorage.getItem('accessToken');
            if (!token) {
                navigate("/login");
                return;
            }
            try {
                const res = await fetch('http://localhost:8080/user/me', {
                    headers: { Authorization: `Bearer ${token}` }
                });
                if (res.ok) {
                    const data = await res.json();
                    setUserName(data.firstName || "");
                } else {
                    navigate("/login");
                }
            } catch (error) {
                console.error("User fetch error:", error);
                navigate("/login");
            }
        };
        fetchUserInfo();
    }, [navigate]);

    return (
        <div className="min-h-screen bg-gray-50">
            <header className="bg-white shadow">
                <div className="w-full px-4 sm:px-6 lg:px-8">
                    <div className="flex justify-between items-center h-16">
                        <span className="text-xl font-bold text-indigo-600">{t("appName")}</span>

                        <nav className="flex items-center space-x-4">
                            <LanguageSelector />

                            <button
                                onClick={handleLogout}
                                className="flex items-center text-gray-600 hover:text-red-600 transition"
                            >
                                <FaSignOutAlt className="mr-1" />
                                <span className="hidden sm:inline">{t("dashboardGestionnaire.logout")}</span>
                            </button>
                        </nav>
                    </div>
                </div>
            </header>

            <main className="py-10">
                <div className="w-full px-4 sm:px-6 lg:px-8">
                    <h1 className="text-2xl font-semibold text-gray-900 mb-4">
                        {t("dashboardGestionnaire.welcome")} {userName}!
                    </h1>

                    <PendingCvPage />
                    <div className="my-8 border-t border-gray-300"></div>
                    <PendingOffersPage />
                </div>
            </main>
        </div>
    );
}
