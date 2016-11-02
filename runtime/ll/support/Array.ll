%dv.java_lang_Object = type {i8*, i8*, i1 (%class.java_lang_Object*, %class.java_lang_Object*)*, %class.java_lang_Class* (%class.java_lang_Object*)*, i32 (%class.java_lang_Object*)*, void (%class.java_lang_Object*)*, void (%class.java_lang_Object*)*, %class.java_lang_String* (%class.java_lang_Object*)*, void (%class.java_lang_Object*, i64)*, void (%class.java_lang_Object*, i64, i32)*, void (%class.java_lang_Object*)*, %class.java_lang_Object* (%class.java_lang_Object*)*, void (%class.java_lang_Object*)*}
%class.java_lang_String = type opaque
%dv.support_Array = type {i8*, i8*, i1 (%class.support_Array*, %class.java_lang_Object*)*, %class.java_lang_Class* (%class.support_Array*)*, i32 (%class.support_Array*)*, void (%class.support_Array*)*, void (%class.support_Array*)*, %class.java_lang_String* (%class.support_Array*)*, void (%class.support_Array*, i64)*, void (%class.support_Array*, i64, i32)*, void (%class.support_Array*)*, %class.java_lang_Object* (%class.support_Array*)*, void (%class.support_Array*)*}
%class.java_lang_Class = type opaque
%class.support_Array = type {%dv.support_Array*, i32, i8*}
%class.java_lang_Object = type {%dv.java_lang_Object*}
@Env_support_Array_dv = global %dv.support_Array zeroinitializer
@Env_java_lang_Object_size = external global i64
@Env_java_lang_Object_dv = external global %dv.java_lang_Object
%__ctortype = type { i32, void ()*, i8* }
@llvm.global_ctors = appending global [1 x %__ctortype] [%__ctortype { i32 65535, void ()* @Env_support_Array_init, i8* null }]
declare i8* @malloc(i64 %size)

declare void @Java_java_lang_Object_Object__(%class.java_lang_Object* %arg_0)

declare void @Env_java_lang_Object_init()

define void @Java_support_Array_Array__(%class.support_Array* %_this) {
%_temp.0 = bitcast %class.support_Array* %_this to %class.java_lang_Object*
call void @Java_java_lang_Object_Object__(%class.java_lang_Object* %_temp.0)
ret void
}

define void @Env_support_Array_init() {
call void @Env_java_lang_Object_init()
%_temp.1 = getelementptr %dv.java_lang_Object, %dv.java_lang_Object* @Env_java_lang_Object_dv, i32 0, i32 2
%_temp.3 = load i1 (%class.java_lang_Object*, %class.java_lang_Object*)*, i1 (%class.java_lang_Object*, %class.java_lang_Object*)** %_temp.1
%_temp.4 = bitcast i1 (%class.java_lang_Object*, %class.java_lang_Object*)* %_temp.3 to i1 (%class.support_Array*, %class.java_lang_Object*)*
%_temp.2 = getelementptr %dv.support_Array, %dv.support_Array* @Env_support_Array_dv, i32 0, i32 2
store i1 (%class.support_Array*, %class.java_lang_Object*)* %_temp.4, i1 (%class.support_Array*, %class.java_lang_Object*)** %_temp.2
%_temp.5 = getelementptr %dv.java_lang_Object, %dv.java_lang_Object* @Env_java_lang_Object_dv, i32 0, i32 3
%_temp.7 = load %class.java_lang_Class* (%class.java_lang_Object*)*, %class.java_lang_Class* (%class.java_lang_Object*)** %_temp.5
%_temp.8 = bitcast %class.java_lang_Class* (%class.java_lang_Object*)* %_temp.7 to %class.java_lang_Class* (%class.support_Array*)*
%_temp.6 = getelementptr %dv.support_Array, %dv.support_Array* @Env_support_Array_dv, i32 0, i32 3
store %class.java_lang_Class* (%class.support_Array*)* %_temp.8, %class.java_lang_Class* (%class.support_Array*)** %_temp.6
%_temp.9 = getelementptr %dv.java_lang_Object, %dv.java_lang_Object* @Env_java_lang_Object_dv, i32 0, i32 4
%_temp.11 = load i32 (%class.java_lang_Object*)*, i32 (%class.java_lang_Object*)** %_temp.9
%_temp.12 = bitcast i32 (%class.java_lang_Object*)* %_temp.11 to i32 (%class.support_Array*)*
%_temp.10 = getelementptr %dv.support_Array, %dv.support_Array* @Env_support_Array_dv, i32 0, i32 4
store i32 (%class.support_Array*)* %_temp.12, i32 (%class.support_Array*)** %_temp.10
%_temp.13 = getelementptr %dv.java_lang_Object, %dv.java_lang_Object* @Env_java_lang_Object_dv, i32 0, i32 5
%_temp.15 = load void (%class.java_lang_Object*)*, void (%class.java_lang_Object*)** %_temp.13
%_temp.16 = bitcast void (%class.java_lang_Object*)* %_temp.15 to void (%class.support_Array*)*
%_temp.14 = getelementptr %dv.support_Array, %dv.support_Array* @Env_support_Array_dv, i32 0, i32 5
store void (%class.support_Array*)* %_temp.16, void (%class.support_Array*)** %_temp.14
%_temp.17 = getelementptr %dv.java_lang_Object, %dv.java_lang_Object* @Env_java_lang_Object_dv, i32 0, i32 6
%_temp.19 = load void (%class.java_lang_Object*)*, void (%class.java_lang_Object*)** %_temp.17
%_temp.20 = bitcast void (%class.java_lang_Object*)* %_temp.19 to void (%class.support_Array*)*
%_temp.18 = getelementptr %dv.support_Array, %dv.support_Array* @Env_support_Array_dv, i32 0, i32 6
store void (%class.support_Array*)* %_temp.20, void (%class.support_Array*)** %_temp.18
%_temp.21 = getelementptr %dv.java_lang_Object, %dv.java_lang_Object* @Env_java_lang_Object_dv, i32 0, i32 7
%_temp.23 = load %class.java_lang_String* (%class.java_lang_Object*)*, %class.java_lang_String* (%class.java_lang_Object*)** %_temp.21
%_temp.24 = bitcast %class.java_lang_String* (%class.java_lang_Object*)* %_temp.23 to %class.java_lang_String* (%class.support_Array*)*
%_temp.22 = getelementptr %dv.support_Array, %dv.support_Array* @Env_support_Array_dv, i32 0, i32 7
store %class.java_lang_String* (%class.support_Array*)* %_temp.24, %class.java_lang_String* (%class.support_Array*)** %_temp.22
%_temp.25 = getelementptr %dv.java_lang_Object, %dv.java_lang_Object* @Env_java_lang_Object_dv, i32 0, i32 8
%_temp.27 = load void (%class.java_lang_Object*, i64)*, void (%class.java_lang_Object*, i64)** %_temp.25
%_temp.28 = bitcast void (%class.java_lang_Object*, i64)* %_temp.27 to void (%class.support_Array*, i64)*
%_temp.26 = getelementptr %dv.support_Array, %dv.support_Array* @Env_support_Array_dv, i32 0, i32 8
store void (%class.support_Array*, i64)* %_temp.28, void (%class.support_Array*, i64)** %_temp.26
%_temp.29 = getelementptr %dv.java_lang_Object, %dv.java_lang_Object* @Env_java_lang_Object_dv, i32 0, i32 9
%_temp.31 = load void (%class.java_lang_Object*, i64, i32)*, void (%class.java_lang_Object*, i64, i32)** %_temp.29
%_temp.32 = bitcast void (%class.java_lang_Object*, i64, i32)* %_temp.31 to void (%class.support_Array*, i64, i32)*
%_temp.30 = getelementptr %dv.support_Array, %dv.support_Array* @Env_support_Array_dv, i32 0, i32 9
store void (%class.support_Array*, i64, i32)* %_temp.32, void (%class.support_Array*, i64, i32)** %_temp.30
%_temp.33 = getelementptr %dv.java_lang_Object, %dv.java_lang_Object* @Env_java_lang_Object_dv, i32 0, i32 10
%_temp.35 = load void (%class.java_lang_Object*)*, void (%class.java_lang_Object*)** %_temp.33
%_temp.36 = bitcast void (%class.java_lang_Object*)* %_temp.35 to void (%class.support_Array*)*
%_temp.34 = getelementptr %dv.support_Array, %dv.support_Array* @Env_support_Array_dv, i32 0, i32 10
store void (%class.support_Array*)* %_temp.36, void (%class.support_Array*)** %_temp.34
%_temp.37 = getelementptr %dv.java_lang_Object, %dv.java_lang_Object* @Env_java_lang_Object_dv, i32 0, i32 11
%_temp.39 = load %class.java_lang_Object* (%class.java_lang_Object*)*, %class.java_lang_Object* (%class.java_lang_Object*)** %_temp.37
%_temp.40 = bitcast %class.java_lang_Object* (%class.java_lang_Object*)* %_temp.39 to %class.java_lang_Object* (%class.support_Array*)*
%_temp.38 = getelementptr %dv.support_Array, %dv.support_Array* @Env_support_Array_dv, i32 0, i32 11
store %class.java_lang_Object* (%class.support_Array*)* %_temp.40, %class.java_lang_Object* (%class.support_Array*)** %_temp.38
%_temp.41 = getelementptr %dv.java_lang_Object, %dv.java_lang_Object* @Env_java_lang_Object_dv, i32 0, i32 12
%_temp.43 = load void (%class.java_lang_Object*)*, void (%class.java_lang_Object*)** %_temp.41
%_temp.44 = bitcast void (%class.java_lang_Object*)* %_temp.43 to void (%class.support_Array*)*
%_temp.42 = getelementptr %dv.support_Array, %dv.support_Array* @Env_support_Array_dv, i32 0, i32 12
store void (%class.support_Array*)* %_temp.44, void (%class.support_Array*)** %_temp.42
ret void
}

