/*
 * Licensed to Elasticsearch under one or more contributor
 * license agreements. See the NOTICE file distributed with
 * this work for additional information regarding copyright
 * ownership. Elasticsearch licenses this file to you under
 * the Apache License, Version 2.0 (the "License"); you may
 * not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package fr.pilato.elasticsearch.tools;

import org.apache.http.HttpHost;
import org.elasticsearch.client.Request;
import org.elasticsearch.client.Response;
import org.elasticsearch.client.ResponseException;
import org.elasticsearch.client.RestClient;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.net.ConnectException;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import static java.util.Arrays.asList;
import static java.util.Collections.singletonList;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.not;
import static org.junit.Assume.assumeThat;
import static org.junit.Assume.assumeTrue;

public abstract class AbstractBeyonderTest {

    static final Logger logger = LoggerFactory.getLogger(AbstractBeyonderTest.class);

    private final static String DEFAULT_TEST_CLUSTER_HOST = "127.0.0.1";
    private final static String DEFAULT_TEST_CLUSTER_SCHEME = "http";
    private final static Integer DEFAULT_TEST_CLUSTER_REST_PORT = 9400;
    private final static Integer DEFAULT_TEST_CLUSTER_TRANSPORT_PORT = 9500;

    final static String testClusterHost = System.getProperty("tests.cluster.host", DEFAULT_TEST_CLUSTER_HOST);
    private final static String testClusterScheme = System.getProperty("tests.cluster.scheme", DEFAULT_TEST_CLUSTER_SCHEME);
    private final static int testClusterRestPort = Integer.parseInt(System.getProperty("tests.cluster.rest.port", DEFAULT_TEST_CLUSTER_REST_PORT.toString()));
    final static int testClusterTransportPort = Integer.parseInt(System.getProperty("tests.cluster.transport.port", DEFAULT_TEST_CLUSTER_TRANSPORT_PORT.toString()));

    abstract protected void testBeyonder(String root,
                                List<String> indices,
                                List<List<String>> types,
                                List<String> templates) throws Exception;

    private static RestClient client;

    static RestClient restClient() throws IOException {
        if (client == null) {
            startRestClient();
        }
        return client;
    }

    private static void startRestClient() throws IOException {
        if (client == null) {
            client = RestClient.builder(new HttpHost(testClusterHost, testClusterRestPort, testClusterScheme)).build();
            testClusterRunning();
        }
    }

    private static boolean testClusterRunning() throws IOException {
        try {
            Response response = client.performRequest(new Request("GET", "/"));
            Map<String, Object> asMap = (Map<String, Object>) JsonUtil.asMap(response).get("version");
            logger.info("Starting integration tests against an external cluster running elasticsearch [{}]", asMap.get("number"));
            return false;
        } catch (ConnectException e) {
            // If we have an exception here, let's ignore the test
            logger.warn("Integration tests are skipped: [{}]", e.getMessage());
            assumeThat("Integration tests are skipped", e.getMessage(), not(containsString("Connection refused")));
            return false;
        } catch (IOException e) {
            logger.error("Full error is", e);
            throw e;
        }
    }

    static boolean supportsMultipleTypes = true;

    @Test
    public void testDefaultDir() throws Exception {
        // Default dir es
        testBeyonder(null,
                singletonList("twitter"),
                singletonList(singletonList("tweet")),
                null);
    }

    @Test
    public void testOneIndexOneType() throws Exception {
        // Single index/single type
        testBeyonder("models/oneindexonetype",
                singletonList("twitter"),
                singletonList(singletonList("tweet")),
                null);
    }

    @Test
    public void testSettingsAnalyzer() throws Exception {
        // Custom settings (analyzer)
        testBeyonder("models/settingsanalyzer",
                singletonList("twitter"),
                singletonList(singletonList("tweet")),
                null);
    }

    @Test
    public void testOneIndexNoType() throws Exception {
        // 1 index and no type
        testBeyonder("models/oneindexnotype",
                singletonList("twitter"),
                singletonList(null),
                null);
    }

    @Test
    public void testTemplate() throws Exception {
        // 1 template
        testBeyonder("models/template",
                null,
                null,
                singletonList("twitter_template"));
    }

    @Test
    public void testUpdateSettings() throws Exception {
        // 1 _update_settings
        testBeyonder("models/update-settings/step1",
                singletonList("twitter"),
                singletonList(singletonList("tweet")),
                null);
        testBeyonder("models/update-settings/step2",
                singletonList("twitter"),
                singletonList(null),
                null);
    }

    @Test
    public void testWrongClasspathDir() throws Exception {
        testBeyonder("models/bad-classpath-7/doesnotexist",
                null,
                null,
                null);
    }
}
