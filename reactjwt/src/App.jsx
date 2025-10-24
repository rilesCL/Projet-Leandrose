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
import OffersPage from "./components/OffersPage.jsx";
import OfferDetailsPage from "./components/OfferDetailsPage.jsx";
import OfferDetailPage from "./components/OfferDetailPage.jsx";
import StudentInternshipOffersList from "./components/StudentInternshipOffersList.jsx";
import ApplicationsPage from "./components/ApplicationsPage.jsx";
import OfferCandidaturesList from "./components/OfferCandidaturesList.jsx";
import EntentesStagePage from "./components/EntentesStagePage.jsx";
import SignerEntentePage from "./components/SignerEntentePage.jsx";
import EmployeurListeStages from "./components/EmployeurListeStages.jsx";
import CreateEntenteForm from "./components/CreateEntenteForm.jsx";
import StageDetails from "./components/StageDetails.jsx";

function App() {
    const router = createBrowserRouter(
        createRoutesFromElements(
            <Route path="/" element={<RouteLayout />}>
                <Route index element={<Navigate to="/login" replace />} />

                {/* Registration Routes */}
                <Route path="register" element={<RegisterLanding />} />
                <Route path="register/etudiant" element={<RegisterEtudiant />} />
                <Route path="register/employeur" element={<RegisterEmployeur />} />
                <Route path="login" element={<Login />} />

                {/* Employeur Routes */}
                <Route path="dashboard/employeur" element={<DashBoardEmployeur />} />
                <Route path="dashboard/employeur/createOffer" element={<UploadStageEmployeur />} />
                <Route path="dashboard/employeur/offers/:offerId/candidatures" element={<OfferCandidaturesList />} />
                <Route path="dashboard/employeur/ententes" element={<EmployeurListeStages/>}/>
                <Route path="dashboard/employeur/ententes/:id" element={<StageDetails />}/>
                <Route path="dashboard/employeur/ententes/:id/signer" element={<SignerEntentePage/>}/>

                {/* Student Routes */}
                <Route path="dashboard/student" element={<DashBoardStudent />} />
                <Route path="dashboard/student/uploadCv" element={<UploadCvStudent />} />
                <Route path="dashboard/student/offers" element={<StudentInternshipOffersList />} />
                <Route path="dashboard/student/offers/:offerId" element={<OfferDetailPage />} />
                <Route path="dashboard/student/applications" element={<ApplicationsPage />} />

                {/* Gestionnaire Routes */}
                <Route path="dashboard/gestionnaire" element={<DashBoardGestionnaire />}>
                    <Route path="cv" element={<PendingCvPage />} />
                    <Route path="applications" element={<ApplicationsPage />} />
                </Route>
                <Route path="dashboard/gestionnaire/offers" element={<OffersPage />} />
                <Route path="dashboard/gestionnaire/ententes" element={<EntentesStagePage/>} />
                <Route path="dashboard/gestionnaire/offers/:id" element={<OfferDetailsPage />} />
                <Route path="/dashboard/gestionnaire/ententes/create" element={<CreateEntenteForm />} />


                {/* Fallback Dashboard Route */}
                <Route path="dashboard" element={<h1>DashBoard</h1>} />
            </Route>
        )
    );

    return <RouterProvider router={router} />;
}

export default App;