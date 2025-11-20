import React, { useEffect, useState } from "react";
import { FaSignOutAlt } from "react-icons/fa";
import { useNavigate } from "react-router-dom";
import { useTranslation } from "react-i18next";
import PendingCvPage from "./PendingCvPage.jsx";
import PendingOffersPage from "./PendingOffersPage.jsx";
import OffersPage from "./OffersPage.jsx";
import EntentesStagePage from "./EntentesStagePage.jsx";
import LanguageSelector from "../LanguageSelector.jsx";
import GestionnaireListeEntentes from "./GestionnaireListeEntentes.jsx";

export default function DashBoardGestionnaire() {
    const navigate = useNavigate();
    const { t } = useTranslation();
    const [userName, setUserName] = useState("");

    const [section, setSection] = useState(() => {
        const params = new URLSearchParams(window.location.search);
        const raw = (params.get('tab') || params.get('section') || 'cv').toLowerCase();
        if (["cv", "offers", "pending","applications-accepted", "ententes"].includes(raw)) return raw;
        return 'cv';
    });

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

    const Btn = ({ target, children }) => (
        <button
            onClick={() => setSection(target)}
            className={`relative flex-1 min-w-[140px] px-6 py-3.5 text-sm font-semibold rounded-lg transition-all duration-200 ease-in-out ${
                section === target
                    ? 'bg-gradient-to-r from-indigo-600 to-indigo-700 text-white shadow-lg shadow-indigo-500/50 scale-105'
                    : 'bg-white text-gray-700 hover:bg-indigo-50 hover:text-indigo-700 hover:shadow-md shadow-sm border border-gray-200'
            }`}
        >
            {children}
            {section === target && (
                <span className="absolute bottom-0 left-1/2 transform -translate-x-1/2 translate-y-1/2 w-2 h-2 bg-indigo-600 rounded-full"></span>
            )}
        </button>
    );

    return (
        <div className="min-h-screen bg-gradient-to-br from-slate-50 via-blue-50 to-indigo-50">
            <header className="bg-white shadow-md border-b border-indigo-100">
                <div className="w-full px-4 sm:px-6 lg:px-8 h-16 flex justify-between items-center">
                    <span className="text-xl font-bold bg-gradient-to-r from-indigo-600 to-indigo-800 bg-clip-text text-transparent">
                        {t("appName")}
                    </span>
                    <div className="flex items-center gap-4">
                        <LanguageSelector />
                        <button
                            onClick={() => navigate("/profil")}
                            className="text-sm text-gray-600 hover:text-indigo-600"
                        >
                            {t("profile.menu") || "Mon profil"}
                        </button>
                        <button
                            onClick={handleLogout}
                            className="flex items-center gap-2 px-4 py-2 text-gray-600 hover:text-red-600 hover:bg-red-50 rounded-lg text-sm font-medium transition-all duration-200"
                        >
                            <FaSignOutAlt /> {t("dashboardGestionnaire.logout")}
                        </button>
                    </div>
                </div>
            </header>

            <main className="py-6">
                <div className="w-full px-4 sm:px-6 lg:px-8 max-w-7xl mx-auto">
                    <div className="mb-6">
                        <h1 className="text-3xl font-bold text-gray-900 mb-1">
                            {t("dashboardGestionnaire.welcome")} {userName} 👋
                        </h1>
                        <p className="text-gray-600 text-sm">{t("dashboardGestionnaire.description")}</p>
                    </div>

                    <div className="bg-white rounded-2xl shadow-lg border border-gray-100 p-2.5 mb-6">
                        <div className="flex flex-wrap gap-2">
                            <Btn target="cv">{t("dashboardGestionnaire.tabs.cv")}</Btn>
                            <Btn target="pending">{t("dashboardGestionnaire.tabs.pendingOffers")}</Btn>
                            <Btn target="offers">{t("dashboardGestionnaire.tabs.offers")}</Btn>
                            <Btn target="applications-accepted">{t("dashboardGestionnaire.tabs.applications-accepted")}</Btn>
                            <Btn target="ententes">{t("dashboardGestionnaire.tabs.ententes")}</Btn>
                        </div>
                    </div>

                    {section === 'cv' && (
                        <div className="transition-opacity duration-300 ease-in-out">
                            <PendingCvPage />
                        </div>
                    )}

                    {section === 'pending' && (
                        <div className="transition-opacity duration-300 ease-in-out">
                            <PendingOffersPage />
                        </div>
                    )}

                    {section === 'offers' && (
                        <div className="transition-opacity duration-300 ease-in-out">
                            <OffersPage />
                        </div>
                    )}

                    {section === 'applications-accepted' && (
                        <div className="transition-opacity duration-300 ease-in-out">
                            <EntentesStagePage />
                        </div>
                    )}
                    {section === 'ententes' && (
                        <div className="transition-opacity duration-300 ease-in-out">
                            <GestionnaireListeEntentes />
                        </div>
                    )}
                </div>
            </main>
        </div>
    );
}