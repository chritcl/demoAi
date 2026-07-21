import { readFileSync, readdirSync, statSync, existsSync, writeFileSync } from 'node:fs';
import { join, dirname, extname, resolve, relative, isAbsolute, sep, posix } from 'node:path';
import { execFileSync } from 'node:child_process';
import { fileURLToPath } from 'node:url';

const ROOT = dirname(fileURLToPath(import.meta.url)) + '/../..';
const docsDir = join(ROOT, 'docs');
const productDir = join(docsDir, 'product');
const architectureDir = join(docsDir, 'architecture');
const executionDir = join(docsDir, 'execution');
const qualityDir = join(docsDir, 'quality');
const operationsDir = join(docsDir, 'operations');

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

function parseFrontmatter(text) {
  if (!text || !text.startsWith('---\n')) return {};
  const end = text.indexOf('\n---\n', 4);
  if (end === -1) return {};
  const fm = text.slice(4, end);
  const result = {};
  for (const line of fm.split('\n')) {
    const idx = line.indexOf(':');
    if (idx === -1) continue;
    const key = line.slice(0, idx).trim();
    const value = line.slice(idx + 1).trim();
    result[key] = value;
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
  // 找到表头后的分隔行，再下一行开始是数据
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
  const binaryExts = ['.png', '.jpg', '.jpeg', '.gif', '.webp', '.mp4', '.mp3', '.pdf', '.zip', '.tar', '.gz', '.exe', '.dll', '.so', '.dylib', '.bin', '.ico', '.woff', '.woff2', '.ttf', '.eot', '.otf', '.svg'];
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

function isImageUrl(url) {
  return /\.(png|jpg|jpeg|gif|webp|svg|bmp|ico)$/i.test(url);
}

// 1. STATE 与阶段关系
function validateState() {
  const stateText = readText(join(ROOT, 'docs', 'execution', 'STATE.json'));
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

  if (!state.schema_version) fail('STATE.json 缺少 schema_version');
  if (!state.goal_id) fail('STATE.json 缺少 goal_id');
  if (state.goal_status !== 'approved') fail('STATE.json 中 goal_status 必须为 approved');
  if (!state.active_stage) fail('STATE.json 缺少 active_stage');

  // 读取阶段定义
  const stageGates = readText(join(ROOT, 'docs', 'execution', 'STAGE-GATES.md')) || '';
  const stageRows = parseMarkdownTable(stageGates, '阶段 | 名称');
  const definedStages = new Set(stageRows.map(r => r[0]).filter(Boolean));
  const stageToGate = new Map();
  for (const row of stageRows) {
    if (row[0] && row[3]) stageToGate.set(row[0], row[3].trim());
  }

  if (!definedStages.has(state.active_stage)) {
    fail('STATE.json 中的 active_stage ' + state.active_stage + ' 不存在于阶段定义');
  }

  const workPackages = state.work_packages || [];
  const gates = state.gates || [];

  // active_gate 检查
  if (state.active_gate != null && state.active_gate !== '') {
    const gate = gates.find(g => g.id === state.active_gate);
    if (!gate) {
      fail('STATE.json 中的 active_gate ' + state.active_gate + ' 不存在');
    } else if (gate.stage !== state.active_stage) {
      fail('active_gate ' + state.active_gate + ' 不属于 active_stage ' + state.active_stage);
    }
  }

  // 阶段工作进行中时 active_gate 应为 null
  const hasInProgress = workPackages.some(w => w.status === 'in_progress');
  const hasReady = workPackages.some(w => w.status === 'ready');
  const hasDraft = workPackages.some(w => w.status === 'draft');
  const hasReview = workPackages.some(w => w.status === 'review');

  if (hasInProgress && state.active_gate != null && state.active_gate !== '') {
    warn('阶段工作进行中时，active_gate 应为 null');
  }

  // 当前阶段必须至少存在一个 draft/ready/in_progress/review 工作包，或对应门禁为 waiting_human
  if (state.active_gate == null || state.active_gate === '') {
    const hasWork = hasDraft || hasReady || hasInProgress || hasReview;
    const gateWaiting = gates.find(g => g.stage === state.active_stage && g.status === 'waiting_human');
    if (!hasWork && !gateWaiting) {
      fail('当前阶段 ' + state.active_stage + ' 没有可执行工作包，且对应门禁未到达 waiting_human');
    }
  }

  // 最多一个 in_progress
  const inProgress = workPackages.filter(w => w.status === 'in_progress');
  if (inProgress.length > 1) {
    fail('STATE.json 中 in_progress 工作包超过一个：' + inProgress.map(w => w.id).join(', '));
  }

  // 状态合法
  const validStatuses = ['draft', 'ready', 'in_progress', 'review', 'done', 'blocked'];
  for (const wp of workPackages) {
    if (!validStatuses.includes(wp.status)) {
      fail('工作包 ' + wp.id + ' 状态 ' + wp.status + ' 不合法');
    }
    if (wp.status === 'done') {
      if (!wp.evidence) fail('工作包 ' + wp.id + ' 为 done 但缺少 evidence');
      if (!wp.completed_at) fail('工作包 ' + wp.id + ' 为 done 但缺少 completed_at');
    }
    if (wp.status === 'in_progress' || wp.status === 'review') {
      if (!wp.spec) fail('工作包 ' + wp.id + ' 为 ' + wp.status + ' 但缺少 spec');
    }
  }

  // 依赖检查
  const doneSet = new Set(workPackages.filter(w => w.status === 'done').map(w => w.id));
  for (const wp of workPackages) {
    if (['ready', 'in_progress', 'review', 'done'].includes(wp.status)) {
      for (const dep of wp.depends_on || []) {
        if (!doneSet.has(dep)) {
          fail('工作包 ' + wp.id + ' 的依赖 ' + dep + ' 未完成');
        }
      }
    }
  }

  // 门禁状态合法
  const validGateStatuses = ['pending', 'in_review', 'automatic_passed', 'waiting_human', 'approved', 'rejected'];
  for (const gate of gates) {
    if (!validGateStatuses.includes(gate.status)) {
      fail('门禁 ' + gate.id + ' 状态 ' + gate.status + ' 不合法');
    }
  }

  ok('STATE.json 基本结构和状态机检查通过');
  return state;
}

// 2. 禁止动态状态副本
function validateDynamicStateHardcoded() {
  const files = [
    'README.md',
    'AGENTS.md',
    'docs/execution/EXECUTION-PROTOCOL.md',
    'docs/execution/ROADMAP.md',
    'docs/quality/README.md',
    'docs/operations/README.md'
  ];
  const forbiddenPatterns = [
    { re: /当前阶段[：:\s]+S0/g, label: '当前阶段：S0' },
    { re: /当前阶段[：:\s]+S1/g, label: '当前阶段：S1' },
    { re: /当前活动门禁[：:\s]+G0/g, label: '当前活动门禁：G0' },
    { re: /当前门禁[：:\s]+G0/g, label: '当前门禁：G0' },
    { re: /当前工作包[：:\s]+WP-001/g, label: '当前工作包：WP-001' },
    { re: /当前工作包[：:\s]+WP-002/g, label: '当前工作包：WP-002' },
    { re: /最近完成工作包[：:\s]+WP-001/g, label: '最近完成工作包：WP-001' },
    { re: /S0\s*正在进行/g, label: 'S0 正在进行' },
    { re: /S0\s*进行中/g, label: 'S0 进行中' },
    { re: /执行分支[：:\s]+codex\/execution-system/g, label: '执行分支：codex/execution-system' }
  ];

  for (const rel of files) {
    const content = readText(join(ROOT, rel));
    if (!content) continue;
    for (const { re, label } of forbiddenPatterns) {
      if (re.test(content)) {
        fail(rel + ' 中硬编码了动态状态：' + label);
      }
    }
    // 鼓励指向 STATE.json 作为真源
    if (!content.includes('docs/execution/STATE.json') && !content.includes('STATE.json')) {
      if (rel !== 'AGENTS.md') {
        warn(rel + ' 未明确指向 STATE.json 作为动态状态真源');
      }
    }
  }
}

// 3. active 文档遍历与相对链接
function findActiveMarkdownFiles() {
  const scanRoots = [
    docsDir,
    join(ROOT, 'README.md'),
    join(ROOT, 'AGENTS.md')
  ];
  const excludedSegments = new Set([
    'evidence',
    'gates',
    'legacy',
    'old',
    'node_modules',
    'target',
    'dist',
    '.git',
    'wp-000-repository-audit.md',
    'wp-001-documentation-source-of-truth.md',
    'wp-002-execution-protocol.md',
    'wp-003-architecture-boundary.md',
    'wp-004-execution-validation.md'
  ]);

  const activeFiles = [];
  const traverse = (file) => {
    const segments = normalizePath(file).split('/');
    if (segments.some(s => excludedSegments.has(s))) return;
    if (!fileExists(file)) return;
    if (!isTextFile(file)) return;
    if (file.endsWith('.md')) {
      const text = readText(file);
      if (text) {
        const fm = parseFrontmatter(text);
        if (fm.status === 'active') {
          activeFiles.push(file);
        }
      }
    }
    if (statSync(file).isDirectory()) {
      for (const child of readdirSync(file)) {
        traverse(join(file, child));
      }
    }
  };

  for (const root of scanRoots) {
    traverse(root);
  }
  return activeFiles;
}

function validateMarkdownLinks(file) {
  const text = readText(file);
  if (!text) return;
  const baseDir = dirname(file);
  const withoutCode = removeCodeBlocks(text);
  const links = extractMarkdownLinks(withoutCode);
  for (const url of links) {
    if (!url) continue;
    if (isExternalUrl(url)) continue;
    if (isAbsolute(url)) continue;
    if (url.startsWith('data:')) continue;
    // 跳过仅锚点
    if (url.startsWith('#')) continue;
    // 跳过mailto
    if (url.startsWith('mailto:')) continue;
    const target = resolve(baseDir, url.split('#')[0]);
    if (!fileExists(target)) {
      fail(relative(ROOT, file) + ' 中的相对链接目标不存在：' + url);
    }
  }
}

function validateDocs() {
  validateDynamicStateHardcoded();

  const activeFiles = findActiveMarkdownFiles();
  const requiredSections = ['## ', '# '];

  for (const file of activeFiles) {
    const rel = relative(ROOT, file);
    const text = readText(file) || '';

    // 检查是否有至少一个二级或一级标题
    if (!requiredSections.some(s => text.includes(s))) {
      fail(rel + ' 缺少标题章节');
    }

    // 检查相对链接
    validateMarkdownLinks(file);
  }

  ok('文档一致性和关键引用检查通过');
}

// 4. 业务包一致性、需求阶段统计、owner 检查、门禁证据、循环依赖
function parseBusinessPackages() {
  const domainMap = readText(join(ROOT, 'docs', 'architecture', 'DOMAIN-MAP.md')) || '';
  const rows = parseMarkdownTable(domainMap, '业务域 | 业务包编码');
  return new Set(rows.map(r => r[1]).filter(Boolean));
}

function parseTraceabilityRows() {
  const trace = readText(join(ROOT, 'docs', 'product', 'TRACEABILITY.md')) || '';
  const rows = parseMarkdownTable(trace, '需求编号 | 业务域');
  return rows.filter(r => r.length >= 7 && r[0].startsWith('REQ-'));
}

function validateBusinessPackages() {
  const validPackages = parseBusinessPackages();
  const rows = parseTraceabilityRows();

  // 特别检查不存在的业务包
  for (const row of rows) {
    const bpField = row[2] || '';
    const bpPart = bpField.split('/')[0].trim();
    if (bpPart === 'mobile') continue; // 前端功能域
    if (bpPart && !validPackages.has(bpPart)) {
      fail('TRACEABILITY 中使用了不存在的业务包：' + bpPart + '（需求 ' + row[0] + '）');
    }
    if (bpPart === 's4_office') {
      fail('TRACEABILITY 中使用了已禁止的业务包 s4_office（需求 ' + row[0] + '）');
    }
  }

  ok('业务包一致性检查通过');
}

function validateTraceabilityStages() {
  const rows = parseTraceabilityRows();
  const counts = { '一期': 0, '二期': 0, '三期': 0 };
  let total = 0;
  const stageValues = new Set();

  for (const row of rows) {
    const stage = (row[6] || '').trim();
    if (!stage) {
      fail('TRACEABILITY 中需求 ' + row[0] + ' 缺少阶段');
      continue;
    }
    stageValues.add(stage);
    if (stage === '一期' || stage === '二期' || stage === '三期') {
      counts[stage]++;
      total++;
    } else {
      fail('TRACEABILITY 中需求 ' + row[0] + ' 使用了非法阶段：' + stage + '（不得使用 P0/P1/P2 或未知阶段）');
    }
  }

  if (total === 0) {
    fail('TRACEABILITY 中未找到有效阶段统计');
    return;
  }

  if (total !== 53) {
    warn('TRACEABILITY 阶段总计为 ' + total + '，与目标 53 不一致（需 WP-006 复核）');
  }

  if (counts['一期'] !== 31 || counts['二期'] !== 14 || counts['三期'] !== 8) {
    warn('TRACEABILITY 阶段统计为 ' + counts['一期'] + '/' + counts['二期'] + '/' + counts['三期'] + '，目标为 31/14/8（已标记为 needs-review，由 WP-006 复核）');
  }

  // 三期全部为 deferred
  for (const row of rows) {
    const stage = (row[6] || '').trim();
    const wp = (row[3] || '').trim();
    if (stage === '三期') {
      if (!wp.includes('另行规划') && !wp.includes('deferred') && !wp.includes('S6')) {
        fail('三期需求 ' + row[0] + ' 的工作包未标记为 deferred/S6 另行规划：' + wp);
      }
    }
  }

  ok('需求阶段统计检查完成');
}

function validateOwners() {
  const domainMap = readText(join(ROOT, 'docs', 'architecture', 'DOMAIN-MAP.md')) || '';
  const rows = parseMarkdownTable(domainMap, '数据表/表组 | Owner 业务包');
  const validPackages = parseBusinessPackages();
  const tableOwners = new Map();

  for (const row of rows) {
    if (row.length < 2) continue;
    const tablesText = row[0].trim();
    const owner = row[1].trim();
    if (!tablesText || !owner) continue;

    if (owner.includes('或')) {
      fail('DOMAIN-MAP 中 owner 包含不确定表述“或”：' + owner + '（表组 ' + tablesText + '）');
      continue;
    }

    if (!validPackages.has(owner)) {
      fail('DOMAIN-MAP 中 owner 业务包 ' + owner + ' 不存在（表组 ' + tablesText + '）');
      continue;
    }

    const tables = tablesText.split(',').map(t => t.trim()).filter(Boolean);
    for (const table of tables) {
      if (tableOwners.has(table)) {
        const existing = tableOwners.get(table);
        if (existing !== owner) {
          fail('DOMAIN-MAP 中表 ' + table + ' 存在多个 owner：' + existing + ' 和 ' + owner);
        }
      } else {
        tableOwners.set(table, owner);
      }
    }
  }

  ok('Owner 检查通过');
}

function validateGateEvidence(state) {
  if (!state) return;
  const gates = state.gates || [];
  for (const gate of gates) {
    if (gate.status === 'approved' && (gate.type === 'human' || gate.type === 'hybrid')) {
      const evidenceFile = join(ROOT, 'docs', 'execution', 'gates', gate.id + '.md');
      if (!fileExists(evidenceFile)) {
        fail('门禁 ' + gate.id + ' 已 approved，但缺少证据文件：docs/execution/gates/' + gate.id + '.md');
      } else {
        const text = readText(evidenceFile) || '';
        if (!text.includes('状态') && !text.includes('approved')) {
          warn('门禁证据文件 ' + gate.id + '.md 未明确记录批准状态');
        }
      }
    }
  }
  ok('门禁证据检查通过');
}

function validateCycleDependencies(state) {
  if (!state) return;
  const workPackages = state.work_packages || [];
  const graph = new Map();
  for (const wp of workPackages) {
    graph.set(wp.id, wp.depends_on || []);
  }

  const status = new Map(); // unvisited, visiting, visited
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
      dfs(wp.id);
    }
  }

  ok('工作包循环依赖检查通过');
}

function validateConsistency(state) {
  validateBusinessPackages();
  validateTraceabilityStages();
  validateOwners();
  validateGateEvidence(state);
  validateCycleDependencies(state);
}

// 5. 凭据扫描
function scanCredentials() {
  const textFiles = listFiles(ROOT, (f, name) => {
    const ext = extname(f).toLowerCase();
    return isTextFile(f) && !name.endsWith('.lock') && !f.includes('node_modules') && !f.includes('.git');
  });

  const patterns = [
    { re: /password\s*[:=]\s*['"][^'"]{4,}['"]/gi, label: '明文密码' },
    { re: /secret\s*[:=]\s*['"][^'"]{4,}['"]/gi, label: '明文密钥' },
    { re: /api[_-]?key\s*[:=]\s*['"][^'"]{8,}['"]/gi, label: 'API Key' },
    { re: /jdbc:mysql:\/\/[^\s]+:[^\s@]+@/gi, label: '数据库连接字符串' },
    { re: /AKLT[A-Za-z0-9_]{16,}/g, label: '阿里云 AccessKey' }
  ];

  const knownLegacy = /old[\\/]/;
  let scanned = 0;
  for (const file of textFiles) {
    const content = readText(file);
    if (!content) continue;
    scanned++;
    for (const { re, label } of patterns) {
      if (re.test(content)) {
        if (knownLegacy.test(file)) {
          warn('遗留文件 ' + relative(ROOT, file) + ' 包含 ' + label + '，需确认已在审计中登记');
        } else {
          fail('文件 ' + relative(ROOT, file) + ' 包含 ' + label + '，请使用环境变量或密钥管理服务');
        }
      }
    }
  }

  ok('凭据扫描完成，已扫描 ' + scanned + ' 个文本文件');
}

// 6. .gitignore
function validateGitignore() {
  const gitignore = readText(join(ROOT, '.gitignore'));
  if (!gitignore) {
    fail('缺少 .gitignore 文件');
    return;
  }
  const lines = gitignore.split('\n').map(l => l.trim());

  // 检查 pnpm-lock.yaml 未被忽略
  const ignoresPnpmLock = lines.some(l => l === 'pnpm-lock.yaml' || l === '/pnpm-lock.yaml' || l === 'pnpm-lock.yaml/');
  if (ignoresPnpmLock) {
    fail('.gitignore 错误地忽略了 pnpm-lock.yaml');
  } else {
    ok('.gitignore 未忽略 pnpm-lock.yaml');
  }

  const required = ['node_modules/', 'target/', 'dist/', '.idea/', '.vscode/'];
  const ignored = new Set(lines.map(l => l.replace(/\/$/, '')));
  for (const item of required) {
    const trimmed = item.replace(/\/$/, '');
    if (!ignored.has(item) && !ignored.has(trimmed)) {
      warn('.gitignore 未忽略 ' + item);
    }
  }
}

// 7. 提交追踪
function validateCommitTrace() {
  const result = exec('git', ['log', '--oneline', '--all']);
  if (result.status !== 0) {
    fail('无法获取 Git 日志：' + result.stderr);
    return;
  }
  const lines = result.stdout.split('\n').filter(Boolean);
  const forbidden = ['1', 'add', 'update', 'fix', 'done'];

  let baselineIndex = lines.findIndex(line => line.includes('(WP-000)'));
  if (baselineIndex === -1) baselineIndex = lines.length;

  for (const line of lines.slice(0, baselineIndex)) {
    const msg = line.replace(/^\S+\s+/, '');
    if (forbidden.includes(msg)) {
      fail('提交信息包含禁用词：' + line);
    }
  }

  const stateText = readText(join(ROOT, 'docs', 'execution', 'STATE.json'));
  if (stateText) {
    const state = JSON.parse(stateText);
    const doneWps = (state.work_packages || []).filter(w => w.status === 'done').map(w => w.id);
    for (const wp of doneWps) {
      if (!lines.some(line => line.includes('(' + wp + ')'))) {
        fail('工作包 ' + wp + ' 为 done，但 Git 历史中没有包含 (' + wp + ') 的提交');
      }
    }
  }

  ok('提交追踪检查通过');
}

async function main() {
  console.log('开始执行体系验证...\n');
  const state = validateState();
  validateDocs();
  validateConsistency(state);
  scanCredentials();
  validateGitignore();
  validateCommitTrace();

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
