import React from "react";
import {useNavigate} from "react-router-dom";
import {useTranslation} from "react-i18next";
import LanguageSelector from "./LanguageSelector.jsx";


export default function RegisterLanding() {
    const navigate = useNavigate();
    const {t, i18n} = useTranslation();

    return (
        <div className="min-h-screen bg-gray-50 flex items-center justify-center p-4">
            <div className="max-w-4xl w-full">
                <header className="text-center mb-8">
                    <h1 className="text-4xl font-bold text-indigo-600 mb-2">{t("appName")}</h1>
                    <p className="text-gray-600 text-lg">{t("registerLanding.subtitle")}</p>
                </header>

                <div className="flex justify-end mb-6">
                    <LanguageSelector/>
                </div>

                <div className="grid grid-cols-1 md:grid-cols-2 gap-6">
                    <div
                        className="bg-white rounded-lg shadow-lg p-8 hover:shadow-xl transition-shadow border-2 border-transparent hover:border-indigo-500">
                        <div className="text-center mb-6">
                            <div
                                className="mx-auto w-20 h-20 bg-indigo-100 rounded-full flex items-center justify-center mb-4">
                                <span className="text-4xl">🎓</span>
                            </div>
                            <h2 className="text-2xl font-bold text-gray-800 mb-2">
                                {t("registerLanding.student.title")}
                            </h2>
                            <p className="text-gray-600">
                                {t("registerLanding.student.description")}
                            </p>
                        </div>

                        <ul className="space-y-3 mb-6">
                            <li className="flex items-start">
                                <span className="text-green-500 mr-2">✓</span>
                                <span className="text-gray-700">{t("registerLanding.student.feature1")}</span>
                            </li>
                            <li className="flex items-start">
                                <span className="text-green-500 mr-2">✓</span>
                                <span className="text-gray-700">{t("registerLanding.student.feature2")}</span>
                            </li>
                            <li className="flex items-start">
                                <span className="text-green-500 mr-2">✓</span>
                                <span className="text-gray-700">{t("registerLanding.student.feature3")}</span>
                            </li>
                        </ul>

                        <button
                            onClick={() => navigate("/register/etudiant")}
                            className="w-full px-6 py-3 bg-indigo-600 text-white font-medium rounded-md hover:bg-indigo-700 transition-colors"
                        >
                            {t("registerLanding.student.button")}
                        </button>
                    </div>

                    <div
                        className="bg-white rounded-lg shadow-lg p-8 hover:shadow-xl transition-shadow border-2 border-transparent hover:border-indigo-500">
                        <div className="text-center mb-6">
                            <div
                                className="mx-auto w-20 h-20 bg-indigo-100 rounded-full flex items-center justify-center mb-4">
                                <span className="text-4xl">💼</span>
                            </div>
                            <h2 className="text-2xl font-bold text-gray-800 mb-2">
                                {t("registerLanding.employer.title")}
                            </h2>
                            <p className="text-gray-600">
                                {t("registerLanding.employer.description")}
                            </p>
                        </div>

                        <ul className="space-y-3 mb-6">
                            <li className="flex items-start">
                                <span className="text-green-500 mr-2">✓</span>
                                <span className="text-gray-700">{t("registerLanding.employer.feature1")}</span>
                            </li>
                            <li className="flex items-start">
                                <span className="text-green-500 mr-2">✓</span>
                                <span className="text-gray-700">{t("registerLanding.employer.feature2")}</span>
                            </li>
                            <li className="flex items-start">
                                <span className="text-green-500 mr-2">✓</span>
                                <span className="text-gray-700">{t("registerLanding.employer.feature3")}</span>
                            </li>
                        </ul>

                        <button
                            onClick={() => navigate("/register/employeur")}
                            className="w-full px-6 py-3 bg-indigo-600 text-white font-medium rounded-md hover:bg-indigo-700 transition-colors"
                        >
                            {t("registerLanding.employer.button")}
                        </button>
                    </div>
                </div>

                <div className="text-center mt-8">
                    <p className="text-gray-600">
                        {t("registerLanding.alreadyAccount")}{" "}
                        <button
                            onClick={() => navigate("/login")}
                            className="text-indigo-600 hover:underline font-medium"
                        >
                            {t("registerLanding.login")}
                        </button>
                    </p>
                </div>
            </div>
        </div>
    );
}