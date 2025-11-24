import { render, screen } from '@testing-library/react';
import { MemoryRouter, Route, Routes } from 'react-router-dom';
import { I18nextProvider, initReactI18next } from 'react-i18next';
import i18n from 'i18next';
import EvaluationForm from '../EvaluationForm.jsx';
import * as apiProf from '../../../api/apiProf';
import userEvent from "@testing-library/user-event";

vi.mock('../../../api/apiProf', () => ({
    checkExistingEvaluation: vi.fn(),
    getEvaluationInfo: vi.fn(),
    createEvaluation: vi.fn(),
    generateEvaluationPdfWithId: vi.fn(),
}));

const mockedApi = vi.mocked(apiProf);

const buildTestI18n = () => {
    const resources = {
        en: {
            translation: {
                evaluation: {
                    title: 'Evaluation Form',
                    submittedSuccess: 'Evaluation submitted successfully!',
                    studentInfo: 'Student Information',
                    companyInfo: 'Company Information',
                    name: 'Name',
                    contact_person: 'Contact Person',
                    address: 'Address',
                    email: 'Email',
                    internStartDate: 'Internship Start Date',
                    loading: 'Loading evaluation form...',
                    errors: {
                        initializationError: 'Error loading form',
                        submit: 'Error submitting evaluation'
                    },
                    conformity: {
                        title: 'CONFORMITY',
                        description: 'Conformity with internship requirements',
                        q1: 'Question 1 about conformity',
                        q2: 'Question 2 about conformity',
                        q3: 'Question 3 about conformity'
                    },
                    environment: {
                        title: 'ENVIRONMENT',
                        description: 'Work environment assessment',
                        q1: 'Question 1 about environment',
                        q2: 'Question 2 about environment'
                    },
                    general: {
                        title: 'GENERAL',
                        description: 'General assessment',
                        q1: 'Question 1 general',
                        q2: 'Question 2 general',
                        q3: 'Question 3 general',
                        q4: 'Question 4 general',
                        q5: 'Question 5 general'
                    },
                    rating: {
                        totally_agree: 'Totally agree',
                        mostly_agree: 'Mostly agree',
                        mostly_disagree: 'Mostly disagree',
                        totally_disagree: 'Totally disagree'
                    },
                    placeholders: {
                        salary: 'Enter salary',
                        first_month: 'First month',
                        second_month: 'Second month',
                        third_month: 'Third month',
                        nbHoursWeek: 'Hours per week'
                    },
                    observations: {
                        title: 'General Observations',
                        q1: 'Preferred internship stage?',
                        q2: 'Capacity to host interns?',
                        q3: 'Same trainee next stage?',
                        q4: 'Variable work shifts?',
                        first_intern: 'First internship',
                        second_intern: 'Second internship',
                        stage1: '1 intern',
                        stage2: '2 interns',
                        stage3: '3 interns',
                        stage4: '4 interns',
                        yes: 'Yes',
                        no: 'No',
                        from: 'From',
                        to: 'To'
                    },
                    validation: {
                        missingRating: 'Rating is required',
                        selectOption: 'Please select an option',
                        hoursRequired: 'Hours are required',
                        salaryRequired: 'Salary is required',
                        missingFields: 'Please fill all fields'
                    },
                    submitEvaluation: 'Submit Evaluation',
                    submitting: 'Submitting...',
                    submitted: 'Submitted'
                }
            }
        }
    };

    const testI18n = i18n.createInstance();
    testI18n.use(initReactI18next).init({
        lng: 'en',
        fallbackLng: 'en',
        resources,
        interpolation: {
            escapeValue: false
        }
    });

    return testI18n;
};

const MockProfEvaluationForm = () => {
    const i18nInstance = buildTestI18n();
    return (
        <I18nextProvider i18n={i18nInstance}>
            <MemoryRouter initialEntries={['/prof/evaluation/123/456']}>
                <Routes>
                    <Route path="/prof/evaluation/:studentId/:offerId" element={<EvaluationForm />} />
                </Routes>
            </MemoryRouter>
        </I18nextProvider>
    );
};

describe('EvaluationForm (Prof)', () => {
    beforeEach(() => {
        vi.resetAllMocks();

        mockedApi.checkExistingEvaluation.mockResolvedValue({ exists: false });
        mockedApi.getEvaluationInfo.mockResolvedValue({
            entrepriseTeacherDto: {
                companyName: 'ABC Inc',
                contactName: 'John Doe',
                address: '123 Main St',
                email: 'contact@abc.com'
            },
            studentTeacherDto: {
                fullname: 'Student A',
                internshipStartDate: '2024-01-01'
            }
        });

        mockedApi.createEvaluation.mockResolvedValue({ id: 'eval-123' });
        mockedApi.generateEvaluationPdfWithId.mockResolvedValue({});
    });

    afterEach(() => {
        vi.restoreAllMocks();
    });

    it('renders company and student details after loading', async () => {
        render(<MockProfEvaluationForm />);

        expect(await screen.findByText('Evaluation Form')).toBeInTheDocument();

        expect(await screen.findByText('ABC Inc')).toBeInTheDocument();
        expect(await screen.findByText('John Doe')).toBeInTheDocument();
        expect(await screen.findByText('123 Main St')).toBeInTheDocument();

        expect(await screen.findByText('Student A')).toBeInTheDocument();

        expect(mockedApi.checkExistingEvaluation).toHaveBeenCalledWith('123', '456');
        expect(mockedApi.getEvaluationInfo).toHaveBeenCalledWith('123', '456');
    });

    it('renders all evaluation categories with questions', async () => {
        render(<MockProfEvaluationForm />);

        await screen.findByText('Evaluation Form');

        expect(await screen.findByText('CONFORMITY')).toBeInTheDocument();
        expect(await screen.findByText('ENVIRONMENT')).toBeInTheDocument();
        expect(await screen.findByText('GENERAL')).toBeInTheDocument();

        expect(await screen.findByText('Question 1 about conformity')).toBeInTheDocument();
        expect(await screen.findByText('Question 1 about environment')).toBeInTheDocument();
        expect(await screen.findByText('Question 1 general')).toBeInTheDocument();
    });

    it('shows rating options for questions', async () => {
        render(<MockProfEvaluationForm />);

        await screen.findByText('Question 1 about conformity');

        const totallyAgreeButtons = await screen.findAllByRole('radio', { name: /Totally agree/i });
        const mostlyAgreeButtons = await screen.findAllByRole('radio', { name: /Mostly agree/i });

        expect(totallyAgreeButtons.length).toBeGreaterThan(0);
        expect(mostlyAgreeButtons.length).toBeGreaterThan(0);
    });

    it('renders observations section with all options', async () => {
        render(<MockProfEvaluationForm />);

        await screen.findByText('General Observations');

        const labels = await screen.findAllByText('Same trainee next stage?')
        const yes = await screen.findAllByText('Yes')
        const no = await screen.findAllByText('No')

        expect(await screen.findByText('Preferred internship stage?')).toBeInTheDocument();
        expect(await screen.findByText('Capacity to host interns?')).toBeInTheDocument();
        expect(labels.length).toBeGreaterThan(0)

        expect(await screen.findByText('First internship')).toBeInTheDocument();
        expect(await screen.findByText('Second internship')).toBeInTheDocument();
        expect(await screen.findByText('1 intern')).toBeInTheDocument();

        expect(yes.length).toBeGreaterThan(0)
        expect(no.length).toBeGreaterThan(0)
    });

    it('renders hours input fields for conformity category', async () => {
        render(<MockProfEvaluationForm />);

        await screen.findByText('CONFORMITY');

        const hoursInputs = await screen.findAllByPlaceholderText(/month/i);
        expect(hoursInputs.length).toBe(3); // First, Second, Third month
    });

    it('renders salary input field for general category', async () => {
        render(<MockProfEvaluationForm />);

        await screen.findByText('GENERAL');

        expect(await screen.findByPlaceholderText('Enter salary')).toBeInTheDocument();
    });

    it('shows submit button', async () => {
        render(<MockProfEvaluationForm />);

        const submitButton = await screen.findByRole('button', { name: /Submit Evaluation/i });
        expect(submitButton).toBeInTheDocument();
        expect(submitButton).toBeEnabled();
    });

    it("shows validations errors when required fields are missing", async  () => {
        const user = userEvent.setup()
        render(<MockProfEvaluationForm />);

        const submitButton = await screen.findByRole('button', {name: /Submit Evaluation/i})
        await user.click(submitButton)

        const errorsRating = await screen.findAllByText(/Rating is required/i)
        const errorsOption = await screen.findAllByText(/Please select an option/i)


        expect(errorsRating).toHaveLength(10)
        expect(errorsOption).toHaveLength(4)

        expect(mockedApi.createEvaluation).not.toHaveBeenCalled()
        expect(mockedApi.generateEvaluationPdfWithId).not.toHaveBeenCalled()
    })
    it("validates hours fields when conformity questions are answered", async () => {
        const user = userEvent.setup()
        render(<MockProfEvaluationForm />);

        await screen.findByText("CONFORMITY")

        const ratings = await screen.findAllByRole('radio', {name: /Totally agree/i})


        await user.click(ratings[0])
        await user.click(ratings[1])
        await user.click(ratings[2])

        const submitButton = await screen.findByRole('button', {name: /Submit Evaluation/i})
        await user.click(submitButton)
        expect(await screen.findByText(/Hours are required/i)).toBeInTheDocument()

    })
    it("validates work shift ranges when YES is selected", async () => {
        const user = userEvent.setup()
        render(<MockProfEvaluationForm />);

        await screen.findByText("General Observations")

        const yesButtons = await screen.findAllByText("Yes")
        await user.click(yesButtons[1])

        const submitButton = await screen.findByRole('button', {name: /Submit Evaluation/i})
        await user.click(submitButton)

        const errors = await screen.findAllByText(/Please fill all fields/i)
        expect(errors.length).toBeGreaterThan(0)

    })
});