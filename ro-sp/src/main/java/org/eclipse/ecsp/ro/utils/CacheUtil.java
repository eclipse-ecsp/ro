/*
 *
 * ******************************************************************************
 *
 *  Copyright (c) 2023-24 Harman International
 *
 *
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *
 *  you may not use this file except in compliance with the License.
 *
 *  You may obtain a copy of the License at
 *
 *
 *
 *  http://www.apache.org/licenses/LICENSE-2.0
 *
 *
 *
 *  Unless required by applicable law or agreed to in writing, software
 *
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *
 *  See the License for the specific language governing permissions and
 *
 *  limitations under the License.
 *
 *
 *
 *  SPDX-License-Identifier: Apache-2.0
 *
 *  *******************************************************************************
 *
 */

package org.eclipse.ecsp.ro.utils;

import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;
import net.sf.ehcache.Cache;
import net.sf.ehcache.CacheManager;
import net.sf.ehcache.config.CacheConfiguration;
import net.sf.ehcache.config.MemoryUnit;
import net.sf.ehcache.config.PersistenceConfiguration;
import net.sf.ehcache.store.MemoryStoreEvictionPolicy.MemoryStoreEvictionPolicyEnum;
import org.eclipse.ecsp.ro.constants.Constants;
import org.eclipse.ecsp.utils.logger.IgniteLogger;
import org.eclipse.ecsp.utils.logger.IgniteLoggerFactory;
import org.springframework.stereotype.Component;

/**
 * Utility class for Cache operations.
 */
@Component
public class CacheUtil {

    private static final IgniteLogger LOGGER = IgniteLoggerFactory.getLogger(CacheUtil.class);

    CacheManager cacheManager;

    /**
     * Setup cache.
     */
    @PostConstruct
    public void init() {

        // To disable update check every time the cache initializes
        System.setProperty("net.sf.ehcache.skipUpdateCheck", "true");

        createCache(Constants.RO_CACHE_NAME);
    }

    /**
     * Method to create an instance of a cache based on the cache name provided with the
     * pre-configured configuration to be managed by the singleton instance of cacheManager created
     * within for the said cache.
     *
     * @param cacheName cache name to create
     */
    public void createCache(String cacheName) {
        final int Two = 2;
        final int ttl = 3600;
        LOGGER.debug("Initializing the {} cache", cacheName);

        // Create a Cache specifying its configuration.
        CacheConfiguration cacheConfig = new CacheConfiguration();
        cacheConfig
                .name(cacheName)
                .memoryStoreEvictionPolicy(MemoryStoreEvictionPolicyEnum.LRU.name())
                .maxBytesLocalHeap(Two, MemoryUnit.MEGABYTES)
                .persistence(
                        new PersistenceConfiguration().strategy(PersistenceConfiguration.Strategy.NONE))
                .timeToLiveSeconds(ttl)
                .timeToIdleSeconds(ttl)
                .eternal(true);

        net.sf.ehcache.config.Configuration config = new net.sf.ehcache.config.Configuration();
        config.addCache(cacheConfig);

        cacheManager = new CacheManager(config);

        LOGGER.debug("{} cache initialized successfully", cacheName);
    }

    /**
     * Method to fetch an instance of a cache based on the cacheName which is already instantiated and
     * managed by the referenced instance of the cacheManager.
     *
     * @param cacheName cache name
     * @return Cache object
     */
    public Cache getCache(String cacheName) {
        return cacheManager.getCache(cacheName);
    }

    /**
     * Method to shut down the cacheManager and all the dependent instances of cache on it which were
     * initialized at the construction of this class.
     */
    @PreDestroy
    public void closeCache() {

        LOGGER.debug("PreDestroy shutdown hook called for cache");
        cacheManager.shutdown();
        LOGGER.debug("PreDestroy shutdown hook completed successfully");
    }
}
