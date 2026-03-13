import React from 'react';
import { ShoppingBag, Printer, ExternalLink } from 'lucide-react';

const Orders = () => {
  const mockOrders = [
    { id: 1, poNumber: 'PO-A92B3C', vendor: 'Global Supplies', date: '2026-03-12', amount: 5400, status: 'SHIPPED' },
    { id: 2, poNumber: 'PO-F8E1D2', vendor: 'Tech Corp', date: '2026-03-10', amount: 12500, status: 'DELIVERED' },
    { id: 3, poNumber: 'PO-3C2B1A', vendor: 'Office World', date: '2026-03-13', amount: 850, status: 'ISSUED' },
  ];

  return (
    <div className="animate-fade-in">
      <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', marginBottom: '30px' }}>
        <h1>Purchase Orders</h1>
        <button className="btn btn-primary" style={{ display: 'flex', alignItems: 'center', gap: '8px' }}>
          <ShoppingBag size={20} /> New Order
        </button>
      </div>

      <div className="glass" style={{ overflow: 'hidden' }}>
        <table style={{ width: '100%', borderCollapse: 'collapse', textAlign: 'left' }}>
          <thead>
            <tr style={{ background: 'rgba(255,255,255,0.03)', borderBottom: '1px solid var(--glass-border)' }}>
              <th style={{ padding: '15px 20px' }}>PO Number</th>
              <th style={{ padding: '15px 20px' }}>Vendor</th>
              <th style={{ padding: '15px 20px' }}>Date</th>
              <th style={{ padding: '15px 20px' }}>Amount</th>
              <th style={{ padding: '15px 20px' }}>Status</th>
              <th style={{ padding: '15px 20px' }}>Actions</th>
            </tr>
          </thead>
          <tbody>
            {mockOrders.map(order => (
              <tr key={order.id} style={{ borderBottom: '1px solid var(--glass-border)' }}>
                <td style={{ padding: '15px 20px', fontWeight: 'bold' }}>{order.poNumber}</td>
                <td style={{ padding: '15px 20px' }}>{order.vendor}</td>
                <td style={{ padding: '15px 20px' }}>{order.date}</td>
                <td style={{ padding: '15px 20px' }}>${order.amount?.toLocaleString()}</td>
                <td style={{ padding: '15px 20px' }}>
                  <span style={{ 
                    padding: '4px 10px', borderRadius: '12px', fontSize: '0.75rem',
                    background: order.status === 'DELIVERED' ? '#10b98120' : order.status === 'SHIPPED' ? '#6366f120' : '#f59e0b20',
                    color: order.status === 'DELIVERED' ? '#10b981' : order.status === 'SHIPPED' ? '#6366f1' : '#f59e0b'
                  }}>{order.status}</span>
                </td>
                <td style={{ padding: '15px 20px' }}>
                  <div style={{ display: 'flex', gap: '15px' }}>
                    <button style={{ background: 'none', border: 'none', color: 'var(--text-dim)', cursor: 'pointer' }}><Printer size={18} /></button>
                    <button style={{ background: 'none', border: 'none', color: 'var(--text-dim)', cursor: 'pointer' }}><ExternalLink size={18} /></button>
                  </div>
                </td>
              </tr>
            ))}
          </tbody>
        </table>
      </div>
    </div>
  );
};

export default Orders;
