import React, { useEffect, useState } from 'react';
import { Box, Typography, List, ListItem, ListItemText, ListItemAvatar, Avatar, CircularProgress } from '@mui/material';
import {Outlet, useNavigate} from 'react-router-dom';
import Navbar from './components/NavBar';
import { GetChannelsListOutputModel } from '../models/output/GetChannelsListOutputModel';
import {getChannels, listenToMessages} from "../services/ChannelsService";
import {ChannelOutputModel} from "../models/output/ChannelOutputModel";
import {MessageOutputModel} from "../models/output/MessagesOutputModel";
import eventBus from "./components/EventBus"; // Ensure you have the correct type for channels

const YourChannels: React.FC = () => {
    const navigate = useNavigate();
    const [channels, setChannels] = useState<ChannelOutputModel[]>([]); // Assuming GetChannelsListOutputModel is the correct type
    const [loading, setLoading] = useState<boolean>(true);
    const [error, setError] = useState<string | null>(null);
    const [newMessagesCount, setNewMessagesCount] = useState<{ [key: number]: number }>({});

    useEffect(() => {
        let eventSource: EventSource | null = null;

        const fetchChannels = async () => {
            const response = await getChannels();
            if (response.status === 200) {
                const channelsData = response.json as GetChannelsListOutputModel;
                setChannels(channelsData.channels); // Assuming response is an array of channels
                setLoading(false);
            }
        };

        const initializeEventSource = () => {
            eventSource = listenToMessages();

            eventSource.onmessage = (event) => {
                const newIncomingMessage = JSON.parse(event.data) as MessageOutputModel;
                eventBus.emit("message", newIncomingMessage);
                console.log("newIncomingMessage", newIncomingMessage);
            };

            eventSource.onerror = () => {
                console.error("EventSource encountered an error and will be closed.");
                eventSource?.close();
            };
        };

        // Initialize data and EventSource
        fetchChannels();
        initializeEventSource();

        return () => {
            // Cleanup: Close EventSource and clear the EventBus
            if (eventSource) {
                eventSource.close();
                console.log("EventSource closed");
            }
            eventBus.close();
            console.log("EventBus handlers cleared");
        };
    }, []);


    useEffect(() => {
        const handleNewMessage = (newIncomingMessage: any) => {

            const channelId = newIncomingMessage.channelId;

            // Check if the current URL matches the channelId
            const currentPath = `/channels/${channelId}`;
            if (location.pathname === currentPath) {
                // Do nothing if the user is already on the channel page
                return;
            }
            setNewMessagesCount((prevCount) => ({
                ...prevCount,
                [channelId]: (prevCount[channelId] || 0) + 1,
            }));
        };

        eventBus.subscribe('message', handleNewMessage);

        return () => {
            eventBus.unsubscribe('message', handleNewMessage);
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
                    <Outlet />
                </Box>
            </Box>
        </Box>
    );
};

export default YourChannels;
