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

package org.apache.shardingsphere.data.pipeline.cdc.core.metadata.processor;

import lombok.extern.slf4j.Slf4j;
import org.apache.shardingsphere.data.pipeline.cdc.api.job.type.CDCJobType;
import org.apache.shardingsphere.data.pipeline.core.context.PipelineContext;
import org.apache.shardingsphere.data.pipeline.core.job.PipelineJobCenter;
import org.apache.shardingsphere.data.pipeline.core.metadata.node.event.handler.PipelineChangedJobConfigurationProcessor;
import org.apache.shardingsphere.elasticjob.infra.pojo.JobConfigurationPOJO;
import org.apache.shardingsphere.mode.repository.cluster.listener.DataChangedEvent.Type;

import java.util.concurrent.CompletableFuture;

/**
 * CDC job configuration changed processor.
 */
@Slf4j
public final class CDCJobConfigurationChangedProcessor implements PipelineChangedJobConfigurationProcessor {
    
    @Override
    public void process(final Type eventType, final JobConfigurationPOJO jobConfigPOJO) {
        String jobId = jobConfigPOJO.getJobName();
        if (jobConfigPOJO.isDisabled()) {
            PipelineJobCenter.stop(jobId);
            return;
        }
        switch (eventType) {
            case ADDED:
            case UPDATED:
                if (PipelineJobCenter.isJobExisting(jobId)) {
                    log.info("{} added to executing jobs failed since it already exists", jobId);
                } else {
                    CompletableFuture.runAsync(() -> execute(jobConfigPOJO), PipelineContext.getEventListenerExecutor()).whenComplete((unused, throwable) -> {
                        if (null != throwable) {
                            log.error("execute failed, jobId={}", jobId, throwable);
                        }
                    });
                }
                break;
            case DELETED:
                PipelineJobCenter.stop(jobId);
                break;
            default:
                break;
        }
    }
    
    private void execute(final JobConfigurationPOJO jobConfigPOJO) {
        // TODO not implement yet
    }
    
    @Override
    public String getType() {
        return new CDCJobType().getTypeName();
    }
}
