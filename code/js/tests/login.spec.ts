import { test, expect } from '@playwright/test';

test.describe('Login Page functionality', () => {
    test('should display login form and handle successful login', async ({ page }) => {
        // Navigate to the login page
        await page.goto('http://localhost:3000/login');

        // Ensure the login form is displayed
        const usernameInput = page.getByRole('textbox', { name: 'Username' });
        const passwordInput = page.getByLabel('Password');
        const loginButton = page.getByRole('button', { name: 'Login' });

        await expect(usernameInput).toBeVisible();
        await expect(passwordInput).toBeVisible();
        await expect(loginButton).toBeVisible();

        // Simulate user input
        await usernameInput.fill('testuser');
        await passwordInput.fill('password123');

        // Mock the API response for login
        await page.route('**/api/login', (route) =>
            route.fulfill({
                status: 200,
                contentType: 'application/json',
                body: JSON.stringify({ token: 'test-token' }),
            })
        );

        // Submit the form
        await loginButton.click();

        // Check if the token is stored (mock cookies or local storage behavior)
        const authToken = await page.evaluate(() => document.cookie);
        expect(authToken).toContain('authToken=test-token');

        // Check if the user is redirected to the home page
        await expect(page).toHaveURL('http://localhost:3000/');
    });

    test('should navigate to register page when link is clicked', async ({ page }) => {
        // Navigate to the login page
        await page.goto('http://localhost:3000/login');

        // Ensure the register link is displayed
        const registerLink = page.getByRole('link', { name: 'Click here to register' });
        await expect(registerLink).toBeVisible();

        // Click the register link
        await registerLink.click();

        // Ensure the user is redirected to the register page
        await expect(page).toHaveURL('http://localhost:3000/register');
    });
});
