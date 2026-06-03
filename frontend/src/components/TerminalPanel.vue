<script setup lang="ts">
import { computed, nextTick, onBeforeUnmount, ref, watch } from 'vue'
import { SendHorizonal, Terminal } from '@lucide/vue'

const props = defineProps<{
  sessionId?: number
  enabled: boolean
}>()

const emit = defineEmits<{
  transcript: [value: string]
}>()

const lines = ref<string[]>([])
const command = ref('')
const terminalBody = ref<HTMLElement | null>(null)
let socket: WebSocket | null = null

const connectionLabel = computed(() => {
  if (!props.enabled) {
    return '等待启动'
  }
  if (socket?.readyState === WebSocket.OPEN) {
    return 'WebSocket 已连接'
  }
  return '连接中'
})

watch(
  () => props.sessionId,
  () => {
    connect()
  },
  { immediate: true },
)

onBeforeUnmount(() => {
  socket?.close()
})

function connect() {
  socket?.close()
  socket = null
  lines.value = []
  if (!props.sessionId) {
    lines.value.push('启动实验后，这里会连接到浏览器 Linux VM 终端。')
    emitTranscript()
    return
  }
  const protocol = window.location.protocol === 'https:' ? 'wss' : 'ws'
  socket = new WebSocket(`${protocol}://${window.location.host}/ws/lab-sessions/${props.sessionId}/terminal`)
  socket.onmessage = (event) => {
    lines.value.push(event.data)
    emitTranscript()
    scrollToBottom()
  }
  socket.onopen = () => {
    lines.value.push('[terminal] connected')
    emitTranscript()
    scrollToBottom()
  }
  socket.onclose = () => {
    lines.value.push('[terminal] disconnected')
    emitTranscript()
  }
  socket.onerror = () => {
    lines.value.push('[terminal] connection error')
    emitTranscript()
  }
}

function sendCommand() {
  const value = command.value.trim()
  if (!value) {
    return
  }
  lines.value.push(`student@lab:~$ ${value}`)
  if (socket?.readyState === WebSocket.OPEN) {
    socket.send(value)
  } else {
    lines.value.push('终端未连接，命令已保存在本地 transcript。')
  }
  command.value = ''
  emitTranscript()
  scrollToBottom()
}

function emitTranscript() {
  emit('transcript', lines.value.join('\n'))
}

async function scrollToBottom() {
  await nextTick()
  if (terminalBody.value) {
    terminalBody.value.scrollTop = terminalBody.value.scrollHeight
  }
}
</script>

<template>
  <section class="terminal-panel">
    <header>
      <div>
        <Terminal :size="18" />
        浏览器终端
      </div>
      <span>{{ connectionLabel }}</span>
    </header>
    <div ref="terminalBody" class="terminal-body">
      <pre v-for="(line, index) in lines" :key="index">{{ line }}</pre>
    </div>
    <form class="terminal-input" @submit.prevent="sendCommand">
      <span>$</span>
      <input
        v-model="command"
        :disabled="!enabled"
        autocomplete="off"
        placeholder="输入命令，例如 pwd、touch /tmp/linux-foundation/permission-ok"
      />
      <button type="submit" title="发送命令" :disabled="!enabled">
        <SendHorizonal :size="17" />
      </button>
    </form>
  </section>
</template>
