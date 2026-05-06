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

'use client'

import * as React from 'react'

import { cn } from '@/shared/lib/ui-kit-utils'
import { Sidebar, SidebarContent, SidebarRail } from '@/shared/ui/kit/sidebar'
import { SIDEBAR_DESKTOP_TOP_OFFSET_CLASS } from '@/shared/ui/kit/sidebar/sidebar-constants'
import { NavMain, type NavMainItem } from '@/shared/ui/nav-main'

export type AppSidebarProps = React.ComponentProps<typeof Sidebar> & {
  navItems: NavMainItem[]
}

export function AppSidebar({ navItems, className, ...props }: AppSidebarProps) {
  return (
    <Sidebar
      collapsible="icon"
      className={cn(
        SIDEBAR_DESKTOP_TOP_OFFSET_CLASS,
        'bottom-0 h-auto min-h-0',
        className,
      )}
      {...props}
    >
      <SidebarContent>
        <NavMain items={navItems} />
      </SidebarContent>
      <SidebarRail />
    </Sidebar>
  )
}
