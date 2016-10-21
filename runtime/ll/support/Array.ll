%class.support.Array = type {%dv.support.Array*, i32,i8*}
%dv.support.Array = type {i8*, i1 (%class.support.Array*, %class.java.lang.Object*)*, %class.java.lang.Class* (%class.support.Array*)*, i32 (%class.support.Array*)*, void (%class.support.Array*)*, void (%class.support.Array*)*, %class.java.lang.String* (%class.support.Array*)*, void (%class.support.Array*, i64)*, void (%class.support.Array*, i64, i32)*, void (%class.support.Array*)*, %class.java.lang.Object* (%class.support.Array*)*, void (%class.support.Array*)*}
%dv.java.lang.Object = type {i8*, i1 (%class.java.lang.Object*, %class.java.lang.Object*)*, %class.java.lang.Class* (%class.java.lang.Object*)*, i32 (%class.java.lang.Object*)*, void (%class.java.lang.Object*)*, void (%class.java.lang.Object*)*, %class.java.lang.String* (%class.java.lang.Object*)*, void (%class.java.lang.Object*, i64)*, void (%class.java.lang.Object*, i64, i32)*, void (%class.java.lang.Object*)*, %class.java.lang.Object* (%class.java.lang.Object*)*, void (%class.java.lang.Object*)*}
%class.java.lang.CloneNotSupportedException = type opaque
%class.java.lang.Class = type opaque
%class.java.lang.Object = type {%dv.java.lang.Object*}
%class.java.lang.String = type opaque
@_J_size_13support.Array = global i64 0
@_J_dv_13support.Array = global %dv.support.Array zeroinitializer
@_J_size_16java.lang.Object = external global i64
@_J_dv_16java.lang.Object = external global %dv.java.lang.Object
%__ctortype = type { i32, void ()*, i8* }
@llvm.global_ctors = appending global [1 x %__ctortype] [%__ctortype { i32 65535, void ()* @_J_init_13support.Array, i8* null }]
declare i8* @malloc(i64 %size)

declare void @_J_16java.lang.Object__constructor__void(%class.java.lang.Object* %arg_0)

declare %class.java.lang.Object* @_J_16java.lang.Object_5clone_void(%class.java.lang.Object* %arg_0)

declare void @_J_init_16java.lang.Object()

define void @_J_13support.Array__constructor__i32(%class.support.Array* %_this, i32 %length) {
%arg_length = alloca i32, i32 1
store i32 %length, i32* %arg_length
%_temp.0 = bitcast %class.support.Array* %_this to %class.java.lang.Object*
call void @_J_16java.lang.Object__constructor__void(%class.java.lang.Object* %_temp.0)
%_temp.4 = getelementptr %class.support.Array, %class.support.Array* %_this, i32 0, i32 1
%_temp.3 = load i32, i32* %arg_length
store i32 %_temp.3, i32* %_temp.4
ret void
}

define void @_J_13support.Array__constructor__a_i32(%class.support.Array* %_this, %class.support.Array* %lengths) {
%flat$9 = alloca i32, i32 1
%flat$7 = alloca i32, i32 1
%flat$8 = alloca i32, i32 1
%flat$5 = alloca i32, i32 1
%flat$6 = alloca i32, i32 1
%flat$3 = alloca i32, i32 1
%newLengths = alloca %class.support.Array*, i32 1
%flat$4 = alloca i1, i32 1
%flat$2 = alloca i32, i32 1
%i = alloca i32, i32 1
%loop$1 = alloca i1, i32 1
%loop$0 = alloca i1, i32 1
%flat$11 = alloca i32, i32 1
%TEMPOBJECT = alloca %class.java.lang.Object*, i32 1
%flat$10 = alloca i32, i32 1
%INDEX = alloca i32, i32 1
%flat$12 = alloca i32, i32 1
%arg_lengths = alloca %class.support.Array*, i32 1
store %class.support.Array* %lengths, %class.support.Array** %arg_lengths
%_temp.5 = bitcast %class.support.Array* %_this to %class.java.lang.Object*
call void @_J_16java.lang.Object__constructor__void(%class.java.lang.Object* %_temp.5)
%_temp.6 = load %class.support.Array*, %class.support.Array** %arg_lengths
%_result.0 = getelementptr %class.support.Array, %class.support.Array* %_temp.6, i32 0, i32 2
%_temp.7 = sext i32 0 to i64
%_elementPtr.0 = getelementptr i8*, i8** %_result.0, i64 %_temp.7
%_temp.8 = load i8*, i8** %_elementPtr.0
%_temp.9 = ptrtoint i8* %_temp.8 to i32
store i32 %_temp.9, i32* %flat$2
%_temp.13 = getelementptr %class.support.Array, %class.support.Array* %_this, i32 0, i32 1
%_temp.12 = load i32, i32* %flat$2
store i32 %_temp.12, i32* %_temp.13
%_temp.14 = load %class.support.Array*, %class.support.Array** %arg_lengths
%_temp.15 = getelementptr %class.support.Array, %class.support.Array* %_temp.14, i32 0, i32 1
%_temp.16 = load i32, i32* %_temp.15
store i32 %_temp.16, i32* %flat$3
%_temp.17 = load i32, i32* %flat$3
%_temp.18 = icmp eq i32 %_temp.17, 1
store i1 %_temp.18, i1* %flat$4
%_temp.19 = load i1, i1* %flat$4
br i1 %_temp.19, label %label.0, label %label.1
label.0:
ret void
br label %label.2
label.1:
br label %label.2
label.2:
%_temp.20 = load %class.support.Array*, %class.support.Array** %arg_lengths
%_temp.21 = getelementptr %class.support.Array, %class.support.Array* %_temp.20, i32 0, i32 1
%_temp.22 = load i32, i32* %_temp.21
store i32 %_temp.22, i32* %flat$5
%_temp.23 = load i32, i32* %flat$5
%_temp.24 = sub i32 %_temp.23, 1
store i32 %_temp.24, i32* %flat$6
store %class.support.Array* null, %class.support.Array** %newLengths
%_temp.27 = load i32, i32* %flat$6
%_mulByEightVar.0 = mul i32 8, %_temp.27
%_addTwoVar.0 = add i32 2, %_mulByEightVar.0
%_sizeCastVar.0 = sext i32 %_addTwoVar.0 to i64
%_temp.128 = alloca i64, i32 1
store i64 %_sizeCastVar.0, i64* %_temp.128
%_temp.129 = load i64, i64* %_temp.128

%_temp.28 = call i8* @malloc(i64 %_temp.129)
%_temp.29 = bitcast i8* %_temp.28 to %class.support.Array*
%_temp.30 = getelementptr %class.support.Array, %class.support.Array* %_temp.29, i32 0, i32 0
store %dv.support.Array* @_J_dv_13support.Array, %dv.support.Array** %_temp.30
%_temp.26 = load i32, i32* %flat$6
%_temp.130 = alloca i32, i32 1
store i32 %_temp.26, i32* %_temp.130
%_temp.131 = load i32, i32* %_temp.130
call void @_J_13support.Array__constructor__i32(%class.support.Array* %_temp.29, i32 %_temp.131)
store %class.support.Array* %_temp.29, %class.support.Array** %newLengths
store i32 0, i32* %i
store i32 0, i32* %i
store i1 0, i1* %loop$0
store i1 0, i1* %loop$0
br label %loop.head.0
loop.head.0:
br label %label.9
label.9:
%_temp.33 = load i1, i1* %loop$0
br i1 %_temp.33, label %label.3, label %label.4
label.3:
%_temp.34 = load i32, i32* %i
%_temp.35 = add i32 %_temp.34, 1
store i32 %_temp.35, i32* %flat$7
%_temp.37 = load i32, i32* %flat$7
store i32 %_temp.37, i32* %i

%_NOP.0 = add i64 0, 0
br label %label.5
label.4:
br label %label.5
label.5:
%_temp.38 = load %class.support.Array*, %class.support.Array** %newLengths
%_temp.39 = getelementptr %class.support.Array, %class.support.Array* %_temp.38, i32 0, i32 1
%_temp.40 = load i32, i32* %_temp.39
store i32 %_temp.40, i32* %flat$8
%_temp.42 = load i32, i32* %i
%_temp.132 = alloca i32, i32 1
store i32 %_temp.42, i32* %_temp.132
%_temp.43 = load i32, i32* %flat$8
%_temp.133 = load i32, i32* %_temp.132
%_temp.44 = icmp slt i32 %_temp.133, %_temp.43
store i1 %_temp.44, i1* %loop$0
%_temp.45 = load i1, i1* %loop$0
br i1 %_temp.45, label %label.6, label %label.7
label.6:
%_temp.46 = load i32, i32* %i
%_temp.47 = add i32 %_temp.46, 1
store i32 %_temp.47, i32* %flat$9
%_temp.48 = load %class.support.Array*, %class.support.Array** %arg_lengths
%_result.1 = getelementptr %class.support.Array, %class.support.Array* %_temp.48, i32 0, i32 2
%_temp.49 = load i32, i32* %flat$9
%_temp.50 = sext i32 %_temp.49 to i64
%_elementPtr.1 = getelementptr i8*, i8** %_result.1, i64 %_temp.50
%_temp.51 = load i8*, i8** %_elementPtr.1
%_temp.52 = ptrtoint i8* %_temp.51 to i32
store i32 %_temp.52, i32* %flat$10
%_temp.58 = load i32, i32* %flat$10
%_temp.60 = inttoptr i32 %_temp.58 to i8*
%_temp.53 = load %class.support.Array*, %class.support.Array** %newLengths
%_result.3 = getelementptr %class.support.Array, %class.support.Array* %_temp.53, i32 0, i32 2
%_temp.54 = load i32, i32* %i
%_temp.59 = sext i32 %_temp.54 to i64
%_elementPtr.3 = getelementptr i8*, i8** %_result.3, i64 %_temp.59
store i8* %_temp.60, i8** %_elementPtr.3
br label %label.8
label.7:
br label %loop.end.0
br label %label.8
label.8:
br label %loop.head.0
loop.end.0:

store i32 0, i32* %i
store i32 0, i32* %i
store i1 0, i1* %loop$1
store i1 0, i1* %loop$1
br label %loop.head.1
loop.head.1:
br label %label.16
label.16:
%_temp.63 = load i1, i1* %loop$1
br i1 %_temp.63, label %label.10, label %label.11
label.10:
%_temp.64 = load i32, i32* %i
%_temp.65 = add i32 %_temp.64, 1
store i32 %_temp.65, i32* %flat$11
%_temp.67 = load i32, i32* %flat$11
store i32 %_temp.67, i32* %i

%_NOP.1 = add i64 0, 0
br label %label.12
label.11:
br label %label.12
label.12:
%_temp.68 = getelementptr %class.support.Array, %class.support.Array* %_this, i32 0, i32 1
%_temp.69 = load i32, i32* %_temp.68
store i32 %_temp.69, i32* %flat$12
%_temp.71 = load i32, i32* %i
%_temp.134 = alloca i32, i32 1
store i32 %_temp.71, i32* %_temp.134
%_temp.72 = load i32, i32* %flat$12
%_temp.135 = load i32, i32* %_temp.134
%_temp.73 = icmp slt i32 %_temp.135, %_temp.72
store i1 %_temp.73, i1* %loop$1
%_temp.74 = load i1, i1* %loop$1
br i1 %_temp.74, label %label.13, label %label.14
label.13:
store %class.java.lang.Object* null, %class.java.lang.Object** %TEMPOBJECT

%_temp.77 = call i8* @malloc(i64 16)
%_temp.78 = bitcast i8* %_temp.77 to %class.support.Array*
%_temp.79 = getelementptr %class.support.Array, %class.support.Array* %_temp.78, i32 0, i32 0
store %dv.support.Array* @_J_dv_13support.Array, %dv.support.Array** %_temp.79
%_temp.76 = load %class.support.Array*, %class.support.Array** %newLengths
%_temp.136 = alloca %class.support.Array*, i32 1
store %class.support.Array* %_temp.76, %class.support.Array** %_temp.136
%_temp.137 = load %class.support.Array*, %class.support.Array** %_temp.136
call void @_J_13support.Array__constructor__a_i32(%class.support.Array* %_temp.78, %class.support.Array* %_temp.137)
%_temp.80 = bitcast %class.support.Array* %_temp.78 to %class.java.lang.Object*

;%__loadthis = load %class.support.Array*, %class.support.Array** %_this
%__arrayptr = getelementptr %class.support.Array, %class.support.Array* %_this, i32 0, i32 2
%__index = load i32, i32* %i
%__castindex = sext i32 %__index to i64
%__elementPtr.0 = getelementptr i8*, i8** %__arrayptr, i64 %__castindex
%__arrayi8star = bitcast %class.support.Array* %_temp.78 to i8*
store i8* %__arrayi8star, i8** %__elementPtr.0




br label %label.15
label.14:
br label %loop.end.1
br label %label.15
label.15:
br label %loop.head.1
loop.end.1:

ret void
}

define %class.java.lang.Object* @_J_13support.Array_5clone_void(%class.support.Array* %_this) {
%flat$13 = alloca %class.java.lang.Object*, i32 1
%_temp.84 = bitcast %class.java.lang.Object* (%class.java.lang.Object*)* @_J_16java.lang.Object_5clone_void to %class.java.lang.Object* (%class.support.Array*)*
%_temp.85 = call %class.java.lang.Object* %_temp.84(%class.support.Array* %_this)
store %class.java.lang.Object* %_temp.85, %class.java.lang.Object** %flat$13
%_temp.86 = load %class.java.lang.Object*, %class.java.lang.Object** %flat$13
ret %class.java.lang.Object* %_temp.86
ret %class.java.lang.Object* null
}

define void @_J_init_13support.Array() {
call void @_J_init_16java.lang.Object()
%_temp.87 = getelementptr %dv.java.lang.Object, %dv.java.lang.Object* @_J_dv_16java.lang.Object, i32 0, i32 1
%_temp.89 = load i1 (%class.java.lang.Object*, %class.java.lang.Object*)*, i1 (%class.java.lang.Object*, %class.java.lang.Object*)** %_temp.87
%_temp.90 = bitcast i1 (%class.java.lang.Object*, %class.java.lang.Object*)* %_temp.89 to i1 (%class.support.Array*, %class.java.lang.Object*)*
%_temp.88 = getelementptr %dv.support.Array, %dv.support.Array* @_J_dv_13support.Array, i32 0, i32 1
store i1 (%class.support.Array*, %class.java.lang.Object*)* %_temp.90, i1 (%class.support.Array*, %class.java.lang.Object*)** %_temp.88
%_temp.91 = getelementptr %dv.java.lang.Object, %dv.java.lang.Object* @_J_dv_16java.lang.Object, i32 0, i32 2
%_temp.93 = load %class.java.lang.Class* (%class.java.lang.Object*)*, %class.java.lang.Class* (%class.java.lang.Object*)** %_temp.91
%_temp.94 = bitcast %class.java.lang.Class* (%class.java.lang.Object*)* %_temp.93 to %class.java.lang.Class* (%class.support.Array*)*
%_temp.92 = getelementptr %dv.support.Array, %dv.support.Array* @_J_dv_13support.Array, i32 0, i32 2
store %class.java.lang.Class* (%class.support.Array*)* %_temp.94, %class.java.lang.Class* (%class.support.Array*)** %_temp.92
%_temp.95 = getelementptr %dv.java.lang.Object, %dv.java.lang.Object* @_J_dv_16java.lang.Object, i32 0, i32 3
%_temp.97 = load i32 (%class.java.lang.Object*)*, i32 (%class.java.lang.Object*)** %_temp.95
%_temp.98 = bitcast i32 (%class.java.lang.Object*)* %_temp.97 to i32 (%class.support.Array*)*
%_temp.96 = getelementptr %dv.support.Array, %dv.support.Array* @_J_dv_13support.Array, i32 0, i32 3
store i32 (%class.support.Array*)* %_temp.98, i32 (%class.support.Array*)** %_temp.96
%_temp.99 = getelementptr %dv.java.lang.Object, %dv.java.lang.Object* @_J_dv_16java.lang.Object, i32 0, i32 4
%_temp.101 = load void (%class.java.lang.Object*)*, void (%class.java.lang.Object*)** %_temp.99
%_temp.102 = bitcast void (%class.java.lang.Object*)* %_temp.101 to void (%class.support.Array*)*
%_temp.100 = getelementptr %dv.support.Array, %dv.support.Array* @_J_dv_13support.Array, i32 0, i32 4
store void (%class.support.Array*)* %_temp.102, void (%class.support.Array*)** %_temp.100
%_temp.103 = getelementptr %dv.java.lang.Object, %dv.java.lang.Object* @_J_dv_16java.lang.Object, i32 0, i32 5
%_temp.105 = load void (%class.java.lang.Object*)*, void (%class.java.lang.Object*)** %_temp.103
%_temp.106 = bitcast void (%class.java.lang.Object*)* %_temp.105 to void (%class.support.Array*)*
%_temp.104 = getelementptr %dv.support.Array, %dv.support.Array* @_J_dv_13support.Array, i32 0, i32 5
store void (%class.support.Array*)* %_temp.106, void (%class.support.Array*)** %_temp.104
%_temp.107 = getelementptr %dv.java.lang.Object, %dv.java.lang.Object* @_J_dv_16java.lang.Object, i32 0, i32 6
%_temp.109 = load %class.java.lang.String* (%class.java.lang.Object*)*, %class.java.lang.String* (%class.java.lang.Object*)** %_temp.107
%_temp.110 = bitcast %class.java.lang.String* (%class.java.lang.Object*)* %_temp.109 to %class.java.lang.String* (%class.support.Array*)*
%_temp.108 = getelementptr %dv.support.Array, %dv.support.Array* @_J_dv_13support.Array, i32 0, i32 6
store %class.java.lang.String* (%class.support.Array*)* %_temp.110, %class.java.lang.String* (%class.support.Array*)** %_temp.108
%_temp.111 = getelementptr %dv.java.lang.Object, %dv.java.lang.Object* @_J_dv_16java.lang.Object, i32 0, i32 7
%_temp.113 = load void (%class.java.lang.Object*, i64)*, void (%class.java.lang.Object*, i64)** %_temp.111
%_temp.114 = bitcast void (%class.java.lang.Object*, i64)* %_temp.113 to void (%class.support.Array*, i64)*
%_temp.112 = getelementptr %dv.support.Array, %dv.support.Array* @_J_dv_13support.Array, i32 0, i32 7
store void (%class.support.Array*, i64)* %_temp.114, void (%class.support.Array*, i64)** %_temp.112
%_temp.115 = getelementptr %dv.java.lang.Object, %dv.java.lang.Object* @_J_dv_16java.lang.Object, i32 0, i32 8
%_temp.117 = load void (%class.java.lang.Object*, i64, i32)*, void (%class.java.lang.Object*, i64, i32)** %_temp.115
%_temp.118 = bitcast void (%class.java.lang.Object*, i64, i32)* %_temp.117 to void (%class.support.Array*, i64, i32)*
%_temp.116 = getelementptr %dv.support.Array, %dv.support.Array* @_J_dv_13support.Array, i32 0, i32 8
store void (%class.support.Array*, i64, i32)* %_temp.118, void (%class.support.Array*, i64, i32)** %_temp.116
%_temp.119 = getelementptr %dv.java.lang.Object, %dv.java.lang.Object* @_J_dv_16java.lang.Object, i32 0, i32 9
%_temp.121 = load void (%class.java.lang.Object*)*, void (%class.java.lang.Object*)** %_temp.119
%_temp.122 = bitcast void (%class.java.lang.Object*)* %_temp.121 to void (%class.support.Array*)*
%_temp.120 = getelementptr %dv.support.Array, %dv.support.Array* @_J_dv_13support.Array, i32 0, i32 9
store void (%class.support.Array*)* %_temp.122, void (%class.support.Array*)** %_temp.120
%_temp.123 = getelementptr %dv.support.Array, %dv.support.Array* @_J_dv_13support.Array, i32 0, i32 10
store %class.java.lang.Object* (%class.support.Array*)* @_J_13support.Array_5clone_void, %class.java.lang.Object* (%class.support.Array*)** %_temp.123
%_temp.124 = getelementptr %dv.java.lang.Object, %dv.java.lang.Object* @_J_dv_16java.lang.Object, i32 0, i32 11
%_temp.126 = load void (%class.java.lang.Object*)*, void (%class.java.lang.Object*)** %_temp.124
%_temp.127 = bitcast void (%class.java.lang.Object*)* %_temp.126 to void (%class.support.Array*)*
%_temp.125 = getelementptr %dv.support.Array, %dv.support.Array* @_J_dv_13support.Array, i32 0, i32 11
store void (%class.support.Array*)* %_temp.127, void (%class.support.Array*)** %_temp.125
ret void
}
