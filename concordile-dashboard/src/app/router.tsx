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

import { createBrowserRouter } from 'react-router-dom'

import { AuthLayout, DashboardLayout } from '@/app/layouts'
import { AdminPage } from '@/modules/admin/pages'
import {
  ApplicationDetailsPage,
  ApplicationsPage,
} from '@/modules/applications/pages'
import { LoginPage } from '@/modules/auth/pages'
import { DashboardPage } from '@/modules/dashboard/pages'
import { NotFoundPage } from '@/modules/errors/pages'
import { VerificationsPage } from '@/modules/verifications/pages'
import { routes } from '@/shared/config/routes'

export const router = createBrowserRouter([
  {
    element: <AuthLayout />,
    children: [
      {
        path: routes.login,
        element: <LoginPage />,
      },
    ],
  },
  {
    element: <DashboardLayout />,
    children: [
      {
        path: routes.dashboard,
        element: <DashboardPage />,
      },
      {
        path: routes.applications,
        element: <ApplicationsPage />,
      },
      {
        path: routes.applicationDetails(':applicationId'),
        element: <ApplicationDetailsPage />,
      },
      {
        path: routes.verifications,
        element: <VerificationsPage />,
      },
      {
        path: routes.admin,
        element: <AdminPage />,
      },
      {
        path: '*',
        element: <NotFoundPage />,
      },
    ],
  },
])
