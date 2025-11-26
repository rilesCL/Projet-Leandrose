import React, { useEffect, useMemo, useState, useCallback } from "react";
import { useNavigate } from "react-router-dom";
import { fetchProfStudents } from "../../api/apiProf.jsx";
import { t } from "i18next";

const STATUS_LABELS = {
    EN_COURS: "profStudentsPage.status.inProgress",
    TERMINE: "profStudentsPage.status.finished",
    A_FAIRE: "profStudentsPage.status.toDo",
    EN_COURS_EVAL: "profStudentsPage.status.inProgress",
    TERMINEE: "profStudentsPage.status.finished",
};

function prettifyStatus(value) {
    if (!value) return "";
    if (STATUS_LABELS[value]) return t(STATUS_LABELS[value]);
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
            setErrorMsg(t("profStudentsPage.auth.notAuthenticated"));
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
                if (!res.ok) throw new Error(t("profStudentsPage.auth.fetchProfileError"));
                const me = await res.json();
                if (me?.role !== "PROF") {
                    throw new Error(t("profStudentsPage.auth.profRoleRequired"));
                }
                setProfId(me.id);
                setUserName(me.firstName || "");
            } catch (e) {
                setErrorMsg(e.message || t("profStudentsPage.auth.error"));
            } finally {
                setLoading(false);
            }
        })();
    }, []);

    const doSearch = useCallback(async (isInitialLoad = false) => {
        if (!profId) return;
        setLoading(true);
        setErrorMsg("");
        try {
            const data = await fetchProfStudents(profId, {
                name: name.trim() || undefined,
                evaluationStatus: evaluationStatus || undefined,
                sortBy,
                asc,
            });
            setRows(Array.isArray(data) ? data : []);
            if (isInitialLoad) {
                setFirstLoadDone(true);
            }
        } catch (err) {
            setErrorMsg(err?.response?.data || t("profStudentsPage.errors.loadingError"));
        } finally {
            setLoading(false);
        }
    }, [profId, name, evaluationStatus, sortBy, asc]);

    useEffect(() => {
        if (profId && !firstLoadDone) {
            doSearch(true);
        }
    }, [profId, firstLoadDone, doSearch]);

    useEffect(() => {
        if (profId && firstLoadDone) {
            const timeoutId = setTimeout(() => {
                doSearch(false);
            }, 300);
            return () => clearTimeout(timeoutId);
        }
    }, [profId, firstLoadDone, name, evaluationStatus, sortBy, asc, doSearch]);

    const resetFilters = () => {
        setName("");
        setEvaluationStatus("");
        setSortBy("name");
        setAsc(true);
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
        <div>
            <div className="py-10">
                <div className="w-full px-4 sm:px-6 lg:px-8">
                    <h1 className="text-2xl font-semibold text-gray-900 mb-6">
                        {t("profStudentsPage.page.students")}
                    </h1>

                    {/* Filters */}
                    <div className="bg-white rounded-lg shadow p-6 mb-6 border border-gray-200">
                        <div className="grid grid-cols-1 md:grid-cols-2 gap-4 mb-4">
                            <input
                                type="text"
                                placeholder={t("profStudentsPage.filters.studentName") || "Nom de l'étudiant"}
                                value={name}
                                onChange={(e) => setName(e.target.value)}
                                className="px-3 py-2 rounded border border-gray-300 bg-white text-gray-900 focus:outline-none focus:ring-2 focus:ring-indigo-500 focus:border-transparent"
                            />
                            <select
                                value={evaluationStatus}
                                onChange={(e) => setEvaluationStatus(e.target.value)}
                                className="px-3 py-2 rounded border border-gray-300 bg-white text-gray-900 focus:outline-none focus:ring-2 focus:ring-indigo-500 focus:border-transparent"
                            >
                                <option value="">{t("profStudentsPage.filters.evaluationStatus") || "Statut d'évaluation"}</option>
                                <option value="A_FAIRE">{t("profStudentsPage.status.toDo") || "À faire"}</option>
                                <option value="EN_COURS">{t("profStudentsPage.status.inProgress") || "En cours"}</option>
                                <option value="TERMINEE">{t("profStudentsPage.status.finished") || "Terminée"}</option>
                            </select>
                        </div>

                        <div className="flex gap-3">
                            <button
                                onClick={resetFilters}
                                className="px-4 py-2 rounded bg-gray-200 text-gray-700 hover:bg-gray-300 transition"
                            >
                                {t("profStudentsPage.buttons.reset") || "Réinitialiser"}
                            </button>
                        </div>
                    </div>

                    {/* Messages */}
                    {errorMsg && (
                        <div className="mb-6 p-4 border border-red-300 bg-red-50 text-red-800 rounded-lg">
                            {errorMsg}
                        </div>
                    )}

                    {/* Table */}
                    <div className="bg-white rounded-lg shadow overflow-hidden border border-gray-200">
                        <div className="overflow-x-auto">
                            <table className="min-w-full text-sm">
                                <thead className="bg-gray-50 border-b border-gray-200">
                                <tr>
                                    <th
                                        className="text-left px-6 py-3 text-gray-700 font-semibold whitespace-nowrap cursor-pointer select-none hover:bg-gray-100 transition"
                                        onClick={onClickSortStudent}
                                        title={t("profStudentsPage.table.sortByName")}
                                    >
                                        {t("profStudentsPage.table.student")}{" "}
                                        <span className="inline-block text-indigo-600">
                        {sortBy === "name" ? sortedIcon : ""}
                      </span>
                                    </th>
                                    <th className="text-left px-6 py-3 text-gray-700 font-semibold">
                                        {t("profStudentsPage.table.company")}
                                    </th>
                                    <th className="text-left px-6 py-3 text-gray-700 font-semibold">
                                        {t("profStudentsPage.table.dates")}
                                    </th>
                                    <th className="text-left px-6 py-3 text-gray-700 font-semibold">
                                        {t("profStudentsPage.table.stageStatus")}
                                    </th>
                                    <th className="text-left px-6 py-3 text-gray-700 font-semibold">
                                        {t("profStudentsPage.table.evaluationStatus")}
                                    </th>
                                </tr>
                                </thead>

                                <tbody className="divide-y divide-gray-200">
                                {loading && (
                                    <tr>
                                        <td className="px-6 py-8 text-center text-gray-500" colSpan={5}>
                                            {t("profStudentsPage.messages.loading")}
                                        </td>
                                    </tr>
                                )}

                                {!loading && rows.length === 0 && (
                                    <tr>
                                        <td className="px-6 py-8 text-center text-gray-500" colSpan={5}>
                                            {t("profStudentsPage.messages.noStudents")}
                                        </td>
                                    </tr>
                                )}

                                {!loading &&
                                    rows.map((it) => (
                                        <tr key={it.ententeId} className="hover:bg-gray-50 transition">
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
                                                    {it.endDate ? <span className="text-gray-500">→ {it.endDate}</span> : null}
                                                </div>
                                            </td>
                                            <td className="px-6 py-4">
                          <span
                              className={`px-2 py-1 rounded text-xs font-medium ${badgeClass(it.stageStatus)}`}
                          >
                            {prettifyStatus(it.stageStatus)}
                          </span>
                                            </td>
                                            <td className="px-6 py-4">
                          <span
                              className={`px-2 py-1 rounded text-xs font-medium ${badgeClass(it.evaluationStatus)}`}
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