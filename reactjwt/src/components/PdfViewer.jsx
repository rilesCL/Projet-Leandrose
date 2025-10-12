import React, { useState, useEffect, useRef } from "react";
import { Document, Page, pdfjs } from "react-pdf";
import {useTranslation} from "react-i18next";
import "pdfjs-dist/web/pdf_viewer.css";

pdfjs.GlobalWorkerOptions.workerSrc = `//unpkg.com/pdfjs-dist@${pdfjs.version}/build/pdf.worker.min.mjs`;

export default function PdfViewer({ file, onClose }) {
    const [numPages, setNumPages] = useState(null);
    const [pageNumber, setPageNumber] = useState(1);
    const { t } = useTranslation();
    const [scale, setScale] = useState(1);
    const [pageSize, setPageSize] = useState(null);
    const containerRef = useRef(null);

    const onDocumentLoadSuccess = ({numPages}) => {
        setNumPages(numPages);
        setPageNumber(1);
    };

    const onPageLoadSuccess = (page) => {
        const viewport = page.getViewport({scale: 1});
        setPageSize({width: viewport.width, height: viewport.height});
    };

    useEffect(() => {
        const updateScale = () => {
            if (!containerRef.current || !pageSize) return;

            const padding = 8;
            const containerWidth = containerRef.current.offsetWidth - padding;
            const containerHeight = containerRef.current.offsetHeight - padding;

            const scaleX = containerWidth / pageSize.width;
            const scaleY = containerHeight / pageSize.height;

            setScale(Math.min(scaleX, scaleY));
        };

        updateScale();
        window.addEventListener("resize", updateScale);
        return () => window.removeEventListener("resize", updateScale);
    }, [pageSize]);

    const nextPage = () => setPageNumber((prev) => Math.min(prev + 1, numPages));
    const prevPage = () => setPageNumber((prev) => Math.max(prev - 1, 1));

    return (
        <div className="fixed inset-0 bg-black bg-opacity-60 flex items-center justify-center z-50 p-4">
            <div className="bg-white rounded-2xl shadow-xl w-full max-w-6xl h-[95vh] flex flex-col overflow-hidden">
                <div className="flex justify-between items-center px-4 py-1 border-b border-gray-200 flex-shrink-0">
                    <h3 className="text-base font-medium text-gray-900">{t("pdfViewer.preview")}</h3>
                    <button
                        onClick={onClose}
                        className="text-gray-400 hover:text-gray-600 text-xl leading-none"
                        aria-label={t("pdfViewer.close")}
                    >
                        ×
                    </button>
                </div>
                <div
                    ref={containerRef}
                    className="flex-1 flex justify-center items-center bg-gray-50 overflow-hidden p-2"
                >
                    <Document
                        file={file}
                        onLoadSuccess={onDocumentLoadSuccess}
                        loading={<p className="text-gray-600">{t("pdfViewer.loading")}</p>}
                        error={<p className="text-red-600">{t("pdfViewer.error")}</p>}
                    >
                        <Page
                            pageNumber={pageNumber}
                            renderTextLayer={false}
                            renderAnnotationLayer={false}
                            scale={scale}
                            onLoadSuccess={onPageLoadSuccess}
                            className="shadow-lg"
                        />
                    </Document>
                </div>

                {/* Pagination */}
                <div
                    className="flex items-center justify-between px-4 py-2 border-t border-gray-200 bg-white flex-shrink-0">
                    <button
                        onClick={prevPage}
                        disabled={pageNumber <= 1}
                        className={`px-3 py-1 rounded-md text-sm ${
                            pageNumber <= 1
                                ? "bg-gray-100 text-gray-400 cursor-not-allowed"
                                : "bg-indigo-100 text-indigo-700 hover:bg-indigo-200"
                        }`}
                    >
                        ← {t("pdfViewer.previous")}
                    </button>

                    <p className="text-sm text-gray-700">
                        Page {pageNumber} / {numPages || "?"}
                    </p>

                    <button
                        onClick={nextPage}
                        disabled={pageNumber >= numPages}
                        className={`px-3 py-1 rounded-md text-sm ${
                            pageNumber >= numPages
                                ? "bg-gray-100 text-gray-400 cursor-not-allowed"
                                : "bg-indigo-100 text-indigo-700 hover:bg-indigo-200"
                        }`}
                    >
                        t{("pdfViewer.next")} →
                    </button>
                </div>
            </div>
        </div>
    );
}
