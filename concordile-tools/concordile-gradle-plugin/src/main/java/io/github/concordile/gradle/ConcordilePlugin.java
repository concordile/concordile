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

import io.github.concordile.gradle.config.ConsumerVerificationConfigurer;
import io.github.concordile.gradle.config.ProviderVerificationConfigurer;
import io.github.concordile.gradle.extension.ConcordileExtension;
import org.gradle.api.Plugin;
import org.gradle.api.Project;

import static io.github.concordile.gradle.ConcordilePluginConstants.EXTENSION_NAME;

@SuppressWarnings("unused")
public class ConcordilePlugin implements Plugin<Project> {

    @Override
    public void apply(Project project) {
        var extension = project.getExtensions().create(
                EXTENSION_NAME,
                ConcordileExtension.class,
                project
        );

        new ConsumerVerificationConfigurer(project, extension).configure();
        new ProviderVerificationConfigurer(project, extension).configure();
    }

}
