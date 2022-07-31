package dev.abhimanyu.distributedcircularbuffer.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;

/**
 * @author abhimanyu.abhimanyu1
 */
@Configuration
public class CircularQueueConfig {

    @Value("${queue.capacity:10}")
    private int capacity;

    @Value("${queue.name:test}")
    private String name;

    @Value("${circular.queue.lock:circular_queue_lock}")
    private String lockName;

    public int getCapacity() {
        return capacity;
    }

    public String getName() {
        return name;
    }

    public String getLockName() {
        return lockName;
    }
}
