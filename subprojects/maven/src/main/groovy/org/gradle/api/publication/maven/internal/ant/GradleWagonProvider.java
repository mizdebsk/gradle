/*
 * Copyright 2014 the original author or authors.
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

import org.apache.maven.wagon.Wagon;
import org.codehaus.plexus.PlexusContainer;
import org.eclipse.aether.internal.transport.wagon.PlexusWagonProvider;
import org.eclipse.aether.transport.wagon.WagonProvider;

public class GradleWagonProvider implements WagonProvider {

    private WagonProvider delegate;

    public void initService(PlexusContainer container) {
        delegate = new PlexusWagonProvider(container);
    }

    public Wagon lookup(String roleHint) throws Exception {
        return delegate.lookup(roleHint);
    }

    public void release(Wagon wagon) {
        delegate.release(wagon);
    }
}
