export interface ApiResponse<T> {
  success: boolean
  data: T
  message: string
}

export interface CatalogResponse {
  learningPaths: LearningPathView[]
  courses: CourseView[]
}

export interface LearningPathView {
  code: 'BEGINNER' | 'OPS'
  title: string
  description: string
  recommendedFirstLabId: number | null
}

export interface CourseView {
  id: number
  title: string
  summary: string
  modules: ModuleView[]
}

export interface ModuleView {
  id: number
  title: string
  outcome: string
  lessons: LessonView[]
}

export interface LessonView {
  id: number
  title: string
  objective: string
  durationMinutes: number
  lab: LabView | null
}

export interface LabView {
  id: number
  title: string
  description: string
  imageRef: string
  cpuCores: number
  memoryMb: number
  timeoutMinutes: number
  networkWhitelist: string
  sandboxTier: string
  tasks: TaskView[]
}

export interface TaskView {
  id: number
  title: string
  instruction: string
  checkerType: string
  hint: string
  score: number
}

export interface LabSessionResponse {
  id: number
  labId: number
  labTitle: string
  status: string
  vmId: string
  vmHost: string
  snapshotRef: string
  startedAt: string
  endedAt: string | null
  expiresAt: string
  progressPercent: number
  tasks: TaskProgressView[]
}

export interface TaskProgressView {
  taskId: number
  title: string
  checkerType: string
  passed: boolean
  lastMessage: string
  checkedAt: string | null
}

export interface CheckTaskResponse {
  taskId: number
  checkerType: string
  passed: boolean
  message: string
  progressPercent: number
}

export interface LearningProgressResponse {
  rows: ProgressRow[]
}

export interface ProgressRow {
  sessionId: number
  username: string
  displayName: string
  labTitle: string
  status: string
  progressPercent: number
  startedAt: string
  expiresAt: string
}
