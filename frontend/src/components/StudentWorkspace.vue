<script setup lang="ts">
import { Clock3, Cpu, Network, Play, RotateCcw, ServerCog } from '@lucide/vue'
import type { LabSessionResponse, LabView } from '../types'
import TaskPanel from './TaskPanel.vue'
import TerminalPanel from './TerminalPanel.vue'

defineProps<{
  loading: boolean
  busy: boolean
  lab: LabView | null
  session: LabSessionResponse | null
  checkingTaskId: number | null
}>()

const emit = defineEmits<{
  start: []
  reset: []
  check: [taskId: number]
  transcript: [value: string]
}>()
</script>

<template>
  <section class="workspace-grid">
    <div class="lab-main">
      <section class="lab-header-panel">
        <div>
          <p class="panel-kicker">当前实验</p>
          <h2>{{ lab?.title ?? '请选择实验' }}</h2>
          <p>{{ lab?.description ?? '从左侧选择一个实验后启动 VM。' }}</p>
        </div>
        <div class="lab-actions">
          <button class="primary-button" :disabled="!lab || busy || loading" @click="emit('start')">
            <Play :size="17" />
            启动实验
          </button>
          <button class="secondary-button" :disabled="!session || busy" @click="emit('reset')">
            <RotateCcw :size="17" />
            重置 VM
          </button>
        </div>
      </section>

      <div class="metric-row">
        <div class="metric-tile">
          <Cpu :size="18" />
          <span>资源</span>
          <strong>{{ lab ? `${lab.cpuCores}C / ${lab.memoryMb}MB` : '-' }}</strong>
        </div>
        <div class="metric-tile">
          <Clock3 :size="18" />
          <span>时长</span>
          <strong>{{ lab ? `${lab.timeoutMinutes} 分钟` : '-' }}</strong>
        </div>
        <div class="metric-tile">
          <Network :size="18" />
          <span>联网</span>
          <strong>白名单</strong>
        </div>
        <div class="metric-tile">
          <ServerCog :size="18" />
          <span>沙箱</span>
          <strong>{{ lab?.sandboxTier ?? '-' }}</strong>
        </div>
      </div>

      <TerminalPanel
        :session-id="session?.id"
        :enabled="Boolean(session)"
        @transcript="emit('transcript', $event)"
      />
    </div>

    <TaskPanel
      :lab="lab"
      :session="session"
      :checking-task-id="checkingTaskId"
      @check="emit('check', $event)"
    />
  </section>
</template>
