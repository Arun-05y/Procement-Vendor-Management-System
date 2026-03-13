import React from 'react';
import { BrowserRouter as Router, Routes, Route, Navigate } from 'react-router-dom';
import { AuthProvider, useAuth } from './context/AuthContext';
import Login from './pages/Login';
import Register from './pages/Register';
import Dashboard from './pages/Dashboard';
import Vendors from './pages/Vendors';
import ProcurementRequests from './pages/ProcurementRequests';
import RFQManagement from './pages/RFQManagement';
import Analytics from './pages/Analytics';
import Orders from './pages/Orders';
import VendorProfile from './pages/VendorProfile';
import DeliveryTracking from './pages/DeliveryTracking';
import Layout from './components/Layout';
import './index.css';

const ProtectedRoute = ({ children }) => {
  const { user, loading } = useAuth();
  if (loading) return <div>Loading...</div>;
  if (!user) return <Navigate to="/login" />;
  return <Layout>{children}</Layout>;
};

function App() {
  return (
    <AuthProvider>
      <Router>
        <Routes>
          <Route path="/login" element={<Login />} />
          <Route path="/register" element={<Register />} />
          <Route path="/dashboard" element={
            <ProtectedRoute>
              <Dashboard />
            </ProtectedRoute>
          } />
          <Route path="/analytics" element={
            <ProtectedRoute>
              <Analytics />
            </ProtectedRoute>
          } />
          <Route path="/profile" element={
            <ProtectedRoute>
              <VendorProfile />
            </ProtectedRoute>
          } />
          <Route path="/rfq" element={
            <ProtectedRoute>
              <RFQManagement />
            </ProtectedRoute>
          } />
          <Route path="/requests" element={
            <ProtectedRoute>
              <ProcurementRequests />
            </ProtectedRoute>
          } />
          <Route path="/deliveries" element={
            <ProtectedRoute>
              <DeliveryTracking />
            </ProtectedRoute>
          } />
          <Route path="/orders" element={
            <ProtectedRoute>
              <Orders />
            </ProtectedRoute>
          } />
          <Route path="/vendors" element={
            <ProtectedRoute>
              <Vendors />
            </ProtectedRoute>
          } />
          <Route path="/" element={<Navigate to="/dashboard" />} />
        </Routes>
      </Router>
    </AuthProvider>
  );
}

export default App;
