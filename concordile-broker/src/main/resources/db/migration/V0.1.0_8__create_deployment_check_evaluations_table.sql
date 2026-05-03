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

create table if not exists deployment_check_evaluations
(
    id                   uuid primary key not null default gen_random_uuid(),
    version              bigint           not null default 0,
    created_at           timestamptz      not null default now(),
    modified_at          timestamptz      not null default now(),
    deleted_at           timestamptz      null,
    check_id             uuid             not null references deployment_checks (id),
    party_id             uuid             not null references applications (id),
    party_version        text             not null,
    counterparty_id      uuid             not null references applications (id),
    counterparty_version text             not null,
    status               text             not null,
    reason               text             not null,
    context              jsonb            not null default '{}'::jsonb
);
