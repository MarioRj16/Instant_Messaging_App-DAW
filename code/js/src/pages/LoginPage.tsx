import React, { useState } from 'react';
import { Box, Button, TextField, Typography, Link, CircularProgress } from '@mui/material';
import { login } from "../services/UsersService";
import { setAuthToken } from "../services/Utils/CookiesHandling";
import { LoginOutputModel } from "../models/output/LoginOutputModel";
import { useNavigate } from "react-router-dom";
import ErrorPopup from "./components/ErrorPopup";
import {ProblemModel} from "../models/ProblemModel"; // Import ErrorPopup component

const LoginPage: React.FC = () => {
    const navigate = useNavigate();

    const [username, setUsername] = useState('');
    const [password, setPassword] = useState('');
    const [loading, setLoading] = useState(false);

    // States for ErrorPopup
    const [errorPopupOpen, setErrorPopupOpen] = useState(false);
    const [errorTitle, setErrorTitle] = useState('');
    const [errorDetails, setErrorDetails] = useState('');

    const handleLogin = async (e: React.FormEvent) => {
        e.preventDefault();
        setLoading(true);

        try {
            const response = await login({ username, password });

            if (response.contentType === "application/json") {
                    const token = response.json as LoginOutputModel;
                    setAuthToken(token.token);
                    navigate('/');
                } else {
                    // Handle error response in ProblemModel format
                    const errorData = response.json as ProblemModel;
                    setErrorTitle(errorData.title);
                    setErrorDetails(errorData.detail);
                    setErrorPopupOpen(true); // Show ErrorPopup
                }
        } catch (error) {
            console.error("Login failed:", error);
            setErrorTitle("Network Error");
            setErrorDetails("A network error occurred. Please check your connection and try again.");
            setErrorPopupOpen(true); // Show ErrorPopup
        } finally {
            setLoading(false);
        }
    };

    return (
        <Box
            display="flex"
            flexDirection="column"
            alignItems="center"
            justifyContent="center"
            minHeight="100vh"
        >
            <Typography variant="h4" component="h1" gutterBottom>
                Login
            </Typography>
            <Box
                component="form"
                onSubmit={handleLogin}
                sx={{ display: 'flex', flexDirection: 'column', gap: 2, width: '100%', maxWidth: 400 }}
            >
                <TextField
                    label="Username"
                    name="username"
                    variant="outlined"
                    fullWidth
                    required
                    value={username}
                    onChange={(e) => setUsername(e.target.value)}
                />
                <TextField
                    label="Password"
                    name="password"
                    type="password"
                    variant="outlined"
                    fullWidth
                    required
                    value={password}
                    onChange={(e) => setPassword(e.target.value)}
                />
                <Button
                    type="submit"
                    variant="contained"
                    color="primary"
                    fullWidth
                    disabled={loading}
                >
                    {loading ? <CircularProgress size={24} /> : "Login"}
                </Button>
            </Box>
            <Typography variant="body2" sx={{ marginTop: 2 }}>
                Don’t have an account?{' '}
                <Link href="/register" underline="hover">
                    Click here to register
                </Link>
            </Typography>

            {/* ErrorPopup Component */}
            <ErrorPopup
                open={errorPopupOpen}
                title={errorTitle}
                details={errorDetails}
                onClose={() => setErrorPopupOpen(false)} // Close the popup
            />
        </Box>
    );
};

export default LoginPage;
