import React, {useState, useEffect} from "react";
import { FaBell } from "react-icons/fa"
import {getPendingCvs} from "../api/apiManager.jsx"
import {Link, Outlet} from "react-router-dom"

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
                                <Link to="cv" className="relative inline-block">
                                    <FaBell className="w-6 h-6 text-gray-600 cursor-pointer hover:text-indigo-600" />

                                    {pendingCount > 0 && (
                                        <span className="absolute -top-1 -right-1 bg-red-500 text-white text-xs rounded-full px-2 py-0.5">
                                             {pendingCount}
                                        </span>
                                    )}
                                </Link>
                            </div>
                        </nav>
                    </div>
                </div>
            </header>

            <main className="py-10">
                <div className="max-w-5xl mx-auto px-4 sm:px-6 lg:px-8">
                    <h1 className="text-2xl font-semibold text-gray-900 mb-4">Bienvenue Gestionnaire</h1>

                    <Outlet/>
                </div>
            </main>
        </div>
    );
}