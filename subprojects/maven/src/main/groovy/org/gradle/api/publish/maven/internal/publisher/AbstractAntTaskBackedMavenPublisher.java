/*
 * Copyright 2013 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.gradle.api.publish.maven.internal.publisher;

import org.apache.tools.ant.Project;
import org.eclipse.aether.internal.ant.tasks.AbstractDistTask;
import org.eclipse.aether.internal.ant.types.Artifact;
import org.eclipse.aether.internal.ant.types.Pom;
import org.gradle.api.artifacts.repositories.MavenArtifactRepository;
import org.gradle.api.logging.LogLevel;
import org.gradle.api.publication.maven.internal.ant.EmptyMavenSettingsSupplier;
import org.gradle.api.publication.maven.internal.ant.MavenSettingsSupplier;
import org.gradle.api.publish.maven.MavenArtifact;
import org.gradle.internal.Factory;
import org.gradle.logging.LoggingManagerInternal;
import org.gradle.util.AntUtil;
import org.gradle.util.GUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;

abstract public class AbstractAntTaskBackedMavenPublisher<T extends AbstractDistTask> implements MavenPublisher {
    private final Factory<LoggingManagerInternal> loggingManagerFactory;

    private static Logger logger = LoggerFactory.getLogger(AbstractAntTaskBackedMavenPublisher.class);
    protected final Factory<File> temporaryDirFactory;

    public AbstractAntTaskBackedMavenPublisher(Factory<LoggingManagerInternal> loggingManagerFactory, Factory<File> temporaryDirFactory) {
        this.loggingManagerFactory = loggingManagerFactory;
        this.temporaryDirFactory = temporaryDirFactory;
    }

    public void publish(MavenNormalizedPublication publication, MavenArtifactRepository artifactRepository) {
        logger.info("Publishing to repository {}", artifactRepository);
        Project project = AntUtil.createProject();
        T deployTask = createDeployTask();
        deployTask.setProject(project);

        MavenSettingsSupplier mavenSettingsSupplier = new EmptyMavenSettingsSupplier();
        mavenSettingsSupplier.supply(project);

        postConfigure(deployTask, artifactRepository);
        addPomAndArtifacts(deployTask, publication);
        execute(deployTask);

        mavenSettingsSupplier.done();
    }

    abstract protected void postConfigure(T task, MavenArtifactRepository artifactRepository);

    abstract protected T createDeployTask();

    private void addPomAndArtifacts(AbstractDistTask task, MavenNormalizedPublication publication) {
        Pom pom = new Pom();
        pom.setProject(task.getProject());
        pom.setFile(publication.getPomFile());
        task.addPom(pom);

        for (MavenArtifact mavenArtifact : publication.getArtifacts()) {
            Artifact artifact = new Artifact();
            artifact.setProject(task.getProject());
            artifact.setClassifier(GUtil.elvis(mavenArtifact.getClassifier(), ""));
            artifact.setType(GUtil.elvis(mavenArtifact.getExtension(), ""));
            artifact.setFile(mavenArtifact.getFile());
            task.addArtifact(artifact);
        }
    }

    private void execute(AbstractDistTask deployTask) {
        LoggingManagerInternal loggingManager = loggingManagerFactory.create();
        loggingManager.captureStandardOutput(LogLevel.INFO).start();
        try {
            deployTask.execute();
        } finally {
            loggingManager.stop();
        }
    }

}
