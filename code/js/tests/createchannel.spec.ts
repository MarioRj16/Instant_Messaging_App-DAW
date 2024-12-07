import { test, expect } from '@playwright/test';

test('Create Channel Page functionality', async ({ page }) => {
    // Step 1: Navigate to the login page and log in
    await page.goto('http://localhost:3000/login');
    await page.fill('input[name="username"]', 'admin123');
    await page.fill('input[name="password"]', 'Sunny-Day7-Green-Trees');
    await page.click('button:has-text("Login")');

    // Step 2: Wait for navigation to the home page and go to Create Channel page
    await page.waitForURL('http://localhost:3000/');
    await page.click('button:has-text("Create Channel")');

    // Step 3: Fill in the channel creation form
    await page.fill('input[label="Channel Name"]', 'Test Channel');
    await page.click('label:has-text("Channel Type")');
    await page.click('li:has-text("Private")'); // Choose "Private" from the dropdown

    // Step 4: Submit the form
    const createButton = page.getByRole('button', { name: 'Create Channel' });
    await expect(createButton).toBeEnabled();
    await createButton.click();

    // Step 5: Wait for navigation to the Channels page
    await page.waitForURL('http://localhost:3000/channels');

    // Verify the user is on the Channels page
    await expect(page).toHaveURL('http://localhost:3000/channels');
    await expect(page.locator('h1')).toHaveText('Your Channels'); // Adjust based on your UI
});
