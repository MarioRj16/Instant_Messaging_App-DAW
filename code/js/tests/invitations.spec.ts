import { test, expect } from '@playwright/test';

test('Invitations Page functionality', async ({ page }) => {
    // Step 1: Navigate to the login page and log in
    await page.goto('http://localhost:3000/login');

    const usernameField = page.locator('input[name="username"]');
    const passwordField = page.locator('input[name="password"]');
    const loginButton = page.getByRole('button', { name: 'Login' });

    await usernameField.fill('admin123');
    await passwordField.fill('Sunny-Day7-Green-Trees');
    await loginButton.click();

    // Step 2: Wait for navigation to the home page
    await page.waitForURL('http://localhost:3000/');

    // Step 3: Navigate to the Invitations page
    const invitationsNavButton = page.getByRole('button', { name: 'Invitations' });
    await invitationsNavButton.click();

    // Step 4: Verify loading state
    const loadingIndicator = page.locator('.MuiCircularProgress-root'); // Adjust if specific class changes
    await expect(loadingIndicator).toBeVisible();

    // Step 5: Wait for invitations to load
    await loadingIndicator.waitFor({ state: 'hidden' });
// Step 6: Check if invitations are listed or empty state is shown
    const emptyStateMessage = page.getByText('No invitations available.');
    const invitationsList = page.locator('ul'); // Adjust this to the actual selector for the invitations list

// Wait for the invitations list or the empty state to become visible
    await Promise.race([
        emptyStateMessage.waitFor({ state: 'visible' }).catch(() => false),
        invitationsList.waitFor({ state: 'visible' }).catch(() => false),
    ]);

    const hasInvitations = await invitationsList.isVisible();

    if (hasInvitations) {
        // Invitations exist; test functionality for accepting and declining
        const firstInvitation = invitationsList.locator('li').first();
        await expect(firstInvitation).toBeVisible();

        const acceptButton = firstInvitation.getByRole('button', { name: 'Accept' });
        await acceptButton.click();
        await expect(firstInvitation).not.toBeVisible();

        const secondInvitation = invitationsList.locator('li').first();
        if (await secondInvitation.isVisible()) {
            const declineButton = secondInvitation.getByRole('button', { name: 'Decline' });
            await declineButton.click();
            await expect(secondInvitation).not.toBeVisible();
        }
    } else {
        // No invitations available
        await expect(emptyStateMessage).toBeVisible();
    }


});
