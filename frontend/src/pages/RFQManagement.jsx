import React, { useState, useEffect } from 'react';
import axios from 'axios';
import { useAuth } from '../context/AuthContext';
import { Package, Calendar, Award, TrendingUp } from 'lucide-react';

const RFQManagement = () => {
  const [rfqs, setRfqs] = useState([]);
  const [loading, setLoading] = useState(true);
  const { user } = useAuth();

  useEffect(() => {
    fetchRfqs();
  }, []);

  const fetchRfqs = async () => {
    // In a real app, this would be an API call
    // For now, let's mock some RFQs since we haven't created them via UI yet
    setRfqs([
      { id: 1, title: 'Laptops for IT Dept', deadline: '2026-04-01', status: 'OPEN', responses: 3 },
      { id: 2, title: 'Office Furniture', deadline: '2026-03-25', status: 'CLOSED', responses: 5 },
    ]);
    setLoading(false);
  };

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
            <h3 style={{ marginBottom: '15px' }}>{rfq.title}</h3>
            <div style={{ display: 'flex', alignItems: 'center', gap: '8px', color: 'var(--text-dim)', fontSize: '0.85rem', marginBottom: '8px' }}>
              <Calendar size={16} /> Deadline: {rfq.deadline}
            </div>
            <div style={{ display: 'flex', alignItems: 'center', gap: '8px', color: 'var(--text-dim)', fontSize: '0.85rem', marginBottom: '25px' }}>
              <TrendingUp size={16} /> Responses: {rfq.responses}
            </div>
            
            <button className="btn btn-primary" style={{ width: '100%', display: 'flex', alignItems: 'center', justifyContent: 'center', gap: '8px' }}>
              <Award size={18} /> Compare Quotations
            </button>
          </div>
        ))}
      </div>
    </div>
  );
};

export default RFQManagement;
