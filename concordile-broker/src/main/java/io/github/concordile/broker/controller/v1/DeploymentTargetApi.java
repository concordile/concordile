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

import io.github.concordile.broker.api.v1.CreateDeploymentTargetRequest;
import io.github.concordile.broker.api.v1.DeploymentTargetResponse;
import io.github.concordile.broker.controller.annotation.Json200ApiResponse;
import io.github.concordile.broker.controller.annotation.Json201ApiResponse;
import io.github.concordile.broker.controller.annotation.Problem400ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springdoc.core.annotations.ParameterObject;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.data.web.PagedModel;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;

@Tag(name = "deployments")
@RequestMapping("/api/v1/deployments/targets")
public interface DeploymentTargetApi {

    @Json200ApiResponse
    @Problem400ApiResponse
    @GetMapping
    ResponseEntity<PagedModel<DeploymentTargetResponse>> searchDeploymentTargets(
            @ParameterObject @PageableDefault(size = 20)
            Pageable pageable
    );

    @Json201ApiResponse
    @Problem400ApiResponse
    @PostMapping
    ResponseEntity<DeploymentTargetResponse> createDeploymentTarget(
            @Valid @RequestBody CreateDeploymentTargetRequest request
    );

}
