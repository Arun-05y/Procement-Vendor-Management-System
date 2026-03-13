import React, { useState, useEffect } from 'react';
import axios from 'axios';
import { useAuth } from '../context/AuthContext';
import { Upload, File, CheckCircle } from 'lucide-react';

const VendorProfile = () => {
  const [vendor, setVendor] = useState(null);
  const [documents, setDocuments] = useState([]);
  const { user } = useAuth();

  useEffect(() => {
    // In a real app, fetch based on authenticated user's vendor ID
    setVendor({
      companyName: 'Global Supplies Inc',
      taxId: 'TX-99001122',
      status: 'APPROVED',
      rating: 4.8
    });
    setDocuments([
      { id: 1, type: 'Business License', name: 'license_2024.pdf', date: '2024-01-10' },
      { id: 2, type: 'Tax Certificate', name: 'tax_cert.pdf', date: '2024-01-12' }
    ]);
  }, []);

  if (!vendor) return <div>Loading profile...</div>;

  return (
    <div className="animate-fade-in">
      <div className="glass" style={{ padding: '30px', marginBottom: '30px', display: 'flex', gap: '30px', alignItems: 'center' }}>
        <div style={{ width: '80px', height: '80px', borderRadius: '40px', background: 'var(--primary)', display: 'flex', justifyContent: 'center', alignItems: 'center', fontSize: '2rem' }}>
          {vendor.companyName[0]}
        </div>
        <div>
          <h1>{vendor.companyName}</h1>
          <p style={{ color: 'var(--text-dim)' }}>Tax ID: {vendor.taxId} | Rating: {vendor.rating} ★</p>
        </div>
        <div style={{ marginLeft: 'auto' }}>
          <span style={{ padding: '6px 16px', borderRadius: '20px', background: '#10b98120', color: '#10b981', fontWeight: '600' }}>
            {vendor.status}
          </span>
        </div>
      </div>

      <div className="glass" style={{ padding: '30px' }}>
        <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', marginBottom: '20px' }}>
          <h3>Compliance Documents</h3>
          <button className="btn btn-primary" style={{ display: 'flex', alignItems: 'center', gap: '8px' }}>
            <Upload size={18} /> Upload New
          </button>
        </div>

        <div style={{ display: 'grid', gridTemplateColumns: 'repeat(auto-fill, minmax(280px, 1fr))', gap: '20px' }}>
          {documents.map(doc => (
            <div key={doc.id} style={{ padding: '20px', background: 'rgba(255,255,255,0.03)', borderRadius: '12px', border: '1px solid var(--glass-border)' }}>
              <div style={{ display: 'flex', alignItems: 'center', gap: '12px', marginBottom: '10px' }}>
                <File color="var(--primary)" />
                <span style={{ fontWeight: '600' }}>{doc.type}</span>
              </div>
              <p style={{ fontSize: '0.8rem', color: 'var(--text-dim)', marginBottom: '15px' }}>{doc.name}</p>
              <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center' }}>
                <span style={{ fontSize: '0.75rem', color: 'var(--text-dim)' }}>Uploaded {doc.date}</span>
                <CheckCircle size={16} color="#10b981" />
              </div>
            </div>
          ))}
        </div>
      </div>
    </div>
  );
};

export default VendorProfile;
