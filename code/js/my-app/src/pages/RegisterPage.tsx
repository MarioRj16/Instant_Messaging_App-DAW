import React, { useState } from 'react';
import { Box, Button, TextField, Typography, Link, Alert } from '@mui/material';

const RegisterPage: React.FC = () => {
    const [username, setUsername] = useState('');
    const [password, setPassword] = useState('');
    const [confirmPassword, setConfirmPassword] = useState('');
    const [token,setToken] = useState('');
    const [error, setError] = useState('');

    const handleSubmit = (e: React.FormEvent) => {
        e.preventDefault();

        if (password !== confirmPassword) {
            setError("Passwords do not match.");
            return;
        }

        setError('');
        // Handle successful registration logic here
        console.log("Form submitted successfully!");
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
            <Box component="form" onSubmit={handleSubmit} sx={{ display: 'flex', flexDirection: 'column', gap: 2, width: '100%', maxWidth: 400 }}>
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
                    value={token}
                    onChange={(e) => setToken(e.target.value)}
                />
                {error && <Alert severity="error">{error}</Alert>}
                <Button type="submit" variant="contained" color="primary" fullWidth>
                    Register
                </Button>
            </Box>
            <Typography variant="body2" sx={{ marginTop: 2 }}>
                Already have an account?{' '}
                <Link href="/login" underline="hover">
                    Click here to Login
                </Link>
            </Typography>
        </Box>
    );
};

export default RegisterPage;
