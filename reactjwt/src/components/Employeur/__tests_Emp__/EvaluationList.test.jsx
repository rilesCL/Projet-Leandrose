import { render, screen } from '@testing-library/react';
import { MemoryRouter } from 'react-router-dom';
import { I18nextProvider, initReactI18next } from 'react-i18next';
import i18n from 'i18next';
import EvaluationList from '../EvaluationList.jsx';
import * as apiEmployeur from '../../../api/apiEmployeur.jsx';

vi.mock('../../../api/apiEmployeur', () => ({
  getEligibleEvaluations: vi.fn(),
  checkExistingEvaluation: vi.fn(),
  previewEvaluationPdf: vi.fn()
}));

vi.mock('../../PdfViewer.jsx', () => ({
  __esModule: true,
  default: () => <div data-testid="pdf-viewer-mock" />
}));

const mockedApi = vi.mocked(apiEmployeur);

const buildTestI18n = () => {
  const resources = {
    en: {
      translation: {
        evaluationList: {
          title: 'List of Evaluations',
          subtitle: 'Review eligible agreements and jump into drafts or submitted evaluations.',
          no_evaluations: 'No evaluations available at this time.',
          create_evaluation: 'Create Evaluation',
          view_pdf: 'View PDF',
          evaluation: 'Evaluation',
          submitted: 'Submitted',
          draft_created: 'Draft Created',
          errors: {
            checking_evaluation: 'Error checking the evaluation for the student: ',
            fetching_evaluations: 'Error retrieving eligible agreements',
            pdf_loading: 'Error loading PDF'
          }
        },
        ententeStage: {
          back: 'Back to dashboard',
          student: 'Student'
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

const MockEvaluationList = () => {
  const i18nInstance = buildTestI18n();

  return (
    <I18nextProvider i18n={i18nInstance}>
      <MemoryRouter>
        <EvaluationList />
      </MemoryRouter>
    </I18nextProvider>
  );
};

describe('EvaluationList', () => {
  beforeEach(() => {
    vi.resetAllMocks();

    mockedApi.getEligibleEvaluations.mockResolvedValue([
      {
        id: 1,
        studentId: 101,
        offerId: 201,
        studentFirstName: 'Laura',
        studentLastName: 'Dupont',
        studentProgram: 'computer_science',
        internshipDescription: 'Full-stack intern',
        companyName: 'Innovatech'
      }
    ]);

    mockedApi.checkExistingEvaluation.mockResolvedValue({ exists: false });
  });

  it('renders list title and card data', async () => {
    render(<MockEvaluationList />);

    expect(await screen.findByText('List of Evaluations')).toBeInTheDocument();
    expect(await screen.findByText('Laura Dupont')).toBeInTheDocument();
    expect(screen.getByText('Full-stack intern')).toBeInTheDocument();
    expect(screen.getByText('Innovatech')).toBeInTheDocument();
    expect(screen.getByText('Create Evaluation')).toBeInTheDocument();
  });

  it('shows evaluation status badge when evaluation exists', async () => {
    mockedApi.checkExistingEvaluation.mockResolvedValue({
      exists: true,
      evaluation: {
        id: 999,
        submitted: true,
        dateEvaluation: '2025-01-10T00:00:00Z'
      }
    });

    render(<MockEvaluationList />);

    expect(await screen.findByText(/Evaluation Submitted/)).toBeInTheDocument();
    expect(screen.getByText('View PDF')).toBeInTheDocument();
  });

  it('shows empty state when no agreements are returned', async () => {
    mockedApi.getEligibleEvaluations.mockResolvedValueOnce([]);

    render(<MockEvaluationList />);

    expect(await screen.findByText('No evaluations available at this time.')).toBeInTheDocument();
  });
});

