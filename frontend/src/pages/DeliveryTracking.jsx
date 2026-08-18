import React, { useState, useEffect } from 'react';
import api from '../services/api';
import { Truck, Package, MapPin, Calendar } from 'lucide-react';

const DeliveryTracking = () => {
  const [deliveries, setDeliveries] = useState([]);
  const [loading, setLoading] = useState(true);

  useEffect(() => {
    fetchDeliveries();
  }, []);

  const fetchDeliveries = async () => {
    try {
      const response = await api.get('/deliveries');
      setDeliveries(response.data);
    } catch (err) {
      console.warn('Backend API connection failed, using local mock Deliveries.');
      setDeliveries([
        { id: 1, purchaseOrder: { poNumber: 'PO-A92B3C' }, carrier: 'FedEx', trackingNumber: '1234567890', status: 'IN_TRANSIT', deliveryDate: null },
        { id: 2, purchaseOrder: { poNumber: 'PO-F8E1D2' }, carrier: 'DHL', trackingNumber: '9876543210', status: 'DELIVERED', deliveryDate: '2026-03-11' }
      ]);
    } finally {
      setLoading(false);
    }
  };

  if (loading) return <div style={{ padding: '40px', textAlign: 'center' }}>Loading Deliveries...</div>;

  return (
    <div className="animate-fade-in">
      <h1 style={{ marginBottom: '30px' }}>Delivery Tracking</h1>

      <div style={{ display: 'grid', gridTemplateColumns: '1fr', gap: '20px' }}>
        {deliveries.map(delivery => (
          <div key={delivery.id} className="glass" style={{ padding: '24px' }}>
            <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center' }}>
              <div style={{ display: 'flex', gap: '20px', alignItems: 'center' }}>
                <div style={{ padding: '15px', borderRadius: '12px', background: 'rgba(99,102,241,0.1)', color: 'var(--primary)' }}>
                  <Truck size={32} />
                </div>
                <div>
                  <h3 style={{ marginBottom: '5px' }}>{delivery.purchaseOrder?.poNumber || 'PO-N/A'}</h3>
                  <p style={{ color: 'var(--text-dim)', fontSize: '0.9rem' }}>Carrier: {delivery.carrier} | Tracking: {delivery.trackingNumber}</p>
                </div>
              </div>
              <div style={{ textAlign: 'right' }}>
                <span style={{ 
                  padding: '6px 16px', borderRadius: '20px', fontSize: '0.85rem', fontWeight: '600',
                  background: delivery.status === 'DELIVERED' ? '#10b98120' : '#6366f120',
                  color: delivery.status === 'DELIVERED' ? '#10b981' : '#6366f1'
                }}>{delivery.status}</span>
                <p style={{ marginTop: '10px', fontSize: '0.85rem', color: 'var(--text-dim)' }}>
                  Delivery: {delivery.deliveryDate ? new Date(delivery.deliveryDate).toLocaleDateString() : 'Expected Soon'}
                </p>
              </div>
            </div>

            <div style={{ marginTop: '30px', position: 'relative', height: '4px', background: 'rgba(255,255,255,0.05)', borderRadius: '2px' }}>
              <div style={{ 
                position: 'absolute', top: 0, left: 0, height: '100%', 
                width: delivery.status === 'DELIVERED' ? '100%' : delivery.status === 'IN_TRANSIT' ? '66%' : '33%', 
                background: 'var(--primary)', borderRadius: '2px' 
              }}></div>
              <div style={{ display: 'flex', justifyContent: 'space-between', marginTop: '15px', fontSize: '0.8rem', color: 'var(--text-dim)' }}>
                <span>Processing</span>
                <span>Shipped / In Transit</span>
                <span>Delivered</span>
              </div>
            </div>
          </div>
        ))}
      </div>
      {deliveries.length === 0 && (
        <div style={{ padding: '40px', textAlign: 'center', color: 'var(--text-dim)', background: 'rgba(255,255,255,0.01)', borderRadius: '12px', border: '1px solid var(--glass-border)' }}>
          No deliveries currently tracked.
        </div>
      )}
    </div>
  );
};

export default DeliveryTracking;
