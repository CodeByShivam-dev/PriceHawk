package com.pricehawk.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.concurrent.*;

/**
 *  AsyncConfig
 * ----------------
 * This configuration class creates a custom thread pool (ExecutorService)
 * for handling multiple API calls or background tasks in parallel.
 *
 * It ensures that the application runs efficiently when multiple users
 * search for prices at the same time (e.g., Amazon, Flipkart, Croma APIs).
 *
 *
 * Why use it?
 * - To avoid blocking the main server thread
 * - To handle concurrent requests faster
 * - To prevent server overload by limiting max threads
 */
@Configuration
public class AsyncConfig
{
    /**
     * 🧩 Bean: apiExecutor
     * --------------------
     * Creates and configures a ThreadPoolExecutor that Spring will manage.
     * Other classes can inject this executor using:
     *    @Autowired @Qualifier("apiExecutor")
     *
     * ThreadPool Settings:
     * - Core pool size: 10 threads always kept ready
     * - Max pool size: 40 threads allowed during heavy load
     * - Queue size: 200 pending tasks allowed (beyond that, new tasks handled by main thread)
     * - Idle timeout: Threads idle for 60 seconds will be removed
     * - Rejection policy: CallerRunsPolicy (runs task in calling thread if pool full)
     */
    @Bean(name = "apiExecutor")
    public Executor apiExecutor()
    {
        ThreadPoolExecutor exec = new ThreadPoolExecutor
                (
                10,        // Minimum number of threads to keep alive
                40,        // ⚙ Maximum number of threads allowed
                60L, TimeUnit.SECONDS, // ⏱Thread idle timeout
                new LinkedBlockingQueue<>(200), // Task queue with 200 capacity
                Executors.defaultThreadFactory(), // 🧵 Default thread naming & creation
                new ThreadPoolExecutor.CallerRunsPolicy() // If pool is full, run task in caller's thread
        );

        // Allow even core threads to time out when idle (saves memory)
        exec.allowCoreThreadTimeOut(true);

        return exec;
    }
}
