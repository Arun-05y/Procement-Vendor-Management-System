import React, { useState } from 'react';
import { useNavigate, Link } from 'react-router-dom';
import axios from 'axios';
import { User, Mail, Lock, Building } from 'lucide-react';

const Register = () => {
  const [formData, setFormData] = useState({ username: '', email: '', password: '', role: ['user'] });
  const [error, setError] = useState('');
  const navigate = useNavigate();

  const handleSubmit = async (e) => {
    e.preventDefault();
    try {
      await axios.post('http://localhost:8080/api/auth/signup', formData);
      navigate('/login');
    } catch (err) {
      setError(err.response?.data?.message || 'Registration failed');
    }
  };

  return (
    <div className="animate-fade-in" style={{ display: 'flex', justifyContent: 'center', alignItems: 'center', minHeight: '100vh', padding: '20px' }}>
      <div className="glass" style={{ padding: '40px', width: '100%', maxWidth: '450px' }}>
        <h2 style={{ textAlign: 'center', marginBottom: '10px' }}>Create Account</h2>
        <p style={{ textAlign: 'center', color: 'var(--text-dim)', marginBottom: '30px' }}>Join the Smart Procurement Network</p>

        {error && <div style={{ color: 'var(--accent)', marginBottom: '20px', textAlign: 'center' }}>{error}</div>}

        <form onSubmit={handleSubmit}>
          <div className="input-group">
            <label className="input-label">Username</label>
            <div style={{ position: 'relative' }}>
              <User size={18} style={{ position: 'absolute', left: '12px', top: '12px', color: 'var(--text-dim)' }} />
              <input className="input-field" style={{ paddingLeft: '40px' }} type="text" onChange={e => setFormData({...formData, username: e.target.value})} required />
            </div>
          </div>

          <div className="input-group">
            <label className="input-label">Email Address</label>
            <div style={{ position: 'relative' }}>
              <Mail size={18} style={{ position: 'absolute', left: '12px', top: '12px', color: 'var(--text-dim)' }} />
              <input className="input-field" style={{ paddingLeft: '40px' }} type="email" onChange={e => setFormData({...formData, email: e.target.value})} required />
            </div>
          </div>

          <div className="input-group">
            <label className="input-label">Password</label>
            <div style={{ position: 'relative' }}>
              <Lock size={18} style={{ position: 'absolute', left: '12px', top: '12px', color: 'var(--text-dim)' }} />
              <input className="input-field" style={{ paddingLeft: '40px' }} type="password" onChange={e => setFormData({...formData, password: e.target.value})} required />
            </div>
          </div>

          <div className="input-group">
            <label className="input-label">I am a...</label>
            <div style={{ display: 'flex', gap: '10px' }}>
              <button type="button" onClick={() => setFormData({...formData, role: ['user']})} className={formData.role[0] === 'user' ? 'btn btn-primary' : 'btn btn-outline'} style={{ flex: 1, fontSize: '0.8rem' }}>Employee</button>
              <button type="button" onClick={() => setFormData({...formData, role: ['vendor']})} className={formData.role[0] === 'vendor' ? 'btn btn-primary' : 'btn btn-outline'} style={{ flex: 1, fontSize: '0.8rem' }}>Vendor</button>
            </div>
          </div>

          <button className="btn btn-primary" style={{ width: '100%', marginTop: '20px' }}>Sign Up</button>
        </form>

        <p style={{ marginTop: '20px', textAlign: 'center', color: 'var(--text-dim)' }}>
          Already have an account? <Link to="/login" style={{ color: 'var(--primary)', textDecoration: 'none' }}>Login</Link>
        </p>
      </div>
    </div>
  );
};

export default Register;
