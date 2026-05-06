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

export const routes = {
  login: '/login',

  overview: '/',

  applications: '/applications',
  applicationDetails: (applicationId: string) =>
    `/applications/${applicationId}`,

  contracts: '/contracts',
  contractDetails: (contractId: string) => `/contracts/${contractId}`,

  verifications: '/verifications',
  verificationDetails: (verificationId: string) =>
    `/verifications/${verificationId}`,

  deployments: '/deployments',
  deploymentDetails: (deploymentId: string) => `/deployments/${deploymentId}`,
  deploymentTargets: `/deployments/targets`,
  deploymentChecks: `/deployments/checks`,
} as const
