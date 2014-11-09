/*
 * Copyright 2015 the original author or authors.
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

package org.gradle.api.internal.classpath;

import org.gradle.api.internal.GradleDistributionLocator;
import org.gradle.internal.classloader.ClasspathUtil;

import java.io.File;
import java.io.FileFilter;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

public class DefaultGradleDistributionLocator implements GradleDistributionLocator {
    private final File distDir;
    private final List<File> libDirs = new ArrayList<File>();

    public DefaultGradleDistributionLocator() {
        this(DefaultGradleDistributionLocator.class);
    }

    public DefaultGradleDistributionLocator(Class<?> clazz) {
        this(findDistDir(clazz));
    }

    public DefaultGradleDistributionLocator(File distDir) {
        this.distDir = distDir;

        if (distDir != null) {
            libDirs.addAll(findLibDirs(distDir));
        }
    }

    private List<File> findLibDirs(File distDir) {
        List<File> libDirAndSubdirs = new ArrayList<File>();
        collectWithSubdirectories(new File(distDir, "lib"), libDirAndSubdirs);
        return libDirAndSubdirs;
    }

    private void collectWithSubdirectories(File root, Collection<File> collection) {
        collection.add(root);
        File[] subDirs = root.listFiles(DIRECTORY_FILTER);

        if(subDirs != null) {
            for (File subdirectory : subDirs) {
                collectWithSubdirectories(subdirectory, collection);
            }
        }
    }

    public static final FileFilter DIRECTORY_FILTER = new FileFilter() {
        public boolean accept(File pathname) {
            return pathname.isDirectory();
        }
    };

    private static File findDistDir(Class<?> clazz) {
        return new File("/usr/share/gradle");
    }

    /**
     * Returns the root directory of a distribution based on the code source of a JAR file. The JAR can either sit in the lib or plugins subdirectory. Returns null if distribution doesn't have
     * expected directory layout.
     *
     * The expected directory layout for JARs of a distribution looks as such:
     *
     * dist-root
     *    |_ lib
     *       |_ plugins
     *
     * @param codeSource Code source of JAR file
     * @return Distribution root directory
     */
    private static File determineDistRootDir(File codeSource) {
        File parentDir = codeSource.getParentFile();

        if(parentDir.getName().equals("lib")) {
            File pluginsDir = new File(parentDir, "plugins");
            return parentDir.isDirectory() && pluginsDir.exists() && pluginsDir.isDirectory() ? parentDir.getParentFile() : null;
        }

        if(parentDir.getName().equals("plugins")) {
            File libDir = parentDir.getParentFile();
            return parentDir.isDirectory() && libDir.exists() && libDir.isDirectory() && libDir.getName().equals("lib") ? libDir.getParentFile() : null;
        }

        return null;
    }

    public File getGradleHome() {
        return distDir;
    }

    public List<File> getLibDirs() {
        return libDirs;
    }
}
