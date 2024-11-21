import React, { useState } from 'react';
import {
    Box, Button, List, ListItem, ListItemText,
    Dialog, DialogActions, DialogContent, DialogTitle,
    TextField, FormControl, InputLabel, Select, MenuItem
} from '@mui/material';
import Navbar from "./components/NavBar";

// Mock data for channel participants
const participants = [
    { id: '1', username: 'Alice', role: 'owner' },
    { id: '2', username: 'Bob', role: 'participant' },
    { id: '3', username: 'Eve', role: 'viewer' },
    { id: '1', username: 'Alice', role: 'owner' },
    { id: '2', username: 'Bob', role: 'participant' },
    { id: '3', username: 'Eve', role: 'viewer' },
    { id: '1', username: 'Alice', role: 'owner' },
    { id: '2', username: 'Bob', role: 'participant' },
    { id: '3', username: 'Eve', role: 'viewer' },
    { id: '1', username: 'Alice', role: 'owner' },
    { id: '2', username: 'Bob', role: 'participant' },
    { id: '3', username: 'Eve', role: 'viewer' },
    { id: '1', username: 'Alice', role: 'owner' },
    { id: '2', username: 'Bob', role: 'participant' },
    { id: '3', username: 'Eve', role: 'viewer' },
    { id: '1', username: 'Alice', role: 'owner' },
    { id: '2', username: 'Bob', role: 'participant' },
    { id: '3', username: 'Eve', role: 'viewer' },
    // Add more participants if needed
];

type Role = 'owner' | 'participant' | 'viewer';

const ChannelSettingsPage: React.FC = () => {
    const [inviteDialogOpen, setInviteDialogOpen] = useState(false);
    const [newUsername, setNewUsername] = useState('');
    const [newUserRole, setNewUserRole] = useState<Role>('participant');
    const currentUserRole: Role = 'owner'; // Mock current user's role

    // Toggle invite dialog
    const handleOpenInviteDialog = () => setInviteDialogOpen(true);
    const handleCloseInviteDialog = () => {
        setInviteDialogOpen(false);
        setNewUsername('');
        setNewUserRole('participant');
    };

    const handleInvite = () => {
        console.log(`Inviting ${newUsername} as ${newUserRole}`);
        // TODO: Implement the invite logic (e.g., API call to invite user)
        handleCloseInviteDialog();
    };

    // Leave Channel functionality
    const handleLeaveChannel = () => {
        console.log("Leaving the channel...");
        // TODO: Implement leave channel functionality, such as API call
    };

    return (
        <Box display="flex" flexDirection="column" minHeight="100vh" width="100%" height="100%">
            {/* Participants List */}

            <List sx={{ width: '100%', bgcolor: 'background.paper' }}>
                {participants.map((participant) => (
                    <ListItem key={participant.id} divider>
                        <ListItemText
                            primary={participant.username}
                            secondary={`Role: ${participant.role.charAt(0).toUpperCase() + participant.role.slice(1)}`}
                        />
                    </ListItem>
                ))}
            </List>

            {/* Invite Button */}
            {currentUserRole === 'owner' && (
                <Button
                    variant="contained"
                    color="primary"
                    onClick={handleOpenInviteDialog}
                    sx={{ mt: 2 }}
                >
                    Invite Participant
                </Button>
            )}

            {/* Leave Channel Button */}
            <Button
                variant="contained"
                color="error"
                onClick={handleLeaveChannel}
                sx={{ mt: 2 }}
            >
                Leave Channel
            </Button>

            {/* Invite Dialog */}
            <Dialog open={inviteDialogOpen} onClose={handleCloseInviteDialog}>
                <DialogTitle>Invite a New Participant</DialogTitle>
                <DialogContent>
                    <TextField
                        label="Username"
                        variant="outlined"
                        fullWidth
                        margin="normal"
                        value={newUsername}
                        onChange={(e) => setNewUsername(e.target.value)}
                    />
                    <FormControl fullWidth margin="normal" variant="outlined">
                        <InputLabel id="role-select-label">Role</InputLabel>
                        <Select
                            labelId="role-select-label"
                            value={newUserRole}
                            onChange={(e) => setNewUserRole(e.target.value as Role)}
                            label="Role"
                        >
                            <MenuItem value="participant">Participant</MenuItem>
                            <MenuItem value="viewer">Viewer</MenuItem>
                        </Select>
                    </FormControl>
                </DialogContent>
                <DialogActions>
                    <Button onClick={handleCloseInviteDialog} color="secondary">
                        Cancel
                    </Button>
                    <Button
                        onClick={handleInvite}
                        color="primary"
                        variant="contained"
                        disabled={!newUsername}
                    >
                        Invite
                    </Button>
                </DialogActions>
            </Dialog>
        </Box>
    );
};

export default ChannelSettingsPage;
