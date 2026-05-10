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

import {
  CheckCircleIcon,
  QuestionIcon,
  XCircleIcon,
} from '@phosphor-icons/react'

import { DeploymentCheckStatus } from '@/shared/api/generated/models/deploymentCheckStatus'
import { VerificationStatus } from '@/shared/api/generated/models/verificationStatus'
import { Badge } from '@/shared/ui/kit/badge'

export function VerificationStatusBadge({
  status,
}: {
  status?: VerificationStatus
}) {
  if (status == null) {
    return (
      <Badge variant="outline">
        <QuestionIcon />
        Unknown
      </Badge>
    )
  }
  if (status === VerificationStatus.PASSED) {
    return (
      <Badge variant="secondary">
        <CheckCircleIcon />
        Passed
      </Badge>
    )
  }
  return (
    <Badge variant="destructive">
      <XCircleIcon />
      Failed
    </Badge>
  )
}

export function DeploymentCheckStatusBadge({
  status,
}: {
  status?: DeploymentCheckStatus
}) {
  if (status == null) {
    return (
      <Badge variant="outline">
        <QuestionIcon />
        Unknown
      </Badge>
    )
  }
  if (status === DeploymentCheckStatus.READY) {
    return (
      <Badge variant="secondary">
        <CheckCircleIcon />
        Ready
      </Badge>
    )
  }
  if (status === DeploymentCheckStatus.BLOCKED) {
    return (
      <Badge variant="destructive">
        <XCircleIcon />
        Blocked
      </Badge>
    )
  }
  return (
    <Badge variant="outline">
      <QuestionIcon />
      Unknown
    </Badge>
  )
}
