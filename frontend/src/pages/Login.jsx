import React, { useState } from 'react';
import { useAuth } from '../context/AuthContext';
import { useNavigate, Link } from 'react-router-dom';
import axios from 'axios';
import { Lock, User as UserIcon } from 'lucide-react';

const Login = () => {
  const [username, setUsername] = useState('');
  const [password, setPassword] = useState('');
  const [error, setError] = useState('');
  const { login } = useAuth();
  const navigate = useNavigate();

  const handleSubmit = async (e) => {
    e.preventDefault();
    try {
      const response = await axios.post('http://localhost:8080/api/auth/signin', { username, password });
      login(response.data);
      navigate('/dashboard');
    } catch (err) {
      setError('Invalid credentials');
    }
  };

  return (
    <div className="login-container animate-fade-in" style={{
      display: 'flex', justifyContent: 'center', alignItems: 'center', height: '100vh'
    }}>
      <div className="glass login-card" style={{ padding: '40px', width: '400px' }}>
        <h2 style={{ marginBottom: '10px', textAlign: 'center' }}>Welcome Back</h2>
        <p style={{ color: 'var(--text-dim)', textAlign: 'center', marginBottom: '30px' }}>
          Please enter your details to sign in
        </p>

        {error && <div style={{ color: 'var(--accent)', marginBottom: '20px', textAlign: 'center' }}>{error}</div>}

        <form onSubmit={handleSubmit}>
          <div className="input-group">
            <label className="input-label">Username</label>
            <div style={{ position: 'relative' }}>
              <UserIcon size={18} style={{ position: 'absolute', left: '12px', top: '12px', color: 'var(--text-dim)' }} />
              <input 
                className="input-field" 
                style={{ paddingLeft: '40px' }}
                type="text" 
                value={username}
                onChange={(e) => setUsername(e.target.value)}
                required
              />
            </div>
          </div>

          <div className="input-group">
            <label className="input-label">Password</label>
            <div style={{ position: 'relative' }}>
              <Lock size={18} style={{ position: 'absolute', left: '12px', top: '12px', color: 'var(--text-dim)' }} />
              <input 
                className="input-field" 
                style={{ paddingLeft: '40px' }}
                type="password" 
                value={password}
                onChange={(e) => setPassword(e.target.value)}
                required
              />
            </div>
          </div>

          <button className="btn btn-primary" style={{ width: '100%', marginTop: '10px' }}>Sign In</button>
        </form>

        <p style={{ marginTop: '20px', textAlign: 'center', color: 'var(--text-dim)' }}>
          Don't have an account? <Link to="/register" style={{ color: 'var(--primary)', textDecoration: 'none' }}>Register</Link>
        </p>
      </div>
    </div>
  );
};

export default Login;
