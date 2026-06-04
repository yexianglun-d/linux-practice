<script setup lang="ts">
import { computed, nextTick, onBeforeUnmount, onMounted, ref } from 'vue'
import { Activity, CircleHelp, Command, RotateCcw, TerminalSquare } from '@lucide/vue'
import {
  checkTask,
  getLabSession,
  resetLabSession,
  startDefaultLabSession,
} from '../api'
import type { LabSessionResponse, LearningPathCode } from '../types'

type LineKind = 'system' | 'prompt' | 'output' | 'success' | 'warning' | 'error' | 'muted'

interface TerminalLine {
  id: number
  kind: LineKind
  text: string
}

const MAX_COMMAND_SUMMARY_LENGTH = 1000
const MAX_EVIDENCE_LINES = 60

const session = ref<LabSessionResponse | null>(null)
const terminalLines = ref<TerminalLine[]>([])
const evidenceLines = ref<string[]>([])
const input = ref('')
const ready = ref(false)
const booting = ref(true)
const paletteOpen = ref(false)
const currentMode = ref<LearningPathCode>('BEGINNER')
const commandHistory = ref<string[]>([])
const commandCount = ref(0)
const historyIndex = ref(-1)
const promptPath = ref('~')
const terminalBody = ref<HTMLElement | null>(null)
const commandInput = ref<HTMLInputElement | null>(null)

let lineId = 0
let socket: WebSocket | null = null

const statusText = computed(() => {
  if (booting.value) {
    return 'BOOTING'
  }
  if (!session.value) {
    return 'OFFLINE'
  }
  return session.value.status
})

const progressPercent = computed(() => session.value?.progressPercent ?? 0)
const totalTaskCount = computed(() => session.value?.tasks.length ?? 0)
const passedTaskCount = computed(() => session.value?.tasks.filter((task) => task.passed).length ?? 0)
const currentTask = computed(() => {
  const tasks = session.value?.tasks ?? []
  return tasks.find((task) => !task.passed) ?? null
})
const suggestedCommand = computed(() => {
  const instruction = currentTask.value?.instruction ?? ''
  return instruction.match(/`([^`]+)`/)?.[1] ?? 'help'
})

onMounted(() => {
  window.addEventListener('keydown', handleGlobalKeydown)
  window.addEventListener('resize', handleViewportResize)
  bootDefaultSession()
})

onBeforeUnmount(() => {
  socket?.close()
  window.removeEventListener('keydown', handleGlobalKeydown)
  window.removeEventListener('resize', handleViewportResize)
})

async function bootDefaultSession() {
  booting.value = true
  ready.value = false
  clearTerminal()
  append('system', 'Booting Linux Lab...')
  try {
    await startLab('BEGINNER')
  } catch (error) {
    append('error', error instanceof Error ? error.message : '启动失败，请稍后重试。')
    booting.value = false
  }
}

async function startLab(mode: LearningPathCode) {
  socket?.close()
  ready.value = false
  booting.value = true
  currentMode.value = mode
  session.value = null
  evidenceLines.value = []
  commandCount.value = 0
  promptPath.value = '~'
  append('muted', `学习路径: ${mode.toLowerCase()} · 正在加载代码内置实验`)
  const nextSession = await startDefaultLabSession(mode)
  session.value = nextSession
  append('system', `已进入实验: ${nextSession.labTitle}`)
  connectTerminal(nextSession.id)
}

function connectTerminal(sessionId: number) {
  const protocol = window.location.protocol === 'https:' ? 'wss' : 'ws'
  socket = new WebSocket(`${protocol}://${window.location.host}/ws/lab-sessions/${sessionId}/terminal`)

  socket.onopen = () => {
    booting.value = false
    ready.value = true
    append('success', 'VM ready.')
    append('muted', '先输入 help 看学习指令；也可以直接输入 Linux 命令自由探索。')
    announceCurrentTask()
    focusInput()
  }

  socket.onmessage = (event) => {
    updatePromptPath(event.data)
    append('output', event.data, true)
  }

  socket.onclose = () => {
    ready.value = false
    if (session.value?.status === 'RUNNING') {
      append('warning', '终端连接已断开。可以点击 reset 重新创建会话。')
    }
  }

  socket.onerror = () => {
    append('error', '终端连接异常。')
  }
}

async function submitCommand() {
  const command = input.value.trim()
  if (!command || !ready.value) {
    return
  }
  input.value = ''
  commandHistory.value.push(command)
  commandCount.value += 1
  historyIndex.value = commandHistory.value.length
  append('prompt', `student@linux-lab:${promptPath.value}$ ${command}`, true)

  if (await handleLearningCommand(command)) {
    return
  }

  if (socket?.readyState === WebSocket.OPEN) {
    socket.send(command)
    return
  }
  append('error', '终端尚未连接。可以点击 reset 重新创建会话。')
}

async function handleLearningCommand(command: string) {
  const normalized = command.toLowerCase()
  if (normalized === '?' || normalized === 'help') {
    printHelp()
    return true
  }
  if (normalized === 'learn') {
    printLearningPaths()
    return true
  }
  if (normalized === 'hint') {
    printHint()
    return true
  }
  if (normalized === 'check') {
    await runCheck()
    return true
  }
  if (normalized === 'next') {
    printNextStep()
    return true
  }
  if (normalized === 'path beginner') {
    await switchPath('BEGINNER')
    return true
  }
  if (normalized === 'path ops') {
    await switchPath('OPS')
    return true
  }
  return false
}

function printHelp() {
  append('system', [
    '学习指令:',
    '  help             查看这份说明',
    '  learn            查看可选学习方向',
    '  hint             查看当前任务提示',
    '  check            检查当前目标是否完成',
    '  next             查看下一步建议',
    '  path beginner    切到 Linux 基础路径',
    '  path ops         切到服务运维路径',
    '',
    'Linux 命令可以直接敲: pwd, whoami, ls, ll, cd /, touch /tmp/linux-foundation/permission-ok',
    '命令写错时会返回 command not found，并给出修正建议；做完当前目标后输入 check。',
  ].join('\n'))
}

function printLearningPaths() {
  append('system', [
    '可选学习方向:',
    '  Linux 基础      pwd / whoami / 文件 / 权限',
    '  服务运维        systemctl / logs / nginx',
    '  Shell           variables / pipes / scripts',
    '  Docker          images / containers / deployment',
    '  K8s             kubectl / pods / workload status',
    '  故障排查        observe / isolate / verify',
    '',
    '输入 path beginner 或 path ops 切换路径。',
  ].join('\n'))
}

function printHint() {
  const task = currentTask.value
  if (!task) {
    append('warning', '当前没有进行中的目标。输入 learn 查看可选方向。')
    return
  }
  append('system', `任务提示: ${task.title}\n${task.hint}`)
}

function printNextStep() {
  const task = currentTask.value
  if (!task) {
    append('success', '当前实验目标已完成。可以继续自由探索，或输入 path ops 切到运维路径。')
    return
  }
  append('system', `下一步建议:\n${task.instruction}\n建议先敲: ${suggestedCommand.value}\n完成后输入 check 检查结果，卡住就输入 hint。`)
}

async function runCheck() {
  if (!session.value || !currentTask.value) {
    append('warning', '当前没有可检查的会话或目标。')
    return
  }
  const task = currentTask.value
  append('muted', `正在检查: ${task.checkerType}`)
  try {
    const result = await checkTask(session.value.id, task.taskId, buildCommandSummary())
    session.value = await getLabSession(session.value.id)
    append(result.passed ? 'success' : 'warning', result.message)
    append('muted', `学习进度: ${result.progressPercent}% · 已通过 ${passedTaskCount.value}/${totalTaskCount.value}`)
    if (result.passed) {
      printNextStep()
    }
  } catch (error) {
    append('error', error instanceof Error ? error.message : '检查失败，请稍后重试。')
  }
}

async function switchPath(mode: LearningPathCode) {
  append('system', `正在切换到 ${mode.toLowerCase()} 路径...`)
  await startLab(mode)
}

async function resetCurrentSession() {
  if (!session.value) {
    await bootDefaultSession()
    return
  }
  append('system', '正在从快照重置 VM...')
  ready.value = false
  socket?.close()
  const resetSession = await resetLabSession(session.value.id)
  session.value = resetSession
  evidenceLines.value = []
  commandCount.value = 0
  promptPath.value = '~'
  connectTerminal(resetSession.id)
}

function announceCurrentTask() {
  const task = currentTask.value
  if (!task) {
    return
  }
  append('system', [
    `当前练习: ${task.title}`,
    `目标: ${task.instruction}`,
    `建议先敲: ${suggestedCommand.value}`,
    '做完输入 check；卡住输入 hint；想自己探索就直接敲 Linux 命令。',
  ].join('\n'))
}

function updatePromptPath(output: string) {
  const changedDirectory = output.match(/已切换到 ([^。\n]+)。/)?.[1]
  if (!changedDirectory) {
    return
  }
  promptPath.value = changedDirectory === '/home/student' ? '~' : changedDirectory
}

function buildCommandSummary() {
  const summary = evidenceLines.value.slice(-MAX_EVIDENCE_LINES).join('\n')
  if (summary.length <= MAX_COMMAND_SUMMARY_LENGTH) {
    return summary
  }
  return summary.slice(summary.length - MAX_COMMAND_SUMMARY_LENGTH)
}

function append(kind: LineKind, text: string, includeEvidence = false) {
  terminalLines.value.push({ id: ++lineId, kind, text })
  if (includeEvidence) {
    evidenceLines.value.push(text)
  }
  scrollToBottom()
}

function clearTerminal() {
  terminalLines.value = []
  evidenceLines.value = []
}

function handleGlobalKeydown(event: KeyboardEvent) {
  if ((event.ctrlKey || event.metaKey) && event.key.toLowerCase() === 'k') {
    event.preventDefault()
    paletteOpen.value = !paletteOpen.value
  }
}

function handleViewportResize() {
  void scrollToBottom()
}

function handleInputKeydown(event: KeyboardEvent) {
  if (event.key === 'ArrowUp') {
    event.preventDefault()
    if (commandHistory.value.length === 0) {
      return
    }
    historyIndex.value = Math.max(0, historyIndex.value - 1)
    input.value = commandHistory.value[historyIndex.value] ?? ''
  }
  if (event.key === 'ArrowDown') {
    event.preventDefault()
    historyIndex.value = Math.min(commandHistory.value.length, historyIndex.value + 1)
    input.value = commandHistory.value[historyIndex.value] ?? ''
  }
}

function runPaletteCommand(command: string) {
  paletteOpen.value = false
  input.value = command
  submitCommand()
}

async function scrollToBottom() {
  await nextTick()
  await waitForLayoutFrame()
  if (terminalBody.value) {
    terminalBody.value.scrollTop = terminalBody.value.scrollHeight
  }
}

function waitForLayoutFrame() {
  return new Promise<void>((resolve) => {
    window.requestAnimationFrame(() => resolve())
  })
}

async function focusInput() {
  await nextTick()
  commandInput.value?.focus()
}
</script>

<template>
  <section class="terminal-first" aria-label="Linux 实战学堂终端学习界面">
    <header class="terminal-topbar">
      <div class="terminal-brand">
        <TerminalSquare :size="20" aria-hidden="true" />
        <div>
          <h1>Linux 实战学堂</h1>
          <span>{{ session?.labTitle ?? '正在准备沙箱' }}</span>
        </div>
      </div>
      <div class="terminal-status" aria-label="当前学习状态">
        <span class="status-pill">
          <Activity :size="15" aria-hidden="true" />
          {{ statusText }}
        </span>
        <span class="status-pill">MODE {{ currentMode.toLowerCase() }}</span>
        <span class="status-pill">PASS {{ passedTaskCount }}/{{ totalTaskCount }}</span>
        <span class="status-pill">CMD {{ commandCount }}</span>
        <span class="status-pill">CWD {{ promptPath }}</span>
        <button class="terminal-tool" type="button" aria-label="重置 VM" @click="resetCurrentSession">
          <RotateCcw :size="16" aria-hidden="true" />
          reset
        </button>
        <button class="terminal-tool" type="button" aria-label="打开命令面板" @click="paletteOpen = true">
          <Command :size="16" aria-hidden="true" />
          Ctrl K
        </button>
        <button class="terminal-tool" type="button" aria-label="查看帮助" @click="runPaletteCommand('help')">
          <CircleHelp :size="16" aria-hidden="true" />
          help
        </button>
      </div>
    </header>

    <div class="practice-focus" aria-label="当前练习焦点">
      <div class="focus-copy">
        <span class="focus-kicker">CURRENT DRILL · PROGRESS {{ progressPercent }}%</span>
        <strong>{{ currentTask?.title ?? '自由探索' }}</strong>
        <p>{{ currentTask?.instruction ?? '直接输入 Linux 命令开始探索。需要方向时输入 learn。' }}</p>
      </div>
      <div class="focus-command" aria-label="建议命令">{{ suggestedCommand }}</div>
    </div>

    <div ref="terminalBody" class="terminal-screen" role="log" aria-live="polite" aria-label="终端输出">
      <pre
        v-for="line in terminalLines"
        :key="line.id"
        class="terminal-line"
        :class="`line-${line.kind}`"
      >{{ line.text }}</pre>
      <span class="terminal-cursor" aria-hidden="true" />
    </div>

    <form class="terminal-compose" @submit.prevent="submitCommand">
      <label class="sr-only" for="terminal-command">Linux 命令输入</label>
      <span class="prompt-sign" aria-hidden="true">$</span>
      <input
        id="terminal-command"
        ref="commandInput"
        v-model="input"
        :disabled="!ready"
        autocomplete="off"
        autocapitalize="off"
        spellcheck="false"
        inputmode="text"
        placeholder="输入 help、hint、check、next，或直接输入 Linux 命令"
        @keydown="handleInputKeydown"
      />
      <button type="submit" :disabled="!ready" aria-label="执行命令">run</button>
    </form>

    <div v-if="paletteOpen" class="command-palette" role="dialog" aria-modal="true" aria-label="命令面板">
      <div class="palette-panel">
        <div class="palette-title">Command Palette</div>
        <button type="button" @click="runPaletteCommand('help')">help · 查看学习指令</button>
        <button type="button" @click="runPaletteCommand('learn')">learn · 列出学习方向</button>
        <button type="button" @click="runPaletteCommand('hint')">hint · 获取当前提示</button>
        <button type="button" @click="runPaletteCommand('check')">check · 检查当前目标</button>
        <button type="button" @click="runPaletteCommand('next')">next · 推荐下一步</button>
        <button type="button" @click="runPaletteCommand('path beginner')">path beginner · Linux 基础</button>
        <button type="button" @click="runPaletteCommand('path ops')">path ops · 运维实战</button>
        <button type="button" class="palette-close" @click="paletteOpen = false">close</button>
      </div>
    </div>
  </section>
</template>
