import React, { useState, useEffect } from 'react';
import { Link } from 'react-router-dom';
import { getEligibleEvaluations } from '../../api/apiEmployeur';

export default function EvaluationsList() {
    const [eligibleAgreements, setEligibleAgreements] = useState([]);
    const [loading, setLoading] = useState(true);

    useEffect(() => {
        const fetchEligibleAgreements = async () => {
            try {
                const agreements = await getEligibleEvaluations();
                setEligibleAgreements(agreements);
            } catch (error) {
                console.error('Error fetching eligible agreements:', error);
            } finally {
                setLoading(false);
            }
        };
        fetchEligibleAgreements();
    }, []);

    if (loading) return <div>Loading...</div>;

    return (
        <div className="max-w-4xl mx-auto">
            <h1 className="text-2xl font-bold mb-6">Evaluations Available</h1>
            {eligibleAgreements.length === 0 ? (
                <p>No evaluations available at this time.</p>
            ) : (
                <div className="space-y-4">
                    {eligibleAgreements.map(agreement => (
                        <div key={agreement.id} className="border rounded-lg p-4">
                            <h3 className="font-semibold">
                                {agreement.studentFirstName} {agreement.studentLastName}
                            </h3>
                            <p>{agreement.internshipDescription}</p>
                            <Link
                                to={`/dashboard/employeur/evaluation/${agreement.studentId}/${agreement.offerId}`}
                                className="bg-blue-600 text-white px-4 py-1 rounded hover:bg-blue-700"
                            >
                                Create Evaluation
                            </Link>
                        </div>
                    ))}
                </div>
            )}
        </div>
    );
}