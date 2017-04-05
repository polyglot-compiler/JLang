---
title: "Developer Guide"
layout: default
---

Contents
--------
{:.no_toc}

* [table of contents goes here]
{:toc}


Overview
--------

PolyLLVM is built as an extension to the
[Polyglot](https://www.cs.cornell.edu/projects/polyglot/) compiler. As
PolyLLVM is a backend only, it does not extend the parser, or the type
system built into polyglot. PolyLLVM adds compiler passes for desugaring 
and translating the code. PolyLLVM translates directly to LLVM IR using 
the LLVM C API. 


Desugaring Passes
-----------------

There are currently 2 desugaring passes implemented as a part of
PolyLLVM: `StringConversionVisitor` and `MakeCastsExplicitVisitor`.
The `StringConversionVisitor` pass converts String addition to 
a call to the `concat` method, and handles converting expressions 
to strings.  The `MakeCastsExplicit` adds explicit casts where the 
Java language implicitly casts between two types. This includes adding 
casts to generic types and theie erasure. These are polyglot visitors,
so the logic for these transformations is in the extension objects for 
the Java AST nodes.


LLVM API
--------

The LLVM C API is used through the JavaCPP presets bridge. The LLVM
C API was extended to add support for constructing debug info metadata
through the DIBuilder. 

Translation Pass
----------------

The translation pass is also implemented as a polyglot visitor, but the
visitor translator maintains additional state for translation. The main
data structure the translator uses is a map from Java `Node` objects to 
`LLVMValueRef` objects. When translating a Java node, the translation for
sub-nodes is retrieved using the `getTranslation` method. 

The translator stores references to the LLVM Module that is being 
constructed, the LLVM Context, and the LLVM Builder. These are exposed as
public instance variables so translations can easily add the necessary 
declations to the module using the builder and context. The translator 
also exposes utility classes to aid translation.

<!--TODO: Do we want a section on each of these?-->
- LLVMUtils : Contains methods to construct LLVM IR constructs 
such as creating structs, calls, and translating types into LLVM types
- Debug Info : Contains methods to construct debugging information, 
emmiting line number mappings, variable mappings to source code, and
function mapping to source code
- Class Objects : Construct runtime information needed for `instanceof` checks 
- PolyLLVMMangler : Mangles symbol names 
- JL5TypeUtils : Contains methods to erase generic type variables
and intantiations from types and MemberInstances

The translator contains helper functions to translate objects, local variables and arguments, loops, labeled nodes, switch statements, and exceptions.

### Objects ###
The translator exposes helper methods for obtaining field and method layouts as described below. It exposes methods to obtain the indices of fields in an object, and methods in the dispatch vector.  

### Local Variables and Arguments ###

All local variables are allocated on the stack, usings `alloca`, as LLVM has a pass to lift allocated variables to registers.
The translator keeps track of allocations and arguments for functions, to generate prologue code to allocate
stack space for all variables with the correct names. This is done as LLVM code must be in
SSA form, except for memory locations.

### Loops, Labels and Switch ###

The translator maintains a list of Java loop labels, and 
their associated head and end label used for translation. 
The translator exposes functions for entering and exiting 
loops to maintain its internal data structure.

### Exceptions ###
When translating `Try` nodes, the compiler must know the
landing pad to jump to if an exception is raised, the finally
block, and flags to handle returns. There are methods to set a return while in a try block, and methods to enter and exit a try block. 


Object Layout
-------------

Objects are layed out with a pointer to the dispatch vector, followed by the fields, as shown below.
The fields are ordered by class first, and within a class are in lexicographical order.

| Dispatch Vector Pointer |
|:-------------------------:|
| Field 1                 |
| Field 2                 |
|            ⋮            |

The dispatch vectors are layed out with a pointer to the interface table linked list first, followed by
a pointer to the class type info, followed by the methods. The methods are ordered by class first, and
within a class are ordered by visibility. Public methods are first, followed by package, protected, and
lastly private. Within a visibility, the methods are ordered by name in lexicographical order.

| Interface Table Pointer    |
| Type Info Pointer          |
|:--------------------------:|
| Method 1                   |
| Method 2                   |
|            ⋮               |

The interface tables are layed out with a pointer to the next interface table, followed by the
interface name as a null terminated string, follwed by the methods. The methods are ordered in the same order
as in the dispatch vector

|Interface Table Pointer    |
|char* interface_name       |
|:-------------------------:|
|Method 1                   |
|Method 2                   |
|            ⋮              |

Method and Interface Calls
--------------------------

Static method calls are translated by calling the mangled method name with the translated arguments.
To translate a instance method call, the method pointer has to be retrieved from the dispatch vector.
The translator exposes a method `getMethodIndex` to lookup the method in the dispatch vector layout and return the index.
Using the result of this call `methodIndex`, the following pseudocode illustrates method calls.

```
obj->dv[methodIndex](this, arg1, arg2, ...)
```

The first argument is the pointer to the calling object, and the rest of the arguments are translated and passed in order.

Due to the type system in LLVM, to call a superclass method, bitcasts must be introduced to pass the subclass as a superclass, otherwise, they are translated as static calls.

Interface method calls have to look up the method in the correct interface table, so the linked list
of interfaces implemented is iterated over, comparing the interface strings. The java type system
guarantees there will be a match, so when the correct table is found, the method at `methodIndex`
is returned.The following native code is used to look up the method pointer for an interface method call

```
extern "C" {

void* __getInterfaceMethod(jobject* obj, char* interface_string, int methodIndex) {
    it* itable = obj->dv->it;
    while(itable != 0){
        if (strcmp(itable->interface_name, interface_string) == 0) {
            return ((void **) itable)[methodIndex];
        } else {
            itable = itable->next;
        }
    }
    std::abort(); //Should not reach here
}

} // extern "C"
```


InstanceOf
----------

The following native code is used to execute an `instanceof` check at runtime.

```
extern "C" {

bool instanceof(jobject* obj, void* compare_type_id) {
    type_info* type_info = obj->dv->type_info;
    for (int32_t i = 0, end = type_info->size; i < end; ++i)
        if (type_info->super_type_ids[i] == compare_type_id)
            return true;
    return false;
}

} // extern "C"
```

The function accesses the dispatch vector of `obj` to retrieve a table containing all super-classes and super-interfaces, and looks for a match with `compare_type_id`.


Arrays
------

A Java array (e.g., `int[3]`) is implemented as a contiguous region of memory, with one word to hold the length. Arrays must behave as standard Java objects with respect to type information, so for simplicity arrays are implemented as a Java class (see `Array.java` in the `runtime` directory). The catch is that PolyLLVM allocate extra memory for `Array` instances in order to store data elements. This memory is accessed using native methods `clearEntries` and `setObjectEntry`.

Currently, array elements are always one word in size.


Native Code and Mangling
------------------------

We use native C code in many parts of the runtime, including

- Array access and initialization
- Converting command-line arguments to Java strings
- Calling the Java entry point
- Type reflection (e.g., `instanceof`)
- Printing to stdout
- Interface method calls

Wherever possible, native C code should be preferred over handwritten or compiler-generated LLVM IR. Native code currently resides in the `runtime/jni` directory.

For an example, consider the [native code used to implement `instanceof`](#instanceof). When translating a reference to `instanceof` in Java source code, PolyLLVM emits a call to this native code with the correct arguments. The runtime build system is responsible for including the compiled native code in a `runtime.ll` file linked with each program.

To facilitate potential JNI support, we mangle types as specified in the JNI API [here](https://docs.oracle.com/javase/8/docs/technotes/guides/jni/spec/types.html#type_signatures) and [here](https://docs.oracle.com/javase/8/docs/technotes/guides/jni/spec/design.html#resolving_native_method_names). Mangling can get complicated, so when implementing native code that will be called through a Java native method, it is easiest to first compile the Java native method declaration, then inspect the generated LLVM IR to retrieve the correctly mangled method name.


Control Flow Translation
------------------------

The LLVM C API requires that code be emitted as a collection of basic blocks. The key invariant while translating control flow is as follows:

> After traversing an AST subtree, all paths through the corresponding CFG end at a common block, and the instruction builder is positioned at the end of this block.

For example, an if-statement will (1) build the conditional branch, (2) position the builder at the `true` block, (3) recurse into the `consequent` child, (4) position the builder at the `false` block, and (5) recurse into the `alternative` child. After each recursion it adds a branch to the end block (unless there is already a terminating instruction). Finally, it positions the builder at the end block.


Unneeded AST Nodes
------------------

Some AST extensions are unneeded, either because they do not require a translation, or because they can reuse the translation of another extension. Examples are listed below.

- ArrayAccessAssign (uses AssignExt)
- LocalAssign (uses AssignExt)
- FieldAssign (uses AssignExt)
- MethodDecl (uses ProcedureDeclExt)
- Eval (the child translation suffices)
