import React from 'react';
import { BarChart3, TrendingUp, TrendingDown, DollarSign } from 'lucide-react';

const Analytics = () => {
  return (
    <div className="animate-fade-in">
      <h1 style={{ marginBottom: '30px' }}>Procurement Analytics</h1>

      <div style={{ display: 'grid', gridTemplateColumns: 'repeat(auto-fit, minmax(240px, 1fr))', gap: '20px', marginBottom: '30px' }}>
        <div className="glass" style={{ padding: '24px' }}>
          <div style={{ display: 'flex', justifyContent: 'space-between' }}>
            <div>
              <p style={{ color: 'var(--text-dim)', fontSize: '0.9rem' }}>Monthly Spend</p>
              <h2>$42,500</h2>
            </div>
            <TrendingUp color="#10b981" />
          </div>
          <p style={{ fontSize: '0.8rem', color: '#10b981', marginTop: '10px' }}>+12% from last month</p>
        </div>
        <div className="glass" style={{ padding: '24px' }}>
          <div style={{ display: 'flex', justifyContent: 'space-between' }}>
            <div>
              <p style={{ color: 'var(--text-dim)', fontSize: '0.9rem' }}>Savings Achieved</p>
              <h2>$8,200</h2>
            </div>
            <DollarSign color="#6366f1" />
          </div>
          <p style={{ fontSize: '0.8rem', color: 'var(--text-dim)', marginTop: '10px' }}>Based on quotation comparisons</p>
        </div>
        <div className="glass" style={{ padding: '24px' }}>
          <div style={{ display: 'flex', justifyContent: 'space-between' }}>
            <div>
              <p style={{ color: 'var(--text-dim)', fontSize: '0.9rem' }}>Avg. Lead Time</p>
              <h2>4.2 Days</h2>
            </div>
            <TrendingDown color="#10b981" />
          </div>
          <p style={{ fontSize: '0.8rem', color: '#10b981', marginTop: '10px' }}>-0.5 days improvement</p>
        </div>
      </div>

      <div style={{ display: 'grid', gridTemplateColumns: '1fr 1fr', gap: '30px' }}>
        <div className="glass" style={{ padding: '30px', minHeight: '400px' }}>
          <h3>Spend by Department</h3>
          <div style={{ marginTop: '30px', display: 'flex', flexDirection: 'column', gap: '20px' }}>
            {[
              { name: 'IT Infrastructure', value: 65 },
              { name: 'Office Supplies', value: 40 },
              { name: 'Marketing', value: 30 },
              { name: 'Maintenance', value: 15 }
            ].map(dept => (
              <div key={dept.name}>
                <div style={{ display: 'flex', justifyContent: 'space-between', marginBottom: '8px', fontSize: '0.9rem' }}>
                  <span>{dept.name}</span>
                  <span>{dept.value}%</span>
                </div>
                <div style={{ height: '8px', background: 'rgba(255,255,255,0.05)', borderRadius: '4px' }}>
                  <div style={{ width: `${dept.value}%`, height: '100%', background: 'var(--primary)', borderRadius: '4px' }}></div>
                </div>
              </div>
            ))}
          </div>
        </div>
        
        <div className="glass" style={{ padding: '30px' }}>
          <h3>Cost Trend (6 Months)</h3>
          <div style={{ marginTop: '40px', height: '250px', display: 'flex', alignItems: 'flex-end', gap: '20px', justifyContent: 'space-around' }}>
            {[30, 45, 35, 60, 50, 75].map((h, i) => (
              <div key={i} style={{ flex: 1, display: 'flex', flexDirection: 'column', alignItems: 'center', gap: '10px' }}>
                <div style={{ width: '100%', height: `${h}%`, background: 'var(--primary)', borderRadius: '6px 6px 0 0', opacity: 0.7 }}></div>
                <span style={{ fontSize: '0.75rem', color: 'var(--text-dim)' }}>Month {i+1}</span>
              </div>
            ))}
          </div>
        </div>
      </div>
    </div>
  );
};

export default Analytics;
