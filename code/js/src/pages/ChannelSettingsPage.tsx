import React, { useState } from 'react';
import {
    Box, Button, List, ListItem, ListItemText,
    Dialog, DialogActions, DialogContent, DialogTitle,
    TextField, FormControl, InputLabel, Select, MenuItem
} from '@mui/material';
import { inviteMember, leaveChannel } from "../services/ChannelsService";
import { useNavigate, useOutletContext, useParams } from "react-router-dom";
import { RoleModel } from "../models/RoleModel";
import { MembershipOutputModel } from "../models/output/MembershipOutputModel";
import { getCookie } from "../services/Utils/CookiesHandling";
import ErrorPopup from "./components/ErrorPopup";
import {ProblemModel} from "../models/ProblemModel"; // Import ErrorPopup component

export type membersAndRole = {
    members: MembershipOutputModel[],
    role: RoleModel
};

const ChannelSettingsPage: React.FC = () => {
    const { id } = useParams<{ id: string }>();
    const [inviteDialogOpen, setInviteDialogOpen] = useState(false);
    const [newUsername, setNewUsername] = useState('');
    const [newUserRole, setNewUserRole] = useState<RoleModel>(RoleModel.member);

    const userId = Number(getCookie("userId"));
    const { members, role } = useOutletContext<membersAndRole>();
    const navigate = useNavigate();

    // States for ErrorPopup
    const [errorPopupOpen, setErrorPopupOpen] = useState(false);
    const [errorTitle, setErrorTitle] = useState('');
    const [errorDetails, setErrorDetails] = useState('');

    // Toggle invite dialog
    const handleOpenInviteDialog = () => setInviteDialogOpen(true);
    const handleCloseInviteDialog = () => {
        setInviteDialogOpen(false);
        setNewUsername('');
        setNewUserRole(RoleModel.member);
    };

    const handleInvite = async () => {
        const inviteResponse = await inviteMember(Number(id), { username: newUsername, role: newUserRole });
        if (inviteResponse.status === undefined ) {
            console.log("Successfully invited the member.");


        } else {
            const inviteError = inviteResponse.json as ProblemModel;
            setErrorTitle(inviteError.title || "Invite Error");
            setErrorDetails(inviteError.detail || "Failed to invite the member.");
            setErrorPopupOpen(true);

        }
        handleCloseInviteDialog();

    };

    const handleLeaveChannel = async () => {
        console.log("Attempting to leave the channel...");
        const leaveResponse = await leaveChannel(Number(id));
        console.log(leaveResponse.status)

        if (leaveResponse.status===undefined) {
            console.log("Successfully left the channel.");
            // Redirect to another page or update the state to reflect that the user has left
            navigate('/'); // Redirect after leaving
            return;
        }

        const leaveError = leaveResponse.json as ProblemModel;

        setErrorTitle(leaveError.title || "Leave Error");
        setErrorDetails(leaveError.detail || "Failed to leave the channel.");
        setErrorPopupOpen(true);



    };



    return (
        <Box display="flex" flexDirection="column" minHeight="100vh" width="100%" height="100%">
            {/* Members List */}
            <List sx={{ width: '100%', bgcolor: 'background.paper' }}>
                {members.map((member) => (
                    <ListItem
                        key={member.user.userId}
                        divider
                        sx={{
                            bgcolor: userId === member.user.userId ? '#4CAF50' : 'transparent',
                            color: userId === member.user.userId ? 'white' : 'inherit',
                        }}
                    >
                        <ListItemText
                            primary={member.user.username}
                            secondary={`Role: ${member.role.charAt(0).toUpperCase() + member.role.slice(1)}`}
                            sx={{
                                '& .MuiTypography-body2': {
                                    color: userId === member.user.userId ? 'white' : 'inherit',
                                },
                            }}
                        />
                    </ListItem>
                ))}
            </List>

            {/* Invite Button */}
            {(role === 'owner' || role === 'member') && (
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

export default ChannelSettingsPage;
