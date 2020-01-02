# December 2019 Release

## Concurrency and Synchronization Support 

Concurrency and Synchronization are supported in this release. Specifically, the following changes are made to JLang compiler:

- `thread.start()` is implemented so that a program can execute multiple threads at the same time.
- `synchronized` code blocks and `synchronized` method signatures are now translated by the compiler frontend.
- Monitor entry and exit functions have been implemented in the runtime to support lock acquisition and release.
- All runtime code is now thread-safe.
- Java condition variables such as `object.wait()` and `object.notify()` are now supported.

An example multi-threading program is located at `tests/misc/Eratosthenes.java` to demonstrate JLang's parallelism. It implements the [Sieve of Eratosthenes](https://en.wikipedia.org/wiki/Sieve_of_Eratosthenes), a paralell algorithm to find all prime numbers up to any given limit.

### Threading

Every Java thread is backed by one native thread after it starts. The main Java thread is running on the main native thread. Java daemon and non-daemon threads are respected by JLang runtime so that the program will only exit if all non-daemon threads terminates. Thread `yield` and `interrupt` are not intended as first-class scheduling primitives. In other words, `interrupt` will not force another thread to yield but the other thread will throw an `InterruptedException`  the next time `wait` or `join` is invoked. This is consistent with the Java Thread API.

### Synchronization

`synchronized` code blocks will correctly release the locked object under any exit condition, including exception or any control flow transfer operation that leaves the code block. Similarly, `synchronized` methods correctly release the locked object after the method returns or an uncaught exception is thrown from the method. Static `synchronized` methods lock the class object of the object instead of the object itself, as in Java.

### Runtime Thread-safety

Unlike HotSpot JVM, JLang does not have a dedicated thread for the runtime. Thus, multiple threads may access run-time data structures concurrently. A global mutex is used to ensure thread-safety in JLang runtime. In future releases, we would like to use more fine-grained locks in the runtime to increase parallelism.

## Bug Fixes

- Fixed incorrect JavaClassInfo struct type.
- Removed unnecessary 2-byte empty character at the end of jstring representation.
- Fixed the initialization of `java.reflect.Method` in runtime.
- Fixed the compilation issue on macOS.
