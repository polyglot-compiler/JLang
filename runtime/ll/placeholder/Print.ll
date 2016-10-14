%dv.Array = type {i8*, i1 (%class.Array*, %class.java.lang.Object*)*, %class.java.lang.Class* (%class.Array*)*, i32 (%class.Array*)*, void (%class.Array*)*, void (%class.Array*)*, %class.java.lang.String* (%class.Array*)*, void (%class.Array*, i64)*, void (%class.Array*, i64, i32)*, void (%class.Array*)*, %class.java.lang.Object* (%class.Array*)*, void (%class.Array*)*}
%class.placeholder.Print = type {%dv.placeholder.Print*}
%class.Array = type {%dv.Array*, i32, i8*}
%dv.java.lang.Object = type {i8*, i1 (%class.java.lang.Object*, %class.java.lang.Object*)*, %class.java.lang.Class* (%class.java.lang.Object*)*, i32 (%class.java.lang.Object*)*, void (%class.java.lang.Object*)*, void (%class.java.lang.Object*)*, %class.java.lang.String* (%class.java.lang.Object*)*, void (%class.java.lang.Object*, i64)*, void (%class.java.lang.Object*, i64, i32)*, void (%class.java.lang.Object*)*, %class.java.lang.Object* (%class.java.lang.Object*)*, void (%class.java.lang.Object*)*}
%class.java.lang.Class = type opaque
%dv.placeholder.Print = type {i8*, i1 (%class.placeholder.Print*, %class.java.lang.Object*)*, %class.java.lang.Class* (%class.placeholder.Print*)*, i32 (%class.placeholder.Print*)*, void (%class.placeholder.Print*)*, void (%class.placeholder.Print*)*, %class.java.lang.String* (%class.placeholder.Print*)*, void (%class.placeholder.Print*, i64)*, void (%class.placeholder.Print*, i64, i32)*, void (%class.placeholder.Print*)*, %class.java.lang.Object* (%class.placeholder.Print*)*, void (%class.placeholder.Print*)*}
%class.java.lang.Object = type {%dv.java.lang.Object*}
%class.java.lang.String = type opaque
@_J_dv_5Array = external global %dv.Array
@_J_size_17placeholder.Print = global i64 0
@_J_dv_17placeholder.Print = global %dv.placeholder.Print zeroinitializer
@_J_size_16java.lang.Object = external global i64
@_J_dv_16java.lang.Object = external global %dv.java.lang.Object
%__ctortype = type { i32, void ()*, i8* }
@llvm.global_ctors = appending global [1 x %__ctortype] [%__ctortype { i32 65535, void ()* @_J_init_17placeholder.Print, i8* null }]
declare i8* @malloc(i64 %size)

declare i32 @printf(i8*, ...)
@int_ln_str    = private unnamed_addr constant [4 x i8] c"%d\0A\00",   align 1
@int_str       = private unnamed_addr constant [3 x i8] c"%d\00",      align 1
@long_ln_str   = private unnamed_addr constant [6 x i8] c"%lld\0A\00", align 1
@long_str      = private unnamed_addr constant [5 x i8] c"%lld\00",    align 1
@float_ln_str  = private unnamed_addr constant [4 x i8] c"%f\0A\00",   align 1
@float_str     = private unnamed_addr constant [3 x i8] c"%f\00",      align 1
@double_ln_str = private unnamed_addr constant [4 x i8] c"%f\0A\00",   align 1
@double_str    = private unnamed_addr constant [3 x i8] c"%f\00",      align 1

declare void @_J_init_16java.lang.Object()

define void @_J_17placeholder.Print_7println_16java.lang.String(%class.java.lang.String* %s) {
%arg_s = alloca %class.java.lang.String*, i32 1
store %class.java.lang.String* %s, %class.java.lang.String** %arg_s
ret void
}

define void @_J_17placeholder.Print_5print_16java.lang.String(%class.java.lang.String* %s) {
%arg_s = alloca %class.java.lang.String*, i32 1
store %class.java.lang.String* %s, %class.java.lang.String** %arg_s
ret void
}

define void @_J_17placeholder.Print_7println_i32(i32 %n) {
%arg_n = alloca i32, i32 1
store i32 %n, i32* %arg_n
%call = call i32 (i8*, ...) @printf(i8* getelementptr inbounds ([4 x i8], [4 x i8]* @int_ln_str, i32 0, i32 0), i32 %n)
ret void
}

define void @_J_17placeholder.Print_5print_i32(i32 %n) {
%arg_n = alloca i32, i32 1
store i32 %n, i32* %arg_n
%call = call i32 (i8*, ...) @printf(i8* getelementptr inbounds ([3 x i8], [3 x i8]* @int_str, i32 0, i32 0), i32 %n)
ret void
}

define void @_J_17placeholder.Print_7println_i64(i64 %n) {
%arg_n = alloca i64, i32 1
store i64 %n, i64* %arg_n
%call = call i32 (i8*, ...) @printf(i8* getelementptr inbounds ([6 x i8], [6 x i8]* @long_ln_str, i32 0, i32 0), i64 %n)
ret void
}

define void @_J_17placeholder.Print_5print_i64(i64 %n) {
%arg_n = alloca i64, i32 1
store i64 %n, i64* %arg_n
%call = call i32 (i8*, ...) @printf(i8* getelementptr inbounds ([5 x i8], [5 x i8]* @long_str, i32 0, i32 0), i64 %n)
ret void
}

define void @_J_17placeholder.Print_7println_f(float %n) {
%arg_n = alloca float, i32 1
store float %n, float* %arg_n
%call = call i32 (i8*, ...) @printf(i8* getelementptr inbounds ([4 x i8], [4 x i8]* @float_ln_str, i32 0, i32 0), float %n)
ret void
}

define void @_J_17placeholder.Print_5print_f(float %n) {
%arg_n = alloca float, i32 1
store float %n, float* %arg_n
%call = call i32 (i8*, ...) @printf(i8* getelementptr inbounds ([3 x i8], [3 x i8]* @float_str, i32 0, i32 0), float %n)
ret void
}

define void @_J_17placeholder.Print_7println_d(double %n) {
%arg_n = alloca double, i32 1
store double %n, double* %arg_n
%call = call i32 (i8*, ...) @printf(i8* getelementptr inbounds ([4 x i8], [4 x i8]* @double_ln_str, i32 0, i32 0), double %n)
ret void
}

define void @_J_17placeholder.Print_5print_d(double %n) {
%arg_n = alloca double, i32 1
store double %n, double* %arg_n
%call = call i32 (i8*, ...) @printf(i8* getelementptr inbounds ([3 x i8], [3 x i8]* @double_str, i32 0, i32 0), double %n)
ret void
}

define void @_J_17placeholder.Print__constructor__void(%class.placeholder.Print* %_this) {
ret void
}

define void @_J_init_17placeholder.Print() {
call void @_J_init_16java.lang.Object()
%_temp.1 = getelementptr %dv.java.lang.Object, %dv.java.lang.Object* @_J_dv_16java.lang.Object, i32 0, i32 1
%_temp.3 = load i1 (%class.java.lang.Object*, %class.java.lang.Object*)*, i1 (%class.java.lang.Object*, %class.java.lang.Object*)** %_temp.1
%_temp.4 = bitcast i1 (%class.java.lang.Object*, %class.java.lang.Object*)* %_temp.3 to i1 (%class.placeholder.Print*, %class.java.lang.Object*)*
%_temp.2 = getelementptr %dv.placeholder.Print, %dv.placeholder.Print* @_J_dv_17placeholder.Print, i32 0, i32 1
store i1 (%class.placeholder.Print*, %class.java.lang.Object*)* %_temp.4, i1 (%class.placeholder.Print*, %class.java.lang.Object*)** %_temp.2
%_temp.5 = getelementptr %dv.java.lang.Object, %dv.java.lang.Object* @_J_dv_16java.lang.Object, i32 0, i32 2
%_temp.7 = load %class.java.lang.Class* (%class.java.lang.Object*)*, %class.java.lang.Class* (%class.java.lang.Object*)** %_temp.5
%_temp.8 = bitcast %class.java.lang.Class* (%class.java.lang.Object*)* %_temp.7 to %class.java.lang.Class* (%class.placeholder.Print*)*
%_temp.6 = getelementptr %dv.placeholder.Print, %dv.placeholder.Print* @_J_dv_17placeholder.Print, i32 0, i32 2
store %class.java.lang.Class* (%class.placeholder.Print*)* %_temp.8, %class.java.lang.Class* (%class.placeholder.Print*)** %_temp.6
%_temp.9 = getelementptr %dv.java.lang.Object, %dv.java.lang.Object* @_J_dv_16java.lang.Object, i32 0, i32 3
%_temp.11 = load i32 (%class.java.lang.Object*)*, i32 (%class.java.lang.Object*)** %_temp.9
%_temp.12 = bitcast i32 (%class.java.lang.Object*)* %_temp.11 to i32 (%class.placeholder.Print*)*
%_temp.10 = getelementptr %dv.placeholder.Print, %dv.placeholder.Print* @_J_dv_17placeholder.Print, i32 0, i32 3
store i32 (%class.placeholder.Print*)* %_temp.12, i32 (%class.placeholder.Print*)** %_temp.10
%_temp.13 = getelementptr %dv.java.lang.Object, %dv.java.lang.Object* @_J_dv_16java.lang.Object, i32 0, i32 4
%_temp.15 = load void (%class.java.lang.Object*)*, void (%class.java.lang.Object*)** %_temp.13
%_temp.16 = bitcast void (%class.java.lang.Object*)* %_temp.15 to void (%class.placeholder.Print*)*
%_temp.14 = getelementptr %dv.placeholder.Print, %dv.placeholder.Print* @_J_dv_17placeholder.Print, i32 0, i32 4
store void (%class.placeholder.Print*)* %_temp.16, void (%class.placeholder.Print*)** %_temp.14
%_temp.17 = getelementptr %dv.java.lang.Object, %dv.java.lang.Object* @_J_dv_16java.lang.Object, i32 0, i32 5
%_temp.19 = load void (%class.java.lang.Object*)*, void (%class.java.lang.Object*)** %_temp.17
%_temp.20 = bitcast void (%class.java.lang.Object*)* %_temp.19 to void (%class.placeholder.Print*)*
%_temp.18 = getelementptr %dv.placeholder.Print, %dv.placeholder.Print* @_J_dv_17placeholder.Print, i32 0, i32 5
store void (%class.placeholder.Print*)* %_temp.20, void (%class.placeholder.Print*)** %_temp.18
%_temp.21 = getelementptr %dv.java.lang.Object, %dv.java.lang.Object* @_J_dv_16java.lang.Object, i32 0, i32 6
%_temp.23 = load %class.java.lang.String* (%class.java.lang.Object*)*, %class.java.lang.String* (%class.java.lang.Object*)** %_temp.21
%_temp.24 = bitcast %class.java.lang.String* (%class.java.lang.Object*)* %_temp.23 to %class.java.lang.String* (%class.placeholder.Print*)*
%_temp.22 = getelementptr %dv.placeholder.Print, %dv.placeholder.Print* @_J_dv_17placeholder.Print, i32 0, i32 6
store %class.java.lang.String* (%class.placeholder.Print*)* %_temp.24, %class.java.lang.String* (%class.placeholder.Print*)** %_temp.22
%_temp.25 = getelementptr %dv.java.lang.Object, %dv.java.lang.Object* @_J_dv_16java.lang.Object, i32 0, i32 7
%_temp.27 = load void (%class.java.lang.Object*, i64)*, void (%class.java.lang.Object*, i64)** %_temp.25
%_temp.28 = bitcast void (%class.java.lang.Object*, i64)* %_temp.27 to void (%class.placeholder.Print*, i64)*
%_temp.26 = getelementptr %dv.placeholder.Print, %dv.placeholder.Print* @_J_dv_17placeholder.Print, i32 0, i32 7
store void (%class.placeholder.Print*, i64)* %_temp.28, void (%class.placeholder.Print*, i64)** %_temp.26
%_temp.29 = getelementptr %dv.java.lang.Object, %dv.java.lang.Object* @_J_dv_16java.lang.Object, i32 0, i32 8
%_temp.31 = load void (%class.java.lang.Object*, i64, i32)*, void (%class.java.lang.Object*, i64, i32)** %_temp.29
%_temp.32 = bitcast void (%class.java.lang.Object*, i64, i32)* %_temp.31 to void (%class.placeholder.Print*, i64, i32)*
%_temp.30 = getelementptr %dv.placeholder.Print, %dv.placeholder.Print* @_J_dv_17placeholder.Print, i32 0, i32 8
store void (%class.placeholder.Print*, i64, i32)* %_temp.32, void (%class.placeholder.Print*, i64, i32)** %_temp.30
%_temp.33 = getelementptr %dv.java.lang.Object, %dv.java.lang.Object* @_J_dv_16java.lang.Object, i32 0, i32 9
%_temp.35 = load void (%class.java.lang.Object*)*, void (%class.java.lang.Object*)** %_temp.33
%_temp.36 = bitcast void (%class.java.lang.Object*)* %_temp.35 to void (%class.placeholder.Print*)*
%_temp.34 = getelementptr %dv.placeholder.Print, %dv.placeholder.Print* @_J_dv_17placeholder.Print, i32 0, i32 9
store void (%class.placeholder.Print*)* %_temp.36, void (%class.placeholder.Print*)** %_temp.34
%_temp.37 = getelementptr %dv.java.lang.Object, %dv.java.lang.Object* @_J_dv_16java.lang.Object, i32 0, i32 10
%_temp.39 = load %class.java.lang.Object* (%class.java.lang.Object*)*, %class.java.lang.Object* (%class.java.lang.Object*)** %_temp.37
%_temp.40 = bitcast %class.java.lang.Object* (%class.java.lang.Object*)* %_temp.39 to %class.java.lang.Object* (%class.placeholder.Print*)*
%_temp.38 = getelementptr %dv.placeholder.Print, %dv.placeholder.Print* @_J_dv_17placeholder.Print, i32 0, i32 10
store %class.java.lang.Object* (%class.placeholder.Print*)* %_temp.40, %class.java.lang.Object* (%class.placeholder.Print*)** %_temp.38
%_temp.41 = getelementptr %dv.java.lang.Object, %dv.java.lang.Object* @_J_dv_16java.lang.Object, i32 0, i32 11
%_temp.43 = load void (%class.java.lang.Object*)*, void (%class.java.lang.Object*)** %_temp.41
%_temp.44 = bitcast void (%class.java.lang.Object*)* %_temp.43 to void (%class.placeholder.Print*)*
%_temp.42 = getelementptr %dv.placeholder.Print, %dv.placeholder.Print* @_J_dv_17placeholder.Print, i32 0, i32 11
store void (%class.placeholder.Print*)* %_temp.44, void (%class.placeholder.Print*)** %_temp.42
ret void
}
