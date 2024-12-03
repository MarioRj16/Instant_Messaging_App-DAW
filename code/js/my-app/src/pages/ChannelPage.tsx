import React, { useState, useEffect, useRef } from 'react';
import { Box, List, ListItem, ListItemText, TextField, Button } from '@mui/material';
import SendIcon from '@mui/icons-material/Send';
import { useParams, useLocation, Outlet } from 'react-router-dom';
import { GetMessagesListOutputModel } from '../models/output/GetMessagesListOutputModel';
import { getMessages, listenToMessages, sendMessage } from "../services/ChannelsService";
import { UserOutputModel } from "../models/output/UserOutputModel";
import { me } from "../services/UsersService";
import { MessageOutputModel } from "../models/output/MessagesOutputModel";

const ChannelPage: React.FC = () => {
    const { id } = useParams<{ id: string }>(); // Channel ID from URL
    const location = useLocation();

    const [currentUser, setCurrentUser] = useState<UserOutputModel>(); // Change this to the actual logged-in user name
    const [messages, setMessages] = useState<MessageOutputModel[]>([]); // Manage messages with state
    const [newMessage, setNewMessage] = useState(''); // Store new message input

    const messagesEndRef = useRef<HTMLDivElement | null>(null); // Ref for auto-scroll
    const eventSourceRef = useRef<EventSource | null>(null); // To store the EventSource for cleanup

    // Fetch user and messages initially
    useEffect(() => {
        const fetchUser = async () => {
            const response = await me();
            if (response.contentType === "application/json") {
                const user = response.json as UserOutputModel;
                setCurrentUser(user);
            }
        };

        const fetchMessages = async () => {
            if (id) {
                try {
                    const fetchedMessages = await getMessages(Number(id));
                    const messagesData = fetchedMessages.json as GetMessagesListOutputModel;
                    setMessages(messagesData.messages.sort((a, b) => a.createdAt.localeCompare(b.createdAt)));
                } catch (error) {
                    console.error('Failed to fetch messages:', error);
                }
            }
        };

        fetchUser();
        fetchMessages();
    }, [id]);

    // Set up EventSource for listening to new messages
    useEffect(() => {
        if (!id) return;

        // Open connection and listen for messages
        const eventSource = listenToMessages();
        eventSourceRef.current = eventSource;

        eventSource.onopen = () => {
            console.log('Listening to messages...');
        };

        eventSource.onmessage = (event) => {
            console.log("RIGHT????")
            const newIncomingMessage = JSON.parse(event.data) as MessageOutputModel; // Parse incoming message
            console.log('New message:', newIncomingMessage);
            if (newIncomingMessage.channelId===Number(id)) setMessages((prevMessages) => [...prevMessages, newIncomingMessage]); // Add new message
        };

        eventSource.onerror = (error) => {
            console.error('Error in listening for messages:', error);
            eventSource.close(); // Close the connection on error
        };

        // Cleanup EventSource when the component unmounts or id changes
        return () => {
            if (eventSourceRef.current) {
                eventSourceRef.current.close();
                console.log('EventSource closed');
            }
        };

    }, [id]);

    // Auto-scroll to the latest message
    useEffect(() => {
        if (messagesEndRef.current) {
            messagesEndRef.current.scrollIntoView({ behavior: 'smooth' });
        }
    }, [messages]);

    // Handle message send
    const handleSendMessage = async () => {
        if (newMessage.trim() !== '') {
            const message = {
                content: newMessage,
            };
            try {
                await sendMessage(Number(id), message);
                setNewMessage('');
            } catch (error) {
                console.error('Failed to send message:', error);
            }
        }
    };

    // Handle Enter key for sending message
    const handleKeyDown = (e: React.KeyboardEvent) => {
        if (e.key === 'Enter') {
            e.preventDefault();
            handleSendMessage();
        }
    };

    const isOutletEmpty = location.pathname === `/channels/${id}`;

    return (
        <Box height="100%" width="100%" display="flex" flexDirection="row">
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
                    }}
                >
                    <List>
                        {messages.map((message) => (
                            <ListItem key={message.messageId} sx={{ mb: 1, display: 'flex', alignItems: 'flex-start' }}>
                                <ListItemText
                                    primary={
                                        message.senderInfo.userId === currentUser?.userId
                                            ? message.content
                                            : `${message.senderInfo.username}: ${message.content}`
                                    }
                                    secondary={message.createdAt}
                                    sx={{
                                        borderRadius: 2,
                                        p: 1.5,
                                        alignSelf: message.senderInfo.userId === currentUser?.userId ? 'flex-end' : 'flex-start',
                                        textAlign: message.senderInfo.userId === currentUser?.userId ? 'right' : 'left',
                                        backgroundColor: message.senderInfo.userId === currentUser?.userId ? '#4CAF50' : '#e0e0e0',
                                        color: message.senderInfo.userId === currentUser?.userId ? '#fff' : 'inherit',
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

            <Box flex={isOutletEmpty ? 0 : 0.3} sx={{ overflowY: 'auto' }}>
                <Outlet />
            </Box>
        </Box>
    );
};

export default ChannelPage;
