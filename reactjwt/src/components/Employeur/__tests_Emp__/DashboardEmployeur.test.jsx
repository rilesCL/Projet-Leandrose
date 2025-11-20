import { render, screen, fireEvent, waitFor } from '@testing-library/react';
import { MemoryRouter } from 'react-router-dom';
import { I18nextProvider, initReactI18next } from 'react-i18next';
import i18n from 'i18next';
import DashBoardEmployeur from '../DashBoardEmployeur.jsx';

vi.mock('../InternshipOffersList.jsx', () => ({
    __esModule: true,
    default: () => <div data-testid="internship-offers-list">Internship Offers List</div>
}));

vi.mock('../EmployeurListeStages.jsx', () => ({
    __esModule: true,
    default: () => <div data-testid="employeur-liste-stages">Employeur Liste Stages</div>
}));

vi.mock('../EvaluationList.jsx', () => ({
    __esModule: true,
    default: () => <div data-testid="evaluation-list">Evaluation List</div>
}));

vi.mock('../../LanguageSelector.jsx', () => ({
    __esModule: true,
    default: () => <div data-testid="language-selector">Language Selector</div>
}));

global.fetch = vi.fn();

const mockNavigate = vi.fn();

const buildTestI18n = () => {
    const resources = {
        en: {
            translation: {
                appName: 'Internship Platform',
                dashboardEmployeur: {
                    welcome: 'Welcome',
                    description: 'Manage your internship offers, agreements, and evaluations.',
                    logout: 'Logout',
                    navigation: {
                        mainNavigation: 'Main Navigation'
                    },
                    tabs: {
                        offers: 'Internship Offers',
                        ententes: 'Agreements',
                        evaluations: 'Evaluations'
                    }
                }
            }
        },
        fr: {
            translation: {
                appName: 'Plateforme de Stage',
                dashboardEmployeur: {
                    welcome: 'Bienvenue',
                    description: 'Gérez vos offres de stage, ententes et évaluations.',
                    logout: 'Déconnexion',
                    navigation: {
                        mainNavigation: 'Navigation principale'
                    },
                    tabs: {
                        offers: 'Offres de stage',
                        ententes: 'Ententes',
                        evaluations: 'Évaluations'
                    }
                }
            }
        }
    };

    const instance = i18n.createInstance();
    instance.use(initReactI18next).init({
        lng: 'en',
        fallbackLng: 'en',
        resources
    });

    return instance;
};

vi.mock('react-router-dom', async () => {
    const actual = await vi.importActual('react-router-dom');
    return {
        ...actual,
        useNavigate: () => mockNavigate
    };
});

const MockDashBoardEmployeur = ({ initialTab = '' } = {}) => {
    const i18nInstance = buildTestI18n();
    const searchParams = initialTab ? `?tab=${initialTab}` : '';

    return (
        <I18nextProvider i18n={i18nInstance}>
            <MemoryRouter initialEntries={[`/dashboard/employeur${searchParams}`]}>
                <DashBoardEmployeur />
            </MemoryRouter>
        </I18nextProvider>
    );
};

describe('DashBoardEmployeur', () => {
    beforeEach(() => {
        vi.resetAllMocks();
        sessionStorage.clear();
        localStorage.clear();

        global.fetch.mockResolvedValue({
            ok: true,
            json: async () => ({ firstName: 'Jean', lastName: 'Dupuis' })
        });

        sessionStorage.setItem('accessToken', 'fake-token-123');
    });

    afterEach(() => {
        vi.clearAllMocks();
    });

    describe('Initial Render and Authentication', () => {
        it('renders dashboard with user name when authenticated', async () => {
            render(<MockDashBoardEmployeur />);

            expect(await screen.findByText(/Welcome Jean!/)).toBeInTheDocument();
            expect(screen.getByText('Internship Platform')).toBeInTheDocument();
            expect(screen.getByText('Manage your internship offers, agreements, and evaluations.')).toBeInTheDocument();
        });

        it('redirects to login when no token is present', async () => {
            sessionStorage.clear();

            render(<MockDashBoardEmployeur />);

            await waitFor(() => {
                expect(mockNavigate).toHaveBeenCalledWith('/login');
            });
        });

        it('redirects to login when user fetch fails', async () => {
            global.fetch.mockResolvedValueOnce({
                ok: false,
                status: 401
            });

            render(<MockDashBoardEmployeur />);

            await waitFor(() => {
                expect(mockNavigate).toHaveBeenCalledWith('/login');
            });
        });

        it('calls user API with correct authorization header', async () => {
            render(<MockDashBoardEmployeur />);

            await waitFor(() => {
                expect(global.fetch).toHaveBeenCalledWith(
                    'http://localhost:8080/user/me',
                    expect.objectContaining({
                        headers: {
                            Authorization: 'Bearer fake-token-123'
                        }
                    })
                );
            });
        });
    });

    describe('Tab Navigation', () => {
        it('renders all three tabs', async () => {
            render(<MockDashBoardEmployeur />);

            await screen.findByText(/Welcome Jean!/);

            expect(screen.getByText('Internship Offers')).toBeInTheDocument();
            expect(screen.getByText('Agreements')).toBeInTheDocument();
            expect(screen.getByText('Evaluations')).toBeInTheDocument();
        });

        it('displays offers section by default', async () => {
            render(<MockDashBoardEmployeur />);

            await screen.findByText(/Welcome Jean!/);

            expect(screen.getByTestId('internship-offers-list')).toBeInTheDocument();
            expect(screen.queryByTestId('employeur-liste-stages')).not.toBeInTheDocument();
            expect(screen.queryByTestId('evaluation-list')).not.toBeInTheDocument();
        });

        it('switches to ententes section when clicked', async () => {
            render(<MockDashBoardEmployeur />);

            await screen.findByText(/Welcome Jean!/);

            const ententesTab = screen.getByText('Agreements');
            fireEvent.click(ententesTab);

            expect(screen.getByTestId('employeur-liste-stages')).toBeInTheDocument();
            expect(screen.queryByTestId('internship-offers-list')).not.toBeInTheDocument();
            expect(screen.queryByTestId('evaluation-list')).not.toBeInTheDocument();
        });

        it('switches to evaluations section when clicked', async () => {
            render(<MockDashBoardEmployeur />);

            await screen.findByText(/Welcome Jean!/);

            const evaluationsTab = screen.getByText('Evaluations');
            fireEvent.click(evaluationsTab);

            expect(screen.getByTestId('evaluation-list')).toBeInTheDocument();
            expect(screen.queryByTestId('internship-offers-list')).not.toBeInTheDocument();
            expect(screen.queryByTestId('employeur-liste-stages')).not.toBeInTheDocument();
        });

        it('applies active styling to selected tab', async () => {
            render(<MockDashBoardEmployeur />);

            await screen.findByText(/Welcome Jean!/);

            let offersTab = screen.getByText('Internship Offers').closest('button');
            let ententesTab = screen.getByText('Agreements').closest('button');

            expect(offersTab).toHaveClass('bg-gradient-to-r', 'from-indigo-600');
            expect(ententesTab).toHaveClass('bg-white');

            fireEvent.click(ententesTab);

            await waitFor(() => {
                offersTab = screen.getByText('Internship Offers').closest('button');
                ententesTab = screen.getByText('Agreements').closest('button');
                expect(ententesTab).toHaveClass('bg-gradient-to-r', 'from-indigo-600');
            });
            expect(offersTab).toHaveClass('bg-white');
        });
    });

    describe('URL Parameter Handling', () => {
        it('loads offers tab when tab parameter is "offers"', async () => {
            render(<MockDashBoardEmployeur initialTab="offers" />);

            await screen.findByText(/Welcome Jean!/);

            expect(screen.getByTestId('internship-offers-list')).toBeInTheDocument();
        });

        it('loads ententes tab when tab parameter is "ententes"', async () => {
            render(<MockDashBoardEmployeur initialTab="ententes" />);

            await screen.findByText(/Welcome Jean!/);

            expect(screen.getByTestId('employeur-liste-stages')).toBeInTheDocument();
        });

        it('loads evaluations tab when tab parameter is "evaluations"', async () => {
            render(<MockDashBoardEmployeur initialTab="evaluations" />);

            await screen.findByText(/Welcome Jean!/);

            expect(screen.getByTestId('evaluation-list')).toBeInTheDocument();
        });

        it('defaults to offers tab when invalid tab parameter is provided', async () => {
            render(<MockDashBoardEmployeur initialTab="invalid" />);

            await screen.findByText(/Welcome Jean!/);

            expect(screen.getByTestId('internship-offers-list')).toBeInTheDocument();
        });
    });

    describe('Logout Functionality', () => {
        it('clears storage and navigates to login on logout', async () => {
            render(<MockDashBoardEmployeur />);

            await screen.findByText(/Welcome Jean!/);

            const logoutButton = screen.getByText('Logout').closest('button');
            fireEvent.click(logoutButton);

            expect(sessionStorage.length).toBe(0);
            expect(localStorage.length).toBe(0);
            expect(mockNavigate).toHaveBeenCalledWith('/login', { replace: true });
        });
    });

    describe('Header Components', () => {
        it('renders language selector', async () => {
            render(<MockDashBoardEmployeur />);

            await screen.findByText(/Welcome Jean!/);

            expect(screen.getByTestId('language-selector')).toBeInTheDocument();
        });

        it('renders logout button with icon', async () => {
            render(<MockDashBoardEmployeur />);

            await screen.findByText(/Welcome Jean!/);

            const logoutButton = screen.getByText('Logout');
            expect(logoutButton).toBeInTheDocument();
            expect(logoutButton.closest('button')).toHaveClass('flex', 'items-center');
        });
    });

    describe('Error Handling', () => {
        it('handles fetch error gracefully', async () => {
            global.fetch.mockRejectedValueOnce(new Error('Network error'));

            const consoleSpy = vi.spyOn(console, 'error').mockImplementation(() => {});

            render(<MockDashBoardEmployeur />);

            await waitFor(() => {
                expect(mockNavigate).toHaveBeenCalledWith('/login');
            });

            consoleSpy.mockRestore();
        });
    });

    describe('Responsive Design', () => {
        it('renders with responsive classes', async () => {
            render(<MockDashBoardEmployeur />);

            await screen.findByText(/Welcome Jean!/);

            const mainContainer = screen.getByRole('main');
            expect(mainContainer).toHaveClass('py-10');

            const header = screen.getByRole('banner');
            expect(header).toHaveClass('bg-white', 'shadow');
        });
    });
});