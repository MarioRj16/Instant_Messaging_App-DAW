import React, { useState } from 'react';
import { Box, Button, TextField, Typography, Link } from '@mui/material';
import {login} from "../services/UsersService";
import {setAuthToken} from "../services/Utils/CookiesHandling";
import {LoginOutputModel} from "../models/output/LoginOutputModel";
import {useNavigate} from "react-router-dom";

const LoginPage: React.FC = () => {

    const navigate = useNavigate()
    const [username, setUsername] = useState('');
    const [password, setPassword] = useState('');

    const handleLogin = (e: React.FormEvent) => {
        e.preventDefault();

        login({username, password}).then(r => {
            console.log(r.json)
            if(r.contentType==="application/json"){
                const token= r.json as LoginOutputModel;
                setAuthToken(token.token);
                navigate('/');
            }
            console.log("login failed")
        })

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
            <Box component="form" onSubmit={handleLogin} sx={{ display: 'flex', flexDirection: 'column', gap: 2, width: '100%', maxWidth: 400 }}>
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
                <Button type="submit" variant="contained" color="primary" fullWidth>
                    Login
                </Button>
            </Box>
            <Typography variant="body2" sx={{ marginTop: 2 }}>
                Don’t have an account?{' '}
                <Link href="/register" underline="hover">
                    Click here to register
                </Link>
            </Typography>
        </Box>
    );
};

export default LoginPage;
