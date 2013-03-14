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
import java.net.*;
import java.util.*;

import org.apache.maven.settings.DefaultMavenSettingsBuilder;
import org.apache.maven.settings.Proxy;
import org.apache.maven.settings.Settings;
import org.junit.Before;
import org.junit.Test;

/**
 * @author Boris Stanojevic
 *
 */
public class HtmlDownloadMojoTest {

    private HttpDownloadMojo mojo;

    Proxy proxy;
    
    @Before
    public void setUp() throws Exception {
        DefaultMavenSettingsBuilder builder = new DefaultMavenSettingsBuilder();
        String separator = System.getProperty("file.separator");
        File file = new File(System.getProperty("user.home") + separator + ".m2" + separator + "settings.xml");
        proxy = builder.buildSettings(file).getActiveProxy();
        mojo = new HttpDownloadMojo();
        mojo.outputDirectory = new File("target/downloaded-profiles");
        withProxy();
    }

    private void withProxy(){
      Settings settings = new Settings();
      if (proxy != null && proxy.isActive()){
          settings.getProxies().add(proxy);
      }
      mojo.settings = settings;
    }

    @Test
    public void testExportWithParams() throws Exception {
        String url = "http://gazelle.ihe.net/GazelleHL7v2Validator/viewProfile.seam";
        String oids = "          1.3.6.1.4.12559.11.1.1.7,\n" +
                "                1.3.6.1.4.12559.11.1.1.139,\n" +
                "                1.3.6.1.4.12559.11.1.1.141,\n" +
                "                1.3.6.1.4.12559.11.1.1.150";
        Map<String, String> paramMap = new HashMap();
        paramMap.put("oid", oids);
        mojo.parameterMap = paramMap;
        mojo.baseUrl = new URL(url);
        mojo.execute();
    }

    @Test
    public void testExportWithoutParams() throws Exception {
        String url = "http://repo.openehealth.org/sites/ipf/releases/ipf-2.5-m1-src.zip";
        mojo.fileName = "ipf-2.5-m1-src.zip";
        mojo.baseUrl = new URL(url);
        mojo.execute();
    }

}
