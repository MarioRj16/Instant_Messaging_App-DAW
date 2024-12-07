import React from 'react';
import { Box, CircularProgress, Typography } from '@mui/material';

const LoadingPage: React.FC = () => {
    return (
        <Box
            display="flex"
            flexDirection="column"
            justifyContent="center"
            alignItems="center"
            height="100vh"
            bgcolor="#f5f5f5"
        >
            <CircularProgress size={60} />
            <Typography variant="h6" color="textSecondary" mt={2}>
                Loading, please wait...
            </Typography>
        </Box>
    );
};

export default LoadingPage;
