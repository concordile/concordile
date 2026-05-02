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

package io.github.concordile.gradle.extension;

import org.gradle.api.Action;
import org.gradle.api.NamedDomainObjectContainer;
import org.gradle.api.Project;

import javax.inject.Inject;

public abstract class ConcordileExtension {

    private final ConcordileBrokerExtension broker;

    private final ConcordileApplicationExtension application;

    private final NamedDomainObjectContainer<ConcordileConsumerExtension> consumers;

    @Inject
    public ConcordileExtension(Project project) {
        var objects = project.getObjects();

        this.broker = objects.newInstance(ConcordileBrokerExtension.class);
        this.application = objects.newInstance(ConcordileApplicationExtension.class);
        this.consumers = objects.domainObjectContainer(
                ConcordileConsumerExtension.class,
                name -> objects.newInstance(ConcordileConsumerExtension.class, name)
        );

        broker.getUrl().convention("http://localhost:8080");

        application.getGroupId()
                .convention(project.provider(() -> project.getRootProject().getGroup().toString()));

        application.getName()
                .convention(project.getRootProject().getName());

        application.getVersion()
                .convention(project.provider(() -> project.getRootProject().getVersion().toString()));
    }

    public ConcordileBrokerExtension getBroker() {
        return broker;
    }

    @SuppressWarnings("unused")
    public void broker(Action<? super ConcordileBrokerExtension> action) {
        action.execute(broker);
    }

    public ConcordileApplicationExtension getApplication() {
        return application;
    }

    @SuppressWarnings("unused")
    public void application(Action<? super ConcordileApplicationExtension> action) {
        action.execute(application);
    }

    public NamedDomainObjectContainer<ConcordileConsumerExtension> getConsumers() {
        return consumers;
    }

    @SuppressWarnings("unused")
    public void consumers(Action<? super NamedDomainObjectContainer<ConcordileConsumerExtension>> action) {
        action.execute(consumers);
    }

}
