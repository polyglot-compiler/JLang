%dv.java_lang_Object = type {i8*, i8*, i1 (%class.java_lang_Object*, %class.java_lang_Object*)*, %class.java_lang_String* (%class.java_lang_Object*)*}
%class.java_lang_String = type {%dv.java_lang_String*, %class.support_Array*}
%dv.support_Array = type {i8*, i8*, i1 (%class.support_Array*, %class.java_lang_Object*)*, %class.java_lang_String* (%class.support_Array*)*, %class.java_lang_Object* (%class.support_Array*)*}
%dv.java_lang_String = type {i8*, i8*, i1 (%class.java_lang_String*, %class.java_lang_Object*)*, %class.java_lang_String* (%class.java_lang_String*)*}
%class.java_lang_Class = type opaque
%class.support_Array = type {%dv.support_Array*, i32, i8*}
%class.java_lang_Object = type {%dv.java_lang_Object*}
@Env_support_Array_dv = external global %dv.support_Array
@Env_java_lang_Object_size = global i64 zeroinitializer
@Env_java_lang_Object_dv = global %dv.java_lang_Object zeroinitializer
@Env_java_lang_String_size = external global i64
@Env_java_lang_String_dv = external global %dv.java_lang_String
%__ctortype = type { i32, void ()*, i8* }
@llvm.global_ctors = appending global [1 x %__ctortype] [%__ctortype { i32 65535, void ()* @Env_java_lang_Object_init, i8* null }]
declare i8* @malloc(i64 %size)

define i1 @Java_java_lang_Object_equals__Ljava_lang_Object_2(%class.java_lang_Object* %_this, %class.java_lang_Object* %other) {
%arg_other = alloca %class.java_lang_Object*, i32 1
store %class.java_lang_Object* %other, %class.java_lang_Object** %arg_other
ret i1 0
ret i1 0
}

define %class.java_lang_String* @Java_java_lang_Object_toString__(%class.java_lang_Object* %_this) {
%_temp.0 = bitcast i8* null to %class.java_lang_String*
ret %class.java_lang_String* %_temp.0
ret %class.java_lang_String* null
}

define void @Java_java_lang_Object_Object__(%class.java_lang_Object* %_this) {
ret void
}

define void @Env_java_lang_Object_init() {
%_temp.1 = getelementptr %dv.java_lang_Object, %dv.java_lang_Object* @Env_java_lang_Object_dv, i32 0, i32 2
store i1 (%class.java_lang_Object*, %class.java_lang_Object*)* @Java_java_lang_Object_equals__Ljava_lang_Object_2, i1 (%class.java_lang_Object*, %class.java_lang_Object*)** %_temp.1
%_temp.2 = getelementptr %dv.java_lang_Object, %dv.java_lang_Object* @Env_java_lang_Object_dv, i32 0, i32 3
store %class.java_lang_String* (%class.java_lang_Object*)* @Java_java_lang_Object_toString__, %class.java_lang_String* (%class.java_lang_Object*)** %_temp.2
ret void
}

