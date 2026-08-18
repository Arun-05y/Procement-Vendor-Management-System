import React, { useState, useEffect } from 'react';
import { ShoppingBag, Users, FileText, CheckCircle, DollarSign } from 'lucide-react';
import api from '../services/api';

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
  const [stats, setStats] = useState(null);
  const [requests, setRequests] = useState([]);
  const [vendors, setVendors] = useState([]);
  const [loading, setLoading] = useState(true);

  useEffect(() => {
    const loadDashboardData = async () => {
      try {
        const [statsRes, requestsRes, vendorsRes] = await Promise.all([
          api.get('/analytics/summary'),
          api.get('/procurement/requests'),
          api.get('/vendors')
        ]);
        setStats(statsRes.data);
        setRequests(requestsRes.data.slice(0, 5)); // show recent 5
        setVendors(vendorsRes.data.slice(0, 3)); // show top 3
      } catch (err) {
        console.warn('Backend API connection failed, using local mock data fallback.');
        setStats({
          vendorCount: 24,
          rfqCount: 12,
          totalSpend: 2540000,
          avgLeadTime: 4.2
        });
        setRequests([
          { id: 1, title: 'Laptops for IT Dept', department: 'IT', estimatedBudget: 50000, status: 'APPROVED' },
          { id: 2, title: 'Office Chairs', department: 'HR', estimatedBudget: 8000, status: 'SUBMITTED' },
          { id: 3, title: 'Server Rack Upgrade', department: 'IT', estimatedBudget: 120000, status: 'DRAFT' }
        ]);
        setVendors([
          { id: 1, companyName: 'Global Supplies Inc', rating: 4.8 },
          { id: 2, companyName: 'Tech Corp', rating: 4.5 },
          { id: 3, companyName: 'Office World', rating: 3.9 }
        ]);
      } finally {
        setLoading(false);
      }
    };
    loadDashboardData();
  }, []);

  if (loading) return <div style={{ padding: '40px', textAlign: 'center' }}>Loading Overview...</div>;

  return (
    <div>
      <h1 style={{ marginBottom: '30px' }}>Procurement Overview</h1>
      
      <div style={{ display: 'flex', gap: '20px', marginBottom: '40px', flexWrap: 'wrap' }}>
        <StatCard title="Active Vendors" value={stats?.vendorCount || 0} icon={<Users />} color="#6366f1" />
        <StatCard title="Pending RFQs" value={stats?.rfqCount || 0} icon={<FileText />} color="#f59e0b" />
        <StatCard title="Total Spend" value={`$${(stats?.totalSpend || 0).toLocaleString()}`} icon={<DollarSign />} color="#10b981" />
        <StatCard title="Avg. Lead Time" value={`${stats?.avgLeadTime ? stats.avgLeadTime.toFixed(1) : 0} days`} icon={<CheckCircle />} color="#6366f1" />
      </div>

      <div style={{ display: 'grid', gridTemplateColumns: '2fr 1fr', gap: '30px' }}>
        <div className="glass" style={{ padding: '30px', minHeight: '300px' }}>
          <h3>Recent Procurement Requests</h3>
          <div style={{ marginTop: '20px' }}>
            <table style={{ width: '100%', borderCollapse: 'collapse', textAlign: 'left' }}>
              <thead>
                <tr style={{ borderBottom: '1px solid var(--glass-border)' }}>
                  <th style={{ padding: '10px 5px', fontSize: '0.8rem', color: 'var(--text-dim)' }}>Title</th>
                  <th style={{ padding: '10px 5px', fontSize: '0.8rem', color: 'var(--text-dim)' }}>Dept</th>
                  <th style={{ padding: '10px 5px', fontSize: '0.8rem', color: 'var(--text-dim)' }}>Budget</th>
                  <th style={{ padding: '10px 5px', fontSize: '0.8rem', color: 'var(--text-dim)' }}>Status</th>
                </tr>
              </thead>
              <tbody>
                {requests.map(req => (
                  <tr key={req.id} style={{ borderBottom: '1px solid rgba(255,255,255,0.02)' }}>
                    <td style={{ padding: '10px 5px', fontWeight: '500' }}>{req.title}</td>
                    <td style={{ padding: '10px 5px' }}>{req.department}</td>
                    <td style={{ padding: '10px 5px' }}>${req.estimatedBudget?.toLocaleString()}</td>
                    <td style={{ padding: '10px 5px' }}>
                      <span style={{ 
                        fontSize: '0.75rem', padding: '2px 8px', borderRadius: '10px',
                        background: req.status === 'APPROVED' ? '#10b98120' : '#6366f120',
                        color: req.status === 'APPROVED' ? '#10b981' : '#6366f1'
                      }}>{req.status}</span>
                    </td>
                  </tr>
                ))}
              </tbody>
            </table>
            {requests.length === 0 && (
              <p style={{ color: 'var(--text-dim)', marginTop: '40px', textAlign: 'center' }}>
                No recent procurement requests found.
              </p>
            )}
          </div>
        </div>

        <div className="glass" style={{ padding: '30px' }}>
          <h3>Vendor Ratings</h3>
          <div style={{ marginTop: '20px' }}>
            {vendors.map(v => (
              <div key={v.id} style={{ marginBottom: '15px' }}>
                <div style={{ display: 'flex', justifyContent: 'space-between', marginBottom: '5px' }}>
                  <span>{v.companyName}</span>
                  <span style={{ color: '#10b981' }}>{v.rating || 0} ★</span>
                </div>
                <div style={{ height: '6px', background: 'rgba(255,255,255,0.05)', borderRadius: '3px' }}>
                  <div style={{ width: `${(v.rating || 0) * 20}%`, height: '100%', background: 'var(--primary)', borderRadius: '3px' }}></div>
                </div>
              </div>
            ))}
            {vendors.length === 0 && (
              <p style={{ color: 'var(--text-dim)', textAlign: 'center', marginTop: '20px' }}>
                No vendor data.
              </p>
            )}
          </div>
        </div>
      </div>
    </div>
  );
};

export default Dashboard;
