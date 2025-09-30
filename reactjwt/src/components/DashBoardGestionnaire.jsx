import React from "react";
import { FaSignOutAlt } from "react-icons/fa";
import { useNavigate } from "react-router-dom";
import PendingCvPage from "./PendingCvPage";
import PendingOffersPage from "./PendingOffersPage.jsx"; // unchanged

export default function DashBoardGestionnaire() {
    const navigate = useNavigate();

    const handleLogout = () => {
        sessionStorage.clear();
        localStorage.clear();
        navigate("/login", { replace: true });
    };

    return (
        <div className="min-h-screen bg-gray-50">
            <header className="bg-white shadow">
                <div className="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8 w-full">
                    <div className="flex justify-between items-center h-16">
                        <span className="text-xl font-bold text-indigo-600">LeandrOSE</span>
                        <button
                            onClick={handleLogout}
                            className="flex items-center text-gray-600 hover:text-red-600 transition"
                        >
                            <FaSignOutAlt className="mr-1" />
                            <span className="hidden sm:inline">Logout</span>
                        </button>
                    </div>
                </div>
            </header>

            <main className="py-10">
                <div className="max-w-6xl mx-auto px-4 sm:px-6 lg:px-8">
                    <h1 className="text-2xl font-semibold text-gray-900 mb-4">
                        Bienvenue Gestionnaire
                    </h1>

                    {/* Show pending CVs immediately */}
                    <PendingCvPage />
                    <div className="my-8 border-t border-gray-300"></div>
                    <PendingOffersPage />
                </div>
            </main>
        </div>
    );
}
