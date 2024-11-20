import React from 'react';
import { Box, Button, List, ListItem, ListItemText, Typography } from '@mui/material';
// @ts-ignore
import dayjs from 'dayjs';
import Navbar from "./components/NavBar";

// Mock data for invitations
const invitations = [
    {
        id: '1',
        channelName: 'React Devs',
        owner: 'Alice',
        inviter: 'Bob',
        expirationDate: '2024-12-01',
    },
    {
        id: '2',
        channelName: 'JavaScript Enthusiasts',
        owner: 'Charlie',
        inviter: 'Dave',
        expirationDate: '2023-11-01',
    },
    // Add more invitations as needed
];

const InvitationsPage: React.FC = () => {
    const handleAcceptInvite = (id: string) => {
        console.log(`Invitation ${id} accepted.`);
        // TODO: Implement invitation acceptance logic
    };

    return (
        <Box display="flex" flexDirection="column" minHeight="100vh">
            <Navbar title={"Invitations"} onLogoutClick={() => {}} />

            <Box
                display="flex"
                flexDirection="column"
                alignItems="center"
                flexGrow={1}
                p={2}
            >
                <List sx={{ width: '80%'}}>
                    {invitations.map((invite) => {
                        const isExpired = dayjs().isAfter(dayjs(invite.expirationDate));

                        return (
                            <ListItem
                                key={invite.id}
                                sx={{
                                    display: 'flex',
                                    justifyContent: 'space-between',
                                    alignItems: 'center',
                                    borderBottom: '1px solid #ddd',
                                    paddingY: 2,
                                }}
                            >
                                <ListItemText
                                    primary={`${invite.channelName}`}
                                    secondary={
                                        <>
                                            <Typography variant="body2">
                                                <strong>Owner:</strong> {invite.owner}
                                            </Typography>
                                            <Typography variant="body2">
                                                <strong>Inviter:</strong> {invite.inviter}
                                            </Typography>
                                            <Typography variant="body2">
                                                <strong>Expires on:</strong> {dayjs(invite.expirationDate).format('YYYY-MM-DD')}
                                            </Typography>
                                        </>
                                    }
                                />
                                <Button
                                    variant="contained"
                                    color="primary"
                                    disabled={isExpired}
                                    onClick={() => handleAcceptInvite(invite.id)}
                                    sx={{ minWidth: '100px' }}
                                >
                                    {isExpired ? 'Expired' : 'Accept'}
                                </Button>
                            </ListItem>
                        );
                    })}
                </List>
            </Box>
        </Box>
    );
};

export default InvitationsPage;
