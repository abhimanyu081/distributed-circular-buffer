package dev.abhimanyu.distributedcircularbuffer.service;

import dev.abhimanyu.distributedcircularbuffer.config.CircularQueueConfig;
import dev.abhimanyu.distributedcircularbuffer.config.RedisConfig;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.integration.redis.util.RedisLockRegistry;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(SpringExtension.class)
@ContextConfiguration(classes = {RedisConfig.class, CircularQueueConfig.class})
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
//@DirtiesContext(classMode = ClassMode.BEFORE_CLASS)
public class CircularQueueTest {

    private static final int    QUEUE_CAPACITY = 10;
    private static final String QUEUE_NAME     = "test_queue";

    @Autowired
    private RedisTemplate<String, String> redisTemplate;

    @Autowired
    private CircularQueueConfig circularQueueConfig;

    @Autowired
    private RedisLockRegistry redisLockRegistry;

    private CircularQueue<String> queue;

    @BeforeAll
    public void init() {
        this.queue = new RedisListCircularQueue<String>(redisTemplate, circularQueueConfig, redisLockRegistry);
    }

    @Test
    public void testQueueLength() {
        queue.clear();
        queue.insertAll(List.of("a", "b", "c", "d"));
        int len = queue.length();
        assertEquals(len, 4);
    }

    @Test
    public void shouldNotHaveMoreThanCapacityElements_WhenInsertingAfterQueueIsFull() {
        queue.clear();
        List<String> elements = getRandomStrings(QUEUE_CAPACITY + 5);
        queue.insertAll(elements);
        assertEquals(queue.length(), QUEUE_CAPACITY);
    }

    @Test
    public void shouldNotHaveMoreThanCapacityElements_WhenInsertingQueueFull() {
        queue.clear();
        List<String> elements = getRandomStrings(QUEUE_CAPACITY);
        queue.insertAll(elements);
        queue.insertAll(getRandomStrings(2));
        assertEquals(queue.length(), QUEUE_CAPACITY);
    }

    @Test
    public void shouldInsertSingleElement() {
        queue.clear();
        queue.insert(getRandomStrings(1).get(0));
        assertDoesNotThrow(() -> {
        });
    }

    @Test
    public void shouldInsertManyElements() {
        queue.clear();
        queue.insertAll(getRandomStrings(100));
        assertDoesNotThrow(() -> {
        });
    }

    @Test
    public void shouldInsertMoreThanQueueCapacity() {
        queue.clear();
        queue.insertAll(getRandomStrings(2 * QUEUE_CAPACITY));
        assertDoesNotThrow(() -> {
        });
    }

    @Test
    public void shouldHaveLastCapacityNumberOfElementsInTheQueue() {

        //0,1,2,3,4,5,6,7,8,9,10,11,12,13,14,15,16
        List<String> randomString = getRandomStrings(QUEUE_CAPACITY + 7);
        List<String> expectedResult = randomString.subList(7, randomString.size());
        queue.clear();
        queue.insertAll(randomString);

        List<String> fromQResult = queue.getAll();

        assertIterableEquals(expectedResult, fromQResult);
    }

    @Test
    public void shouldMaintainCapacityWhenMultipleCallsToInsert() {
        //0,1,2,3,4,5,6,7,8,9,10,11,12,13,14,15,16

        queue.clear();

        for (int i = 0; i < 10; i++) {
            queue.insertAll(getRandomStrings(getRandomNumber(1, 5)));

        }

        assertEquals(queue.length(), QUEUE_CAPACITY);
    }

    @Test
    public void testInsertionWithConcurrency() throws InterruptedException {
        queue.clear();
        queue.insertAll(getRandomStrings(QUEUE_CAPACITY));

        int numberOfThreads = 5;
        ExecutorService service = Executors.newFixedThreadPool(10);
        CountDownLatch latch = new CountDownLatch(numberOfThreads);
        for (int i = 0; i < numberOfThreads; i++) {
            int finalI = i;
            service.submit(() -> {
                queue.insertAll(getRandomStrings(getRandomNumber(1, 10)));
                latch.countDown();
            });
        }
        latch.await();
        assertEquals(queue.length(), QUEUE_CAPACITY);
    }

    public int getRandomNumber(int min, int max) {
        Random random = new Random();
        return random.nextInt(max - min) + min;
    }

    public List<String> getRandomStrings(int count) {

        List<String> randomStrings = new ArrayList<>(count);

        int leftLimit = 97; // letter 'a'
        int rightLimit = 122; // letter 'z'
        int targetStringLength = 1;
        Random random = new Random();

        for (int i = 0; i < count; i++) {

            String generatedString = random.ints(leftLimit, rightLimit + 1).limit(targetStringLength)
                    .collect(StringBuilder::new, StringBuilder::appendCodePoint, StringBuilder::append).toString();
            randomStrings.add(generatedString);

        }

        return randomStrings;
    }
}
