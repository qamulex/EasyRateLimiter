# EasyRateLimiter

As the name says - makes it easy to limit the frequency of requests.

## Installation

**EasyRateLimiter** requires Java 8 or higher.

### Gradle

```groovy
repositories {
    maven { url "https://nexus.lowperformance.dev/repository/maven-public/" }
}

dependencies {
    implementation "me.qamulex:easyratelimiter:3.0"
}
```

### Maven

```xml
<repositories>
    <repository>
        <id>lowperformancedev</id>
        <url>https://nexus.lowperformance.dev/repository/maven-public/</url>
    </repository>
</repositories>

<dependencies>
    <dependency>
        <groupId>me.qamulex</groupId>
        <artifactId>easyratelimiter</artifactId>
        <version>3.0</version>
        <scope>compile</scope>
    </dependency>
</dependencies>
```

## Usage

### Fixed Delay

```java
RateLimiter rateLimiter = RateLimiterBuilder.newBuilder()
        .withDelay(100, TimeUnit.MILLISECONDS)
        .build();

System.out.println(rateLimiter.tryRequest());       // true
System.out.println(rateLimiter.isRequestAllowed()); // false
Thread.sleep(100);
System.out.println(rateLimiter.isRequestAllowed()); // true
```

### Window-Based

```java
// fixed window strategy
RateLimiter fwRateLimiter = RateLimiterBuilder.newBuilder()
        .withWindowSize(1, TimeUnit.SECONDS)
        .withMaxQuota(5)
        .useFixedWindow()
        .build();

for (int i = 0; i < 5; i++) 
    System.out.println(fwRateLimiter.tryRequest()); // true x5
System.out.println(fwRateLimiter.tryRequest());     // false
Thread.sleep(1000);
System.out.println(fwRateLimiter.tryRequest());     // true

// sliding window strategy
RateLimiter swRateLimiter = RateLimiterBuilder.newBuilder()
        .withWindowSize(500, TimeUnit.MILLISECONDS)
        .withMaxQuota(5)
        .useSlidingWindow() // used by default
        .build();

for (int i = 0; i < 5; i++)
    System.out.println(swRateLimiter.tryRequest()); // true x5
System.out.println(swRateLimiter.tryRequest());     // false
Thread.sleep(500);
System.out.println(swRateLimiter.tryRequest());     // true

swRateLimiter.reset();
for (int i = 0; i < 10; i++) {
    System.out.println(swRateLimiter.tryRequest()); // true x10
    Thread.sleep(150);
}
```
### Combination: Fixed Delay + Window-Based

```java
RateLimiter rateLimiter = RateLimiterBuilder.newBuilder()
        .withDelay(100, TimeUnit.MILLISECONDS)
        .withWindowSize(1, TimeUnit.SECONDS)
        .withMaxQuota(5)
        .build();

for (int i = 0; i < 5; i++)
    System.out.println(rateLimiter.tryRequest());   // true x1 then false x4
rateLimiter.reset();
for (int i = 0; i < 5; i++) {
    Thread.sleep(100);
    System.out.println(rateLimiter.tryRequest());   // true x5
}
System.out.println(rateLimiter.isRequestAllowed()); // false
Thread.sleep(600);
System.out.println(rateLimiter.isRequestAllowed()); // true
```

### "Multi-channel" - Map<T, RateLimiter>

```java
RateLimiterMap<String> limiters = RateLimiterBuilder.newBuilder()
        .withWindowSize(1, TimeUnit.SECONDS)
        .withMaxQuota(5)
        .buildMap();

String userId = "...";
if (!limiters.get(userId).tryRequest()) {
    // ban user
}
```

Alternatively, you can provide a custom map implementation as an argument:

```java
RateLimiterMap<String> limiters = RateLimiterBuilder.newBuilder()
        .withWindowSize(1, TimeUnit.SECONDS)
        .withMaxQuota(5)
        .buildMap(new ConcurrentHashMap<>());
```

### ExecutorService with rate limiting

```java
ExecutorService executorService = RateLimiterBuilder.newBuilder()
        .withDelay(50, TimeUnit.MILLISECONDS)
        .withWindowSize(200, TimeUnit.MILLISECONDS)
        .withQuota(2)
        .buildExecutorService();

for (int i = 0; i < 8; i++) {
    executorService.submit(() -> {
        System.out.println(i);
    });
}
```
Output: 
```log
[  0 ms] 0
[ 50 ms] 1
[200 ms] 2
[250 ms] 3
[400 ms] 4
[450 ms] 5
[600 ms] 6
[650 ms] 7
```

## API Overview

### `RateLimiter` Methods
| Method | Description |
|-|-|
| `getTimeUntilNextRequest()` | Returns the estimated time until the next request is allowed. |
| `isRequestAllowed()` | Checks if a request is currently allowed. |
| `tryRequest()` | Attempts to perform a request without blocking. |
| `blockUntilRequestAllowed()` | Blocks until a request is allowed. |
| `blockUntilRequestAllowed(long, TimeUnit)` | Blocks for a maximum time until a request is allowed. |
| `reset()` | Resets the limiter state. |

### `RateLimiterBuilder` Methods
| Method | Description |
|-|-|
| `static newBuilder()` | Creates a new builder instance. |
| `withDelay(long, TimeUnit)` | Configures the delay between requests. |
| `withWindowSize(long, TimeUnit)` | Configures the size of the time window. |
| `withMaxQuota(int)` | Configures the maximum number of requests within window. |
| `useFixedWindow()` | Configures to use fixed time window strategy. |
| `useSlidingWindow()` | Configures to use sliding time window strategy. |
| `enforceThreadSafety(boolean)` | Enables/disables thread-safety enforcement. |
| `build()` | Builds a `RateLimiter` instance. |
| `<K> buildMap()` | Builds a `RateLimiterMap<K>` for multi-channel limiting. |
| `<K> buildMap(Map<K, RateLimiter>)` | Builds a `RateLimiterMap<K>` using a custom map implementation. |
| `buildExecutorService()` | Builds a `RateLimitingExecutorService` with a default single-threaded executor. |
| `buildExecutorService(ExecutorService)` | Builds a `RateLimitingExecutorService` using the provided executor service. |