import React, { useEffect, useState } from 'react';
import { Box, Typography, List, ListItem, ListItemText, ListItemAvatar, Avatar, CircularProgress } from '@mui/material';
import {Outlet, useNavigate} from 'react-router-dom';
import Navbar from './components/NavBar';
import { GetChannelsListOutputModel } from '../models/output/GetChannelsListOutputModel';
import {getChannels} from "../services/ChannelsService";
import {ChannelOutputModel} from "../models/output/ChannelOutputModel"; // Ensure you have the correct type for channels

const YourChannels: React.FC = () => {
    const navigate = useNavigate();
    const [channels, setChannels] = useState<ChannelOutputModel[]>([]); // Assuming GetChannelsListOutputModel is the correct type
    const [loading, setLoading] = useState<boolean>(true);
    const [error, setError] = useState<string | null>(null);

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

        fetchChannels();
    }, []);

    const handleChannelClick = (id: number) => {
        navigate(`/channels/${id}`); // Navigate to the specific channel's route
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
                                        /*
                                        secondary={
                                            <React.Fragment>
                                                <Typography
                                                    component="span"
                                                    variant="body2"
                                                    color="text.primary"
                                                >
                                                    {channel.lastMessage}
                                                </Typography>
                                                {' — '}
                                                {channel.timeSent}
                                            </React.Fragment>
                                        }
                                        
                                         */
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
