import React from 'react';
import { Route, Routes } from 'react-router-dom';
// @ts-ignore
import LoginPage from './pages/LoginPage.tsx';
// @ts-ignore
import HomePage from './pages/HomePage.tsx';
import RegisterPage from './pages/RegisterPage';
import YourChannels from './pages/YourChannelsPage';
import ChannelPage from './pages/ChannelPage';
import InvitationsPage from './pages/InvitationsPage';
import SearchChannelsPage from './pages/SearchChannelsPage';
import CreateChannelPage from './pages/CreateChannelPage';
import ChannelSettingsPage from './pages/ChannelSettingsPage';
import './App.css';

function App() {
    return (
      <div className="App-content">
          <Routes>
              <Route path="/" element={<HomePage />} />
              <Route path="/login" element={<LoginPage />} />
              <Route path="/register" element={<RegisterPage />} />
              <Route path="/channels" element={<YourChannels />}>

                  <Route path=":id" element={<ChannelPage />}>

                      <Route path="settings" element={<ChannelSettingsPage />} />

                  </Route>

              </Route>
              <Route path="/search-channels" element={<SearchChannelsPage />} />
              <Route path="/create-channel" element={<CreateChannelPage />} />
              <Route path="/invitations" element={<InvitationsPage />} />
          </Routes>
      </div>
    );
}

export default App;
