import React, { useState, useEffect } from 'react';
import axios from 'axios';
import { useAuth } from '../context/AuthContext';
import { CheckCircle, XCircle, Clock } from 'lucide-react';

const Vendors = () => {
  const [vendors, setVendors] = useState([]);
  const [loading, setLoading] = useState(true);
  const { user } = useAuth();

  useEffect(() => {
    fetchVendors();
  }, []);

  const fetchVendors = async () => {
    try {
      const response = await axios.get('http://localhost:8080/api/vendors', {
        headers: { Authorization: `Bearer ${user.token}` }
      });
      setVendors(response.data);
    } catch (err) {
      console.error('Error fetching vendors', err);
    } finally {
      setLoading(false);
    }
  };

  const updateStatus = async (id, status) => {
    try {
      await axios.put(`http://localhost:8080/api/vendors/${id}/status?status=${status}`, {}, {
        headers: { Authorization: `Bearer ${user.token}` }
      });
      fetchVendors();
    } catch (err) {
      console.error('Error updating status', err);
    }
  };

  if (loading) return <div>Loading vendors...</div>;

  return (
    <div className="animate-fade-in">
      <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', marginBottom: '30px' }}>
        <h1>Vendor Management</h1>
        <button className="btn btn-primary">Invite Vendor</button>
      </div>

      <div className="glass" style={{ overflow: 'hidden' }}>
        <table style={{ width: '100%', borderCollapse: 'collapse', textAlign: 'left' }}>
          <thead>
            <tr style={{ background: 'rgba(255,255,255,0.03)', borderBottom: '1px solid var(--glass-border)' }}>
              <th style={{ padding: '15px 20px' }}>Company</th>
              <th style={{ padding: '15px 20px' }}>Tax ID</th>
              <th style={{ padding: '15px 20px' }}>Status</th>
              <th style={{ padding: '15px 20px' }}>Rating</th>
              <th style={{ padding: '15px 20px' }}>Actions</th>
            </tr>
          </thead>
          <tbody>
            {vendors.map(vendor => (
              <tr key={vendor.id} style={{ borderBottom: '1px solid var(--glass-border)' }}>
                <td style={{ padding: '15px 20px' }}>
                  <div style={{ fontWeight: '600' }}>{vendor.companyName}</div>
                  <div style={{ fontSize: '0.8rem', color: 'var(--text-dim)' }}>{vendor.website}</div>
                </td>
                <td style={{ padding: '15px 20px' }}>{vendor.taxId}</td>
                <td style={{ padding: '15px 20px' }}>
                  <span style={{ 
                    padding: '4px 10px', borderRadius: '20px', fontSize: '0.8rem',
                    background: vendor.status === 'APPROVED' ? '#10b98120' : vendor.status === 'PENDING' ? '#f59e0b20' : '#f43f5e20',
                    color: vendor.status === 'APPROVED' ? '#10b981' : vendor.status === 'PENDING' ? '#f59e0b' : '#f43f5e',
                    display: 'flex', alignItems: 'center', gap: '5px', width: 'fit-content'
                   }}>
                    {vendor.status === 'APPROVED' ? <CheckCircle size={14} /> : vendor.status === 'PENDING' ? <Clock size={14} /> : <XCircle size={14} />}
                    {vendor.status}
                  </span>
                </td>
                <td style={{ padding: '15px 20px' }}>{vendor.rating} ★</td>
                <td style={{ padding: '15px 20px' }}>
                  {vendor.status === 'PENDING' && (
                    <div style={{ display: 'flex', gap: '10px' }}>
                      <button onClick={() => updateStatus(vendor.id, 'APPROVED')} className="btn btn-primary" style={{ padding: '6px 12px', fontSize: '0.8rem' }}>Approve</button>
                      <button onClick={() => updateStatus(vendor.id, 'REJECTED')} className="btn btn-outline" style={{ padding: '6px 12px', fontSize: '0.8rem' }}>Reject</button>
                    </div>
                  )}
                </td>
              </tr>
            ))}
          </tbody>
        </table>
        {vendors.length === 0 && (
          <div style={{ padding: '40px', textAlign: 'center', color: 'var(--text-dim)' }}>
            No vendors found in the system.
          </div>
        )}
      </div>
    </div>
  );
};

export default Vendors;
