import React from 'react';
import { Box, Typography, List, ListItem, ListItemText, ListItemAvatar, Avatar } from '@mui/material';
import { useNavigate } from 'react-router-dom';
import Navbar from "./components/NavBar";

// Mock data for channels
let channels = [
    {
        id: '1',
        name: 'Family Group',
        lastMessage: 'See you at dinner!',
        timeSent: '10:30 AM',
    },
    {
        id: '2',
        name: 'Work Chat',
        lastMessage: 'Project deadline is tomorrow.',
        timeSent: '9:15 AM',
    },
    {
        id: '3',
        name: 'Friends',
        lastMessage: 'Let’s go hiking this weekend!',
        timeSent: 'Yesterday',
    },
    // More channels as needed
];
createMockChannels(channels,4,30);

function createMockChannels(channels: ({ name: string; lastMessage: string; timeSent: string; id: string }[]), start: number, number: number){

    for (let i = start; i < number; i++) {
        channels.push({
            id: i.toString(),
            name: `Channel ${i}`,
            lastMessage: `Last message in channel ${i}`,
            timeSent: '10:30 AM',
        });
    }
    return channels;
}


const YourChannels: React.FC = () => {
    const navigate = useNavigate();

    const handleChannelClick = (id: string) => {
        navigate(`/channels/${id}`);
    };
    const handleLogout = () => {

        //TODO(IMPLEMENT LOGOUT FUNCTION)
        navigate('/login');
    };

    return (
        <Box
            display="flex"
            flexDirection="column"
            minHeight="100vh"
            sx={{ backgroundColor: '#f0f0f0' }}
        >
            <Navbar
                title="Your Channels"
                onLogoutClick={handleLogout}
            />
            <List sx={{ bgcolor: 'background.paper' }}>
                {channels.map((channel) => (
                    <ListItem
                        key={channel.id}
                        onClick={() => handleChannelClick(channel.id)}
                        sx={{
                            borderBottom: '1px solid #e0e0e0',
                            '&:hover': { backgroundColor: '#f9f9f9' },
                        }}
                        component="li" // Add this line to specify the component
                    >
                        <ListItemAvatar>
                            <Avatar>{channel.name.charAt(0)}</Avatar>
                        </ListItemAvatar>
                        <ListItemText
                            primary={channel.name}
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
                        />
                    </ListItem>
                ))}
            </List>
        </Box>
    );
};

export default YourChannels;
