---
title: "Thread Proposal"
layout: default
---

Contents
--------
{:.no_toc}

* [table of contents goes here]
{:toc}


Introduction
------------

Java provides library support for writing multi-threaded applications, mainly through the `Thread` class, and `Object` methods for synchronization. This proposal details implementing threading support in PolyLLVM using the `pthread` library.

Object Header
-------------

Java objects must be updated to include a mutex and condition variable. As all objects require these, but most do not use them, the object header should not contain both the mutex and condition variable, but a pointer to a struct containing them. Upon first use, the mutex and condition variable can be created. The proposed struct containing the mutex and condition variable is as follows:

```
struct sync_vars {
	pthread_mutex_t* mutex;
	pthread_cond_t *condition_variable;
};
```

The proposed object header then needs a single pointer the the above struct:

```
struct jobj_header {
	dv* dispatch_vector;
	struct sync_vars* synchronization_vars;
};
```
The translation for new object creation will set the `synchronization_vars` pointer to null initally. Checking if the mutex or condition variable is null, and setting them requires obtaining a global mutex. If the `synchronization_vars` pointer is null, it is allocated, and the mutex and condition variable are constructed. The mutex must be constructed using the `PTHREAD_MUTEX_RECURSIVE` type. The global lock is released before the object lock is used. 

<!-- Possible to store mutexes in global hashtable -->

Word Tearing
------------

The LLVM memory model makes non-atomic memory races undefined, so it cannot introduce data races from the source program, thus store widening is dissalowed. This matches the Java Specification.

Synchronization Blocks and Methods
----------------------------------

Synchronization blocks must be translated within a try-finally block. If execution of the synchronized block is completed, either abruptly or normally, the lock must be released. The lock is obtained with the `pthread_mutex_lock` function call on the object's mutex, and the lock is released with the `pthread_mutex_unlock` function call. 

Waiting and Notification
------------------------

The `Object.wait` method can be implemented using the `pthread_cond_wait` function, the `Object.notify` method can be implemented using the `pthread_cond_signal`. and the `Object.notifyAll` method can be implemented using the function `pthread_cond_broadcast`.

To wait for a specified amount of time, the `pthread_cond_timedwait` function can be used. 

These methods need to check if the mutex is held, which can be done using the `pthread_mutex_trylock` function.

Interrupts
----------

TODO

Ordering Constraints
--------------------

TODO

Final Fields
------------

TODO

