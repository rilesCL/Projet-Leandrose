import {render, screen, waitFor} from "@testing-library/react";
import {beforeEach, describe, expect, test, vi} from "vitest";
import StudentContactsPage from "../Étudiant/InfosContactPage.jsx";

vi.mock("react-i18next", () => ({
    useTranslation: () => ({
        t: (key, options) => {
            const translations = {
                "dashboardStudent.contacts.title": "Mes contacts",
                "dashboardStudent.contacts.subtitle": "Retrouvez tous vos contacts",
                "dashboardStudent.contacts.loading": "Chargement...",
                "dashboardStudent.contacts.noContact": "Aucun contact disponible",
                "dashboardStudent.contacts.supportTeam": "Équipe de soutien",
                "dashboardStudent.contacts.professor": "Professeur",
                "dashboardStudent.contacts.gestionnaire": "Gestionnaire",
                "dashboardStudent.contacts.employeurs": "Employeurs",
                "dashboardStudent.contacts.employeur": "Employeur",
                "dashboardStudent.contacts.noEmployeur": "Aucun employeur disponible",
                "dashboardStudent.contacts.department": "Département",
                "dashboardStudent.contacts.infoReadonlyTitle": "Information:",
                "dashboardStudent.contacts.infoReadonly": "Ces informations sont en lecture seule."
            };
            return options?.defaultValue || translations[key] || key;
        }
    })
}));

const mockProfessor = {
    id: 1,
    firstName: "Marie",
    lastName: "Dupont",
    email: "marie.dupont@college.ca",
    phoneNumber: "514-123-4567",
    department: "Informatique"
};

const mockGestionnaire = {
    id: 2,
    firstName: "Jean",
    lastname: "Martin",
    email: "jean.martin@college.ca",
    phoneNumber: "514-987-6543"
};

const mockEmployeurs = [
    {
        id: 3,
        firstName: "Sophie",
        lastName: "Tremblay",
        email: "sophie@entreprise.com",
        phoneNumber: "438-555-1234",
        companyName: "Tech Corp",
        field: "Développement web"
    },
    {
        id: 4,
        firstName: "Marc",
        lastName: "Leblanc",
        email: "marc@startup.com",
        phoneNumber: "514-555-9876",
        companyName: "Startup Inc",
        field: "Intelligence artificielle"
    }
];

describe("StudentContactsPage", () => {
    beforeEach(() => {
        vi.clearAllMocks();
        Storage.prototype.getItem = vi.fn(() => "fake-token");
        global.fetch = vi.fn();
    });

    test("affiche le loader pendant le chargement", () => {
        global.fetch.mockImplementation(() => new Promise(() => {
        }));

        render(<StudentContactsPage/>);

        expect(screen.getByText("Chargement...")).toBeInTheDocument();
    });

    test("affiche tous les contacts avec succès", async () => {
        global.fetch
            .mockResolvedValueOnce({
                ok: true,
                json: async () => mockProfessor
            })
            .mockResolvedValueOnce({
                ok: true,
                json: async () => mockGestionnaire
            })
            .mockResolvedValueOnce({
                ok: true,
                json: async () => mockEmployeurs
            });

        render(<StudentContactsPage/>);

        await waitFor(() => {
            expect(screen.getByText("Mes contacts")).toBeInTheDocument();
        });

        expect(screen.getByText("Marie Dupont")).toBeInTheDocument();
        expect(screen.getByText("Jean Martin")).toBeInTheDocument();
        expect(screen.getByText("Sophie Tremblay")).toBeInTheDocument();
        expect(screen.getByText("Marc Leblanc")).toBeInTheDocument();
    });

    test("affiche les emails et téléphones", async () => {
        global.fetch
            .mockResolvedValueOnce({
                ok: true,
                json: async () => mockProfessor
            })
            .mockResolvedValueOnce({
                ok: true,
                json: async () => mockGestionnaire
            })
            .mockResolvedValueOnce({
                ok: true,
                json: async () => []
            });

        render(<StudentContactsPage/>);

        await waitFor(() => {
            expect(screen.getByText("marie.dupont@college.ca")).toBeInTheDocument();
        });

        expect(screen.getByText("514-123-4567")).toBeInTheDocument();
        expect(screen.getByText("jean.martin@college.ca")).toBeInTheDocument();
    });

    test("affiche le message quand il n'y a pas de professeur", async () => {
        global.fetch
            .mockResolvedValueOnce({
                ok: false
            })
            .mockResolvedValueOnce({
                ok: true,
                json: async () => mockGestionnaire
            })
            .mockResolvedValueOnce({
                ok: true,
                json: async () => []
            });

        render(<StudentContactsPage/>);

        await waitFor(() => {
            const noContactMessages = screen.getAllByText("Aucun contact disponible");
            expect(noContactMessages.length).toBeGreaterThan(0);
        });
    });

    test("affiche le message quand il n'y a pas d'employeurs", async () => {
        global.fetch
            .mockResolvedValueOnce({
                ok: true,
                json: async () => mockProfessor
            })
            .mockResolvedValueOnce({
                ok: true,
                json: async () => mockGestionnaire
            })
            .mockResolvedValueOnce({
                ok: true,
                json: async () => []
            });

        render(<StudentContactsPage/>);

        await waitFor(() => {
            expect(screen.getByText("Aucun employeur disponible")).toBeInTheDocument();
        });
    });

    test("affiche les informations de l'entreprise pour les employeurs", async () => {
        global.fetch
            .mockResolvedValueOnce({
                ok: true,
                json: async () => mockProfessor
            })
            .mockResolvedValueOnce({
                ok: true,
                json: async () => mockGestionnaire
            })
            .mockResolvedValueOnce({
                ok: true,
                json: async () => mockEmployeurs
            });

        render(<StudentContactsPage/>);

        await waitFor(() => {
            expect(screen.getByText("Tech Corp")).toBeInTheDocument();
        });

        expect(screen.getByText("Startup Inc")).toBeInTheDocument();
        expect(screen.getByText("Développement web")).toBeInTheDocument();
        expect(screen.getByText("Intelligence artificielle")).toBeInTheDocument();
    });

    test("affiche le département du professeur", async () => {
        global.fetch
            .mockResolvedValueOnce({
                ok: true,
                json: async () => mockProfessor
            })
            .mockResolvedValueOnce({
                ok: false
            })
            .mockResolvedValueOnce({
                ok: true,
                json: async () => []
            });

        render(<StudentContactsPage/>);

        await waitFor(() => {
            expect(screen.getByText(/Département.*Informatique/)).toBeInTheDocument();
        });
    });

    test("affiche le nombre d'employeurs", async () => {
        global.fetch
            .mockResolvedValueOnce({
                ok: true,
                json: async () => mockProfessor
            })
            .mockResolvedValueOnce({
                ok: true,
                json: async () => mockGestionnaire
            })
            .mockResolvedValueOnce({
                ok: true,
                json: async () => mockEmployeurs
            });

        render(<StudentContactsPage/>);

        await waitFor(() => {
            expect(screen.getByText(/Employeurs.*\(2\)/)).toBeInTheDocument();
        });
    });

    test("gère les erreurs de fetch gracieusement", async () => {
        global.fetch.mockRejectedValue(new Error("Network error"));

        render(<StudentContactsPage/>);

        await waitFor(() => {
            expect(screen.getByText("Mes contacts")).toBeInTheDocument();
        });

        const noContactMessages = screen.getAllByText("Aucun contact disponible");
        expect(noContactMessages.length).toBeGreaterThan(0);
    });

    test("affiche le message d'information en lecture seule", async () => {
        global.fetch
            .mockResolvedValueOnce({
                ok: true,
                json: async () => mockProfessor
            })
            .mockResolvedValueOnce({
                ok: true,
                json: async () => mockGestionnaire
            })
            .mockResolvedValueOnce({
                ok: true,
                json: async () => []
            });

        render(<StudentContactsPage/>);

        await waitFor(() => {
            expect(screen.getByText("Ces informations sont en lecture seule.")).toBeInTheDocument();
        });
    });
});