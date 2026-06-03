<script setup lang="ts">
import { Layers3, Route, Server, ShieldCheck } from '@lucide/vue'
import type { CatalogResponse, LabView } from '../types'

defineProps<{
  catalog: CatalogResponse | null
  selectedPathCode: string
  selectedLabId: number | null
}>()

const emit = defineEmits<{
  selectPath: [code: string]
  selectLab: [lab: LabView]
}>()
</script>

<template>
  <aside class="sidebar">
    <div class="sidebar-title">
      <div class="logo-box">
        <ShieldCheck :size="20" />
      </div>
      <div>
        <strong>Linux Lab</strong>
        <span>VM 沙箱训练</span>
      </div>
    </div>

    <section class="sidebar-section">
      <div class="section-label">
        <Route :size="15" />
        学习路径
      </div>
      <button
        v-for="path in catalog?.learningPaths ?? []"
        :key="path.code"
        class="path-card"
        :class="{ active: selectedPathCode === path.code }"
        @click="emit('selectPath', path.code)"
      >
        <span>{{ path.title }}</span>
        <small>{{ path.description }}</small>
      </button>
    </section>

    <section class="sidebar-section course-scroll">
      <div class="section-label">
        <Layers3 :size="15" />
        课程阶段
      </div>
      <div v-if="!catalog" class="sidebar-empty">正在读取课程...</div>
      <div v-for="course in catalog?.courses ?? []" :key="course.id" class="course-block">
        <h2>{{ course.title }}</h2>
        <p>{{ course.summary }}</p>
        <div v-for="module in course.modules" :key="module.id" class="module-block">
          <div class="module-heading">
            <Server :size="15" />
            {{ module.title }}
          </div>
          <button
            v-for="lesson in module.lessons"
            :key="lesson.id"
            class="lesson-row"
            :class="{ active: lesson.lab?.id === selectedLabId }"
            :disabled="!lesson.lab"
            @click="lesson.lab && emit('selectLab', lesson.lab)"
          >
            <span>{{ lesson.title }}</span>
            <small>{{ lesson.durationMinutes }} 分钟</small>
          </button>
        </div>
      </div>
    </section>
  </aside>
</template>
