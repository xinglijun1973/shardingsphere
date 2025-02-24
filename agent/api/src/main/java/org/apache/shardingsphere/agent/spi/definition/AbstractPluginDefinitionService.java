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

package org.apache.shardingsphere.agent.spi.definition;

import org.apache.shardingsphere.agent.api.point.PluginInterceptorPoint;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Abstract plugin definition service.
 */
public abstract class AbstractPluginDefinitionService implements PluginDefinitionService {
    
    private final Map<String, PluginInterceptorPoint.Builder> interceptorPointMap = new HashMap<>();
    
    /**
     * Install to collection of plugin interceptor point.
     *
     * @param isEnhancedForProxy is enhanced for proxy
     * @return collection of plugin interceptor point
     */
    public final Collection<PluginInterceptorPoint> install(final boolean isEnhancedForProxy) {
        if (isEnhancedForProxy) {
            defineProxyInterceptors();
        } else {
            defineJdbcInterceptors();
        }
        return interceptorPointMap.values().stream().map(PluginInterceptorPoint.Builder::install).collect(Collectors.toList());
    }
    
    /**
     * Define proxy interceptors to collection of plugin interceptor point.
     */
    public abstract void defineProxyInterceptors();
    
    /**
     * Define JDBC interceptors to collection of plugin interceptor point.
     */
    public abstract void defineJdbcInterceptors();
    
    protected final PluginInterceptorPoint.Builder defineInterceptor(final String classNameOfTarget) {
        if (interceptorPointMap.containsKey(classNameOfTarget)) {
            return interceptorPointMap.get(classNameOfTarget);
        }
        PluginInterceptorPoint.Builder builder = PluginInterceptorPoint.intercept(classNameOfTarget);
        interceptorPointMap.put(classNameOfTarget, builder);
        return builder;
    }
}
