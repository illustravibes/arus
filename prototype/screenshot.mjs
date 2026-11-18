import { mkdir } from 'node:fs/promises';
import path from 'node:path';
import { chromium } from 'playwright';

const width = Number(process.env.ARUS_WIDTH || 390);
const height = Number(process.env.ARUS_HEIGHT || 844);
const url = process.env.ARUS_URL || 'http://127.0.0.1:5500/prototype/index.html';
const output = process.env.ARUS_SCREENSHOT || path.join('screenshots', `${width}x${height}.png`);

await mkdir(path.dirname(output), { recursive: true });
const browser = await chromium.launch({ headless: true });
try {
  const page = await browser.newPage({ viewport: { width, height }, deviceScaleFactor: 1 });
  await page.goto(url, { waitUntil: 'domcontentloaded' });
  await page.locator('#app .shell').waitFor({ state: 'visible' });
  await page.screenshot({ path: output, fullPage: false });
  console.log(`Saved ${output} (${width}×${height}) from ${url}`);
} finally {
  await browser.close();
}
