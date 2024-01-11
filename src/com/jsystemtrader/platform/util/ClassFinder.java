package com.jsystemtrader.platform.util;

import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.Enumeration;
import java.util.List;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;

import com.jsystemtrader.platform.model.JSystemTraderException;

public class ClassFinder {

    /**
     * Searches the classpath (including inside the JAR files) to find classes
     * that extend the specified superclass. The intent is to be able to implement
     * new strategy classes as "plug-and-play" units of JSystemTrader. That is,
     * JSystemTrader will know how to run the trading strategy as long as this
     * strategy is implemented in a class that extends the base Strategy class.
     */
    public List<Class<?>> getClasses(String packageName, String superClassName) throws URISyntaxException, IOException, ClassNotFoundException {
    	return getClasses(packageName, superClassName, false);
    }
    
    public List<Class<?>> getInterfaces(String packageName, String interfaceName) throws URISyntaxException, IOException, ClassNotFoundException {
    	return getClasses(packageName, interfaceName, true);
    }    
   
    public List<Class<?>> getClasses(String packageName, String parentName, boolean parentIsInterface) throws URISyntaxException, IOException, ClassNotFoundException {

    	System.out.println("Yan>>>>>"+ packageName );
    	String packagePath = packageName.replace('.', '/');
        List<Class<?>> classes = new ArrayList<Class<?>>();
        ClassLoader tclassLoader = Thread.currentThread().getContextClassLoader();
        assert tclassLoader != null;
      
        String path = packageName.replace('.', '/');
        Enumeration resources = tclassLoader.getResources(path);
 
//		The following line will throw class cast exception with jdk > 8  currently I'm using java version "20.0.2" 2023-07-18
//      URL[] classpath = ((URLClassLoader) ClassLoader.getSystemClassLoader()).getURLs();
//        for (URL url : classpath) {
        while (resources.hasMoreElements()) {
            List<String> classNames = new ArrayList<String>();
            URL url = (URL) resources.nextElement();
            ClassLoader classLoader = new URLClassLoader(new URL[]{url});
            URI uri = url.toURI();
            File file = new File(uri);
            if (file.getPath().endsWith(".jar")) {
                if (file.exists()) {
                    JarFile jarFile = new JarFile(file);
                    for (Enumeration<JarEntry> entries = jarFile.entries(); entries.hasMoreElements();) {
                        String entryName = (entries.nextElement()).getName();
                        if (entryName.matches(packagePath + "/\\w*\\.class")) {// get only class files in package dir
                            String className = entryName.replace('/', '.').substring(0, entryName.lastIndexOf('.'));
                            classNames.add(className);
                        }
                    }
                }
            } else {// directory
//              File packageDirectory = new File(file.getPath() + "/" + packagePath);
                File packageDirectory = new File(file.getPath());
                if (packageDirectory.exists()) {
                    for (File f : packageDirectory.listFiles()) {
                        if (f.getPath().endsWith(".class")) {
                            String className = packageName + "." + f.getName()
                                    .substring(0, f.getName().lastIndexOf('.'));
                            classNames.add(className);
                        }
                    }
                }
            }

            // make sure the strategy extends the base parentName class
            for (String className : classNames) {
                Class<?> clazz = classLoader.loadClass(className);                
                if ( parentIsInterface ) {
                	boolean interfaceFound = false;
                	for(Class<?> implementedInterface: clazz.getInterfaces()) {
                		if(implementedInterface.getName().equals(parentName)) {
                			interfaceFound = true;
                			break;
                		}
                	}
                	if(interfaceFound) {
                		classes.add(clazz);
                	}
                }
                else if(clazz.getSuperclass()!=null && clazz.getSuperclass().getName().equals(parentName)) {
                    classes.add(clazz);
                }
            }
        }

        Collections.sort(classes, new Comparator<Class<?>>() {
            public int compare(Class<?> o1, Class<?> o2) {
                return o1.getName().compareTo(o2.getName());
            }
        });
        return classes;
    }

    public String[] getClassNames() throws JSystemTraderException {
        String[] names;
        try {
            List<Class<?>> classes = getClasses("com.jsystemtrader.strategy", "com.jsystemtrader.platform.strategy.Strategy");
            List<String> classNames = new ArrayList<String>();

            for (Class<?> strategyClass : classes) {
                classNames.add(strategyClass.getSimpleName());
            }
            names = classNames.toArray(new String[classNames.size()]);
        } catch (Exception e) {
            throw new JSystemTraderException(e);
        }

        Arrays.sort(names);
        return names;
    }
}
