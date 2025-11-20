import React, { useEffect, useMemo, useState } from "react";
import { useNavigate } from "react-router-dom";
import { FaSignOutAlt } from "react-icons/fa";
import { fetchProfStudents } from "../../api/apiProf.jsx";
import ThemeToggle from "../ThemeToggle.jsx";

const STATUS_LABELS = {
    // stage
    EN_COURS: "En cours",
    TERMINE: "Terminé",
    // évaluation
    A_FAIRE: "À faire",
    EN_COURS_EVAL: "En cours",
    TERMINEE: "Terminée",
};

function prettifyStatus(value) {
    if (!value) return "";
    if (STATUS_LABELS[value]) return STATUS_LABELS[value];
    return value
        .toString()
        .split("_")
        .map((w) => w.charAt(0) + w.slice(1).toLowerCase())
        .join(" ");
}

function badgeClass(value) {
    switch (value) {
        case "A_FAIRE":
            return "bg-yellow-100 text-yellow-800 border border-yellow-300";
        case "EN_COURS":
        case "EN_COURS_EVAL":
            return "bg-blue-100 text-blue-800 border border-blue-300";
        case "TERMINE":
        case "TERMINEE":
            return "bg-emerald-100 text-emerald-800 border border-emerald-300";
        default:
            return "bg-gray-100 text-gray-800 border border-gray-300";
    }
}

export default function ProfStudentsPage() {
    const navigate = useNavigate();
    const [profId, setProfId] = useState(null);
    const [userName, setUserName] = useState("");
    const [name, setName] = useState("");
    const [company, setCompany] = useState("");
    const [dateFrom, setDateFrom] = useState("");
    const [dateTo, setDateTo] = useState("");
    const [evaluationStatus, setEvaluationStatus] = useState("");
    const [sortBy, setSortBy] = useState("name");
    const [asc, setAsc] = useState(true);
    const [rows, setRows] = useState([]);
    const [loading, setLoading] = useState(true);
    const [firstLoadDone, setFirstLoadDone] = useState(false);
    const [errorMsg, setErrorMsg] = useState("");

    const handleLogout = () => {
        sessionStorage.clear();
        localStorage.clear();
        navigate("/login", { replace: true });
    };

    useEffect(() => {
        const token = sessionStorage.getItem("accessToken");
        if (!token) {
            setErrorMsg("Non authentifié. Veuillez vous connecter.");
            setLoading(false);
            return;
        }
        (async () => {
            try {
                const res = await fetch("http://localhost:8080/user/me", {
                    method: "GET",
                    headers: {
                        Accept: "application/json",
                        Authorization: `Bearer ${token}`,
                    },
                });
                if (!res.ok) throw new Error("Impossible de récupérer le profil.");
                const me = await res.json();
                if (me?.role !== "PROF") {
                    throw new Error("Accès refusé : rôle PROF requis.");
                }
                setProfId(me.id);
                setUserName(me.firstName || "");
            } catch (e) {
                setErrorMsg(e.message || "Erreur d'authentification.");
            } finally {
                setLoading(false);
            }
        })();
    }, []);

    const doSearch = async () => {
        if (!profId) return;
        setLoading(true);
        setErrorMsg("");
        try {
            const data = await fetchProfStudents(profId, {
                name: name.trim() || undefined,
                company: company.trim() || undefined,
                dateFrom: dateFrom || undefined,
                dateTo: dateTo || undefined,
                evaluationStatus: evaluationStatus || undefined,
                sortBy,
                asc,
            });
            setRows(Array.isArray(data) ? data : []);
            setFirstLoadDone(true);
        } catch (err) {
            setErrorMsg(err?.response?.data || "Erreur lors du chargement.");
        } finally {
            setLoading(false);
        }
    };

    useEffect(() => {
        if (profId && !firstLoadDone) {
            doSearch();
        }
    }, [profId, firstLoadDone]);

    const resetFilters = () => {
        setName("");
        setCompany("");
        setDateFrom("");
        setDateTo("");
        setEvaluationStatus("");
        setSortBy("name");
        setAsc(true);
        if (profId) doSearch();
    };

    const onClickSortStudent = () => {
        if (sortBy === "name") {
            setAsc((v) => !v);
        } else {
            setSortBy("name");
            setAsc(true);
        }
    };

    const sortedIcon = useMemo(() => (asc ? "▲" : "▼"), [asc]);

    return (
        <div className="min-h-screen bg-gradient-to-br from-slate-50 via-blue-50 to-indigo-50">
            <header className="bg-white shadow">
                <div className="w-full px-4 sm:px-6 lg:px-8">
                    <div className="flex justify-between items-center h-16">
                        <div className="flex items-center">
                            <span className="text-xl font-bold text-indigo-600">
                                LeandrOSE
                            </span>
                        </div>

                        <nav
                            className="flex items-center space-x-4"
                            aria-label="Navigation principale"
                        >
                            <span className="text-gray-700">
                                Bienvenue, {userName}
                            </span>
                            <ThemeToggle/>
                            <button
                                onClick={handleLogout}
                                className="flex items-center text-gray-600 hover:text-red-600 transition"
                            >
                                <FaSignOutAlt className="mr-1" />
                                <span className="hidden sm:inline">
                                    Déconnexion
                                </span>
                            </button>
                        </nav>
                    </div>
                </div>
            </header>

            <div className="py-10">
                <div className="w-full px-4 sm:px-6 lg:px-8">
                    <h1 className="text-2xl font-semibold text-gray-900 mb-6">Mes étudiants</h1>

                    {/* Filtres */}
                    <div className="bg-white rounded-lg shadow p-6 mb-6">
                        <div className="grid grid-cols-1 md:grid-cols-5 gap-3 mb-4">
                            <input
                                type="text"
                                placeholder="Nom de l'étudiant"
                                value={name}
                                onChange={(e) => setName(e.target.value)}
                                className="px-3 py-2 rounded border border-gray-300 bg-white text-gray-900 focus:outline-none focus:ring-2 focus:ring-indigo-500 focus:border-transparent"
                            />
                            <input
                                type="text"
                                placeholder="Entreprise"
                                value={company}
                                onChange={(e) => setCompany(e.target.value)}
                                className="px-3 py-2 rounded border border-gray-300 bg-white text-gray-900 focus:outline-none focus:ring-2 focus:ring-indigo-500 focus:border-transparent"
                            />
                            <input
                                type="date"
                                placeholder="De"
                                value={dateFrom}
                                onChange={(e) => setDateFrom(e.target.value)}
                                className="px-3 py-2 rounded border border-gray-300 bg-white text-gray-900 focus:outline-none focus:ring-2 focus:ring-indigo-500 focus:border-transparent"
                            />
                            <input
                                type="date"
                                placeholder="À"
                                value={dateTo}
                                onChange={(e) => setDateTo(e.target.value)}
                                className="px-3 py-2 rounded border border-gray-300 bg-white text-gray-900 focus:outline-none focus:ring-2 focus:ring-indigo-500 focus:border-transparent"
                            />
                            <select
                                value={evaluationStatus}
                                onChange={(e) => setEvaluationStatus(e.target.value)}
                                className="px-3 py-2 rounded border border-gray-300 bg-white text-gray-900 focus:outline-none focus:ring-2 focus:ring-indigo-500 focus:border-transparent"
                            >
                                <option value="">Statut évaluation</option>
                                <option value="A_FAIRE">À faire</option>
                                <option value="EN_COURS">En cours</option>
                                <option value="TERMINEE">Terminée</option>
                            </select>
                        </div>

                        <div className="flex gap-3">
                            <button
                                onClick={doSearch}
                                disabled={loading || !profId}
                                className="px-4 py-2 rounded bg-indigo-600 text-white hover:bg-indigo-700 disabled:opacity-50 disabled:cursor-not-allowed transition"
                            >
                                Rechercher
                            </button>
                            <button
                                onClick={resetFilters}
                                className="px-4 py-2 rounded bg-gray-200 text-gray-700 hover:bg-gray-300 transition"
                            >
                                Réinitialiser
                            </button>
                        </div>
                    </div>

                    {/* Messages */}
                    {errorMsg && (
                        <div className="mb-6 p-4 border border-red-300 bg-red-50 text-red-800 rounded-lg">
                            {errorMsg}
                        </div>
                    )}

                    {/* Tableau */}
                    <div className="bg-white rounded-lg shadow overflow-hidden">
                        <div className="overflow-x-auto">
                            <table className="min-w-full text-sm">
                                <thead className="bg-gray-50 border-b border-gray-200">
                                <tr>
                                    <th
                                        className="text-left px-6 py-3 text-gray-700 font-semibold whitespace-nowrap cursor-pointer select-none hover:bg-gray-100 transition"
                                        onClick={onClickSortStudent}
                                        title="Trier par nom"
                                    >
                                        Étudiant{" "}
                                        <span className="inline-block text-indigo-600">
                                                {sortBy === "name" ? sortedIcon : ""}
                                            </span>
                                    </th>
                                    <th className="text-left px-6 py-3 text-gray-700 font-semibold">
                                        Entreprise
                                    </th>
                                    <th className="text-left px-6 py-3 text-gray-700 font-semibold">
                                        Début / Fin
                                    </th>
                                    <th className="text-left px-6 py-3 text-gray-700 font-semibold">
                                        Statut stage
                                    </th>
                                    <th className="text-left px-6 py-3 text-gray-700 font-semibold">
                                        Statut évaluation
                                    </th>
                                </tr>
                                </thead>

                                <tbody className="divide-y divide-gray-200">
                                {loading && (
                                    <tr>
                                        <td className="px-6 py-8 text-center text-gray-500" colSpan={5}>
                                            Chargement…
                                        </td>
                                    </tr>
                                )}

                                {!loading && rows.length === 0 && (
                                    <tr>
                                        <td className="px-6 py-8 text-center text-gray-500" colSpan={5}>
                                            Aucun étudiant à afficher.
                                        </td>
                                    </tr>
                                )}

                                {!loading &&
                                    rows.map((it) => (
                                        <tr
                                            key={it.ententeId}
                                            className="hover:bg-gray-50 transition"
                                        >
                                            <td className="px-6 py-4">
                                                <div className="font-semibold text-gray-900">
                                                    {`${it.studentLastName ?? ""} ${it.studentFirstName ?? ""}`.trim()}
                                                </div>
                                                <div className="text-xs text-gray-500">{it.offerTitle}</div>
                                            </td>
                                            <td className="px-6 py-4 text-gray-700">
                                                {it.companyName || <span className="text-gray-400">—</span>}
                                            </td>
                                            <td className="px-6 py-4">
                                                <div className="text-sm text-gray-700">
                                                    {it.startDate || "—"}{" "}
                                                    {it.endDate ? (
                                                        <span className="text-gray-500">→ {it.endDate}</span>
                                                    ) : null}
                                                </div>
                                            </td>
                                            <td className="px-6 py-4">
                                                    <span className={`px-2 py-1 rounded text-xs font-medium ${badgeClass(it.stageStatus)}`}>
                                                        {prettifyStatus(it.stageStatus)}
                                                    </span>
                                            </td>
                                            <td className="px-6 py-4">
                                                    <span
                                                        className={`px-2 py-1 rounded text-xs font-medium ${badgeClass(
                                                            it.evaluationStatus
                                                        )}`}
                                                    >
                                                        {prettifyStatus(it.evaluationStatus)}
                                                    </span>
                                            </td>
                                        </tr>
                                    ))}
                                </tbody>
                            </table>
                        </div>
                    </div>
                </div>
            </div>
        </div>
    );
}