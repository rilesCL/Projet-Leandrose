import React, {useEffect, useState} from "react";
import {getApprovedOffers, getRejectedOffers, getOfferDetails} from "../api/apiGestionnaire.jsx";

export default function OffersPage() {
    const [offers, setOffers] = useState([]);
    const [filter, setFilter] = useState("approved");
    const [error, setError] = useState("");

    useEffect(() => {
        async function loadOffers() {
            try {
                let data = [];
                if (filter === "approved") data = await getApprovedOffers();
                if (filter === "rejected") data = await getRejectedOffers();
                setOffers(data);
            } catch (err) {
                console.error(err);
                setError("Failed to load offers");
            }
        }
        loadOffers();
    }, [filter]);

    return (
        <div className="p-6">
            <h2 className="text-xl font-bold mb-4">Internship Offers</h2>

            {/* Filter buttons */}
            <div className="flex gap-4 mb-6">
                <button
                    onClick={() => setFilter("approved")}
                    className={`px-3 py-1 rounded ${
                        filter === "approved" ? "bg-green-600 text-white" : "bg-gray-200"
                    }`}
                >
                    Approved
                </button>
                <button
                    onClick={() => setFilter("rejected")}
                    className={`px-3 py-1 rounded ${
                        filter === "rejected" ? "bg-red-600 text-white" : "bg-gray-200"
                    }`}
                >
                    Rejected
                </button>
            </div>

            {error && <p className="text-red-500 mb-4">{error}</p>}

            <table className="w-full border-collapse border border-gray-300 text-sm">
                <thead className="bg-gray-100">
                <tr>
                    <th className="border px-2 py-1">Title</th>
                    <th className="border px-2 py-1">Company</th>
                    <th className="border px-2 py-1">PDF</th>
                    <th className="border px-2 py-1">Status</th>
                </tr>
                </thead>
                <tbody>
                {offers.map((offer) => (
                    <tr key={offer.id} className="hover:bg-gray-50">
                        <td className="border px-2 py-1">{offer.description}</td>
                        <td className="border px-2 py-1">{offer.companyName}</td>
                        <td className="border px-2 py-1">
                            {offer.pdfPath ? (
                                <a
                                    href={`http://localhost:8080/${offer.pdfPath}`}
                                    target="_blank"
                                    rel="noopener noreferrer"
                                    className="text-blue-600 underline"
                                >
                                    PDF
                                </a>
                            ) : (
                                "—"
                            )}
                        </td>
                        <td className="border px-2 py-1">{offer.status}</td>
                    </tr>
                ))}
                </tbody>
            </table>
        </div>
    );
}
