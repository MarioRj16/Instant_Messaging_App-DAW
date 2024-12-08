import React, { useState } from 'react';
import { Box, Button, TextField, Typography, Link, Alert, CircularProgress } from '@mui/material';
import { login, register } from "../services/UsersService";
import { RegisterOutputModel } from "../models/output/RegisterOutputModel";
import { setAuthToken } from "../services/Utils/CookiesHandling";
import { LoginOutputModel } from "../models/output/LoginOutputModel";
import { useNavigate } from "react-router-dom";
import ErrorPopup from "./components/ErrorPopup";
import {ProblemModel} from "../models/ProblemModel"; // Import ErrorPopup component

const RegisterPage: React.FC = () => {
    const [username, setUsername] = useState('');
    const [password, setPassword] = useState('');
    const [confirmPassword, setConfirmPassword] = useState('');
    const [invitationCode, setInvitationCode] = useState('');
    const [error, setError] = useState('');
    const [loading, setLoading] = useState(false); // Loading state for button

    // States for ErrorPopup
    const [errorPopupOpen, setErrorPopupOpen] = useState(false);
    const [errorTitle, setErrorTitle] = useState('');
    const [errorDetails, setErrorDetails] = useState('');

    const navigate = useNavigate();

    const handleRegister = async (e: React.FormEvent) => {
        e.preventDefault();

        if (password !== confirmPassword) {
            setError("Passwords do not match.");
            return;
        }

        setError('');
        setLoading(true);

        try {
            // Register API call
            const registerResponse = await register({ username, password, invitationCode });

            if (registerResponse.contentType === "application/json") {
                    // Successful registration, proceed to login
                    const loginResponse = await login({ username, password });

                    if (loginResponse.contentType === "application/json") {
                        const token = loginResponse.json as LoginOutputModel;
                        setAuthToken(token.token);
                        navigate('/');
                    } else {
                        const loginError = loginResponse.json as ProblemModel;
                        setErrorTitle("Login Error");
                        setErrorDetails(loginError.detail || "An error occurred during login.");
                        setErrorPopupOpen(true);
                    }
                } else {
                    const registerError = registerResponse.json as ProblemModel;
                    setErrorTitle(registerError.title || "Registration Error");
                    setErrorDetails(registerError.detail || "An error occurred during registration.");
                    setErrorPopupOpen(true);
                }
        } catch (err) {
            console.error("Registration failed:", err);
            setErrorTitle("Network Error");
            setErrorDetails("A network error occurred. Please check your connection and try again.");
            setErrorPopupOpen(true);
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
                Register
            </Typography>
            <Box
                component="form"
                onSubmit={handleRegister}
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
                <TextField
                    label="Confirm Password"
                    name="confirmPassword"
                    type="password"
                    variant="outlined"
                    fullWidth
                    required
                    value={confirmPassword}
                    onChange={(e) => setConfirmPassword(e.target.value)}
                />
                <TextField
                    label="Registration Token"
                    name="token"
                    variant="outlined"
                    fullWidth
                    required
                    value={invitationCode}
                    onChange={(e) => setInvitationCode(e.target.value)}
                />
                {error && <Alert severity="error">{error}</Alert>}
                <Button
                    type="submit"
                    variant="contained"
                    color="primary"
                    fullWidth
                    disabled={loading}
                >
                    {loading ? <CircularProgress size={24} /> : "Register"}
                </Button>
            </Box>
            <Typography variant="body2" sx={{ marginTop: 2 }}>
                Already have an account?{' '}
                <Link href="/login" underline="hover">
                    Click here to Login
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

export default RegisterPage;
