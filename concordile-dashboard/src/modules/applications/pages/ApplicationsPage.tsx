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

import { useEffect, useState } from 'react'

import {
  ArrowCounterClockwiseIcon,
  MagnifyingGlassIcon,
} from '@phosphor-icons/react'

import { ApplicationsTable } from '@/modules/applications/components/ApplicationsTable'
import { useSearchApplications } from '@/shared/api'
import { DeploymentCheckStatus } from '@/shared/api/generated/models/deploymentCheckStatus'
import { VerificationStatus } from '@/shared/api/generated/models/verificationStatus'
import { Button } from '@/shared/ui/kit/button'
import { Card, CardContent } from '@/shared/ui/kit/card'
import { Input } from '@/shared/ui/kit/input'
import {
  Select,
  SelectContent,
  SelectItem,
  SelectTrigger,
  SelectValue,
} from '@/shared/ui/kit/select'

const VERIFICATION_FILTER_ALL = '__all__'
const DEPLOYMENT_FILTER_ALL = '__all__'

const deploymentCheckStatusValues = Object.values(DeploymentCheckStatus)
const verificationStatusValues = Object.values(VerificationStatus)

export function ApplicationsPage() {
  const [queryInput, setQueryInput] = useState('')
  const [debouncedQuery, setDebouncedQuery] = useState('')
  const [verificationFilter, setVerificationFilter] = useState(
    VERIFICATION_FILTER_ALL,
  )
  const [deploymentFilter, setDeploymentFilter] = useState(
    DEPLOYMENT_FILTER_ALL,
  )
  const [page, setPage] = useState(0)
  const [size, setSize] = useState(10)

  useEffect(() => {
    const id = window.setTimeout(() => {
      setDebouncedQuery((prev) => {
        const next = queryInput.trim()
        if (next !== prev) {
          queueMicrotask(() => {
            setPage(0)
          })
        }
        return next
      })
    }, 300)
    return () => window.clearTimeout(id)
  }, [queryInput])

  const verificationStatus =
    verificationFilter === VERIFICATION_FILTER_ALL
      ? undefined
      : (verificationFilter as VerificationStatus)
  const deploymentCheckStatus =
    deploymentFilter === DEPLOYMENT_FILTER_ALL
      ? undefined
      : (deploymentFilter as DeploymentCheckStatus)

  const { data, isPending, isError, refetch } = useSearchApplications({
    query: debouncedQuery.trim() || undefined,
    verificationStatus,
    deploymentCheckStatus,
    page,
    size,
  })

  const rows = data?.content ?? []
  const pageMetadata = data?.page

  const handleResetFilters = () => {
    setQueryInput('')
    setDebouncedQuery('')
    setVerificationFilter(VERIFICATION_FILTER_ALL)
    setDeploymentFilter(DEPLOYMENT_FILTER_ALL)
    setPage(0)
    setSize(10)
  }

  const handlePageSizeChange = (next: number) => {
    setSize(next)
    setPage(0)
  }

  return (
    <main className="flex flex-col gap-4">
      <h1 className="text-lg font-medium">Applications</h1>

      <Card size="sm">
        <CardContent>
          <div className="flex flex-col gap-4 md:flex-row md:flex-wrap md:items-end">
            <div className="flex min-w-[14rem] max-w-md flex-1 flex-col gap-1">
              <span className="text-xs text-muted-foreground">Search</span>
              <div className="relative">
                <MagnifyingGlassIcon className="pointer-events-none absolute top-1/2 left-2.5 -translate-y-1/2 text-muted-foreground" />
                <Input
                  aria-label="Search by name or group ID"
                  placeholder="Search by name or group ID"
                  className="pl-8"
                  value={queryInput}
                  onChange={(e) => setQueryInput(e.target.value)}
                />
              </div>
            </div>

            <div className="flex flex-col gap-1">
              <span className="text-xs text-muted-foreground">
                Verification
              </span>
              <Select
                value={verificationFilter}
                onValueChange={(v) => {
                  setVerificationFilter(v)
                  setPage(0)
                }}
              >
                <SelectTrigger className="min-w-40">
                  <SelectValue placeholder="All" />
                </SelectTrigger>
                <SelectContent position="popper">
                  <SelectItem value={VERIFICATION_FILTER_ALL}>All</SelectItem>
                  {verificationStatusValues.map((s) => (
                    <SelectItem key={s} value={s}>
                      {s}
                    </SelectItem>
                  ))}
                </SelectContent>
              </Select>
            </div>

            <div className="flex flex-col gap-1">
              <span className="text-xs text-muted-foreground">
                Deployment Check
              </span>
              <Select
                value={deploymentFilter}
                onValueChange={(v) => {
                  setDeploymentFilter(v)
                  setPage(0)
                }}
              >
                <SelectTrigger className="min-w-40">
                  <SelectValue placeholder="All" />
                </SelectTrigger>
                <SelectContent position="popper">
                  <SelectItem value={DEPLOYMENT_FILTER_ALL}>All</SelectItem>
                  {deploymentCheckStatusValues.map((s) => (
                    <SelectItem key={s} value={s}>
                      {s}
                    </SelectItem>
                  ))}
                </SelectContent>
              </Select>
            </div>

            <Button
              type="button"
              variant="outline"
              size="sm"
              onClick={handleResetFilters}
            >
              <ArrowCounterClockwiseIcon data-icon="inline-start" />
              Reset filters
            </Button>
          </div>
        </CardContent>
      </Card>

      <ApplicationsTable
        rows={rows}
        pageMetadata={pageMetadata}
        pageIndex={page}
        pageSize={size}
        onPageIndexChange={setPage}
        onPageSizeChange={handlePageSizeChange}
        isPending={isPending}
        isError={isError}
        onRetry={() => void refetch()}
      />
    </main>
  )
}
