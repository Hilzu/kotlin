/*
 * Copyright 2010-2013 JetBrains s.r.o.
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

package org.jetbrains.jet.jvm.compiler;

import com.google.common.base.Predicates;
import com.google.common.collect.Lists;
import com.intellij.openapi.Disposable;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.Disposer;
import com.intellij.openapi.util.io.FileUtil;
import com.intellij.psi.PsiFile;
import com.intellij.util.ArrayUtil;
import junit.framework.Assert;
import junit.framework.Test;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.jet.*;
import org.jetbrains.jet.cli.jvm.compiler.CompileEnvironmentUtil;
import org.jetbrains.jet.cli.jvm.compiler.JetCoreEnvironment;
import org.jetbrains.jet.codegen.ClassFileFactory;
import org.jetbrains.jet.codegen.GenerationUtils;
import org.jetbrains.jet.config.CompilerConfiguration;
import org.jetbrains.jet.di.InjectorForJavaSemanticServices;
import org.jetbrains.jet.di.InjectorForTopDownAnalyzerForJvm;
import org.jetbrains.jet.lang.descriptors.ModuleDescriptor;
import org.jetbrains.jet.lang.descriptors.NamespaceDescriptor;
import org.jetbrains.jet.lang.psi.JetFile;
import org.jetbrains.jet.lang.resolve.AnalyzerScriptParameter;
import org.jetbrains.jet.lang.resolve.BindingTrace;
import org.jetbrains.jet.lang.resolve.TopDownAnalysisParameters;
import org.jetbrains.jet.lang.resolve.java.DescriptorSearchRule;
import org.jetbrains.jet.lang.resolve.java.JavaDescriptorResolver;
import org.jetbrains.jet.lang.resolve.name.FqName;
import org.jetbrains.jet.lang.resolve.name.Name;
import org.jetbrains.jet.test.TestCaseWithTmpdir;
import org.jetbrains.jet.test.util.NamespaceComparator;

import java.io.File;
import java.io.FilenameFilter;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.regex.Pattern;

import static org.jetbrains.jet.jvm.compiler.LoadDescriptorUtil.compileKotlinToDirAndGetAnalyzeExhaust;
import static org.jetbrains.jet.test.util.NamespaceComparator.compareNamespaceWithFile;

public abstract class AbstractCompileKotlinAgainstCustomJavaTest extends TestCaseWithTmpdir {
    protected void doTest(String ktFilePath) throws Exception {
        org.junit.Assert.assertTrue(ktFilePath.endsWith(".kt"));
        File ktFile = new File(ktFilePath);
        File dir = ktFile.getParentFile();

        checkCompiledNamespace(ktFilePath, dir, LoadDescriptorUtil.TEST_PACKAGE_FQNAME, "expected.txt");
        // checkCompiledNamespace(ktFilePath, dir, FqName.topLevel(Name.identifier("testing")), "expectedtesting.txt");
    }

    private void checkCompiledNamespace(String ktFilePath, File dir, FqName namespaceFqn, String fileName) throws IOException {
        JetCoreEnvironment environment = getEnvironment(ktFilePath);

        Project project = environment.getProject();

        InjectorForJavaSemanticServices injectorForJava = new InjectorForJavaSemanticServices(project);

        // we need the same binding trace for resolve from Java and Kotlin
        BindingTrace bindingTrace = injectorForJava.getBindingTrace();

        InjectorForTopDownAnalyzerForJvm injectorForAnalyzer = new InjectorForTopDownAnalyzerForJvm(
                project,
                new TopDownAnalysisParameters(
                        Predicates.<PsiFile>alwaysFalse(), false, false, Collections.<AnalyzerScriptParameter>emptyList()),
                bindingTrace,
                new ModuleDescriptor(Name.special("<test module>")));

        injectorForAnalyzer.getTopDownAnalyzer().analyzeFiles(
                Collections.singletonList(JetTestUtils.loadJetFile(project, new File(ktFilePath))), Collections.<AnalyzerScriptParameter>emptyList());

        JavaDescriptorResolver javaDescriptorResolver = injectorForJava.getJavaDescriptorResolver();
        NamespaceDescriptor namespaceDescriptor = javaDescriptorResolver.resolveNamespace(namespaceFqn, DescriptorSearchRule.INCLUDE_KOTLIN);
        assert namespaceDescriptor != null;

        compareNamespaceWithFile(namespaceDescriptor, NamespaceComparator.DONT_INCLUDE_METHODS_OF_OBJECT, new File(dir, fileName));

        ExpectedLoadErrorsUtil.checkForLoadErrors(namespaceDescriptor, bindingTrace.getBindingContext());
    }

    private JetCoreEnvironment getEnvironment(String ktFilePath) {
        File ktFile = new File(ktFilePath);
        File dir = ktFile.getParentFile();

        List<File> jarFiles = FileUtil.findFilesByMask(Pattern.compile("^.*\\.jar$"), dir);

        CopyOnWriteArrayList<File> extras = Lists.newCopyOnWriteArrayList();
        extras.addAll(jarFiles);
        extras.add(JetTestUtils.getAnnotationsJar());
        extras.add(dir);

        CompilerConfiguration configurationWithADirInClasspath = CompileCompilerDependenciesTest.compilerConfigurationForTests(
                ConfigurationKind.ALL, TestJdkKind.MOCK_JDK, ArrayUtil.toObjectArray(extras, File.class));

        return new JetCoreEnvironment(getTestRootDisposable(), configurationWithADirInClasspath);
    }
}
