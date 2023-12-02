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
    implementation "me.qamulex:easyratelimiter:2.0.2"
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
        <version>2.0.2</version>
        <scope>compile</scope>
    </dependency>
</dependencies>
```

## Basic usage

```java
// delay limiter
RateLimiter delayLimiter = RateLimiterBuilder.newBuilder()
        .setMaximumBandwidth(1)
        .setAffectedTimeRange(100, TimeUnit.MILLISECONDS)
        .setDelayBetweenRequests(0)
        .build();

System.out.println(delayLimiter.request());    // true
System.out.println(delayLimiter.canRequest()); // false
```
```java
// bandwidth limiter
RateLimiter bandwidthLimiter = RateLimiterBuilder.newBuilder()
        .setMaximumBandwidth(5)
        .setAffectedTimeRange(1, TimeUnit.SECONDS)
        .setDelayBetweenRequests(0)
        .build();

for (int i = 0; i < 5; i++)
    System.out.println(bandwidthLimiter.request()); // true x5
System.out.println(bandwidthLimiter.canRequest());  // false
```
```java
// bandwidth limiter with delay between requests
RateLimiter bandwidthLimiterWithDelay = RateLimiterBuilder.newBuilder()
        .setMaximumBandwidth(5)
        .setAffectedTimeRange(1, TimeUnit.SECONDS)
        .setDelayBetweenRequests(100, TimeUnit.MILLISECONDS)
        .build();

for (int i = 0; i < 5; i++)
    System.out.println(bandwidthLimiterWithDelay.request()); // true x1 then false x4
System.out.println(bandwidthLimiterWithDelay.canRequest());  // false
Thread.sleep(100);
System.out.println(bandwidthLimiterWithDelay.canRequest());  // true
```

### `RateLimiterBuilder` methods
| builder method | meaning |
| - | - |
| `static newBuilder()` | constructs a new RateLimiterBuilder instance with default parameters |
| `useClock(Clock)` | time source used for the limiter calculations |
| `setMaximumBandwidth(int)` | sets maximum number of requests within a time range |
| `setAffectedTimeRange(long)` | sets affected time range in millis |
| `setAffectedTimeRange(long, TimeUnit)` | sets affected time range in the specified time unit |
| `setAffectedTimeRange(Duration)` | sets affected time range from the specified duration |
| `setDelayBetweenRequests(long)` | sets delay between requests in millis |
| `setDelayBetweenRequests(long, TimeUnit)` | sets delay between requests in the specified time unit |
| `setDelayBetweenRequests(Duration)` | sets delay between requests from the specified duration |
| `useCapturedTimestampsStorage(Supplier<List<Long>>)` | sets supplier of the list used to capture request timestamps |
| `build()` | builds an instance of `RateLimiter` |
| `buildMap(Map<K, RateLimiter>)` | builds an instance of `RateLimiterMap<K>` with the specified underlying map instance |
| `buildMap()` | builds an instance of `RateLimiterMap<K>` with `HashMap<K, RateLimiter>` as underlying map instance |

The `RateLimiter` instance can be used via the method `request()` that returns **true** when the request is allowed. Another method called `canRequest()` indicates whether the next call to `request()` will be **true** or not.

## Multi-channel usage

Multi-channel variant of rate limiter can be built using method `RateLimiterBuilder#buildMap()`:

```java
RateLimiterMap<K> limiters = RateLimiterBuilder.newBuilder()
        // ...
        .buildMap();
```

Created map then can be used for rate limiting like this:

```java
Object someObject = ...;

boolean requestIsAllowed = limiters.get(someObject).request();
```