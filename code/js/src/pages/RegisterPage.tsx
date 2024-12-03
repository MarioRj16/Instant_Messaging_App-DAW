import React, { useState } from 'react';
import { Box, Button, TextField, Typography, Link, Alert } from '@mui/material';
import {login, register} from "../services/UsersService";
import {RegisterOutputModel} from "../models/output/RegisterOutputModel";
import {setAuthToken} from "../services/Utils/CookiesHandling";
import {LoginOutputModel} from "../models/output/LoginOutputModel";
import {useNavigate} from "react-router-dom";

const RegisterPage: React.FC = () => {
    const [username, setUsername] = useState('');
    const [password, setPassword] = useState('');
    const [confirmPassword, setConfirmPassword] = useState('');
    const [invitationCode,setInvitationCode] = useState('');
    const [error, setError] = useState('');
    const navigate= useNavigate();

    const handleRegister = (e: React.FormEvent) => {
        e.preventDefault();

        if (password !== confirmPassword) {
            setError("Passwords do not match.");
            return;
        }

        setError('');

        register({ username, password, invitationCode }).then(r => {
            console.log(r.json)
            if (r.contentType === "application/json") {
                login({username, password}).then(res => {
                    console.log(res.json)
                    if(r.contentType==="application/json"){
                        const token= res.json as LoginOutputModel;
                        setAuthToken(token.token);
                        navigate('/');
                    }else console.log("login failed")
                })

            }
            console.log("login failed")
        })

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
            <Box component="form" onSubmit={handleRegister} sx={{ display: 'flex', flexDirection: 'column', gap: 2, width: '100%', maxWidth: 400 }}>
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
