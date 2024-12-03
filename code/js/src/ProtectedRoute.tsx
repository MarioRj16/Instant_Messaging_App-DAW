import React, { useEffect} from 'react';
import {useNavigate} from 'react-router-dom';
import {me} from "./services/UsersService";


function ProtectedRoute({ children }: { children: JSX.Element }) {
    const navigate = useNavigate()

    useEffect(() => {
        const checkAuth = async () => {
            const user = await me();
            if (user.contentType==='application/json'){
                console.log("authenticated")
            }
            else navigate("/login")

        };

        checkAuth();
    }, []);

    return children;
}

export default ProtectedRoute;
