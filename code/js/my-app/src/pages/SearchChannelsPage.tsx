import React from 'react';
import { Box, Button, List, ListItem, ListItemText, Typography } from '@mui/material';
import Navbar from "./components/NavBar";
import { useNavigate } from "react-router-dom";

// Mock data for public channels
const publicChannels = [
    { id: '1', name: 'React Devs', owner: 'Alice' },
    { id: '2', name: 'JavaScript Enthusiasts', owner: 'Charlie' },
    { id: '3', name: 'Python Masters', owner: 'Eve' },
    // Add more channels as needed
];

const SearchChannelsPage: React.FC = () => {
    const navigate = useNavigate();

    const handleJoinChannel = (id: string) => {
        console.log(`Joining channel with ID: ${id}`);
        // TODO: Implement join channel functionality
    };

    const handleLogout = () => {
        // TODO: Implement logout functionality
        navigate('/login');
    };

    return (
        <Box display="flex" flexDirection="column" minHeight="100vh">
            <Navbar title="Search Channels" onLogoutClick={handleLogout} />

            <Box display="flex" flexDirection="column" alignItems="center" flexGrow={1} p={2}>
                <List sx={{ width: '80%', maxWidth: 600 }}>
                    {publicChannels.map((channel) => (
                        <ListItem
                            key={channel.id}
                            sx={{
                                display: 'flex',
                                justifyContent: 'space-between',
                                alignItems: 'center',
                                borderBottom: '1px solid #ddd',
                                paddingY: 2,
                            }}
                        >
                            <ListItemText
                                primary={channel.name}
                                secondary={
                                    <Typography variant="body2">
                                        <strong>Owner:</strong> {channel.owner}
                                    </Typography>
                                }
                            />
                            <Button
                                variant="contained"
                                color="primary"
                                onClick={() => handleJoinChannel(channel.id)}
                                sx={{ minWidth: '100px' }}
                            >
                                Join
                            </Button>
                        </ListItem>
                    ))}
                </List>
            </Box>
        </Box>
    );
};

export default SearchChannelsPage;
