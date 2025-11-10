import { render, screen } from '@testing-library/react';
import { MemoryRouter, Route, Routes } from 'react-router-dom';
import { I18nextProvider, initReactI18next } from 'react-i18next';
import i18n from 'i18next';
import EvaluationForm from '../EvaluationForm.jsx';
import * as apiEmployeur from '../../../api/apiEmployeur';

vi.mock('../../../api/apiEmployeur', () => ({
  checkExistingEvaluation: vi.fn(),
  getEvaluationInfo: vi.fn(),
  createEvaluation: vi.fn(),
  generateEvaluationPdfWithId: vi.fn(),
  previewEvaluationPdf: vi.fn()
}));

const mockedApi = vi.mocked(apiEmployeur);

vi.mock('../../PdfViewer.jsx', () => ({
  __esModule: true,
  default: () => <div data-testid="pdf-viewer-placeholder" />
}));

const buildTestI18n = () => {
  const resources = {
    en: {
      translation: {
        evaluation: {
          title: 'Intern Evaluation Form',
          submittedSuccess: 'Evaluation submitted successfully!',
          studentInfo: 'Student Information',
          studentName: 'Student Name',
          program: 'Program',
          companyInfo: 'Company Information',
          company: 'Company',
          internship: 'Internship Position',
          evaluationDate: 'Evaluation Date',
          instructions: 'Please review the intern performance below.',
          generalComments: 'General Comments',
          generalCommentsPlaceholder: 'Add your overall feedback here...',
          backToEvaluationsList: 'Back to Evaluations',
          loading: 'Loading evaluation form...',
          productivitiy: {},
          productivity: {
            title: 'PRODUCTIVITY',
            description: 'Ability to optimize work performance',
            q1: 'Plan and organize work effectively'
          },
          quality: {
            title: 'WORK QUALITY',
            description: 'Ability to produce careful and precise work',
            q1: 'Demonstrate rigor in assigned tasks'
          },
          relationships: {
            title: 'INTERPERSONAL RELATIONSHIPS',
            description: 'Ability to establish harmonious interactions',
            q1: 'Builds rapport with teammates'
          },
          skills: {
            title: 'PERSONAL SKILLS',
            description: 'Ability to demonstrate mature behaviour',
            q1: 'Shows motivation at work'
          },
          rating: {
            totally_agree: 'Totally agree',
            mostly_agree: 'Mostly agree',
            mostly_disagree: 'Mostly disagree',
            totally_disagree: 'Totally disagree'
          },
          globalAssessment: {
            title: 'Overall assessment of the intern',
            specify: 'Specify your rating',
            discussed: 'This evaluation was discussed with the intern',
            yes: 'Yes',
            no: 'No',
            maybe: 'Maybe',
            welcome_maybe: 'Maybe',
            supervision_hours: 'Weekly supervision hours',
            welcome_nextInternship: 'Would you welcome this intern again?',
            technical_training: 'Was the technical training sufficient?',
            option0: 'The skills demonstrated far exceeded expectations',
            option1: 'The skills demonstrated exceeded expectations',
            option2: 'The skills demonstrated fully meet expectations',
            option3: 'The skills demonstrated partially meet expectations',
            option4: 'The skills demonstrated do not meet expectations',
            yesOption: 'Yes',
            noOption: 'No'
          },
          commentsPlaceholder: 'Additional comments'
        },
        program: {
          computer_science: 'Computer Science'
        }
      }
    }
  };

  const testI18n = i18n.createInstance();
  testI18n.use(initReactI18next).init({
    lng: 'en',
    fallbackLng: 'en',
    resources
  });

  return testI18n;
};

const MockEvaluationForm = () => {
  const i18nInstance = buildTestI18n();
  return (
    <I18nextProvider i18n={i18nInstance}>
      <MemoryRouter initialEntries={['/dashboard/employeur/evaluation/123/456']}>
        <Routes>
          <Route path="/dashboard/employeur/evaluation/:studentId/:offerId" element={<EvaluationForm />} />
        </Routes>
      </MemoryRouter>
    </I18nextProvider>
  );
};

describe('EvaluationForm', () => {
  beforeEach(() => {
    vi.resetAllMocks();
    vi.spyOn(window, 'alert').mockImplementation(() => {});

    mockedApi.checkExistingEvaluation.mockResolvedValue({ exists: false });
    mockedApi.getEvaluationInfo.mockResolvedValue({
      studentInfo: {
        firstName: 'John',
        lastName: 'Doe',
        program: 'computer_science'
      },
      internshipInfo: {
        description: 'Software Developer Intern',
        companyName: 'Tech Corp'
      }
    });

    mockedApi.createEvaluation.mockResolvedValue({ evaluationId: 'abc-123' });
    mockedApi.generateEvaluationPdfWithId.mockResolvedValue({});
    mockedApi.previewEvaluationPdf.mockResolvedValue(new Blob());
  });

  beforeAll(() => {
    console.log('RUNNING ONLY ONCE BEFORE ALL TEST');
  });

  afterEach(() => {
    vi.restoreAllMocks();
    console.log('RUNNING AFTER EACH TEST');
  });

  afterAll(() => {
    console.log('RUNNING ONLY ONCE AFTER ALL TEST');
  });

  it('renders student details after loading data', async () => {
    render(<MockEvaluationForm />);

    expect(await screen.findByText('Intern Evaluation Form')).toBeInTheDocument();
    expect(await screen.findByText('John Doe')).toBeInTheDocument();
    expect(mockedApi.checkExistingEvaluation).toHaveBeenCalledWith('123', '456');
    expect(mockedApi.getEvaluationInfo).toHaveBeenCalledWith('123', '456');
  });

  it('shows rating options for the first question', async () => {
    render(<MockEvaluationForm />);

    await screen.findByText('Plan and organize work effectively');

    const radioTotallyAgree = await screen.findAllByRole('radio', { name: /Totally agree/i });
    const radioMostlyDisagree = await screen.findAllByRole('radio', { name: /Mostly disagree/i });

    expect(radioTotallyAgree.length).toBeGreaterThan(0);
    expect(radioMostlyDisagree.length).toBeGreaterThan(0);
  });
});

