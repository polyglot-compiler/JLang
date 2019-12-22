# December 2019 Release

## Concurrency and Synchronization Support 

Concurrency and Synchronization are supported in this release. Specifically, the following changes are made to JLang compiler:

- `thread.start()` is implemented so that a program could run multiple threads at the same time.
- `synchronized` code block and `synchronized` method signature are translated in the compiler frontend.
- A monitor is implemented in the runtime to acquire and release a lock.
- All runtime code is modified to be thread-safe.
- Java condition variables such as `object.wait()` and `object.notify()` are now supported.

An example multi-threading program is located at `tests/misc/Eratosthenes.java` to demonstrate JLang's parallelism. It implements [Sieve of Eratosthenes](https://en.wikipedia.org/wiki/Sieve_of_Eratosthenes), a paralell algorithm to find all prime numbers up to any given limit.

### Threading

Every Java thread is backed by one native thread after it starts. The main Java thread is running on the main native thread. Java daemon and non-daemon threads are respected by JLang runtime so that the program will only exit if all non-daemon threads terminates. Thread `yield` and `interrupt` are also implemented with a caveat that the current threading model is cooperative. In other words, `interrupt` could not force another thread to yield but the other thread will throw an `InterruptedException`  the next time `wait` or `join` is invoked.

### Synchronization

`synchronized` code block could correctly unlock the locked object in any circumstance, including exception or any control flow transfer operation. Similarly, `synchronized` method correctly unlock the locked object after the method returns or an uncaught exception is thrown from the method. Static `synchronized` method locks the class object of the object instead of the object itself.

### Runtime Thread-safety

Unlike HotSpot JVM, JLang does not have a dedicated thread for the runtime. Thus, multiple threads could access runtime data structures at the same time. A global mutex is used to ensure the thread-safety in JLang runtime. In the future release, we would consider an optimization to use more fine-grained locks to increase parallelism.

## Bug Fix

- Fix JavaClassInfo struct type.
- Remove 2-byte empty character at the end of jstring.
- Fix the initialization of `java.reflect.Method` in runtime.
- Fix the compilation issue on macOS.