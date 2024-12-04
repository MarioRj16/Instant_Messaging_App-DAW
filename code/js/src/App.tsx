import React from 'react';
import { Route, Routes } from 'react-router-dom';
import LoginPage from './pages/LoginPage';
import HomePage from './pages/HomePage';
import RegisterPage from './pages/RegisterPage';
import YourChannels from './pages/YourChannelsPage';
import ChannelPage from './pages/ChannelPage';
import InvitationsPage from './pages/InvitationsPage';
import SearchChannelsPage from './pages/SearchChannelsPage';
import CreateChannelPage from './pages/CreateChannelPage';
import ChannelSettingsPage from './pages/ChannelSettingsPage';
import NotFoundPage from "./pages/NotFoundPage";
import ProtectedRoute from './ProtectedRoute';
import './App.css';

function App() {
    return (
        <div className="App-content">
            <Routes>
                {/* Public Routes */}
                <Route path="/login" element={<LoginPage />} />
                <Route path="/register" element={<RegisterPage />} />

                {/* Protected Routes */}
                <Route
                    path="/"
                    element={
                        <ProtectedRoute>
                            <HomePage />
                        </ProtectedRoute>
                    }
                />
                <Route
                    path="/channels"
                    element={
                        <ProtectedRoute>
                            <YourChannels />
                        </ProtectedRoute>
                    }
                >
                    <Route
                        path=":id"
                        element={
                            <ProtectedRoute>
                                <ChannelPage />
                            </ProtectedRoute>
                        }
                    >
                        <Route
                            path="settings"
                            element={
                                <ProtectedRoute>
                                    <ChannelSettingsPage />
                                </ProtectedRoute>
                            }
                        />
                    </Route>
                </Route>
                <Route
                    path="/search-channels"
                    element={
                        <ProtectedRoute>
                            <SearchChannelsPage />
                        </ProtectedRoute>
                    }
                />
                <Route
                    path="/create-channel"
                    element={
                        <ProtectedRoute>
                            <CreateChannelPage />
                        </ProtectedRoute>
                    }
                />
                <Route
                    path="/invitations"
                    element={
                        <ProtectedRoute>
                            <InvitationsPage />
                        </ProtectedRoute>
                    }
                />
                <Route path="*" element={<NotFoundPage />} />

            </Routes>
        </div>
    );
}

export default App;
