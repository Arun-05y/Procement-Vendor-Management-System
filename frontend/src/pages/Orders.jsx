import React, { useState, useEffect } from 'react';
import api from '../services/api';
import { ShoppingBag, Printer, ExternalLink } from 'lucide-react';

const Orders = () => {
  const [orders, setOrders] = useState([]);
  const [loading, setLoading] = useState(true);

  useEffect(() => {
    fetchOrders();
  }, []);

  const fetchOrders = async () => {
    try {
      const response = await api.get('/purchase-orders');
      setOrders(response.data);
    } catch (err) {
      console.error('Error fetching purchase orders', err);
    } finally {
      setLoading(false);
    }
  };

  if (loading) return <div style={{ padding: '40px', textAlign: 'center' }}>Loading Orders...</div>;

  return (
    <div className="animate-fade-in">
      <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', marginBottom: '30px' }}>
        <h1>Purchase Orders</h1>
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
            {orders.map(order => (
              <tr key={order.id} style={{ borderBottom: '1px solid var(--glass-border)' }}>
                <td style={{ padding: '15px 20px', fontWeight: 'bold' }}>{order.poNumber}</td>
                <td style={{ padding: '15px 20px' }}>{order.quotation?.vendor?.companyName || 'N/A'}</td>
                <td style={{ padding: '15px 20px' }}>{order.issuedDate ? new Date(order.issuedDate).toLocaleDateString() : 'N/A'}</td>
                <td style={{ padding: '15px 20px' }}>${order.quotation?.totalAmount?.toLocaleString() || 0}</td>
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
        {orders.length === 0 && (
          <div style={{ padding: '40px', textAlign: 'center', color: 'var(--text-dim)' }}>
            No purchase orders found in the system.
          </div>
        )}
      </div>
    </div>
  );
};

export default Orders;
