package dev.abhimanyu.distributedcircularbuffer.service;

import dev.abhimanyu.distributedcircularbuffer.config.CircularQueueConfig;
import dev.abhimanyu.distributedcircularbuffer.exception.CircularQueueInsertionFailedException;
import dev.abhimanyu.distributedcircularbuffer.exception.CircularQueueLockingFailedException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.ListOperations;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.integration.redis.util.RedisLockRegistry;
import org.springframework.retry.annotation.Backoff;
import org.springframework.retry.annotation.Retryable;
import org.springframework.util.CollectionUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.Lock;

@Slf4j
public class RedisListCircularQueue<T> implements CircularQueue<T> {

    private final String                    queueName;
    private final int                       capacity;
    private final RedisTemplate<String, T>  redisTemplate;
    private final ListOperations<String, T> listOps;
    private final CircularQueueConfig       circularQueueLockConfig;
    private final RedisLockRegistry         redisLockRegistry;

    public RedisListCircularQueue(RedisTemplate<String, T> redisTemplate, CircularQueueConfig circularQueueLockConfig,
            RedisLockRegistry redisLockRegistry) {
        this.queueName = circularQueueLockConfig.getName();
        this.capacity = circularQueueLockConfig.getCapacity();
        this.redisTemplate = redisTemplate;
        this.listOps = redisTemplate.opsForList();
        this.circularQueueLockConfig = circularQueueLockConfig;
        this.redisLockRegistry = redisLockRegistry;
    }

    @Override
    public T insert(T element) {
        List<T> result = this.insertAll(List.of(element));
        return CollectionUtils.isEmpty(result) ? null : result.get(0);

    }

    @Override
    @Retryable(value = CircularQueueLockingFailedException.class, maxAttemptsExpression = "${queue.insert.retry.maxAttempts}", backoff = @Backoff(delayExpression = "${queue.insert.retry.maxDelay}"))
    public List<T> insertAll(List<T> elements) {
        log.debug("inserting in queue={},elements={}", queueName, elements);

        Lock lock = null;
        try {
            lock = redisLockRegistry.obtain(circularQueueLockConfig.getLockName());
        } catch (Exception e) {
            log.error("Error while obtaining lock with name={}", circularQueueLockConfig.getLockName(), e);
        }
        try {
            if (lock.tryLock(2, TimeUnit.SECONDS)) {
                return insertAllWithoutLock(elements);
            } else {
                throw new CircularQueueLockingFailedException("Could not acquire lock");
            }
        } catch (CircularQueueLockingFailedException e) {
            log.error("Error while acquiring lock", e);
        } catch (Exception e) {
            log.error("Error while acquiring lock", e);
            throw new CircularQueueInsertionFailedException("Insertion failed");
        } finally {
            lock.unlock();
        }
        return new ArrayList<>();
    }

    private List<T> insertAllWithoutLock(List<T> elements) {

        int size = length();
        int remainingCapacity = capacity - size;
        int numberOfElementsToBePopped = 0;

        if (remainingCapacity < elements.size()) {
            numberOfElementsToBePopped = elements.size() - remainingCapacity;
        }

        log.debug("queue={},pushCount={},popCount={}", queueName, elements.size(), numberOfElementsToBePopped);
        //do not change sequence of push and pop operations
        listOps.rightPushAll(queueName, elements);

        if (numberOfElementsToBePopped > 0) {
            return listOps.leftPop(queueName, numberOfElementsToBePopped);
        }

        return new ArrayList<>();
    }

    @Override
    public int length() {
        Long size = listOps.size(queueName);
        return size == null ? 0 : size.intValue();
    }

    /**
     * returns capacity of the queue
     *
     * @return queue capacity
     */
    @Override
    public int capacity() {
        return circularQueueLockConfig.getCapacity();
    }

    /**
     * returns remaining capacity of the queue.
     * i.e. the number of elements that can be pushed to the queue without popping any elements
     *
     * @return queue remaining capacity
     */
    @Override
    public int remainingCapacity() {
        return 0;
    }

    @Override
    public void clear() {
        redisTemplate.delete(queueName);

    }

    /**
     * @return
     */
    @Override
    public List<T> getAll() {
        return listOps.range(queueName, 0, -1);
    }

}
