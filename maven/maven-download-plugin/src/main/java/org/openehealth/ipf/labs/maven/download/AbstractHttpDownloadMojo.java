/*
 * Copyright 2013 the original author or authors.
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *     
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.openehealth.ipf.labs.maven.download;

import java.io.File;
import java.net.InetAddress;
import java.net.UnknownHostException;

import org.apache.http.HttpHost;
import org.apache.http.auth.*;
import org.apache.http.conn.params.ConnRoutePNames;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.settings.Proxy;
import org.apache.maven.settings.Settings;

/**
 * @author Boris Stanojevic
 * @author Mitko Kolev
 *
 */
public abstract class AbstractHttpDownloadMojo extends AbstractMojo {

    protected DefaultHttpClient client;

    /**
     * The Maven Settings.
     * @parameter default-value="${settings}"
     * @required
     * @readonly
     */
    protected Settings settings;

    /**
     * Location of the output directory.
     * @parameter expression="${output.directory}" default-value="${project.build.outputDirectory}"
     * @required
     */
    protected File outputDirectory;


    protected void configureHttpClientProxy() throws UnknownHostException {
        Proxy activeProxy = this.settings.getActiveProxy();
        HttpHost proxy = new HttpHost(activeProxy.getHost(), activeProxy.getPort());

        String proxyUserName = activeProxy.getUsername();
        if (defined(proxyUserName)) {
            String proxyPassword = activeProxy.getPassword();
            client.getCredentialsProvider()
                    .setCredentials(new AuthScope(proxy.getHostName(), proxy.getPort()),
                                    new NTCredentials(
                                            proxyUserName,
                                            proxyPassword,
                                            InetAddress.getLocalHost().getHostName(),
                                            ""));
        }
        getLog().info("Using proxy: " + activeProxy.getHost()+":"+ activeProxy.getPort());
        client.getParams().setParameter(ConnRoutePNames.DEFAULT_PROXY, proxy);
    }
    
    
    protected static boolean defined(String value) {
        return value != null && !value.isEmpty();
    }

    protected boolean hasProxy(){
        if (settings == null || settings.getActiveProxy() == null) {
            return false;
        }
        return true;
    }
}
