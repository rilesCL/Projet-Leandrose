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
        <div className="p-6">
            <h2 className="text-xl font-bold mb-4">{t("offerPagesDetails.title")}</h2>

            {/* Filter buttons */}
            <div className="flex gap-4 mb-6">
                <button
                    onClick={() => setFilter("approved")}
                    className={`px-3 py-1 rounded ${
                        filter === "approved" ? "bg-green-600 text-white" : "bg-gray-200"
                    }`}
                >
                    {t("pendingOffers.status.approved")}
                </button>
                <button
                    onClick={() => setFilter("rejected")}
                    className={`px-3 py-1 rounded ${
                        filter === "rejected" ? "bg-red-600 text-white" : "bg-gray-200"
                    }`}
                >
                    {t("pendingOffers.status.rejected")}
                </button>
            </div>

            {error && <p className="text-red-500 mb-4">{error}</p>}

            <table className="w-full border-collapse border border-gray-300 text-sm">
                <thead className="bg-gray-100">
                <tr>
                    <th className="border px-2 py-1">{t("pendingOffers.table.company")}</th>
                    <th className="border px-2 py-1">{t("pendingOffers.table.status")}</th>
                    <th className="border px-2 py-1">{t("pendingOffers.table.actions")}</th>
                </tr>
                </thead>
                <tbody>
                {offers.map((offer) => (
                    <tr key={offer.id} className="hover:bg-gray-50">
                        <td className="border px-2 py-1">{offer.companyName}</td>
                        <td className="border px-2 py-1">{t(`offerPagesDetails.status.${offer.status}`)}</td>
                        <td>
                            <Link
                                to={`/dashboard/gestionnaire/offers/${offer.id}`}
                                className="text-blue-600 hover:underline"
                            >
                                {t("offerPagesDetails.details")}
                            </Link>
                        </td>
                    </tr>
                ))}
                </tbody>
            </table>

            <div className="p-5">
                <button
                    type="button"
                    onClick={() => navigate(-1)}
                    className="inline-flex items-center justify-center gap-2 px-6 py-3 border border-gray-300 text-sm font-medium rounded-md shadow-sm text-gray-700 bg-white hover:bg-gray-50"
                >
                    {t("uploadStageEmployeur.backToDashboard")}
                </button>
            </div>

        </div>
    );
}
