import React, { useState, useEffect } from 'react';
import api from '../services/api';
import { Package, Calendar, Award, TrendingUp } from 'lucide-react';

const RFQManagement = () => {
  const [rfqs, setRfqs] = useState([]);
  const [loading, setLoading] = useState(true);

  useEffect(() => {
    fetchRfqs();
  }, []);

  const fetchRfqs = async () => {
    try {
      const response = await api.get('/procurement/rfqs');
      setRfqs(response.data);
    } catch (err) {
      console.warn('Backend API connection failed, using local mock RFQs.');
      setRfqs([
        { id: 1, status: 'OPEN', request: { title: 'Laptops for IT Dept', id: 101 }, deadline: '2026-09-30', invitedVendors: [1, 2, 3] },
        { id: 2, status: 'CLOSED', request: { title: 'Office Furniture', id: 102 }, deadline: '2026-08-10', invitedVendors: [1, 2] }
      ]);
    } finally {
      setLoading(false);
    }
  };

  if (loading) return <div style={{ padding: '40px', textAlign: 'center' }}>Loading RFQs...</div>;

  return (
    <div className="animate-fade-in">
      <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', marginBottom: '30px' }}>
        <h1>RFQ Management</h1>
      </div>

      <div style={{ display: 'grid', gridTemplateColumns: 'repeat(auto-fill, minmax(300px, 1fr))', gap: '20px' }}>
        {rfqs.map(rfq => (
          <div key={rfq.id} className="glass" style={{ padding: '24px' }}>
            <div style={{ display: 'flex', justifyContent: 'space-between', marginBottom: '15px' }}>
              <Package size={24} color="var(--primary)" />
              <span style={{ 
                padding: '4px 10px', borderRadius: '12px', fontSize: '0.75rem',
                background: rfq.status === 'OPEN' ? '#10b98120' : '#f43f5e20',
                color: rfq.status === 'OPEN' ? '#10b981' : '#f43f5e'
              }}>{rfq.status}</span>
            </div>
            <h3 style={{ marginBottom: '15px' }}>{rfq.request?.title || 'RFQ for Request #' + rfq.request?.id}</h3>
            <div style={{ display: 'flex', alignItems: 'center', gap: '8px', color: 'var(--text-dim)', fontSize: '0.85rem', marginBottom: '8px' }}>
              <Calendar size={16} /> Deadline: {rfq.deadline ? new Date(rfq.deadline).toLocaleDateString() : 'N/A'}
            </div>
            <div style={{ display: 'flex', alignItems: 'center', gap: '8px', color: 'var(--text-dim)', fontSize: '0.85rem', marginBottom: '25px' }}>
              <TrendingUp size={16} /> Invited Vendors: {rfq.invitedVendors?.length || 0}
            </div>
            
            <button className="btn btn-primary" style={{ width: '100%', display: 'flex', alignItems: 'center', justifyContent: 'center', gap: '8px' }}>
              <Award size={18} /> Compare Quotations
            </button>
          </div>
        ))}
      </div>
      {rfqs.length === 0 && (
        <div style={{ padding: '40px', textAlign: 'center', color: 'var(--text-dim)' }}>
          No RFQs found in the system.
        </div>
      )}
    </div>
  );
};

export default RFQManagement;
