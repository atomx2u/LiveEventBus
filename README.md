# LiveEventBus

An Android event bus based on LiveData without reflection.

Compared with traditional LiveData eventbus implementation using reflection API, this library just uses simple plain LiveData API to achive stick feature. And also, this library is null-safety and thread-safety.

## Getting Start

In your root `build.gradle`:

```groovy
repositories {
    ...
    maven { url "https://jitpack.io" }
}
```

In your module `build.gradle`

```groovy
dependencies {
    ...
    implementation 'com.github.atomx2u:liveeventbus:0.1'
}
```

## Usgae

#### Step 1. Define an event

```kotlin
class YourEvent(val msg: Any) : LiveEventBus.Event
```

#### Step 2. Observe the event

```kotlin
LiveEventBus.with(YourEvent::class).observe(lifecycleOwner = this) { changed ->
    TODO("Your code")
}
```

#### Step 3.  Emit an event

```kotlin
LiveEventBus.with(YourEvent::class).emit(
    YourEvent(Any())
)
```

#### Advanced usage

**Sticky event**

```kotlin
LiveEventBus.with(YourEvent::class).observe(lifecycleOwner = this, sticky = true) { changed ->
    TODO("Your code")
}
```

