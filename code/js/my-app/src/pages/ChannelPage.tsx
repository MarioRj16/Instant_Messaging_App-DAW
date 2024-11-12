import React, { useState, useEffect, useRef } from 'react';
import { Box, AppBar, Toolbar, IconButton, Typography, List, ListItem, ListItemText, TextField, Button } from '@mui/material';
// @ts-ignore
import SettingsIcon from '@mui/icons-material/Settings';
// @ts-ignore
import SendIcon from '@mui/icons-material/Send';
import { useParams, useNavigate } from 'react-router-dom';
import Navbar from "./components/NavBar";

// Mock data for messages
let initialMessages = [
    { id: '1', text: 'Hello everyone!', sender: 'Alice', timeSent: '10:30 AM' },
    { id: '2', text: 'Hi Alice!', sender: 'Bob', timeSent: '10:31 AM' },
    { id: '3', text: 'What’s the agenda for today?', sender: 'Alice', timeSent: '10:32 AM' },
];

const ChannelPage: React.FC = () => {
    const { id } = useParams<{ id: string }>(); // Channel ID from URL
    const navigate = useNavigate();

    const [messages, setMessages] = useState(initialMessages); // Manage messages with state
    const [newMessage, setNewMessage] = useState(''); // Store new message input
    const currentUser = 'You'; // Change this to the actual logged-in user name

    // Correcting ref typing for a div
    const messagesEndRef = useRef<HTMLDivElement | null>(null); // Corrected typing here

    const handleSettingsClick = () => {
        navigate(`/channels/${id}/settings`);
    };

    // Handle sending the new message
    const handleSendMessage = () => {
        if (newMessage.trim() !== '') {
            const newMessageObj = {
                id: (messages.length + 1).toString(),
                text: newMessage,
                sender: currentUser, // Use the actual logged-in user
                timeSent: new Date().toLocaleTimeString(),
            };

            // Add the new message to the state
            setMessages([...messages, newMessageObj]);
            setNewMessage(''); // Clear the input field
        }
    };

    const handleKeyDown = (e: React.KeyboardEvent) => {
        if (e.key === 'Enter') {
            e.preventDefault();
            handleSendMessage();
        }
    };
    const handleLogout = () => {

        //TODO(IMPLEMENT LOGOUT FUNCTION)
        navigate('/login');
    };

    useEffect(() => {
        if (messagesEndRef.current) {
            messagesEndRef.current.scrollIntoView({ behavior: 'smooth' });
        }
    }, [messages]);
    return (
        <Box display="flex" flexDirection="column" minHeight="100vh">
            {/* Reusable Navbar with Settings Icon */}
            <Navbar
                title={`Channel ${id}`}
                onLogoutClick={handleLogout}
                icon={<SettingsIcon />}
                onIconClick={handleSettingsClick}
            />

            <Box
                flexGrow={1}
                p={2}
                sx={{
                    overflowY: 'auto',
                    backgroundColor: '#f5f5f5',
                    paddingBottom: '80px', // Ensure space for the input box
                }}
            >
                <List>
                    {messages.map((message) => (
                        <ListItem key={message.id} sx={{ mb: 1, display: 'flex', alignItems: 'flex-start' }}>
                            <ListItemText
                                primary={
                                    message.sender === currentUser ? message.text : `${message.sender}: ${message.text}`
                                }
                                secondary={message.timeSent}
                                sx={{
                                    borderRadius: 2,
                                    p: 1.5,
                                    alignSelf: message.sender === currentUser ? 'flex-end' : 'flex-start',
                                    textAlign: message.sender === currentUser ? 'right' : 'left', // Align text based on sender
                                    backgroundColor: message.sender === currentUser ? '#4CAF50' : '#e0e0e0', // Green for user
                                    color: message.sender === currentUser ? '#fff' : 'inherit', // White text for user messages
                                }}
                            />
                        </ListItem>
                    ))}
                </List>
                {/* This div ensures that the page scrolls to this point when a new message is added */}
                <div ref={messagesEndRef} />
            </Box>

            {/* Message Input (Fixed at the bottom) */}
            <Box display="flex" alignItems="center" p={2} borderTop="1px solid #ddd" bgcolor="background.paper" sx={{ position: 'fixed', bottom: 0, left: 0, right: 0, zIndex: 5 }}>
                <TextField
                    fullWidth
                    value={newMessage}
                    onChange={(e) => setNewMessage(e.target.value)} // Update newMessage state on input change
                    onKeyDown={handleKeyDown} // Add key down event to handle Enter press
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
    );
};

export default ChannelPage;
