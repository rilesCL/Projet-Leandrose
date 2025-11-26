import React, {useEffect, useState} from 'react';
import {FaMoon, FaSun} from 'react-icons/fa';

export default function ThemeToggle() {
    const [theme, setTheme] = useState(() => localStorage.getItem('theme') || 'light');

    useEffect(() => {
        if (theme === 'dark') {
            document.documentElement.classList.add('dark');
            document.body.style.backgroundColor = '';
            document.body.style.color = '';
        } else {
            document.documentElement.classList.remove('dark');
            document.body.style.backgroundColor = '';
            document.body.style.color = '';
        }
        localStorage.setItem('theme', theme);
    }, [theme]);

    const toggleTheme = () => {
        setTheme(theme === 'light' ? 'dark' : 'light');
    };

    return (
        <button
            onClick={toggleTheme}
            className="p-2 rounded-lg transition-colors hover:bg-gray-200 dark:hover:bg-gray-700"
            aria-label={theme === 'dark' ? 'Switch to light mode' : 'Switch to dark mode'}
        >
            {theme === 'dark' ? (
                <FaSun className="text-yellow-500 text-xl"/>
            ) : (
                <FaMoon className="text-gray-700 dark:text-gray-300 text-xl"/>
            )}
        </button>
    );
}