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
package org.hawkular.metrics.restServlet;

import static org.hawkular.metrics.restServlet.config.ConfigurationKey.BACKEND;
import static org.hawkular.metrics.restServlet.config.ConfigurationKey.CASSANDRA_CQL_PORT;
import static org.hawkular.metrics.restServlet.config.ConfigurationKey.CASSANDRA_KEYSPACE;
import static org.hawkular.metrics.restServlet.config.ConfigurationKey.CASSANDRA_NODES;

import java.util.HashMap;
import java.util.Map;

import javax.annotation.PostConstruct;
import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.inject.Produces;
import javax.inject.Inject;

import org.hawkular.metrics.core.api.MetricsService;
import org.hawkular.metrics.core.impl.HawkularMetrics;
import org.hawkular.metrics.restServlet.config.Configurable;
import org.hawkular.metrics.restServlet.config.ConfigurationProperty;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author John Sanda
 */
@ApplicationScoped
@Eager
public class MetricsServiceProducer {
    private static final Logger LOG = LoggerFactory.getLogger(MetricsServiceProducer.class);

    @Inject
    @Configurable
    @ConfigurationProperty(BACKEND)
    private String backend;

    @Inject
    @Configurable
    @ConfigurationProperty(CASSANDRA_CQL_PORT)
    private String cqlPort;

    @Inject
    @Configurable
    @ConfigurationProperty(CASSANDRA_NODES)
    private String nodes;

    @Inject
    @Configurable
    @ConfigurationProperty(CASSANDRA_KEYSPACE)
    private String keyspace;

    private MetricsService metricsService;

    @PostConstruct
    void init() {
        LOG.info("Initializing metrics service");
        getMetricsService();
    }

    @Produces
    public MetricsService getMetricsService() {
        if (metricsService == null) {
            HawkularMetrics.Builder metricsServiceBuilder = new HawkularMetrics.Builder();

            if (backend != null) {
                switch (backend) {
                case "cass":
                    LOG.info("Using Cassandra backend implementation");
                    Map<String, String> options = new HashMap<>();
                    options.put("cqlport", cqlPort);
                    options.put("nodes", nodes);
                    options.put("keyspace", keyspace);
                    metricsServiceBuilder.withOptions(options).withCassandraDataStore();
                    break;
                case "mem":
                    throw new RuntimeException("The memory backend is no longer supported");
                case "embedded_cass":
                default:
                    LOG.info("Using Cassandra backend implementation with an embedded Server");
                    metricsServiceBuilder.withEmbeddedCassandraDataStore();
                }
            } else {
                metricsServiceBuilder.withCassandraDataStore();
            }

            metricsService = metricsServiceBuilder.build();
        }

        return metricsService;
    }
}
