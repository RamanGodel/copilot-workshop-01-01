package com.example.workshop.config;

import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

import java.util.concurrent.Executor;

/**
 * Configuration for asynchronous task execution.
 */
@Slf4j
@Configuration
public class AsyncConfig {

    /**
     * Configures the thread pool for async tasks.
     * This executor is used for async operations like parallel rate fetching from multiple providers.
     */
    @Bean(name = "taskExecutor")
    public Executor taskExecutor() {
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
        
        // Core pool size: minimum number of threads to keep alive
        executor.setCorePoolSize(5);
        
        // Max pool size: maximum number of threads
        executor.setMaxPoolSize(10);
        
        // Queue capacity: number of tasks that can be queued
        executor.setQueueCapacity(25);
        
        // Thread name prefix for easier debugging
        executor.setThreadNamePrefix("async-rate-");
        
        // Wait for tasks to complete on shutdown
        executor.setWaitForTasksToCompleteOnShutdown(true);
        
        // Await termination time in seconds
        executor.setAwaitTerminationSeconds(30);
        
        executor.initialize();
        
        log.info("Configured async task executor with core pool size: {}, max pool size: {}, queue capacity: {}",
                executor.getCorePoolSize(), executor.getMaxPoolSize(), executor.getQueueCapacity());
        
        return executor;
    }
}
