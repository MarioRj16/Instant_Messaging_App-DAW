import { test, expect } from '@playwright/test';

test('Register Page functionality', async ({ page }) => {
    // Navigate to the register page
    await page.goto('http://localhost:3000/register');

    // Locate elements using unique attributes
    const usernameField = page.locator('input[name="username"]');
    const passwordField = page.locator('input[name="password"]');
    const confirmPasswordField = page.locator('input[name="confirmPassword"]');
    const tokenField = page.locator('input[name="token"]');
    const registerButton = page.getByRole('button', { name: 'Register' });

    // Verify form fields are visible
    await expect(usernameField).toBeVisible();
    await expect(passwordField).toBeVisible();
    await expect(confirmPasswordField).toBeVisible();
    await expect(tokenField).toBeVisible();
    await expect(registerButton).toBeVisible();

    // Fill out the form
    await usernameField.fill('testuser');
    await passwordField.fill('password123');
    await confirmPasswordField.fill('password123');
    await tokenField.fill('sample-token');

    // Submit the form
    await registerButton.click();

});
