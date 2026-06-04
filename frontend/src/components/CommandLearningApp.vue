<script setup lang="ts">
import { computed, nextTick, onBeforeUnmount, onMounted, ref } from 'vue'
import {
  Activity,
  CheckCircle2,
  Keyboard,
  RotateCcw,
  TerminalSquare,
  XCircle,
  Zap,
} from '@lucide/vue'
import {
  getDefaultCommandExercises,
  resetLabSession,
  startDefaultLabSession,
  submitCommandAttempt,
} from '../api'
import type {
  CommandAttemptResponse,
  CommandExercise,
  CommandProgress,
  LabSessionResponse,
} from '../types'

type FeedbackKind = 'idle' | 'success' | 'warning' | 'error'
type LabLineKind = 'system' | 'prompt' | 'output' | 'error' | 'success'

interface FeedbackState {
  kind: FeedbackKind
  title: string
  message: string
}

interface LabLine {
  id: number
  kind: LabLineKind
  text: string
}

const exercises = ref<CommandExercise[]>([])
const chapterTitle = ref('Linux 基础命令')
const progress = ref<CommandProgress | null>(null)
const activeIndex = ref(0)
const commandInput = ref('')
const loading = ref(true)
const submitting = ref(false)
const completed = ref(false)
const showHint = ref(false)
const feedback = ref<FeedbackState>({ kind: 'idle', title: '等待输入', message: '照着当前命令目标手敲一遍。' })
const exerciseStartedAt = ref(Date.now())
const inputRef = ref<HTMLInputElement | null>(null)

const labOpen = ref(false)
const labStarting = ref(false)
const labReady = ref(false)
const labSession = ref<LabSessionResponse | null>(null)
const labInput = ref('')
const labLines = ref<LabLine[]>([])
const labBody = ref<HTMLElement | null>(null)

let advanceTimer: number | undefined
let labLineId = 0
let labSocket: WebSocket | null = null

const currentExercise = computed(() => exercises.value[activeIndex.value] ?? null)
const completedIds = computed(() => new Set(progress.value?.completedExerciseIds ?? []))
const progressPercent = computed(() => {
  if (!progress.value || progress.value.totalExercises === 0) {
    return 0
  }
  return Math.round((progress.value.completedExercises * 100) / progress.value.totalExercises)
})
const commandTokens = computed(() => currentExercise.value?.expectedCommand.split(' ') ?? [])

onMounted(() => {
  loadExercises()
})

onBeforeUnmount(() => {
  window.clearTimeout(advanceTimer)
  labSocket?.close()
})

async function loadExercises() {
  loading.value = true
  try {
    const catalog = await getDefaultCommandExercises()
    exercises.value = catalog.exercises
    chapterTitle.value = catalog.chapterTitle
    progress.value = catalog.progress
    selectNextExercise(catalog.progress.completedExerciseIds)
  } catch (error) {
    feedback.value = {
      kind: 'error',
      title: '加载失败',
      message: error instanceof Error ? error.message : '命令练习加载失败。',
    }
  } finally {
    loading.value = false
    focusInput()
  }
}

async function submitExercise() {
  const exercise = currentExercise.value
  const input = commandInput.value.trim()
  if (!exercise || !input || submitting.value || completed.value) {
    return
  }
  submitting.value = true
  window.clearTimeout(advanceTimer)
  try {
    const result = await submitCommandAttempt(exercise.id, input, Date.now() - exerciseStartedAt.value)
    progress.value = result.progress
    feedback.value = feedbackFromResult(result)
    if (result.correct) {
      advanceTimer = window.setTimeout(() => {
        commandInput.value = ''
        showHint.value = false
        selectNextExercise(result.progress.completedExerciseIds)
      }, 720)
    }
  } catch (error) {
    feedback.value = {
      kind: 'error',
      title: '提交失败',
      message: error instanceof Error ? error.message : '提交练习失败。',
    }
  } finally {
    submitting.value = false
  }
}

function feedbackFromResult(result: CommandAttemptResponse): FeedbackState {
  if (result.correct) {
    return {
      kind: 'success',
      title: '输入正确',
      message: `${result.message} ${result.explanation}`,
    }
  }
  return {
    kind: result.errorType === 'TYPO' || result.errorType === 'MISSING_ARGUMENT' ? 'warning' : 'error',
    title: errorTitle(result.errorType),
    message: result.message,
  }
}

function errorTitle(errorType: string) {
  const titles: Record<string, string> = {
    TYPO: '拼写需要修正',
    MISSING_ARGUMENT: '参数不完整',
    WRONG_COMMAND: '命令不匹配',
    WRONG_OPTION: '参数不符合目标',
    NOT_TARGET: '当前目标不是这个',
    EMPTY: '还没有输入命令',
  }
  return titles[errorType] ?? '继续调整'
}

function selectNextExercise(passedIds: string[]) {
  const nextIndex = exercises.value.findIndex((exercise) => !passedIds.includes(exercise.id))
  if (nextIndex < 0 && exercises.value.length > 0) {
    completed.value = true
    activeIndex.value = exercises.value.length - 1
    feedback.value = {
      kind: 'success',
      title: '本章完成',
      message: '你已经完成本章命令肌肉记忆练习，可以进入实战终端验证。',
    }
    return
  }
  completed.value = false
  activeIndex.value = Math.max(0, nextIndex)
  exerciseStartedAt.value = Date.now()
  feedback.value = { kind: 'idle', title: '等待输入', message: '照着当前命令目标手敲一遍。' }
  focusInput()
}

async function openPracticeLab() {
  if (!completed.value) {
    return
  }
  labOpen.value = true
  if (labSession.value) {
    focusLab()
    return
  }
  labStarting.value = true
  labReady.value = false
  appendLab('system', '正在创建实战终端环境...')
  try {
    const session = await startDefaultLabSession('BEGINNER')
    labSession.value = session
    appendLab('success', `已进入实战实验: ${session.labTitle}`)
    connectLabTerminal(session.id)
  } catch (error) {
    appendLab('error', error instanceof Error ? error.message : '实战终端启动失败。')
  } finally {
    labStarting.value = false
  }
}

function connectLabTerminal(sessionId: number) {
  const protocol = window.location.protocol === 'https:' ? 'wss' : 'ws'
  labSocket?.close()
  labSocket = new WebSocket(`${protocol}://${window.location.host}/ws/lab-sessions/${sessionId}/terminal`)
  labSocket.onopen = () => {
    labReady.value = true
    appendLab('success', 'VM ready. 这里用于自由实战验证。')
    focusLab()
  }
  labSocket.onmessage = (event) => appendLab('output', event.data)
  labSocket.onerror = () => appendLab('error', '终端连接异常。')
  labSocket.onclose = () => {
    labReady.value = false
  }
}

function submitLabCommand() {
  const command = labInput.value.trim()
  if (!command || labSocket?.readyState !== WebSocket.OPEN) {
    return
  }
  labInput.value = ''
  appendLab('prompt', `student@linux-lab:~$ ${command}`)
  labSocket.send(command)
}

async function resetLab() {
  if (!labSession.value) {
    await openPracticeLab()
    return
  }
  appendLab('system', '正在重置实战终端...')
  labReady.value = false
  labSocket?.close()
  const resetSession = await resetLabSession(labSession.value.id)
  labSession.value = resetSession
  connectLabTerminal(resetSession.id)
}

function appendLab(kind: LabLineKind, text: string) {
  labLines.value.push({ id: ++labLineId, kind, text })
  scrollLab()
}

async function scrollLab() {
  await nextTick()
  if (labBody.value) {
    labBody.value.scrollTop = labBody.value.scrollHeight
  }
}

async function focusInput() {
  await nextTick()
  inputRef.value?.focus()
}

async function focusLab() {
  await nextTick()
  document.getElementById('lab-command')?.focus()
}
</script>

<template>
  <section class="command-app" aria-label="Linux 命令学习台">
    <header class="command-topbar">
      <div class="command-brand">
        <Keyboard :size="22" aria-hidden="true" />
        <div>
          <h1>Linux 实战学堂</h1>
          <span>{{ chapterTitle }}</span>
        </div>
      </div>
      <div class="command-stats" aria-label="练习状态">
        <span class="metric-pill">
          <Activity :size="15" aria-hidden="true" />
          {{ loading ? 'LOADING' : 'DRILL' }}
        </span>
        <span class="metric-pill">进度 {{ progressPercent }}%</span>
        <span class="metric-pill">正确率 {{ progress?.accuracyPercent ?? 0 }}%</span>
        <span class="metric-pill">连续 {{ progress?.currentStreak ?? 0 }}</span>
        <span class="metric-pill">尝试 {{ progress?.attempts ?? 0 }}</span>
      </div>
    </header>

    <main v-if="!labOpen" class="command-stage">
      <section class="drill-board" aria-label="当前命令练习">
        <div class="chapter-strip">
          <span>COMMAND DRILL</span>
          <strong>{{ currentExercise?.difficulty ?? '入门' }}</strong>
        </div>

        <div class="command-target" aria-label="当前命令目标">
          <p class="scenario">{{ currentExercise?.scenario ?? '正在加载命令练习...' }}</p>
          <div class="target-command" aria-label="要练习的命令">
            <span v-for="token in commandTokens" :key="token">{{ token }}</span>
          </div>
          <p class="explanation">{{ currentExercise?.explanation ?? '系统会根据你的输入给出即时反馈。' }}</p>
        </div>

        <form class="drill-entry" @submit.prevent="submitExercise">
          <label class="sr-only" for="command-drill-input">命令练习输入</label>
          <span aria-hidden="true">$</span>
          <input
            id="command-drill-input"
            ref="inputRef"
            v-model="commandInput"
            :disabled="loading || submitting || completed"
            autocomplete="off"
            autocapitalize="off"
            spellcheck="false"
            placeholder="照着当前命令手敲一遍"
          />
          <button type="submit" :disabled="loading || submitting || completed">
            {{ submitting ? 'checking' : 'enter' }}
          </button>
        </form>

        <div class="feedback-row" :class="`feedback-${feedback.kind}`" role="status" aria-live="polite">
          <CheckCircle2 v-if="feedback.kind === 'success'" :size="19" aria-hidden="true" />
          <XCircle v-else-if="feedback.kind === 'warning' || feedback.kind === 'error'" :size="19" aria-hidden="true" />
          <Zap v-else :size="19" aria-hidden="true" />
          <div>
            <strong>{{ feedback.title }}</strong>
            <span>{{ feedback.message }}</span>
          </div>
        </div>

        <div class="completion-panel" v-if="completed">
          <div>
            <span>实战验证已解锁</span>
            <strong>进入终端，把刚练过的命令用在真实环境里。</strong>
          </div>
          <button type="button" @click="openPracticeLab">
            <TerminalSquare :size="17" aria-hidden="true" />
            进入实战终端
          </button>
        </div>
      </section>

      <aside class="coach-panel" aria-label="练习辅助信息">
        <div class="coach-section">
          <span class="section-label">当前目标</span>
          <h2>{{ currentExercise?.title ?? '准备中' }}</h2>
          <p>{{ currentExercise?.hint ?? '加载后即可开始手敲。' }}</p>
          <button type="button" class="hint-toggle" @click="showHint = !showHint">
            {{ showHint ? '隐藏提示' : '查看提示' }}
          </button>
          <p v-if="showHint" class="hint-copy">{{ currentExercise?.hint }}</p>
        </div>

        <div class="coach-section">
          <span class="section-label">命令队列</span>
          <ol class="exercise-list">
            <li
              v-for="(exercise, index) in exercises"
              :key="exercise.id"
              :class="{ active: index === activeIndex, done: completedIds.has(exercise.id) }"
            >
              <span>{{ String(index + 1).padStart(2, '0') }}</span>
              <div>
                <strong>{{ exercise.expectedCommand }}</strong>
                <small>{{ exercise.title }}</small>
              </div>
            </li>
          </ol>
        </div>

        <div class="coach-section">
          <span class="section-label">掌握状态</span>
          <div class="mastery-grid">
            <div>
              <strong>{{ progress?.completedExercises ?? 0 }}/{{ progress?.totalExercises ?? exercises.length }}</strong>
              <span>已完成</span>
            </div>
            <div>
              <strong>{{ progress?.bestStreak ?? 0 }}</strong>
              <span>最佳连续</span>
            </div>
          </div>
        </div>
      </aside>
    </main>

    <main v-else class="lab-stage" aria-label="实战终端验证">
      <header class="lab-header">
        <div>
          <span>LAB TERMINAL</span>
          <h2>{{ labSession?.labTitle ?? '实战终端' }}</h2>
        </div>
        <div class="lab-actions">
          <button type="button" @click="labOpen = false">回到练习</button>
          <button type="button" :disabled="labStarting" @click="resetLab">
            <RotateCcw :size="16" aria-hidden="true" />
            reset
          </button>
        </div>
      </header>

      <div ref="labBody" class="lab-terminal" role="log" aria-live="polite">
        <pre v-for="line in labLines" :key="line.id" :class="`lab-line-${line.kind}`">{{ line.text }}</pre>
      </div>

      <form class="lab-compose" @submit.prevent="submitLabCommand">
        <label class="sr-only" for="lab-command">实战终端命令输入</label>
        <span aria-hidden="true">$</span>
        <input
          id="lab-command"
          v-model="labInput"
          :disabled="!labReady"
          autocomplete="off"
          autocapitalize="off"
          spellcheck="false"
          placeholder="在实战终端输入 Linux 命令"
        />
        <button type="submit" :disabled="!labReady">run</button>
      </form>
    </main>
  </section>
</template>
