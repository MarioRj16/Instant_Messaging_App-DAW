import { test, expect } from '@playwright/test';

test.describe('Your Channels Page', () => {
    test.beforeEach(async ({ page }) => {
        // Navigate to login and authenticate
        await page.goto('http://localhost:3000/login');
        await page.fill('input[name="username"]', 'admin123');
        await page.fill('input[name="password"]', 'Sunny-Day7-Green-Trees');
        await page.click('button:has-text("Login")');

        // Wait for redirection to the homepage and navigate to Your Channels
        await page.waitForURL('http://localhost:3000/');
        await page.click('button:has-text("Your Channels")');
    });

    test('displays loading spinner, fetches and displays channels', async ({ page }) => {
        // Check that the loading spinner is visible
        await expect(page.locator('.MuiCircularProgress-root')).toBeVisible();

        // Wait for loading to complete and channels to render
        await page.waitForSelector('ul');
        const channels = await page.locator('ul > li').count();
        expect(channels).toBeGreaterThan(0);
    });

    test('navigates to channel page on channel click', async ({ page }) => {
        // Wait for channels to load
        await page.waitForSelector('ul > li');
        const firstChannel = page.locator('ul > li').first();

        // Click the first channel and verify navigation
        await firstChannel.click();
        await expect(page).toHaveURL(/\/channels\/\d+/);
    });


});
