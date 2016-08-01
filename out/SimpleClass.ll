%class.SimpleClass = type {%dv.SimpleClass*, i32, i16}
%dv.java.lang.Object = type {i8*, i1 (%class.java.lang.Object*, %class.java.lang.Object*)*, %class.java.lang.Class* (%class.java.lang.Object*)*, i32 (%class.java.lang.Object*)*, void (%class.java.lang.Object*)*, void (%class.java.lang.Object*)*, %class.java.lang.String* (%class.java.lang.Object*)*, void (%class.java.lang.Object*, i64)*, void (%class.java.lang.Object*, i64, i32)*, void (%class.java.lang.Object*)*, %class.java.lang.Object* (%class.java.lang.Object*)*, void (%class.java.lang.Object*)*}
%dv.SimpleClass = type {i8*, i1 (%class.SimpleClass*, %class.java.lang.Object*)*, %class.java.lang.Class* (%class.SimpleClass*)*, i32 (%class.SimpleClass*)*, void (%class.SimpleClass*)*, void (%class.SimpleClass*)*, %class.java.lang.String* (%class.SimpleClass*)*, void (%class.SimpleClass*, i64)*, void (%class.SimpleClass*, i64, i32)*, void (%class.SimpleClass*)*, %class.java.lang.Object* (%class.SimpleClass*)*, void (%class.SimpleClass*)*, i32 (%class.SimpleClass*)*, i32 (%class.SimpleClass*)*}
%class.java.lang.Class = type opaque
%class.java.lang.Object = type {%dv.java.lang.Object*}
%class.java.lang.String = type opaque
@_J_size_11SimpleClass = global i64 0
@_J_dv_11SimpleClass = global %dv.SimpleClass zeroinitializer
@_J_size_16java.lang.Object = external global i64
@_J_dv_16java.lang.Object = external global %dv.java.lang.Object
%__ctortype = type { i32, void ()*, i8* }
@llvm.global_ctors = appending global [1 x %__ctortype] [%__ctortype { i32 65535, void ()* @_J_init_11SimpleClass, i8* null }]
declare void @_J_11SimpleClass_5print_i32(i32 %i)

declare i8* @malloc(i64 %size)

declare void @_J_init_16java.lang.Object()

define i32 @_J_11SimpleClass_6method_void(%class.SimpleClass* %_this) {
%flat$1 = alloca i16, i32 1
%flat$2 = alloca i32, i32 1
%flat$0 = alloca i32, i32 1
%_dvDoublePtrResult.0 = getelementptr %class.SimpleClass, %class.SimpleClass* %_this, i32 0, i32 0
%_dvPtrValue.0 = load %dv.SimpleClass*, %dv.SimpleClass** %_dvDoublePtrResult.0
%_temp.0 = getelementptr %dv.SimpleClass, %dv.SimpleClass* %_dvPtrValue.0, i32 0, i32 13
%_temp.1 = load i32 (%class.SimpleClass*)*, i32 (%class.SimpleClass*)** %_temp.0
%_temp.2 = call i32 %_temp.1(%class.SimpleClass* %_this)
%_temp.3 = getelementptr %class.SimpleClass, %class.SimpleClass* %_this, i32 0, i32 1
%_temp.4 = load i32, i32* %_temp.3
store i32 %_temp.4, i32* %flat$0
%_temp.5 = getelementptr %class.SimpleClass, %class.SimpleClass* %_this, i32 0, i32 2
%_temp.6 = load i16, i16* %_temp.5
store i16 %_temp.6, i16* %flat$1
%_temp.7 = load i32, i32* %flat$0
%_temp.61 = alloca i32, i32 1
store i32 %_temp.7, i32* %_temp.61
%_temp.8 = load i16, i16* %flat$1
%_temp.9 = zext i16 %_temp.8 to i32
%_temp.62 = load i32, i32* %_temp.61
%_temp.10 = add i32 %_temp.62, %_temp.9
store i32 %_temp.10, i32* %flat$2
%_temp.11 = load i32, i32* %flat$2
ret i32 %_temp.11
}

define i32 @_J_11SimpleClass_13privateMethod_void(%class.SimpleClass* %_this) {
%_temp.14 = getelementptr %class.SimpleClass, %class.SimpleClass* %_this, i32 0, i32 2
store i16 65, i16* %_temp.14
ret i32 23
}

define void @_J_init_11SimpleClass() {
call void @_J_init_16java.lang.Object()
%_temp.15 = getelementptr %dv.java.lang.Object, %dv.java.lang.Object* @_J_dv_16java.lang.Object, i32 0, i32 1
%_temp.17 = load i1 (%class.java.lang.Object*, %class.java.lang.Object*)*, i1 (%class.java.lang.Object*, %class.java.lang.Object*)** %_temp.15
%_temp.18 = bitcast i1 (%class.java.lang.Object*, %class.java.lang.Object*)* %_temp.17 to i1 (%class.SimpleClass*, %class.java.lang.Object*)*
%_temp.16 = getelementptr %dv.SimpleClass, %dv.SimpleClass* @_J_dv_11SimpleClass, i32 0, i32 1
store i1 (%class.SimpleClass*, %class.java.lang.Object*)* %_temp.18, i1 (%class.SimpleClass*, %class.java.lang.Object*)** %_temp.16
%_temp.19 = getelementptr %dv.java.lang.Object, %dv.java.lang.Object* @_J_dv_16java.lang.Object, i32 0, i32 2
%_temp.21 = load %class.java.lang.Class* (%class.java.lang.Object*)*, %class.java.lang.Class* (%class.java.lang.Object*)** %_temp.19
%_temp.22 = bitcast %class.java.lang.Class* (%class.java.lang.Object*)* %_temp.21 to %class.java.lang.Class* (%class.SimpleClass*)*
%_temp.20 = getelementptr %dv.SimpleClass, %dv.SimpleClass* @_J_dv_11SimpleClass, i32 0, i32 2
store %class.java.lang.Class* (%class.SimpleClass*)* %_temp.22, %class.java.lang.Class* (%class.SimpleClass*)** %_temp.20
%_temp.23 = getelementptr %dv.java.lang.Object, %dv.java.lang.Object* @_J_dv_16java.lang.Object, i32 0, i32 3
%_temp.25 = load i32 (%class.java.lang.Object*)*, i32 (%class.java.lang.Object*)** %_temp.23
%_temp.26 = bitcast i32 (%class.java.lang.Object*)* %_temp.25 to i32 (%class.SimpleClass*)*
%_temp.24 = getelementptr %dv.SimpleClass, %dv.SimpleClass* @_J_dv_11SimpleClass, i32 0, i32 3
store i32 (%class.SimpleClass*)* %_temp.26, i32 (%class.SimpleClass*)** %_temp.24
%_temp.27 = getelementptr %dv.java.lang.Object, %dv.java.lang.Object* @_J_dv_16java.lang.Object, i32 0, i32 4
%_temp.29 = load void (%class.java.lang.Object*)*, void (%class.java.lang.Object*)** %_temp.27
%_temp.30 = bitcast void (%class.java.lang.Object*)* %_temp.29 to void (%class.SimpleClass*)*
%_temp.28 = getelementptr %dv.SimpleClass, %dv.SimpleClass* @_J_dv_11SimpleClass, i32 0, i32 4
store void (%class.SimpleClass*)* %_temp.30, void (%class.SimpleClass*)** %_temp.28
%_temp.31 = getelementptr %dv.java.lang.Object, %dv.java.lang.Object* @_J_dv_16java.lang.Object, i32 0, i32 5
%_temp.33 = load void (%class.java.lang.Object*)*, void (%class.java.lang.Object*)** %_temp.31
%_temp.34 = bitcast void (%class.java.lang.Object*)* %_temp.33 to void (%class.SimpleClass*)*
%_temp.32 = getelementptr %dv.SimpleClass, %dv.SimpleClass* @_J_dv_11SimpleClass, i32 0, i32 5
store void (%class.SimpleClass*)* %_temp.34, void (%class.SimpleClass*)** %_temp.32
%_temp.35 = getelementptr %dv.java.lang.Object, %dv.java.lang.Object* @_J_dv_16java.lang.Object, i32 0, i32 6
%_temp.37 = load %class.java.lang.String* (%class.java.lang.Object*)*, %class.java.lang.String* (%class.java.lang.Object*)** %_temp.35
%_temp.38 = bitcast %class.java.lang.String* (%class.java.lang.Object*)* %_temp.37 to %class.java.lang.String* (%class.SimpleClass*)*
%_temp.36 = getelementptr %dv.SimpleClass, %dv.SimpleClass* @_J_dv_11SimpleClass, i32 0, i32 6
store %class.java.lang.String* (%class.SimpleClass*)* %_temp.38, %class.java.lang.String* (%class.SimpleClass*)** %_temp.36
%_temp.39 = getelementptr %dv.java.lang.Object, %dv.java.lang.Object* @_J_dv_16java.lang.Object, i32 0, i32 7
%_temp.41 = load void (%class.java.lang.Object*, i64)*, void (%class.java.lang.Object*, i64)** %_temp.39
%_temp.42 = bitcast void (%class.java.lang.Object*, i64)* %_temp.41 to void (%class.SimpleClass*, i64)*
%_temp.40 = getelementptr %dv.SimpleClass, %dv.SimpleClass* @_J_dv_11SimpleClass, i32 0, i32 7
store void (%class.SimpleClass*, i64)* %_temp.42, void (%class.SimpleClass*, i64)** %_temp.40
%_temp.43 = getelementptr %dv.java.lang.Object, %dv.java.lang.Object* @_J_dv_16java.lang.Object, i32 0, i32 8
%_temp.45 = load void (%class.java.lang.Object*, i64, i32)*, void (%class.java.lang.Object*, i64, i32)** %_temp.43
%_temp.46 = bitcast void (%class.java.lang.Object*, i64, i32)* %_temp.45 to void (%class.SimpleClass*, i64, i32)*
%_temp.44 = getelementptr %dv.SimpleClass, %dv.SimpleClass* @_J_dv_11SimpleClass, i32 0, i32 8
store void (%class.SimpleClass*, i64, i32)* %_temp.46, void (%class.SimpleClass*, i64, i32)** %_temp.44
%_temp.47 = getelementptr %dv.java.lang.Object, %dv.java.lang.Object* @_J_dv_16java.lang.Object, i32 0, i32 9
%_temp.49 = load void (%class.java.lang.Object*)*, void (%class.java.lang.Object*)** %_temp.47
%_temp.50 = bitcast void (%class.java.lang.Object*)* %_temp.49 to void (%class.SimpleClass*)*
%_temp.48 = getelementptr %dv.SimpleClass, %dv.SimpleClass* @_J_dv_11SimpleClass, i32 0, i32 9
store void (%class.SimpleClass*)* %_temp.50, void (%class.SimpleClass*)** %_temp.48
%_temp.51 = getelementptr %dv.java.lang.Object, %dv.java.lang.Object* @_J_dv_16java.lang.Object, i32 0, i32 10
%_temp.53 = load %class.java.lang.Object* (%class.java.lang.Object*)*, %class.java.lang.Object* (%class.java.lang.Object*)** %_temp.51
%_temp.54 = bitcast %class.java.lang.Object* (%class.java.lang.Object*)* %_temp.53 to %class.java.lang.Object* (%class.SimpleClass*)*
%_temp.52 = getelementptr %dv.SimpleClass, %dv.SimpleClass* @_J_dv_11SimpleClass, i32 0, i32 10
store %class.java.lang.Object* (%class.SimpleClass*)* %_temp.54, %class.java.lang.Object* (%class.SimpleClass*)** %_temp.52
%_temp.55 = getelementptr %dv.java.lang.Object, %dv.java.lang.Object* @_J_dv_16java.lang.Object, i32 0, i32 11
%_temp.57 = load void (%class.java.lang.Object*)*, void (%class.java.lang.Object*)** %_temp.55
%_temp.58 = bitcast void (%class.java.lang.Object*)* %_temp.57 to void (%class.SimpleClass*)*
%_temp.56 = getelementptr %dv.SimpleClass, %dv.SimpleClass* @_J_dv_11SimpleClass, i32 0, i32 11
store void (%class.SimpleClass*)* %_temp.58, void (%class.SimpleClass*)** %_temp.56
%_temp.59 = getelementptr %dv.SimpleClass, %dv.SimpleClass* @_J_dv_11SimpleClass, i32 0, i32 12
store i32 (%class.SimpleClass*)* @_J_11SimpleClass_6method_void, i32 (%class.SimpleClass*)** %_temp.59
%_temp.60 = getelementptr %dv.SimpleClass, %dv.SimpleClass* @_J_dv_11SimpleClass, i32 0, i32 13
store i32 (%class.SimpleClass*)* @_J_11SimpleClass_13privateMethod_void, i32 (%class.SimpleClass*)** %_temp.60
ret void
}

