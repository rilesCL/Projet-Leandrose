import React, { useEffect, useState } from "react";
import { FaSignOutAlt } from "react-icons/fa";
import { useNavigate, Link, useLocation, Outlet } from "react-router-dom";
import { useTranslation } from "react-i18next";
import LanguageSelector from "./LanguageSelector.jsx";

export default function DashBoardGestionnaire() {
    const navigate = useNavigate();
    const location = useLocation();
    const { t } = useTranslation();
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

    // helper pour savoir quel onglet est actif (startsWith pour sous-routes)
    const isActive = (path) => location.pathname.startsWith(path);

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
                    {/* Nouveau menu d'onglets placé juste au-dessus du Welcome */}
                    <div className="mb-4">
                        <nav className="flex space-x-4 border-b">
                            <Link
                                to="/dashboard/gestionnaire/offers"
                                className={
                                    "px-3 py-2 -mb-px text-sm font-medium " +
                                    (isActive("/dashboard/gestionnaire/offers")
                                        ? "text-indigo-600 border-b-2 border-indigo-600"
                                        : "text-gray-600 hover:text-indigo-600")
                                }
                            >
                                {t("dashboardGestionnaire.offer")}
                            </Link>
                            <Link
                                to="/dashboard/gestionnaire/ententes"
                                className={
                                    "px-3 py-2 -mb-px text-sm font-medium " +
                                    (isActive("/dashboard/gestionnaire/ententes")
                                        ? "text-indigo-600 border-b-2 border-indigo-600"
                                        : "text-gray-600 hover:text-indigo-600")
                                }
                            >
                                {t("dashboardGestionnaire.ententes")}
                            </Link>
                        </nav>
                    </div>

                    <h1 className="text-2xl font-semibold text-gray-900 mb-4">
                        {t("dashboardGestionnaire.welcome")} {userName}!
                    </h1>

                    {/* Outlet pour rendre les routes enfants (index, offers, ententes, cv, etc.) */}
                    <div className="space-y-6">
                        <Outlet />
                    </div>
                </div>
            </main>
        </div>
    );
}