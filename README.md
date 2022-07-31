# Distributed Queue Using Redis
## Distributed queue has the following features
1. insert
2. insertAll
3. get length
4. get capacity
5. get remaining capacity

## Atomic and concurrent insert/insertAll operations

Distributed Queue here means that multiple instances of an application can use/modify same queue.
But a single queue is not distributed over multiple servers, and it remains on a single server only.

But we can create multiple queue on multiple servers using sharding based on queue name. [TODO]

### While implementing distributed queue the only challenge was how to insert elements in the queue atomically.
Insertion in queue consists of below 3 steps.
1. check length of queue, if it is full or not yet.
2. if push new element(s)
3. pop elements from the queue if the newly inserted elements exceeding the queue capacity.

### Approaches that were considered.
#### Using Redis transaction using MULTI, EXEC operation.
Limitation --> Redis uses same connection for all the write operations in a single transaction but it uses a different connection for read operations.
because of this the check length operation did not return correct length of the queue on concurrent insertion in the queue.

#### Using Redis LUA script
Limitation --> When redis executes a lua scripts it pauses all other operations that is going on right now. So due to this queue operations may become bottleneck for other applications using the same instance of redis.

#### using Distributed Redis Locks (RedisLockRegistry from Spring Integration)
Acquired lock before doing any operations then performed all above 3 steps while holding the lock.

#### Points to think about.
Even though lua scripts block other operations, can this approach be faster than the Distributed lock approach. (If the script execution time considered to be very short and with a timeout configured)? 

