import React, { useEffect, useMemo, useState } from "react";
import { fetchProfStudents } from "../../api/apiProf.jsx";


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
            return "bg-yellow-500/20 text-yellow-300 border border-yellow-500/40";
        case "EN_COURS":
        case "EN_COURS_EVAL":
            return "bg-blue-500/20 text-blue-300 border border-blue-500/40";
        case "TERMINE":
        case "TERMINEE":
            return "bg-emerald-500/20 text-emerald-300 border border-emerald-500/40";
        default:
            return "bg-slate-500/20 text-slate-300 border border-slate-500/40";
    }
}

export default function ProfStudentsPage() {
    const [profId, setProfId] = useState(null);

    const [name, setName] = useState("");
    const [company, setCompany] = useState("");
    const [dateFrom, setDateFrom] = useState("");
    const [dateTo, setDateTo] = useState("");
    const [evaluationStatus, setEvaluationStatus] = useState("");


    const [sortBy, setSortBy] = useState("name"); // name|date|company
    const [asc, setAsc] = useState(true);


    const [rows, setRows] = useState([]);
    const [loading, setLoading] = useState(true);
    const [firstLoadDone, setFirstLoadDone] = useState(false);
    const [errorMsg, setErrorMsg] = useState("");

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
            } catch (e) {
                setErrorMsg(e.message || "Erreur d’authentification.");
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

    }, [profId]);

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
        <div className="min-h-screen bg-neutral-900 text-gray-100 p-6">
            <div className="max-w-6xl mx-auto">
                <h1 className="text-3xl font-bold mb-6">Mes étudiants</h1>

                {/* Filtres */}
                <div className="grid grid-cols-1 md:grid-cols-5 gap-3 mb-4">
                    <input
                        type="text"
                        placeholder="Nom de l’étudiant"
                        value={name}
                        onChange={(e) => setName(e.target.value)}
                        className="px-3 py-2 rounded bg-neutral-800 border border-neutral-700 focus:outline-none focus:ring-2 focus:ring-indigo-500"
                    />
                    <input
                        type="text"
                        placeholder="Entreprise"
                        value={company}
                        onChange={(e) => setCompany(e.target.value)}
                        className="px-3 py-2 rounded bg-neutral-800 border border-neutral-700 focus:outline-none focus:ring-2 focus:ring-indigo-500"
                    />
                    <input
                        type="date"
                        placeholder="De"
                        value={dateFrom}
                        onChange={(e) => setDateFrom(e.target.value)}
                        className="px-3 py-2 rounded bg-neutral-800 border border-neutral-700 focus:outline-none focus:ring-2 focus:ring-indigo-500"
                    />
                    <input
                        type="date"
                        placeholder="À"
                        value={dateTo}
                        onChange={(e) => setDateTo(e.target.value)}
                        className="px-3 py-2 rounded bg-neutral-800 border border-neutral-700 focus:outline-none focus:ring-2 focus:ring-indigo-500"
                    />
                    <select
                        value={evaluationStatus}
                        onChange={(e) => setEvaluationStatus(e.target.value)}
                        className="px-3 py-2 rounded bg-neutral-800 border border-neutral-700 focus:outline-none focus:ring-2 focus:ring-indigo-500"
                    >
                        <option value="">Statut évaluation</option>
                        <option value="A_FAIRE">À faire</option>
                        <option value="EN_COURS">En cours</option>
                        <option value="TERMINEE">Terminée</option>
                    </select>
                </div>

                <div className="flex gap-3 mb-6">
                    <button
                        onClick={doSearch}
                        disabled={loading || !profId}
                        className={`px-4 py-2 rounded bg-indigo-600 hover:bg-indigo-700 disabled:opacity-50`}
                    >
                        Rechercher
                    </button>
                    <button
                        onClick={resetFilters}
                        className="px-4 py-2 rounded bg-neutral-700 hover:bg-neutral-600"
                    >
                        Réinitialiser
                    </button>
                </div>

                {/* Messages */}
                {errorMsg && (
                    <div className="mb-4 p-3 border border-red-500/40 bg-red-500/10 text-red-200 rounded">
                        {errorMsg}
                    </div>
                )}

                {/* Tableau */}
                <div className="overflow-x-auto rounded border border-neutral-800">
                    <table className="min-w-full text-sm">
                        <thead className="bg-neutral-800/60 text-gray-300">
                        <tr>
                            <th
                                className="text-left px-4 py-3 whitespace-nowrap cursor-pointer select-none"
                                onClick={onClickSortStudent}
                                title="Trier par nom"
                            >
                                Étudiant <span className="inline-block text-indigo-400">{sortBy === "name" ? sortedIcon : ""}</span>
                            </th>
                            <th className="text-left px-4 py-3">Entreprise</th>
                            <th className="text-left px-4 py-3">Début / Fin</th>
                            <th className="text-left px-4 py-3">Statut stage</th>
                            <th className="text-left px-4 py-3">Statut évaluation</th>
                        </tr>
                        </thead>

                        <tbody>
                        {loading && (
                            <tr>
                                <td className="px-4 py-6 text-center text-gray-400" colSpan={5}>
                                    Chargement…
                                </td>
                            </tr>
                        )}

                        {!loading && rows.length === 0 && (
                            <tr>
                                <td className="px-4 py-6 text-center text-gray-400" colSpan={5}>
                                    Aucun étudiant à afficher.
                                </td>
                            </tr>
                        )}

                        {!loading &&
                            rows.map((it) => (
                                <tr
                                    key={it.ententeId}
                                    className="border-t border-neutral-800 hover:bg-neutral-800/40"
                                >
                                    <td className="px-4 py-3">
                                        <div className="font-semibold uppercase tracking-wide">
                                            {`${it.studentLastName ?? ""} ${it.studentFirstName ?? ""}`.trim()}
                                        </div>
                                        <div className="text-xs text-gray-400">{it.offerTitle}</div>
                                    </td>
                                    <td className="px-4 py-3">
                                        {it.companyName || <span className="text-gray-500">—</span>}
                                    </td>
                                    <td className="px-4 py-3">
                                        <div className="text-sm">
                                            {it.startDate || "—"}{" "}
                                            {it.endDate ? (
                                                <span className="text-gray-400">→ {it.endDate}</span>
                                            ) : null}
                                        </div>
                                    </td>
                                    <td className="px-4 py-3">
                      <span className={`px-2 py-1 rounded text-xs ${badgeClass(it.stageStatus)}`}>
                        {prettifyStatus(it.stageStatus)}
                      </span>
                                    </td>
                                    <td className="px-4 py-3">
                      <span
                          className={`px-2 py-1 rounded text-xs ${badgeClass(
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
    );
}
