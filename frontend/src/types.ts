export interface ApiResponse<T> {
  success: boolean
  data: T
  message: string
}

export type LearningPathCode = 'BEGINNER' | 'OPS'

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
  instruction: string
  checkerType: string
  hint: string
  score: number
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
