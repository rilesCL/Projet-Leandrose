// LanguageSelector.jsx
import React from 'react';
import { useTranslation } from 'react-i18next';

const LanguageSelector = ({ className = "w-32" }) => {
    const { i18n } = useTranslation();

    const handleLanguageChange = (e) => {
        const newLang = e.target.value;
        i18n.changeLanguage(newLang);
        localStorage.setItem('i18nextLng', newLang);
    };

    return (
        <div className={className}>
            <select
                value={i18n.language}
                onChange={handleLanguageChange}
                className="block w-full bg-white border border-gray-300 text-gray-700 py-2 px-3 rounded shadow-sm focus:outline-none focus:ring-2 focus:ring-indigo-500 focus:border-indigo-500"
            >
                <option value="fr">Français</option>
                <option value="en">English</option>
            </select>
        </div>
    );
};

export default LanguageSelector;