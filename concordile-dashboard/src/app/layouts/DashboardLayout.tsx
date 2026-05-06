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

import { Outlet } from 'react-router-dom'

import { dashboardSidebarNavItems } from '@/shared/config/dashboard-sidebar'
import { AppSidebar } from '@/shared/ui/app-sidebar'
import { SidebarInset, SidebarProvider } from '@/shared/ui/kit/sidebar'
import { SiteHeader } from '@/shared/ui/site-header'

export function DashboardLayout() {
  return (
    <div className="flex min-h-svh flex-col">
      <SidebarProvider className="flex min-h-0 flex-1 flex-col">
        <SiteHeader />
        <div className="flex min-h-0 flex-1">
          <AppSidebar navItems={dashboardSidebarNavItems} />
          <SidebarInset className="flex min-h-0 min-w-0 flex-1 flex-col overflow-hidden">
            <div className="flex min-h-0 flex-1 flex-col gap-4 overflow-y-auto p-4">
              <Outlet />
            </div>
          </SidebarInset>
        </div>
      </SidebarProvider>
    </div>
  )
}
