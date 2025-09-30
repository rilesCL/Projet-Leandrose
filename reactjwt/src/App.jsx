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
import DashBoardGestionnaire from "./components/DashBoardGestionnaire.jsx";
import PendingCvPage from "./components/PendingCvPage.jsx";
import RegisterLanding from "./components/RegisterLanding.jsx";
import AddProgramPage from "./components/AddProgramPage.jsx";

function App() {
    const router = createBrowserRouter(
        createRoutesFromElements(
            <Route path="/" element={<RouteLayout />}>
                <Route index element={<Navigate to="/login" replace />} />
                <Route path="register" element={<RegisterLanding />} />
                <Route path="register/etudiant" element={<RegisterEtudiant />} />
                <Route path="register/employeur" element={<RegisterEmployeur />} />
                <Route path="login" element={<Login />} />

                <Route path="dashboard/employeur" element={<DashBoardEmployeur />} />
                <Route path="dashboard/employeur/createOffer" element={<UploadStageEmployeur />} />

                <Route path="dashboard/student" element={<DashBoardStudent />} />
                <Route path="dashboard/student/uploadCv" element={<UploadCvStudent />} />

                <Route path="dashboard" element={<h1>DashBoard</h1>} />
                <Route path="dashboard/gestionnaire/programs" element={<AddProgramPage />} />
                <Route path="dashboard/gestionnaire" element={<DashBoardGestionnaire />}>
                    <Route path="cv" element={<PendingCvPage />} />
                </Route>


            </Route>
        )
    );

    return <RouterProvider router={router} />;
}

export default App;
