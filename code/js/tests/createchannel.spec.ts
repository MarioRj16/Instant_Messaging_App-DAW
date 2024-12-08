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

    // Step 4: Submit the form
    const createButton = page.getByRole('button', { name: 'Create Channel' });
    await expect(createButton).toBeEnabled();
    await createButton.click();

});
