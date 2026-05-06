/*
 * Copyright 2025-present The Concordile Authors
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

import { type ProblemDetail } from './generated/models'

export type ApiErrorPayload = {
  status: number
  message: string
  problem?: ProblemDetail
  cause?: unknown
}

export class ApiError extends Error {
  readonly status: number
  readonly problem?: ProblemDetail

  constructor(payload: ApiErrorPayload) {
    super(
      payload.message,
      payload.cause !== undefined ? { cause: payload.cause } : undefined,
    )
    this.name = 'ApiError'
    this.status = payload.status
    this.problem = payload.problem
    Object.setPrototypeOf(this, new.target.prototype)
  }
}

export function isApiError(e: unknown): e is ApiError {
  return e instanceof ApiError
}

type HttpMethod = 'GET' | 'POST' | 'PUT' | 'PATCH' | 'DELETE'
type ResponseType = 'auto' | 'json' | 'text' | 'arrayBuffer'

export type RequestConfig = Omit<RequestInit, 'method' | 'body'> & {
  method?: HttpMethod
  baseUrl?: string
  params?: Record<string, string | number | boolean | undefined | null>
  body?: unknown
  responseType?: ResponseType
}

function buildUrl(
  path: string,
  baseUrl: string,
  params?: RequestConfig['params'],
) {
  const url = new URL(path, baseUrl || window.location.origin)

  if (params) {
    for (const [key, value] of Object.entries(params)) {
      if (value === undefined || value === null) continue
      url.searchParams.set(key, String(value))
    }
  }

  if (!baseUrl) return `${url.pathname}${url.search}${url.hash}`
  return url.toString()
}

function getContentType(headers?: HeadersInit): string | undefined {
  if (!headers) return undefined
  const h = new Headers(headers)
  return h.get('content-type') ?? h.get('Content-Type') ?? undefined
}

function isJsonContentType(contentType?: string) {
  if (!contentType) return false
  return (
    contentType.includes('application/json') ||
    contentType.includes('application/problem+json')
  )
}

function isRawBody(body: unknown): body is BodyInit {
  return (
    typeof body === 'string' ||
    body instanceof Blob ||
    body instanceof FormData ||
    body instanceof URLSearchParams ||
    body instanceof ArrayBuffer
  )
}

async function parseResponseBody(
  res: Response,
  responseType: ResponseType = 'auto',
) {
  if (responseType === 'text') return res.text()
  if (responseType === 'json') return res.json()
  if (responseType === 'arrayBuffer') return res.arrayBuffer()
  const contentType = res.headers.get('content-type') || ''
  if (isJsonContentType(contentType)) return res.json()
  if (contentType.startsWith('text/')) return res.text()
  return res.arrayBuffer()
}

function isProblemDetail(data: unknown): data is ProblemDetail {
  return (
    typeof data === 'object' &&
    data !== null &&
    ('title' in data ||
      'detail' in data ||
      'status' in data ||
      'type' in data ||
      'instance' in data)
  )
}

async function parseApiError(res: Response): Promise<ApiError> {
  let problem: ProblemDetail | undefined
  let message = `Request failed with status ${res.status}`

  try {
    const data = await parseResponseBody(res)

    if (isProblemDetail(data)) {
      problem = data
      message = problem.title || problem.detail || message
    } else if (typeof data === 'string' && data) {
      message = data
    }
  } catch {
    // ignore
  }

  return new ApiError({
    status: res.status,
    message,
    problem,
  })
}

export async function request<T>(
  path: string,
  config: RequestConfig = {},
): Promise<T> {
  const {
    method = 'GET',
    baseUrl = '',
    params,
    body,
    headers,
    responseType = 'auto',
    ...rest
  } = config

  const url = buildUrl(path, baseUrl, params)

  const existingContentType = getContentType(headers)

  const finalHeaders = new Headers(headers)

  let finalBody: BodyInit | undefined

  if (body !== undefined) {
    if (isRawBody(body)) {
      finalBody = body
    } else {
      if (!existingContentType) {
        finalHeaders.set('Content-Type', 'application/json')
      }
      finalBody = JSON.stringify(body)
    }
  }

  let res: Response
  try {
    res = await fetch(url, {
      method,
      credentials: 'include',
      headers: finalHeaders,
      ...(finalBody !== undefined ? { body: finalBody } : {}),
      ...rest,
    })
  } catch (cause) {
    // network/CORS/aborted
    throw new ApiError({
      status: 0,
      message: 'Network error',
      cause,
    })
  }
  if (res.ok) {
    if (res.status === 204) return undefined as T
    return (await parseResponseBody(res, responseType)) as T
  }
  throw await parseApiError(res)
}
