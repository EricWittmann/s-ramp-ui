/*
 * Copyright 2013 JBoss Inc
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
package org.overlord.sramp.ui.server;

import java.io.File;
import java.net.MalformedURLException;
import java.net.URL;

import javax.inject.Singleton;

import org.apache.commons.configuration.CompositeConfiguration;
import org.apache.commons.configuration.Configuration;
import org.apache.commons.configuration.ConfigurationException;
import org.apache.commons.configuration.PropertiesConfiguration;
import org.apache.commons.configuration.SystemConfiguration;
import org.apache.commons.configuration.reloading.FileChangedReloadingStrategy;
import org.overlord.sramp.ui.server.api.SrampApiClientAccessor;
import org.overlord.sramp.ui.server.i18n.Messages;

/**
 * Global access to configuration information.
 *
 * @author eric.wittmann@redhat.com
 */
@Singleton
public class SrampUIConfig {

    public static final String SRAMP_UI_CONFIG_FILE_NAME     = "sramp-ui.config.file.name"; //$NON-NLS-1$
    public static final String SRAMP_UI_CONFIG_FILE_REFRESH  = "sramp-ui.config.file.refresh"; //$NON-NLS-1$

    public static final String SRAMP_API_ENDPOINT = "s-ramp-ui.atom-api.endpoint"; //$NON-NLS-1$
    public static final String SRAMP_API_VALIDATING = "s-ramp-ui.atom-api.validating"; //$NON-NLS-1$
    public static final String SRAMP_API_AUTH_PROVIDER = "s-ramp-ui.atom-api.authentication.provider"; //$NON-NLS-1$
    public static final String SRAMP_API_BASIC_AUTH_USER = "s-ramp-ui.atom-api.authentication.basic.user"; //$NON-NLS-1$
    public static final String SRAMP_API_BASIC_AUTH_PASS = "s-ramp-ui.atom-api.authentication.basic.password"; //$NON-NLS-1$
    public static final String SRAMP_API_SAML_AUTH_ISSUER = "s-ramp-ui.atom-api.authentication.saml.issuer"; //$NON-NLS-1$
    public static final String SRAMP_API_SAML_AUTH_SERVICE = "s-ramp-ui.atom-api.authentication.saml.service"; //$NON-NLS-1$


    public static CompositeConfiguration config;
    static {
        SrampUIConfig.config = new CompositeConfiguration();
        SrampUIConfig.config.addConfiguration(new SystemConfiguration());
        String configFile = config.getString(SRAMP_UI_CONFIG_FILE_NAME);
        Long refreshDelay = config.getLong(SRAMP_UI_CONFIG_FILE_REFRESH, 30000l);
        URL url = findSrampUiConfig(configFile);
        try {
            if (url != null) {
                PropertiesConfiguration propertiesConfiguration = new PropertiesConfiguration(url);
                FileChangedReloadingStrategy fileChangedReloadingStrategy = new FileChangedReloadingStrategy();
                fileChangedReloadingStrategy.setRefreshDelay(refreshDelay);
                propertiesConfiguration.setReloadingStrategy(fileChangedReloadingStrategy);
                config.addConfiguration(propertiesConfiguration);
            }
            SrampUIConfig.config.addConfiguration(new PropertiesConfiguration(SrampApiClientAccessor.class.getResource("/META-INF/config/org.overlord.sramp.ui.server.api.properties"))); //$NON-NLS-1$
        } catch (ConfigurationException e) {
            e.printStackTrace();
        }
        System.out.println(Messages.i18n.format("Config.Loaded", SrampUIConfig.config.getString(SRAMP_API_ENDPOINT))); //$NON-NLS-1$
    }

    /**
     * Try to find the sramp-ui.properties configuration file.  This will look for the
     * config file in a number of places, depending on the value for 'config file'
     * found on the system properties.
     * @param configFile
     * @throws MalformedURLException
     */
    private static URL findSrampUiConfig(String configFile) {
        try {
            // If a config file was given (via system properties) then try to
            // find it.  If not, then look for a 'standard' config file.
            if (configFile != null) {
                // Check on the classpath
                URL fromClasspath = SrampUIConfig.class.getClassLoader().getResource(configFile);
                if (fromClasspath != null)
                    return fromClasspath;

                // Check on the file system
                File file = new File(configFile);
                if (file.isFile())
                    return file.toURI().toURL();
            } else {
                // Check the current user's home directory
                String userHomeDir = System.getProperty("user.home"); //$NON-NLS-1$
                if (userHomeDir != null) {
                    File dirFile = new File(userHomeDir);
                    if (dirFile.isDirectory()) {
                        File cfile = new File(dirFile, "sramp-ui.properties"); //$NON-NLS-1$
                        if (cfile.isFile())
                            return cfile.toURI().toURL();
                    }
                }

                // Next, check for JBoss
                String jbossConfigDir = System.getProperty("jboss.server.config.dir"); //$NON-NLS-1$
                if (jbossConfigDir != null) {
                    File dirFile = new File(jbossConfigDir);
                    if (dirFile.isDirectory()) {
                        File cfile = new File(dirFile, "sramp-ui.properties"); //$NON-NLS-1$
                        if (cfile.isFile())
                            return cfile.toURI().toURL();
                    }
                }
                String jbossConfigUrl = System.getProperty("jboss.server.config.url"); //$NON-NLS-1$
                if (jbossConfigUrl != null) {
                    File dirFile = new File(jbossConfigUrl);
                    if (dirFile.isDirectory()) {
                        File cfile = new File(dirFile, "sramp-ui.properties"); //$NON-NLS-1$
                        if (cfile.isFile())
                            return cfile.toURI().toURL();
                    }
                }
            }
        } catch (MalformedURLException e) {
            e.printStackTrace();
        }
        return null;
    }

    /**
     * Constructor.
     */
    public SrampUIConfig() {
    }

    /**
     * @return the configuration
     */
    public Configuration getConfig() {
        return config;
    }

}
