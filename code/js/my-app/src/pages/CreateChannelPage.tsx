import React, { useState } from 'react';
import { Box, Button, TextField, Typography, FormControl, InputLabel, Select, MenuItem } from '@mui/material';
import { useNavigate } from 'react-router-dom';
import Navbar from "./components/NavBar";

const CreateChannelPage: React.FC = () => {
    const [channelName, setChannelName] = useState('');
    const [channelType, setChannelType] = useState<'public' | 'private'>('public');
    const navigate = useNavigate();

    const handleCreateChannel = () => {
        console.log(`Creating channel: ${channelName} (${channelType})`);
        // TODO: Implement create channel functionality
        // Navigate to the newly created channel or home page after creation
        navigate('/channels');
    };


    return (
        <Box display="flex" flexDirection="column" minHeight="100vh">
            <Navbar title={"Create Channel"} canLogout={true} />

            <Box
                display="flex"
                flexDirection="column"
                alignItems="center"
                justifyContent="center"
                flexGrow={1}
                p={2}
            >
                <Box
                    component="form"
                    display="flex"
                    flexDirection="column"
                    gap={3}
                    width="100%"
                    maxWidth="400px"
                    onSubmit={(e) => {
                        e.preventDefault();
                        handleCreateChannel();
                    }}
                >
                    {/* Channel Name Input */}
                    <TextField
                        label="Channel Name"
                        variant="outlined"
                        fullWidth
                        required
                        value={channelName}
                        onChange={(e) => setChannelName(e.target.value)}
                    />

                    {/* Channel Type Select */}
                    <FormControl fullWidth variant="outlined" required>
                        <InputLabel id="channel-type-label">Channel Type</InputLabel>
                        <Select
                            labelId="channel-type-label"
                            label="Channel Type"
                            value={channelType}
                            onChange={(e) => setChannelType(e.target.value as 'public' | 'private')}
                        >
                            <MenuItem value="public">Public</MenuItem>
                            <MenuItem value="private">Private</MenuItem>
                        </Select>
                    </FormControl>

                    {/* Create Channel Button */}
                    <Button type="submit" variant="contained" color="primary" fullWidth>
                        Create Channel
                    </Button>
                </Box>
            </Box>
        </Box>
    );
};

export default CreateChannelPage;
