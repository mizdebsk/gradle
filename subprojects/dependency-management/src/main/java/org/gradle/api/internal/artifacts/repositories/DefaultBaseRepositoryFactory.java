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

package org.gradle.api.internal.artifacts.repositories;

import org.gradle.api.artifacts.dsl.RepositoryHandler;
import org.gradle.api.artifacts.repositories.ArtifactRepository;
import org.gradle.api.artifacts.repositories.AuthenticationContainer;
import org.gradle.api.artifacts.repositories.FlatDirectoryArtifactRepository;
import org.gradle.api.artifacts.repositories.IvyArtifactRepository;
import org.gradle.api.artifacts.repositories.MavenArtifactRepository;
import org.gradle.api.internal.InstantiatorFactory;
import org.gradle.api.internal.artifacts.BaseRepositoryFactory;
import org.gradle.api.internal.artifacts.ImmutableModuleIdentifierFactory;
import org.gradle.api.internal.artifacts.dsl.DefaultRepositoryHandler;
import org.gradle.api.internal.artifacts.ivyservice.IvyContextManager;
import org.gradle.api.internal.artifacts.ivyservice.ivyresolve.parser.MetaDataParser;
import org.gradle.api.internal.artifacts.ivyservice.ivyresolve.parser.ModuleMetadataParser;
import org.gradle.api.internal.artifacts.mvnsettings.LocalMavenRepositoryLocator;
import org.gradle.api.internal.artifacts.repositories.transport.RepositoryTransportFactory;
import org.gradle.api.internal.file.FileResolver;
import org.gradle.authentication.Authentication;
import org.gradle.internal.authentication.AuthenticationSchemeRegistry;
import org.gradle.internal.authentication.DefaultAuthenticationContainer;
import org.gradle.internal.component.external.model.ModuleComponentArtifactIdentifier;
import org.gradle.internal.component.external.model.ModuleComponentArtifactMetadata;
import org.gradle.internal.component.external.model.MutableMavenModuleResolveMetadata;
import org.gradle.internal.reflect.Instantiator;
import org.gradle.internal.resource.local.FileResourceRepository;
import org.gradle.internal.resource.local.FileStore;
import org.gradle.internal.resource.local.LocallyAvailableResourceFinder;

import java.io.File;
import java.lang.reflect.Method;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.Arrays;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;

public class DefaultBaseRepositoryFactory implements BaseRepositoryFactory {
    private final LocalMavenRepositoryLocator localMavenRepositoryLocator;
    private final FileResolver fileResolver;
    private final Instantiator instantiator;
    private final RepositoryTransportFactory transportFactory;
    private final LocallyAvailableResourceFinder<ModuleComponentArtifactMetadata> locallyAvailableResourceFinder;
    private final FileStore<ModuleComponentArtifactIdentifier> artifactFileStore;
    private final FileStore<String> externalResourcesFileStore;
    private final MetaDataParser<MutableMavenModuleResolveMetadata> pomParser;
    private final ModuleMetadataParser metadataParser;
    private final AuthenticationSchemeRegistry authenticationSchemeRegistry;
    private final IvyContextManager ivyContextManager;
    private final ImmutableModuleIdentifierFactory moduleIdentifierFactory;
    private final InstantiatorFactory instantiatorFactory;
    private final FileResourceRepository fileResourceRepository;

    public DefaultBaseRepositoryFactory(LocalMavenRepositoryLocator localMavenRepositoryLocator,
                                        FileResolver fileResolver,
                                        RepositoryTransportFactory transportFactory,
                                        LocallyAvailableResourceFinder<ModuleComponentArtifactMetadata> locallyAvailableResourceFinder,
                                        FileStore<ModuleComponentArtifactIdentifier> artifactFileStore,
                                        FileStore<String> externalResourcesFileStore,
                                        MetaDataParser<MutableMavenModuleResolveMetadata> pomParser,
                                        ModuleMetadataParser metadataParser,
                                        AuthenticationSchemeRegistry authenticationSchemeRegistry,
                                        IvyContextManager ivyContextManager,
                                        ImmutableModuleIdentifierFactory moduleIdentifierFactory,
                                        InstantiatorFactory instantiatorFactory,
                                        FileResourceRepository fileResourceRepository) {
        this.localMavenRepositoryLocator = localMavenRepositoryLocator;
        this.fileResolver = fileResolver;
        this.metadataParser = metadataParser;
        this.instantiator = instantiatorFactory.decorate();
        this.transportFactory = transportFactory;
        this.locallyAvailableResourceFinder = locallyAvailableResourceFinder;
        this.artifactFileStore = artifactFileStore;
        this.externalResourcesFileStore = externalResourcesFileStore;
        this.pomParser = pomParser;
        this.authenticationSchemeRegistry = authenticationSchemeRegistry;
        this.ivyContextManager = ivyContextManager;
        this.moduleIdentifierFactory = moduleIdentifierFactory;
        this.instantiatorFactory = instantiatorFactory;
        this.fileResourceRepository = fileResourceRepository;
    }

    public FlatDirectoryArtifactRepository createFlatDirRepository() {
        return instantiator.newInstance(DefaultFlatDirArtifactRepository.class, fileResolver, transportFactory, locallyAvailableResourceFinder, artifactFileStore, ivyContextManager, moduleIdentifierFactory, fileResourceRepository);
    }

    public MavenArtifactRepository createMavenLocalRepository() {
        MavenArtifactRepository mavenRepository = instantiator.newInstance(DefaultMavenLocalArtifactRepository.class, fileResolver, transportFactory, locallyAvailableResourceFinder, instantiator, artifactFileStore, pomParser, metadataParser, createAuthenticationContainer(), moduleIdentifierFactory, fileResourceRepository);
        File localMavenRepository = localMavenRepositoryLocator.getLocalMavenRepository();
        mavenRepository.setUrl(localMavenRepository);
        return mavenRepository;
    }

    public MavenArtifactRepository createJCenterRepository() {
        MavenArtifactRepository mavenRepository = createMavenRepository();
        mavenRepository.setUrl(DefaultRepositoryHandler.BINTRAY_JCENTER_URL);
        return mavenRepository;
    }

    public MavenArtifactRepository createMavenCentralRepository() {
        MavenArtifactRepository mavenRepository = createMavenRepository();
        mavenRepository.setUrl(RepositoryHandler.MAVEN_CENTRAL_URL);
        return mavenRepository;
    }

    public MavenArtifactRepository createGoogleRepository() {
        MavenArtifactRepository mavenRepository = createMavenRepository();
        mavenRepository.setUrl(RepositoryHandler.GOOGLE_URL);
        return mavenRepository;
    }

    public IvyArtifactRepository createIvyRepository() {
        return instantiator.newInstance(DefaultIvyArtifactRepository.class, fileResolver, transportFactory, locallyAvailableResourceFinder, artifactFileStore, externalResourcesFileStore, createAuthenticationContainer(), ivyContextManager, moduleIdentifierFactory, instantiatorFactory, fileResourceRepository);
    }

    public MavenArtifactRepository createMavenRepository() {
        return instantiator.newInstance(DefaultMavenArtifactRepository.class, fileResolver, transportFactory, locallyAvailableResourceFinder, instantiator, artifactFileStore, pomParser, metadataParser, createAuthenticationContainer(), moduleIdentifierFactory, externalResourcesFileStore, fileResourceRepository);
    }

    protected AuthenticationContainer createAuthenticationContainer() {
        DefaultAuthenticationContainer container = instantiator.newInstance(DefaultAuthenticationContainer.class, instantiator);

        for (Map.Entry<Class<Authentication>, Class<? extends Authentication>> e : authenticationSchemeRegistry.getRegisteredSchemes().entrySet()) {
            container.registerBinding(e.getKey(), e.getValue());
        }

        return container;
    }

    public ArtifactRepository createXMvnResolver() {
        // Check if XMvn connector is available and inform user if it's not.
        // This is more user-friendly as it prevents cryptic stack traces.
        if (!new File("/usr/share/java/xmvn/xmvn-connector-gradle.jar").exists())
            throw new RuntimeException("Local mode for Gradle is not available because XMvn Gradle connector is not installed. "
                                       + "To use local mode you need to install gradle-local package.");

        // XMvn connector for Gradle is an external library and it is not
        // included in default Gradle classpath. Before it can be accessed
        // we need to add its implementation JARs to current class loader.
        /*
        try {
            ClassLoader classLoader = getClass().getClassLoader();
            Set<URL> newUrls = new LinkedHashSet<URL>();
            newUrls.add(new File("/usr/share/java/xmvn/xmvn-api.jar").toURI().toURL());
            newUrls.add(new File("/usr/share/java/xmvn/xmvn-launcher.jar").toURI().toURL());
            newUrls.add(new File("/usr/share/java/xmvn/xmvn-connector-gradle.jar").toURI().toURL());
            Method getterMethod = classLoader.getClass().getMethod("getURLs");
            Object[] currentUrls = (Object[]) getterMethod.invoke(classLoader);
            newUrls.removeAll(Arrays.asList(currentUrls));
            Method adderMethod = classLoader.getClass().getMethod("addURLs", Iterable.class);
            adderMethod.invoke(classLoader, newUrls);
        } catch (ReflectiveOperationException e) {
            throw new RuntimeException("Unable to inject XMvn JARs to Gradle class loader", e);
        } catch (MalformedURLException e) {
            // Should not happen
            throw new RuntimeException(e);
        }
        */
        Class xmvnClass;

        try {
            URL[] newUrls = new URL[] {
                new File("/usr/share/java/xmvn/xmvn-api.jar").toURI().toURL(),
                new File("/usr/share/java/xmvn/xmvn-core.jar").toURI().toURL(),
                new File("/usr/share/java/xmvn/xmvn-connector-gradle.jar").toURI().toURL()
            };
            ClassLoader classLoader = new URLClassLoader(newUrls, getClass().getClassLoader());
            String xmvnConnectorRole = "org.fedoraproject.xmvn.connector.gradle.GradleResolver";
            xmvnClass = classLoader.loadClass(xmvnConnectorRole);
        } catch (ClassNotFoundException e) {
            throw new RuntimeException("Unable to inject XMvn JARs to Gradle class loader", e);
        } catch (MalformedURLException e) {
            // Should not happen
            throw new RuntimeException(e);
        }

        try {
            return (ArtifactRepository) xmvnClass.getConstructor(MetaDataParser.class, ImmutableModuleIdentifierFactory.class, FileResourceRepository.class)
                .newInstance(pomParser, moduleIdentifierFactory, fileResourceRepository);
        } catch (ReflectiveOperationException e) {
            throw new RuntimeException("Failed to inject XMvn resolver", e);
        }
    }
}
