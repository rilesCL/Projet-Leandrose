import React, { useState, useRef, useEffect } from 'react';
import { Send, Trash2, User } from 'lucide-react';
import { useTranslation } from 'react-i18next';
import { sendChatMessage, clearChatSession } from '../../api/apiGestionnaire.jsx';

function getAuthHeaders(contentType = "application/json") {
    const accessToken = sessionStorage.getItem("accessToken");
    const tokenType = (sessionStorage.getItem("tokenType") || "BEARER").toUpperCase();
    const headers = {};
    if (contentType) headers["Content-Type"] = contentType;
    if (!accessToken) return headers;
    headers["Authorization"] = tokenType.startsWith("BEARER") ? `Bearer ${accessToken}` : accessToken;
    return headers;
}

export default function Chatbot({ isOpen = false, onToggle }) {
    const { t } = useTranslation();
    const [messages, setMessages] = useState([]);
    const [input, setInput] = useState('');
    const [loading, setLoading] = useState(false);
    const [sessionId, setSessionId] = useState(null);
    const messagesEndRef = useRef(null);

    // Mapping des clés de programmes vers leurs traductions
    const programMapping = {
        'COMPUTER_SCIENCE': t('program.computer_science'),
        'SOFTWARE_ENGINEERING': t('program.software_engineering'),
        'INFORMATION_TECHNOLOGY': t('program.information_technology'),
        'DATA_SCIENCE': t('program.data_science'),
        'CYBER_SECURITY': t('program.cyber_security'),
        'ARTIFICIAL_INTELLIGENCE': t('program.artificial_intelligence'),
        'ELECTRICAL_ENGINEERING': t('program.electrical_engineering'),
        'MECHANICAL_ENGINEERING': t('program.mechanical_engineering'),
        'CIVIL_ENGINEERING': t('program.civil_engineering'),
        'CHEMICAL_ENGINEERING': t('program.chemical_engineering'),
        'BIOMEDICAL_ENGINEERING': t('program.biomedical_engineering'),
        'BUSINESS_ADMINISTRATION': t('program.business_administration'),
        'ACCOUNTING': t('program.accounting'),
        'FINANCE': t('program.finance'),
        'ECONOMICS': t('program.economics'),
        'MARKETING': t('program.marketing'),
        'MANAGEMENT': t('program.management'),
        'PSYCHOLOGY': t('program.psychology'),
        'SOCIOLOGY': t('program.sociology'),
        'POLITICAL_SCIENCE': t('program.political_science'),
        'INTERNATIONAL_RELATIONS': t('program.international_relations'),
        'LAW': t('program.law'),
        'EDUCATION': t('program.education'),
        'LITERATURE': t('program.literature'),
        'HISTORY': t('program.history'),
        'PHILOSOPHY': t('program.philosophy'),
        'LINGUISTICS': t('program.linguistics'),
        'BIOLOGY': t('program.biology'),
        'CHEMISTRY': t('program.chemistry'),
        'PHYSICS': t('program.physics'),
        'MATHEMATICS': t('program.mathematics'),
        'STATISTICS': t('program.statistics'),
        'ENVIRONMENTAL_SCIENCE': t('program.environmental_science'),
        'MEDICINE': t('program.medicine'),
        'NURSING': t('program.nursing'),
        'PHARMACY': t('program.pharmacy'),
        'DENTISTRY': t('program.dentistry'),
        'ARCHITECTURE': t('program.architecture'),
        'FINE_ARTS': t('program.fine_arts'),
        'MUSIC': t('program.music'),
        'THEATER': t('program.theater'),
        'FILM_STUDIES': t('program.film_studies'),
        'COMMUNICATION': t('program.communication'),
        'JOURNALISM': t('program.journalism'),
        'DESIGN': t('program.design'),
        'ANTHROPOLOGY': t('program.anthropology'),
        'GEOGRAPHY': t('program.geography'),
        'SPORTS_SCIENCE': t('program.sports_science')
    };

    // Fonction pour remplacer les clés par leurs valeurs traduites
    const replaceProgramKeys = (text) => {
        let result = text;
        Object.keys(programMapping).forEach(key => {
            const regex = new RegExp(`\\b${key}\\b`, 'g');
            result = result.replace(regex, programMapping[key]);
        });
        return result;
    };

    useEffect(() => {
        const newSessionId = `session-${Date.now()}-${Math.random().toString(36).substr(2, 9)}`;
        setSessionId(newSessionId);

        setMessages([{
            role: 'assistant',
            content: 'Bonjour! Je suis votre assistant pour LeandrOSE. Je peux vous aider avec:\n\n• Les offres de stage (en attente, approuvées, rejetées)\n• Les CV en attente d\'approbation\n• Les programmes disponibles\n• Les candidatures acceptées\n• Les ententes de stage\n\nComment puis-je vous aider aujourd\'hui?',
            timestamp: new Date()
        }]);
    }, []);

    const scrollToBottom = () => {
        messagesEndRef.current?.scrollIntoView({ behavior: 'smooth' });
    };

    useEffect(() => {
        scrollToBottom();
    }, [messages]);

    const sendMessage = async () => {
        if (!input.trim() || loading) return;

        const userMessage = {
            role: 'user',
            content: input,
            timestamp: new Date()
        };

        setMessages(prev => [...prev, userMessage]);
        setInput('');
        setLoading(true);

        try {
            const token = sessionStorage.getItem("accessToken");
            const data = await sendChatMessage(input, sessionId, token);

            // Nettoyer les astérisques et remplacer les clés par les valeurs traduites
            let cleanContent = data.response.replace(/^\s*\*\s+/gm, '').trim();
            cleanContent = replaceProgramKeys(cleanContent);

            const assistantMessage = {
                role: 'assistant',
                content: cleanContent,
                timestamp: new Date()
            };

            setMessages(prev => [...prev, assistantMessage]);
        } catch (error) {
            console.error('❌ Erreur:', error);
            setMessages(prev => [...prev, {
                role: 'assistant',
                content: `Désolé, une erreur s'est produite: ${error.message}`,
                timestamp: new Date(),
                error: true
            }]);
        } finally {
            setLoading(false);
        }
    };

    const clearChat = async () => {
        if (!sessionId) return;

        try {
            const token = sessionStorage.getItem("accessToken");
            await clearChatSession(sessionId, token);

            const newSessionId = `session-${Date.now()}-${Math.random().toString(36).substr(2, 9)}`;
            setSessionId(newSessionId);

            setMessages([{
                role: 'assistant',
                content: 'Conversation réinitialisée. Comment puis-je vous aider?',
                timestamp: new Date()
            }]);
        } catch (error) {
            console.error('Erreur lors de la réinitialisation:', error);
        }
    };

    const handleKeyPress = (e) => {
        if (e.key === 'Enter' && !e.shiftKey) {
            e.preventDefault();
            sendMessage();
        }
    };

    const suggestedQuestions = [
        "Quelles sont les offres en attente d'approbation?",
        "Montre-moi les CV à approuver",
        "Liste les programmes disponibles",
        "Quelles candidatures sont acceptées?",
        "Affiche toutes les ententes de stage"
    ];

    if (!isOpen) {
        return null;
    }

    return (
        <div className="fixed bottom-4 right-4 z-50">
            <div className="bg-white rounded-2xl shadow-2xl border border-gray-200 w-96 h-[600px] flex flex-col overflow-hidden">
                {/* Header */}
                <div className="bg-gradient-to-r from-blue-600 to-blue-700 text-white p-4">
                    <div className="flex items-center justify-between">
                        <div className="flex items-center space-x-3">
                            <span className="text-2xl">🤖</span>
                            <div>
                                <h1 className="text-base font-bold">Assistant LeandrOSE</h1>
                                <p className="text-xs text-blue-100">Aide intelligente</p>
                            </div>
                        </div>
                        <div className="flex items-center gap-2">
                            <button
                                onClick={clearChat}
                                className="p-2 bg-gray-200 hover:bg-gray-300 text-gray-700 rounded-lg transition-colors"
                                title="Réinitialiser"
                            >
                                <Trash2 className="w-4 h-4" />
                            </button>
                            {onToggle && (
                                <button
                                    onClick={onToggle}
                                    className="p-2 bg-gray-200 hover:bg-gray-300 text-gray-700 rounded-lg transition-colors"
                                    title="Fermer"
                                >
                                    <span className="text-xl">×</span>
                                </button>
                            )}
                        </div>
                    </div>
                </div>

                {/* Messages */}
                <div className="flex-1 overflow-y-auto p-4 space-y-3 bg-gray-50">
                    {messages.map((message, index) => (
                        <div
                            key={index}
                            className={`flex ${message.role === 'user' ? 'justify-end' : 'justify-start'}`}
                        >
                            <div
                                className={`flex items-start space-x-2 max-w-[85%] ${
                                    message.role === 'user' ? 'flex-row-reverse space-x-reverse' : ''
                                }`}
                            >
                                <div
                                    className={`flex-shrink-0 w-7 h-7 rounded-full flex items-center justify-center ${
                                        message.role === 'user'
                                            ? 'bg-blue-600 text-white'
                                            : 'bg-gray-300 text-gray-700'
                                    }`}
                                >
                                    {message.role === 'user' ? <User className="w-4 h-4" /> : <span className="text-lg">🤖</span>}
                                </div>
                                <div
                                    className={`rounded-lg p-3 ${
                                        message.role === 'user'
                                            ? 'bg-blue-600 text-white'
                                            : message.error
                                                ? 'bg-red-50 text-red-900 border border-red-200'
                                                : 'bg-white text-gray-900 shadow-sm'
                                    }`}
                                >
                                    <div className="text-sm whitespace-pre-wrap break-words">{message.content}</div>
                                    <div className={`text-xs mt-1 ${
                                        message.role === 'user' ? 'text-blue-100' : 'text-gray-500'
                                    }`}>
                                        {message.timestamp.toLocaleTimeString('fr-FR', {
                                            hour: '2-digit',
                                            minute: '2-digit'
                                        })}
                                    </div>
                                </div>
                            </div>
                        </div>
                    ))}

                    {loading && (
                        <div className="flex justify-start">
                            <div className="flex items-center space-x-2 bg-white rounded-lg p-3 shadow-sm">
                                <span className="text-lg">🤖</span>
                                <div className="flex space-x-1">
                                    <div className="w-2 h-2 bg-blue-600 rounded-full animate-bounce" style={{ animationDelay: '0ms' }}></div>
                                    <div className="w-2 h-2 bg-blue-600 rounded-full animate-bounce" style={{ animationDelay: '150ms' }}></div>
                                    <div className="w-2 h-2 bg-blue-600 rounded-full animate-bounce" style={{ animationDelay: '300ms' }}></div>
                                </div>
                            </div>
                        </div>
                    )}

                    <div ref={messagesEndRef} />
                </div>

                {/* Suggested Questions */}
                {messages.length === 1 && (
                    <div className="px-4 py-3 bg-gray-50 border-t border-gray-200">
                        <p className="text-xs text-gray-600 mb-2 font-medium">Essayez:</p>
                        <div className="flex flex-col gap-1.5">
                            {suggestedQuestions.slice(0, 3).map((question, index) => (
                                <button
                                    key={index}
                                    onClick={() => setInput(question)}
                                    className="text-xs bg-white border border-gray-300 rounded-md px-2 py-1.5 hover:bg-gray-50 transition-colors text-left"
                                >
                                    {question}
                                </button>
                            ))}
                        </div>
                    </div>
                )}

                {/* Input */}
                <div className="border-t bg-white p-3">
                    <div className="flex space-x-2">
                        <textarea
                            value={input}
                            onChange={(e) => setInput(e.target.value)}
                            onKeyPress={handleKeyPress}
                            placeholder="Votre question..."
                            className="flex-1 border border-gray-300 rounded-lg px-3 py-2 text-sm focus:outline-none focus:ring-2 focus:ring-blue-500 resize-none"
                            rows="2"
                            disabled={loading}
                        />
                        <button
                            onClick={sendMessage}
                            disabled={!input.trim() || loading}
                            className="bg-blue-600 text-white rounded-lg px-4 hover:bg-blue-700 disabled:bg-gray-300 disabled:cursor-not-allowed transition-colors flex items-center justify-center"
                        >
                            <Send className="w-4 h-4" />
                        </button>
                    </div>
                    <p className="text-xs text-gray-500 mt-1.5">
                        Entrée = envoyer • Shift+Entrée = nouvelle ligne
                    </p>
                </div>
            </div>
        </div>
    );
}