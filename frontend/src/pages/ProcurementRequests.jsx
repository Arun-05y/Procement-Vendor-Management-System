import React, { useState, useEffect } from 'react';
import axios from 'axios';
import { useAuth } from '../context/AuthContext';
import { Plus, FileText, Send, CheckCircle } from 'lucide-react';

const ProcurementRequests = () => {
  const [requests, setRequests] = useState([]);
  const [showModal, setShowModal] = useState(false);
  const [newRequest, setNewRequest] = useState({ title: '', description: '', estimatedBudget: '', department: '' });
  const { user } = useAuth();

  useEffect(() => {
    fetchRequests();
  }, []);

  const fetchRequests = async () => {
    try {
      const response = await axios.get('http://localhost:8080/api/procurement/requests', {
        headers: { Authorization: `Bearer ${user.token}` }
      });
      setRequests(response.data);
    } catch (err) {
      console.error('Error fetching requests', err);
    }
  };

  const handleSubmit = async (e) => {
    e.preventDefault();
    try {
      await axios.post('http://localhost:8080/api/procurement/requests', newRequest, {
        headers: { Authorization: `Bearer ${user.token}` }
      });
      setShowModal(false);
      fetchRequests();
    } catch (err) {
      console.error('Error creating request', err);
    }
  };

  return (
    <div className="animate-fade-in">
      <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', marginBottom: '30px' }}>
        <h1>Procurement Requests</h1>
        <button onClick={() => setShowModal(true)} className="btn btn-primary" style={{ display: 'flex', alignItems: 'center', gap: '8px' }}>
          <Plus size={20} /> New Request
        </button>
      </div>

      <div style={{ display: 'grid', gridTemplateColumns: 'repeat(auto-fill, minmax(350px, 1fr))', gap: '20px' }}>
        {requests.map(req => (
          <div key={req.id} className="glass" style={{ padding: '24px' }}>
            <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'start', marginBottom: '15px' }}>
              <div style={{ padding: '10px', borderRadius: '10px', background: 'rgba(99, 102, 241, 0.1)', color: 'var(--primary)' }}>
                <FileText size={24} />
              </div>
              <span style={{ 
                padding: '4px 12px', borderRadius: '20px', fontSize: '0.75rem', fontWeight: '600',
                background: req.status === 'APPROVED' ? '#10b98120' : '#6366f120',
                color: req.status === 'APPROVED' ? '#10b981' : '#6366f1'
               }}>
                {req.status}
              </span>
            </div>
            <h3 style={{ marginBottom: '10px' }}>{req.title}</h3>
            <p style={{ color: 'var(--text-dim)', fontSize: '0.9rem', marginBottom: '20px', height: '40px', overflow: 'hidden' }}>
              {req.description}
            </p>
            <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', paddingTop: '20px', borderTop: '1px solid var(--glass-border)' }}>
              <div>
                <p style={{ fontSize: '0.75rem', color: 'var(--text-dim)' }}>Estimated Budget</p>
                <p style={{ fontWeight: '700', color: 'var(--text-main)' }}>${req.estimatedBudget?.toLocaleString()}</p>
              </div>
              <button className="btn btn-outline" style={{ padding: '8px 16px', fontSize: '0.85rem' }}>View Details</button>
            </div>
          </div>
        ))}
      </div>

      {showModal && (
        <div style={{
          position: 'fixed', top: 0, left: 0, width: '100%', height: '100%',
          background: 'rgba(0,0,0,0.8)', display: 'flex', justifyContent: 'center', alignItems: 'center', zIndex: 100
        }}>
          <div className="glass" style={{ padding: '40px', width: '500px' }}>
            <h2>New Procurement Request</h2>
            <form onSubmit={handleSubmit} style={{ marginTop: '20px' }}>
              <div className="input-group">
                <label className="input-label">Title</label>
                <input className="input-field" type="text" onChange={e => setNewRequest({...newRequest, title: e.target.value})} required />
              </div>
              <div className="input-group">
                <label className="input-label">Description</label>
                <textarea className="input-field" style={{ minHeight: '100px' }} onChange={e => setNewRequest({...newRequest, description: e.target.value})} required />
              </div>
              <div style={{ display: 'grid', gridTemplateColumns: '1fr 1fr', gap: '20px' }}>
                <div className="input-group">
                  <label className="input-label">Budget ($)</label>
                  <input className="input-field" type="number" onChange={e => setNewRequest({...newRequest, estimatedBudget: e.target.value})} required />
                </div>
                <div className="input-group">
                  <label className="input-label">Department</label>
                  <input className="input-field" type="text" onChange={e => setNewRequest({...newRequest, department: e.target.value})} required />
                </div>
              </div>
              <div style={{ display: 'flex', gap: '15px', marginTop: '20px' }}>
                <button type="submit" className="btn btn-primary" style={{ flex: 1 }}>Submit Request</button>
                <button type="button" onClick={() => setShowModal(false)} className="btn btn-outline" style={{ flex: 1 }}>Cancel</button>
              </div>
            </form>
          </div>
        </div>
      )}
    </div>
  );
};

export default ProcurementRequests;
