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

export interface CommandExerciseCatalogResponse {
  chapterTitle: string
  exercises: CommandExercise[]
  progress: CommandProgress
}

export interface CommandExercise {
  id: string
  title: string
  scenario: string
  expectedCommand: string
  acceptedCommands: string[]
  hint: string
  explanation: string
  difficulty: string
  tags: string[]
  sortOrder: number
}

export interface CommandAttemptResponse {
  exerciseId: string
  correct: boolean
  errorType: string
  message: string
  expectedCommand: string
  explanation: string
  progress: CommandProgress
}

export interface CommandProgress {
  totalExercises: number
  completedExercises: number
  completedExerciseIds: string[]
  attempts: number
  correctAttempts: number
  currentStreak: number
  bestStreak: number
  accuracyPercent: number
  mastery: string
}
