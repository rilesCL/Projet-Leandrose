import React, {useEffect, useState} from "react";
import {useNavigate} from "react-router-dom";
import {useTranslation} from "react-i18next";
import StudentCvList from "./StudentCvList.jsx";
import {FaSignOutAlt} from "react-icons/fa";
import LanguageSelector from "../LanguageSelector.jsx";
import StudentInternshipOffersList from "./StudentInternshipOffersList.jsx";
import StudentApplicationsList from './StudentApplicationsList.jsx';
import StudentEntentesListe from "./StudentEntentesListe.jsx";
import {getStudentMe, updateStudentInfo} from "../../api/apiStudent.jsx";
import {fetchPrograms} from "../../api/apiRegister.jsx";
import InfosContactPage from "./InfosContactPage.jsx";
import ThemeToggle from '../ThemeToggle.jsx';

export default function DashBoardStudent() {
    const navigate = useNavigate();
    const {t} = useTranslation();
    const [userName, setUserName] = useState("");
    const [studentInfo, setStudentInfo] = useState(null);
    const [showReregistrationModal, setShowReregistrationModal] = useState(false);
    const [programs, setPrograms] = useState([]);
    const [selectedProgram, setSelectedProgram] = useState("");
    const [isUpdating, setIsUpdating] = useState(false);

    const getProgramLabel = (program, tFn) => {
        if (!program) return "";
        if (typeof program === "string") return tFn(program);
        if (typeof program === "object") {
            return tFn(program.translationKey || "") || program.code || "";
        }
        return "";
    };

    const translateSchoolTerm = (termString, tFn) => {
        if (!termString || typeof termString !== 'string') return '';
        const parts = termString.trim().split(/\s+/);
        const seasonKey = parts.shift().toUpperCase();
        const rest = parts.join(' ');
        const translationKey = `terms.${seasonKey}`;
        const translated = tFn ? tFn(translationKey) : translationKey;
        const seasonLabel = (translated === translationKey)
            ? (seasonKey.charAt(0).toUpperCase() + seasonKey.slice(1).toLowerCase())
            : translated;
        return rest ? `${seasonLabel} ${rest}` : seasonLabel;
    };

    const [section, setSection] = useState(() => {
        const params = new URLSearchParams(window.location.search);
        const raw = (params.get('tab') || params.get('section') || 'offers').toLowerCase();
        if (["offers", "cv", "applications", "ententes", "contacts"].includes(raw)) return raw;
        return 'offers';
    });

    const handleLogout = () => {
        sessionStorage.clear();
        localStorage.clear();
        navigate("/login", {replace: true});
    };

    useEffect(() => {
        const fetchUserInfo = async () => {
            const token = sessionStorage.getItem('accessToken');
            if (!token) {
                navigate("/login");
                return;
            }
            try {
                const data = await getStudentMe(token);
                setUserName(data.firstName || "");
                setStudentInfo(data);

                if (data.expired) {
                    setShowReregistrationModal(true);
                    setSelectedProgram(
                        typeof data.program === "object" ? data.program.translationKey : data.program || ""
                    );
                    try {
                        const programsList = await fetchPrograms();
                        setPrograms(programsList);
                    } catch (error) {
                        console.error("Error loading programs:", error);
                    }
                }
            } catch (error) {
                navigate("/login");
            }
        };
        fetchUserInfo();
    }, [navigate]);

    const handleReregistration = async () => {
        if (!selectedProgram) {
            alert(t("dashboardStudent.reregistration.selectProgram"));
            return;
        }

        setIsUpdating(true);
        try {
            const updatedStudent = await updateStudentInfo(selectedProgram);
            setStudentInfo(updatedStudent);
            setShowReregistrationModal(false);
            window.location.reload();
        } catch (error) {
            console.error("Error during reregistration:", error);
            alert(error.message || "Erreur lors de la réinscription");
        } finally {
            setIsUpdating(false);
        }
    };

    const handleContinueLater = () => {
        setShowReregistrationModal(false);
    };

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
            {/* Reregistration Modal */}
            {showReregistrationModal && (
                <div className="fixed inset-0 bg-black bg-opacity-50 flex items-center justify-center z-50 p-4">
                    <div className="bg-white rounded-2xl shadow-2xl max-w-md w-full p-8">
                        <div className="text-center mb-6">
                            <div
                                className="mx-auto h-16 w-16 bg-amber-100 rounded-full flex items-center justify-center mb-4">
                                <span className="text-amber-600 text-3xl">⚠️</span>
                            </div>
                            <h2 className="text-2xl font-bold text-gray-900 mb-2">
                                {t("dashboardStudent.reregistration.title") || "Réinscription requise"}
                            </h2>
                            <p className="text-gray-600">
                                {t("dashboardStudent.reregistration.message") ||
                                    "Votre session d'inscription est expirée. Veuillez vous réinscrire pour la nouvelle session."}
                            </p>
                        </div>

                        {studentInfo && (
                            <div className="mb-6 p-4 bg-indigo-50 rounded-lg">
                                <p className="text-sm text-gray-700">
                                    <span className="font-semibold">Session actuelle:</span>{" "}
                                    {translateSchoolTerm(studentInfo.internshipTerm, t)}
                                </p>
                            </div>
                        )}

                        <div className="mb-6">
                            <label className="block text-sm font-medium text-gray-700 mb-2">
                                {t("dashboardStudent.reregistration.program")}
                            </label>
                            <select
                                value={selectedProgram}
                                onChange={(e) => setSelectedProgram(e.target.value)}
                                className="w-full px-4 py-2 border border-gray-300 rounded-lg focus:ring-2 focus:ring-indigo-500 focus:border-indigo-500"
                                disabled={isUpdating}
                            >
                                <option value="">
                                    {t("dashboardStudent.reregistration.selectProgram")}
                                </option>
                                {programs.map((prog) => (
                                    <option key={prog.code} value={prog.translationKey}>
                                        {t(prog.translationKey)}
                                    </option>
                                ))}
                            </select>
                        </div>

                        <div className="space-y-3">
                            <button
                                onClick={handleReregistration}
                                disabled={isUpdating || !selectedProgram}
                                className="w-full bg-gradient-to-r from-indigo-600 to-indigo-700 text-white py-3 rounded-lg font-semibold hover:from-indigo-700 hover:to-indigo-800 transition-all disabled:opacity-50 disabled:cursor-not-allowed"
                            >
                                {isUpdating
                                    ? (t("dashboardStudent.reregistration.updating") || "Mise à jour...")
                                    : (t("dashboardStudent.reregistration.confirm") || "Me réinscrire maintenant")}
                            </button>

                            <button
                                onClick={handleContinueLater}
                                disabled={isUpdating}
                                className="w-full bg-gray-200 text-gray-700 py-3 rounded-lg font-semibold hover:bg-gray-300 transition-all disabled:opacity-50"
                            >
                                {t("dashboardStudent.reregistration.continueLater") || "Continuer plus tard"}
                            </button>
                        </div>
                    </div>
                </div>
            )}

            <header className="bg-white border-b">
                <div className="w-full px-4 sm:px-6 lg:px-8 h-16 flex justify-between items-center">
                    <span className="text-xl font-bold text-indigo-600">{t("appName")}</span>
                    <div className="flex items-center gap-4">
                        <ThemeToggle/>
                        <LanguageSelector/>
                        <button
                            onClick={handleLogout}
                            className="flex items-center text-gray-600 hover:text-red-600 text-sm"
                        >
                            <FaSignOutAlt className="mr-1"/> {t("dashboardStudent.logout")}
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

                    {studentInfo && studentInfo.internshipTerm && (
                        <div className="bg-indigo-50 border border-indigo-200 rounded-lg p-4 mb-6">
                            <p className="text-sm text-indigo-900">
                                <span className="font-semibold">{t("program.description")}</span>{" "}
                                {getProgramLabel(studentInfo.program, t)} |
                                <span className="font-semibold ml-2">{t("terms.term")}:</span>{" "}
                                {translateSchoolTerm(studentInfo.internshipTerm, t)}
                            </p>
                        </div>
                    )}

                    <div className="bg-white rounded-2xl shadow-lg border border-gray-100 p-2.5 mb-6">
                        <div className="flex flex-wrap gap-2">
                            <Btn target="offers">{t("dashboardStudent.tabs.offers")}</Btn>
                            <Btn target="cv">{t("dashboardStudent.tabs.cv")}</Btn>
                            <Btn target="applications">{t("dashboardStudent.tabs.applications")}</Btn>
                            <Btn target="ententes">{t("dashboardStudent.tabs.ententes") || "Ententes"}</Btn>
                            <Btn target="contacts">{t("dashboardStudent.tabs.contacts")}</Btn>
                        </div>
                    </div>

                    {section === 'offers' && (
                        <div className="space-y-8">
                            <StudentInternshipOffersList
                                studentInfo={studentInfo}
                                onReregisterClick={() => setShowReregistrationModal(true)}
                            />
                        </div>
                    )}

                    {section === 'cv' && <StudentCvList/>}
                    {section === 'applications' && <StudentApplicationsList/>}
                    {section === 'ententes' && <StudentEntentesListe/>}
                    {section === 'contacts' && <InfosContactPage/>}
                </div>
            </main>
        </div>
    );
}