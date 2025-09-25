import React, {useState, useEffect} from "react";
import { FaBell, FaSignOutAlt } from "react-icons/fa"
import {getPendingCvs} from "../api/apiManager.jsx"
import {Link, Outlet, useNavigate} from "react-router-dom"

export default function DashBoardGestionnaire(){
    const navigate = useNavigate()
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

    const handleLogout = () => {
        sessionStorage.clear()
        navigate("/login", {replace: true})
    }

    return (
        <div className="min-h-screen bg-gray-50">
            <header className="bg-white shadow">
                <div className="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8 w-full">
                    <div className="flex justify-between items-center h-16">
                        <div className="flex items-center space-x-6">
                            <nav className="flex items-center space-x-5">
                                <Link to="cv" className="relative inline-block">
                                    <FaBell className="h-6 w-6 text-gray-600" />
                                    {pendingCount > 0 && (
                                        <span className="absolute -top-2 -right-2 bg-red-500 text-white text-xs font-bold rounded-full h-5 w-5 flex items-center justify-center">
                                            {pendingCount}
                                         </span>
                                    )}
                                </Link>
                            </nav>
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
                </div>
            </header>

            <main className="py-10">
                <div className="max-w-6xl mx-auto px-4 sm:px-6 lg:px-8">
                    <h1 className="text-2xl font-semibold text-gray-900 mb-4">
                        Bienvenue Gestionnaire
                    </h1>

                    <Outlet />
                </div>
            </main>
        </div>
    );
}