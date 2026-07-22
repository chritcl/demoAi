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
    'old'
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

    // 检查 Excel 功能归属或集成引用
    if (!text.includes('覆盖的 Excel 功能') && !text.includes('引用的 Excel 功能')) {
      fail('工作包 ' + wp.id + ' 缺少"覆盖的 Excel 功能"或"引用的 Excel 功能"章节');
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

// 15.6 Excel 功能归属、阶段与集成引用检查
function readExcelFunctions() {
  const script = String.raw`
import json
import sys
from openpyxl import load_workbook

sys.stdout.reconfigure(encoding='utf-8')
workbook = load_workbook('docs/功能开发清单.xlsx', data_only=True, read_only=True)
sheet = workbook['功能开发清单']
headers = [str(value).strip() if value is not None else '' for value in next(sheet.iter_rows(min_row=2, max_row=2, values_only=True))]
columns = {name: index for index, name in enumerate(headers)}
required = ['序号', '一级功能', '二级功能', '阶段']
missing = [name for name in required if name not in columns]
if missing:
    raise ValueError('Excel 缺少字段：' + '、'.join(missing))

features = []
number = ''
level1 = ''
for row_number, row in enumerate(sheet.iter_rows(min_row=3, values_only=True), start=3):
    current_number = row[columns['序号']]
    current_level1 = row[columns['一级功能']]
    level2 = row[columns['二级功能']]
    stage = row[columns['阶段']]
    if current_number not in (None, ''):
        number = str(current_number).strip()
    if current_level1 not in (None, ''):
        level1 = str(current_level1).strip()
    if level2 in (None, '') or stage in (None, ''):
        continue
    features.append({
        'number': number,
        'level1': level1,
        'level2': str(level2).strip(),
        'stage': str(stage).strip(),
        'row': row_number,
    })

print(json.dumps(features, ensure_ascii=False))
`;
  const result = exec('python', ['-c', script]);
  if (result.status !== 0) {
    fail('无法使用 openpyxl 读取功能开发清单.xlsx：' + (result.stderr || '未知错误'));
    return [];
  }
  try {
    return JSON.parse(result.stdout);
  } catch (error) {
    fail('功能开发清单.xlsx 解析结果不是有效 JSON：' + error.message);
    return [];
  }
}

function featureKey(level1, level2, stage) {
  return level1 + '|' + level2 + '|' + stage;
}

function isIntegrationWorkPackage(text) {
  return text.includes('集成验证') || text.includes('G2 准备');
}

function validateExcelFunctionMappings() {
  const expectedPhaseCounts = { '一期': 31, '二期': 14, '三期': 8 };
  const excelFunctions = readExcelFunctions();
  if (excelFunctions.length === 0) return;

  const excelByKey = new Map();
  const excelCountByPhase = new Map();
  for (const feature of excelFunctions) {
    const key = featureKey(feature.level1, feature.level2, feature.stage);
    if (excelByKey.has(key)) {
      fail('Excel 功能重复：' + key.replaceAll('|', ' > '));
    }
    excelByKey.set(key, feature);
    excelCountByPhase.set(feature.stage, (excelCountByPhase.get(feature.stage) || 0) + 1);
  }

  for (const [phase, expectedCount] of Object.entries(expectedPhaseCounts)) {
    const actualCount = excelCountByPhase.get(phase) || 0;
    if (actualCount !== expectedCount) {
      fail('Excel ' + phase + '功能数量为 ' + actualCount + '，期望 ' + expectedCount);
    } else {
      ok('Excel ' + phase + '功能数量为 ' + expectedCount);
    }
  }

  const ownerMap = new Map();
  const referenceMap = new Map();
  const wpDir = join(ROOT, 'docs', 'work-packages');
  const wpFiles = readdirSync(wpDir).filter(file => file.endsWith('.md')).map(file => join(wpDir, file));
  for (const file of wpFiles) {
    const text = readText(file);
    if (!text) continue;
    const wpId = file.replace(/.*[/\\]/, '').replace('.md', '').toUpperCase();
    const targetMap = isIntegrationWorkPackage(text) ? referenceMap : ownerMap;
    const rows = parseMarkdownTable(text, '序号 | 一级功能');
    for (const row of rows) {
      if (row.length < 4) continue;
      const level1 = (row[1] || '').trim();
      const level2 = (row[2] || '').trim();
      const stage = (row[3] || '').trim();
      if (!level1 || !level2 || !stage) continue;
      const key = featureKey(level1, level2, stage);
      if (!excelByKey.has(key)) {
        const stageCandidates = excelFunctions.filter(feature => feature.level1 === level1 && feature.level2 === level2);
        if (stageCandidates.length > 0) {
          fail('工作包 ' + wpId + ' 中功能“' + level1 + ' > ' + level2 + '”阶段为 ' + stage + '，与 Excel 的 ' + stageCandidates.map(feature => feature.stage).join('、') + ' 不一致');
        } else {
          fail('工作包 ' + wpId + ' 引用了 Excel 中不存在的功能：' + level1 + ' > ' + level2 + ' > ' + stage);
        }
        continue;
      }
      if (!targetMap.has(key)) targetMap.set(key, []);
      targetMap.get(key).push(wpId);
    }
  }

  for (const phase of ['一期', '二期']) {
    const expectedFeatures = excelFunctions.filter(feature => feature.stage === phase);
    const ownerRows = [...ownerMap.entries()].filter(([key]) => key.endsWith('|' + phase));
    const ownerCount = ownerRows.reduce((count, [, workPackages]) => count + workPackages.length, 0);
    if (ownerCount !== expectedFeatures.length) {
      fail(phase + '业务工作包映射数量为 ' + ownerCount + '，期望 ' + expectedFeatures.length);
    } else {
      ok(phase + '业务工作包映射数量为 ' + ownerCount);
    }
    for (const feature of expectedFeatures) {
      const key = featureKey(feature.level1, feature.level2, phase);
      const owners = ownerMap.get(key) || [];
      if (owners.length !== 1) {
        fail(phase + '功能“' + feature.level1 + ' > ' + feature.level2 + '”必须恰好由一个业务工作包拥有，当前为：' + (owners.join('、') || '无'));
      }
    }
  }

  for (const [key, owners] of ownerMap) {
    if (owners.length > 1) {
      fail('功能“' + key.replaceAll('|', ' > ') + '”同时由多个业务工作包拥有：' + owners.join('、'));
    }
  }

  for (const [key, references] of referenceMap) {
    const owners = ownerMap.get(key) || [];
    if (owners.length !== 1) {
      fail('集成验证工作包 ' + references.join('、') + ' 引用的功能“' + key.replaceAll('|', ' > ') + '”没有唯一业务所有者');
    }
  }

  ok('Excel 功能归属、阶段与集成引用检查通过');
}

// 15.7 WP-03 与 WP-07 会签边界检查
function validateCountersignDependency() {
  const wpDir = join(ROOT, 'docs', 'work-packages');
  const wp03 = readText(join(wpDir, 'WP-03.md')) || '';
  const wp07 = readText(join(wpDir, 'WP-07.md')) || '';
  const serviceName = 'CountersignWorkflowService';
  const capabilities = ['会签任务', '并行会签节点', '会签结果汇聚', '会签通过条件', '会签人员和结果审计'];
  const declarationFields = ['调用方', '输入', '输出', '错误语义', '数据所有权边界'];

  if (!wp03.includes('WP-07') || !wp03.includes(serviceName)) {
    fail('WP-03 未向 WP-07 声明公开的 ' + serviceName);
  }
  for (const capability of capabilities) {
    if (!wp03.includes(capability)) {
      fail('WP-03 缺少 WP-07 所需的会签能力：' + capability);
    }
  }
  for (const field of declarationFields) {
    if (!wp03.includes(field)) {
      fail('WP-03 跨包 Service 声明缺少：' + field);
    }
  }
  if (!wp07.includes('WP-03') || !wp07.includes(serviceName)) {
    fail('WP-07 未声明依赖 WP-03 的 ' + serviceName);
  }
  for (const capability of capabilities) {
    if (!wp07.includes(capability)) {
      fail('WP-07 未声明使用会签能力：' + capability);
    }
  }
  if (!wp07.includes('自行实现另一套流程引擎')) {
    fail('WP-07 未禁止在公文业务包中自行实现另一套流程引擎');
  }

  ok('WP-03 与 WP-07 会签依赖检查通过');
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
  validateExcelFunctionMappings();
  validateCountersignDependency();
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
