import React from "react";

export default function DashBoardEmployeur() {
    return (
        <div className="min-h-screen bg-gray-50">
            <header className="bg-white shadow">
                <div className="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8">
                    <div className="flex justify-between items-center h-16">
                        <div className="flex items-center">
                            <span className="text-xl font-bold text-indigo-600">LeandrOSE</span>
                        </div>

                        <nav className="flex items-center space-x-4" aria-label="Main navigation">
                            <a
                                href="/dashboard/employeur/createOffer"
                                className="text-gray-700 hover:text-indigo-600 px-3 py-2 rounded-md text-sm font-medium focus:outline-none focus:ring-2 focus:ring-indigo-500"
                            >
                                Téléverser offre de stage
                            </a>
                        </nav>
                    </div>
                </div>
            </header>

            <main className="py-10">
                <div className="max-w-3xl mx-auto px-4 sm:px-6 lg:px-8">
                    <h1 className="text-2xl font-semibold text-gray-900 mb-4">Bienvenue</h1>
                    <p className="text-gray-600">
                        Ceci est une page simple avec un menu contenant un seul lien. Clique sur "Offres" pour naviguer.
                    </p>
                </div>
            </main>
        </div>
    );
}