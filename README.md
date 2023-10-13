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
    implementation "me.qamulex:easyratelimiter:2.0.1"
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
        <version>2.0.1</version>
        <scope>compile</scope>
    </dependency>
</dependencies>
```

## Basic usage

```java
RateLimiter delayLimiter = RateLimiterBuilder.newBuilder()
        .setMaximumBandwidth(1)
        .setAffectedTimeRange(100, TimeUnit.MILLISECONDS)
        .setDelayBetweenRequests(0)
        .build();

RateLimiter bandwidthLimiter = RateLimiterBuilder.newBuilder()
        .setMaximumBandwidth(5)
        .setAffectedTimeRange(1, TimeUnit.SECONDS)
        .setDelayBetweenRequests(0)
        .build();

RateLimiter bandwidthLimiterWithDelay = RateLimiterBuilder.newBuilder()
        .setMaximumBandwidth(5)
        .setAffectedTimeRange(1, TimeUnit.SECONDS)
        .setDelayBetweenRequests(100, TimeUnit.MILLISECONDS)
        .build();
```

### `RateLimiterBuilder` methods
| builder method | meaning |
| - | - |
| `useClock(Clock)` | time source used for the limiter calculations |
| `setMaximumBandwidth(int)` | sets maximum number of requests within a time range |
| `setAffectedTimeRange(long, TimeUnit)` | sets affected time range in the specified time unit |
| `setAffectedTimeRange(long)` | sets affected time range in millis |
| `setDelayBetweenRequests(long, TimeUnit)` | sets delay between requests in the specified time unit |
| `setDelayBetweenRequests(long)` | sets delay between requests in millis |
| `useCapturedTimestampsStorage(Supplier<List<Long>>)` | sets supplier of the list used to capture request timestamps |
| `build()` | builds an instance of `RateLimiter` |

The `RateLimiter` instance can be used via the method `request()` that returns **true** when the request is allowed. Another method called `canRequest()` indicates whether the next call to `request()` will be **true** or not.

## Multi-channel usage

Multi-channel variant of rate limiter can be built using method `RateLimiterBuilder#buildMap()`:

```java
Map<T, RateLimiter> limiters = RateLimiterBuilder.newBuilder()
        // ...
        .buildMap();
```

Created map then can be used for rate limiting like this:

```java
Object someObject = ...;

boolean requestIsAllowed = limiters.get(someObject).request();
```