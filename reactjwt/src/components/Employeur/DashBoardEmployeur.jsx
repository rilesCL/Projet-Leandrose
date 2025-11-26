import React, {useEffect, useState} from "react";
import InternshipOffersList from "./InternshipOffersList.jsx";
import {useLocation, useNavigate} from "react-router-dom";
import {FaSignOutAlt} from "react-icons/fa";
import {useTranslation} from "react-i18next";
import LanguageSelector from "../LanguageSelector.jsx";
import EmployeurListeStages from "./EmployeurListeStages.jsx";
import EvaluationList from "./EvaluationList.jsx";
import ThemeToggle from "../ThemeToggle.jsx";
import TermSelector from "../TermSelector.jsx";
import {getEmployeurMe} from "../../api/apiEmployeur.jsx";

export default function DashBoardEmployeur() {
    const navigate = useNavigate();
    const location = useLocation();
    const {t, i18n} = useTranslation();
    const [userName, setUserName] = useState("");
    const [selectedTerm, setSelectedTerm] = useState(null);

    const [section, setSection] = useState(() => {
        const params = new URLSearchParams(location.search);
        const raw = (params.get('tab') || params.get('section') || 'offers').toLowerCase();
        if (["offers", "ententes", "evaluations"].includes(raw)) return raw;
        return 'offers';
    });

    const handleLogout = () => {
        sessionStorage.clear();
        localStorage.clear();
        navigate("/login", {replace: true});
    };

    const handleTermChange = (term) => {
        setSelectedTerm(term);
    };

    useEffect(() => {
        const fetchUserInfo = async () => {
            const token = sessionStorage.getItem('accessToken');
            if (!token) {
                navigate("/login");
                return;
            }
            try {
                const data = await getEmployeurMe(token);
                setUserName(data.firstName || "");
            } catch (error) {
                console.error("User fetch error:", error);
                navigate("/login");
            }
        };
        fetchUserInfo();
    }, [navigate]);

    const Btn = ({target, children}) => (
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
                <span
                    className="absolute bottom-0 left-1/2 transform -translate-x-1/2 translate-y-1/2 w-2 h-2 bg-indigo-600 rounded-full"></span>
            )}
        </button>
    );

    return (
        <div className="min-h-screen bg-gradient-to-br from-slate-50 via-blue-50 to-indigo-50">
            <header className="bg-white shadow">
                <div className="w-full px-4 sm:px-6 lg:px-8">
                    <div className="flex justify-between items-center h-16">
                        <div className="flex items-center">
                            <span className="text-xl font-bold text-indigo-600">
                                {t("appName")}
                            </span>
                        </div>

                        <nav
                            className="flex items-center space-x-4"
                            aria-label={t("dashboardEmployeur.navigation.mainNavigation")}
                        >
                            <TermSelector onTermChange={handleTermChange}/>
                            <ThemeToggle/>
                            <LanguageSelector/>
                            <button
                                onClick={() => navigate("/profil")}
                                className="text-sm text-gray-600 hover:text-indigo-600"
                            >
                                {t("profile.menu") || "Mon profil"}
                            </button>

                            <button
                                onClick={handleLogout}
                                className="flex items-center text-gray-600 hover:text-red-600 transition"
                            >
                                <FaSignOutAlt className="mr-1"/>
                                <span className="hidden sm:inline">
                                    {t("dashboardEmployeur.logout")}
                                </span>
                            </button>
                        </nav>
                    </div>
                </div>
            </header>

            <main className="py-10">
                <div className="w-full px-4 sm:px-6 lg:px-8">
                    <h1 className="text-2xl font-semibold text-gray-900 mb-4">
                        {t("dashboardEmployeur.welcome")} {userName}!
                    </h1>
                    <p className="text-gray-600 mb-6">
                        {t("dashboardEmployeur.description")}
                    </p>

                    <div className="bg-white rounded-2xl shadow-lg border border-gray-100 p-2.5 mb-6">
                        <div className="flex flex-wrap gap-2">
                            <Btn target="offers">
                                {t("dashboardEmployeur.tabs.offers") || "Offres de stage"}
                            </Btn>
                            <Btn target="ententes">
                                {t("dashboardEmployeur.tabs.ententes") || "Ententes"}
                            </Btn>
                            <Btn target="evaluations">
                                {t("dashboardEmployeur.tabs.evaluations") || "Évaluations"}
                            </Btn>
                        </div>
                    </div>

                    {section === 'offers' && (
                        <div className="space-y-8">
                            <InternshipOffersList selectedTerm={selectedTerm}/>
                        </div>
                    )}

                    {section === 'ententes' && (
                        <div className="space-y-8">
                            <EmployeurListeStages selectedTerm={selectedTerm}/>
                        </div>
                    )}

                    {section === 'evaluations' && (
                        <div className="space-y-8">
                            <EvaluationList selectedTerm={selectedTerm}/>
                        </div>
                    )}
                </div>
            </main>
        </div>
    );
}