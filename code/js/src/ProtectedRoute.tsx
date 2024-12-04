import React, { useEffect, useState } from 'react';
import {Navigate, useNavigate} from 'react-router-dom';
import {me} from "./services/UsersService";
import LoadingPage from "./pages/LoadingPage";

// Define states for authentication status
type AuthStatus = 'loading' | 'authenticated' | 'unauthenticated';

function ProtectedRoute({ children }: { children: JSX.Element }) {
    const [authStatus, setAuthStatus] = useState<AuthStatus>('loading');
    const navigate = useNavigate();

    useEffect(() => {
        const checkAuth = async () => {
           const user = await me(); // Call the me function
           if (user.contentType=== "application/json") setAuthStatus('authenticated');
           else setAuthStatus('unauthenticated');

        };

        checkAuth();
    }, []);

    if (authStatus === 'loading') {
        return <LoadingPage/>; // Optional loading indicator
    }

    if (authStatus === 'unauthenticated') {
        navigate('/login')
        return
    }

    return children;
}

export default ProtectedRoute;
