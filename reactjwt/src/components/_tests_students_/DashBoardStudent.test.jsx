import {fireEvent, render, screen, waitFor} from "@testing-library/react";
import {beforeEach, describe, expect, test, vi} from "vitest";
import DashBoardStudent from "../Étudiant/DashBoardStudent.jsx";

vi.mock("react-i18next", () => ({
    useTranslation: () => ({
        t: (key, options) => {
            const translations = {
                "appName": "LeandrOSE",
                "dashboardStudent.welcome": "Bienvenue",
                "dashboardStudent.description": "Gérez vos stages et candidatures",
                "dashboardStudent.logout": "Déconnexion",
                "dashboardStudent.tabs.offers": "Offres de stage",
                "dashboardStudent.tabs.cv": "Mon CV",
                "dashboardStudent.tabs.applications": "Mes candidatures",
                "dashboardStudent.tabs.ententes": "Ententes",
                "dashboardStudent.tabs.contacts": "Mes contacts",
                "program.description": "Programme:",
                "terms.term": "Session",
                "terms.WINTER": "Hiver",
                "terms.SUMMER": "Été",
                "terms.FALL": "Automne",
                "dashboardStudent.reregistration.title": "Réinscription requise",
                "dashboardStudent.reregistration.message": "Votre session est expirée",
                "dashboardStudent.reregistration.program": "Sélectionnez votre programme",
                "dashboardStudent.reregistration.selectProgram": "Choisir un programme",
                "dashboardStudent.reregistration.confirm": "Me réinscrire maintenant",
                "dashboardStudent.reregistration.continueLater": "Continuer plus tard",
                "dashboardStudent.reregistration.updating": "Mise à jour...",
                "programs.COMPUTER_SCIENCE": "Informatique"
            };
            return options?.defaultValue || translations[key] || key;
        }
    })
}));

const mockNavigate = vi.fn();
vi.mock("react-router-dom", () => ({
    useNavigate: () => mockNavigate
}));

vi.mock("../Étudiant/StudentCvList.jsx", () => ({
    default: () => <div>Student CV List Component</div>
}));

vi.mock("../Étudiant/StudentInternshipOffersList.jsx", () => ({
    default: () => <div>Student Internship Offers List Component</div>
}));

vi.mock("../Étudiant/StudentApplicationsList.jsx", () => ({
    default: () => <div>Student Applications List Component</div>
}));

vi.mock("../Étudiant/StudentEntentesListe.jsx", () => ({
    default: () => <div>Student Ententes Liste Component</div>
}));

vi.mock("../Étudiant/InfosContactPage.jsx", () => ({
    default: () => <div>Infos Contact Page Component</div>
}));

vi.mock("../LanguageSelector.jsx", () => ({
    default: () => <div>Language Selector</div>
}));

vi.mock("../../api/apiStudent.jsx", () => ({
    updateStudentInfo: vi.fn(),
    getPublishedOffers: vi.fn()
}));

vi.mock("../../api/apiRegister.jsx", () => ({
    fetchPrograms: vi.fn()
}));

const mockStudentData = {
    firstName: "Alice",
    lastName: "Tremblay",
    email: "alice@example.com",
    program: {
        code: "420.B0",
        translationKey: "programs.COMPUTER_SCIENCE"
    },
    internshipTerm: "WINTER 2025",
    expired: false
};

const mockExpiredStudentData = {
    ...mockStudentData,
    expired: true
};

const mockPrograms = [
    {
        code: "420.B0",
        translationKey: "programs.COMPUTER_SCIENCE"
    }
];

describe("DashBoardStudent", () => {
    beforeEach(() => {
        vi.clearAllMocks();
        Storage.prototype.getItem = vi.fn(() => "fake-token");
        Storage.prototype.clear = vi.fn();
        global.fetch = vi.fn();
        delete window.location;
        window.location = {search: "", reload: vi.fn()};
    });

    test("redirige vers login si pas de token", async () => {
        Storage.prototype.getItem = vi.fn(() => null);
        render(<DashBoardStudent/>);
        await waitFor(() => {
            expect(mockNavigate).toHaveBeenCalledWith("/login");
        });
    });

    test("affiche le nom de l'étudiant", async () => {
        global.fetch.mockResolvedValueOnce({
            ok: true,
            json: async () => mockStudentData
        });
        render(<DashBoardStudent/>);
        await waitFor(() => {
            expect(screen.getByText(/Bienvenue.*Alice/)).toBeInTheDocument();
        });
    });

    test("affiche les informations du programme et session", async () => {
        global.fetch.mockResolvedValueOnce({
            ok: true,
            json: async () => mockStudentData
        });
        render(<DashBoardStudent/>);
        await waitFor(() => {
            expect(screen.getByText(/Informatique/)).toBeInTheDocument();
        });
        expect(screen.getByText(/Hiver 2025/)).toBeInTheDocument();
    });

    test("change de section avec les boutons", async () => {
        global.fetch.mockResolvedValueOnce({
            ok: true,
            json: async () => mockStudentData
        });
        render(<DashBoardStudent/>);
        await waitFor(() => {
            expect(screen.getByText("Bienvenue Alice!")).toBeInTheDocument();
        });
        const cvButton = screen.getByRole("button", {name: /Mon CV/i});
        fireEvent.click(cvButton);
        await waitFor(() => {
            expect(screen.getByText("Student CV List Component")).toBeInTheDocument();
        });
    });

    test("affiche toutes les sections disponibles", async () => {
        global.fetch.mockResolvedValueOnce({
            ok: true,
            json: async () => mockStudentData
        });
        render(<DashBoardStudent/>);
        await waitFor(() => {
            expect(screen.getByText("Offres de stage")).toBeInTheDocument();
        });
        expect(screen.getByText("Mon CV")).toBeInTheDocument();
        expect(screen.getByText("Mes candidatures")).toBeInTheDocument();
        expect(screen.getByText("Ententes")).toBeInTheDocument();
        expect(screen.getByText("Mes contacts")).toBeInTheDocument();
    });

    test("déconnecte l'utilisateur", async () => {
        global.fetch.mockResolvedValueOnce({
            ok: true,
            json: async () => mockStudentData
        });
        render(<DashBoardStudent/>);
        await waitFor(() => {
            expect(screen.getByText("Bienvenue Alice!")).toBeInTheDocument();
        });
        const logoutButton = screen.getByRole("button", {name: /Déconnexion/i});
        fireEvent.click(logoutButton);
        expect(Storage.prototype.clear).toHaveBeenCalled();
        expect(mockNavigate).toHaveBeenCalledWith("/login", {replace: true});
    });

    test("affiche le modal de réinscription si étudiant expiré", async () => {
        const {fetchPrograms} = await import("../../api/apiRegister.jsx");
        fetchPrograms.mockResolvedValue(mockPrograms);
        global.fetch.mockResolvedValueOnce({
            ok: true,
            json: async () => mockExpiredStudentData
        });
        render(<DashBoardStudent/>);
        await waitFor(() => {
            expect(screen.getByText("Réinscription requise")).toBeInTheDocument();
        });
        expect(screen.getByText("Votre session est expirée")).toBeInTheDocument();
    });

    test("permet de fermer le modal de réinscription", async () => {
        const {fetchPrograms} = await import("../../api/apiRegister.jsx");
        fetchPrograms.mockResolvedValue(mockPrograms);
        global.fetch.mockResolvedValueOnce({
            ok: true,
            json: async () => mockExpiredStudentData
        });
        render(<DashBoardStudent/>);
        await waitFor(() => {
            expect(screen.getByText("Réinscription requise")).toBeInTheDocument();
        });
        const continueLaterButton = screen.getByRole("button", {name: /Continuer plus tard/i});
        fireEvent.click(continueLaterButton);
        await waitFor(() => {
            expect(screen.queryByText("Réinscription requise")).not.toBeInTheDocument();
        });
    });

    test("gère la réinscription avec succès", async () => {
        const {fetchPrograms} = await import("../../api/apiRegister.jsx");
        const {updateStudentInfo} = await import("../../api/apiStudent.jsx");
        fetchPrograms.mockResolvedValue(mockPrograms);
        updateStudentInfo.mockResolvedValue(mockStudentData);
        global.fetch.mockResolvedValueOnce({
            ok: true,
            json: async () => mockExpiredStudentData
        });
        render(<DashBoardStudent/>);
        await waitFor(() => {
            expect(screen.getByText("Réinscription requise")).toBeInTheDocument();
        });
        const select = screen.getByRole("combobox");
        fireEvent.change(select, {target: {value: "programs.COMPUTER_SCIENCE"}});
        const confirmButton = screen.getByRole("button", {name: /Me réinscrire maintenant/i});
        fireEvent.click(confirmButton);
        await waitFor(() => {
            expect(updateStudentInfo).toHaveBeenCalledWith("programs.COMPUTER_SCIENCE");
        });
    });

    test("navigue vers la section spécifiée dans l'URL", async () => {
        window.location.search = "?tab=cv";
        global.fetch.mockResolvedValueOnce({
            ok: true,
            json: async () => mockStudentData
        });
        render(<DashBoardStudent/>);
        await waitFor(() => {
            expect(screen.getByText("Student CV List Component")).toBeInTheDocument();
        });
    });

    test("affiche les applications quand on clique sur le bouton", async () => {
        global.fetch.mockResolvedValueOnce({
            ok: true,
            json: async () => mockStudentData
        });
        render(<DashBoardStudent/>);
        await waitFor(() => {
            expect(screen.getByText("Bienvenue Alice!")).toBeInTheDocument();
        });
        const applicationsButton = screen.getByRole("button", {name: /Mes candidatures/i});
        fireEvent.click(applicationsButton);
        await waitFor(() => {
            expect(screen.getByText("Student Applications List Component")).toBeInTheDocument();
        });
    });

    test("affiche les ententes quand on clique sur le bouton", async () => {
        global.fetch.mockResolvedValueOnce({
            ok: true,
            json: async () => mockStudentData
        });
        render(<DashBoardStudent/>);
        await waitFor(() => {
            expect(screen.getByText("Bienvenue Alice!")).toBeInTheDocument();
        });
        const ententesButton = screen.getByRole("button", {name: /Ententes/i});
        fireEvent.click(ententesButton);
        await waitFor(() => {
            expect(screen.getByText("Student Ententes Liste Component")).toBeInTheDocument();
        });
    });

    test("affiche les contacts quand on clique sur le bouton", async () => {
        global.fetch.mockResolvedValueOnce({
            ok: true,
            json: async () => mockStudentData
        });
        render(<DashBoardStudent/>);
        await waitFor(() => {
            expect(screen.getByText("Bienvenue Alice!")).toBeInTheDocument();
        });
        const contactsButton = screen.getByRole("button", {name: /Mes contacts/i});
        fireEvent.click(contactsButton);
        await waitFor(() => {
            expect(screen.getByText("Infos Contact Page Component")).toBeInTheDocument();
        });
    });
});
