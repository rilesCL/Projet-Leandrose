import './App.css';
import { RouterProvider, createBrowserRouter, createRoutesFromElements, Route, Navigate } from 'react-router-dom';

import RouteLayout from "./components/RouteLayout.jsx";
import RegisterEtudiant from "./components/RegisterStudentForm.jsx";
import RegisterEmployeur from "./components/RegisterEmployeurForm.jsx";
import Login from "./components/Login.jsx";
import UploadStageEmployeur from "./components/Employeur/UploadStageEmployeur.jsx";
import DashBoardEmployeur from "./components/Employeur/DashBoardEmployeur.jsx";
import DashBoardStudent from "./components/Étudiant/DashBoardStudent.jsx";
import UploadCvStudent from "./components/Étudiant/UploadCvStudent.jsx";
import DashBoardGestionnaire from "./components/Gestionnaire/DashBoardGestionnaire.jsx";
import PendingCvPage from "./components/Gestionnaire/PendingCvPage.jsx";
import RegisterLanding from "./components/RegisterLanding.jsx";
import OffersPage from "./components/Gestionnaire/OffersPage.jsx";
import OfferDetailsPage from "./components/Gestionnaire/OfferDetailsPage.jsx";
import OfferDetailPage from "./components/Étudiant/OfferDetailPage.jsx";
import StudentInternshipOffersList from "./components/Étudiant/StudentInternshipOffersList.jsx";
import ApplicationsPage from "./components/Étudiant/ApplicationsPage.jsx";
import OfferCandidaturesList from "./components/Employeur/OfferCandidaturesList.jsx";
import EntentesStagePage from "./components/Gestionnaire/EntentesStagePage.jsx";
import CreateEntenteForm from "./components/Gestionnaire/CreateEntenteForm.jsx";
import EmployeurListeStages from "./components/Employeur/EmployeurListeStages.jsx";
import SignerEntentePage from "./components/SignerEntentePage.jsx";
import ProfStudentPage from "./components/Prof/ProfStudentPage.jsx";
import EvaluationForm from "./components/Employeur/EvaluationForm.jsx";
import EvaluationsList from "./components/Employeur/EvaluationList.jsx";
import DashboardProf from "./components/Prof/DashboardProf.jsx";

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

                <Route path="dashboard/employeur/ententes/:id/signer" element={<SignerEntentePage/>}/>
                <Route path="dashboard/employeur/evaluations" element={<EvaluationsList/>}/>
                <Route path="dashboard/employeur/evaluation/:studentId/:offerId" element={<EvaluationForm/>}/>


                {/* Student Routes */}
                {/* Student Routes */}
                <Route path="dashboard/student" element={<DashBoardStudent />} />
                <Route path="dashboard/student/uploadCv" element={<UploadCvStudent />} />
                <Route path="dashboard/student/offers" element={<StudentInternshipOffersList />} />
                <Route path="dashboard/student/offers/:offerId" element={<OfferDetailPage />} />
                <Route path="dashboard/student/applications" element={<ApplicationsPage />} />
                <Route path="dashboard/student/ententes/:id/signer" element={<SignerEntentePage/>}/>
                {/* Gestionnaire Routes */}
                <Route path="dashboard/gestionnaire" element={<DashBoardGestionnaire />}>
                    <Route path="cv" element={<PendingCvPage />} />
                    <Route path="applications" element={<ApplicationsPage />} />
                </Route>
                <Route path="dashboard/gestionnaire/offers" element={<OffersPage />} />
                <Route path="dashboard/gestionnaire/ententes" element={<EntentesStagePage/>} />
                <Route path="dashboard/gestionnaire/ententes/:id/signer" element={<SignerEntentePage/>}/>
                <Route path="dashboard/gestionnaire/offers/:id" element={<OfferDetailsPage />} />
                <Route path="/dashboard/gestionnaire/ententes/create" element={<CreateEntenteForm />} />


                <Route path="/dashboard/prof" element={<DashboardProf />} />
                <Route path="/dashboard/prof/etudiants" element={<ProfStudentPage />} />
                <Route path="dashboard/prof/evaluations" element={<EvaluationsList/>}/>

                {/* Fallback Dashboard Route */}
                <Route path="dashboard" element={<h1>DashBoard</h1>} />
            </Route>
        )
    );

    return <RouterProvider router={router} />;
}

export default App;