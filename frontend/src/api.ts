import type {
  ApiResponse,
  CheckTaskResponse,
  CommandAttemptResponse,
  CommandExerciseCatalogResponse,
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
  const rawBody = await response.text()
  const payload = parseApiResponse<T>(response, rawBody)
  if (!response.ok || !payload.success) {
    throw new Error(payload.message || '请求失败')
  }
  return payload.data
}

function parseApiResponse<T>(response: Response, rawBody: string): ApiResponse<T> {
  if (!rawBody.trim()) {
    throw new Error(apiUnavailableMessage(response.status))
  }
  try {
    return JSON.parse(rawBody) as ApiResponse<T>
  } catch {
    throw new Error(response.ok ? '服务端返回了无法解析的数据。' : apiUnavailableMessage(response.status))
  }
}

function apiUnavailableMessage(status: number) {
  return `服务端暂时不可用（HTTP ${status}）：请确认 Spring Boot 后端已启动。`
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

export function getDefaultCommandExercises() {
  return request<CommandExerciseCatalogResponse>('/api/command-exercises/default')
}

export function submitCommandAttempt(exerciseId: string, input: string, elapsedMs: number) {
  return request<CommandAttemptResponse>(`/api/command-exercises/${exerciseId}/attempt`, {
    method: 'POST',
    body: JSON.stringify({ input, elapsedMs }),
  })
}
