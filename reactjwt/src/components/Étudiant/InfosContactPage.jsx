import React, { useEffect, useState } from 'react';
import { useTranslation } from 'react-i18next';
import { FaUser, FaBuilding, FaEnvelope, FaPhone, FaBriefcase, FaSpinner } from 'react-icons/fa';

export default function StudentContactsPage() {
    const { t } = useTranslation();
    const [professor, setProfessor] = useState(null);
    const [gestionnaire, setGestionnaire] = useState(null);
    const [employeurs, setEmployeurs] = useState([]);
    const [loading, setLoading] = useState(true);

    useEffect(() => {
        fetchAllContacts();
    }, []);

    const fetchAllContacts = async () => {
        try {
            setLoading(true);
            const token = sessionStorage.getItem('accessToken');
            const headers = {
                'Authorization': `Bearer ${token}`,
                'Content-Type': 'application/json',
            };

            const [profResponse, gestionnaireResponse, employeursResponse] = await Promise.allSettled([
                fetch('http://localhost:8080/student/prof', { method: 'GET', headers }),
                fetch('http://localhost:8080/student/gestionnaire', { method: 'GET', headers }),
                fetch('http://localhost:8080/student/employeurs', { method: 'GET', headers })
            ]);

            if (profResponse.status === 'fulfilled' && profResponse.value.ok) {
                setProfessor(await profResponse.value.json());
            }

            if (gestionnaireResponse.status === 'fulfilled' && gestionnaireResponse.value.ok) {
                setGestionnaire(await gestionnaireResponse.value.json());
            }

            if (employeursResponse.status === 'fulfilled' && employeursResponse.value.ok) {
                setEmployeurs(await employeursResponse.value.json());
            }
        } catch (err) {
            console.error('Error:', err);
        } finally {
            setLoading(false);
        }
    };

    const ContactCard = ({ title, icon: Icon, person, showCompany = false, isProfessor = false }) => {
        if (!person || !person.id) {
            return (
                <div className="bg-white rounded-lg shadow-md p-6 border border-gray-200">
                    <div className="flex items-center gap-3 mb-4">
                        <div className="bg-gray-100 p-3 rounded-full">
                            <Icon className="text-gray-400 text-xl" />
                        </div>
                        <h3 className="text-xl font-semibold text-gray-900">{title}</h3>
                    </div>
                    <p className="text-gray-500 text-center py-8">{t('dashboardStudent.contacts.noContact')}</p>
                </div>
            );
        }

        return (
            <div className="bg-white rounded-lg shadow-md p-6 border border-gray-200 hover:shadow-lg transition-shadow">
                <div className="flex items-center gap-3 mb-4">
                    <div className="bg-blue-100 p-3 rounded-full">
                        <Icon className="text-blue-600 text-xl" />
                    </div>
                    <h3 className="text-xl font-semibold text-gray-900">{title}</h3>
                </div>

                <div className="space-y-3">
                    <div className="flex items-center gap-2">
                        <FaUser className="text-gray-400 text-sm" />
                        <span className="text-gray-900 font-medium">
              {person.firstName} {person.lastName || person.lastname}
            </span>
                    </div>

                    {person.email && (
                        <div className="flex items-center gap-2">
                            <FaEnvelope className="text-gray-400 text-sm" />
                            <a
                                href={`mailto:${person.email}`}
                                className="text-blue-600 hover:text-blue-800 hover:underline break-all"
                            >
                                {person.email}
                            </a>
                        </div>
                    )}

                    {person.phoneNumber && (
                        <div className="flex items-center gap-2">
                            <FaPhone className="text-gray-400 text-sm" />
                            <a href={`tel:${person.phoneNumber}`} className="text-blue-600 hover:text-blue-800 hover:underline">
                                {person.phoneNumber}
                            </a>
                        </div>
                    )}

                    {showCompany && person.companyName && (
                        <div className="flex items-center gap-2">
                            <FaBuilding className="text-gray-400 text-sm" />
                            <span className="text-gray-700 font-medium">{person.companyName}</span>
                        </div>
                    )}

                    {person.program && (
                        <div className="flex items-center gap-2">
                            <FaBriefcase className="text-gray-400 text-sm" />
                            <span className="text-gray-700 font-medium">
                                {t(person.program.replace("program.", ""))}
        </span>
                        </div>
                    )}


                    {person.field && (
                        <div className="flex items-center gap-2">
                            <FaBriefcase className="text-gray-400 text-sm" />
                            <span className="text-gray-700">{person.field}</span>
                        </div>
                    )}

                    {isProfessor && person.department && (
                        <div className="flex items-center gap-2">
                            <FaBriefcase className="text-gray-400 text-sm" />
                            <span className="text-gray-700">{t('dashboardStudent.contacts.department')}: {person.department}</span>
                        </div>
                    )}
                </div>
            </div>
        );
    };

    if (loading) {
        return (
            <div className="flex justify-center items-center h-64">
                <FaSpinner className="animate-spin text-blue-600 text-5xl" />
                <span className="ml-4 text-blue-600 font-medium">{t('dashboardStudent.contacts.loading')}</span>
            </div>
        );
    }

    return (
        <div className="max-w-6xl mx-auto p-6">
            <div className="mb-8">
                <h1 className="text-3xl font-bold text-gray-900 mb-2">{t('dashboardStudent.contacts.title')}</h1>
                <p className="text-gray-600">{t('dashboardStudent.contacts.subtitle')}</p>
            </div>

            <div className="space-y-8">
                <div>
                    <h2 className="text-2xl font-semibold text-gray-800 mb-4">{t('dashboardStudent.contacts.supportTeam')}</h2>
                    <div className="grid grid-cols-1 md:grid-cols-2 gap-6">
                        <ContactCard title={t('dashboardStudent.contacts.professor')} icon={FaUser} person={professor} isProfessor={true} />
                        <ContactCard title={t('dashboardStudent.contacts.gestionnaire')} icon={FaUser} person={gestionnaire} />
                    </div>
                </div>

                <div>
                    <h2 className="text-2xl font-semibold text-gray-800 mb-4">
                        {t('dashboardStudent.contacts.employeurs')} {employeurs.length > 0 && `(${employeurs.length})`}
                    </h2>

                    {employeurs.length === 0 ? (
                        <div className="bg-white rounded-lg shadow-md p-6 border border-gray-200">
                            <p className="text-gray-500 text-center py-8">{t('dashboardStudent.contacts.noEmployeur')}</p>
                        </div>
                    ) : (
                        <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-3 gap-6">
                            {employeurs.map((employeur, index) => (
                                <ContactCard
                                    key={employeur.id || index}
                                    title={`${t('dashboardStudent.contacts.employeur')} ${employeurs.length > 1 ? index + 1 : ''}`}
                                     icon={FaBuilding}
                                    person={employeur}
                                    showCompany={true}
                                />
                            ))}
                        </div>
                    )}
                </div>
            </div>

            <div className="mt-8 bg-blue-50 border border-blue-200 rounded-lg p-4">
                <p className="text-blue-800 text-sm">
                    <strong>{t('dashboardStudent.contacts.infoReadonlyTitle', { defaultValue: 'Information:' })}</strong> {t('dashboardStudent.contacts.infoReadonly', { defaultValue: 'Ces informations sont en lecture seule.' })}
                </p>
            </div>
        </div>
    );
}