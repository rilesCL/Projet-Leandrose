import React, { useEffect, useState } from "react";
import { useNavigate } from "react-router-dom";
import { useTranslation } from "react-i18next";
import StudentCvList from "./StudentCvList";
import { FaSignOutAlt } from "react-icons/fa";
import LanguageSelector from "./LanguageSelector.jsx";
import StudentInternshipOffersList from "./StudentInternshipOffersList.jsx";

export default function DashBoardStudent() {
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
                        <div className="flex items-center">
                            <span className="text-xl font-bold text-indigo-600">{t("appName")}</span>
                        </div>
                        <nav className="flex items-center space-x-4">
                            <LanguageSelector />

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
                    <h1 className="text-2xl font-semibold text-gray-900 mb-4">
                        {t("dashboardStudent.welcome")} {userName}!
                    </h1>
                    <p className="text-gray-600 mb-6">
                        {t("dashboardStudent.description")}
                    </p>

                    <StudentCvList />
                    <div className="mt-8">
                        <StudentInternshipOffersList />
                    </div>
                </div>
            </main>
        </div>
    );
}
