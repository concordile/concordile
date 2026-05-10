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

import { BuildingsIcon, CaretRightIcon } from '@phosphor-icons/react'
import type { ColumnDef } from '@tanstack/react-table'

import type { ApplicationItem } from '@/shared/api/generated/models/applicationItem'

import {
  DeploymentCheckStatusBadge,
  VerificationStatusBadge,
} from './application-status-badges'

const lastActivityFormatter = new Intl.DateTimeFormat(undefined, {
  dateStyle: 'medium',
  timeStyle: 'short',
})

function formatLastActivity(value?: string) {
  if (value == null || value === '') {
    return '—'
  }
  const d = new Date(value)
  if (Number.isNaN(d.getTime())) {
    return '—'
  }
  return lastActivityFormatter.format(d)
}

export function getApplicationsTableColumns(): ColumnDef<ApplicationItem>[] {
  return [
    {
      id: 'name',
      header: 'Name',
      cell: ({ row }) => {
        const name = row.original.name
        return (
          <div className="flex items-center gap-2">
            <BuildingsIcon className="text-muted-foreground" />
            <span className="font-medium text-primary">
              {name != null && name !== '' ? name : '—'}
            </span>
          </div>
        )
      },
    },
    {
      accessorKey: 'groupId',
      header: 'Group ID',
      cell: ({ getValue }) => {
        const v = getValue<string | undefined>()
        return v != null && v !== '' ? v : '—'
      },
    },
    {
      accessorKey: 'providedContracts',
      header: 'Provided Contracts',
      cell: ({ getValue }) => {
        const v = getValue<number | undefined>()
        return v != null ? v : '—'
      },
    },
    {
      accessorKey: 'consumedContracts',
      header: 'Consumed Contracts',
      cell: ({ getValue }) => {
        const v = getValue<number | undefined>()
        return v != null ? v : '—'
      },
    },
    {
      id: 'latestVerificationStatus',
      header: 'Latest Verification Status',
      cell: ({ row }) => (
        <VerificationStatusBadge
          status={row.original.latestVerificationStatus}
        />
      ),
    },
    {
      id: 'latestDeploymentCheckStatus',
      header: 'Latest Deployment Check Status',
      cell: ({ row }) => (
        <DeploymentCheckStatusBadge
          status={row.original.latestDeploymentCheckStatus}
        />
      ),
    },
    {
      id: 'lastActivity',
      header: 'Last Activity',
      cell: ({ row }) => formatLastActivity(row.original.lastActivity),
    },
    {
      id: 'action',
      header: () => <span className="sr-only">Open details</span>,
      cell: () => <CaretRightIcon className="text-muted-foreground" />,
    },
  ]
}
