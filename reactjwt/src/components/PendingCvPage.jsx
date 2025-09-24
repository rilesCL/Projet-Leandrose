import {useEffect, useState} from "react"
import {approveCv, rejectCv, getPendingCvs, downloadCv} from "../api/apiManager.jsx";



export default function PendingCvPage() {
    const [pendingCvs, setPendingCvs] = useState([]);

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


    return (
        <div className="space-y-6">
            <h2 className="text-2xl font-bold">CVs en attente</h2>
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
                            <input
                                type="text"
                                placeholder="Écrire un commentaire..."
                                className="border rounded p-1 w-full"
                            />
                        </td>
                        <td className="p-2 space-x-2">
                            <button className="bg-green-500 text-white px-3 py-1 rounded">Approuver</button>
                            <button className="bg-red-500 text-white px-3 py-1 rounded">Rejeter</button>
                        </td>
                    </tr>
                ))}
                </tbody>
            </table>
        </div>
    );
}
