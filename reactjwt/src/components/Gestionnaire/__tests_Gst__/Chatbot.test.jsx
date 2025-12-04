import {fireEvent, render, screen, waitFor} from '@testing-library/react';
import {I18nextProvider, initReactI18next} from 'react-i18next';
import i18n from 'i18next';
import Chatbot from '../Chatbot.jsx';
import {beforeEach, describe, expect, it, vi} from 'vitest';


vi.mock('lucide-react', () => ({
    Send: () => <svg data-testid="send-icon"/>,
    Trash2: () => <svg data-testid="trash-icon"/>,
    User: () => <svg data-testid="user-icon"/>
}));

const buildTestI18n = () => {
    const resources = {
        fr: {
            translation: {
                program: {
                    computer_science: 'Informatique',
                    software_engineering: 'Génie logiciel',
                    information_technology: 'Technologie de l\'information',
                    data_science: 'Science des données',
                    cyber_security: 'Cybersécurité',
                    artificial_intelligence: 'Intelligence artificielle',
                    electrical_engineering: 'Génie électrique',
                    mechanical_engineering: 'Génie mécanique',
                    civil_engineering: 'Génie civil',
                    chemical_engineering: 'Génie chimique',
                    biomedical_engineering: 'Génie biomédical',
                    business_administration: 'Administration des affaires',
                    accounting: 'Comptabilité',
                    finance: 'Finance',
                    economics: 'Économie',
                    marketing: 'Marketing',
                    management: 'Gestion',
                    psychology: 'Psychologie',
                    sociology: 'Sociologie',
                    political_science: 'Science politique',
                    international_relations: 'Relations internationales',
                    law: 'Droit',
                    education: 'Éducation',
                    literature: 'Littérature',
                    history: 'Histoire',
                    philosophy: 'Philosophie',
                    linguistics: 'Linguistique',
                    biology: 'Biologie',
                    chemistry: 'Chimie',
                    physics: 'Physique',
                    mathematics: 'Mathématiques',
                    statistics: 'Statistiques',
                    environmental_science: 'Science de l\'environnement',
                    medicine: 'Médecine',
                    nursing: 'Soins infirmiers',
                    pharmacy: 'Pharmacie',
                    dentistry: 'Dentisterie',
                    architecture: 'Architecture',
                    fine_arts: 'Beaux-arts',
                    music: 'Musique',
                    theater: 'Théâtre',
                    film_studies: 'Études cinématographiques',
                    communication: 'Communication',
                    journalism: 'Journalisme',
                    design: 'Design',
                    anthropology: 'Anthropologie',
                    geography: 'Géographie',
                    sports_science: 'Sciences du sport'
                }
            }
        }
    };

    const testI18n = i18n.createInstance();
    testI18n.use(initReactI18next).init({
        lng: 'fr',
        fallbackLng: 'fr',
        resources
    });

    return testI18n;
};

const MockChatbot = ({isOpen = true, onToggle = vi.fn()}) => {
    const i18nInstance = buildTestI18n();
    return (
        <I18nextProvider i18n={i18nInstance}>
            <Chatbot isOpen={isOpen} onToggle={onToggle}/>
        </I18nextProvider>
    );
};

describe('Chatbot', () => {
    beforeEach(() => {
        vi.resetAllMocks();


        const sessionStorageMock = {
            getItem: vi.fn((key) => {
                if (key === 'accessToken') return 'test-token';
                if (key === 'tokenType') return 'Bearer';
                return null;
            }),
            setItem: vi.fn(),
            removeItem: vi.fn(),
            clear: vi.fn()
        };
        global.sessionStorage = sessionStorageMock;


        global.fetch = vi.fn();


        Element.prototype.scrollIntoView = vi.fn();
    });

    it('ne rend rien quand isOpen est false', () => {
        const {container} = render(<MockChatbot isOpen={false}/>);
        expect(container.firstChild).toBeNull();
    });

    it('affiche le header avec le titre et l\'icône robot', () => {
        render(<MockChatbot/>);

        expect(screen.getByText('Assistant LeandrOSE')).toBeInTheDocument();
        expect(screen.getByText('Aide intelligente')).toBeInTheDocument();
        expect(screen.getAllByText('🤖').length).toBeGreaterThan(0);
    });

    it('affiche le message de bienvenue initial', () => {
        render(<MockChatbot/>);

        expect(screen.getByText(/Bonjour! Je suis votre assistant pour LeandrOSE/i)).toBeInTheDocument();
    });

    it('affiche les boutons de réinitialisation et de fermeture', () => {
        const onToggle = vi.fn();
        render(<MockChatbot onToggle={onToggle}/>);

        const trashButton = screen.getByTitle('Réinitialiser');
        const closeButton = screen.getByTitle('Fermer');

        expect(trashButton).toBeInTheDocument();
        expect(closeButton).toBeInTheDocument();
    });

    it('affiche les questions suggérées au démarrage', () => {
        render(<MockChatbot/>);

        expect(screen.getByText("Quelles sont les offres en attente d'approbation?")).toBeInTheDocument();
        expect(screen.getByText("Montre-moi les CV à approuver")).toBeInTheDocument();
        expect(screen.getByText("Liste les programmes disponibles")).toBeInTheDocument();
    });

    it('remplit l\'input quand on clique sur une question suggérée', () => {
        render(<MockChatbot/>);

        const questionButton = screen.getByText("Quelles sont les offres en attente d'approbation?");
        fireEvent.click(questionButton);

        const textarea = screen.getByPlaceholderText('Votre question...');
        expect(textarea.value).toBe("Quelles sont les offres en attente d'approbation?");
    });

    it('permet de saisir du texte dans l\'input', () => {
        render(<MockChatbot/>);

        const textarea = screen.getByPlaceholderText('Votre question...');
        fireEvent.change(textarea, {target: {value: 'Test question'}});

        expect(textarea.value).toBe('Test question');
    });

    it('envoie un message quand on clique sur le bouton envoyer', async () => {
        global.fetch.mockResolvedValueOnce({
            ok: true,
            json: async () => ({
                response: 'Réponse du chatbot',
                sessionId: 'test-session-123'
            })
        });

        render(<MockChatbot/>);

        const textarea = screen.getByPlaceholderText('Votre question...');
        fireEvent.change(textarea, {target: {value: 'Test question'}});

        const buttons = screen.getAllByRole('button');
        const sendButton = buttons.find(btn => btn.querySelector('[data-testid="send-icon"]'));
        fireEvent.click(sendButton);

        await waitFor(() => {
            expect(screen.getByText('Test question')).toBeInTheDocument();
        });

        await waitFor(() => {
            expect(screen.getByText('Réponse du chatbot')).toBeInTheDocument();
        });

        expect(global.fetch).toHaveBeenCalledWith(
            'http://localhost:8080/gestionnaire/chatclient',
            expect.objectContaining({
                method: 'POST',
                headers: expect.objectContaining({
                    'Content-Type': 'application/json',
                    'Authorization': 'Bearer test-token',
                    'X-Session-Id': expect.any(String)
                }),
                body: JSON.stringify({query: 'Test question'})
            })
        );
    });

    it('envoie un message avec la touche Entrée', async () => {
        global.fetch.mockResolvedValueOnce({
            ok: true,
            json: async () => ({
                response: 'Réponse du chatbot',
                sessionId: 'test-session-123'
            })
        });

        render(<MockChatbot/>);

        const textarea = screen.getByPlaceholderText('Votre question...');
        fireEvent.change(textarea, {target: {value: 'Test question'}});
        fireEvent.keyPress(textarea, {key: 'Enter', code: 'Enter', charCode: 13});

        await waitFor(() => {
            expect(screen.getByText('Test question')).toBeInTheDocument();
        });
    });

    it('n\'envoie pas de message avec Shift+Entrée', async () => {
        render(<MockChatbot/>);

        const textarea = screen.getByPlaceholderText('Votre question...');
        fireEvent.change(textarea, {target: {value: 'Test question'}});
        fireEvent.keyPress(textarea, {key: 'Enter', code: 'Enter', charCode: 13, shiftKey: true});

        await waitFor(() => {
            expect(global.fetch).not.toHaveBeenCalled();
        });
    });

    it('affiche un indicateur de chargement pendant l\'envoi', async () => {
        let resolveFetch;
        const fetchPromise = new Promise((resolve) => {
            resolveFetch = resolve;
        });

        global.fetch.mockReturnValueOnce(fetchPromise);

        render(<MockChatbot/>);

        const textarea = screen.getByPlaceholderText('Votre question...');
        fireEvent.change(textarea, {target: {value: 'Test question'}});

        const buttons = screen.getAllByRole('button');
        const sendButton = buttons.find(btn => btn.querySelector('[data-testid="send-icon"]'));
        fireEvent.click(sendButton);

        await waitFor(() => {
            const loadingDots = screen.getAllByRole('generic').filter(el =>
                el.className.includes('animate-bounce')
            );
            expect(loadingDots.length).toBeGreaterThan(0);
        });

        resolveFetch({
            ok: true,
            json: async () => ({
                response: 'Réponse du chatbot',
                sessionId: 'test-session-123'
            })
        });
    });

    it('nettoie les astérisques de la réponse', async () => {
        global.fetch.mockResolvedValueOnce({
            ok: true,
            json: async () => ({
                response: '* Item 1\n* Item 2\n* Item 3',
                sessionId: 'test-session-123'
            })
        });

        render(<MockChatbot/>);

        const textarea = screen.getByPlaceholderText('Votre question...');
        fireEvent.change(textarea, {target: {value: 'Test question'}});

        const buttons = screen.getAllByRole('button');
        const sendButton = buttons.find(btn => btn.querySelector('[data-testid="send-icon"]'));
        fireEvent.click(sendButton);

        await waitFor(() => {
            const response = screen.getByText(/Item 1/i);
            expect(response.textContent).not.toContain('*');
        });
    });

    it('remplace les clés de programmes par leurs traductions', async () => {
        global.fetch.mockResolvedValueOnce({
            ok: true,
            json: async () => ({
                response: 'Les programmes disponibles sont: COMPUTER_SCIENCE, SOFTWARE_ENGINEERING',
                sessionId: 'test-session-123'
            })
        });

        render(<MockChatbot/>);

        const textarea = screen.getByPlaceholderText('Votre question...');
        fireEvent.change(textarea, {target: {value: 'Test question'}});

        const buttons = screen.getAllByRole('button');
        const sendButton = buttons.find(btn => btn.querySelector('[data-testid="send-icon"]'));
        fireEvent.click(sendButton);

        await waitFor(() => {
            expect(screen.getByText(/Informatique/i)).toBeInTheDocument();
            expect(screen.getByText(/Génie logiciel/i)).toBeInTheDocument();
            expect(screen.queryByText(/COMPUTER_SCIENCE/i)).not.toBeInTheDocument();
            expect(screen.queryByText(/SOFTWARE_ENGINEERING/i)).not.toBeInTheDocument();
        });
    });

    it('affiche un message d\'erreur en cas d\'échec de l\'API', async () => {
        global.fetch.mockRejectedValueOnce(new Error('Erreur réseau'));

        render(<MockChatbot/>);

        const textarea = screen.getByPlaceholderText('Votre question...');
        fireEvent.change(textarea, {target: {value: 'Test question'}});

        const buttons = screen.getAllByRole('button');
        const sendButton = buttons.find(btn => btn.querySelector('[data-testid="send-icon"]'));
        fireEvent.click(sendButton);

        await waitFor(() => {
            expect(screen.getByText(/Désolé, une erreur s'est produite/i)).toBeInTheDocument();
        });
    });

    it('affiche un message d\'erreur pour une réponse 401', async () => {
        global.fetch.mockResolvedValueOnce({
            ok: false,
            status: 401,
            statusText: 'Unauthorized'
        });

        render(<MockChatbot/>);

        const textarea = screen.getByPlaceholderText('Votre question...');
        fireEvent.change(textarea, {target: {value: 'Test question'}});

        const buttons = screen.getAllByRole('button');
        const sendButton = buttons.find(btn => btn.querySelector('[data-testid="send-icon"]'));
        fireEvent.click(sendButton);

        await waitFor(() => {
            expect(screen.getByText(/Non authentifié/i)).toBeInTheDocument();
        });
    });

    it('affiche un message d\'erreur pour une réponse 403', async () => {
        global.fetch.mockResolvedValueOnce({
            ok: false,
            status: 403,
            statusText: 'Forbidden'
        });

        render(<MockChatbot/>);

        const textarea = screen.getByPlaceholderText('Votre question...');
        fireEvent.change(textarea, {target: {value: 'Test question'}});

        const buttons = screen.getAllByRole('button');
        const sendButton = buttons.find(btn => btn.querySelector('[data-testid="send-icon"]'));
        fireEvent.click(sendButton);

        await waitFor(() => {
            expect(screen.getByText(/Accès refusé/i)).toBeInTheDocument();
        });
    });

    it('désactive le bouton envoyer quand l\'input est vide', () => {
        render(<MockChatbot/>);

        const buttons = screen.getAllByRole('button');
        const sendButton = buttons.find(btn => btn.querySelector('[data-testid="send-icon"]'));
        expect(sendButton).toBeDisabled();
    });

    it('désactive le bouton envoyer pendant le chargement', async () => {
        let resolveFetch;
        const fetchPromise = new Promise((resolve) => {
            resolveFetch = resolve;
        });

        global.fetch.mockReturnValueOnce(fetchPromise);

        render(<MockChatbot/>);

        const textarea = screen.getByPlaceholderText('Votre question...');
        fireEvent.change(textarea, {target: {value: 'Test question'}});

        const buttons = screen.getAllByRole('button');
        const sendButton = buttons.find(btn => btn.querySelector('[data-testid="send-icon"]'));
        fireEvent.click(sendButton);

        await waitFor(() => {
            expect(sendButton).toBeDisabled();
        });

        resolveFetch({
            ok: true,
            json: async () => ({
                response: 'Réponse du chatbot',
                sessionId: 'test-session-123'
            })
        });
    });

    it('appelle onToggle quand on clique sur le bouton fermer', () => {
        const onToggle = vi.fn();
        render(<MockChatbot onToggle={onToggle}/>);

        const closeButton = screen.getByTitle('Fermer');
        fireEvent.click(closeButton);

        expect(onToggle).toHaveBeenCalledTimes(1);
    });

    it('réinitialise la conversation quand on clique sur le bouton poubelle', async () => {
        global.fetch.mockResolvedValueOnce({
            ok: true,
            json: async () => ({
                response: 'Réponse du chatbot',
                sessionId: 'test-session-123'
            })
        });


        global.fetch.mockResolvedValueOnce({
            ok: true
        });

        render(<MockChatbot/>);


        const textarea = screen.getByPlaceholderText('Votre question...');
        fireEvent.change(textarea, {target: {value: 'Test question'}});

        const buttons = screen.getAllByRole('button');
        const sendButton = buttons.find(btn => btn.querySelector('[data-testid="send-icon"]'));
        fireEvent.click(sendButton);

        await waitFor(() => {
            expect(screen.getByText('Test question')).toBeInTheDocument();
        });


        const trashButton = screen.getByTitle('Réinitialiser');
        fireEvent.click(trashButton);

        await waitFor(() => {
            expect(screen.getByText(/Conversation réinitialisée/i)).toBeInTheDocument();
        });

        expect(global.fetch).toHaveBeenCalledWith(
            expect.stringContaining('/gestionnaire/chatclient/session/'),
            expect.objectContaining({
                method: 'DELETE'
            })
        );
    });

    it('n\'affiche pas les questions suggérées après le premier message', async () => {
        global.fetch.mockResolvedValueOnce({
            ok: true,
            json: async () => ({
                response: 'Réponse du chatbot',
                sessionId: 'test-session-123'
            })
        });

        render(<MockChatbot/>);


        expect(screen.getByText("Quelles sont les offres en attente d'approbation?")).toBeInTheDocument();


        const textarea = screen.getByPlaceholderText('Votre question...');
        fireEvent.change(textarea, {target: {value: 'Test question'}});

        const buttons = screen.getAllByRole('button');
        const sendButton = buttons.find(btn => btn.querySelector('[data-testid="send-icon"]'));
        fireEvent.click(sendButton);

        await waitFor(() => {
            expect(screen.queryByText("Quelles sont les offres en attente d'approbation?")).not.toBeInTheDocument();
        });
    });
});

