import React, { useState, useEffect } from 'react';
import api from '../services/api';
import { BarChart3, TrendingUp, DollarSign, Clock } from 'lucide-react';

const Analytics = () => {
  const [stats, setStats] = useState(null);
  const [loading, setLoading] = useState(true);

  useEffect(() => {
    fetchSummary();
  }, []);

  const fetchSummary = async () => {
    try {
      const response = await api.get('/analytics/summary');
      setStats(response.data);
    } catch (err) {
      console.warn('Backend API connection failed, using local mock Analytics.');
      setStats({
        totalSpend: 2540000,
        totalSavings: 420000,
        avgLeadTime: 4.2,
        spendByDepartment: {
          'IT Infrastructure': 1650000,
          'Office Supplies': 400000,
          'Marketing': 300000,
          'Maintenance': 190000
        },
        costTrends: {
          'March': 450000,
          'April': 350000,
          'May': 600000,
          'June': 500000,
          'July': 640000
        }
      });
    } finally {
      setLoading(false);
    }
  };

  if (loading) return <div style={{ padding: '40px', textAlign: 'center' }}>Loading Analytics...</div>;

  const costTrends = stats?.costTrends || {};
  const spendByDept = stats?.spendByDepartment || {};
  const maxTrendVal = Math.max(...Object.values(costTrends), 1);

  return (
    <div className="animate-fade-in">
      <h1 style={{ marginBottom: '30px' }}>Procurement Analytics</h1>

      <div style={{ display: 'grid', gridTemplateColumns: 'repeat(auto-fit, minmax(240px, 1fr))', gap: '20px', marginBottom: '30px' }}>
        <div className="glass" style={{ padding: '24px' }}>
          <div style={{ display: 'flex', justifyContent: 'space-between' }}>
            <div>
              <p style={{ color: 'var(--text-dim)', fontSize: '0.9rem' }}>Total Spend</p>
              <h2>${stats?.totalSpend?.toLocaleString() || 0}</h2>
            </div>
            <DollarSign color="#10b981" />
          </div>
          <p style={{ fontSize: '0.8rem', color: 'var(--text-dim)', marginTop: '10px' }}>Total PO disbursements</p>
        </div>
        <div className="glass" style={{ padding: '24px' }}>
          <div style={{ display: 'flex', justifyContent: 'space-between' }}>
            <div>
              <p style={{ color: 'var(--text-dim)', fontSize: '0.9rem' }}>Savings Achieved</p>
              <h2>${stats?.totalSavings?.toLocaleString() || 0}</h2>
            </div>
            <TrendingUp color="#6366f1" />
          </div>
          <p style={{ fontSize: '0.8rem', color: '#10b981', marginTop: '10px' }}>From quotation competition</p>
        </div>
        <div className="glass" style={{ padding: '24px' }}>
          <div style={{ display: 'flex', justifyContent: 'space-between' }}>
            <div>
              <p style={{ color: 'var(--text-dim)', fontSize: '0.9rem' }}>Avg. Lead Time</p>
              <h2>{stats?.avgLeadTime ? stats.avgLeadTime.toFixed(1) : 0} Days</h2>
            </div>
            <Clock color="#f59e0b" />
          </div>
          <p style={{ fontSize: '0.8rem', color: 'var(--text-dim)', marginTop: '10px' }}>From PO issue to delivery</p>
        </div>
      </div>

      <div style={{ display: 'grid', gridTemplateColumns: '1fr 1fr', gap: '30px' }}>
        <div className="glass" style={{ padding: '30px', minHeight: '400px' }}>
          <h3>Spend by Department</h3>
          <div style={{ marginTop: '30px', display: 'flex', flexDirection: 'column', gap: '20px' }}>
            {Object.entries(spendByDept).map(([dept, value]) => {
              const pct = stats.totalSpend > 0 ? (value / stats.totalSpend) * 100 : 0;
              return (
                <div key={dept}>
                  <div style={{ display: 'flex', justifyContent: 'space-between', marginBottom: '8px', fontSize: '0.9rem' }}>
                    <span>{dept}</span>
                    <span>${value.toLocaleString()} ({pct.toFixed(0)}%)</span>
                  </div>
                  <div style={{ height: '8px', background: 'rgba(255,255,255,0.05)', borderRadius: '4px' }}>
                    <div style={{ width: `${pct}%`, height: '100%', background: 'var(--primary)', borderRadius: '4px' }}></div>
                  </div>
                </div>
              );
            })}
            {Object.keys(spendByDept).length === 0 && (
              <p style={{ color: 'var(--text-dim)', textAlign: 'center', marginTop: '40px' }}>No department spend data available.</p>
            )}
          </div>
        </div>
        
        <div className="glass" style={{ padding: '30px' }}>
          <h3>Cost Trend (Monthly)</h3>
          <div style={{ marginTop: '40px', height: '250px', display: 'flex', alignItems: 'flex-end', gap: '20px', justifyContent: 'space-around' }}>
            {Object.entries(costTrends).map(([month, val]) => {
              const h = (val / maxTrendVal) * 80; // Normalize height (max 80%)
              return (
                <div key={month} style={{ flex: 1, display: 'flex', flexDirection: 'column', alignItems: 'center', gap: '10px', height: '100%', justifyContent: 'flex-end' }}>
                  <div style={{ width: '100%', height: `${Math.max(h, 5)}%`, background: 'var(--primary)', borderRadius: '6px 6px 0 0', opacity: 0.7 }} title={`$${val.toLocaleString()}`}></div>
                  <span style={{ fontSize: '0.75rem', color: 'var(--text-dim)', whiteSpace: 'nowrap', overflow: 'hidden', textOverflow: 'ellipsis', width: '100%', textAlign: 'center' }}>{month}</span>
                </div>
              );
            })}
            {Object.keys(costTrends).length === 0 && (
              <p style={{ color: 'var(--text-dim)', textAlign: 'center', width: '100%', marginBottom: '100px' }}>No trend data available.</p>
            )}
          </div>
        </div>
      </div>
    </div>
  );
};

export default Analytics;
