package org.jini.glyph.di;

import java.io.IOException;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;

import org.jini.glyph.Injection;

import net.jini.config.Configuration;
import net.jini.config.ConfigurationException;
import net.jini.config.ConfigurationProvider;



public class DIFactory {

		private static Map<String, DIPlugin> lookupPlugins = new HashMap<String,DIPlugin>();
	
        private static Map<String, Injector> configmap;

        private static Map<String, Map<String, Method>> classMethodMap = new HashMap<String, Map<String, Method>>();
        
        static {
                configmap = new HashMap<String, Injector>();
        }
        
        public static void registerPlugin(Class recognisedClass, DIPlugin plugin){
        	lookupPlugins.put(recognisedClass.getName(), plugin);
        }
        
        public static void injectObject(Object delegate, String configFilename) throws ConfigurationException{
            
            if(delegate.getClass().getAnnotation(Injection.class)!=null){
                Injector injector = DIFactory.getInjector(configFilename);
                injector.inject(delegate);
            } else
                System.out.println("No Injection Annotation on " + delegate.getClass().getName());
        }

        public static Injector getInjector(String filename) throws ConfigurationException {
                //System.out.println("Looking for: " + filename);
                if (!configmap.containsKey(filename)) {
                        Configuration config = ConfigurationProvider.getInstance(new String[] { filename });

                        ConfigurationLoader handler = new ConfigurationLoader(filename);
                        long start = System.currentTimeMillis();
                        try {
                                handler.parse();
                                long end = System.currentTimeMillis();
                              //  System.out.println("Parsing took: " + (end - start) + " ms (CL: " + Thread.currentThread().getContextClassLoader().toString() + " )");
                                Injector scanner = new Injector(handler.getScan(), config, classMethodMap);
                                configmap.put(filename, scanner);
                                scanner.setLookupPlugins(lookupPlugins);
                                
                                return scanner;
                        } catch (IOException e) {
                                // TODO Auto-generated catch block
                                e.printStackTrace();
                        }
                        return null;
                } else
                        return configmap.get(filename);
        }

     
}
