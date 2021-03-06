/*
 * Copyright 2010 the original author or authors.
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
package org.gradle.configuration

import org.gradle.api.artifacts.Configuration
import org.gradle.api.artifacts.ConfigurationContainer
import org.gradle.api.initialization.dsl.ScriptHandler
import org.gradle.api.internal.DocumentationRegistry
import org.gradle.api.internal.file.FileLookup
import org.gradle.api.internal.initialization.ClassLoaderScope
import org.gradle.api.internal.initialization.ScriptHandlerFactory
import org.gradle.api.internal.initialization.ScriptHandlerInternal
import org.gradle.groovy.scripts.*
import org.gradle.groovy.scripts.internal.CompiledScript
import org.gradle.groovy.scripts.internal.FactoryBackedCompileOperation
import org.gradle.internal.Factory
import org.gradle.internal.reflect.Instantiator
import org.gradle.internal.service.ServiceRegistry
import org.gradle.logging.LoggingManagerInternal
import org.gradle.model.dsl.internal.transform.ClosureCreationInterceptingVerifier
import org.gradle.model.internal.inspect.ModelRuleSourceDetector
import org.gradle.plugin.use.internal.PluginRequestApplicator
import spock.lang.Specification

public class DefaultScriptPluginFactoryTest extends Specification {

    def scriptCompilerFactory = Mock(ScriptCompilerFactory)
    def scriptCompiler = Mock(ScriptCompiler)
    def scriptSource = Mock(ScriptSource)
    def scriptRunner = Mock(ScriptRunner)
    def script = Mock(BasicScript)
    def instantiator = Mock(Instantiator)
    def targetScope = Mock(ClassLoaderScope)
    def baseScope = Mock(ClassLoaderScope)
    def scopeClassLoader = Mock(ClassLoader)
    def baseChildClassLoader = Mock(ClassLoader)
    def scriptHandlerFactory = Mock(ScriptHandlerFactory)
    def pluginRequestApplicator = Mock(PluginRequestApplicator)
    def scriptHandler = Mock(ScriptHandlerInternal)
    def classPathScriptRunner = Mock(ScriptRunner)
    def classPathScript = Mock(BasicScript)
    def loggingManagerFactory = Mock(Factory) as Factory<LoggingManagerInternal>
    def loggingManager = Mock(LoggingManagerInternal)
    def fileLookup = Mock(FileLookup)
    def documentationRegistry = Mock(DocumentationRegistry)
    def classpathClosureName = "buildscript"
    def compiledScript = Mock(CompiledScript)
    def classpathCompiledScript = Mock(CompiledScript)

    def factory = new DefaultScriptPluginFactory(scriptCompilerFactory, loggingManagerFactory, instantiator, scriptHandlerFactory, pluginRequestApplicator, fileLookup,
            documentationRegistry, new ModelRuleSourceDetector())

    def setup() {
        def configurations = Mock(ConfigurationContainer)
        scriptHandler.configurations >> configurations
        def configuration = Mock(Configuration)
        configurations.getByName(ScriptHandler.CLASSPATH_CONFIGURATION) >> configuration
        configuration.getFiles() >> Collections.emptySet()
        baseScope.getExportClassLoader() >> baseChildClassLoader

        1 * targetScope.getLocalClassLoader() >> scopeClassLoader
    }

    void configuresATargetObjectUsingScript() {
        when:
        final Object target = new Object()

        1 * loggingManagerFactory.create() >> loggingManager
        1 * scriptCompilerFactory.createCompiler(scriptSource) >> scriptCompiler
        1 * scriptCompiler.compile(DefaultScript, _ as FactoryBackedCompileOperation, baseChildClassLoader, classpathClosureName, _) >> classPathScriptRunner
        1 * classPathScriptRunner.getScript() >> classPathScript
        1 * classPathScript.init(target, _ as ServiceRegistry)
        1 * classPathScriptRunner.run()
        1 * classPathScriptRunner.getCompiledScript() >> classpathCompiledScript
        1 * scriptCompiler.compile(DefaultScript, { it.transformer != null }, scopeClassLoader, classpathClosureName, ClosureCreationInterceptingVerifier.INSTANCE) >> scriptRunner
        1 * scriptRunner.getScript() >> script
        1 * scriptRunner.compiledScript >> compiledScript
        1 * compiledScript.data >> true
        1 * script.init(target, _ as ServiceRegistry)
        1 * scriptRunner.run()

        then:
        ScriptPlugin configurer = factory.create(scriptSource, scriptHandler, targetScope, baseScope, "buildscript", DefaultScript, false)
        configurer.apply(target)
    }

    void configuresAScriptAwareObjectUsingScript() {
        when:
        def target = Mock(ScriptAware)

        1 * loggingManagerFactory.create() >> loggingManager
        1 * scriptCompilerFactory.createCompiler(scriptSource) >> scriptCompiler
        1 * scriptCompiler.compile(DefaultScript, _ as FactoryBackedCompileOperation, baseChildClassLoader, classpathClosureName, _) >> classPathScriptRunner
        1 * classPathScriptRunner.getScript() >> classPathScript
        1 * classPathScript.init(target, _ as ServiceRegistry)
        1 * classPathScriptRunner.run()
        1 * classPathScriptRunner.getCompiledScript() >> classpathCompiledScript
        1 * scriptCompiler.compile(DefaultScript, { it.transformer != null }, scopeClassLoader, classpathClosureName, ClosureCreationInterceptingVerifier.INSTANCE) >> scriptRunner
        1 * scriptRunner.getScript() >> script
        1 * scriptRunner.compiledScript >> compiledScript
        1 * compiledScript.data >> true
        1 * script.init(target, _ as ServiceRegistry)
        1 * scriptRunner.run()

        then:
        ScriptPlugin configurer = factory.create(scriptSource, scriptHandler, targetScope, baseScope, "buildscript", DefaultScript, false)
        configurer.apply(target)
    }
}