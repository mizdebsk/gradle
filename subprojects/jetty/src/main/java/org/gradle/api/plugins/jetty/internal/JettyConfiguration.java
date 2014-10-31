/*
 * Copyright 2009 the original author or authors.
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

package org.gradle.api.plugins.jetty.internal;

import java.io.File;
import java.net.URLClassLoader;
import java.util.List;
import java.util.Arrays;

import org.eclipse.jetty.webapp.WebAppContext;
import org.eclipse.jetty.webapp.WebAppClassLoader;
import org.eclipse.jetty.webapp.WebXmlConfiguration;
import org.eclipse.jetty.util.log.Log;
import org.eclipse.jetty.util.log.Logger;

public class JettyConfiguration extends WebXmlConfiguration {
    private List<File> classPathFiles;
    private File webXmlFile;

    private static final Logger LOG = Log.getLogger(JettyConfiguration.class);

    public JettyConfiguration() {
        super();
    }

    public void setClassPathConfiguration(List<File> classPathFiles) {
        this.classPathFiles = classPathFiles;
    }

    public void setWebXml(File webXmlFile) {
        this.webXmlFile = webXmlFile;
    }

    /**
     * Set up the classloader for the webapp, using the various parts of the Maven project
     *
     * @see org.eclipse.jetty.webapp.Configuration#configureClassLoader()
     */
    @Override
    public void configure(WebAppContext context) throws Exception {
        if (classPathFiles != null) {
            LOG.debug("Setting up classpath ...");

            //put the classes dir and all dependencies into the classpath
            for (File classPathFile : classPathFiles) {
                ((WebAppClassLoader) context.getClassLoader()).addClassPath(
                        classPathFile.getCanonicalPath());
            }

            if (LOG.isDebugEnabled()) {
                Log.getLog().debug("Classpath = " + Arrays.asList(
                        ((URLClassLoader) context.getClassLoader()).getURLs()));
            }
        } else {
            super.configure(context);
        }
    }
}
