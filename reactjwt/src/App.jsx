
import './App.css'
import {
    RouterProvider,
    createBrowserRouter,
    createRoutesFromElements,
    Route
} from 'react-router';
import RouteLayout from "./components/RouteLayout.jsx";
import RegisterEmployeurForm from "./components/RegisterEmployeurForm.jsx";
import RegistrationForm from "./components/RegistrationForm.jsx";

function App() {
    const router = createBrowserRouter(
        createRoutesFromElements(
            <Route path="/" element={<RouteLayout />}>
                <Route index element={<RegistrationForm />} />

            </Route>
        )
    );

    return <RouterProvider router={router} />;
}

export default App;