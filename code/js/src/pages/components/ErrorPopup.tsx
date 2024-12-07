import React from 'react';
import { Dialog, DialogTitle, DialogContent, DialogActions, Typography, Button, Box } from '@mui/material';

interface ErrorPopupProps {
    open: boolean; // Controls whether the popup is visible
    title: string; // Title of the error
    details: string; // Details about the error
    onClose: () => void; // Function to close the popup
}

const ErrorPopup: React.FC<ErrorPopupProps> = ({ open, title, details, onClose }) => {
    return (
        <Dialog open={open} onClose={onClose} maxWidth="sm" fullWidth>
            <DialogTitle>
                <Typography variant="h6" color="error">
                    {title}
                </Typography>
            </DialogTitle>
            <DialogContent>
                <Box>
                    <Typography variant="body1">{details}</Typography>
                </Box>
            </DialogContent>
            <DialogActions>
                <Button onClick={onClose} variant="contained" color="primary">
                    Close
                </Button>
            </DialogActions>
        </Dialog>
    );
};

export default ErrorPopup;
