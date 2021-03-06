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

package org.gradle.play.integtest.fixtures

import org.gradle.play.internal.DefaultPlayPlatform

class PlayCoverage {
    static final List<String> DEFAULT = ["2.2.1", "2.2.6", "2.3.1", DefaultPlayPlatform.DEFAULT_PLAY_VERSION].asImmutable()
    static final List<String> JDK8 = (DEFAULT + ["2.4.0"]).asImmutable()
}
