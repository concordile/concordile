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

import { useMemo } from 'react'
import { useNavigate } from 'react-router-dom'

import { CaretLeftIcon, CaretRightIcon } from '@phosphor-icons/react'
import {
  flexRender,
  getCoreRowModel,
  useReactTable,
} from '@tanstack/react-table'

import type { ApplicationItem } from '@/shared/api/generated/models/applicationItem'
import type { PageMetadata } from '@/shared/api/generated/models/pageMetadata'
import { routes } from '@/shared/config/routes'
import { cn } from '@/shared/lib/ui-kit-utils'
import { Button } from '@/shared/ui/kit/button'
import { Card, CardContent, CardFooter } from '@/shared/ui/kit/card'
import {
  Select,
  SelectContent,
  SelectItem,
  SelectTrigger,
  SelectValue,
} from '@/shared/ui/kit/select'
import {
  Table,
  TableBody,
  TableCell,
  TableHead,
  TableHeader,
  TableRow,
} from '@/shared/ui/kit/table'

import { getApplicationsTableColumns } from './applications-table-columns'

const PAGE_SIZE_OPTIONS = [10, 20, 50] as const

export type ApplicationsTableProps = {
  rows: ApplicationItem[]
  pageMetadata?: PageMetadata
  pageIndex: number
  pageSize: number
  onPageIndexChange: (pageIndex: number) => void
  onPageSizeChange: (pageSize: number) => void
  isPending: boolean
  isError: boolean
  onRetry: () => void
}

export function ApplicationsTable({
  rows,
  pageMetadata,
  pageIndex,
  pageSize,
  onPageIndexChange,
  onPageSizeChange,
  isPending,
  isError,
  onRetry,
}: ApplicationsTableProps) {
  const navigate = useNavigate()
  const columns = useMemo(() => getApplicationsTableColumns(), [])

  const totalPages = Math.max(0, pageMetadata?.totalPages ?? 0)
  const totalElements = pageMetadata?.totalElements ?? 0
  const pageCount = isError
    ? 0
    : pageMetadata?.totalPages != null
      ? pageMetadata.totalPages
      : isPending
        ? -1
        : 0

  // eslint-disable-next-line react-hooks/incompatible-library -- TanStack Table returns unstable function refs by design
  const table = useReactTable({
    data: rows,
    columns,
    getCoreRowModel: getCoreRowModel(),
    manualPagination: true,
    pageCount,
    state: {
      pagination: {
        pageIndex,
        pageSize,
      },
    },
  })

  const startIndex = totalElements === 0 ? 0 : pageIndex * pageSize + 1
  const endIndex = totalElements === 0 ? 0 : pageIndex * pageSize + rows.length

  const rangeLabel =
    isPending && pageMetadata == null
      ? 'Loading applications…'
      : totalElements === 0
        ? 'Showing 0 of 0 applications'
        : `Showing ${startIndex}-${endIndex} of ${totalElements} applications`

  return (
    <Card>
      <CardContent className="px-0 pt-0">
        {isError ? (
          <div className="flex flex-col items-start gap-3 px-4 py-6">
            <p className="text-destructive">
              Could not load applications. Check your connection and try again.
            </p>
            <Button variant="outline" size="sm" onClick={() => onRetry()}>
              Retry
            </Button>
          </div>
        ) : (
          <Table>
            <TableHeader>
              {table.getHeaderGroups().map((headerGroup) => (
                <TableRow key={headerGroup.id} className="hover:bg-transparent">
                  {headerGroup.headers.map((header) => (
                    <TableHead key={header.id}>
                      {header.isPlaceholder
                        ? null
                        : flexRender(
                            header.column.columnDef.header,
                            header.getContext(),
                          )}
                    </TableHead>
                  ))}
                </TableRow>
              ))}
            </TableHeader>
            <TableBody>
              {isPending ? (
                <TableRow className="hover:bg-transparent">
                  <TableCell
                    colSpan={columns.length}
                    className="h-24 text-center text-muted-foreground"
                  >
                    Loading applications…
                  </TableCell>
                </TableRow>
              ) : rows.length === 0 ? (
                <TableRow className="hover:bg-transparent">
                  <TableCell
                    colSpan={columns.length}
                    className="h-24 text-center text-muted-foreground"
                  >
                    No applications match your filters.
                  </TableCell>
                </TableRow>
              ) : (
                table.getRowModel().rows.map((row) => {
                  const id = row.original.id
                  const canNavigate = id != null && id !== ''
                  return (
                    <TableRow
                      key={row.id}
                      tabIndex={canNavigate ? 0 : undefined}
                      className={cn(canNavigate && 'cursor-pointer')}
                      onClick={() => {
                        if (canNavigate) {
                          navigate(routes.applicationDetails(id))
                        }
                      }}
                      onKeyDown={(e) => {
                        if (
                          canNavigate &&
                          (e.key === 'Enter' || e.key === ' ')
                        ) {
                          e.preventDefault()
                          navigate(routes.applicationDetails(id))
                        }
                      }}
                    >
                      {row.getVisibleCells().map((cell) => (
                        <TableCell key={cell.id}>
                          {flexRender(
                            cell.column.columnDef.cell,
                            cell.getContext(),
                          )}
                        </TableCell>
                      ))}
                    </TableRow>
                  )
                })
              )}
            </TableBody>
          </Table>
        )}
      </CardContent>
      <CardFooter className="flex flex-wrap items-center justify-between gap-4">
        <p className="text-muted-foreground">{rangeLabel}</p>
        <div className="flex flex-wrap items-center gap-4">
          <div className="flex items-center gap-2">
            <span className="text-muted-foreground">Rows per page</span>
            <Select
              value={String(pageSize)}
              onValueChange={(v) => {
                const next = Number(v)
                onPageSizeChange(next)
              }}
              disabled={isPending || isError}
            >
              <SelectTrigger
                size="sm"
                className="min-w-24"
                aria-label="Rows per page"
              >
                <SelectValue />
              </SelectTrigger>
              <SelectContent position="popper">
                {PAGE_SIZE_OPTIONS.map((n) => (
                  <SelectItem key={n} value={String(n)}>
                    {n}
                  </SelectItem>
                ))}
              </SelectContent>
            </Select>
          </div>
          <div className="flex items-center gap-1">
            <Button
              variant="outline"
              size="sm"
              className="size-8 p-0"
              aria-label="Previous page"
              disabled={
                isPending || isError || pageIndex <= 0 || totalPages <= 0
              }
              onClick={() => onPageIndexChange(pageIndex - 1)}
            >
              <CaretLeftIcon />
            </Button>
            {totalPages > 0
              ? Array.from({ length: totalPages }, (_, i) => (
                  <Button
                    key={`page-${i}`}
                    variant={i === pageIndex ? 'default' : 'outline'}
                    size="sm"
                    className="min-w-8 px-2"
                    aria-label={`Page ${i + 1}`}
                    aria-current={i === pageIndex ? 'page' : undefined}
                    disabled={isPending || isError}
                    onClick={() => onPageIndexChange(i)}
                  >
                    {i + 1}
                  </Button>
                ))
              : null}
            <Button
              variant="outline"
              size="sm"
              className="size-8 p-0"
              aria-label="Next page"
              disabled={
                isPending ||
                isError ||
                totalPages <= 0 ||
                pageIndex >= totalPages - 1
              }
              onClick={() => onPageIndexChange(pageIndex + 1)}
            >
              <CaretRightIcon />
            </Button>
          </div>
        </div>
      </CardFooter>
    </Card>
  )
}
