import React, { useEffect, useState } from "react";
import { useParams, useNavigate } from "react-router-dom";
import {getOfferDetails, downloadOfferPdf} from "../api/apiGestionnaire.jsx";
import {useTranslation} from "react-i18next";


export default function OfferDetailsPage(){
    const {id} = useParams()
    const {t} = useTranslation()
    const navigate = useNavigate()
    const [loading, setLoading] = useState(true)
    const [offer, setOffer] = useState(null)
    const [error, setError] = useState("")

    const handleDownload = async (offerId) => {
        try {
            await downloadOfferPdf(offerId);
        } catch (err) {
            console.error(err);
        }
    };

    useEffect(() => {
        async function loadOffer(){
            try{
                const data = await getOfferDetails(id)
                setOffer(data)

            }
            catch(err){
                setError(t("pendingOffers.errors.serverError") + err)
            }
            finally{
                setLoading(false)
            }
        }
        loadOffer()
    }, [id])

    if(error){
        return <p className="text-red-500 p-6">{error}</p>
    }
    if(loading){
        return <p className="p-6">{t("pendingOffers.loading")}</p>
    }


    return (
        <div className="p-6 flex justify-center">
            <div className="bg-white rounded-2xl shadow-lg w-full max-w-2xl p-8">
                {/* Back button */}
                <button
                    onClick={() => navigate(-1)}
                    className="mb-6 text-indigo-600 hover:underline text-sm flex items-center"
                >
                    ← {t("offerPagesDetails.back")}
                </button>

                <div className="border-b pb-4 mb-6">
                    <h2 className="text-2xl font-bold text-gray-800">
                        {t("offerPagesDetails.contact")}
                    </h2>
                    <p className="text-gray-600">
                        {offer?.employeur?.firstName} {offer?.employeur?.lastName}
                    </p>
                    <p className="text-sm text-gray-500">{offer?.employeur?.email}</p>
                </div>

                <div className="grid grid-cols-2 gap-6">
                    <div>
                        <p className="text-sm font-semibold text-gray-700">
                            {t("pendingOffers.table.company")}:
                        </p>
                        <p className="text-gray-800">{offer.companyName}</p>
                    </div>

                    <div>
                        <p className="text-sm font-semibold text-gray-700">
                            {t("pendingOffers.table.title")}:
                        </p>
                        <p className="text-gray-800">{offer.description}</p>
                    </div>

                    <div>
                        <p className="text-sm font-semibold text-gray-700">
                            {t("offerPagesDetails.duration")}:
                        </p>
                        <p className="text-gray-800">{offer.durationInWeeks} weeks</p>
                    </div>

                    <div>
                        <p className="text-sm font-semibold text-gray-700">
                            {t("offerPagesDetails.start_date")}:
                        </p>
                        <p className="text-gray-800">
                            {new Date(offer.startDate).toLocaleDateString()}
                        </p>
                    </div>

                    <div className="col-span-2">
                        <p className="text-sm font-semibold text-gray-700">
                            {t("offerPagesDetails.address")}:
                        </p>
                        <p className="text-gray-800">{offer.address}</p>
                    </div>
                </div>

                <div className="mt-6 flex justify-center">
                    <button
                        onClick={() => handleDownload(offer.id)}
                        className="px-4 py-2 bg-indigo-600 text-white rounded-lg hover:bg-indigo-700 transition"
                    >
                        {t("pendingOffers.actions.downloadPdf")}
                    </button>
                </div>
            </div>
        </div>
    );
}