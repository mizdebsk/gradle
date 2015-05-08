/*
 * Copyright 2007-2008 the original author or authors.
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
package org.gradle.api.publication.maven.internal.action;

import java.io.File;
import java.util.Collection;

import org.apache.maven.artifact.ant.RemoteRepository;
import org.eclipse.aether.RepositorySystem;
import org.eclipse.aether.RepositorySystemSession;
import org.eclipse.aether.artifact.Artifact;
import org.eclipse.aether.deployment.DeployRequest;
import org.eclipse.aether.deployment.DeploymentException;
import org.eclipse.aether.repository.Authentication;
import org.eclipse.aether.repository.Proxy;
import org.eclipse.aether.repository.RemoteRepository.Builder;
import org.eclipse.aether.util.repository.AuthenticationBuilder;
import org.eclipse.aether.util.repository.DefaultProxySelector;
import org.gradle.api.GradleException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class MavenDeployAction extends AbstractMavenPublishAction {
    private static final Logger LOGGER = LoggerFactory.getLogger(MavenDeployAction.class);

    private RemoteRepository remoteRepository;
    private RemoteRepository remoteSnapshotRepository;
    private SnapshotVersionManager snapshotVersionManager = new SnapshotVersionManager();

    public MavenDeployAction(File pomFile) {
        super(pomFile);
    }

    public void setRepositories(RemoteRepository repository, RemoteRepository snapshotRepository) {
        this.remoteRepository = repository;
        this.remoteSnapshotRepository = snapshotRepository;
    }

    public void setUniqueVersion(boolean uniqueVersion) {
        snapshotVersionManager.setUniqueVersion(uniqueVersion);
    }

    @Override
    protected void publishArtifacts(Collection<Artifact> artifacts, RepositorySystem repositorySystem, RepositorySystemSession session) throws DeploymentException {
        RemoteRepository gradleRepo = remoteRepository;
        if (artifacts.iterator().next().isSnapshot() && remoteSnapshotRepository != null) {
            gradleRepo = remoteSnapshotRepository;
        }
        if (gradleRepo == null) {
            throw new GradleException("Must specify a repository for deployment");
        }

        org.eclipse.aether.repository.RemoteRepository aetherRepo = createRepository(gradleRepo);

        DeployRequest request = new DeployRequest();
        request.setRepository(aetherRepo);
        for (Artifact artifact : artifacts) {
            request.addArtifact(artifact);
        }

        snapshotVersionManager.install(repositorySystem);

        LOGGER.info("Deploying to " + gradleRepo.getUrl());
        repositorySystem.deploy(session, request);
    }

    private org.eclipse.aether.repository.RemoteRepository createRepository(RemoteRepository gradleRepo) {
        Builder repoBuilder = new Builder("remote", gradleRepo.getLayout(), gradleRepo.getUrl());

        org.apache.maven.artifact.ant.Authentication auth = gradleRepo.getAuthentication();
        if (auth != null) {
            AuthenticationBuilder authBuilder = new AuthenticationBuilder();
            authBuilder.addUsername(auth.getUserName()).addPassword(auth.getPassword());
            authBuilder.addPrivateKey(auth.getPrivateKey(), auth.getPassphrase());
            repoBuilder.setAuthentication(authBuilder.build());
        }

        org.apache.maven.artifact.ant.Proxy proxy = gradleRepo.getProxy();
        if (proxy != null) {
            DefaultProxySelector proxySelector = new DefaultProxySelector();
            Authentication proxyAuth = new AuthenticationBuilder().addUsername(proxy.getUserName()).addPassword(proxy.getPassword()).build();
            proxySelector.add(new Proxy(proxy.getType(), proxy.getHost(), proxy.getPort(), proxyAuth), proxy.getNonProxyHosts());
            repoBuilder.setProxy(proxySelector.getProxy(repoBuilder.build()));
        }

        return repoBuilder.build();
    }
}
