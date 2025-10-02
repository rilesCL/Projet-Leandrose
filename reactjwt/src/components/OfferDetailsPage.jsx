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
        <div className="p-6">
            <button
                onClick={() => navigate(-1)}
                className="mb-4 text-indigo-600 hover:underline"
            >
                ← {t("offerPagesDetails.back")}
            </button>

            <div className="space-y-2">
                <h2>{t("offerPagesDetails.contact")}</h2>
                <p>
                    {offer.employeur.firstName} {offer.employeur.lastName}
                </p>
                <p><strong>{t("offerPagesDetails.email")}:</strong> {offer.employeur.email}</p>
                <p><strong>{t("pendingOffers.table.company")}:</strong> {offer.employeur.companyName}</p>
                <p><strong>{t("pendingOffers.table.title")}:</strong> {offer.description}</p>
                <p><strong>{t("offerPagesDetails.email")}:</strong> {offer.durationInWeeks} weeks</p>
                <p><strong>{t("offerPagesDetails.start_date")}:</strong> {new Date(offer.startDate).toLocaleDateString()}</p>
                <p><strong>{t("offerPagesDetails.address")}:</strong> {offer.address}</p>
               <button
                    onClick={() => handleDownload(offer.id)}
                    className="text-blue-600 underline">
                   {t("pendingOffers.actions.downloadPdf")}
               </button>
            </div>
        </div>
    );
}