// src/components/UserProfilePage.jsx
import React, { useEffect, useState } from "react";
import { useNavigate } from "react-router-dom";
import { useTranslation } from "react-i18next";
import { FaChevronDown, FaChevronUp } from "react-icons/fa";

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
        newPassword: "",
    });

    const [currentPassword, setCurrentPassword] = useState("");
    const [confirmPassword, setConfirmPassword] = useState("");

    const [successPopup, setSuccessPopup] = useState("");
    const [errorPopup, setErrorPopup] = useState("");

    const [showNameSection, setShowNameSection] = useState(false);
    const [showPhoneSection, setShowPhoneSection] = useState(true);
    const [showPasswordSection, setShowPasswordSection] = useState(false);

    const getDashboardByRole = (role) => {
        switch (role) {
            case "STUDENT":
                return "/dashboard/student";
            case "EMPLOYEUR":
                return "/dashboard/employeur";
            case "GESTIONNAIRE":
                return "/dashboard/gestionnaire";
            case "PROF":
                return "/dashboard/prof?tab=etudiants";
            default:
                return "/dashboard";
        }
    };

    useEffect(() => {
        const token = sessionStorage.getItem("accessToken");
        if (!token) {
            navigate("/login");
            return;
        }

        (async () => {
            try {
                const res = await fetch("http://localhost:8080/user/me", {
                    headers: { Authorization: `Bearer ${token}` },
                });

                if (!res.ok) throw new Error(t("userProfile.errors.loadFailed"));

                const data = await res.json();
                setUser(data);
                setForm({
                    firstName: data.firstName || "",
                    lastName: data.lastName || "",
                    email: data.email || "",
                    phone: "", // Champ vide par défaut, le numéro actuel est affiché en haut
                    newPassword: "",
                });
            } catch (err) {
                setErrorPopup(err.message || t("userProfile.errors.loadFailed"));
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
        setErrorPopup("");
        setSuccessPopup("");

        const token = sessionStorage.getItem("accessToken");
        if (!token) {
            navigate("/login");
            return;
        }

        if (!currentPassword) {
            setErrorPopup(t("userProfile.errors.currentPasswordRequired"));
            return;
        }

        if (form.newPassword && form.newPassword !== confirmPassword) {
            setErrorPopup(t("userProfile.errors.passwordMismatch"));
            return;
        }

        const payload = {
            firstName: form.firstName,
            lastName: form.lastName,
            phoneNumber: form.phone,
        };
        
        if (currentPassword) {
            payload.currentPassword = currentPassword;
        }

        if (form.newPassword) {
            payload.newPassword = form.newPassword;
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
                const errorData = await res.json().catch(() => ({}));
                throw new Error(errorData.error || t("userProfile.errors.updateFailed"));
            }

            const updated = await res.json();

            setUser((prev) => ({ ...prev, ...updated }));
            setForm((prev) => ({
                ...prev,
                firstName: updated.firstName || prev.firstName,
                lastName: updated.lastName || prev.lastName,
                phone: "", // Réinitialiser le champ téléphone après sauvegarde
                newPassword: "",
            }));

            setCurrentPassword("");
            setConfirmPassword("");

            setSuccessPopup(t("userProfile.success.updateSuccess"));
            
            // Auto-hide success message after 3 seconds and redirect
            setTimeout(() => {
                setSuccessPopup("");
                const roleForRedirect = updated.role || (user && user.role);
                navigate(getDashboardByRole(roleForRedirect));
            }, 3000);
        } catch (err) {
            setErrorPopup(err.message || t("userProfile.errors.updateFailed"));
        } finally {
            setSaving(false);
        }
    };

    if (loading) {
        return (
            <div className="min-h-screen flex items-center justify-center text-gray-600">
                {t("userProfile.loading")}
            </div>
        );
    }

    return (
        <div className="min-h-screen relative bg-gradient-to-br from-slate-50 via-blue-50 to-indigo-50 py-10">
            {successPopup && (
                <>
                    {/* Overlay pour bloquer la page */}
                    <div className="fixed inset-0 bg-black bg-opacity-50 z-40"></div>
                    {/* Message de succès */}
                    <div className="fixed top-1/3 left-1/2 -translate-x-1/2 bg-green-600 text-white px-6 py-3 rounded-lg shadow-lg text-lg z-50">
                        {successPopup}
                    </div>
                </>
            )}

            {errorPopup && (
                <div className="fixed top-4 left-1/2 -translate-x-1/2 bg-red-500 text-white px-5 py-2 rounded-lg shadow-md z-50">
                    {errorPopup}
                </div>
            )}

            <div className="max-w-2xl mx-auto px-4">
                <div className="bg-white rounded-2xl shadow-lg border p-6">
                    <h1 className="text-2xl font-bold mb-2">{t("userProfile.title")}</h1>
                    <p className="text-gray-500 mb-6">{t("userProfile.subtitle")}</p>

                    <form onSubmit={handleSubmit} className="space-y-6">
                        <div className="border rounded-xl p-4">
                            <button
                                type="button"
                                onClick={() => setShowNameSection(!showNameSection)}
                                className="w-full flex justify-between items-center"
                            >
                                <span className="font-semibold text-gray-800">{t("userProfile.nameSection.title")}</span>
                                {showNameSection ? <FaChevronUp /> : <FaChevronDown />}
                            </button>

                            {showNameSection && (
                                <div className="mt-4 grid grid-cols-1 md:grid-cols-2 gap-4">
                                    <div>
                                        <label className="block text-sm font-medium text-gray-700 mb-1">
                                            {t("userProfile.nameSection.firstName")}
                                        </label>
                                        <input
                                            type="text"
                                            name="firstName"
                                            value={form.firstName}
                                            onChange={handleChange}
                                            className="w-full px-3 py-2 border rounded-lg focus:outline-none focus:ring-2 focus:ring-indigo-500"
                                        />
                                    </div>
                                    <div>
                                        <label className="block text-sm font-medium text-gray-700 mb-1">
                                            {t("userProfile.nameSection.lastName")}
                                        </label>
                                        <input
                                            type="text"
                                            name="lastName"
                                            value={form.lastName}
                                            onChange={handleChange}
                                            className="w-full px-3 py-2 border rounded-lg focus:outline-none focus:ring-2 focus:ring-indigo-500"
                                        />
                                    </div>
                                </div>
                            )}
                        </div>

                        <div className="border rounded-xl p-4">
                            <span className="font-semibold text-gray-800">{t("userProfile.emailSection.title")}</span>
                            <div className="mt-3">
                                <input
                                    type="email"
                                    value={form.email}
                                    disabled
                                    className="w-full px-3 py-2 border rounded-lg bg-gray-100 text-gray-500 cursor-not-allowed"
                                />
                                <p className="text-xs text-gray-500 mt-1">
                                    {t("userProfile.emailSection.cannotBeChanged")}
                                </p>
                            </div>
                        </div>

                        <div className="border rounded-xl p-4">
                            {user && (user.phoneNumber || user.phone) && (
                                <div className="mb-3 text-sm text-gray-600">
                                    <span className="font-medium">{t("userProfile.phoneSection.current")}: </span>
                                    <span>{user.phoneNumber || user.phone}</span>
                                </div>
                            )}
                            <label className="block text-sm font-medium text-gray-700 mb-1">
                                {t("userProfile.phoneSection.label")}
                            </label>
                            <input
                                type="text"
                                name="phone"
                                value={form.phone || ""}
                                onChange={handleChange}
                                placeholder={t("userProfile.phoneSection.placeholder")}
                                className="w-full px-3 py-2 border rounded-lg focus:outline-none focus:ring-2 focus:ring-indigo-500"
                            />
                        </div>

                        <div className="border rounded-xl p-4">
                            <button
                                type="button"
                                onClick={() => setShowPasswordSection(!showPasswordSection)}
                                className="w-full flex justify-between items-center"
                            >
                                <span className="font-semibold text-gray-800">{t("userProfile.passwordSection.title")}</span>
                                {showPasswordSection ? <FaChevronUp /> : <FaChevronDown />}
                            </button>

                            {showPasswordSection && (
                                <div className="mt-4 space-y-4">
                                    <div>
                                        <label className="block text-sm font-medium text-gray-700 mb-1">
                                            {t("userProfile.passwordSection.newPasswordLabel")}
                                        </label>
                                        <input
                                            type="password"
                                            name="newPassword"
                                            value={form.newPassword}
                                            onChange={handleChange}
                                            placeholder={t("userProfile.passwordSection.newPasswordPlaceholder")}
                                            className="w-full px-3 py-2 border rounded-lg focus:outline-none focus:ring-2 focus:ring-indigo-500"
                                        />
                                    </div>
                                    <div>
                                        <label className="block text-sm font-medium text-gray-700 mb-1">
                                            {t("userProfile.passwordSection.confirmPasswordLabel")}
                                        </label>
                                        <input
                                            type="password"
                                            value={confirmPassword}
                                            onChange={(e) => setConfirmPassword(e.target.value)}
                                            placeholder={t("userProfile.passwordSection.confirmPasswordPlaceholder")}
                                            className="w-full px-3 py-2 border rounded-lg focus:outline-none focus:ring-2 focus:ring-indigo-500"
                                        />
                                    </div>
                                </div>
                            )}
                        </div>

                        <div className="border rounded-xl p-4">
                            <label className="block text-sm font-medium text-gray-700 mb-1">
                                {t("userProfile.currentPasswordSection.label")} <span className="text-red-500">*</span>
                            </label>
                            <input
                                type="password"
                                value={currentPassword}
                                onChange={(e) => setCurrentPassword(e.target.value)}
                                placeholder={t("userProfile.currentPasswordSection.placeholder")}
                                className="w-full px-3 py-2 border rounded-lg focus:outline-none focus:ring-2 focus:ring-indigo-500"
                            />
                            <p className="text-xs text-gray-500 mt-1">
                                {t("userProfile.currentPasswordSection.helpText")}
                            </p>
                        </div>

                        <div className="pt-4 flex justify-between items-center">
                            <button
                                type="button"
                                onClick={() => navigate(getDashboardByRole(user?.role))}
                                className="px-4 py-2 rounded-lg border border-gray-300 text-gray-700 bg-white hover:bg-gray-50 text-sm font-medium"
                                disabled={saving}
                            >
                                {t("userProfile.buttons.back")}
                            </button>

                            <button
                                type="submit"
                                disabled={saving}
                                className="px-4 py-2 rounded-lg bg-indigo-600 text-white text-sm font-semibold hover:bg-indigo-700 disabled:opacity-50"
                            >
                                {saving ? t("userProfile.buttons.saving") : t("userProfile.buttons.save")}
                            </button>
                        </div>
                    </form>
                </div>
            </div>
        </div>
    );
}
