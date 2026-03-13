import React from 'react';
import { ShoppingBag, Users, FileText, CheckCircle } from 'lucide-react';

const StatCard = ({ title, value, icon, color }) => (
  <div className="glass animate-fade-in" style={{ padding: '24px', flex: 1, minWidth: '200px' }}>
    <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'start' }}>
      <div>
        <p style={{ color: 'var(--text-dim)', fontSize: '0.9rem', marginBottom: '8px' }}>{title}</p>
        <h2 style={{ fontSize: '1.8rem' }}>{value}</h2>
      </div>
      <div style={{ padding: '12px', borderRadius: '12px', background: `${color}20`, color: color }}>
        {icon}
      </div>
    </div>
  </div>
);

const Dashboard = () => {
  return (
    <div>
      <h1 style={{ marginBottom: '30px' }}>Procurement Overview</h1>
      
      <div style={{ display: 'flex', gap: '20px', marginBottom: '40px', flexWrap: 'wrap' }}>
        <StatCard title="Active Vendors" value="24" icon={<Users />} color="#6366f1" />
        <StatCard title="Pending RFQs" value="12" icon={<FileText />} color="#f59e0b" />
        <StatCard title="Total Orders" value="156" icon={<ShoppingBag />} color="#10b981" />
        <StatCard title="Completed" value="142" icon={<CheckCircle />} color="#6366f1" />
      </div>

      <div style={{ display: 'grid', gridTemplateColumns: '2fr 1fr', gap: '30px' }}>
        <div className="glass" style={{ padding: '30px', minHeight: '300px' }}>
          <h3>Recent Procurement Requests</h3>
          <p style={{ color: 'var(--text-dim)', marginTop: '40px', textAlign: 'center' }}>
            Fetch data from API to display here
          </p>
        </div>
        <div className="glass" style={{ padding: '30px' }}>
          <h3>Vendor Ratings</h3>
          <div style={{ marginTop: '20px' }}>
            {['Vendor Alpha', 'Global Supplies', 'Tech Corp'].map(v => (
              <div key={v} style={{ marginBottom: '15px' }}>
                <div style={{ display: 'flex', justifyContent: 'space-between', marginBottom: '5px' }}>
                  <span>{v}</span>
                  <span style={{ color: '#10b981' }}>4.8 ★</span>
                </div>
                <div style={{ height: '6px', background: 'rgba(255,255,255,0.05)', borderRadius: '3px' }}>
                  <div style={{ width: '90%', height: '100%', background: 'var(--primary)', borderRadius: '3px' }}></div>
                </div>
              </div>
            ))}
          </div>
        </div>
      </div>
    </div>
  );
};

export default Dashboard;
