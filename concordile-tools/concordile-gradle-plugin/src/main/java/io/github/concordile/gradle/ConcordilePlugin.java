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

package io.github.concordile.gradle;

import org.gradle.api.Plugin;
import org.gradle.api.Project;

public class ConcordilePlugin implements Plugin<Project> {

    public static final String PLUGIN_ID = "io.github.concordile";
    public static final String SPRING_CLOUD_CONTRACT_PLUGIN_ID = "org.springframework.cloud.contract";
    public static final String EXTENSION_GAV = "io.github.concordile.spring:concordile-spring-cloud-contract-extension:0.1.0-SNAPSHOT";

    public void apply(Project project) {
        project.getPluginManager().withPlugin(SPRING_CLOUD_CONTRACT_PLUGIN_ID, plugin -> {
            project.getDependencies().add("contractTestImplementation", EXTENSION_GAV);
        });
    }

}
