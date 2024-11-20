// src/components/Navbar.tsx

import React from 'react';
import { AppBar, Toolbar, IconButton, Typography, Button } from '@mui/material';
// @ts-ignore
import ArrowBackIcon from '@mui/icons-material/ArrowBack';
import { useNavigate } from 'react-router-dom';

interface NavbarProps {
    title: string;
    onBackClick?: () => void;
    onLogoutClick: () => void;
    icon?: React.ReactNode; // Optional icon component, e.g., <SettingsIcon />
    onIconClick?: () => void; // Click handler for the optional icon
}

const Navbar: React.FC<NavbarProps> = ({ title, onBackClick, onLogoutClick, icon, onIconClick }) => {
    const navigate = useNavigate();

    const handleBack = () => {
        if (onBackClick) {
            onBackClick();
        } else {
            navigate(-1); // Go back one step if no custom back function is provided
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

                {/* Optional Icon Button (e.g., Settings) */}
                {icon && (
                    <IconButton color="inherit" onClick={onIconClick}>
                        {icon}
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
