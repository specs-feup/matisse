/**
 * Copyright 2016 SPeCS.
 * 
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except in compliance with
 * the License. You may obtain a copy of the License at
 * 
 * http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software distributed under the License is distributed on
 * an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations under the License. under the License.
 */

package org.specs.matisselib.io;

import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.List;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;

import pt.up.fe.specs.util.SpecsIo;
import pt.up.fe.specs.util.SpecsLogs;

public class ClassFinderHelper {

    /**
     * Gets all classes in the current classpath that are in one of the specified packages (or subpackages) and belong
     * to the specified base class.
     * 
     * To get all classes, use an empty list as the package and Object as the base class.
     * 
     * @param packages
     *            Packages to include in the result
     * @param baseClass
     *            The base type of all returned classes
     * @return A map with all found classes that match the specified criteria. The keys are the name of the classes
     *         (excluding the package).
     * @throws IOException
     *             If a file operation failed
     */
    @SuppressWarnings("unchecked")
    public static <T> HashMap<String, Class<? extends T>> findClasses(List<String> packages, Class<T> baseClass)
            throws IOException {

        HashMap<String, Class<? extends T>> passClasses = new HashMap<>();

        File jarFile;
        try {
            jarFile = new File(
                    ClassFinderHelper.class.getProtectionDomain().getCodeSource().getLocation().toURI());
        } catch (URISyntaxException e1) {
            throw new RuntimeException(e1);
        }

        if (jarFile.isFile()) {
            try (JarFile jar = new JarFile(jarFile)) {
                for (Enumeration<JarEntry> iterator = jar.entries(); iterator.hasMoreElements();) {
                    JarEntry entry = iterator.nextElement();
                    String name = entry.getName();

                    if (!isNonNestedClass(name)) {
                        continue;
                    }

                    String resourceName = getNameWithoutClassExtension(name);
                    resourceName = resourceName.replace("/", ".");

                    tryAddPassClass(packages, baseClass, passClasses, resourceName);
                }
            }
        } else {
            ClassLoader classLoader = ClassFinderHelper.class.getClassLoader();

            Enumeration<URL> roots = classLoader.getResources("");
            while (roots.hasMoreElements()) {
                URL url = roots.nextElement();

                File rootFile;
                try {

                    // Ignoring JARs inside the given folder
                    if (url.toURI().toString().startsWith("jar:")) {
                        continue;
                    }

                    rootFile = new File(url.toURI());
                } catch (URISyntaxException e) {
                    SpecsLogs.warn("Error message:\n", e);
                    throw new RuntimeException(e);
                }
                for (File file : SpecsIo.getFilesRecursive(rootFile)) {
                    String fileName = file.getName();
                    if (!isNonNestedClass(fileName)) {
                        continue;
                    }

                    String resourceName = getNameWithoutClassExtension(fileName);
                    for (;;) {
                        file = file.getParentFile();
                        if (file.equals(rootFile)) {
                            break;
                        }
                        resourceName = file.getName() + "." + resourceName;
                    }

                    tryAddPassClass(packages, baseClass, passClasses, resourceName);
                }
            }
        }

        return passClasses;
    }

    private static <T> void tryAddPassClass(List<String> packages, Class<T> baseClass,
            HashMap<String, Class<? extends T>> passClasses, String resourceName) {
        try {
            if (!packages.stream()
                    .map(pkg -> pkg.replace('/', '.'))
                    .anyMatch(resourceName::startsWith)) {
                return;
            }

            Class<?> clazz = Class.forName(resourceName);

            if (!baseClass.isAssignableFrom(clazz)) {
                return;
            }

            passClasses.put(clazz.getSimpleName(), (Class<? extends T>) clazz);
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        } catch (NoClassDefFoundError e) {
            // Class could not be used. Ignore.
        } catch (VerifyError e) {
            System.out.println("VerifyError at " + resourceName);

            throw e;
        }
    }

    private static String getNameWithoutClassExtension(String fileName) {
        return fileName.substring(0, fileName.length() - ".class".length());
    }

    private static boolean isNonNestedClass(String fileName) {
        return !fileName.contains("$") && fileName.endsWith(".class");
    }
}
