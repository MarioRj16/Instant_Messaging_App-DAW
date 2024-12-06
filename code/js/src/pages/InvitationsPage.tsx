import React, { useEffect, useState } from 'react';
import { Box, Button, List, ListItem, ListItemText, Typography, CircularProgress } from '@mui/material';
// @ts-ignore
import dayjs from 'dayjs';
import Navbar from "./components/NavBar";
import { getListInvitations, acceptInvitation, declineInvitation } from "../services/ChannelsService";
import { GetChannelsListOutputModel } from "../models/output/GetChannelsListOutputModel";
import {ChannelInvitationOutputModel} from "../models/output/ChannelInvitationOutputModel";
import {GetChannelInvitationsListOutputModel} from "../models/output/GetChannelInvitationsListOutputModel";

const InvitationsPage: React.FC = () => {
    const [invitations, setInvitations] = useState<ChannelInvitationOutputModel[]>([]);
    const [loading, setLoading] = useState(true);

    useEffect(() => {
        const fetchInvitations = async () => {
            try {
                setLoading(true);
                const response = await getListInvitations();
                if(response.contentType === "application/json") {
                    const data = response.json as GetChannelInvitationsListOutputModel;
                    setInvitations(data.invitations);
                }else setInvitations([])
            } catch (error) {
                console.error('Failed to fetch invitations:', error);
            } finally {
                setLoading(false);
            }
        };

        fetchInvitations();
    }, []);

    const handleAcceptInvite = async (id: number) => {
        try {
            await acceptInvitation(id);
            setInvitations((prev) => prev.filter((invite) => invite.channel.channelId!== id));
        } catch (error) {
            console.error(`Failed to accept invitation ${id}:`, error);
        }
    };

    const handleDeclineInvite = async (id: number) => {
        try {
            await declineInvitation(id);
            setInvitations((prev) => prev.filter((invite) => invite.channel.channelId !== id));
        } catch (error) {
            console.error(`Failed to decline invitation ${id}:`, error);
        }
    };

    return (
        <Box display="flex" flexDirection="column" minHeight="100vh">
            <Navbar title="Invitations" canLogout={true} />

            <Box
                display="flex"
                flexDirection="column"
                alignItems="center"
                flexGrow={1}
                p={2}
            >
                {loading ? (
                    <CircularProgress />
                ) : invitations.length === 0 ? (
                    <Typography variant="h6">No invitations available.</Typography>
                ) : (
                    <List sx={{ width: '80%' }}>
                        {invitations.map((invite) => {
                            return (
                                <ListItem
                                    key={invite.channelInvitationId}
                                    sx={{
                                        display: 'flex',
                                        justifyContent: 'space-between',
                                        alignItems: 'center',
                                        borderBottom: '1px solid #ddd',
                                        paddingY: 2,
                                    }}
                                >
                                    <ListItemText
                                        primary={`${invite.channel.channelName} - ${invite.role}`}
                                        secondary={
                                            <>
                                                <Typography variant="body2">
                                                    <strong>Inviter:</strong> {invite.inviter.username}
                                                </Typography>
                                                <Typography variant="body2">
                                                    <strong>Invited :</strong> {dayjs(invite.createdAt).format('YYYY-MM-DD')}
                                                </Typography>
                                            </>
                                        }
                                    />
                                    <Box sx={{ display: 'flex', gap: 2 }}>
                                        <Button
                                            variant="contained"
                                            color="primary"
                                            onClick={() => handleAcceptInvite(invite.channel.channelId)}
                                            sx={{ minWidth: '100px' }}
                                        >
                                            {'Accept'}
                                        </Button>

                                        <Button
                                            variant="contained"
                                            color="primary"
                                            onClick={() => handleDeclineInvite(invite.channel.channelId)}
                                            sx={{ minWidth: '100px' }}
                                        >
                                            { 'Decline'}
                                        </Button>
                                    </Box>
                                </ListItem>
                            );
                        })}
                    </List>
                )}
            </Box>
        </Box>
    );
};

export default InvitationsPage;
