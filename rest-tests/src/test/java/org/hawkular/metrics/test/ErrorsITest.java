/*
 * Copyright 2014-2015 Red Hat, Inc. and/or its affiliates
 * and other contributors as indicated by the @author tags.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.hawkular.metrics.test;

import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import org.junit.After;
import org.junit.Assert;
import org.junit.Test;

/**
 * @author Jeeva Kandasamy
 */
public class ErrorsITest extends RESTTest {
    Response response = null;

    @After
    public void closeResponse() {
        if (response != null) {
            response.close();
            response = null;
        }
    }

    @Test
    public void testNotAllowedException() {
        response = target.clone()
                .path("/test/gauge/test/tags")
                .request(MediaType.APPLICATION_JSON_TYPE)
                .post(null);
        Assert.assertEquals(405, response.getStatus());
        ApiErrorJson apiErrorJson = response.readEntity(ApiErrorJson.class);
        Assert.assertEquals("No resource method found for POST, return 405 with Allow header",
                apiErrorJson.getErrorMsg());
    }

    @Test
    public void testNotFoundException() {
        response = target.clone()
                .path("/test/gaugesssss/test/data")
                .queryParam("buckets", "999")
                .request(MediaType.APPLICATION_JSON_TYPE)
                .get();
        Assert.assertEquals(404, response.getStatus());
        ApiErrorJson apiErrorJson = response.readEntity(ApiErrorJson.class);
        Assert.assertEquals("Could not find resource for full path: http://"+baseURI
                + "/test/gaugesssss/test/data?buckets=999",
                apiErrorJson.getErrorMsg());
    }

    @Test
    public void testNumberFormatException() {
        response = target.clone()
                .path("/test/gauge/test/data")
                .queryParam("buckets", "999999999999999999999999")
                .request(MediaType.APPLICATION_JSON_TYPE)
                .get();
        Assert.assertEquals(400, response.getStatus());
        ApiErrorJson apiErrorJson = response.readEntity(ApiErrorJson.class);
        Assert.assertEquals("For input string: \"999999999999999999999999\"",
                apiErrorJson.getErrorMsg());
    }

    @Test
    public void testNotAcceptableException() {
        response = target.clone()
                .path("/test/gauge/test/data")
                .request(MediaType.TEXT_PLAIN)
                .get();
        Assert.assertEquals(406, response.getStatus());
        ApiErrorJson apiErrorJson = response.readEntity(ApiErrorJson.class);
        Assert.assertEquals("No match for accept header",
                apiErrorJson.getErrorMsg());
    }

    @Test
    public void testNotSupportedException() {
        response = target.clone()
                .path("/test/gauge/test/data")
                .request(MediaType.TEXT_PLAIN)
                .post(null);
        Assert.assertEquals(415, response.getStatus());
        ApiErrorJson apiErrorJson = response.readEntity(ApiErrorJson.class);
        Assert.assertEquals("Cannot consume content type",
                apiErrorJson.getErrorMsg());
    }
}