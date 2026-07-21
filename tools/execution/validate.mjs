#!/usr/bin/env node
// 执行体系验证器：验证 STATE.json、工作包、文档一致性和凭据风险
// 使用 Node.js 内置模块，无第三方依赖

import { readFile, access, stat } from 'node:fs/promises';
import { constants } from 'node:fs';
import { fileURLToPath } from 'node:url';
import { dirname, join, resolve } from 'node:path';
import { spawnSync } from 'node:child_process';

const __filename = fileURLToPath(import.meta.url);
const __dirname = dirname(__filename);
const ROOT = resolve(__dirname, '..', '..');

// 颜色输出
function red(msg) { return '\u001b[31m' + msg + '\u001b[0m'; }
function green(msg) { return '\u001b[32m' + msg + '\u001b[0m'; }
function yellow(msg) { return '\u001b[33m' + msg + '\u001b[0m'; }

const errors = [];
const warnings = [];

function fail(msg) {
  errors.push(msg);
  console.error(red('✗ ' + msg));
}

function warn(msg) {
  warnings.push(msg);
  console.warn(yellow('! ' + msg));
}

function ok(msg) {
  console.log(green('✓ ' + msg));
}

async function pathExists(p) {
  try {
    await access(p, constants.F_OK);
    return true;
  } catch {
    return false;
  }
}

async function readText(p) {
  try {
    return await readFile(p, 'utf8');
  } catch (e) {
    return null;
  }
}

function exec(cmd, args, options) {
  const result = spawnSync(cmd, args || [], {
    encoding: 'utf8',
    cwd: ROOT,
    shell: false,
    ...(options || {})
  });
  return {
    stdout: (result.stdout || '').trim(),
    stderr: (result.stderr || '').trim(),
    status: result.status
  };
}

// 1. 读取 STATE.json
async function validateState() {
  const statePath = join(ROOT, 'docs', 'execution', 'STATE.json');
  if (!await pathExists(statePath)) {
    fail('STATE.json 不存在');
    return;
  }
  const content = await readText(statePath);
  let state;
  try {
    state = JSON.parse(content);
  } catch (e) {
    fail('STATE.json 不是合法 JSON：' + e.message);
    return;
  }

  if (!state.schema_version || state.schema_version !== 1) {
    fail('STATE.json schema_version 必须是 1');
  }
  if (!state.goal_id) fail('STATE.json 缺少 goal_id');
  if (!state.goal_status) fail('STATE.json 缺少 goal_status');
  if (!state.active_stage) fail('STATE.json 缺少 active_stage');
  if (!Array.isArray(state.work_packages)) fail('STATE.json work_packages 必须是数组');
  if (!Array.isArray(state.gates)) fail('STATE.json gates 必须是数组');

  const ids = new Set();
  const inProgress = [];
  for (const wp of state.work_packages) {
    if (!wp.id || !wp.id.match(/^WP-\d{3}$/)) {
      fail('工作包 ID 不合法：' + (wp.id || 'undefined'));
      continue;
    }
    if (ids.has(wp.id)) {
      fail('工作包 ID 重复：' + wp.id);
    }
    ids.add(wp.id);

    const validStatuses = ['draft', 'ready', 'in_progress', 'review', 'done', 'blocked'];
    if (!validStatuses.includes(wp.status)) {
      fail('工作包 ' + wp.id + ' 状态不合法：' + wp.status);
    }

    if (wp.status === 'in_progress') inProgress.push(wp.id);

    if (!Array.isArray(wp.depends_on)) {
      fail('工作包 ' + wp.id + ' depends_on 必须是数组');
    }

    // spec 文件存在性
    if (wp.spec && !(await pathExists(join(ROOT, wp.spec)))) {
      fail('工作包 ' + wp.id + ' spec 文件不存在：' + wp.spec);
    }

    // done 工作包必须有 evidence
    if (wp.status === 'done') {
      if (!wp.evidence) {
        fail('工作包 ' + wp.id + ' 为 done，但缺少 evidence');
      } else if (!(await pathExists(join(ROOT, wp.evidence)))) {
        fail('工作包 ' + wp.id + ' evidence 文件不存在：' + wp.evidence);
      }
      if (!wp.completed_at) {
        fail('工作包 ' + wp.id + ' 为 done，但缺少 completed_at');
      }
    }

    // 依赖存在性
    for (const dep of wp.depends_on) {
      if (!state.work_packages.some(w => w.id === dep)) {
        fail('工作包 ' + wp.id + ' 依赖不存在的工作包：' + dep);
      }
    }
  }

  if (inProgress.length === 0) {
    warn('没有处于 in_progress 的工作包');
  } else if (inProgress.length > 1) {
    fail('同时存在多个 in_progress 工作包：' + inProgress.join(', '));
  }

  // 依赖关系
  const done = new Set(state.work_packages.filter(w => w.status === 'done').map(w => w.id));
  for (const wp of state.work_packages) {
    if (['in_progress', 'done', 'review', 'ready'].includes(wp.status)) {
      for (const dep of wp.depends_on) {
        if (!done.has(dep)) {
          fail('工作包 ' + wp.id + ' 依赖 ' + dep + ' 尚未完成');
        }
      }
    }
  }

  // 循环依赖
  const graph = new Map(state.work_packages.map(w => [w.id, w.depends_on]));
  for (const wpId of graph.keys()) {
    const visited = new Set();
    const stack = [wpId];
    while (stack.length) {
      const current = stack.pop();
      if (visited.has(current)) continue;
      visited.add(current);
      for (const dep of graph.get(current) || []) {
        if (dep === wpId) {
          fail('检测到循环依赖：' + wpId + ' 依赖自身');
        }
        if (graph.has(dep)) stack.push(dep);
      }
    }
  }

  // gate 一致性
  for (const gate of state.gates) {
    if (!gate.id || !gate.id.match(/^G\d$/)) {
      fail('门禁 ID 不合法：' + (gate.id || 'undefined'));
    }
    if (!['automatic', 'human', 'hybrid'].includes(gate.type)) {
      fail('门禁 ' + gate.id + ' 类型不合法：' + gate.type);
    }
    if (!['pending', 'in_review', 'automatic_passed', 'waiting_human', 'approved', 'rejected'].includes(gate.status)) {
      fail('门禁 ' + gate.id + ' 状态不合法：' + gate.status);
    }
  }

  ok('STATE.json 基本结构和状态机检查通过');
}// 2. 文档和链接检查
async function validateDocs() {
  const files = [
    'README.md',
    'AGENTS.md',
    'docs/product/GOAL.md',
    'docs/execution/EXECUTION-PROTOCOL.md',
    'docs/execution/STAGE-GATES.md',
    'docs/execution/WORK-PACKAGE-TEMPLATE.md',
    'docs/execution/ROADMAP.md',
    'docs/execution/EVIDENCE-FORMAT.md'
  ];

  for (const f of files) {
    const p = join(ROOT, f);
    if (!(await pathExists(p))) {
      fail('必备文档缺失：' + f);
    }
  }

  const readme = await readText(join(ROOT, 'README.md')) || '';
  const agents = await readText(join(ROOT, 'AGENTS.md')) || '';
  const goal = await readText(join(ROOT, 'docs/product/GOAL.md')) || '';

  // 检查 README 中的旧启动命令
  const oldStartPatterns = [
    /npm\s+start/i,
    /npm\s+run\s+dev/i,
    /yarn\s+start/i,
    /yarn\s+dev/i
  ];
  for (const pattern of oldStartPatterns) {
    if (pattern.test(readme)) {
      warn('README.md 包含旧启动命令引用，需确认已声明不可用');
    }
  }

  // 检查 Vant 声明
  for (const doc of [readme, agents, goal]) {
    if (/Vant/.test(doc) && !/旧|legacy|曾经|旧版|已废弃|历史/.test(doc)) {
      fail('active 文档中将 Vant 声明为当前技术栈');
    }
  }

  // 检查 old/ 之外是否声明新系统依赖 old/
  const docs = { README: readme, AGENTS: agents, GOAL: goal };
  for (const [name, content] of Object.entries(docs)) {
    if (/新系统.*依赖.*old|old.*是新系统.*真源|直接复制.*old/.test(content)) {
      fail(name + '.md 中存在禁止的 old/ 依赖声明');
    }
  }

  ok('文档一致性和关键引用检查通过');
}

// 3. 凭据扫描
async function scanCredentials() {
  const result = exec('git', ['ls-files']);
  if (result.status !== 0) {
    fail('无法获取 Git 已跟踪文件：' + result.stderr);
    return;
  }
  const files = result.stdout.split('\n').filter(Boolean);
  const patterns = [
    { name: '硬编码密码', regex: /password\s*=\s*['"][^'"]+['"]/i },
    { name: '硬编码密钥', regex: /secret(key)?\s*=\s*['"][^'"]+['"]/i },
    { name: '硬编码 Token', regex: /token\s*=\s*['"][^'"]+['"]/i },
    { name: '数据库 URL', regex: /jdbc:(mysql|postgresql|oracle|sqlserver):\/\/[^\s]+/i },
    { name: 'Redis 地址', regex: /redis:\/\/[^\s]+/i },
    { name: '默认账号密码', regex: /admin\s*[:=]\s*['"][^'"]+['"]/i }
  ];

  let scanned = 0;
  for (const file of files) {
    const p = join(ROOT, file);
    let content;
    try {
      // 只扫描文本文件
      const s = await stat(p);
      if (s.size > 1024 * 1024) continue;
      content = await readText(p);
      if (content === null || content.includes('\u0000')) continue;
    } catch {
      continue;
    }
    scanned++;
    for (const { name, regex } of patterns) {
      for (const line of content.split('\n')) {
        if (regex.test(line)) {
          // 允许环境变量占位符和明显无效示例
          if (line.includes('${') || line.includes('YOUR_') || line.includes('EXAMPLE_') || line.includes('***')) continue;
          // 允许在 old/ 和 docs/legacy/ 中存在（作为遗留参考），但需记录
          if (file.startsWith('old/') || file.startsWith('docs/legacy/')) {
            warnings.push('遗留文件 ' + file + ' 包含 ' + name + ' 引用，需确认已在审计中登记');
            console.warn(yellow('! 遗留文件 ' + file + ' 包含 ' + name + ' 引用，需确认已在审计中登记'));
          } else {
            fail('发现 ' + name + '：' + file);
          }
        }
      }
    }
  }

  ok('凭据扫描完成，已扫描 ' + scanned + ' 个文本文件');
}

// 4. .gitignore 检查
async function validateGitignore() {
  const gitignorePath = join(ROOT, '.gitignore');
  if (!(await pathExists(gitignorePath))) {
    fail('.gitignore 不存在');
    return;
  }
  const content = await readText(gitignorePath);
  const lines = content.split('\n');
  let pnpmLockIgnored = false;
  for (const line of lines) {
    if (line.trim() === 'pnpm-lock.yaml') {
      pnpmLockIgnored = true;
    }
  }
  if (pnpmLockIgnored) {
    fail('.gitignore 错误地忽略了 pnpm-lock.yaml');
  } else {
    ok('.gitignore 未忽略 pnpm-lock.yaml');
  }

  // 检查是否忽略了构建产物
  const required = ['node_modules/', 'target/', 'dist/', '.idea/', '.vscode/'];
  const ignored = new Set(lines.map(l => l.trim()));
  for (const item of required) {
    const trimmedItem = item.replace(/\/$/, '');
    const hasIgnore = ignored.has(item) || ignored.has(trimmedItem);
    if (!hasIgnore) {
      warn('.gitignore 未忽略 ' + item);
    }
  }
}

// 5. 提交追踪
async function validateCommitTrace() {
  const result = exec('git', ['log', '--oneline', '--all']);
  if (result.status !== 0) {
    fail('无法获取 Git 日志：' + result.stderr);
    return;
  }
  const lines = result.stdout.split('\n').filter(Boolean);
  const forbidden = ['1', 'add', 'update', 'fix', 'done'];

  // 找到 WP-000 基线提交，只检查基线之后的提交
  let baselineIndex = lines.findIndex(line => line.includes('(WP-000)'));
  if (baselineIndex === -1) {
    warn('未找到 WP-000 基线提交，禁用词检查将应用于所有提交');
    baselineIndex = lines.length;
  }

  for (const line of lines.slice(0, baselineIndex)) {
    const msg = line.replace(/^\S+\s+/, '');
    if (forbidden.includes(msg)) {
      fail('提交信息包含禁用词：' + line);
    }
  }

  // 检查 done 工作包是否可通过提交信息追踪
  const state = JSON.parse(await readText(join(ROOT, 'docs', 'execution', 'STATE.json')) || '{}');
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
  await validateState();
  await validateDocs();
  await scanCredentials();
  await validateGitignore();
  await validateCommitTrace();

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
