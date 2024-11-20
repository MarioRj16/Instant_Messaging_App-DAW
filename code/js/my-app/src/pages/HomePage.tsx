import React, { useState } from 'react';
import { Box, Button, Typography, Dialog, DialogActions, DialogContent, DialogTitle, IconButton } from '@mui/material';
import { useNavigate } from 'react-router-dom';
import Navbar from "./components/NavBar";
import ContentCopyIcon from '@mui/icons-material/ContentCopy';

const HomePage: React.FC = () => {
    const [isAuthenticated] = useState(true); // Mock authentication state
    const [tokenDialogOpen, setTokenDialogOpen] = useState(false);
    const [token] = useState(() => generateToken()); // Generate a mock token once
    const navigate = useNavigate();

    if (!isAuthenticated) {
        navigate('/login');
        return null;
    }

    const handleLogout = () => {
        navigate('/login');
    };

    // Open and close the dialog for the token
    const handleOpenTokenDialog = () => setTokenDialogOpen(true);
    const handleCloseTokenDialog = () => setTokenDialogOpen(false);

    // Generate a mock token (replace this with your token generation logic if needed)
    function generateToken() {
        return '12345-abcde-67890-fghij'; // Mock token
    }

    // Function to copy the token to the clipboard
    const handleCopyToken = () => {
        navigator.clipboard.writeText(token);
        alert('Token copied to clipboard!');
    };

    return (
        <Box display="flex" flexDirection="column" minHeight="100vh" justifyContent="center">
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

                {/* New Button for Token Dialog */}
                <Button variant="contained" color="primary" fullWidth sx={{ height: 60 }} onClick={handleOpenTokenDialog}>
                    Create Registration Invite
                </Button>
            </Box>

            <Dialog open={tokenDialogOpen} onClose={handleCloseTokenDialog}>
                <DialogTitle align="center">Your Token</DialogTitle>
                <DialogContent>
                    <Box display="flex" alignItems="center" gap={1}>
                        <Typography variant="body1">{token}</Typography>
                        <IconButton onClick={handleCopyToken} color="primary" aria-label="copy token">
                            <ContentCopyIcon />
                        </IconButton>
                    </Box>
                </DialogContent>
                <DialogActions>
                    <Button onClick={handleCloseTokenDialog} color="primary">Close</Button>
                </DialogActions>
            </Dialog>
        </Box>
    );
};

export default HomePage;
