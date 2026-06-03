<script setup lang="ts">
import { CheckCircle2, Circle, ClipboardCheck, HelpCircle, LoaderCircle } from '@lucide/vue'
import type { LabSessionResponse, LabView, TaskProgressView } from '../types'

const props = defineProps<{
  lab: LabView | null
  session: LabSessionResponse | null
  checkingTaskId: number | null
}>()

const emit = defineEmits<{
  check: [taskId: number]
}>()

function progressFor(taskId: number): TaskProgressView | undefined {
  return props.session?.tasks.find((task) => task.taskId === taskId)
}
</script>

<template>
  <aside class="task-panel">
    <section class="progress-card">
      <div class="progress-header">
        <div>
          <p class="panel-kicker">实验进度</p>
          <h3>{{ session?.progressPercent ?? 0 }}%</h3>
        </div>
        <ClipboardCheck :size="24" />
      </div>
      <div class="progress-track">
        <span :style="{ width: `${session?.progressPercent ?? 0}%` }" />
      </div>
      <p>{{ session ? `VM ${session.vmId} · ${session.status}` : '启动实验后开始记录任务进度' }}</p>
    </section>

    <section class="task-list">
      <div class="task-list-title">任务检查</div>
      <article v-for="task in lab?.tasks ?? []" :key="task.id" class="task-card">
        <div class="task-card-head">
          <component :is="progressFor(task.id)?.passed ? CheckCircle2 : Circle" :size="19" />
          <div>
            <h4>{{ task.title }}</h4>
            <span>{{ task.checkerType }} · {{ task.score }} 分</span>
          </div>
        </div>
        <p>{{ task.instruction }}</p>
        <details>
          <summary>
            <HelpCircle :size="15" />
            查看提示
          </summary>
          <p>{{ task.hint }}</p>
        </details>
        <div class="task-result">{{ progressFor(task.id)?.lastMessage ?? '尚未检查' }}</div>
        <button
          class="check-button"
          :disabled="!session || checkingTaskId === task.id"
          @click="emit('check', task.id)"
        >
          <LoaderCircle v-if="checkingTaskId === task.id" class="spin" :size="16" />
          <CheckCircle2 v-else :size="16" />
          运行检查
        </button>
      </article>
      <div v-if="!lab" class="empty-panel">请选择左侧课程实验。</div>
    </section>
  </aside>
</template>
