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

package io.github.concordile.gradle.context;

import org.gradle.api.Project;

import java.util.LinkedHashMap;
import java.util.Map;

import static io.github.concordile.gradle.ConcordilePluginConstants.PLUGIN_ID;
import static io.github.concordile.gradle.ConcordilePluginConstants.PLUGIN_VERSION;

public final class VerificationContextFactory {

    public Map<String, Object> create(Project project) {
        var context = new LinkedHashMap<String, Object>();

        context.put("build", build(project));
        context.put("environment", environment());
        context.put("scm", scm());
        context.put("publisher", publisher());

        return context;
    }

    private Map<String, Object> build(Project project) {
        var build = new LinkedHashMap<String, Object>();

        build.put("tool", "gradle");
        build.put("gradleVersion", project.getGradle().getGradleVersion());
        build.put("rootProjectName", project.getRootProject().getName());
        build.put("projectName", project.getName());
        build.put("projectPath", project.getPath());
        build.put("tasks", project.getGradle().getStartParameter().getTaskNames());

        putEnv(build, "buildId", "GITHUB_RUN_ID", "CI_PIPELINE_ID", "BUILD_ID");
        putEnv(build, "buildNumber", "GITHUB_RUN_NUMBER", "CI_PIPELINE_IID", "BUILD_NUMBER");
        putEnv(build, "buildUrl", "GITHUB_SERVER_URL", "BUILD_URL");

        return build;
    }

    private Map<String, Object> environment() {
        var environment = new LinkedHashMap<String, Object>();

        environment.put("ci", Boolean.parseBoolean(System.getenv().getOrDefault("CI", "false")));
        environment.put("javaVersion", System.getProperty("java.version"));
        environment.put("javaVendor", System.getProperty("java.vendor"));
        environment.put("osName", System.getProperty("os.name"));
        environment.put("osVersion", System.getProperty("os.version"));
        environment.put("osArch", System.getProperty("os.arch"));

        return environment;
    }

    private Map<String, Object> scm() {
        var scm = new LinkedHashMap<String, Object>();

        putEnv(scm, "branch", "GITHUB_HEAD_REF", "GITHUB_REF_NAME", "CI_COMMIT_BRANCH", "BRANCH_NAME");
        putEnv(scm, "commitSha", "GITHUB_SHA", "CI_COMMIT_SHA", "GIT_COMMIT");
        putEnv(scm, "repositoryUrl", "GITHUB_REPOSITORY", "CI_PROJECT_URL", "GIT_URL");
        putEnv(scm, "pullRequest", "GITHUB_REF_NAME", "CHANGE_ID");

        return scm;
    }

    private Map<String, Object> publisher() {
        var publisher = new LinkedHashMap<String, Object>();

        publisher.put("name", PLUGIN_ID);
        publisher.put("version", PLUGIN_VERSION);

        return publisher;
    }

    private void putEnv(
            Map<String, Object> target,
            String key,
            String... names
    ) {
        for (var name : names) {
            var value = System.getenv(name);

            if (value != null && !value.isBlank()) {
                target.put(key, value);
                return;
            }
        }
    }

}
