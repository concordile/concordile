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

import { Link, Outlet } from 'react-router-dom'

import { routes } from '@/shared/config/routes'

export function DashboardLayout() {
  return (
    <div>
      <aside>
        <strong>Concordile</strong>

        <nav>
          <Link to={routes.dashboard}>Dashboard</Link>
          <Link to={routes.applications}>Applications</Link>
          <Link to={routes.verifications}>Verifications</Link>
          <Link to={routes.admin}>Admin</Link>
        </nav>
      </aside>

      <section>
        <header>
          <span>Dashboard skeleton</span>
        </header>

        <main>
          <Outlet />
        </main>
      </section>
    </div>
  )
}
