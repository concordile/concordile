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

import { HouseIcon, RowsIcon, SealCheckIcon } from '@phosphor-icons/react'

import { routes } from '@/shared/config/routes'
import type { NavMainItem } from '@/shared/ui/nav-main'

export const dashboardSidebarNavItems: NavMainItem[] = [
  {
    title: 'Overview',
    url: routes.overview,
    icon: <HouseIcon />,
  },
  {
    title: 'Applications',
    url: routes.applications,
    icon: <RowsIcon />,
  },
  {
    title: 'Verifications',
    url: routes.verifications,
    icon: <SealCheckIcon />,
  },
]
