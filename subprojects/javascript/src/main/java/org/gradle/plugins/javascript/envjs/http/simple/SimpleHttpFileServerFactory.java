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

package org.gradle.plugins.javascript.envjs.http.simple;

import org.eclipse.jetty.server.Handler;
import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.server.handler.DefaultHandler;
import org.eclipse.jetty.server.handler.HandlerList;
import org.eclipse.jetty.server.handler.ResourceHandler;
import org.gradle.api.UncheckedIOException;
import org.gradle.internal.concurrent.Stoppable;
import org.gradle.plugins.javascript.envjs.http.HttpFileServer;
import org.gradle.plugins.javascript.envjs.http.HttpFileServerFactory;

import java.io.File;

public class SimpleHttpFileServerFactory implements HttpFileServerFactory {

    public HttpFileServer start(File contentRoot, int port) {
        try {
            final Server server = new Server(8080);

            ResourceHandler handler = new ResourceHandler();
            handler.setResourceBase(contentRoot.getPath());

            HandlerList handlers = new HandlerList();
            handlers.setHandlers(new Handler[] { handler, new DefaultHandler() });
            server.setHandler(handlers);

            server.start();

            return new SimpleHttpFileServer(contentRoot, port, new Stoppable() {
                public void stop() {
                    try {
                        server.join();
                    } catch (InterruptedException e) {
                        throw new UncheckedIOException(e);
                    }
                }
            });
        } catch (Exception e) {
            throw new UncheckedIOException(e);
        }
    }


}
