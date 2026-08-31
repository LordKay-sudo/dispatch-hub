import { expect, test } from '@playwright/test';

async function login(page: import('@playwright/test').Page, username: string, tenant: string) {
  await page.goto('/');
  await expect(page.getByRole('button', { name: 'Login' })).toBeVisible();

  await page.locator('input[name="username"]').fill(username);
  await page.locator('input[name="password"]').fill('password');
  await page.locator('input[name="tenantCode"]').fill(tenant);

  const loginResponsePromise = page.waitForResponse(
    (res) => res.url().includes('/api/v1/auth/login') && res.request().method() === 'POST',
    { timeout: 20_000 },
  );
  await page.getByRole('button', { name: 'Login' }).click();
  const loginResponse = await loginResponsePromise;
  expect(loginResponse.status(), await loginResponse.text()).toBe(200);
  await expect(page).toHaveURL(/\/app\//, { timeout: 15_000 });
}

test.describe('Dispatch Hub smoke', () => {
  test('login, create destination, submit event', async ({ page }) => {
    const suffix = Date.now();
    await login(page, 'admin.acme', 'acme');

    await page.getByRole('link', { name: 'Destinations' }).click();
    await expect(page.getByText('Create destination')).toBeVisible();

    await page.locator('input[name="name"]').fill(`e2e-echo-${suffix}`);
    await page.locator('input[name="targetUrl"]').fill('http://webhook-echo:5678/');
    await page.getByRole('button', { name: 'Create' }).click();
    await expect(page.getByText(`e2e-echo-${suffix}`)).toBeVisible();

    await page.getByRole('link', { name: 'Events' }).click();
    await page.locator('input[name="idempotencyKey"]').fill(`e2e-key-${suffix}`);
    await page.locator('textarea[name="payloadText"]').fill(`{"source":"playwright","n":${suffix}}`);
    await page.getByRole('button', { name: 'Submit event' }).click();

    await expect(page.getByText(`e2e-key-${suffix}`)).toBeVisible({ timeout: 20_000 });
    await expect(page.getByText('SUCCESS').first()).toBeVisible({ timeout: 45_000 });
  });

  test('viewer cannot create destinations', async ({ page }) => {
    await login(page, 'viewer.acme', 'acme');
    await page.getByRole('link', { name: 'Destinations' }).click();
    await expect(page.getByText('Create destination')).toHaveCount(0);
  });
});
