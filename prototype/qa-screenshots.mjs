import { mkdir } from 'node:fs/promises';
import path from 'node:path';
import { chromium } from 'playwright';

const url = process.env.ARUS_URL || 'http://127.0.0.1:5500/prototype/index.html';
const outputDir = process.env.ARUS_QA_DIR || 'screenshots/qa';
const cases = [
  { name: '320x720', width: 320, height: 720 },
  { name: '360x800', width: 360, height: 800 },
  { name: '320x720-text-150', width: 320, height: 720, textScale: 1.5 },
];

await mkdir(outputDir, { recursive: true });
const browser = await chromium.launch({ headless: true });
try {
  for (const item of cases) {
    const page = await browser.newPage({ viewport: { width: item.width, height: item.height }, deviceScaleFactor: 1 });
    await page.goto(url, { waitUntil: 'domcontentloaded' });
    await page.locator('#app .shell').waitFor({ state: 'visible' });
    if (item.textScale) {
      await page.evaluate((scale) => {
        const walker = document.createTreeWalker(document.body, NodeFilter.SHOW_TEXT);
        const textElements = new Set();
        while (walker.nextNode()) {
          if (walker.currentNode.textContent.trim()) textElements.add(walker.currentNode.parentElement);
        }
        for (const element of textElements) {
          const size = Number.parseFloat(getComputedStyle(element).fontSize);
          element.style.setProperty('font-size', `${size * scale}px`, 'important');
        }
        for (const element of document.querySelectorAll('input, select, textarea, button')) {
          const size = Number.parseFloat(getComputedStyle(element).fontSize);
          element.style.setProperty('font-size', `${size * scale}px`, 'important');
        }
      }, item.textScale);
    }
    const output = path.join(outputDir, `${item.name}.png`);
    await page.screenshot({ path: output, fullPage: false });
    console.log(`Saved ${output} (${item.width}×${item.height}${item.textScale ? ', text 150%' : ''})`);
    await page.close();
  }
} finally {
  await browser.close();
}
