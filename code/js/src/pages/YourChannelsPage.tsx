import React, { useEffect, useState } from 'react';
import { Box, Typography, List, ListItem, ListItemText, ListItemAvatar, Avatar, CircularProgress } from '@mui/material';
import {Outlet, useNavigate} from 'react-router-dom';
import Navbar from './components/NavBar';
import { GetChannelsListOutputModel } from '../models/output/GetChannelsListOutputModel';
import {getChannels, listenToMessages} from "../services/ChannelsService";
import {ChannelOutputModel} from "../models/output/ChannelOutputModel";
import {MessageOutputModel} from "../models/output/MessagesOutputModel"; // Ensure you have the correct type for channels

const YourChannels: React.FC = () => {
    const navigate = useNavigate();
    const [channels, setChannels] = useState<ChannelOutputModel[]>([]); // Assuming GetChannelsListOutputModel is the correct type
    const [loading, setLoading] = useState<boolean>(true);
    const [error, setError] = useState<string | null>(null);
    const [eventSource, setEventSource] = useState<EventSource | null>(null);
    const [newMessagesCount, setNewMessagesCount] = useState<{ [key: number]: number }>({});

    // Fetch channels when component mounts
    useEffect(() => {
        const fetchChannels = async () => {
            const response = await getChannels();
            if(response.contentType==="application/json") {
                const channelsData = response.json as GetChannelsListOutputModel;
                setChannels(channelsData.channels);  // Assuming response is an array of channels
                setLoading(false);
            }
        };
        const eventOnMessages = () => {
            const eventSourceInstance = listenToMessages();
            setEventSource(eventSourceInstance);
            console.log("STARTED LISTENING TO MESSAGES");
            //TODO(TRY TO IMPLEMENT TWO ONMESSAGE )
/*
            eventSourceInstance.onmessage = (event) => {
                const newIncomingMessage = JSON.parse(event.data) as MessageOutputModel;
                console.log('New message received:', newIncomingMessage);

                // Update new message count for the channel
                setNewMessagesCount((prevCount) => {
                    const channelId = newIncomingMessage.channelId;
                    return {
                        ...prevCount,
                        [channelId]: (prevCount[channelId] || 0) + 1, // Increment count for the channel
                    };
                });
            };

 */
        };

        eventOnMessages();
        fetchChannels();

        return () => {
            if (eventSource) {
                eventSource.close();
            }
        };
    }, []);

    const handleChannelClick = (id: number) => {
        navigate(`/channels/${id}`);
        setNewMessagesCount((prevCount) => ({
            ...prevCount,
            [id]: 0,
        }));
    };

    return (
        <Box display="flex" flexDirection="column" minHeight="100vh">
            {/* Navbar */}
            <Navbar title="Your Channels" canLogout={true} />

            {/* Content Area */}
            <Box display="flex" flexGrow={1}>
                {/* Left Sidebar (Channels List) */}
                <Box
                    sx={{
                        width: '30%',
                        bgcolor: 'background.paper',
                        borderRight: '1px solid #e0e0e0',
                        overflowY: 'auto',
                        maxHeight: 'calc(100vh - 64px)', // Account for the navbar height
                    }}
                >
                    {loading ? (
                        <Box display="flex" justifyContent="center" alignItems="center" height="100%">
                            <CircularProgress />
                        </Box>
                    ) : error ? (
                        <Box display="flex" justifyContent="center" alignItems="center" height="100%">
                            <Typography color="error">{error}</Typography>
                        </Box>
                    ) : (
                        <List>
                            {channels.map((channel) => (
                                <ListItem
                                    key={channel.channelId}
                                    onClick={() => handleChannelClick(channel.channelId)}
                                    sx={{
                                        borderBottom: '1px solid #e0e0e0',
                                        '&:hover': { backgroundColor: '#f9f9f9' },
                                        cursor: 'pointer',
                                    }}
                                >
                                    <ListItemAvatar>
                                        <Avatar>{channel.channelName.charAt(0)}</Avatar>
                                    </ListItemAvatar>
                                    <ListItemText
                                        primary={channel.channelName}
                                        secondary={
                                            newMessagesCount[channel.channelId] > 0
                                                ? `New messages: ${newMessagesCount[channel.channelId]}`
                                                : ''
                                        }
                                    />
                                </ListItem>
                            ))}
                        </List>
                    )}
                </Box>

                {/* Right Content Area (Sub-router outlet) */}
                <Box
                    sx={{
                        flexGrow: 1,
                        padding: 2,
                        bgcolor: '#f0f0f0',
                        overflowY: 'auto',
                        maxHeight: 'calc(100vh - 64px)', // Account for the navbar height
                    }}
                >
                    <Outlet context={{ eventSource }}/>
                </Box>
            </Box>
        </Box>
    );
};

export default YourChannels;
