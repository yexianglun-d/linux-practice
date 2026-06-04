import type {
  ApiResponse,
  CheckTaskResponse,
  LabSessionResponse,
  LearningPathCode,
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

export function startDefaultLabSession(learningPath?: LearningPathCode) {
  return request<LabSessionResponse>('/api/lab-sessions/default', {
    method: 'POST',
    body: JSON.stringify({ learningPath }),
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
