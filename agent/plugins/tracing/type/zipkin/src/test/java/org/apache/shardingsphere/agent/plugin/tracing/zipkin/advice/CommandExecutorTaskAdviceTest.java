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

package org.apache.shardingsphere.agent.plugin.tracing.zipkin.advice;

import org.apache.shardingsphere.agent.plugin.tracing.advice.AbstractCommandExecutorTaskAdviceTest;
import org.apache.shardingsphere.agent.plugin.tracing.zipkin.collector.ZipkinCollector;
import org.apache.shardingsphere.agent.plugin.tracing.zipkin.constant.ZipkinConstants;
import org.junit.ClassRule;
import org.junit.Test;
import zipkin2.Span;

import java.io.IOException;
import java.util.Map;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;

public final class CommandExecutorTaskAdviceTest extends AbstractCommandExecutorTaskAdviceTest {
    
    @ClassRule
    public static final ZipkinCollector COLLECTOR = new ZipkinCollector();
    
    @Test
    public void assertMethod() {
        CommandExecutorTaskAdvice advice = new CommandExecutorTaskAdvice();
        advice.beforeMethod(getTargetObject(), null, new Object[]{}, "Zipkin");
        advice.afterMethod(getTargetObject(), null, new Object[]{}, null, "Zipkin");
        Span span = COLLECTOR.pop();
        Map<String, String> tags = span.tags();
        assertThat(tags.get(ZipkinConstants.Tags.DB_TYPE), is(ZipkinConstants.DB_TYPE_VALUE));
        assertThat(tags.get(ZipkinConstants.Tags.COMPONENT), is(ZipkinConstants.COMPONENT_NAME));
        assertThat(tags.get(ZipkinConstants.Tags.CONNECTION_COUNT), is("0"));
        assertThat(span.name(), is("/ShardingSphere/rootInvoke/".toLowerCase()));
    }
    
    @Test
    public void assertExceptionHandle() {
        CommandExecutorTaskAdvice advice = new CommandExecutorTaskAdvice();
        advice.beforeMethod(getTargetObject(), null, new Object[]{}, "Zipkin");
        advice.onThrowing(getTargetObject(), null, new Object[]{}, new IOException(), "Zipkin");
        advice.afterMethod(getTargetObject(), null, new Object[]{}, null, "Zipkin");
        Span span = COLLECTOR.pop();
        Map<String, String> tags = span.tags();
        assertThat(tags.get("error"), is("IOException"));
        assertThat(tags.get(ZipkinConstants.Tags.DB_TYPE), is(ZipkinConstants.DB_TYPE_VALUE));
        assertThat(tags.get(ZipkinConstants.Tags.COMPONENT), is(ZipkinConstants.COMPONENT_NAME));
        assertThat(tags.get(ZipkinConstants.Tags.CONNECTION_COUNT), is("0"));
        assertThat(span.name(), is("/ShardingSphere/rootInvoke/".toLowerCase()));
    }
}
