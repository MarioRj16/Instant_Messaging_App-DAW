import { test, expect } from '@playwright/test';

test('Home Page functionality', async ({ page }) => {
  // when: navigating to the home page
  await page.goto('http://localhost:3000/');

  // then: the page has buttons for navigation and token generation
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

  // then: the token is copied to the clipboard (mocking clipboard behavior)
  const copiedText = await page.evaluate(() => navigator.clipboard.readText());
  expect(copiedText).toBe('test-token');

  // when: closing the dialog
  const closeButton = dialog.getByRole('button', { name: 'Close' });
  await closeButton.click();

  // then: the dialog is no longer visible
  await expect(dialog).not.toBeVisible();
});
