<script setup lang="ts">
import { computed, onMounted, reactive, ref, watch } from 'vue'
import { Database, FilePlus2, RefreshCcw, Rows3, Save, Server } from '@lucide/vue'
import { createCourse, createLab, createTask, getLearningProgress } from '../api'
import type { CatalogResponse, ProgressRow } from '../types'

const props = defineProps<{
  catalog: CatalogResponse | null
}>()

const emit = defineEmits<{
  refreshCatalog: []
}>()

const courseForm = reactive({
  title: '自定义 Linux 运维课',
  summary: '面向团队内部训练的手敲实践课程。',
  initialModuleTitle: '服务部署',
  initialLessonTitle: '发布一个 Web 服务',
})

const labForm = reactive({
  lessonId: 0,
  title: '服务部署实验',
  description: '在 VM 内完成服务安装、启动和状态检查。',
  imageRef: 'ubuntu-22.04-service',
  cpuCores: 2,
  memoryMb: 2048,
  timeoutMinutes: 45,
  networkWhitelist: 'mirrors.aliyun.com,platform.internal',
  sandboxTier: 'VM',
})

const taskForm = reactive({
  labId: 0,
  title: '确认服务状态',
  instruction: '执行 `systemctl status nginx` 并确认服务处于 active running。',
  checkerType: 'SERVICE_ACTIVE',
  expected: 'active (running)',
  hint: '关注输出中的 Active 行。',
  score: 20,
})

const progressRows = ref<ProgressRow[]>([])
const message = ref('后台可直接发布课程、实验和判题任务。')
const submitting = ref(false)

const lessonOptions = computed(() => {
  const options: { id: number; label: string }[] = []
  for (const course of props.catalog?.courses ?? []) {
    for (const module of course.modules) {
      for (const lesson of module.lessons) {
        options.push({ id: lesson.id, label: `${course.title} / ${module.title} / ${lesson.title}` })
      }
    }
  }
  return options
})

const labOptions = computed(() => {
  const options: { id: number; label: string }[] = []
  for (const course of props.catalog?.courses ?? []) {
    for (const module of course.modules) {
      for (const lesson of module.lessons) {
        if (lesson.lab) {
          options.push({ id: lesson.lab.id, label: `${module.title} / ${lesson.lab.title}` })
        }
      }
    }
  }
  return options
})

watch(
  lessonOptions,
  (options) => {
    if (!labForm.lessonId && options[0]) {
      labForm.lessonId = options[0].id
    }
  },
  { immediate: true },
)

watch(
  labOptions,
  (options) => {
    if (!taskForm.labId && options[0]) {
      taskForm.labId = options[0].id
    }
  },
  { immediate: true },
)

onMounted(loadProgress)

async function submitCourse() {
  await submit(async () => {
    const result = await createCourse(courseForm)
    message.value = `课程已创建：#${result.id}`
    emit('refreshCatalog')
  })
}

async function submitLab() {
  await submit(async () => {
    const result = await createLab(labForm)
    message.value = `实验已创建：#${result.id}`
    emit('refreshCatalog')
  })
}

async function submitTask() {
  await submit(async () => {
    const result = await createTask(taskForm)
    message.value = `任务已创建：#${result.id}`
    emit('refreshCatalog')
  })
}

async function loadProgress() {
  try {
    progressRows.value = (await getLearningProgress()).rows
  } catch (error) {
    message.value = error instanceof Error ? error.message : '学习数据读取失败'
  }
}

async function submit(action: () => Promise<void>) {
  submitting.value = true
  try {
    await action()
  } catch (error) {
    message.value = error instanceof Error ? error.message : '提交失败'
  } finally {
    submitting.value = false
  }
}
</script>

<template>
  <section class="admin-grid">
    <header class="admin-header">
      <div>
        <p class="panel-kicker">内容运营</p>
        <h2>轻量后台</h2>
        <span>{{ message }}</span>
      </div>
      <button class="secondary-button" @click="loadProgress">
        <RefreshCcw :size="17" />
        刷新学习数据
      </button>
    </header>

    <form class="admin-card" @submit.prevent="submitCourse">
      <div class="admin-card-title">
        <FilePlus2 :size="19" />
        创建课程
      </div>
      <label>
        课程名称
        <input v-model="courseForm.title" required />
      </label>
      <label>
        课程摘要
        <textarea v-model="courseForm.summary" rows="3" required />
      </label>
      <div class="form-row">
        <label>
          初始阶段
          <input v-model="courseForm.initialModuleTitle" required />
        </label>
        <label>
          初始课时
          <input v-model="courseForm.initialLessonTitle" required />
        </label>
      </div>
      <button class="primary-button" :disabled="submitting">
        <Save :size="17" />
        发布课程
      </button>
    </form>

    <form class="admin-card" @submit.prevent="submitLab">
      <div class="admin-card-title">
        <Server :size="19" />
        创建实验
      </div>
      <label>
        绑定课时
        <select v-model.number="labForm.lessonId" required>
          <option v-for="option in lessonOptions" :key="option.id" :value="option.id">
            {{ option.label }}
          </option>
        </select>
      </label>
      <label>
        实验标题
        <input v-model="labForm.title" required />
      </label>
      <label>
        实验说明
        <textarea v-model="labForm.description" rows="3" required />
      </label>
      <div class="form-row three">
        <label>
          镜像
          <input v-model="labForm.imageRef" required />
        </label>
        <label>
          CPU
          <input v-model.number="labForm.cpuCores" type="number" min="1" required />
        </label>
        <label>
          内存 MB
          <input v-model.number="labForm.memoryMb" type="number" min="512" required />
        </label>
      </div>
      <div class="form-row">
        <label>
          时长分钟
          <input v-model.number="labForm.timeoutMinutes" type="number" min="5" required />
        </label>
        <label>
          沙箱级别
          <select v-model="labForm.sandboxTier">
            <option value="VM">VM</option>
            <option value="KUBEVIRT">KUBEVIRT</option>
          </select>
        </label>
      </div>
      <label>
        网络白名单
        <input v-model="labForm.networkWhitelist" required />
      </label>
      <button class="primary-button" :disabled="submitting || !lessonOptions.length">
        <Save :size="17" />
        发布实验
      </button>
    </form>

    <form class="admin-card" @submit.prevent="submitTask">
      <div class="admin-card-title">
        <Rows3 :size="19" />
        创建任务
      </div>
      <label>
        绑定实验
        <select v-model.number="taskForm.labId" required>
          <option v-for="option in labOptions" :key="option.id" :value="option.id">
            {{ option.label }}
          </option>
        </select>
      </label>
      <label>
        任务标题
        <input v-model="taskForm.title" required />
      </label>
      <label>
        任务说明
        <textarea v-model="taskForm.instruction" rows="3" required />
      </label>
      <div class="form-row">
        <label>
          检查类型
          <select v-model="taskForm.checkerType">
            <option value="OUTPUT_CONTAINS">输出包含</option>
            <option value="FILE_EXISTS">文件存在</option>
            <option value="SERVICE_ACTIVE">服务活跃</option>
          </select>
        </label>
        <label>
          分值
          <input v-model.number="taskForm.score" type="number" min="1" required />
        </label>
      </div>
      <label>
        预期结果
        <input v-model="taskForm.expected" required />
      </label>
      <label>
        提示
        <input v-model="taskForm.hint" required />
      </label>
      <button class="primary-button" :disabled="submitting || !labOptions.length">
        <Save :size="17" />
        发布任务
      </button>
    </form>

    <section class="admin-card progress-table-card">
      <div class="admin-card-title">
        <Database :size="19" />
        学习数据
      </div>
      <div class="table-wrap">
        <table>
          <thead>
            <tr>
              <th>学员</th>
              <th>实验</th>
              <th>状态</th>
              <th>进度</th>
              <th>开始时间</th>
            </tr>
          </thead>
          <tbody>
            <tr v-for="row in progressRows" :key="row.sessionId">
              <td>{{ row.displayName }}</td>
              <td>{{ row.labTitle }}</td>
              <td>{{ row.status }}</td>
              <td>{{ row.progressPercent }}%</td>
              <td>{{ new Date(row.startedAt).toLocaleString() }}</td>
            </tr>
            <tr v-if="!progressRows.length">
              <td colspan="5">暂无实验会话。</td>
            </tr>
          </tbody>
        </table>
      </div>
    </section>
  </section>
</template>
