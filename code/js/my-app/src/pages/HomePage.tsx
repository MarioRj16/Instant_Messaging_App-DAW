import React, { useState } from 'react';
import { Box, Button, Typography } from '@mui/material';
import { useNavigate } from 'react-router-dom';
import Navbar from "./components/NavBar";

const HomePage: React.FC = () => {
    const [isAuthenticated] = useState(true); // Mock authentication state
    const navigate = useNavigate();

    if (!isAuthenticated) {
        navigate('/login');
        return null;
    }

    const handleLogout = () => {
        // TODO: Implement logout functionality
        navigate('/login');
    };

    return (
        <Box
            display="flex"
            flexDirection="column"
            minHeight="100vh"
            justifyContent="center"
        >
            <Navbar title="Home Page" onLogoutClick={handleLogout} />

            <Box
                display="flex"
                flexDirection="column"
                alignItems="center"
                justifyContent="center"
                gap={2}
                width="80%"
                mx="auto"
                flexGrow={1}
            >
                <Button variant="contained" color="primary" fullWidth sx={{ height: 60 }} onClick={() => navigate('/your-channels')}>
                    Your Channels
                </Button>
                <Button variant="contained" color="primary" fullWidth sx={{ height: 60 }} onClick={() => navigate('/search-channels')}>
                    Search Channels
                </Button>
                <Button variant="contained" color="primary" fullWidth sx={{ height: 60 }} onClick={() => navigate('/create-channel')}>
                    Create Channel
                </Button>
                <Button variant="contained" color="primary" fullWidth sx={{ height: 60 }} onClick={() => navigate('/invitations')}>
                    Invitations
                </Button>


            </Box>
        </Box>
    );
};
/*
                <Button variant="contained" color="primary" fullWidth sx={{ height: 60 }} onClick={() => navigate('/about')}>
                    About
                </Button>

 */
export default HomePage;
