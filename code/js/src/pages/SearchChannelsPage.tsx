import React, { useEffect, useState } from 'react';
import { Box, Button, List, ListItem, ListItemText, Typography, TextField } from '@mui/material';
import Navbar from "./components/NavBar";
import { useNavigate, useSearchParams } from "react-router-dom";
import { searchChannels, joinChannel } from "../services/ChannelsService";
import { GetChannelsListOutputModel } from "../models/output/GetChannelsListOutputModel";

const SearchChannelsPage: React.FC = () => {
    const [publicChannels, setPublicChannels] = useState<GetChannelsListOutputModel["channels"]>([]);
    const [loading, setLoading] = useState(false);
    const [searchTerm, setSearchTerm] = useState(""); // State to manage input
    const [searchParams, setSearchParams] = useSearchParams(); // To manage query params
    const navigate = useNavigate();

    // Fetch channels when component mounts or query changes
    useEffect(() => {
        const fetchChannels = async () => {
            const channelName = searchParams.get("channelName") || ""; // Get the query param
            setLoading(true);
            try {
                const response = await searchChannels(channelName); // Pass the search term to the API
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
    }, [searchParams]); // Trigger when searchParams change

    const handleJoinChannel = async (id: number) => {
        try {
            await joinChannel(id);
            console.log(`Successfully joined channel with ID: ${id}`);
            navigate(`/channels/${id}`); // Redirect to the channel page after joining
        } catch (error) {
            console.error(`Failed to join channel with ID ${id}:`, error);
        }
    };

    const handleSearch = () => {
        if (searchTerm.trim()) {
            setSearchParams({ channelName: searchTerm }); // Update the query parameter
        } else {
            setSearchParams({}); // Clear the query parameter if input is empty
        }
    };

    return (
        <Box display="flex" flexDirection="column" minHeight="100vh">
            <Navbar title="Search Channels" canLogout={true} />

            <Box display="flex" flexDirection="column" alignItems="center" flexGrow={1} p={2} gap={2}>
                {/* Search Input */}
                <Box display="flex" justifyContent="center" gap={1} width="100%" maxWidth="600px">
                    <TextField
                        label="Search Channels"
                        variant="outlined"
                        fullWidth
                        value={searchTerm}
                        onChange={(e) => setSearchTerm(e.target.value)}
                    />
                    <Button
                        variant="contained"
                        color="primary"
                        onClick={handleSearch}
                        sx={{ minWidth: "100px" }}
                    >
                        Search
                    </Button>
                </Box>

                {/* Channel List */}
                {loading ? (
                    <Typography variant="h6">Loading channels...</Typography>
                ) : publicChannels.length === 0 ? (
                    <Typography variant="h6">No channels found.</Typography>
                ) : (
                    <List sx={{ width: "80%" }}>
                        {publicChannels.map((channel) => (
                            <ListItem
                                key={channel.channelId}
                                sx={{
                                    display: "flex",
                                    justifyContent: "space-between",
                                    alignItems: "center",
                                    borderBottom: "1px solid #ddd",
                                    paddingY: 2,
                                }}
                            >
                                <ListItemText
                                    primary={channel.channelName}
                                    secondary={
                                        <Typography variant="body2">
                                            <strong>Owner:</strong> {channel.owner.username}
                                        </Typography>
                                    }
                                />
                                <Button
                                    variant="contained"
                                    color="primary"
                                    onClick={() => handleJoinChannel(channel.channelId)}
                                    sx={{ minWidth: "100px" }}
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
