import React, {useState, useEffect} from "react";
import { FaBell } from "react-icons/fa"
import {getPendingCvs} from "../api/apiManager.jsx"

export default function DashBoardManager(){
    const [pendingCount, setPendingCount] = useState(0)

    useEffect(() => {
        const fetchPendingCvs = async () => {
            try{
                const data = await getPendingCvs();
                console.log("Pending Cvs", data)
                setPendingCount(data.length)
            }
            catch(err){
                console.error("Erreur fetching Cvs", err)
            }
        }
        fetchPendingCvs()
    }, []);

    return (
        <div className="min-h-screen bg-gray-50">
            <header className="bg-white shadow">
                <div className="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8">
                    <div className="flex justify-between items-center h-16">
                        <div className="flex items-center">
                            <span className="text-xl font-bold text-indigo-600">LeandrOSE</span>
                        </div>
                        <nav className="flex items-center space-x-4">
                            <div className="relative cursor-pointer">
                                <FaBell className="w-6 h-6 text-gray-600 cursor-pointer left-0 hover:text-indigo-600" />
                                {pendingCount > 0 && (
                                    <span className="absolute -top-2 -right-2 bg-red-600 text-white">
                                        {pendingCount}
                                    </span>
                                )}
                            </div>
                        </nav>
                    </div>
                </div>
            </header>

            <main className="py-10">
                <div className="max-w-5xl mx-auto px-4 sm:px-6 lg:px-8">
                    <h1 className="text-2xl font-semibold text-gray-900 mb-4">Bienvenue Gestionnaire</h1>

                </div>
            </main>
        </div>
    );
}