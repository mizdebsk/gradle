/*
 * Copyright 2007-2014 the original author or authors.
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
package org.gradle.api.publication.maven.internal.ant;

import org.apache.tools.ant.Project;
import org.codehaus.plexus.ContainerConfiguration;
import org.codehaus.plexus.DefaultContainerConfiguration;
import org.codehaus.plexus.DefaultPlexusContainer;
import org.codehaus.plexus.PlexusContainer;
import org.codehaus.plexus.PlexusContainerException;
import org.codehaus.plexus.classworlds.ClassWorld;
import org.codehaus.plexus.classworlds.realm.ClassRealm;
import org.eclipse.aether.impl.DefaultServiceLocator;
import org.eclipse.aether.internal.ant.AntRepoSys;
import org.eclipse.aether.internal.ant.tasks.AbstractDistTask;
import org.eclipse.aether.internal.ant.tasks.Deploy;
import org.eclipse.aether.internal.ant.types.RemoteRepository;
import org.eclipse.aether.internal.transport.wagon.PlexusWagonConfigurator;
import org.eclipse.aether.spi.connector.transport.TransporterFactory;
import org.eclipse.aether.transport.wagon.WagonConfigurator;
import org.eclipse.aether.transport.wagon.WagonProvider;
import org.eclipse.aether.transport.wagon.WagonTransporterFactory;
import org.gradle.api.artifacts.Configuration;
import org.gradle.api.artifacts.maven.MavenDeployer;
import org.gradle.api.artifacts.maven.PomFilterContainer;
import org.gradle.api.publication.maven.internal.ArtifactPomContainer;
import org.gradle.logging.LoggingManagerInternal;

import java.io.File;
import java.lang.reflect.Field;
import java.net.MalformedURLException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

public class BaseMavenDeployer extends AbstractMavenResolver implements MavenDeployer {
    private RemoteRepository remoteRepository;

    private RemoteRepository remoteSnapshotRepository;

    private Configuration configuration;

    // todo remove this property once configuration can handle normal file system dependencies
    private List<File> protocolProviderJars = new ArrayList<File>();

    public BaseMavenDeployer(PomFilterContainer pomFilterContainer, ArtifactPomContainer artifactPomContainer, LoggingManagerInternal loggingManager) {
        super(pomFilterContainer, artifactPomContainer, loggingManager);
    }

    protected AbstractDistTask createPreConfiguredTask(Project project) {
        configureAetherRepoSys(project);
        Deploy deployTask = createTask();
        deployTask.setProject(project);
        addRemoteRepositories(deployTask);
        return deployTask;
    }

    private void configureAetherRepoSys(Project project) {
        PlexusContainer container = createContainer();

        try {
            AntRepoSys repoSys = AntRepoSys.getInstance(project);
            Field field = repoSys.getClass().getDeclaredField("locator");
            field.setAccessible(true);
            DefaultServiceLocator locator = (DefaultServiceLocator) field.get(repoSys);
            locator.addService(TransporterFactory.class, WagonTransporterFactory.class);
            locator.addService(WagonProvider.class, GradleWagonProvider.class);
            locator.addService(WagonConfigurator.class, PlexusWagonConfigurator.class);

            GradleWagonProvider wagonProvider = (GradleWagonProvider) locator.getService(WagonProvider.class);
            wagonProvider.initService(container);
        } catch (ReflectiveOperationException e) {
            throw new RuntimeException(e);
        }
    }

    private PlexusContainer createContainer() {
        try {
            ClassLoader classLoader = Thread.currentThread().getContextClassLoader();
            ClassWorld world = new ClassWorld("plexus.core", classLoader);
            ClassRealm realm = new ClassRealm(world, "plexus.core", classLoader);
            for (File wagonProviderJar : getJars()) {
                realm.addURL(wagonProviderJar.toURI().toURL());
            }

            ContainerConfiguration conf = new DefaultContainerConfiguration();
            conf.setClassWorld(world);
            conf.setRealm(realm);
            conf.setName("plexus.core");

            return new DefaultPlexusContainer(conf);
        } catch (PlexusContainerException e) {
            throw new RuntimeException(e);
        } catch (MalformedURLException e) {
            throw new RuntimeException(e);
        }
    }

    protected Deploy createTask() {
        return new Deploy();
    }

    private List<File> getJars() {
        return configuration != null ? new ArrayList<File>(configuration.resolve()) : protocolProviderJars;
    }

    private void addRemoteRepositories(Deploy deployTask) {
        Project project = deployTask.getProject();
        if (remoteRepository != null) {
            remoteRepository.setProject(project);
        }
        if (remoteSnapshotRepository != null) {
            remoteSnapshotRepository.setProject(project);
        }
        deployTask.addRemoteRepo(remoteRepository);
        deployTask.addSnapshotRepo(remoteSnapshotRepository);
    }

    public RemoteRepository getRepository() {
        return remoteRepository;
    }

    public void setRepository(Object remoteRepository) {
        this.remoteRepository = (RemoteRepository) remoteRepository;
    }

    public RemoteRepository getSnapshotRepository() {
        return remoteSnapshotRepository;
    }

    public void setSnapshotRepository(Object remoteSnapshotRepository) {
        this.remoteSnapshotRepository = (RemoteRepository) remoteSnapshotRepository;
    }

    public void addProtocolProviderJars(Collection<File> jars) {
        protocolProviderJars.addAll(jars);
    }

    public Configuration getConfiguration() {
        return configuration;
    }

    public void setConfiguration(Configuration configuration) {
        this.configuration = configuration;
    }

    public boolean isUniqueVersion() {
        return true;
    }

    public void setUniqueVersion(boolean uniqueVersion) {
        if (!uniqueVersion) {
            throw new IllegalArgumentException("Non-unique snapshot versions are not supported by this version of Gradle");
        }
    }
}
