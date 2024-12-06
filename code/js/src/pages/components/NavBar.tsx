import React from 'react';
import { AppBar, Toolbar, IconButton, Typography, Button } from '@mui/material';
// @ts-ignore
import ArrowBackIcon from '@mui/icons-material/ArrowBack';
// @ts-ignore
import SettingsIcon from '@mui/icons-material/Settings';
import { useNavigate, useLocation } from 'react-router-dom';
import {logout} from "../../services/UsersService";
import {removeAuthToken, removeCookie} from "../../services/Utils/CookiesHandling";

interface NavbarProps {
    title: string;
    onBackClick?: () => void;
    canLogout: boolean;
}

const Navbar: React.FC<NavbarProps> = ({ title, onBackClick, canLogout }) => {
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
            if (location.pathname.includes('/settings')) {
                navigate(`/channels/${channelId}`);
            } else {
                navigate(`/channels/${channelId}/settings`);
            }
        }
    };

    const onLogoutClick = () => {
        if (canLogout) {
           logout().then(r => {
               removeAuthToken()
               removeCookie("username")
               removeCookie("userId")
               navigate(`/login`)
           })
        }
    }

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
