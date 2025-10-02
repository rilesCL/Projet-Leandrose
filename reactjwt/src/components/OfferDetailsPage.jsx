import React, { useEffect, useState } from "react";
import { useParams, useNavigate } from "react-router-dom";
import {getOfferDetails, downloadOfferPdf} from "../api/apiGestionnaire.jsx";


export default function OfferDetailsPage(){
    const {id} = useParams()
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
                setError("Failed to load offer details" + err)
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
        return <p className="p-6">loading...</p>
    }

    if (!offer){
        return <p className="p-6">No offer found</p>
    }

    return (
        <div className="p-6">
            <button
                onClick={() => navigate(-1)}
                className="mb-4 text-indigo-600 hover:underline"
            >
                ← Back
            </button>

            <div className="space-y-2">
                <h2>Employer Contact</h2>
                <p>
                    {offer.employeur.firstName} {offer.employeur.lastName}
                </p>
                <p>Courriel: {offer.employeur.email}</p>
                <p><strong>Company:</strong> {offer.employeur.companyName}</p>
                <p><strong>Description:</strong> {offer.description}</p>
                <p><strong>Duration:</strong> {offer.durationInWeeks} weeks</p>
                <p><strong>Start Date:</strong> {new Date(offer.startDate).toLocaleDateString()}</p>
                <p><strong>Address:</strong> {offer.address}</p>
               <button
                    onClick={() => handleDownload(offer.id)}
                    className="text-blue-600 underline">
                   Download PDF
               </button>
            </div>
        </div>
    );
}