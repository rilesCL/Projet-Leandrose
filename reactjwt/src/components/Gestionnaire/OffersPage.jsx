import React, {useEffect, useState} from "react";
import {getApprovedOffers, getRejectedOffers} from "../../api/apiGestionnaire.jsx";
import {Link, useNavigate} from "react-router-dom";
import {useTranslation} from "react-i18next";

export default function OffersPage({selectedTerm}) {
    const {t} = useTranslation()
    const [offers, setOffers] = useState([]);
    const [filteredOffers, setFilteredOffers] = useState([]);
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
    }, [filter, t]);

    useEffect(() => {
        if (!selectedTerm || !offers.length) {
            setFilteredOffers(offers);
            return;
        }

        const filtered = offers.filter(offer => {
            if (!offer.schoolTerm) return false;

            const termParts = offer.schoolTerm.trim().split(/\s+/);
            const offerSeason = termParts[0]?.toUpperCase();
            const offerYear = parseInt(termParts[1]);

            return offerSeason === selectedTerm.season && offerYear === selectedTerm.year;
        });

        setFilteredOffers(filtered);
    }, [selectedTerm, offers]);

    return (
        <div className="bg-white shadow rounded-lg overflow-hidden">
            <div className="px-4 sm:px-6 py-4 border-b border-gray-200">
                <h3 className="text-lg font-medium text-gray-900">{t("offerPagesDetails.title")}</h3>
            </div>
            <div className="p-6">

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
                    {filteredOffers.length === 0 ? (
                        <tr>
                            <td colSpan="3" className="border px-4 py-8 text-center text-gray-500">
                                {selectedTerm
                                    ? t("pendingOffers.noOffersForTerm", {
                                        term: `${t(`terms.${selectedTerm.season}`)} ${selectedTerm.year}`
                                    })
                                    : t("pendingOffers.noOffers")
                                }
                            </td>
                        </tr>
                    ) : (
                        filteredOffers.map((offer) => (
                            <tr key={offer.id}>
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
                        ))
                    )}
                    </tbody>
                </table>

            </div>
        </div>
    );
}
