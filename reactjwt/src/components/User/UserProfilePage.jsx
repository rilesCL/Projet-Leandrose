import React, { useEffect, useState } from "react";
import { useNavigate } from "react-router-dom";
import { useTranslation } from "react-i18next";

export default function UserProfilePage() {
    const { t } = useTranslation();
    const navigate = useNavigate();

    const [loading, setLoading] = useState(true);
    const [saving, setSaving] = useState(false);
    const [user, setUser] = useState(null);
    const [form, setForm] = useState({
        firstName: "",
        lastName: "",
        email: "",
        phone: "",
        password: "",
    });
    const [message, setMessage] = useState("");
    const [error, setError] = useState("");

    useEffect(() => {
        const token = sessionStorage.getItem("accessToken");
        if (!token) {
            navigate("/login");
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
                const data = await res.json();
                setUser(data);
                setForm({
                    firstName: data.firstName || "",
                    lastName: data.lastName || "",
                    email: data.email || "",
                    phone: data.phone || "",
                    password: "",
                });
            } catch (e) {
                setError(e.message || "Erreur lors du chargement du profil.");
            } finally {
                setLoading(false);
            }
        })();
    }, [navigate]);

    const handleChange = (e) => {
        const { name, value } = e.target;
        setForm((prev) => ({ ...prev, [name]: value }));
    };

    const handleSubmit = async (e) => {
        e.preventDefault();
        setMessage("");
        setError("");

        const token = sessionStorage.getItem("accessToken");
        if (!token) {
            navigate("/login");
            return;
        }

        // On n’envoie pas le mot de passe si le champ est vide
        const payload = { ...form };
        if (!payload.password) {
            delete payload.password;
        }

        setSaving(true);
        try {
            const res = await fetch("http://localhost:8080/user/me", {
                method: "PUT",
                headers: {
                    "Content-Type": "application/json",
                    Authorization: `Bearer ${token}`,
                },
                body: JSON.stringify(payload),
            });

            if (!res.ok) {
                throw new Error("Erreur lors de la sauvegarde du profil.");
            }

            const updated = await res.json();
            setUser(updated);
            setForm((prev) => ({
                ...prev,
                password: "", // on vide le champ password après
            }));
            setMessage("Profil mis à jour avec succès.");
        } catch (e) {
            setError(e.message || "Erreur lors de la mise à jour du profil.");
        } finally {
            setSaving(false);
        }
    };

    if (loading) {
        return (
            <div className="min-h-screen flex items-center justify-center bg-gradient-to-br from-slate-50 via-blue-50 to-indigo-50">
                <p className="text-gray-600">Chargement du profil…</p>
            </div>
        );
    }

    if (!user) {
        return (
            <div className="min-h-screen flex items-center justify-center bg-gradient-to-br from-slate-50 via-blue-50 to-indigo-50">
                <div className="bg-white shadow rounded-lg p-6 max-w-md w-full text-center">
                    <p className="text-red-600 mb-4">
                        {error || "Impossible d’afficher le profil."}
                    </p>
                    <button
                        onClick={() => navigate("/login")}
                        className="px-4 py-2 rounded bg-indigo-600 text-white hover:bg-indigo-700"
                    >
                        Retour à la connexion
                    </button>
                </div>
            </div>
        );
    }

    return (
        <div className="min-h-screen bg-gradient-to-br from-slate-50 via-blue-50 to-indigo-50 py-10">
            <div className="max-w-2xl mx-auto px-4">
                <button
                    onClick={() => navigate(-1)}
                    className="mb-4 text-sm text-gray-600 hover:text-indigo-600"
                >
                    ← Retour
                </button>

                <div className="bg-white rounded-2xl shadow-lg border border-gray-100 p-6">
                    <h1 className="text-2xl font-bold text-gray-900 mb-1">
                        {t("profile.title") || "Mon profil"}
                    </h1>
                    <p className="text-sm text-gray-500 mb-4">
                        {t("profile.subtitle") ||
                            "Modifiez vos informations de compte."}
                    </p>

                    {message && (
                        <div className="mb-3 p-2.5 rounded bg-green-50 border border-green-200 text-green-800 text-sm">
                            {message}
                        </div>
                    )}
                    {error && (
                        <div className="mb-3 p-2.5 rounded bg-red-50 border border-red-200 text-red-800 text-sm">
                            {error}
                        </div>
                    )}

                    <form onSubmit={handleSubmit} className="space-y-4">
                        <div className="grid grid-cols-1 md:grid-cols-2 gap-4">
                            <div>
                                <label className="block text-sm font-medium text-gray-700 mb-1">
                                    Prénom
                                </label>
                                <input
                                    type="text"
                                    name="firstName"
                                    value={form.firstName}
                                    onChange={handleChange}
                                    className="w-full px-3 py-2 border border-gray-300 rounded-lg focus:ring-2 focus:ring-indigo-500 focus:border-indigo-500"
                                />
                            </div>
                            <div>
                                <label className="block text-sm font-medium text-gray-700 mb-1">
                                    Nom
                                </label>
                                <input
                                    type="text"
                                    name="lastName"
                                    value={form.lastName}
                                    onChange={handleChange}
                                    className="w-full px-3 py-2 border border-gray-300 rounded-lg focus:ring-2 focus:ring-indigo-500 focus:border-indigo-500"
                                />
                            </div>
                        </div>

                        <div>
                            <label className="block text-sm font-medium text-gray-700 mb-1">
                                Email
                            </label>
                            <input
                                type="email"
                                name="email"
                                value={form.email}
                                disabled
                                className="w-full px-3 py-2 border border-gray-200 rounded-lg bg-gray-100 cursor-not-allowed text-gray-500"
                            />
                            <p className="text-xs text-gray-500 mt-1">
                                L’adresse courriel ne peut pas être modifiée.
                            </p>
                        </div>

                        <div>
                            <label className="block text-sm font-medium text-gray-700 mb-1">
                                Téléphone
                            </label>
                            <input
                                type="text"
                                name="phone"
                                value={form.phone}
                                onChange={handleChange}
                                className="w-full px-3 py-2 border border-gray-300 rounded-lg focus:ring-2 focus:ring-indigo-500 focus:border-indigo-500"
                            />
                        </div>

                        <div>
                            <label className="block text-sm font-medium text-gray-700 mb-1">
                                Nouveau mot de passe (optionnel)
                            </label>
                            <input
                                type="password"
                                name="password"
                                value={form.password}
                                onChange={handleChange}
                                placeholder="Laisser vide pour ne pas changer"
                                className="w-full px-3 py-2 border border-gray-300 rounded-lg focus:ring-2 focus:ring-indigo-500 focus:border-indigo-500"
                            />
                        </div>

                        <div className="pt-2 flex justify-end gap-3">
                            <button
                                type="button"
                                onClick={() => navigate(-1)}
                                className="px-4 py-2 rounded-lg border border-gray-300 text-gray-700 bg-white hover:bg-gray-50 text-sm font-medium"
                                disabled={saving}
                            >
                                Annuler
                            </button>
                            <button
                                type="submit"
                                disabled={saving}
                                className="px-4 py-2 rounded-lg bg-indigo-600 text-white text-sm font-semibold hover:bg-indigo-700 disabled:opacity-50"
                            >
                                {saving ? "Enregistrement…" : "Sauvegarder"}
                            </button>
                        </div>
                    </form>
                </div>
            </div>
        </div>
    );
}