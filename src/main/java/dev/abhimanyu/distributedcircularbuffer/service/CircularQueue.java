package dev.abhimanyu.distributedcircularbuffer.service;

import java.util.List;

/**
 * circular queue.
 *
 * @author abhimanyu.abhimanyu1
 */
public interface CircularQueue<T> {

    /**
     * inserts given element in the queue. if the queue is full it pops the last
     * oldest element from the queue and then push.
     *
     * @param element
     * @return
     */
    T insert(final T element);

    /**
     * inserts given elements in the queue. if the queue is full it pops the
     * elements.size() the oldest element from the queue and then push.
     *
     * @param elements
     * @return
     */
    List<T> insertAll(final List<T> elements);

    /**
     * returns the number of elements present in the queue at this moment.
     *
     * @return returns length
     */
    int length();

    /**
     * returns capacity of the queue
     *
     * @return queue capacity
     */
    int capacity();

    /**
     * returns remaining capacity of the queue.
     * i.e. the number of elements that can be pushed to the queue without popping any elements
     *
     * @return queue remaining capacity
     */
    int remainingCapacity();

    /**
     * clears the queue(or delete the queue)
     */
    void clear();

    /**
     * returns all elements from the queue.
     * Be careful while calling this method, because the queue can contain very large number of elements
     *
     * @return
     */
    List<T> getAll();

}
