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

package io.github.concordile.broker.repository;

import io.github.concordile.broker.domain.ApplicationFilters;
import io.github.concordile.broker.domain.ApplicationItemView;
import io.github.concordile.broker.domain.DeploymentCheckStatus;
import io.github.concordile.broker.domain.VerificationStatus;
import lombok.RequiredArgsConstructor;
import org.jspecify.annotations.Nullable;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.jdbc.core.simple.JdbcClient;
import org.springframework.stereotype.Repository;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.sql.Types;
import java.util.ArrayList;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.UUID;

@Repository
@RequiredArgsConstructor
public class JdbcApplicationQueryRepository implements ApplicationQueryRepository {

    private static final Map<String, String> SORT_COLUMNS = Map.of(
            "name", "a.name",
            "groupId", "a.group_id",
            "providedContracts", "provided_contracts",
            "consumedContracts", "consumed_contracts",
            "latestVerificationStatus", "latest_verification_status",
            "latestDeploymentCheckStatus", "latest_deployment_check_status",
            "lastActivity", "last_activity"
    );

    private static final String LATEST_STATUS_CTE = """
            latest_verifications as (
                select application_id, status
                from (
                    select
                        v.party_id as application_id,
                        v.status,
                        row_number() over (
                            partition by v.party_id
                            order by v.created_at desc, v.id desc
                        ) as row_number
                    from verifications v
                    where v.deleted_at is null
                ) latest
                where latest.row_number = 1
            ),
            latest_deployment_checks as (
                select application_id, status
                from (
                    select
                        dc.app_id as application_id,
                        dc.status,
                        row_number() over (
                            partition by dc.app_id
                            order by dc.created_at desc, dc.id desc
                        ) as row_number
                    from deployment_checks dc
                    where dc.deleted_at is null
                ) latest
                where latest.row_number = 1
            )
            """;

    private static final String APPLICATION_FILTERS_SQL = """
            and (
                :query is null
                or lower(a.name) like :query
                or lower(a.group_id) like :query
            )
            and (
                :verificationStatus is null
                or lv.status = :verificationStatus
            )
            and (
                :deploymentCheckStatus is null
                or ldc.status = :deploymentCheckStatus
            )
            """;

    private final JdbcClient jdbcClient;

    private static JdbcClient.StatementSpec bindFilters(
            JdbcClient.StatementSpec spec,
            ApplicationFilters filters
    ) {
        return spec
                .param("query", queryLike(filters.query()), Types.VARCHAR)
                .param("verificationStatus", filters.verificationStatus(), Types.VARCHAR)
                .param("deploymentCheckStatus", filters.deploymentCheckStatus(), Types.VARCHAR);
    }

    private static String findApplicationsSql(Sort sort) {
        // language=PostgreSQL
        return """
                with contract_counts as (
                    select
                        a.id as application_id,
                        count(distinct provided_contracts.id) as provided_contracts,
                        count(distinct consumed_contracts.id) as consumed_contracts
                    from applications a
                    left join contracts provided_contracts
                        on provided_contracts.provider_id = a.id
                        and provided_contracts.deleted_at is null
                    left join contracts consumed_contracts
                        on consumed_contracts.consumer_id = a.id
                        and consumed_contracts.deleted_at is null
                    where a.deleted_at is null
                    group by a.id
                ),
                """ + LATEST_STATUS_CTE + """
                ,
                last_activities as (
                    select
                        a.id as application_id,
                        greatest(
                            a.created_at,
                            a.modified_at,
                            coalesce(max(v.created_at), a.created_at),
                            coalesce(max(dc.created_at), a.created_at),
                            coalesce(max(dr.created_at), a.created_at)
                        ) as last_activity
                    from applications a
                    left join verifications v
                        on v.party_id = a.id
                        and v.deleted_at is null
                    left join deployment_checks dc
                        on dc.app_id = a.id
                        and dc.deleted_at is null
                    left join deployment_records dr
                        on dr.app_id = a.id
                        and dr.deleted_at is null
                    where a.deleted_at is null
                    group by a.id
                )
                select
                    a.id,
                    a.group_id,
                    a.name,
                    coalesce(cc.provided_contracts, 0) as provided_contracts,
                    coalesce(cc.consumed_contracts, 0) as consumed_contracts,
                    lv.status as latest_verification_status,
                    ldc.status as latest_deployment_check_status,
                    la.last_activity
                from applications a
                left join contract_counts cc on cc.application_id = a.id
                left join latest_verifications lv on lv.application_id = a.id
                left join latest_deployment_checks ldc on ldc.application_id = a.id
                left join last_activities la on la.application_id = a.id
                where a.deleted_at is null
                """ + APPLICATION_FILTERS_SQL + orderByClause(sort) + """
                limit :limit offset :offset
                """;
    }

    private static String countApplicationsSql() {
        // language=PostgreSQL
        return """
                with
                """ + LATEST_STATUS_CTE + """
                select count(*)
                from applications a
                left join latest_verifications lv on lv.application_id = a.id
                left join latest_deployment_checks ldc on ldc.application_id = a.id
                where a.deleted_at is null
                """ + APPLICATION_FILTERS_SQL;
    }

    private static String orderByClause(Sort sort) {
        if (sort.isUnsorted()) {
            throw new IllegalArgumentException("Applications query requires sorting");
        }

        var orders = sort.stream()
                .map(JdbcApplicationQueryRepository::toSqlOrder)
                .toList();

        var stableOrders = new ArrayList<>(orders);
        stableOrders.add("a.id ASC");

        return " order by " + String.join(", ", stableOrders) + " ";
    }

    private static String toSqlOrder(Sort.Order order) {
        var column = SORT_COLUMNS.get(order.getProperty());

        if (column == null) {
            throw new IllegalArgumentException("Unsupported application sort property: " + order.getProperty());
        }

        return column + " " + (order.isDescending() ? "DESC" : "ASC");
    }

    private static @Nullable String queryLike(@Nullable String query) {
        if (query == null || query.isBlank()) {
            return null;
        }

        return "%" + query.strip().toLowerCase() + "%";
    }

    @Override
    public Page<ApplicationItemView> findAll(ApplicationFilters filters, Pageable pageable) {
        var content = bindFilters(jdbcClient.sql(findApplicationsSql(pageable.getSort())), filters)
                .param("limit", pageable.getPageSize())
                .param("offset", pageable.getOffset())
                .query(this::mapItemView)
                .list();

        var total = bindFilters(jdbcClient.sql(countApplicationsSql()), filters)
                .query(Long.class)
                .single();

        return new PageImpl<>(content, pageable, total);
    }

    private ApplicationItemView mapItemView(ResultSet rs, int rowNum) throws SQLException {
        return new ApplicationItemView(
                Objects.requireNonNull(rs.getObject("id", UUID.class), "No id"),
                Objects.requireNonNull(rs.getString("group_id"), "No group_id"),
                Objects.requireNonNull(rs.getString("name"), "No name"),
                rs.getLong("provided_contracts"),
                rs.getLong("consumed_contracts"),
                Optional.ofNullable(rs.getString("latest_verification_status"))
                        .map(VerificationStatus::valueOf)
                        .orElse(null),
                Optional.ofNullable(rs.getString("latest_deployment_check_status"))
                        .map(DeploymentCheckStatus::valueOf)
                        .orElse(null),
                Optional.ofNullable(rs.getTimestamp("last_activity"))
                        .map(Timestamp::toInstant)
                        .orElse(null)
        );
    }

}
