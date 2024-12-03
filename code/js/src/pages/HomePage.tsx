import React, { useState, useEffect } from 'react';
import {
    Box,
    Button,
    Typography,
    Dialog,
    DialogActions,
    DialogContent,
    DialogTitle,
    IconButton,
} from '@mui/material';
import { useNavigate } from 'react-router-dom';
import Navbar from "./components/NavBar";
import ContentCopyIcon from '@mui/icons-material/ContentCopy';
import { invite } from "../services/UsersService";
import { InviteOutputModel } from "../models/output/InviteOutputModel";

const HomePage: React.FC = () => {
    const [isAuthenticated] = useState(true); // Mock authentication state
    const [tokenDialogOpen, setTokenDialogOpen] = useState(false);
    const [token, setToken] = useState<string>(''); // Store token in state
    const navigate = useNavigate();

    if (!isAuthenticated) {
        navigate('/login');
        return null;
    }

    const fetchToken = async () => {
        const result = await invite();
        if (result.contentType === "application/json") {
            const successToken = result.json as InviteOutputModel;
            return successToken.invitationCode;
        } else {
            return '';
        }
    };

    // Open and close the dialog for the token
    const handleOpenTokenDialog = () =>{
        fetchToken().then(res => setToken(res));
        setTokenDialogOpen(true);
    }

    const handleCloseTokenDialog = () => setTokenDialogOpen(false);

    // Function to copy the token to the clipboard
    function handleCopyToken (){
        navigator.clipboard.writeText(token);
        alert('Token copied to clipboard!');
    };

    return (
        <Box display="flex" flexDirection="column" minHeight="100vh" justifyContent="center">
            <Navbar title="Home Page" canLogout={true} />

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
                <Button variant="contained" color="primary" fullWidth sx={{ height: 60 }} onClick={() => navigate('/channels')}>
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
                        <Typography variant="body1">{token || 'Loading token...'}</Typography>
                        {token && (
                            <IconButton onClick={handleCopyToken} color="primary" aria-label="copy token">
                                <ContentCopyIcon />
                            </IconButton>
                        )}
                    </Box>
                </DialogContent>
                <DialogActions>
                    <Button onClick={handleCloseTokenDialog} color="primary">
                        Close
                    </Button>
                </DialogActions>
            </Dialog>
        </Box>
    );
};

export default HomePage;
