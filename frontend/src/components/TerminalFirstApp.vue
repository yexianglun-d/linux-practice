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

const session = ref<LabSessionResponse | null>(null)
const terminalLines = ref<TerminalLine[]>([])
const evidenceLines = ref<string[]>([])
const input = ref('')
const ready = ref(false)
const booting = ref(true)
const paletteOpen = ref(false)
const currentMode = ref<LearningPathCode>('BEGINNER')
const commandHistory = ref<string[]>([])
const historyIndex = ref(-1)
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
const currentTask = computed(() => {
  const tasks = session.value?.tasks ?? []
  return tasks.find((task) => !task.passed) ?? tasks[0] ?? null
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
    append('error', error instanceof Error ? error.message : 'Boot failed')
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
  append('muted', `Path: ${mode.toLowerCase()} · preparing code-defined Linux lab`)
  const nextSession = await startDefaultLabSession(mode)
  session.value = nextSession
  append('system', `Allocated lab: ${nextSession.labTitle}`)
  connectTerminal(nextSession.id)
}

function connectTerminal(sessionId: number) {
  const protocol = window.location.protocol === 'https:' ? 'wss' : 'ws'
  socket = new WebSocket(`${protocol}://${window.location.host}/ws/lab-sessions/${sessionId}/terminal`)

  socket.onopen = () => {
    booting.value = false
    ready.value = true
    append('success', 'VM ready.')
    append('muted', '输入 help 查看可用学习指令。直接输入 Linux 命令即可开始。')
    announceCurrentTask()
    focusInput()
  }

  socket.onmessage = (event) => {
    append('output', event.data, true)
  }

  socket.onclose = () => {
    ready.value = false
    if (session.value?.status === 'RUNNING') {
      append('warning', 'WebSocket disconnected. Use reset to recreate the terminal session.')
    }
  }

  socket.onerror = () => {
    append('error', 'Terminal connection error.')
  }
}

async function submitCommand() {
  const command = input.value.trim()
  if (!command || !ready.value) {
    return
  }
  input.value = ''
  commandHistory.value.push(command)
  historyIndex.value = commandHistory.value.length
  append('prompt', `student@linux-lab:~$ ${command}`, true)

  if (await handleLearningCommand(command)) {
    return
  }

  if (socket?.readyState === WebSocket.OPEN) {
    socket.send(command)
    return
  }
  append('error', 'Terminal is not connected. Try reset.')
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
    'Learning commands:',
    '  help             show commands',
    '  learn            list learning directions',
    '  hint             get a contextual hint',
    '  check            check the current goal',
    '  next             show the next suggested step',
    '  path beginner    switch to Linux basics',
    '  path ops         switch to ops practice',
    '',
    'Linux commands work normally: pwd, whoami, ls, touch, systemctl status nginx',
  ].join('\n'))
}

function printLearningPaths() {
  append('system', [
    'Available learning directions:',
    '  Linux 基础      pwd / whoami / 文件 / 权限',
    '  服务运维        systemctl / logs / nginx',
    '  Shell           variables / pipes / scripts',
    '  Docker          images / containers / deployment',
    '  K8s             kubectl / pods / workload status',
    '  故障排查        observe / isolate / verify',
    '',
    'Use path beginner or path ops to change the active path.',
  ].join('\n'))
}

function printHint() {
  const task = currentTask.value
  if (!task) {
    append('warning', 'No active task. Try learn or next.')
    return
  }
  append('system', `Hint for "${task.title}":\n${task.hint}`)
}

function printNextStep() {
  const task = currentTask.value
  if (!task) {
    append('success', 'You have completed the available tasks in this lab.')
    return
  }
  append('system', `Next suggested step:\n${task.instruction}\nRun check when you think it is done.`)
}

async function runCheck() {
  if (!session.value || !currentTask.value) {
    append('warning', 'No active session or task.')
    return
  }
  const task = currentTask.value
  append('muted', `Running checker: ${task.checkerType}`)
  try {
    const result = await checkTask(session.value.id, task.taskId, evidenceLines.value.join('\n'))
    session.value = await getLabSession(session.value.id)
    append(result.passed ? 'success' : 'warning', result.message)
    append('muted', `Progress: ${result.progressPercent}%`)
    if (result.passed) {
      printNextStep()
    }
  } catch (error) {
    append('error', error instanceof Error ? error.message : 'Check failed.')
  }
}

async function switchPath(mode: LearningPathCode) {
  append('system', `Switching path to ${mode.toLowerCase()}...`)
  await startLab(mode)
}

async function resetCurrentSession() {
  if (!session.value) {
    await bootDefaultSession()
    return
  }
  append('system', 'Resetting VM from snapshot...')
  ready.value = false
  socket?.close()
  const resetSession = await resetLabSession(session.value.id)
  session.value = resetSession
  evidenceLines.value = []
  connectTerminal(resetSession.id)
}

function announceCurrentTask() {
  const task = currentTask.value
  if (!task) {
    return
  }
  append('system', `Current goal: ${task.title}\n${task.instruction}\nUse hint, check, next, or type a Linux command.`)
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
          <span>{{ session?.labTitle ?? 'Preparing sandbox' }}</span>
        </div>
      </div>
      <div class="terminal-status" aria-label="当前学习状态">
        <span class="status-pill">
          <Activity :size="15" aria-hidden="true" />
          {{ statusText }}
        </span>
        <span class="status-pill">MODE {{ currentMode.toLowerCase() }}</span>
        <span class="status-pill">PROGRESS {{ progressPercent }}%</span>
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
        placeholder="type help, learn, hint, check, next, path ops, or any Linux command"
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
