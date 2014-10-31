/*
 * Copyright 2012 the original author or authors.
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

import org.eclipse.aether.internal.ant.AntRepoSys;
import org.eclipse.aether.internal.ant.tasks.Deploy;
import org.eclipse.aether.internal.ant.types.LocalRepository;
import org.eclipse.aether.internal.ant.types.RemoteRepository;
import org.gradle.api.artifacts.repositories.MavenArtifactRepository;
import org.gradle.internal.Factory;
import org.gradle.logging.LoggingManagerInternal;

import java.io.File;

public class AntTaskBackedMavenPublisher extends AbstractAntTaskBackedMavenPublisher<Deploy> {
    public AntTaskBackedMavenPublisher(Factory<LoggingManagerInternal> loggingManagerFactory, Factory<File> temporaryDirFactory) {
        super(loggingManagerFactory, temporaryDirFactory);
    }

    protected void postConfigure(Deploy task, MavenArtifactRepository artifactRepository) {
        addRepository(task, artifactRepository);
    }

    protected Deploy createDeployTask() {
        Deploy task = new Deploy();
        LocalRepository localRepository = new LocalRepository(task);
        localRepository.setDir(temporaryDirFactory.create());
        AntRepoSys repoSys = AntRepoSys.getInstance(task.getProject());
        repoSys.setLocalRepository(localRepository);
        return task;
    }

    private void addRepository(Deploy deployTask, MavenArtifactRepository artifactRepository) {
        RemoteRepository mavenRepository = new MavenRemoteRepositoryFactory(artifactRepository).create();
        deployTask.addRemoteRepo(mavenRepository);
    }
}
