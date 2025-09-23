import './App.css'
import {
    RouterProvider,
    createBrowserRouter,
    createRoutesFromElements,
    Route,
    Navigate
} from 'react-router-dom';
import RouteLayout from "./components/RouteLayout.jsx";
import RegisterEtudiant from "./components/RegisterStudentForm.jsx";
import RegisterEmployeur from "./components/RegisterEmployeurForm.jsx";
import Login from "./components/Login.jsx";
import UploadStageEmployeur from "./components/UploadStageEmployeur.jsx";
import DashBoardEmployeur from "./components/DashBoardEmployeur.jsx";


function App() {
    const router = createBrowserRouter(
        createRoutesFromElements(
            <Route path="/" element={<RouteLayout />}>
                <Route index element={<Navigate to="/login" replace />} />
                <Route path="register/etudiant" element={<RegisterEtudiant />} />
                <Route path="register/employeur" element={<RegisterEmployeur />} />
                <Route path="login" element={<Login />} />
                <Route path="dashboard/employeur/createOffer" element={<UploadStageEmployeur />} />
                <Route path="dashboard" element={<h1>DashBoard</h1>} />
                <Route path="dashboard/student" element={<div className="p-8 text-center"><h1 className="text-2xl">Dashboard Étudiant</h1><p>Bienvenue dans votre espace étudiant!</p></div>} />
                <Route path="dashboard/employeur" element={<DashBoardEmployeur/>} />
                <Route path="dashboard/gestionnaire" element={<div className="p-8 text-center"><h1 className="text-2xl">Dashboard Gestionnaire</h1></div>} />
            </Route>
        )
    );

    return <RouterProvider router={router} />;
}

export default App;
