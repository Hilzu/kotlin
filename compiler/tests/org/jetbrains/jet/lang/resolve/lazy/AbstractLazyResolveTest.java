/*
 * Copyright 2010-2012 JetBrains s.r.o.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.jetbrains.jet.lang.resolve.lazy;

import com.intellij.openapi.Disposable;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.Disposer;
import org.jetbrains.jet.CompileCompilerDependenciesTest;
import org.jetbrains.jet.cli.jvm.compiler.JetCoreEnvironment;
import org.jetbrains.jet.lang.resolve.java.CompilerDependencies;
import org.jetbrains.jet.lang.resolve.java.CompilerSpecialMode;
import org.junit.After;
import org.junit.BeforeClass;

/**
 * @author abreslav
 */
public class AbstractLazyResolveTest {
    private final Disposable rootDisposable = new Disposable() {
        @Override
        public void dispose() {
        }
    };

    protected final CompilerDependencies
              compilerDependencies = CompileCompilerDependenciesTest.compilerDependenciesForTests(CompilerSpecialMode.REGULAR, true);
    protected final JetCoreEnvironment jetCoreEnvironment = new JetCoreEnvironment(rootDisposable, compilerDependencies);
    protected final Project project = jetCoreEnvironment.getProject();

    @BeforeClass
    public static void setUp() throws Exception {
        System.setProperty("java.awt.headless", "true");
    }

    @After
    public void tearDown() throws Exception {
        Disposer.dispose(rootDisposable);
    }

}