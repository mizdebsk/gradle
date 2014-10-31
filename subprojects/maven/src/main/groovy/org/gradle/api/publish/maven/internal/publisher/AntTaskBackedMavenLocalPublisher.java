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

import org.eclipse.aether.internal.ant.AntRepoSys;
import org.eclipse.aether.internal.ant.tasks.Install;
import org.eclipse.aether.internal.ant.types.LocalRepository;
import org.gradle.api.artifacts.repositories.MavenArtifactRepository;
import org.gradle.internal.Factory;
import org.gradle.logging.LoggingManagerInternal;

import java.io.File;

public class AntTaskBackedMavenLocalPublisher extends AbstractAntTaskBackedMavenPublisher<Install> {
    public AntTaskBackedMavenLocalPublisher(Factory<LoggingManagerInternal> loggingManagerFactory, Factory<File> temporaryDirFactory) {
        super(loggingManagerFactory, temporaryDirFactory);
    }

    @Override
    protected void postConfigure(Install task, MavenArtifactRepository artifactRepository) {
        LocalRepository localRepository = new LocalRepository(task);
        localRepository.setDir(new File(artifactRepository.getUrl().getPath()));
        AntRepoSys repoSys = AntRepoSys.getInstance(task.getProject());
        repoSys.setLocalRepository(localRepository);
    }

    @Override
    protected Install createDeployTask() {
        return new Install();
    }
}
