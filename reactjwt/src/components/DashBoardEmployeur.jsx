import React from "react";
import InternshipOffersList from "./InternshipOffersList";
import {useNavigate} from "react-router-dom";
import {FaSignOutAlt} from "react-icons/fa";


export default function DashBoardEmployeur() {
    const navigate = useNavigate()

    const handleLogout = () => {
        sessionStorage.clear();
        localStorage.clear();
        navigate("/login", {replace: true})
    }

    return (
        <div className="min-h-screen bg-gray-50">
            <header className="bg-white shadow">
                <div className="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8">
                    <div className="flex justify-between items-center h-16">
                        <div className="flex items-center">
                            <span className="text-xl font-bold text-indigo-600">LeandrOSE</span>
                        </div>

                        <nav className="flex items-center space-x-4" aria-label="Main navigation">
                            <button
                                onClick={handleLogout}
                                className="flex items-center text-gray-600 hover:text-red-600 transition"
                            >
                                <FaSignOutAlt className="mr-1" />
                                <span className="hidden sm:inline">Logout</span>
                            </button>
                        </nav>
                    </div>
                </div>
            </header>

            <main className="py-10">
                <div className="max-w-5xl mx-auto px-4 sm:px-6 lg:px-8">
                    <h1 className="text-2xl font-semibold text-gray-900 mb-4">Bienvenue Employeur</h1>
                    <p className="text-gray-600 mb-6">
                        Ceci est votre tableau de bord employeur. Vous pouvez gérer vos offres de stage et consulter les candidatures.
                    </p>

                    <InternshipOffersList />
                </div>
            </main>
        </div>
    );
}