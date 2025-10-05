import React, { useEffect, useState } from 'react';
import { getMyCandidatures } from '../api/apiStudent';
import { useNavigate } from 'react-router-dom';

export default function StudentApplicationsList() {
  const navigate = useNavigate();
  const [candidatures, setCandidatures] = useState([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState(null);

  useEffect(() => {
    let cancelled = false;
    async function fetchData() {
      setLoading(true);
      setError(null);
      try {
        const data = await getMyCandidatures();
        if (!cancelled) {
          setCandidatures(Array.isArray(data) ? data : []);
        }
      } catch (e) {
        if (!cancelled) setError('Impossible de charger vos candidatures');
      } finally {
        if (!cancelled) setLoading(false);
      }
    }
    fetchData();
    return () => { cancelled = true; };
  }, []);

  const formatDate = (dateString) => {
    if (!dateString) return 'Non définie';
    return new Date(dateString).toLocaleDateString('fr-FR');
  };

  const getStatusBadge = (status) => {
    const s = (status || '').toUpperCase();
    const base = 'px-3 py-1 text-xs font-medium rounded-full border';
    switch (s) {
      case 'PENDING': return <span className={`${base} bg-yellow-100 text-yellow-800 border-yellow-200`}>En attente</span>;
      case 'ACCEPTED': return <span className={`${base} bg-green-100 text-green-800 border-green-200`}>Acceptée</span>;
      case 'REJECTED': return <span className={`${base} bg-red-100 text-red-800 border-red-200`}>Refusée</span>;
      default: return <span className={`${base} bg-gray-100 text-gray-800 border-gray-200`}>{s}</span>;
    }
  };

  if (loading) {
    return (
      <div className="bg-white shadow rounded-lg p-6">
        <div className="animate-pulse h-4 bg-gray-200 w-1/3 mb-4 rounded" />
        <div className="space-y-2">
          <div className="h-3 bg-gray-200 rounded" />
          <div className="h-3 bg-gray-200 rounded w-5/6" />
          <div className="h-3 bg-gray-200 rounded w-4/6" />
        </div>
      </div>
    );
  }

  if (error) {
    return (
      <div className="bg-white shadow rounded-lg p-6 text-center">
        <p className="text-red-600 mb-4">{error}</p>
        <button onClick={() => window.location.reload()} className="px-4 py-2 bg-red-600 text-white rounded">Réessayer</button>
      </div>
    );
  }

  if (!candidatures.length) {
    return (
      <div className="bg-white shadow rounded-lg p-6 text-center">
        <div className="mx-auto mb-4 h-14 w-14 rounded-full bg-gray-100 flex items-center justify-center text-2xl">📄</div>
        <h3 className="text-lg font-medium text-gray-900 mb-2">Aucune candidature</h3>
        <p className="text-gray-600 mb-4">Vous n'avez pas encore postulé à une offre.</p>
        <button
          onClick={() => navigate('/dashboard/student')}
          className="px-4 py-2 bg-indigo-600 text-white rounded hover:bg-indigo-700"
        >Voir les offres</button>
      </div>
    );
  }

  return (
    <div className="bg-white shadow rounded-lg overflow-hidden">
      <div className="px-6 py-4 border-b border-gray-200 flex items-center justify-between">
        <div>
          <h3 className="text-lg font-medium text-gray-900">Mes candidatures</h3>
          <p className="text-sm text-gray-600">{candidatures.length} candidature{candidatures.length > 1 ? 's' : ''}</p>
        </div>

      </div>
      <div className="overflow-x-auto">
        <table className="min-w-full divide-y divide-gray-200">
          <thead className="bg-gray-50">
            <tr>
              <th className="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">Offre</th>
              <th className="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">Entreprise</th>
              <th className="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">Date</th>
              <th className="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">Statut</th>
              <th className="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">Actions</th>
            </tr>
          </thead>
          <tbody className="bg-white divide-y divide-gray-200">
            {candidatures.map(c => (
              <tr key={c.id} className="hover:bg-gray-50">
                <td className="px-6 py-4 text-sm font-medium text-gray-900">{c.offerDescription}</td>
                <td className="px-6 py-4 text-sm text-gray-900">{c.companyName}</td>
                <td className="px-6 py-4 text-sm text-gray-900 whitespace-nowrap">{formatDate(c.applicationDate)}</td>
                <td className="px-6 py-4 whitespace-nowrap">{getStatusBadge(c.status)}</td>
                <td className="px-6 py-4 whitespace-nowrap text-sm">
                  <button
                    onClick={() => navigate(`/dashboard/student/offers/${c.offerId}`)}
                    className="text-indigo-600 hover:text-indigo-900"
                  >Voir l'offre</button>
                </td>
              </tr>
            ))}
          </tbody>
        </table>
      </div>
    </div>
  );
}
