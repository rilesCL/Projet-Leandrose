import React, { useEffect, useState } from "react";
import { useNavigate } from "react-router-dom";
import { useTranslation } from "react-i18next";
import StudentCvList from "./StudentCvList";
import { FaSignOutAlt } from "react-icons/fa";
import LanguageSelector from "./LanguageSelector.jsx";
import StudentInternshipOffersList from "./StudentInternshipOffersList.jsx";
import StudentApplicationsList from './StudentApplicationsList.jsx';

export default function DashBoardStudent() {
    const navigate = useNavigate();
    const { t } = useTranslation();
    const [userName, setUserName] = useState("");

    const [section, setSection] = useState(() => {
        const params = new URLSearchParams(window.location.search);
        const raw = (params.get('tab') || params.get('section') || 'offers').toLowerCase();
        if (["offers","cv","applications"].includes(raw)) return raw;
        return 'offers';
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
            <header className="bg-white border-b">
                <div className="w-full px-4 sm:px-6 lg:px-8 h-16 flex justify-between items-center">
                    <span className="text-xl font-bold text-indigo-600">{t("appName")}</span>
                    <div className="flex items-center gap-4">
                        <LanguageSelector />
                        <button
                            onClick={handleLogout}
                            className="flex items-center text-gray-600 hover:text-red-600 text-sm"
                        >
                            <FaSignOutAlt className="mr-1" /> {t("dashboardStudent.logout")}
                        </button>
                    </div>
                </div>
            </header>
            <main className="py-8">
                <div className="w-full px-4 sm:px-6 lg:px-8">
                    <h1 className="text-2xl font-semibold text-gray-900 mb-2">
                        {t("dashboardStudent.welcome")} {userName}!
                    </h1>
                    <p className="text-gray-600 mb-6">{t("dashboardStudent.description")}</p>

                    <div className="bg-white rounded-2xl shadow-lg border border-gray-100 p-2.5 mb-6">
                        <div className="flex flex-wrap gap-2">
                            <Btn target="offers">{t("dashboardStudent.tabs.offers")}</Btn>
                            <Btn target="cv">{t("dashboardStudent.tabs.cv")}</Btn>
                            <Btn target="applications">{t("dashboardStudent.tabs.applications")}</Btn>
                        </div>
                    </div>

                    {section === 'offers' && (
                        <div className="space-y-8">
                            <StudentInternshipOffersList />
                        </div>
                    )}

                    {section === 'cv' && (
                        <div>
                            <StudentCvList />
                        </div>
                    )}

                    {section === 'applications' && (
                        <div>
                            <StudentApplicationsList />
                        </div>
                    )}
                </div>
            </main>
        </div>
    );
}