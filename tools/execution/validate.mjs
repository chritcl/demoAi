import { readFileSync, readdirSync, statSync, existsSync } from 'node:fs';
import { join, dirname, extname, resolve, relative } from 'node:path';
import { execFileSync } from 'node:child_process';
import { fileURLToPath } from 'node:url';

const ROOT = dirname(fileURLToPath(import.meta.url)) + '/../..';
const docsDir = join(ROOT, 'docs');

const errors = [];
const warnings = [];

function red(s) { return '\x1b[31m' + s + '\x1b[0m'; }
function green(s) { return '\x1b[32m' + s + '\x1b[0m'; }
function yellow(s) { return '\x1b[33m' + s + '\x1b[0m'; }
function fail(m) { errors.push(m); console.log(red('✗ ' + m)); }
function warn(m) { warnings.push(m); console.log(yellow('! ' + m)); }
function ok(m) { console.log(green('✓ ' + m)); }

function readText(file) {
  try {
    return readFileSync(file, 'utf-8');
  } catch (e) {
    return null;
  }
}

function fileExists(file) {
  return existsSync(file);
}

function exec(cmd, args) {
  try {
    return { status: 0, stdout: execFileSync(cmd, args, { encoding: 'utf-8', cwd: ROOT }) };
  } catch (e) {
    return { status: e.status || 1, stderr: e.stderr || e.message };
  }
}

function listFiles(dir, predicate) {
  const result = [];
  if (!existsSync(dir)) return result;
  const entries = readdirSync(dir, { withFileTypes: true });
  for (const entry of entries) {
    const full = join(dir, entry.name);
    if (entry.isDirectory()) {
      result.push(...listFiles(full, predicate));
    } else if (predicate(full, entry.name)) {
      result.push(full);
    }
  }
  return result;
}

function parseMarkdownTable(text, startMarker) {
  const lines = text.split('\n');
  let start = -1;
  for (let i = 0; i < lines.length; i++) {
    if (lines[i].includes(startMarker)) {
      start = i;
      break;
    }
  }
  if (start === -1) return [];
  let dataStart = -1;
  for (let i = start + 1; i < lines.length; i++) {
    if (/^\|[-:\s|]+\|$/u.test(lines[i].trim())) {
      dataStart = i + 1;
      break;
    }
  }
  if (dataStart === -1) return [];
  const rows = [];
  for (let i = dataStart; i < lines.length; i++) {
    const line = lines[i].trim();
    if (!line.startsWith('|') || !line.endsWith('|')) break;
    const cells = line.slice(1, -1).split('|').map(c => c.trim());
    rows.push(cells);
  }
  return rows;
}

function isTextFile(file) {
  const ext = extname(file).toLowerCase();
  const binaryExts = ['.png', '.jpg', '.jpeg', '.gif', '.webp', '.mp4', '.mp3', '.pdf', '.zip', '.tar', '.gz', '.exe', '.dll', '.so', '.dylib', '.bin', '.ico', '.woff', '.woff2', '.ttf', '.eot', '.otf', '.svg', '.xlsx'];
  return !binaryExts.includes(ext);
}

function normalizePath(file) {
  return file.replace(/\\/g, '/');
}

function removeCodeBlocks(text) {
  return text.replace(/```[\s\S]*?```/g, '');
}

function extractMarkdownLinks(text) {
  const links = [];
  const regex = /\[([^\]]*)\]\(([^)]+)\)/g;
  let match;
  while ((match = regex.exec(text)) !== null) {
    links.push(match[2]);
  }
  return links;
}

function isExternalUrl(url) {
  return /^https?:\/\//i.test(url) || /^mailto:/i.test(url) || url.startsWith('#');
}

// 15.1 必需文件
function validateRequiredFiles() {
  const required = [
    'AGENTS.md',
    'docs/GOAL.md',
    'docs/EXECUTION.md',
    'docs/STATE.json',
    'docs/ARCHITECTURE.md',
    'docs/功能开发清单.xlsx',
    'docs/work-packages',
    'old/README.md'
  ];
  for (const rel of required) {
    if (!fileExists(join(ROOT, rel))) {
      fail('必需文件缺失：' + rel);
    } else {
      ok('必需文件存在：' + rel);
    }
  }
}

// 15.2 STATE 结构
function validateState() {
  const stateText = readText(join(ROOT, 'docs', 'STATE.json'));
  if (!stateText) {
    fail('无法读取 STATE.json');
    return null;
  }

  let state;
  try {
    state = JSON.parse(stateText);
  } catch (e) {
    fail('STATE.json 格式错误：' + e.message);
    return null;
  }

  if (!state.goal) fail('STATE.json 缺少 goal');
  const validStages = ['S0', 'S1', 'S2', 'S3', 'S4'];
  if (!validStages.includes(state.stage)) fail('STATE.json 中 stage 不合法：' + state.stage);
  const validGates = [null, 'G0', 'G1', 'G2', 'G3', 'G4'];
  if (!validGates.includes(state.gate)) fail('STATE.json 中 gate 不合法：' + state.gate);
  const validGateStatuses = [null, 'pending', 'waiting_human', 'approved', 'failed'];
  if (!validGateStatuses.includes(state.gate_status)) fail('STATE.json 中 gate_status 不合法：' + state.gate_status);

  const workPackages = state.work_packages || [];

  // 工作包 ID 唯一
  const ids = new Set();
  for (const wp of workPackages) {
    if (ids.has(wp.id)) fail('工作包 ID 重复：' + wp.id);
    ids.add(wp.id);
  }

  // 工作包 spec 存在
  for (const wp of workPackages) {
    if (!wp.spec) fail('工作包 ' + wp.id + ' 缺少 spec');
    else if (!fileExists(join(ROOT, wp.spec))) fail('工作包 ' + wp.id + ' 的 spec 文件不存在：' + wp.spec);
  }

  // 工作包状态合法
  const validStatuses = ['draft', 'ready', 'in_progress', 'blocked', 'done'];
  for (const wp of workPackages) {
    if (!validStatuses.includes(wp.status)) fail('工作包 ' + wp.id + ' 状态不合法：' + wp.status);
  }

  // 最多一个 in_progress
  const inProgress = workPackages.filter(w => w.status === 'in_progress');
  if (inProgress.length > 1) fail('in_progress 工作包超过一个：' + inProgress.map(w => w.id).join(', '));

  // current_work_package 与 in_progress 一致
  if (inProgress.length === 1) {
    if (state.current_work_package !== inProgress[0].id) {
      fail('current_work_package (' + state.current_work_package + ') 与 in_progress 工作包 (' + inProgress[0].id + ') 不一致');
    }
  } else if (state.current_work_package != null) {
    warn('没有 in_progress 工作包，但 current_work_package 不为 null');
  }

  // 所有依赖 ID 存在
  for (const wp of workPackages) {
    for (const dep of wp.depends_on || []) {
      if (!ids.has(dep)) fail('工作包 ' + wp.id + ' 的依赖 ' + dep + ' 不存在');
    }
  }

  // 无循环依赖
  validateCycleDependencies(workPackages);

  // ready、in_progress、done 的依赖全部为 done
  const doneSet = new Set(workPackages.filter(w => w.status === 'done').map(w => w.id));
  for (const wp of workPackages) {
    if (['ready', 'in_progress', 'done'].includes(wp.status)) {
      for (const dep of wp.depends_on || []) {
        if (!doneSet.has(dep)) fail('工作包 ' + wp.id + ' 状态为 ' + wp.status + '，但依赖 ' + dep + ' 未完成');
      }
    }
  }

  ok('STATE.json 结构检查通过');
  return state;
}

function validateCycleDependencies(workPackages) {
  const graph = new Map();
  for (const wp of workPackages) {
    graph.set(wp.id, wp.depends_on || []);
  }
  const status = new Map();
  const path = [];
  for (const wp of workPackages) {
    status.set(wp.id, 'unvisited');
  }
  function dfs(id) {
    status.set(id, 'visiting');
    path.push(id);
    for (const dep of graph.get(id) || []) {
      if (!graph.has(dep)) continue;
      const s = status.get(dep);
      if (s === 'visiting') {
        const cycleStart = path.indexOf(dep);
        const cycle = path.slice(cycleStart).concat([dep]);
        fail('工作包存在循环依赖：' + cycle.join(' -> '));
        return true;
      }
      if (s === 'unvisited') {
        if (dfs(dep)) return true;
      }
    }
    path.pop();
    status.set(id, 'visited');
    return false;
  }
  for (const wp of workPackages) {
    if (status.get(wp.id) === 'unvisited') {
      if (dfs(wp.id)) return;
    }
  }
  ok('循环依赖检查通过');
}

// 15.3 门禁关系与跨阶段约束
function validateGateRelationship(state) {
  if (!state) return;
  const stageToGate = { S0: 'G0', S1: 'G1', S2: 'G2', S3: 'G3', S4: 'G4' };
  const expectedGate = stageToGate[state.stage];

  // 门禁必须与当前阶段对应
  if (state.gate != null && state.gate !== expectedGate) {
    fail('门禁 ' + state.gate + ' 与当前阶段 ' + state.stage + ' 不匹配，期望 ' + expectedGate);
  }

  // waiting_human 期间不得存在 ready 或 in_progress
  if (state.gate_status === 'waiting_human') {
    if (state.gate == null) fail('gate_status 为 waiting_human 但 gate 为 null');
    const active = (state.work_packages || []).filter(w => w.status === 'ready' || w.status === 'in_progress');
    if (active.length > 0) fail('gate_status 为 waiting_human 但存在 ready/in_progress 工作包：' + active.map(w => w.id).join(', '));
  }

  // ready 和 in_progress 工作包必须属于当前阶段
  const workPackages = state.work_packages || [];
  for (const wp of workPackages) {
    if ((wp.status === 'ready' || wp.status === 'in_progress') && wp.stage !== state.stage) {
      fail('工作包 ' + wp.id + ' 状态为 ' + wp.status + ' 但属于阶段 ' + wp.stage + '，当前阶段为 ' + state.stage);
    }
  }

  ok('门禁关系与跨阶段约束检查通过');
}

// 15.4 工作包一致性
function validateWorkPackageConsistency(state) {
  if (!state) return;
  const workPackages = state.work_packages || [];

  for (const wp of workPackages) {
    const specPath = join(ROOT, wp.spec);
    const text = readText(specPath);
    if (!text) {
      fail('工作包 ' + wp.id + ' 的 Markdown 文件无法读取：' + wp.spec);
      continue;
    }

    // 检查 ID 一致
    if (!text.includes('**ID**: ' + wp.id) && !text.includes('ID**: ' + wp.id)) {
      fail('工作包 ' + wp.id + ' 的 Markdown 中 ID 与 STATE 不一致');
    }

    // 检查不包含动态状态
    const statusPatterns = [/^状态[：:]\s*(draft|ready|in_progress|done|blocked)/m];
    for (const re of statusPatterns) {
      if (re.test(text)) {
        fail('工作包 ' + wp.id + ' 的 Markdown 中包含动态状态（应在 STATE.json 中管理）');
        break;
      }
    }

    // 检查 Excel 功能引用
    if (!text.includes('覆盖的 Excel 功能')) {
      fail('工作包 ' + wp.id + ' 缺少"覆盖的 Excel 功能"章节');
    }

    // 检查验收标准
    if (!text.includes('验收标准')) {
      fail('工作包 ' + wp.id + ' 缺少验收标准');
    }

    // 检查验证命令
    if (!text.includes('验证命令')) {
      fail('工作包 ' + wp.id + ' 缺少验证命令');
    }
  }

  ok('工作包一致性检查通过');
}

// 15.5 文档链接
function validateDocLinks() {
  const filesToCheck = [
    'README.md',
    'AGENTS.md',
    'docs/README.md',
    'docs/GOAL.md',
    'docs/EXECUTION.md',
    'docs/ARCHITECTURE.md'
  ];

  // 添加工作包
  const wpDir = join(ROOT, 'docs', 'work-packages');
  if (existsSync(wpDir)) {
    for (const f of readdirSync(wpDir)) {
      if (f.endsWith('.md')) filesToCheck.push('docs/work-packages/' + f);
    }
  }

  // 添加流程文档
  const flowsDir = join(ROOT, 'docs', 'reference', 'flows');
  if (existsSync(flowsDir)) {
    for (const f of readdirSync(flowsDir)) {
      if (f.endsWith('.md')) filesToCheck.push('docs/reference/flows/' + f);
    }
  }

  filesToCheck.push('old/README.md');

  for (const rel of filesToCheck) {
    const file = join(ROOT, rel);
    const text = readText(file);
    if (!text) continue;
    const baseDir = dirname(file);
    const withoutCode = removeCodeBlocks(text);
    const links = extractMarkdownLinks(withoutCode);
    for (const url of links) {
      if (!url) continue;
      if (isExternalUrl(url)) continue;
      if (url.startsWith('data:')) continue;
      const target = resolve(baseDir, url.split('#')[0]);
      if (!fileExists(target)) {
        fail(rel + ' 中的相对链接目标不存在：' + url);
      }
    }
  }

  ok('文档链接检查通过');
}

// 15.6 重复功能检查
function validateDuplicateFunctions() {
  const wpDir = join(ROOT, 'docs', 'work-packages');
  if (!existsSync(wpDir)) return;

  const wpFiles = readdirSync(wpDir).filter(f => f.endsWith('.md')).map(f => join(wpDir, f));
  const functionMap = new Map(); // key: "一级功能|二级功能" -> [wp ids]

  for (const file of wpFiles) {
    const text = readText(file);
    if (!text) continue;
    const basename = file.replace(/.*[/\\]/, '').replace('.md', '');
    const wpId = basename.toUpperCase().replace('WP-', 'WP-');

    // 跳过集成验证类和基础设施类工作包
    if (text.includes('集成验证') || text.includes('不直接对应 Excel 功能')) continue;

    const rows = parseMarkdownTable(text, '序号 | 一级功能');
    for (const row of rows) {
      if (row.length < 3) continue;
      const level1 = row[1] ? row[1].trim() : '';
      const level2 = row[2] ? row[2].trim() : '';
      if (!level1 || !level2) continue;
      const key = level1 + '|' + level2;
      if (!functionMap.has(key)) functionMap.set(key, []);
      functionMap.get(key).push(wpId);
    }
  }

  for (const [key, wps] of functionMap) {
    // 过滤掉基础设施类工作包的重复
    const businessWps = wps.filter(wp => wp !== 'WP-01' && wp !== 'WP-04');
    if (businessWps.length > 1) {
      fail('功能 "' + key.replace('|', ' > ') + '" 同时出现在多个业务工作包：' + businessWps.join(', '));
    }
  }

  // 特别检查 WP-02 和 WP-04 的 Excel 功能表不应同时拥有文件设置或操作日志
  const wp02Text = readText(join(wpDir, 'WP-02.md')) || '';
  const wp04Text = readText(join(wpDir, 'WP-04.md')) || '';
  const wp02Rows = parseMarkdownTable(wp02Text, '序号 | 一级功能');
  const wp04Rows = parseMarkdownTable(wp04Text, '序号 | 一级功能');
  const wp02Functions = new Set(wp02Rows.map(r => (r[2] || '').trim()).filter(Boolean));
  const wp04Functions = new Set(wp04Rows.map(r => (r[2] || '').trim()).filter(Boolean));
  for (const func of wp02Functions) {
    if (wp04Functions.has(func)) {
      fail('WP-02 和 WP-04 的 Excel 功能表同时包含"' + func + '"');
    }
  }

  ok('重复功能检查通过');
}

// 15.7 凭据扫描
function scanCredentials() {
  const textFiles = listFiles(ROOT, (f, name) => {
    return isTextFile(f) && !name.endsWith('.lock') && !f.includes('node_modules') && !f.includes('.git');
  });

  const patterns = [
    { re: /password\s*[:=]\s*['"][^'"]{4,}['"]/gi, label: '明文密码' },
    { re: /secret\s*[:=]\s*['"][^'"]{4,}['"]/gi, label: '明文密钥' },
    { re: /api[_-]?key\s*[:=]\s*['"][^'"]{8,}['"]/gi, label: 'API Key' },
    { re: /jdbc:mysql:\/\/[^\s]+:[^\s@]+@/gi, label: '数据库连接字符串' },
    { re: /AKLT[A-Za-z0-9_]{16,}/g, label: '阿里云 AccessKey' }
  ];

  // 占位符模式，不视为真实凭据
  const placeholderPatterns = [
    /\$\{[^}]+\}/,
    /<REDACTED>/i,
    /CHANGE_ME/i,
    /your[_-]?(password|secret|key)/i,
    /xxx/i,
    /placeholder/i
  ];

  const knownLegacy = /old[\\/]/;
  let scanned = 0;
  for (const file of textFiles) {
    const content = readText(file);
    if (!content) continue;
    scanned++;
    for (const { re, label } of patterns) {
      const matches = content.match(re);
      if (matches) {
        // 检查是否为占位符
        const isPlaceholder = matches.every(m =>
          placeholderPatterns.some(p => p.test(m))
        );
        if (isPlaceholder) continue;

        if (knownLegacy.test(file)) {
          warn('遗留文件 ' + relative(ROOT, file) + ' 包含 ' + label);
        } else {
          fail('文件 ' + relative(ROOT, file) + ' 包含 ' + label + '，请使用环境变量或密钥管理服务');
        }
      }
    }
  }

  ok('凭据扫描完成，已扫描 ' + scanned + ' 个文本文件');
}

// 15.8 Git 提交追踪
function validateCommitTrace(state) {
  if (!state) return;
  const result = exec('git', ['log', '--oneline', '--all']);
  if (result.status !== 0) {
    warn('无法获取 Git 日志');
    return;
  }
  const lines = result.stdout.split('\n').filter(Boolean);
  const forbidden = ['1', 'add', 'update', 'fix', 'done'];

  // 只检查包含工作包引用的新体系提交，跳过历史旧提交
  const newSystemCommits = lines.filter(line => /\(WP-\d+\)/.test(line));
  for (const line of newSystemCommits) {
    const msg = line.replace(/^\S+\s+/, '');
    if (forbidden.includes(msg)) {
      fail('提交信息包含禁用词：' + line);
    }
  }

  const doneWps = (state.work_packages || []).filter(w => w.status === 'done').map(w => w.id);
  for (const wp of doneWps) {
    if (!lines.some(line => line.includes('(' + wp + ')'))) {
      fail('工作包 ' + wp + ' 为 done，但 Git 历史中没有包含 (' + wp + ') 的提交');
    }
  }

  ok('提交追踪检查通过');
}

async function main() {
  console.log('开始执行体系验证...\n');

  validateRequiredFiles();
  const state = validateState();
  validateGateRelationship(state);
  validateWorkPackageConsistency(state);
  validateDocLinks();
  validateDuplicateFunctions();
  scanCredentials();
  validateCommitTrace(state);

  console.log('\n---');
  if (errors.length === 0) {
    console.log(green('验证通过：0 个错误，' + warnings.length + ' 个警告'));
    process.exit(0);
  } else {
    console.log(red('验证失败：' + errors.length + ' 个错误，' + warnings.length + ' 个警告'));
    process.exit(1);
  }
}

main().catch(e => {
  console.error(red('验证器异常：' + e.message));
  process.exit(1);
});
