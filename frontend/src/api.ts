import type {
  ApiResponse,
  CatalogResponse,
  CheckTaskResponse,
  LabSessionResponse,
  LearningProgressResponse,
} from './types'

async function request<T>(path: string, init?: RequestInit): Promise<T> {
  const response = await fetch(path, {
    headers: {
      'Content-Type': 'application/json',
      ...(init?.headers ?? {}),
    },
    ...init,
  })
  const payload = (await response.json()) as ApiResponse<T>
  if (!response.ok || !payload.success) {
    throw new Error(payload.message || '请求失败')
  }
  return payload.data
}

export function getCatalog() {
  return request<CatalogResponse>('/api/catalog')
}

export function startLabSession(labId: number) {
  return request<LabSessionResponse>('/api/lab-sessions', {
    method: 'POST',
    body: JSON.stringify({ labId }),
  })
}

export function getLabSession(sessionId: number) {
  return request<LabSessionResponse>(`/api/lab-sessions/${sessionId}`)
}

export function checkTask(sessionId: number, taskId: number, commandSummary: string) {
  return request<CheckTaskResponse>(`/api/lab-sessions/${sessionId}/check`, {
    method: 'POST',
    body: JSON.stringify({ taskId, commandSummary }),
  })
}

export function resetLabSession(sessionId: number) {
  return request<LabSessionResponse>(`/api/lab-sessions/${sessionId}/reset`, {
    method: 'POST',
  })
}

export function createCourse(input: {
  title: string
  summary: string
  initialModuleTitle: string
  initialLessonTitle: string
}) {
  return request<{ id: number }>('/api/admin/courses', {
    method: 'POST',
    body: JSON.stringify(input),
  })
}

export function createLab(input: {
  lessonId: number
  title: string
  description: string
  imageRef: string
  cpuCores: number
  memoryMb: number
  timeoutMinutes: number
  networkWhitelist: string
  sandboxTier: string
}) {
  return request<{ id: number }>('/api/admin/labs', {
    method: 'POST',
    body: JSON.stringify(input),
  })
}

export function createTask(input: {
  labId: number
  title: string
  instruction: string
  checkerType: string
  expected: string
  hint: string
  score: number
}) {
  return request<{ id: number }>('/api/admin/tasks', {
    method: 'POST',
    body: JSON.stringify(input),
  })
}

export function getLearningProgress() {
  return request<LearningProgressResponse>('/api/admin/learning-progress')
}
