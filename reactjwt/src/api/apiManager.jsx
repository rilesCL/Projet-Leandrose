const API_BASE = 'http://localhost:8080/gestionnaire';

const accessToken =  localStorage.getItem('accessToken');
const tokenType = (localStorage.getItem('tokenType') || 'BEARER').toUpperCase();

function getAuthHeaders(){
    return {
        "Content-Type": "application/json",
        Authorization: tokenType.startsWith('BEARER') ? `Bearer ${accessToken}` : accessToken,
    }
}

export async function downloadCv(cvId){
    try{
        const response = await fetch(`${API_BASE}/cv/${cvId}/download`, {
            method: "GET",
            headers: {
                Authorization: tokenType.startsWith('BEARER') ? `Bearer ${accessToken}` : accessToken,
            }
        })

        if(!response.ok){
            throw new error(`HTTP erreur! Status ${response.status}`)
        }

        const blob = await response.blob()
        const url = window.URL.createObjectURL(blob)
        const a = document.createElement("a")
        a.href = url
        a.download = `cv_${cvId}.pdf`
        document.body.appendChild(a)
        a.click()
        a.remove()

        window.URL.revokeObjectURL(url)
    }
    catch(err){
        console.log("Erreur téléchargement PDF")
        throw err
    }
}

export async function getPendingCvs(){
    try{
        const response = await fetch(API_BASE, {
            method: "GET",
            headers: getAuthHeaders()
            // headers: {
            //     "Content-Type": "application/json",
            //     "Authorization": tokenType.startsWith('BEARER') ? `Bearer ${accessToken}` : accessToken,
            // }
        })

        if(!response.ok){
            throw new Error(`HTTP erreur! Status ${response.status}`)
        }

        const json = response.json()
        console.log(json)

        return await json;
    }
    catch(error){
        console.error("Erreur fetching Cvs: ", error)
        throw error;
    }

}
export async function approveCv(cvId){
        const reponse = await fetch(`${API_BASE}/cv/${cvId}/approve`, {
            method: 'POST',
            headers: {
                "Content-Type": "application/json",
                "Authorization": tokenType.startsWith('BEARER') ? `Bearer ${accessToken}` : accessToken,
            }
        });
        return reponse.json()
}

export async function rejectCv(cvId, comment){
        const response = await fetch(`${API_BASE}/cv/${cvId}/reject`, {
            method: "POST",
            headers: {
                "Content-Type": "application/json",
                "Authorization": tokenType.startsWith('BEARER') ? `Bearer ${accessToken}` : accessToken,
            },
            body: JSON.stringify([comment])
        });
        return response.json()
}
