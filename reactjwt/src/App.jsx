import './App.css';
import { RouterProvider, createBrowserRouter, createRoutesFromElements, Route, Navigate } from 'react-router-dom';

import RouteLayout from "./components/RouteLayout.jsx";
import RegisterEtudiant from "./components/RegisterStudentForm.jsx";
import RegisterEmployeur from "./components/RegisterEmployeurForm.jsx";
import Login from "./components/Login.jsx";
import UploadStageEmployeur from "./components/UploadStageEmployeur.jsx";
import DashBoardEmployeur from "./components/DashBoardEmployeur.jsx";
import DashBoardStudent from "./components/DashBoardStudent.jsx";
import UploadCvStudent from "./components/UploadCvStudent.jsx";

function App() {
    const router = createBrowserRouter(
        createRoutesFromElements(
            <Route path="/" element={<RouteLayout />}>
                {/* Login/Registration routes */}
                <Route index element={<Navigate to="/login" replace />} />
                <Route path="register/etudiant" element={<RegisterEtudiant />} />
                <Route path="register/employeur" element={<RegisterEmployeur />} />
                <Route path="login" element={<Login />} />

                {/* Employeur routes */}
                <Route path="dashboard/employeur" element={<DashBoardEmployeur />} />
                <Route path="dashboard/employeur/createOffer" element={<UploadStageEmployeur />} />

                {/* Student routes */}
                <Route path="dashboard/student" element={<DashBoardStudent />} />
                <Route path="dashboard/student/uploadCv" element={<UploadCvStudent />} />

                {/* Other dashboards */}
                <Route path="dashboard" element={<h1>DashBoard</h1>} />
                <Route path="dashboard/gestionnaire" element={<div className="p-8 text-center"><h1 className="text-2xl">Dashboard Gestionnaire</h1></div>} />
            </Route>
        )
    );

    return <RouterProvider router={router} />;
}

export default App;
