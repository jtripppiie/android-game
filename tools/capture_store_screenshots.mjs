// Capture real local gameplay; no image compositing or simulated UI.
// npm install playwright in your tooling environment, or set PLAYWRIGHT_MODULE.
import fs from 'node:fs/promises';
import path from 'node:path';
import {fileURLToPath} from 'node:url';
const {chromium}=await import(process.env.PLAYWRIGHT_MODULE || 'playwright');
const root=path.resolve(path.dirname(fileURLToPath(import.meta.url)),'..');
const output=path.join(root,'store/screenshots');
await fs.mkdir(output,{recursive:true});
const browser=await chromium.launch({headless:true,...(process.env.CHROMIUM_PATH?{executablePath:process.env.CHROMIUM_PATH}:{}),args:['--no-sandbox','--enable-unsafe-swiftshader']});
const page=await browser.newPage({viewport:{width:1920,height:1080}});
const errors=[];
page.on('pageerror',e=>errors.push(String(e)));
page.on('console',m=>{if(m.type()==='error')errors.push(m.text());});
const click=async(x,y)=>page.mouse.click(x*4,y*4);
const shot=async(name)=>page.screenshot({path:path.join(output,name+'.png')});
try {
 await page.goto(process.env.GAME_URL || 'http://127.0.0.1:8060/');
 await page.waitForFunction(()=>!document.getElementById('status'),{},{timeout:60000});
 await page.waitForTimeout(1000);
 await shot('01-menu');
 await click(100,191);
 await page.waitForTimeout(4700);
 await shot('02-race');
 await page.keyboard.press('p');await click(240,191);
 await click(350,191);
 await click(420,231); // Hazard palette.
 await click(90,253);await click(320,126); // Water, upper lane.
 await click(390,253);await click(320,204); // Bus, lower lane.
 await page.waitForTimeout(300);
 await shot('03-track-builder');
 await click(32,52);await click(135,12);
 for(let i=0;i<15;i++)await click(370,106);
 for(let i=0;i<2;i++)await click(370,140);
 for(let i=0;i<2;i++)await click(370,174);
 await shot('04-stunt-setup');
 await click(297,236);
 await page.waitForTimeout(6200);
 await shot('05-stunt-jump');
 await page.waitForTimeout(8500);
 await shot('06-stunt-result');
 if(errors.length)throw Error(errors.join('\n'));
 console.log('PASS: six 1920x1080 local gameplay screenshots captured without browser errors');
} finally {await browser.close();}
