import {render, screen} from '@testing-library/react';
import userEvent from '@testing-library/user-event';
import {I18nextProvider, initReactI18next} from 'react-i18next';
import i18n from 'i18next';
import PdfViewer from '../PdfViewer.jsx';

vi.mock('react-pdf', () => ({
    Document: ({children}) => <div data-testid="document-mock">{children}</div>,
    Page: () => <div data-testid="page-mock"/>,
    pdfjs: {
        version: 'test',
        GlobalWorkerOptions: {workerSrc: ''}
    }
}));

const buildTestI18n = () => {
    const resources = {
        en: {
            translation: {
                previewPdf: {
                    preview: 'Preview',
                    next: 'Next',
                    previous: 'Previous',
                    close: 'Close',
                    download: 'Download PDF'
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

const renderPdfViewer = (props) => {
    const i18nInstance = buildTestI18n();
    return render(
        <I18nextProvider i18n={i18nInstance}>
            <div>
                <div data-testid="open-modal"/>
                {props.isOpen && <PdfViewer {...props} />}
            </div>
        </I18nextProvider>
    );
};

describe('PdfViewer', () => {
    it('affiche l’entête et le téléchargement', () => {
        renderPdfViewer({file: 'https://example.com/doc.pdf', onClose: vi.fn(), isOpen: true});

        expect(screen.getByText('Preview')).toBeInTheDocument();
        expect(screen.getByRole('link', {name: /Download PDF/})).toBeInTheDocument();
    });

    it('affiche les boutons de navigation', () => {
        renderPdfViewer({file: 'https://example.com/doc.pdf', onClose: vi.fn(), isOpen: true});

        expect(screen.getByRole('button', {name: /Next/})).toBeInTheDocument();
        expect(screen.getByRole('button', {name: /Previous/})).toBeInTheDocument();
    });

    it('ferme la modale via le bouton Fermer', async () => {
        const onClose = vi.fn();
        renderPdfViewer({file: 'https://example.com/doc.pdf', onClose, isOpen: true});

        const closeButton = screen.getByRole('button', {name: /Close/});
        await userEvent.click(closeButton);

        expect(onClose).toHaveBeenCalledTimes(1);
    });

    it('affiche les éléments principaux de la modale', () => {
        renderPdfViewer({file: 'https://example.com/doc.pdf', onClose: vi.fn(), isOpen: true});

        expect(screen.getByTestId('document-mock')).toBeInTheDocument();
        expect(screen.getByTestId('page-mock')).toBeInTheDocument();

        const downloadLink = screen.getByRole('link', {name: /Download PDF/});
        expect(downloadLink).toBeInTheDocument();
        expect(downloadLink).toHaveAttribute('href', 'https://example.com/doc.pdf');
    });
});

