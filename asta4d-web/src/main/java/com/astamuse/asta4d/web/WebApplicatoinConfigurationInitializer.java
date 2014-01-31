package com.astamuse.asta4d.web;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.util.Enumeration;
import java.util.Properties;

import javax.servlet.ServletConfig;

import org.apache.commons.beanutils.BeanUtils;
import org.apache.commons.io.FilenameUtils;

import com.astamuse.asta4d.web.util.SystemPropertyUtil;
import com.astamuse.asta4d.web.util.SystemPropertyUtil.PropertyScope;

public class WebApplicatoinConfigurationInitializer {

    public static void initConfigurationFromFile(ServletConfig sc, WebApplicationConfiguration conf) throws Exception {
        String[] fileNames = retrievePossibleConfigurationFileNames();
        InputStream input = null;
        String fileType = null;

        // find file from classpath
        ClassLoader clsLoder = WebApplicatoinConfigurationInitializer.class.getClassLoader();
        for (String name : fileNames) {
            input = clsLoder.getResourceAsStream(name);
            if (input != null) {
                fileType = FilenameUtils.getExtension(name);
                break;
            }
        }

        // find from file system
        // I can do goto by while loop :)
        while (input == null) {

            // find key
            String fileKey = retrieveConfigurationFileNameKey();
            if (fileKey == null) {
                break;
            }

            // get path
            String filePath = retrieveConfigurationFileName(sc, fileKey);
            if (filePath == null) {
                break;
            }

            // load file
            File f = new File(filePath);
            input = new FileInputStream(f);
            fileType = FilenameUtils.getExtension(filePath);
            break;
        }

        if (input != null) {
            try {
                switch (fileType) {
                case "properties":
                    Properties ps = new Properties();
                    ps.load(input);
                    // PropertyUtils.setProperty(bean, name, value)
                    Enumeration<Object> keys = ps.keys();
                    while (keys.hasMoreElements()) {
                        String key = keys.nextElement().toString();
                        BeanUtils.setProperty(conf, key, ps.get(key));
                    }
                    break;
                default:
                    throw new UnsupportedOperationException("File type of " + fileType +
                            " does not be supported for initialize asta4d Configuration.");
                }
            } finally {
                input.close();
            }
        }
    }

    protected static String[] retrievePossibleConfigurationFileNames() {
        // return new String[] {
        // "asta4d.conf.properties, ast4d.conf.js, asta4d.conf.groovy" };
        return new String[] { "asta4d.conf.properties" };
    }

    protected static String retrieveConfigurationFileNameKey() {
        return "asta4d.conf";
    }

    protected static String retrieveConfigurationFileName(ServletConfig sc, String key) {
        return SystemPropertyUtil.retrievePropertyValue(sc, key, PropertyScope.ServletConfig, PropertyScope.JNDI,
                PropertyScope.SystemProperty);
    }
}
