const API_BASE = 'http://localhost:8080/gestionnaire';

export async function getPendingCvs(){
    try{
        const accessToken =  localStorage.getItem('accessToken');
        const tokenType = (localStorage.getItem('tokenType') || 'BEARER').toUpperCase();

        const response = await fetch(API_BASE, {
            method: "GET",
            headers: {
                "Content-Type": "application/json",
                "Authorization": tokenType.startsWith('BEARER') ? `Bearer ${accessToken}` : accessToken,
            }
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