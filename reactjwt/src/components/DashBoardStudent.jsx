import React from "react";
import { Link } from "react-router-dom";
import StudentCvList from "./StudentCvList";

export default function DashBoardStudent() {
    return (
        <div className="min-h-screen bg-gray-50">
            <header className="bg-white shadow">
                <div className="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8">
                    <div className="flex justify-between items-center h-16">
                        <div className="flex items-center">
                            <span className="text-xl font-bold text-indigo-600">LeandrOSE</span>
                        </div>
                        <nav className="flex items-center space-x-4">
                            <Link to="/dashboard/student/uploadCv" className="text-gray-700 hover:text-indigo-600 px-3 py-2 rounded-md text-sm font-medium">Téléverser mon CV</Link>
                        </nav>
                    </div>
                </div>
            </header>

            <main className="py-10">
                <div className="max-w-5xl mx-auto px-4 sm:px-6 lg:px-8">
                    <h1 className="text-2xl font-semibold text-gray-900 mb-4">Bienvenue Étudiant</h1>
                    <p className="text-gray-600 mb-6">
                        Ceci est votre tableau de bord étudiant. Vous pouvez gérer vos CVs et bientôt consulter les offres de stage.
                    </p>

                    <StudentCvList />
                </div>
            </main>
        </div>
    );
}
