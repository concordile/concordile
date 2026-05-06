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

package io.github.concordile.broker.controller.v1;

import io.github.concordile.broker.api.v1.ApplicationItemResponse;
import io.github.concordile.broker.api.v1.DeploymentCheckStatus;
import io.github.concordile.broker.api.v1.VerificationStatus;
import io.github.concordile.broker.domain.ApplicationFilters;
import io.github.concordile.broker.mapper.v1.ApplicationItemResponseMapper;
import io.github.concordile.broker.service.ApplicationService;
import lombok.RequiredArgsConstructor;
import org.jspecify.annotations.Nullable;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PagedModel;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;

import java.util.Optional;

@RestController
@RequiredArgsConstructor
public class ApplicationController implements ApplicationApi {

    private final ApplicationService service;

    private final ApplicationItemResponseMapper itemResponseMapper;

    @Override
    public ResponseEntity<PagedModel<ApplicationItemResponse>> findAllApplications(
            @Nullable String query,
            @Nullable VerificationStatus verificationStatus,
            @Nullable DeploymentCheckStatus deploymentCheckStatus,
            Pageable pageable
    ) {
        var filters = new ApplicationFilters(
                query,
                Optional.ofNullable(verificationStatus)
                        .map(Enum::name)
                        .orElse(null),
                Optional.ofNullable(deploymentCheckStatus)
                        .map(Enum::name)
                        .orElse(null)
        );
        var page = service.findAll(filters, pageable);
        var response = new PagedModel<>(page.map(itemResponseMapper::mapDomain2Response));
        return ResponseEntity.ok(response);
    }

}
