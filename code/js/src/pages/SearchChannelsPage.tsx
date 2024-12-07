import React, { useEffect, useState } from "react";
import {
    Box,
    Button,
    List,
    ListItem,
    ListItemText,
    Typography,
    TextField,
} from "@mui/material";
import Navbar from "./components/NavBar";
import { useNavigate, useSearchParams } from "react-router-dom";
import { searchChannels, joinChannel } from "../services/ChannelsService";
import { GetChannelsListOutputModel } from "../models/output/GetChannelsListOutputModel";
import { getCookie } from "../services/Utils/CookiesHandling";
import { ChannelOutputModel } from "../models/output/ChannelOutputModel";
import PaginationFooter from "./components/PaginationFooter"; // Import new component

const SearchChannelsPage: React.FC = () => {
    const [publicChannels, setPublicChannels] = useState<ChannelOutputModel[]>([]);
    const [loading, setLoading] = useState(false);
    const [searchParams, setSearchParams] = useSearchParams(); // To manage URL query params
    const navigate = useNavigate();
    const currentUserId = Number(getCookie("userId"));

    const searchTerm = searchParams.get("channelName") || ""; // Retrieve searchTerm from URL
    const page = Number(searchParams.get("page") || 1); // Retrieve page number from URL
    const pageSize = Number(searchParams.get("pageSize") || 10); // Retrieve pageSize from URL

    const [totalPages, setTotalPages] = useState(0); // Total pages from API
    const [hasPrevious, setHasPrevious] = useState(false);
    const [hasNext, setHasNext] = useState(false);

    useEffect(() => {
        const fetchChannels = async () => {
            setLoading(true);
            try {
                const response = await searchChannels(searchTerm, page, pageSize); // Pass the query params
                if (response.contentType === "application/json") {
                    const channelsData = response.json as GetChannelsListOutputModel;
                    setPublicChannels(channelsData.channels);
                    setTotalPages(channelsData.totalPages);
                    setHasPrevious(channelsData.hasPrevious);
                    setHasNext(channelsData.hasNext);
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
    }, [searchTerm, page, pageSize]); // Refetch when query params change

    const handleJoinChannel = async (id: number) => {
        try {
            await joinChannel(id);
            console.log(`Successfully joined channel with ID: ${id}`);
            navigate(`/channels/${id}`); // Redirect to the channel page after joining
        } catch (error) {
            console.error(`Failed to join channel with ID ${id}:`, error);
        }
    };

    const handleGoToChannel = (id: number) => {
        navigate(`/channels/${id}`);
    };

    const handleSearch = () => {
        setSearchParams({ channelName: searchTerm, page: "1", pageSize: pageSize.toString() }); // Reset to page 1 on search
    };

    const goToPreviousPage = () => {
        if (hasPrevious) {
            setSearchParams({
                channelName: searchTerm,
                page: (page - 1).toString(),
                pageSize: pageSize.toString(),
            });
        }
    };

    const goToNextPage = () => {
        if (hasNext) {
            setSearchParams({
                channelName: searchTerm,
                page: (page + 1).toString(),
                pageSize: pageSize.toString(),
            });
        }
    };

    const handlePageSizeChange = (newPageSize: number) => {
        setSearchParams({
            channelName: searchTerm,
            page: "1", // Reset to the first page when changing page size
            pageSize: newPageSize.toString(),
        });
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
                        onChange={(e) =>
                            setSearchParams({
                                channelName: e.target.value,
                                page: "1",
                                pageSize: pageSize.toString(),
                            })
                        }
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
                        {publicChannels.map((channel) => {
                            const isMember = channel.members.memberships.some(
                                (membership) => membership.user.userId === currentUserId
                            );

                            return (
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
                                    {isMember ? (
                                        <Button
                                            variant="contained"
                                            color="success"
                                            onClick={() => handleGoToChannel(channel.channelId)}
                                            sx={{ minWidth: "100px" }}
                                        >
                                            Go
                                        </Button>
                                    ) : (
                                        <Button
                                            variant="contained"
                                            color="primary"
                                            onClick={() => handleJoinChannel(channel.channelId)}
                                            sx={{ minWidth: "100px" }}
                                        >
                                            Join
                                        </Button>
                                    )}
                                </ListItem>
                            );
                        })}
                    </List>
                )}
            </Box>

            {/* Footer */}
            <PaginationFooter
                currentPage={page}
                totalPages={totalPages}
                hasPrevious={hasPrevious}
                hasNext={hasNext}
                pageSize={pageSize}
                onPrevious={goToPreviousPage}
                onNext={goToNextPage}
                onPageSizeChange={handlePageSizeChange}
            />
        </Box>
    );
};

export default SearchChannelsPage;
