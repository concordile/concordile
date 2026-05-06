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

import { useSyncExternalStore } from 'react'
import { Link, useLocation } from 'react-router-dom'

import { MoonIcon, SidebarIcon, SunIcon } from '@phosphor-icons/react'

import { useTheme } from '@/app/theme'
import type { Theme } from '@/app/theme/theme-types'
import { routes } from '@/shared/config/routes'
import {
  Breadcrumb,
  BreadcrumbItem,
  BreadcrumbLink,
  BreadcrumbList,
  BreadcrumbPage,
  BreadcrumbSeparator,
} from '@/shared/ui/kit/breadcrumb'
import { Button } from '@/shared/ui/kit/button'
import { Separator } from '@/shared/ui/kit/separator'
import { useSidebar } from '@/shared/ui/kit/sidebar'

function subscribeSystemColorScheme(callback: () => void) {
  const mq = window.matchMedia('(prefers-color-scheme: dark)')
  mq.addEventListener('change', callback)
  return () => mq.removeEventListener('change', callback)
}

function getSystemPrefersDark() {
  return window.matchMedia('(prefers-color-scheme: dark)').matches
}

function useSystemPrefersDark() {
  return useSyncExternalStore(
    subscribeSystemColorScheme,
    getSystemPrefersDark,
    () => false,
  )
}

function useEffectiveDark(theme: Theme): boolean {
  const systemDark = useSystemPrefersDark()
  if (theme === 'dark') {
    return true
  }
  if (theme === 'light') {
    return false
  }
  return systemDark
}

function breadcrumbPageLabel(pathname: string): string {
  if (pathname === routes.overview) {
    return 'Overview'
  }
  if (pathname === routes.applications) {
    return 'Applications'
  }
  if (pathname === routes.verifications) {
    return 'Verifications'
  }
  if (pathname.startsWith(`${routes.applications}/`)) {
    return 'Application'
  }
  return 'Page'
}

export function SiteHeader() {
  const { toggleSidebar } = useSidebar()
  const { pathname } = useLocation()
  const { theme, setTheme } = useTheme()
  const isDark = useEffectiveDark(theme)

  return (
    <header className="sticky top-0 z-40 w-full shrink-0 border-b bg-background">
      <div className="flex w-full items-center gap-2 p-2">
        <Button
          type="button"
          variant="ghost"
          size="icon"
          onClick={toggleSidebar}
          aria-label="Toggle sidebar display"
        >
          <SidebarIcon />
        </Button>
        <Separator
          orientation="vertical"
          className="shrink-0 data-vertical:h-4 data-vertical:self-center"
        />
        <Breadcrumb className="hidden min-w-0 flex-1 sm:block pl-2">
          <BreadcrumbList>
            <BreadcrumbItem>
              <BreadcrumbLink asChild>
                <Link to={routes.overview}>Dashboard</Link>
              </BreadcrumbLink>
            </BreadcrumbItem>
            <BreadcrumbSeparator />
            <BreadcrumbItem>
              <BreadcrumbPage>{breadcrumbPageLabel(pathname)}</BreadcrumbPage>
            </BreadcrumbItem>
          </BreadcrumbList>
        </Breadcrumb>
        <Button
          type="button"
          variant="ghost"
          size="icon"
          className="ml-auto shrink-0"
          onClick={() => setTheme(isDark ? 'light' : 'dark')}
          aria-label={isDark ? 'Switch to light mode' : 'Switch to dark mode'}
        >
          {isDark ? <SunIcon /> : <MoonIcon />}
        </Button>
      </div>
    </header>
  )
}
