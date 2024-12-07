import { test, expect } from '@playwright/test';

test.describe('Channel Page', () => {
    test.beforeEach(async ({ page }) => {
        // Navigate to the login page and authenticate
        await page.goto('http://localhost:3000/login');
        await page.fill('input[name="username"]', 'user123');
        await page.fill('input[name="password"]', 'password123');
        await page.click('button:has-text("Login")');

        // Navigate to a specific channel
        await page.goto('http://localhost:3000/channels/1');
    });

    test('loads and displays messages', async ({ page }) => {
        // Ensure messages are loaded
        const messages = await page.locator('li');
        expect(await messages.count()).toBeGreaterThan(0);

        // Check the first message text
        const firstMessage = await messages.first().textContent();
        expect(firstMessage).toBeTruthy();
    });

    test('sends a message', async ({ page }) => {
        // Type a message and send it
        await page.fill('input[placeholder="Type a message"]', 'Test message');
        await page.click('button:has-text("Send")');

        // Verify the message appears in the chat
        const lastMessage = await page.locator('li').last();
        await expect(lastMessage).toContainText('Test message');
    });

});
