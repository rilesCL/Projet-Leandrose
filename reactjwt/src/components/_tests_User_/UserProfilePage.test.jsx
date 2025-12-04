import {fireEvent, render, screen, waitFor} from "@testing-library/react";
import {beforeEach, describe, expect, test, vi} from "vitest";
import UserProfilePage from "../User/UserProfilePage.jsx";

vi.mock("react-i18next", () => ({
    useTranslation: () => ({t: (k) => k})
}));

const mockNavigate = vi.fn();
vi.mock("react-router-dom", () => ({
    useNavigate: () => mockNavigate
}));

const mockUser = {
    firstName: "John",
    lastName: "Doe",
    email: "john@example.com",
    phone: "1234567890",
    role: "STUDENT"
};

describe("UserProfilePage", () => {
    beforeEach(() => {
        vi.clearAllMocks();
        Storage.prototype.getItem = vi.fn(() => "fake-token");
        global.fetch = vi.fn();
    });

    function renderPage() {
        return render(<UserProfilePage/>);
    }

    test("affiche le titre", async () => {
        global.fetch.mockResolvedValueOnce({
            ok: true,
            json: async () => mockUser
        });

        renderPage();

        const title = await screen.findByText("Mon profil");
        expect(title).toBeInTheDocument();
    });

    test("affiche l'email", async () => {
        global.fetch.mockResolvedValueOnce({
            ok: true,
            json: async () => mockUser
        });

        renderPage();

        const emailInput = await screen.findByDisplayValue("john@example.com");
        expect(emailInput).toBeInTheDocument();
        expect(emailInput).toBeDisabled();
    });

    test("ouvre la section téléphone", async () => {
        global.fetch.mockResolvedValueOnce({
            ok: true,
            json: async () => mockUser
        });

        renderPage();

        await screen.findByText("Mon profil");

        const phoneButton = screen.getByRole("button", {name: /téléphone/i});
        fireEvent.click(phoneButton);

        await waitFor(() => {
            const phoneInput = screen.getByDisplayValue("1234567890");
            expect(phoneInput).toBeInTheDocument();
        });
    });

    test("soumet avec mot de passe actuel", async () => {
        global.fetch.mockResolvedValueOnce({
            ok: true,
            json: async () => mockUser
        });

        global.fetch.mockResolvedValueOnce({
            ok: true,
            json: async () => ({...mockUser, message: "updated"})
        });

        renderPage();

        await screen.findByText("Mon profil");

        const pwdInput = screen.getByPlaceholderText(
            "Entrez votre mot de passe actuel pour confirmer"
        );

        fireEvent.change(pwdInput, {target: {value: "abc123"}});

        const saveButton = screen.getByRole("button", {name: /sauvegarder/i});
        fireEvent.click(saveButton);

        await waitFor(() => {
            expect(global.fetch).toHaveBeenCalledTimes(2);
        });

        expect(global.fetch).toHaveBeenCalledWith(
            "http://localhost:8080/user/me",
            expect.objectContaining({
                method: "PUT",
                headers: expect.objectContaining({
                    "Content-Type": "application/json",
                    Authorization: "Bearer fake-token"
                }),
                body: expect.stringContaining("abc123")
            })
        );
    });

    test("affiche une erreur si le mot de passe actuel est manquant", async () => {
        global.fetch.mockResolvedValueOnce({
            ok: true,
            json: async () => mockUser
        });

        renderPage();

        await screen.findByText("Mon profil");

        const saveButton = screen.getByRole("button", {name: /sauvegarder/i});
        fireEvent.click(saveButton);

        await waitFor(() => {
            expect(screen.getByText("Vous devez entrer votre mot de passe actuel.")).toBeInTheDocument();
        });
    });

    test("ouvre et remplit la section prénom/nom", async () => {
        global.fetch.mockResolvedValueOnce({
            ok: true,
            json: async () => mockUser
        });

        renderPage();

        await screen.findByText("Mon profil");

        const nameButton = screen.getByRole("button", {name: /prénom \/ nom/i});
        fireEvent.click(nameButton);

        await waitFor(() => {
            const firstNameInput = screen.getByDisplayValue("John");
            const lastNameInput = screen.getByDisplayValue("Doe");
            expect(firstNameInput).toBeInTheDocument();
            expect(lastNameInput).toBeInTheDocument();
        });
    });
});