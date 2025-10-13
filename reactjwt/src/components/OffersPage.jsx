import React, {useEffect, useState} from "react";
import {getApprovedOffers, getRejectedOffers} from "../api/apiGestionnaire.jsx";
import {Link, useNavigate} from "react-router-dom";
import {useTranslation} from "react-i18next";

export default function OffersPage() {
    const { t } = useTranslation()
    const [offers, setOffers] = useState([]);
    const [filter, setFilter] = useState("approved");
    const [error, setError] = useState("");
    const navigate = useNavigate()



    useEffect(() => {
        async function loadOffers() {
            try {
                let data = [];
                if (filter === "approved") data = await getApprovedOffers();
                if (filter === "rejected") data = await getRejectedOffers();
                setOffers(data);
            } catch (err) {
                console.error(err);
                setError(t("pendingOffers.errors.serverError"));
            }
        }
        loadOffers();
    }, [filter]);

    return (
        <div className="min-h-screen bg-gray-50 flex items-center justify-center">
            <div className="w-full max-w-6xl bg-white p-8 rounded-xl shadow-md">

                <h2 className="text-2xl font-bold mb-6 text-gray-800 border-b pb-2">
                    {t("offerPagesDetails.title")}
                </h2>

                <div className="flex gap-4 mb-6 justify-center">
                    <button
                        onClick={() => setFilter("approved")}
                        className={`px-3 py-1 rounded ${
                            filter === "approved" ? "bg-green-600 text-white shadow-md" 
                                : "bg-gray-200 text-gray-700 hover:bg-gray-300"
                        }`}
                    >
                        {t("pendingOffers.status.approved")}
                    </button>
                    <button
                        onClick={() => setFilter("rejected")}
                        className={`px-3 py-1 rounded ${
                            filter === "rejected" ? "bg-red-600 text-white shadow-md" 
                                : "bg-gray-200 text-gray-700 hover:bg-gray-300"
                        }`}
                    >
                        {t("pendingOffers.status.rejected")}
                    </button>
                </div>

                {error && <p className="text-red-500 mb-4">{error}</p>}

                <table className="w-full border border-gray-300 rounded-lg overflow-hidden shadow-sm text-sm">
                    <thead className="bg-gray-50 text-gray text-left">
                    <tr>
                        <th className="border px-4 py-2 text-left">{t("pendingOffers.table.title")}</th>
                        <th className="border px-4 py-2 text-left">{t("pendingOffers.table.company")}</th>
                        <th className="border px-4 py-2 text-left">{t("pendingOffers.table.actions")}</th>
                    </tr>
                    </thead>
                    <tbody>
                    {offers.map((offer) => (
                        <tr key={offer.id} >
                            <td className="border px-4 py-2">{offer.description}</td>
                            <td className="border px-4 py-2">{offer.companyName}</td>
                            <td className="border px-4 py-2">
                                <Link
                                    to={`/dashboard/gestionnaire/offers/${offer.id}`}
                                    className="text-blue-600 font-medium hover:underline"
                                >
                                    {t("offerPagesDetails.details")}
                                </Link>
                            </td>
                        </tr>
                    ))}
                    </tbody>
                </table>

                <div className="mt-8 justify-center">
                    <button
                        type="button"
                        onClick={() => navigate(-1)}
                        className="inline-flex items-center justify-center gap-2 px-6 py-3 border border-gray-300
                        text-sm font-medium rounded-md shadow-sm text-gray-700 bg-white hover:bg-gray-50"
                    >
                        {t("uploadStageEmployeur.backToDashboard")}
                    </button>
                </div>
            </div>
        </div>
    );
}
