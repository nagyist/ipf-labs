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

import org.apache.http.HttpException;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;

import java.io.IOException;
import java.net.URL;
import java.net.UnknownHostException;
import java.rmi.RemoteException;
import java.util.*;

/**
 * Http download Mojo. Downloads content over HTTP-GET method.
 *
 * <plugin>
 *     <groupId>org.openehealth.ipf.labs.maven</groupId>
 *     <artifactId>maven-download-plugin</artifactId>
 *     <version>0.1-SNAPSHOT</version>
 *     <executions>
 *         <execution>
 *         <id>download-profile-data</id>
 *         <phase>process-resources</phase>
 *         <goals>
 *             <goal>download</goal>
 *         </goals>
 *         <configuration>
 *             <baseUrl>http://gazelle.ihe.net/GazelleHL7v2Validator/viewProfile.seam</baseUrl>
 *             <outputDirectory>${project.build.directory}/gazelle-profiles</outputDirectory>
 *             <parameterMap>
 *                 <oid>
 *                 1.3.6.1.4.12559.11.1.1.7,
 *                 1.3.6.1.4.12559.11.1.1.139,
 *                 1.3.6.1.4.12559.11.1.1.141,
 *                 1.3.6.1.4.12559.11.1.1.150
 *                 </oid>
 *             </parameterMap>
 *         </configuration>
 *         </execution>
 *         <execution>
 *         <id>download-ipf-sources</id>
 *         <phase>process-resources</phase>
 *         <goals>
 *             <goal>download</goal>
 *         </goals>
 *         <configuration>
 *             <baseUrl>http://repo.openehealth.org/sites/ipf/releases/ipf-2.5-m1-src.zip</baseUrl>
 *             <outputDirectory>${project.build.directory}/ipf-sources</outputDirectory>
 *             <fileName>src-ipf-2.5-m1.zip</fileName>
 *             </configuration>
 *         </execution>
 *     </executions>
 * </plugin>
 *
 * @author Boris Stanojevic
 * @author Mitko Kolev
 *
 * @goal download
 */
public class HttpDownloadMojo extends AbstractHttpDownloadMojo {

    private HttpDownloadTemplate httpDownloadTemplate;

    /**
     * @parameter
     * @required
     */
    protected URL baseUrl;

    /**
     * @parameter
     */
    protected String fileName;

    /**
     * @parameter
     */
    protected Map<String, String> parameterMap;

    private Map<String, List<String>> mojoParameterMap;

    @Override
    public void execute() throws MojoExecutionException, MojoFailureException {
        if (parameterMap != null){
            initializeParamMap();
        }

        client = new DefaultHttpClient();
        if (hasProxy()) {
            try {
                configureHttpClientProxy();
            } catch (UnknownHostException e){
                throw new MojoExecutionException(e.getMessage() , e);
            }
        }

        httpDownloadTemplate = new HttpDownloadTemplate(baseUrl, getLog());
        try {
            httpDownloadTemplate.download(client, mojoParameterMap, outputDirectory, fileName);
        } catch (RemoteException e) {
            throw new MojoExecutionException(e.getMessage(), e);
        } catch (IOException e) {
            throw new MojoExecutionException(e.getMessage() , e);
        }  catch (HttpException e){
            throw new MojoExecutionException(e.getMessage() , e);
        }  finally{
        
            client.getConnectionManager().shutdown();
        }
    }

    private void initializeParamMap(){
        mojoParameterMap = new HashMap<String, List<String>>(parameterMap.size());
        for (String key: parameterMap.keySet()){
            String valuesAsString = parameterMap.get(key);
            if (valuesAsString != null){
                List<String> valuesAsList = new ArrayList<String>();
                StringTokenizer tokenizer = new StringTokenizer(valuesAsString, ",");
                while (tokenizer.hasMoreTokens()){
                    valuesAsList.add(tokenizer.nextToken().trim());
                }
                mojoParameterMap.put(key, valuesAsList);
            }
        }
    }

}
