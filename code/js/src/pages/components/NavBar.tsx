import React from 'react';
import { AppBar, Toolbar, IconButton, Typography, Button } from '@mui/material';
// @ts-ignore
import ArrowBackIcon from '@mui/icons-material/ArrowBack';
// @ts-ignore
import SettingsIcon from '@mui/icons-material/Settings';
import { useNavigate, useLocation } from 'react-router-dom';
import { logout } from "../../services/UsersService";
import { removeAuthToken, removeCookie } from "../../services/Utils/CookiesHandling";

interface NavbarProps {
    title: string;
    canLogout: boolean;
}

const Navbar: React.FC<NavbarProps> = ({ title, canLogout }) => {
    const navigate = useNavigate();
    const location = useLocation();

    // Split the current pathname into segments
    const pathSegments = location.pathname.split('/').filter(segment => segment);

    // Determine the parent path
    const parentPath =
        pathSegments.length > 1
            ? `/${pathSegments.slice(0, -1).join('/')}` // Remove the last segment
            : '/'; // If only one segment, fallback to root "/"

    const handleBack = () => {
        navigate(parentPath); // Navigate to the parent path
    };

    const handleSettingsClick = () => {
        const channelId = pathSegments[1]; // Assuming "/channels/:id/settings"
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
            logout().then(() => {
                removeAuthToken();
                removeCookie("username");
                removeCookie("userId");
                navigate(`/login`);
            });
        }
    };

    return (
        <AppBar position="sticky">
            <Toolbar>
                {/* Back button is hidden only if the current path is the root ("/") */}
                {location.pathname !== '/' && (
                    <IconButton edge="start" color="inherit" onClick={handleBack} aria-label="back">
                        <ArrowBackIcon />
                    </IconButton>
                )}

                <Typography variant="h6" component="div" sx={{ flexGrow: 1, textAlign: 'center' }}>
                    {title}
                </Typography>

                {/* Conditionally render the Settings icon */}
                {location.pathname.includes('/channels') && (
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
