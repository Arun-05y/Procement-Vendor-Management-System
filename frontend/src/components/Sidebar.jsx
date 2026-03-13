import React from 'react';
import { NavLink, useNavigate } from 'react-router-dom';
import { useAuth } from '../context/AuthContext';
import { 
  LayoutDashboard, 
  Users, 
  FileText, 
  ShoppingBag, 
  BarChart3, 
  LogOut,
  Package,
  Truck
} from 'lucide-react';

const Sidebar = () => {
  const { user, logout } = useAuth();
  const navigate = useNavigate();

  const handleLogout = () => {
    logout();
    navigate('/login');
  };

  const navItems = [
    { name: 'Dashboard', icon: <LayoutDashboard size={20} />, path: '/dashboard' },
    { name: 'Profile', icon: <Users size={20} />, path: '/profile', roles: ['ROLE_VENDOR'] },
    { name: 'Vendors', icon: <Users size={20} />, path: '/vendors', roles: ['ROLE_ADMIN', 'ROLE_PROCUREMENT_OFFICER'] },
    { name: 'Requests', icon: <FileText size={20} />, path: '/requests' },
    { name: 'RFQ', icon: <Package size={20} />, path: '/rfq' },
    { name: 'Orders', icon: <ShoppingBag size={20} />, path: '/orders' },
    { name: 'Tracking', icon: <Truck size={20} />, path: '/deliveries' },
    { name: 'Analytics', icon: <BarChart3 size={20} />, path: '/analytics', roles: ['ROLE_ADMIN'] },
  ];

  const filteredNav = navItems.filter(item => 
    !item.roles || item.roles.some(role => user?.roles.includes(role))
  );

  return (
    <div className="glass sidebar" style={{
      width: '260px', height: 'calc(100vh - 40px)', margin: '20px',
      display: 'flex', flexDirection: 'column', padding: '20px'
    }}>
      <div className="logo" style={{ marginBottom: '40px', padding: '10px' }}>
        <h2 style={{ color: 'var(--primary)', display: 'flex', alignItems: 'center', gap: '10px' }}>
          <ShoppingBag /> ProcurEA
        </h2>
      </div>

      <div className="nav-links" style={{ flex: 1 }}>
        {filteredNav.map(item => (
          <NavLink 
            key={item.path} 
            to={item.path}
            className={({ isActive }) => isActive ? 'nav-item active' : 'nav-item'}
            style={({ isActive }) => ({
              display: 'flex', alignItems: 'center', gap: '12px',
              padding: '12px 16px', borderRadius: '10px',
              color: isActive ? 'white' : 'var(--text-dim)',
              background: isActive ? 'var(--primary)' : 'transparent',
              textDecoration: 'none', marginBottom: '8px',
              transition: 'all 0.3s ease'
            })}
          >
            {item.icon} <span>{item.name}</span>
          </NavLink>
        ))}
      </div>

      <div className="user-profile" style={{
        marginTop: 'auto', padding: '15px', borderTop: '1px solid var(--glass-border)',
        display: 'flex', alignItems: 'center', justifyContent: 'space-between'
      }}>
        <div style={{ overflow: 'hidden' }}>
          <p style={{ fontSize: '0.9rem', fontWeight: '600' }}>{user?.username}</p>
          <p style={{ fontSize: '0.75rem', color: 'var(--text-dim)' }}>{user?.roles[0]?.replace('ROLE_', '')}</p>
        </div>
        <button onClick={handleLogout} style={{ 
          background: 'none', border: 'none', color: 'var(--text-dim)', cursor: 'pointer' 
        }}>
          <LogOut size={18} />
        </button>
      </div>
    </div>
  );
};

export default Sidebar;
