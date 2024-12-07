import React from "react";
import { Box, Button, Typography, Select, MenuItem, FormControl, InputLabel, SelectChangeEvent } from "@mui/material";

interface PaginationFooterProps {
    currentPage: number;
    totalPages: number;
    hasPrevious: boolean;
    hasNext: boolean;
    pageSize: number;
    onPrevious: () => void;
    onNext: () => void;
    onPageSizeChange: (newPageSize: number) => void;
}

const PaginationFooter: React.FC<PaginationFooterProps> = ({
       currentPage,
       totalPages,
       hasPrevious,
       hasNext,
       pageSize,
       onPrevious,
       onNext,
       onPageSizeChange,
   }) => {
    const handlePageSizeChange = (event: SelectChangeEvent<number>) => {
        const newSize = Number(event.target.value);
        onPageSizeChange(newSize);
    };

    return (
        <Box
            component="footer"
            display="flex"
            justifyContent="space-between"
            alignItems="center"
            p={2}
            borderTop="1px solid #ddd"
            width="100%"
            mt="auto"
        >
            <Button variant="contained" color="secondary" onClick={onPrevious} disabled={!hasPrevious}>
                Previous
            </Button>

            <Typography variant="body2">
                Page {currentPage} of {totalPages}
            </Typography>

            <Button variant="contained" color="secondary" onClick={onNext} disabled={!hasNext}>
                Next
            </Button>

            <Box>
                <InputLabel id="page-size-select-label">Page Size</InputLabel>
                <FormControl sx={{ minWidth: 120, ml: 2 }}>
                    <Select
                        labelId="page-size-select-label"
                        value={pageSize}
                        onChange={handlePageSizeChange}
                    >
                        <MenuItem value={1}>1</MenuItem>
                        <MenuItem value={3}>3</MenuItem>
                        <MenuItem value={5}>5</MenuItem>
                        <MenuItem value={10}>10</MenuItem>
                        <MenuItem value={15}>15</MenuItem>
                    </Select>
                </FormControl>
            </Box>
        </Box>
    );
};

export default PaginationFooter;
