/*
 * Copyright 2011 the original author or authors.
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

import org.apache.commons.io.FileUtils;
import org.apache.tools.ant.Project;
import org.eclipse.aether.internal.ant.AntRepoSys;
import org.gradle.api.UncheckedIOException;
import org.gradle.api.internal.file.TemporaryFileProvider;
import org.gradle.api.internal.file.TmpDirTemporaryFileProvider;

import java.io.File;
import java.io.IOException;

public class EmptyMavenSettingsSupplier implements MavenSettingsSupplier {

    private final TemporaryFileProvider temporaryFileProvider = new TmpDirTemporaryFileProvider();
    private File settingsXml;

    public void supply(Project project) {
        try {
            settingsXml = temporaryFileProvider.createTemporaryFile("gradle_empty_settings", ".xml");
            FileUtils.writeStringToFile(settingsXml, "<settings/>");
            settingsXml.deleteOnExit();
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
        AntRepoSys repoSys = AntRepoSys.getInstance(project);
        repoSys.setGlobalSettings(settingsXml);
        repoSys.setUserSettings(settingsXml);
    }

    public void done() {
        if (settingsXml != null) {
            settingsXml.delete();
        }
    }
}
