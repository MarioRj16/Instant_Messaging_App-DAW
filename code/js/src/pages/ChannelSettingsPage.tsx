import React, { useState } from 'react';
import {
    Box, Button, List, ListItem, ListItemText,
    Dialog, DialogActions, DialogContent, DialogTitle,
    TextField, FormControl, InputLabel, Select, MenuItem
} from '@mui/material';
import Navbar from "./components/NavBar";
import {inviteMember, leaveChannel} from "../services/ChannelsService";
import {useNavigate, useParams} from "react-router-dom";
import {RoleModel} from "../models/RoleModel";

// Mock data for channel members
const members = [
    { id: '1', username: 'Alice', role: 'owner' },
    { id: '2', username: 'Bob', role: 'member' },
    { id: '3', username: 'Eve', role: 'viewer' },
    // Add more members if needed
];


const ChannelSettingsPage: React.FC = () => {
    const { id } = useParams<{ id: string }>();
    const [inviteDialogOpen, setInviteDialogOpen] = useState(false);
    const [newUsername, setNewUsername] = useState('');
    const [newUserRole, setNewUserRole] = useState<RoleModel>(RoleModel.member);

    //TODO(GET CURRENT USER ROLE)
    const currentUserRole: RoleModel = 'owner';
    const navigate = useNavigate();

    // Toggle invite dialog
    const handleOpenInviteDialog = () => setInviteDialogOpen(true);
    const handleCloseInviteDialog = () => {
        setInviteDialogOpen(false);
        setNewUsername('');
        setNewUserRole('member');
    };

    const handleInvite = () => {
        console.log(`Inviting ${newUsername} as ${newUserRole}`);

        inviteMember(Number(id),{username: newUsername, role: newUserRole}).then()
        handleCloseInviteDialog();
    };

    // Leave Channel functionality
    const handleLeaveChannel = () => {
        console.log("Leaving the channel...");
        leaveChannel(Number(id)).then(res =>{res.contentType})
        navigate('/');
    };

    return (
        <Box display="flex" flexDirection="column" minHeight="100vh" width="100%" height="100%">
            {/* Members List */}

            <List sx={{ width: '100%', bgcolor: 'background.paper' }}>
                {members.map((member) => (
                    <ListItem key={member.id} divider>
                        <ListItemText
                            primary={member.username}
                            secondary={`Role: ${member.role.charAt(0).toUpperCase() + member.role.slice(1)}`}
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
                    Invite Member
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
                <DialogTitle>Invite a New Member</DialogTitle>
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
                                onChange={(e) => setNewUserRole(e.target.value as RoleModel)}
                                label="Role"
                            >
                                <MenuItem value={RoleModel.member}>Member</MenuItem>
                                <MenuItem value={RoleModel.viewer}>Viewer</MenuItem>
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
