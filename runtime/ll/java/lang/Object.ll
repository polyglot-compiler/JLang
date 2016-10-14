%dv.Array = type {i8*, i1 (%class.Array*, %class.java.lang.Object*)*, %class.java.lang.Class* (%class.Array*)*, i32 (%class.Array*)*, void (%class.Array*)*, void (%class.Array*)*, %class.java.lang.String* (%class.Array*)*, void (%class.Array*, i64)*, void (%class.Array*, i64, i32)*, void (%class.Array*)*, %class.java.lang.Object* (%class.Array*)*, void (%class.Array*)*}
%class.Array = type {%dv.Array*, i32, i8*}
%dv.java.lang.Object = type {i8*, i1 (%class.java.lang.Object*, %class.java.lang.Object*)*, %class.java.lang.Class* (%class.java.lang.Object*)*, i32 (%class.java.lang.Object*)*, void (%class.java.lang.Object*)*, void (%class.java.lang.Object*)*, %class.java.lang.String* (%class.java.lang.Object*)*, void (%class.java.lang.Object*, i64)*, void (%class.java.lang.Object*, i64, i32)*, void (%class.java.lang.Object*)*, %class.java.lang.Object* (%class.java.lang.Object*)*, void (%class.java.lang.Object*)*}
%class.java.lang.CloneNotSupportedException = type opaque
%class.java.lang.Class = type opaque
%class.java.lang.Object = type {%dv.java.lang.Object*}
%class.java.lang.InterruptedException = type opaque
%class.java.lang.String = type opaque
%class.java.lang.Throwable = type opaque
@_J_dv_5Array = external global %dv.Array
@_J_size_16java.lang.Object = global i64 0
@_J_dv_16java.lang.Object = global %dv.java.lang.Object zeroinitializer
%__ctortype = type { i32, void ()*, i8* }
@llvm.global_ctors = appending global [1 x %__ctortype] [%__ctortype { i32 65535, void ()* @_J_init_16java.lang.Object, i8* null }]
declare i8* @malloc(i64 %size)

define %class.java.lang.Object* @_J_16java.lang.Object_5clone_void(%class.java.lang.Object* %_this) {
%_temp.0 = bitcast i8* null to %class.java.lang.Object*
ret %class.java.lang.Object* %_temp.0
ret %class.java.lang.Object* null
}

define i1 @_J_16java.lang.Object_6equals_16java.lang.Object(%class.java.lang.Object* %_this, %class.java.lang.Object* %other) {
%arg_other = alloca %class.java.lang.Object*, i32 1
store %class.java.lang.Object* %other, %class.java.lang.Object** %arg_other
ret i1 0
ret i1 0
}

define void @_J_16java.lang.Object_8finalize_void(%class.java.lang.Object* %_this) {
ret void
}

define %class.java.lang.Class* @_J_16java.lang.Object_8getClass_void(%class.java.lang.Object* %_this) {
%_temp.1 = bitcast i8* null to %class.java.lang.Class*
ret %class.java.lang.Class* %_temp.1
ret %class.java.lang.Class* null
}

define i32 @_J_16java.lang.Object_8hashCode_void(%class.java.lang.Object* %_this) {
ret i32 0
ret i32 0
}

define void @_J_16java.lang.Object_6notify_void(%class.java.lang.Object* %_this) {
ret void
}

define void @_J_16java.lang.Object_9notifyAll_void(%class.java.lang.Object* %_this) {
ret void
}

define %class.java.lang.String* @_J_16java.lang.Object_8toString_void(%class.java.lang.Object* %_this) {
%_temp.2 = bitcast i8* null to %class.java.lang.String*
ret %class.java.lang.String* %_temp.2
ret %class.java.lang.String* null
}

define void @_J_16java.lang.Object_4wait_i64(%class.java.lang.Object* %_this, i64 %x) {
%arg_x = alloca i64, i32 1
store i64 %x, i64* %arg_x
ret void
}

define void @_J_16java.lang.Object_4wait_i64_i32(%class.java.lang.Object* %_this, i64 %x, i32 %y) {
%arg_x = alloca i64, i32 1
store i64 %x, i64* %arg_x
%arg_y = alloca i32, i32 1
store i32 %y, i32* %arg_y
ret void
}

define void @_J_16java.lang.Object_4wait_void(%class.java.lang.Object* %_this) {
ret void
}

define void @_J_16java.lang.Object__constructor__void(%class.java.lang.Object* %_this) {
ret void
}

define void @_J_init_16java.lang.Object() {
%_temp.3 = getelementptr %dv.java.lang.Object, %dv.java.lang.Object* @_J_dv_16java.lang.Object, i32 0, i32 1
store i1 (%class.java.lang.Object*, %class.java.lang.Object*)* @_J_16java.lang.Object_6equals_16java.lang.Object, i1 (%class.java.lang.Object*, %class.java.lang.Object*)** %_temp.3
%_temp.4 = getelementptr %dv.java.lang.Object, %dv.java.lang.Object* @_J_dv_16java.lang.Object, i32 0, i32 2
store %class.java.lang.Class* (%class.java.lang.Object*)* @_J_16java.lang.Object_8getClass_void, %class.java.lang.Class* (%class.java.lang.Object*)** %_temp.4
%_temp.5 = getelementptr %dv.java.lang.Object, %dv.java.lang.Object* @_J_dv_16java.lang.Object, i32 0, i32 3
store i32 (%class.java.lang.Object*)* @_J_16java.lang.Object_8hashCode_void, i32 (%class.java.lang.Object*)** %_temp.5
%_temp.6 = getelementptr %dv.java.lang.Object, %dv.java.lang.Object* @_J_dv_16java.lang.Object, i32 0, i32 4
store void (%class.java.lang.Object*)* @_J_16java.lang.Object_6notify_void, void (%class.java.lang.Object*)** %_temp.6
%_temp.7 = getelementptr %dv.java.lang.Object, %dv.java.lang.Object* @_J_dv_16java.lang.Object, i32 0, i32 5
store void (%class.java.lang.Object*)* @_J_16java.lang.Object_9notifyAll_void, void (%class.java.lang.Object*)** %_temp.7
%_temp.8 = getelementptr %dv.java.lang.Object, %dv.java.lang.Object* @_J_dv_16java.lang.Object, i32 0, i32 6
store %class.java.lang.String* (%class.java.lang.Object*)* @_J_16java.lang.Object_8toString_void, %class.java.lang.String* (%class.java.lang.Object*)** %_temp.8
%_temp.9 = getelementptr %dv.java.lang.Object, %dv.java.lang.Object* @_J_dv_16java.lang.Object, i32 0, i32 7
store void (%class.java.lang.Object*, i64)* @_J_16java.lang.Object_4wait_i64, void (%class.java.lang.Object*, i64)** %_temp.9
%_temp.10 = getelementptr %dv.java.lang.Object, %dv.java.lang.Object* @_J_dv_16java.lang.Object, i32 0, i32 8
store void (%class.java.lang.Object*, i64, i32)* @_J_16java.lang.Object_4wait_i64_i32, void (%class.java.lang.Object*, i64, i32)** %_temp.10
%_temp.11 = getelementptr %dv.java.lang.Object, %dv.java.lang.Object* @_J_dv_16java.lang.Object, i32 0, i32 9
store void (%class.java.lang.Object*)* @_J_16java.lang.Object_4wait_void, void (%class.java.lang.Object*)** %_temp.11
%_temp.12 = getelementptr %dv.java.lang.Object, %dv.java.lang.Object* @_J_dv_16java.lang.Object, i32 0, i32 10
store %class.java.lang.Object* (%class.java.lang.Object*)* @_J_16java.lang.Object_5clone_void, %class.java.lang.Object* (%class.java.lang.Object*)** %_temp.12
%_temp.13 = getelementptr %dv.java.lang.Object, %dv.java.lang.Object* @_J_dv_16java.lang.Object, i32 0, i32 11
store void (%class.java.lang.Object*)* @_J_16java.lang.Object_8finalize_void, void (%class.java.lang.Object*)** %_temp.13
ret void
}

