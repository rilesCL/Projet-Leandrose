import {render, screen} from '@testing-library/react';
import {MemoryRouter} from 'react-router-dom';
import {I18nextProvider, initReactI18next} from 'react-i18next';
import i18n from 'i18next';
import GestionnaireListeEntentes from '../GestionnaireListeEntentes.jsx';
import * as apiGestionnaire from '../../../api/apiGestionnaire.jsx';

vi.mock('../../../api/apiGestionnaire.jsx', () => ({
    fetchAgreements: vi.fn(),
    previewEntentePdf: vi.fn(),
    getAllProfs: vi.fn(),
    attribuerProf: vi.fn()
}));

const mockedApi = vi.mocked(apiGestionnaire);

const buildTestI18n = () => {
    const resources = {
        en: {
            translation: {
                ententeStage: {
                    loading: 'Loading agreements...',
                    title: 'Internship Agreements',
                    description: 'Manage and sign agreements',
                    status: {
                        title: 'Status',
                        validation: 'Validated',
                        waiting_other_signatures: 'Waiting for other signatures',
                        waiting_your_signature: 'Waiting for your signature',
                        draft: 'Draft'
                    },
                    created_at: 'Created at',
                    period: 'Period',
                    stage: 'Internship',
                    student: 'Student',
                    actions: {
                        title: 'Actions',
                        look: 'View',
                        sign: 'Sign',
                        assign_prof: 'Assign Prof'
                    }
                },
                studentEntentes: {
                    professor: 'Professor',
                    noProfessor: 'No professor assigned'
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

const renderComponent = () => {
    const i18nInstance = buildTestI18n();

    return render(
        <I18nextProvider i18n={i18nInstance}>
            <MemoryRouter>
                <GestionnaireListeEntentes/>
            </MemoryRouter>
        </I18nextProvider>
    );
};

describe('GestionnaireListeEntentes', () => {
    beforeEach(() => {
        vi.resetAllMocks();

        mockedApi.fetchAgreements.mockImplementation(async (setEntentes, setLoading) => {
            setEntentes([
                {
                    id: 1,
                    student: {firstName: 'Alice', lastName: 'Martin'},
                    internshipOffer: {description: 'Frontend Developer', employeurDto: {companyName: 'Tech Corp'}},
                    dateDebut: '2025-01-06',
                    duree: 12,
                    dateCreation: '2024-12-01',
                    statut: 'VALIDEE',
                    prof: {firstName: 'Jean', lastName: 'Dupont', department: 'Informatique'}
                }
            ]);

            if (typeof setLoading === 'function') {
                setLoading(false);
            }
        });
    });

    it('displays assigned professor in the agreements table', async () => {
        renderComponent();

        expect(await screen.findByText('Alice Martin')).toBeInTheDocument();
        expect(await screen.findByText('Jean Dupont')).toBeInTheDocument();
        expect(screen.getByText('Informatique')).toBeInTheDocument();
    });

    it('shows placeholder when no professor is assigned', async () => {
        mockedApi.fetchAgreements.mockImplementation(async (setEntentes, setLoading) => {
            setEntentes([
                {
                    id: 2,
                    student: {firstName: 'Marc', lastName: 'Leroy'},
                    internshipOffer: {description: 'Backend Developer', employeurDto: {companyName: 'Data Corp'}},
                    dateDebut: '2025-02-03',
                    duree: 8,
                    dateCreation: '2024-12-15',
                    statut: 'VALIDEE',
                    prof: null
                }
            ]);

            if (typeof setLoading === 'function') {
                setLoading(false);
            }
        });

        renderComponent();

        expect(await screen.findByText('Marc Leroy')).toBeInTheDocument();
        expect(await screen.findByText('No professor assigned')).toBeInTheDocument();
    });
});

