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

import { ApiError, request as doRequest, type RequestConfig } from './http'

type Method = 'GET' | 'POST' | 'PUT' | 'PATCH' | 'DELETE'

export type OrvalRequestConfig = RequestInit & {
  method?: Method
  params?: Record<string, unknown>
  data?: unknown
}

export function request<T>(
  url: string,
  config: OrvalRequestConfig = {},
): Promise<T> & { cancel?: () => void } {
  const controller = new AbortController()

  const {
    method = 'GET',
    headers,
    signal,
    params,
    data,
    body,
    ...rest
  } = config

  let finalBody: unknown = undefined

  if (data !== undefined) {
    finalBody = data
  } else if (body !== undefined) {
    if (typeof body === 'string') {
      try {
        finalBody = JSON.parse(body)
      } catch {
        finalBody = body
      }
    } else {
      finalBody = body
    }
  }

  const req: RequestConfig = {
    method,
    headers,
    params: params as RequestConfig['params'],
    signal: signal ?? controller.signal,
    ...(finalBody !== undefined ? { body: finalBody } : {}),
    ...rest,
  }

  const promise = doRequest<T>(url, req) as Promise<T> & { cancel?: () => void }
  promise.cancel = () => controller.abort()

  return promise
}

export type ErrorType<T = unknown> = ApiError & {
  readonly __errorPayloadType?: T
}
