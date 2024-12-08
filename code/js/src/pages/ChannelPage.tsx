import React, { useState, useEffect, useRef } from 'react';
import { Box, List, ListItem, ListItemText, TextField, Button } from '@mui/material';
import SendIcon from '@mui/icons-material/Send';
import {useParams, useLocation, Outlet, useOutletContext, useNavigate} from 'react-router-dom';
import { GetMessagesListOutputModel } from '../models/output/GetMessagesListOutputModel';
import { getChannel, getMessages, listenToMessages, sendMessage } from "../services/ChannelsService";
import { MessageOutputModel } from "../models/output/MessagesOutputModel";
import eventBus from "./components/EventBus";
import { ChannelOutputModel } from "../models/output/ChannelOutputModel";
import { RoleModel } from "../models/RoleModel";
import { MembershipOutputModel } from "../models/output/MembershipOutputModel";
import { getCookie } from "../services/Utils/CookiesHandling";

// Utility function to calculate relative time
const getRelativeTime = (createdAt: string): string => {
    const messageTime = new Date(createdAt);
    const now = new Date();
    const diffInSeconds = Math.floor((now.getTime() - messageTime.getTime()) / 1000);

    if (diffInSeconds < 3) return `now`;
    if (diffInSeconds < 60) return `${diffInSeconds} seconds ago`;
    const diffInMinutes = Math.floor(diffInSeconds / 60);
    if (diffInMinutes < 60) return `${diffInMinutes} minutes ago`;
    const diffInHours = Math.floor(diffInMinutes / 60);
    if (diffInHours < 24) return `${diffInHours} hours ago`;
    const diffInDays = Math.floor(diffInHours / 24);
    return `${diffInDays} days ago`;
};

const ChannelPage: React.FC = () => {
    const { id } = useParams<{ id: string }>(); // Channel ID from URL
    const location = useLocation();
    const navigate = useNavigate()
    const currentUser = Number(getCookie("userId")); // Current user ID
    const [messages, setMessages] = useState<MessageOutputModel[]>([]); // Manage messages
    const [newMessage, setNewMessage] = useState(''); // Store new message input
    const [members, setMembers] = useState<MembershipOutputModel[]>([]);
    const [isViewer, setIsViewer] = useState(false);
    const membersAndRole = {
        members: members,
        role: isViewer ? RoleModel.viewer : RoleModel.member,
    };
    const messagesEndRef = useRef<HTMLDivElement | null>(null); // Ref for auto-scroll

    useEffect(() => {
        const fetchMessages = async () => {
            if (id) {
                const fetchedMessages = await getMessages(Number(id));
                if(fetchedMessages.status === 200) {
                    const messagesData = fetchedMessages.json as GetMessagesListOutputModel;
                    //setMessages(messagesData.messages.sort((a, b) => a.createdAt.localeCompare(b.createdAt)));
                    setMessages(messagesData.messages);
                }else {
                    console.error("Failed to fetch messages: Invalid response format");
                    navigate('/channels')
                }

            }
        };

        const fetchChannel = async () => {
            const response = await getChannel(Number(id));
            if (response.status === 200) {
                const channelData = response.json as ChannelOutputModel;
                setMembers(channelData.members.memberships);
                setIsViewer(
                    channelData.members.memberships.find((membership) => membership.user.userId === currentUser)?.role === RoleModel.viewer
                );
            }
        };

        fetchMessages();
        fetchChannel();
    }, [id]);

    useEffect(() => {
        const handleNewMessage = (newIncomingMessage: MessageOutputModel) => {
            if (newIncomingMessage.channelId === Number(id)) {
                setMessages((prevMessages) => [...prevMessages, newIncomingMessage]);
            }
        };

        eventBus.subscribe('message', handleNewMessage);

        return () => {
            eventBus.unsubscribe('message', handleNewMessage);
        };
    }, [id]);

    // Auto-scroll to the latest message
    useEffect(() => {
        if (messagesEndRef.current) {
            messagesEndRef.current.scrollIntoView({ behavior: 'smooth' });
        }
    }, [messages]);

    // Update message timestamps every minute
    useEffect(() => {
        const interval = setInterval(() => {
            setMessages((prevMessages) => [...prevMessages]); // Trigger re-render
        }, 60000); // Update every minute

        return () => clearInterval(interval); // Cleanup interval on component unmount
    }, []);

    const handleSendMessage = async () => {
        if (newMessage.trim() !== '') {
            const message = { content: newMessage };
            try {
                await sendMessage(Number(id), message);
                setNewMessage('');
            } catch (error) {
                console.error('Failed to send message:', error);
            }
        }
    };

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
                    overflow: 'hidden',
                    flex: isOutletEmpty ? 1 : 0.7,
                }}
            >
                {/* Messages List */}
                <Box flexGrow={1} p={2} sx={{ overflowY: 'auto' }}>
                    <List>
                        {messages.map((message) => (
                            <ListItem key={message.messageId} sx={{ mb: 1, display: 'flex', alignItems: 'flex-start' }}>
                                <ListItemText
                                    primary={
                                        message.senderInfo.userId === currentUser
                                            ? message.content
                                            : `${message.senderInfo.username}: ${message.content}`
                                    }
                                    secondary={getRelativeTime(message.createdAt)} // Use relative time
                                    sx={{
                                        borderRadius: 2,
                                        p: 1.5,
                                        alignSelf: message.senderInfo.userId === currentUser ? 'flex-end' : 'flex-start',
                                        textAlign: message.senderInfo.userId === currentUser ? 'right' : 'left',
                                        backgroundColor: message.senderInfo.userId === currentUser ? '#4CAF50' : '#e0e0e0',
                                        color: message.senderInfo.userId === currentUser ? '#fff' : 'inherit',
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
                        disabled={isViewer}
                        placeholder={!isViewer ? "Type a message" : "You can't type since you are a viewer"}
                        variant="outlined"
                        size="small"
                        sx={{ mr: 1 }}
                    />
                    <Button variant="contained" color="primary" onClick={handleSendMessage} disabled={isViewer}>
                        <SendIcon />
                    </Button>
                </Box>
            </Box>

            <Box flex={isOutletEmpty ? 0 : 0.3} sx={{ overflowY: 'auto' }}>
                <Outlet context={membersAndRole} />
            </Box>
        </Box>
    );
};

export default ChannelPage;
