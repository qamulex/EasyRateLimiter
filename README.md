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
    implementation "me.qamulex:easyratelimiter:1.0.0"
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
        <version>1.0.0</version>
        <scope>compile</scope>
    </dependency>
</dependencies>
```

## Usage

### Single channel

```java
SingleChannelRateLimiter rateLimiter = SingleChannelRateLimiter
    .builder()
    .setMaximumBandwidth(5) // default: 5
    .setTimeRange(1, TimeUnit.SECONDS) // default: 1 second
    .setDelayBetweenRequests(100, TimeUnit.MILLISECONDS) // default: 0 (disabled)
    .useRequestAccumulator(LinkedList::new) // default: LinkedList::new
    .build();

// or

SingleChannelRateLimiter rateLimiter = SingleChannelRateLimiter
    .buildDefault();
```

#### `SingleChannelRateLimiter.Builder` methods
| builder method | meaning |
| - | - |
| `setMaximumBandwidth(int)` | sets maximum number of requests within a time range |
| `setTimeRange(long, TimeUnit)` | sets time range in the specified time unit |
| `setTimeRange(long)` | sets time range in millis |
| `setDelayBetweenRequests(long, TimeUnit)` | sets delay between requests in the specified time unit |
| `setDelayBetweenRequests(long)` | sets delay between requests in millis |
| `useRequestAccumulator(Supplier<List<Long>>)` | sets supplier of the list used to accumulate requests |
| `build()` | builds an instance of `SingleChannelRateLimiter` |

The `SingleChannelRateLimiter` instance can be used via the method `request()` that returns **true** when the request is allowed. Another method called `canRequest()` indicates whether the next call to `request()` will be **true** or not.

### Multi channel

```java
MultiChannelRateLimiter<T> rateLimiter = MultiChannelRateLimiter
    .<T>builder()
    .useRateLimiter( // default: SingleChannelRateLimiter::buildDefault (internally)
            builder -> builder
                    .setMaximumBandwidth(5) // default: 5
                    .setTimeRange(1, TimeUnit.SECONDS) // default: 1 second
                    .setDelayBetweenRequests(100, TimeUnit.MILLISECONDS) // default: 0 (disabled)
                    .useRequestAccumulator(LinkedList::new) // default: LinkedList::new
    )
    .useChannelsMap(HashMap::new) // default: HashMap::new
    .build();

// or

MultiChannelRateLimiter<T> rateLimiter = MultiChannelRateLimiter
    .<T>buildDefault();
```

#### `MultiChannelRateLimiter<T>.Builder` methods
| builder method | meaning |
| - | - |
| `useRateLimiter(UnaryOperator<SingleChannelRateLimiter.Builder>)` | operator used to build separate rate limiting channels |
| `useChannelsMap(Supplier<Map<T, SingleChannelRateLimiter>>)` | sets supplier of the map used to store channels |
| `build()` | builds an instance of `MultiChannelRateLimiter<T>` |

The `MultiChannelRateLimiter<T>` instance can be used via the method `request(T)` that returns **true** when the request is allowed for specified channel or not. Another method called `canRequest(T)` indicates whether the next call to `request(T)` will be **true** or not.