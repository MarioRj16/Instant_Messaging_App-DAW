import React, { useState, useEffect, useRef } from 'react';
import { Box, List, ListItem, ListItemText, TextField, Button } from '@mui/material';
import SettingsIcon from '@mui/icons-material/Settings';
import SendIcon from '@mui/icons-material/Send';
import { useParams, useNavigate, useLocation, Outlet } from 'react-router-dom';

const ChannelPage: React.FC = () => {
    const { id } = useParams<{ id: string }>(); // Channel ID from URL
    const navigate = useNavigate();
    const location = useLocation();

    const [messages, setMessages] = useState([
        { id: '1', text: 'Hello everyone!', sender: 'Alice', timeSent: '10:30 AM' },
        { id: '2', text: 'Hi Alice!', sender: 'Bob', timeSent: '10:31 AM' },
        { id: '3', text: 'What’s the agenda for today?', sender: 'Alice', timeSent: '10:32 AM' },
    ]); // Manage messages with state
    const [newMessage, setNewMessage] = useState(''); // Store new message input
    const currentUser = 'You'; // Change this to the actual logged-in user name

    const messagesEndRef = useRef<HTMLDivElement | null>(null); // Ref for auto-scroll

    // Handle Settings navigation
    const handleSettingsClick = () => {
        navigate(`/channels/${id}/settings`);
    };

    // Handle message send
    const handleSendMessage = () => {
        if (newMessage.trim() !== '') {
            setMessages([
                ...messages,
                {
                    id: (messages.length + 1).toString(),
                    text: newMessage,
                    sender: currentUser,
                    timeSent: new Date().toLocaleTimeString(),
                },
            ]);
            setNewMessage('');
        }
    };

    // Handle Enter key for sending message
    const handleKeyDown = (e: React.KeyboardEvent) => {
        if (e.key === 'Enter') {
            e.preventDefault();
            handleSendMessage();
        }
    };

    // Auto-scroll to the latest message
    useEffect(() => {
        if (messagesEndRef.current) {
            messagesEndRef.current.scrollIntoView({ behavior: 'smooth' });
        }
    }, [messages]);

    // Determine if settings route is active
    const isSettingsRoute = location.pathname.includes('/settings');

    // Check if Outlet has any content
    const isOutletEmpty = location.pathname === `/channels/${id}`;

    return (
        <Box
            height="100%"
            width="100%"
            display="flex"
            flexDirection="row"
        >
            <Box
                display="flex"
                flexDirection="column"
                height="100%"
                sx={{
                    backgroundColor: '#f5f5f5',
                    overflow: 'hidden', // Ensure no unwanted scrollbars
                    flex: isOutletEmpty ? 1 : 0.7, // If Outlet is empty, take full width
                }}
            >
                {/* Messages List */}
                <Box
                    flexGrow={1}
                    p={2}
                    sx={{
                        overflowY: 'auto',
                        // Account for input area
                    }}
                >
                    <List>
                        {messages.map((message) => (
                            <ListItem key={message.id} sx={{ mb: 1, display: 'flex', alignItems: 'flex-start' }}>
                                <ListItemText
                                    primary={
                                        message.sender === currentUser
                                            ? message.text
                                            : `${message.sender}: ${message.text}`
                                    }
                                    secondary={message.timeSent}
                                    sx={{
                                        borderRadius: 2,
                                        p: 1.5,
                                        alignSelf: message.sender === currentUser ? 'flex-end' : 'flex-start',
                                        textAlign: message.sender === currentUser ? 'right' : 'left',
                                        backgroundColor: message.sender === currentUser ? '#4CAF50' : '#e0e0e0',
                                        color: message.sender === currentUser ? '#fff' : 'inherit',
                                    }}
                                />
                            </ListItem>
                        ))}
                    </List>
                    <div ref={messagesEndRef} />
                </Box>

                {/* Message Input */}
                <Box
                    display="flex"
                    alignItems="center"
                    p={2}
                    borderTop="1px solid #ddd"
                    bgcolor="background.paper"
                    sx={{ position: 'relative' }}
                >
                    <TextField
                        fullWidth
                        value={newMessage}
                        onChange={(e) => setNewMessage(e.target.value)}
                        onKeyDown={handleKeyDown}
                        placeholder="Type a message"
                        variant="outlined"
                        size="small"
                        sx={{ mr: 1 }}
                    />
                    <Button variant="contained" color="primary" onClick={handleSendMessage}>
                        <SendIcon />
                    </Button>
                </Box>


            </Box>

            {/* Outlet for route-based content */}
            <Box
                flex={isOutletEmpty ? 0 : 0.3} // Only take space if the Outlet has content

                sx={{
                    overflowY: 'auto',

                }}
            >
                <Outlet />
            </Box>
        </Box>
    );
};

export default ChannelPage;
