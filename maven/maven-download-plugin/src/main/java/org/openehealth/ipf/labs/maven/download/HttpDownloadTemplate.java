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

import org.apache.commons.io.IOUtils;
import org.apache.http.HttpException;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.maven.plugin.logging.Log;

import java.io.*;
import java.net.URL;
import java.util.List;
import java.util.Map;

/**
 * @author Boris Stanojevic
 * @author Mitko Kolev
 * 
 */
public class HttpDownloadTemplate {

    protected URL httpBaseURL;
    protected final Log log;

    public HttpDownloadTemplate(URL httpBaseUrl, Log log) {
        this.httpBaseURL = httpBaseUrl;
        this.log = log;
    }

    public void download(HttpClient httpClient,
                       Map<String, List<String>> paramMap,
                       File outputFolder,
                       String fileName) throws IOException, HttpException {

        if (paramMap == null){
            log.info("Downloading " + httpBaseURL.toExternalForm());
            String downloadFileName = fileName != null? fileName:httpBaseURL.getPath();
            File target =  new File(outputFolder, downloadFileName);
            store(httpBaseURL.toExternalForm(), httpClient, target);
        } else {
            for (String paramName : paramMap.keySet()) {
                log.info("Looping param:" + paramName);

                List<String> paramList = paramMap.get(paramName);

                for (String paramValue: paramList){
                    String downloadUrl = getDownloadUrl(httpBaseURL, paramName, paramValue);
                    log.info("Downloading " + downloadUrl);
                    long downloadStarted = System.currentTimeMillis();
                    File target =  new File(outputFolder, paramValue);

                    store(downloadUrl, httpClient, target);
                    log.info(took("Downloading " + target.getName(), System.currentTimeMillis() - downloadStarted));
                    log.info("-----------");
                }
            }
        }
    }

    private static String getDownloadUrl(URL baseUrl, String paramName, String paramValue){
        return baseUrl.toExternalForm() + "?" + paramName + "=" + paramValue;
    }

    public void store(String downloadUrl,
                      HttpClient client,
                      File targetFile) throws IOException {

        HttpGet get = new HttpGet(downloadUrl);
        InputStream content = null;
        try {
            HttpResponse httpResponse = client.execute(get);
            content = httpResponse.getEntity().getContent();
            download(content, targetFile);
        } finally {
            IOUtils.closeQuietly(content);
        }
    }

    
    private void download(InputStream content, File targetFile) throws IOException {
        if (!targetFile.exists()) {
            targetFile.setWritable(true);
            targetFile.getParentFile().mkdirs();
            boolean created = targetFile.createNewFile();
            if (!created) {
                throw new IOException("Unable to create file " + targetFile.getAbsolutePath());
            }
        }
        OutputStream targetStream = null;
        try {
            targetStream = new BufferedOutputStream(new FileOutputStream(targetFile), 1024 * 4);
            IOUtils.copy(content, targetStream);
        } finally {
            IOUtils.closeQuietly(targetStream);
        }
    }

    protected String took(String prefix, long millis){
        int seconds = (int) (millis / 1000) % 60 ;
        int minutes = (int) ((millis / (1000*60)) % 60);
        int hours   = (int) ((millis / (1000*60*60)) % 24);
        
        StringBuilder took = new StringBuilder(prefix + " took ");
        if (hours != 0){
            took.append((hours + "h "));
            took.append((minutes + "min "));
        } else {
            if (minutes != 0){
                took.append((minutes + "min "));
            }
        }
        took.append(seconds + " seconds");
        
        return took.toString(); 
    }
    
   
}
