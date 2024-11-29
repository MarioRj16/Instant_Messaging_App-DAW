import React, { useEffect, useState } from 'react';
import { Box, Button, List, ListItem, ListItemText, Typography } from '@mui/material';
import Navbar from "./components/NavBar";
import { useNavigate } from "react-router-dom";
import { searchChannels, joinChannel } from "../services/ChannelsService";
import { GetChannelsListOutputModel } from "../models/output/GetChannelsListOutputModel";

const SearchChannelsPage: React.FC = () => {
    const [publicChannels, setPublicChannels] = useState<GetChannelsListOutputModel["channels"]>([]);
    const [loading, setLoading] = useState(false);
    const navigate = useNavigate();

    useEffect(() => {
        const fetchChannels = async () => {
            setLoading(true);
            try {
                const response = await searchChannels();
                if (response.contentType === "application/json") {
                    const channelsData = response.json as GetChannelsListOutputModel;
                    setPublicChannels(channelsData.channels);
                } else {
                    console.error("Failed to fetch channels: Invalid response format");
                }
            } catch (error) {
                console.error("Failed to fetch channels:", error);
            } finally {
                setLoading(false);
            }
        };

        fetchChannels();
    }, []);

    const handleJoinChannel = async (id: number) => {
        try {
            await joinChannel(id);
            console.log(`Successfully joined channel with ID: ${id}`);
            navigate(`/channels/${id}`); // Redirect to the channel page after joining
        } catch (error) {
            console.error(`Failed to join channel with ID ${id}:`, error);
        }
    };

    return (
        <Box display="flex" flexDirection="column" minHeight="100vh">
            <Navbar title="Search Channels" canLogout={true} />

            <Box display="flex" flexDirection="column" alignItems="center" flexGrow={1} p={2}>
                {loading ? (
                    <Typography variant="h6">Loading channels...</Typography>
                ) : publicChannels.length === 0 ? (
                    <Typography variant="h6">No channels available.</Typography>
                ) : (
                    <List sx={{ width: '80%' }}>
                        {publicChannels.map((channel) => (
                            <ListItem
                                key={channel.channelId}
                                sx={{
                                    display: 'flex',
                                    justifyContent: 'space-between',
                                    alignItems: 'center',
                                    borderBottom: '1px solid #ddd',
                                    paddingY: 2,
                                }}
                            >
                                <ListItemText
                                    primary={channel.channelName}
                                    secondary={
                                        <Typography variant="body2">
                                            <strong>Owner:</strong> {channel.ownerId }
                                        </Typography>
                                    }
                                />
                                <Button
                                    variant="contained"
                                    color="primary"
                                    onClick={() => handleJoinChannel(channel.channelId)}
                                    sx={{ minWidth: '100px' }}
                                >
                                    Join
                                </Button>
                            </ListItem>
                        ))}
                    </List>
                )}
            </Box>
        </Box>
    );
};

export default SearchChannelsPage;
