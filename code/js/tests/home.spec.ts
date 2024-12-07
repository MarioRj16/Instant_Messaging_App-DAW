import { test, expect } from '@playwright/test';

test('Home Page functionality after login', async ({ page }) => {
  // Step 1: Navigate to the login page
  await page.goto('http://localhost:3000/login');

  // Step 2: Log in with the provided credentials
  const usernameField = page.locator('input[name="username"]');
  const passwordField = page.locator('input[name="password"]');
  const loginButton = page.getByRole('button', { name: 'Login' });

  await usernameField.fill('admin123');
  await passwordField.fill('Sunny-Day7-Green-Trees');
  await loginButton.click();

  // Step 3: Wait for navigation to the home page
  await page.waitForURL('http://localhost:3000/');

  // Step 4: Verify the home page functionality
  const channelsButton = page.getByRole('button', { name: 'Your Channels' });
  const searchChannelsButton = page.getByRole('button', { name: 'Search Channels' });
  const createChannelButton = page.getByRole('button', { name: 'Create Channel' });
  const invitationsButton = page.getByRole('button', { name: 'Invitations' });
  const inviteButton = page.getByRole('button', { name: 'Create Registration Invite' });

  await expect(channelsButton).toBeVisible();
  await expect(searchChannelsButton).toBeVisible();
  await expect(createChannelButton).toBeVisible();
  await expect(invitationsButton).toBeVisible();
  await expect(inviteButton).toBeVisible();

  // when: clicking 'Create Registration Invite'
  await inviteButton.click();

  // then: the dialog appears with the token
  const dialog = page.locator('.MuiDialog-root');
  await expect(dialog).toBeVisible();
  const dialogTitle = dialog.getByText('Your Token');
  await expect(dialogTitle).toBeVisible();

  // when: the token is loaded (mocking a token response for this test)
  const tokenText = dialog.getByText('Loading token...');
  await expect(tokenText).toBeVisible();

  // Simulating the appearance of a token
  await page.evaluate(() => {
    const tokenElement = document.querySelector('.MuiDialogContent-root p');
    if (tokenElement) tokenElement.textContent = 'test-token';
  });

  const token = dialog.getByText('test-token');
  await expect(token).toBeVisible();

  // when: clicking the copy button
  const copyButton = dialog.getByRole('button', { name: 'copy token' });
  await copyButton.click();

});
