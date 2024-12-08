import React, { useState } from 'react';
import { Box, Button, TextField, FormControl, InputLabel, Select, MenuItem } from '@mui/material';
import { useNavigate } from 'react-router-dom';
import Navbar from "./components/NavBar";
import ErrorPopup from "./components/ErrorPopup";
import { createChannel } from "../services/ChannelsService";
import { ProblemModel } from "../models/ProblemModel";

const CreateChannelPage: React.FC = () => {
    const [channelName, setChannelName] = useState('');
    const [channelType, setChannelType] = useState<boolean>(true);
    const navigate = useNavigate();

    // ErrorPopup state
    const [errorPopupOpen, setErrorPopupOpen] = useState(false);
    const [errorTitle, setErrorTitle] = useState('');
    const [errorDetails, setErrorDetails] = useState('');

    const handleCreateChannel = async () => {
        console.log(`Creating channel: ${channelName} (${channelType})`);
        const resp=await createChannel({ channelName: channelName, isPublic: channelType });
        if(resp.status===201){
            navigate('/channels')
        }else {
            const error = resp.json as ProblemModel;
            setErrorTitle(error.title );
            setErrorDetails(error.detail );
            setErrorPopupOpen(true);
        }
    };

    return (
        <Box display="flex" flexDirection="column" minHeight="100vh">
            <Navbar title="Create Channel" canLogout={true} />

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
                            onChange={(e) => setChannelType(e.target.value === 'true')}
                        >
                            <MenuItem value="true">Public</MenuItem>
                            <MenuItem value="false">Private</MenuItem>
                        </Select>
                    </FormControl>

                    {/* Create Channel Button */}
                    <Button type="submit" variant="contained" color="primary" fullWidth>
                        Create Channel
                    </Button>
                </Box>
            </Box>

            {/* ErrorPopup Component */}
            <ErrorPopup
                open={errorPopupOpen}
                title={errorTitle}
                details={errorDetails}
                onClose={() => setErrorPopupOpen(false)}
            />
        </Box>
    );
};

export default CreateChannelPage;
