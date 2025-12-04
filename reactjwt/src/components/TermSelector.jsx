import React, {useEffect, useRef, useState} from 'react';
import {useTranslation} from 'react-i18next';
import {FaChevronDown} from 'react-icons/fa';

export default function TermSelector({onTermChange}) {
    const {t} = useTranslation();
    const [isOpen, setIsOpen] = useState(false);
    const [selectedTerm, setSelectedTerm] = useState(null);
    const [availableTerms, setAvailableTerms] = useState([]);
    const dropdownRef = useRef(null);


    useEffect(() => {
        const handleClickOutside = (event) => {
            if (dropdownRef.current && !dropdownRef.current.contains(event.target)) {
                setIsOpen(false);
            }
        };

        document.addEventListener('mousedown', handleClickOutside);
        return () => document.removeEventListener('mousedown', handleClickOutside);
    }, []);


    useEffect(() => {
        const currentTerm = getCurrentTerm();
        const terms = [
            getPreviousTerm(getPreviousTerm(currentTerm)),
            getPreviousTerm(currentTerm),
            currentTerm,
            getNextTerm(currentTerm),
            getNextTerm(getNextTerm(currentTerm))
        ];

        setAvailableTerms(terms);
        setSelectedTerm(currentTerm);


        if (onTermChange) {
            onTermChange(currentTerm);
        }
    }, []);

    const getCurrentTerm = () => {
        const now = new Date();
        const currentYear = now.getFullYear();
        const currentMonth = now.getMonth() + 1;

        let season;
        if (currentMonth <= 5) {
            season = 'WINTER';
        } else if (currentMonth <= 8) {
            season = 'SUMMER';
        } else {
            season = 'FALL';
        }

        return {season, year: currentYear};
    };

    const getNextTerm = (term) => {
        const seasonOrder = {WINTER: 0, SUMMER: 1, FALL: 2};
        const seasons = ['WINTER', 'SUMMER', 'FALL'];

        const currentIndex = seasonOrder[term.season];
        const nextIndex = (currentIndex + 1) % 3;
        const nextYear = nextIndex === 0 ? term.year + 1 : term.year;

        return {season: seasons[nextIndex], year: nextYear};
    };

    const getPreviousTerm = (term) => {
        const seasonOrder = {WINTER: 0, SUMMER: 1, FALL: 2};
        const seasons = ['WINTER', 'SUMMER', 'FALL'];

        const currentIndex = seasonOrder[term.season];
        const prevIndex = (currentIndex - 1 + 3) % 3;
        const prevYear = currentIndex === 0 ? term.year - 1 : term.year;

        return {season: seasons[prevIndex], year: prevYear};
    };

    const formatTerm = (term) => {
        if (!term) return '';
        const seasonKey = `terms.${term.season}`;
        const translatedSeason = t(seasonKey);
        return `${translatedSeason} ${term.year}`;
    };

    const handleTermSelect = (term) => {
        setSelectedTerm(term);
        setIsOpen(false);
        if (onTermChange) {
            onTermChange(term);
        }
    };

    const isCurrentTerm = (term) => {
        const current = getCurrentTerm();
        return term.season === current.season && term.year === current.year;
    };

    return (
        <div className="relative" ref={dropdownRef}>
            <button
                onClick={() => setIsOpen(!isOpen)}
                className="flex items-center gap-1 sm:gap-2 px-2 sm:px-4 py-1.5 sm:py-2 bg-white border border-gray-300 rounded-lg hover:bg-gray-50 transition-colors focus:outline-none focus:ring-2 focus:ring-indigo-500 text-xs sm:text-sm"
                aria-haspopup="true"
                aria-expanded={isOpen}
            >
                <span className="font-medium text-gray-700 whitespace-nowrap">
                    {selectedTerm ? formatTerm(selectedTerm) : t('termSelector.selectTerm')}
                </span>
                <FaChevronDown
                    className={`text-gray-500 text-xs transition-transform flex-shrink-0 ${isOpen ? 'rotate-180' : ''}`}
                />
            </button>

            {isOpen && (
                <div
                    className="absolute right-0 mt-2 w-40 sm:w-48 bg-white border border-gray-200 rounded-lg shadow-lg z-50">
                    <div className="py-1">
                        {availableTerms.map((term, index) => (
                            <button
                                key={`${term.season}-${term.year}`}
                                onClick={() => handleTermSelect(term)}
                                className={`w-full text-left px-3 sm:px-4 py-2 text-xs sm:text-sm hover:bg-indigo-50 transition-colors flex items-center justify-between ${
                                    selectedTerm?.season === term.season && selectedTerm?.year === term.year
                                        ? 'bg-indigo-50 text-indigo-700 font-medium'
                                        : 'text-gray-700'
                                }`}
                            >
                                <span className="truncate">{formatTerm(term)}</span>
                                {isCurrentTerm(term) && (
                                    <span
                                        className="text-xs bg-indigo-100 text-indigo-700 px-1.5 sm:px-2 py-0.5 rounded-full ml-1 flex-shrink-0">
                                        {t('termSelector.current')}
                                    </span>
                                )}
                            </button>
                        ))}
                    </div>
                </div>
            )}
        </div>
    );
}