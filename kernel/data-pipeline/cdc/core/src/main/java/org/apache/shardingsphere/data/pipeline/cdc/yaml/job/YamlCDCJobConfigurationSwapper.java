/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.apache.shardingsphere.data.pipeline.cdc.yaml.job;

import org.apache.shardingsphere.data.pipeline.api.datasource.config.yaml.YamlPipelineDataSourceConfigurationSwapper;
import org.apache.shardingsphere.data.pipeline.cdc.config.job.CDCJobConfiguration;
import org.apache.shardingsphere.infra.util.yaml.YamlEngine;
import org.apache.shardingsphere.infra.util.yaml.swapper.YamlConfigurationSwapper;

/**
 * YAML CDC job configuration swapper.
 */
public final class YamlCDCJobConfigurationSwapper implements YamlConfigurationSwapper<YamlCDCJobConfiguration, CDCJobConfiguration> {
    
    private final YamlPipelineDataSourceConfigurationSwapper dataSourceConfigSwapper = new YamlPipelineDataSourceConfigurationSwapper();
    
    @Override
    public YamlCDCJobConfiguration swapToYamlConfiguration(final CDCJobConfiguration data) {
        YamlCDCJobConfiguration result = new YamlCDCJobConfiguration();
        result.setJobId(data.getJobId());
        result.setDatabase(data.getDatabase());
        result.setTableNames(data.getTableNames());
        result.setSubscriptionName(data.getSubscriptionName());
        result.setSubscriptionMode(data.getSubscriptionMode());
        result.setSourceDatabaseType(data.getSourceDatabaseType());
        result.setDataSourceConfiguration(dataSourceConfigSwapper.swapToYamlConfiguration(data.getDataSourceConfiguration()));
        return result;
    }
    
    @Override
    public CDCJobConfiguration swapToObject(final YamlCDCJobConfiguration yamlConfig) {
        return new CDCJobConfiguration(yamlConfig.getJobId(), yamlConfig.getDatabase(), yamlConfig.getTableNames(), yamlConfig.getSubscriptionName(), yamlConfig.getSubscriptionMode(),
                yamlConfig.getSourceDatabaseType(), dataSourceConfigSwapper.swapToObject(yamlConfig.getDataSourceConfiguration()));
    }
    
    /**
     * Swap to job configuration from text.
     *
     * @param jobParam job parameter
     * @return job configuration
     */
    public CDCJobConfiguration swapToObject(final String jobParam) {
        return null == jobParam ? null : swapToObject(YamlEngine.unmarshal(jobParam, YamlCDCJobConfiguration.class, true));
    }
}
