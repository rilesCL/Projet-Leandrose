import {useEffect, useState} from "react"
import {approveCv, rejectCv, getPendingCvs, downloadCv} from "../api/apiManager.jsx";

export default function PendingCvPage() {
    const [pendingCvs, setPendingCvs] = useState([]);
    const [comments, setComments] = useState([])
    const [loading, setLoading] = useState(false)
    const [error, setErrors] = useState(null);

    useEffect(() => {
        const loadPending = async () => {
            try{
                const donne = await getPendingCvs();
                setPendingCvs(donne)

            }
            catch(err){
                console.log(err)
            }
        }
        loadPending()
    }, [])

    async function handleApprove(cvId){
        try{
            await approveCv(cvId)
            setPendingCvs(pendingCvs.filter(cv => cv.id !== cvId));
        }
        catch(err){
            console.log(`Erreur approving Cv: ${err.message}`)
        }
    }

    async function handleReject(cvId){
        const comment = comments[cvId] || ''

        if(!comment.trim()){
            setErrors("Il faut écrire un commentaire avant de rejeter un CV")
            // alert("Il faut écrire un commentaire avant de rejeter un CV")
            return;
        }

        try{
            await rejectCv(cvId)
            setPendingCvs(pendingCvs.filter(cv => cv.id !== cvId));
        }
        catch(err){
            console.log(`Erreur rejecting Cv: ${err.message}`)
        }
    }

    function handleCommentChange(cvId, value){
        setComments(prev => ({
            ...prev,
            [cvId]: value,
        }))
    }



    if (loading) return <p>Loading pending CVs...</p>;
    if (error) return <p style={{ color: "red" }}>{error}</p>;

    return (
        <div className="space-y-6">
            <h2 className="text-2xl font-bold">CVs en attente</h2>
            {pendingCvs.length === 0 && <p>No pending Cvs</p>}


            <table className="w-full border-collapse border border-gray-300">
                <thead>
                <tr className="bg-gray-100">
                    <th className="border p-2">Id</th>
                    <th className="border p-2">CV</th>
                    <th className="border p-2">Commentaire</th>
                    <th className="border p-2">Actions</th>
                </tr>
                </thead>
                <tbody>
                {pendingCvs.map(cv => (
                    <tr key={cv.id} className="border">
                        <td className="p-2">{cv.studentId}</td>
                        <td className="px-4 py-2 border">
                            <button
                                onClick={() => downloadCv(cv.id)}
                                className="text-blue-600 hover:underline"
                            >
                                Télécharger CV
                            </button>
                        </td>
                        <td className="p-2">

                            <textarea
                                placeholder="Write a comment (required for rejection)"
                                value={comments[cv.id] || ""}
                                onChange={e => handleCommentChange(cv.id, e.target.value)}
                                style={{ width: "100%", height: "80px" }}
                            />
                        </td>
                        <td className="p-2 space-x-2">
                            <button onClick={() => handleApprove(cv.id)}
                                    className="bg-green-500 text-white px-3 py-1 rounded">Approuver</button>
                            <button onClick={() => handleReject(cv.id)}
                                    className="bg-red-500 text-white px-3 py-1 rounded">Rejeter</button>
                        </td>
                    </tr>
                ))}
                </tbody>
            </table>
        </div>
    );
}
