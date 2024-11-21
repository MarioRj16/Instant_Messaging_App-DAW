import React from 'react';
import { AppBar, Toolbar, IconButton, Typography, Button } from '@mui/material';
// @ts-ignore
import ArrowBackIcon from '@mui/icons-material/ArrowBack';
// @ts-ignore
import SettingsIcon from '@mui/icons-material/Settings';
import { useNavigate, useLocation } from 'react-router-dom';

interface NavbarProps {
    title: string;
    onBackClick?: () => void;
    onLogoutClick: () => void;
}

const Navbar: React.FC<NavbarProps> = ({ title, onBackClick, onLogoutClick }) => {
    const navigate = useNavigate();
    const location = useLocation();

    const handleBack = () => {
        if (onBackClick) {
            onBackClick();
        } else {
            navigate(-1); // Go back one step if no custom back function is provided
        }
    };

    // Check if the current route matches `/channels/:id`
    const isChannelPage = /\/channels\/\d+/.test(location.pathname);

    // Extract the channel ID from the URL (assuming numeric IDs)
    const channelId = location.pathname.match(/\/channels\/(\d+)/)?.[1];

    const handleSettingsClick = () => {
        if (channelId) {
            // If we're already in settings, navigate back to the channel page without '/settings'
            if (location.pathname.includes('/settings')) {
                navigate(`/channels/${channelId}`);
            } else {
                // Otherwise, navigate to the settings page
                navigate(`/channels/${channelId}/settings`);
            }
        }
    };

    return (
        <AppBar position="sticky">
            <Toolbar>
                <IconButton edge="start" color="inherit" onClick={handleBack} aria-label="back">
                    <ArrowBackIcon />
                </IconButton>

                <Typography variant="h6" component="div" sx={{ flexGrow: 1, textAlign: 'center' }}>
                    {title}
                </Typography>

                {/* Conditionally render the Settings icon */}
                {isChannelPage && (
                    <IconButton color="inherit" onClick={handleSettingsClick}>
                        <SettingsIcon />
                    </IconButton>
                )}

                <Button color="inherit" onClick={onLogoutClick}>
                    Logout
                </Button>
            </Toolbar>
        </AppBar>
    );
};

export default Navbar;
