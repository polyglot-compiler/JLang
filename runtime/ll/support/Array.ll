%dv.class.support.Array = type {i8*, i1 (%class.class.support.Array*, %class.java.lang.Object*)*, %class.java.lang.Class* (%class.class.support.Array*)*, i32 (%class.class.support.Array*)*, void (%class.class.support.Array*)*, void (%class.class.support.Array*)*, %class.java.lang.String* (%class.class.support.Array*)*, void (%class.class.support.Array*, i64)*, void (%class.class.support.Array*, i64, i32)*, void (%class.class.support.Array*)*, %class.java.lang.Object* (%class.class.support.Array*)*, void (%class.class.support.Array*)*}
%class.support.Array = type {%dv.support.Array*, i32, i8*}
%dv.java.lang.Object = type {i8*, i1 (%class.java.lang.Object*, %class.java.lang.Object*)*, %class.java.lang.Class* (%class.java.lang.Object*)*, i32 (%class.java.lang.Object*)*, void (%class.java.lang.Object*)*, void (%class.java.lang.Object*)*, %class.java.lang.String* (%class.java.lang.Object*)*, void (%class.java.lang.Object*, i64)*, void (%class.java.lang.Object*, i64, i32)*, void (%class.java.lang.Object*)*, %class.java.lang.Object* (%class.java.lang.Object*)*, void (%class.java.lang.Object*)*}
%dv.java.lang.CloneNotSupportedException = type opaque
%class.java.lang.CloneNotSupportedException = type opaque
%class.java.lang.Class = type opaque
%class.class.support.Array = type {%dv.class.support.Array*, i32}
%class.java.lang.Object = type {%dv.java.lang.Object*}
%class.java.lang.String = type opaque
%dv.support.Array = type {i8*, i1 (%class.support.Array*, %class.java.lang.Object*)*, %class.java.lang.Class* (%class.support.Array*)*, i32 (%class.support.Array*)*, void (%class.support.Array*)*, void (%class.support.Array*)*, %class.java.lang.String* (%class.support.Array*)*, void (%class.support.Array*, i64)*, void (%class.support.Array*, i64, i32)*, void (%class.support.Array*)*, %class.java.lang.Object* (%class.support.Array*)*, void (%class.support.Array*)*}
@_J_dv_13support.Array = global %dv.support.Array zeroinitializer
@_J_size_16java.lang.Object = external global i64
@_J_dv_16java.lang.Object = external global %dv.java.lang.Object
@_J_size_36java.lang.CloneNotSupportedException = external global i64
@_J_dv_36java.lang.CloneNotSupportedException = external global %dv.java.lang.CloneNotSupportedException
%__ctortype = type { i32, void ()*, i8* }
@llvm.global_ctors = appending global [1 x %__ctortype] [%__ctortype { i32 65535, void ()* @_J_init_13support.Array, i8* null }]
declare i8* @malloc(i64 %size)

declare void @_J_16java.lang.Object__constructor__void(%class.java.lang.Object* %arg_0)

declare %class.java.lang.Object* @_J_16java.lang.Object_5clone_void(%class.java.lang.Object* %arg_0)

declare void @_J_init_16java.lang.Object()

define void @_J_13support.Array__constructor__i32(%class.support.Array* %_this, i32 %length) {
%flat$3 = alloca i32, i32 1
%flat$4 = alloca %class.support.Array*, i32 1
%i = alloca i32, i32 1
%loop$0 = alloca i1, i32 1
%arg_length = alloca i32, i32 1
store i32 %length, i32* %arg_length
%_temp.0 = bitcast %class.support.Array* %_this to %class.java.lang.Object*
call void @_J_16java.lang.Object__constructor__void(%class.java.lang.Object* %_temp.0)
%_temp.4 = getelementptr %class.support.Array, %class.support.Array* %_this, i32 0, i32 1
%_temp.3 = load i32, i32* %arg_length
store i32 %_temp.3, i32* %_temp.4
store i32 0, i32* %i
store i32 0, i32* %i
store i1 0, i1* %loop$0
store i1 0, i1* %loop$0
br label %loop.head.0
loop.head.0:
br label %label.6
label.6:
%_temp.7 = load i1, i1* %loop$0
br i1 %_temp.7, label %label.0, label %label.1
label.0:
%_temp.8 = load i32, i32* %i
%_temp.9 = add i32 %_temp.8, 1
store i32 %_temp.9, i32* %flat$3
%_temp.11 = load i32, i32* %flat$3
store i32 %_temp.11, i32* %i

%_NOP.0 = add i64 0, 0
br label %label.2
label.1:
br label %label.2
label.2:
%_temp.13 = load i32, i32* %i
%_temp.157 = alloca i32, i32 1
store i32 %_temp.13, i32* %_temp.157
%_temp.14 = load i32, i32* %arg_length
%_temp.158 = load i32, i32* %_temp.157
%_temp.15 = icmp slt i32 %_temp.158, %_temp.14
store i1 %_temp.15, i1* %loop$0
%_temp.16 = load i1, i1* %loop$0
br i1 %_temp.16, label %label.3, label %label.4
label.3:
%_temp.17 = getelementptr %class.support.Array, %class.support.Array* %_this, i32 0, i32 2
%_temp.20 = load i32, i32* %i
%_temp.25 = sext i32 %_temp.20 to i64
%_elementPtr.1 = getelementptr i8*, i8** %_temp.17, i64 %_temp.25
store i8* null, i8** %_elementPtr.1
br label %label.5
label.4:
br label %loop.end.0
br label %label.5
label.5:
br label %loop.head.0
loop.end.0:

ret void
}

define void @_J_13support.Array__constructor__a_i32(%class.support.Array* %_this, %class.support.Array* %lengths) {
%flat$9 = alloca i32, i32 1
%flat$7 = alloca i1, i32 1
%flat$17 = alloca %class.support.Array*, i32 1
%flat$8 = alloca i32, i32 1
%flat$16 = alloca %class.support.Array*, i32 1
%flat$5 = alloca i32, i32 1
%flat$6 = alloca i32, i32 1
%newLengths = alloca %class.support.Array*, i32 1
%i = alloca i32, i32 1
%loop$2 = alloca i1, i32 1
%loop$1 = alloca i1, i32 1
%flat$11 = alloca i32, i32 1
%flat$10 = alloca i32, i32 1
%flat$15 = alloca i32, i32 1
%flat$14 = alloca i32, i32 1
%flat$13 = alloca i32, i32 1
%flat$12 = alloca i32, i32 1
%arg_lengths = alloca %class.support.Array*, i32 1
store %class.support.Array* %lengths, %class.support.Array** %arg_lengths
%_temp.27 = bitcast %class.support.Array* %_this to %class.java.lang.Object*
call void @_J_16java.lang.Object__constructor__void(%class.java.lang.Object* %_temp.27)
%_temp.28 = load %class.support.Array*, %class.support.Array** %arg_lengths
%_result.2 = getelementptr %class.support.Array, %class.support.Array* %_temp.28, i32 0, i32 2
%_temp.29 = sext i32 0 to i64
%_elementPtr.2 = getelementptr i8*, i8** %_result.2, i64 %_temp.29
%_temp.30 = load i8*, i8** %_elementPtr.2
%_temp.31 = ptrtoint i8* %_temp.30 to i32
store i32 %_temp.31, i32* %flat$5
%_temp.35 = getelementptr %class.support.Array, %class.support.Array* %_this, i32 0, i32 1
%_temp.34 = load i32, i32* %flat$5
store i32 %_temp.34, i32* %_temp.35
%_temp.36 = load %class.support.Array*, %class.support.Array** %arg_lengths
%_temp.37 = getelementptr %class.support.Array, %class.support.Array* %_temp.36, i32 0, i32 1
%_temp.38 = load i32, i32* %_temp.37
store i32 %_temp.38, i32* %flat$6
%_temp.39 = load i32, i32* %flat$6
%_temp.40 = icmp eq i32 %_temp.39, 1
store i1 %_temp.40, i1* %flat$7
%_temp.41 = load i1, i1* %flat$7
br i1 %_temp.41, label %label.7, label %label.8
label.7:
ret void
br label %label.9
label.8:
br label %label.9
label.9:
%_temp.42 = load %class.support.Array*, %class.support.Array** %arg_lengths
%_temp.43 = getelementptr %class.support.Array, %class.support.Array* %_temp.42, i32 0, i32 1
%_temp.44 = load i32, i32* %_temp.43
store i32 %_temp.44, i32* %flat$8
%_temp.45 = load i32, i32* %flat$8
%_temp.46 = sub i32 %_temp.45, 1
store i32 %_temp.46, i32* %flat$9
store %class.support.Array* null, %class.support.Array** %newLengths
%_temp.49 = load i32, i32* %flat$9
%_mulByEightVar.0 = mul i32 8, %_temp.49
%_addTwoVar.0 = add i32 2, %_mulByEightVar.0
%_sizeCastVar.0 = sext i32 %_addTwoVar.0 to i64
%_temp.159 = alloca i64, i32 1
store i64 %_sizeCastVar.0, i64* %_temp.159
%_temp.160 = load i64, i64* %_temp.159

%_temp.50 = call i8* @malloc(i64 %_temp.160)
%_temp.51 = bitcast i8* %_temp.50 to %class.support.Array*
%_temp.52 = getelementptr %class.support.Array, %class.support.Array* %_temp.51, i32 0, i32 0
store %dv.support.Array* @_J_dv_13support.Array, %dv.support.Array** %_temp.52
%_temp.48 = load i32, i32* %flat$9
%_temp.161 = alloca i32, i32 1
store i32 %_temp.48, i32* %_temp.161
%_temp.162 = load i32, i32* %_temp.161
call void @_J_13support.Array__constructor__i32(%class.support.Array* %_temp.51, i32 %_temp.162)
store %class.support.Array* %_temp.51, %class.support.Array** %newLengths
store i32 0, i32* %i
store i32 0, i32* %i
store i1 0, i1* %loop$1
store i1 0, i1* %loop$1
br label %loop.head.1
loop.head.1:
br label %label.16
label.16:
%_temp.55 = load i1, i1* %loop$1
br i1 %_temp.55, label %label.10, label %label.11
label.10:
%_temp.56 = load i32, i32* %i
%_temp.57 = add i32 %_temp.56, 1
store i32 %_temp.57, i32* %flat$10
%_temp.59 = load i32, i32* %flat$10
store i32 %_temp.59, i32* %i

%_NOP.1 = add i64 0, 0
br label %label.12
label.11:
br label %label.12
label.12:
%_temp.60 = load %class.support.Array*, %class.support.Array** %newLengths
%_temp.61 = getelementptr %class.support.Array, %class.support.Array* %_temp.60, i32 0, i32 1
%_temp.62 = load i32, i32* %_temp.61
store i32 %_temp.62, i32* %flat$11
%_temp.64 = load i32, i32* %i
%_temp.163 = alloca i32, i32 1
store i32 %_temp.64, i32* %_temp.163
%_temp.65 = load i32, i32* %flat$11
%_temp.164 = load i32, i32* %_temp.163
%_temp.66 = icmp slt i32 %_temp.164, %_temp.65
store i1 %_temp.66, i1* %loop$1
%_temp.67 = load i1, i1* %loop$1
br i1 %_temp.67, label %label.13, label %label.14
label.13:
%_temp.68 = load i32, i32* %i
%_temp.69 = add i32 %_temp.68, 1
store i32 %_temp.69, i32* %flat$12
%_temp.70 = load %class.support.Array*, %class.support.Array** %arg_lengths
%_result.3 = getelementptr %class.support.Array, %class.support.Array* %_temp.70, i32 0, i32 2
%_temp.71 = load i32, i32* %flat$12
%_temp.72 = sext i32 %_temp.71 to i64
%_elementPtr.3 = getelementptr i8*, i8** %_result.3, i64 %_temp.72
%_temp.73 = load i8*, i8** %_elementPtr.3
%_temp.74 = ptrtoint i8* %_temp.73 to i32
store i32 %_temp.74, i32* %flat$13
%_temp.80 = load i32, i32* %flat$13
%_temp.82 = inttoptr i32 %_temp.80 to i8*
%_temp.75 = load %class.support.Array*, %class.support.Array** %newLengths
%_result.5 = getelementptr %class.support.Array, %class.support.Array* %_temp.75, i32 0, i32 2
%_temp.76 = load i32, i32* %i
%_temp.81 = sext i32 %_temp.76 to i64
%_elementPtr.5 = getelementptr i8*, i8** %_result.5, i64 %_temp.81
store i8* %_temp.82, i8** %_elementPtr.5
br label %label.15
label.14:
br label %loop.end.1
br label %label.15
label.15:
br label %loop.head.1
loop.end.1:

store i32 0, i32* %i
store i32 0, i32* %i
store i1 0, i1* %loop$2
store i1 0, i1* %loop$2
br label %loop.head.2
loop.head.2:
br label %label.23
label.23:
%_temp.85 = load i1, i1* %loop$2
br i1 %_temp.85, label %label.17, label %label.18
label.17:
%_temp.86 = load i32, i32* %i
%_temp.87 = add i32 %_temp.86, 1
store i32 %_temp.87, i32* %flat$14
%_temp.89 = load i32, i32* %flat$14
store i32 %_temp.89, i32* %i

%_NOP.2 = add i64 0, 0
br label %label.19
label.18:
br label %label.19
label.19:
%_temp.90 = getelementptr %class.support.Array, %class.support.Array* %_this, i32 0, i32 1
%_temp.91 = load i32, i32* %_temp.90
store i32 %_temp.91, i32* %flat$15
%_temp.93 = load i32, i32* %i
%_temp.165 = alloca i32, i32 1
store i32 %_temp.93, i32* %_temp.165
%_temp.94 = load i32, i32* %flat$15
%_temp.166 = load i32, i32* %_temp.165
%_temp.95 = icmp slt i32 %_temp.166, %_temp.94
store i1 %_temp.95, i1* %loop$2
%_temp.96 = load i1, i1* %loop$2
br i1 %_temp.96, label %label.20, label %label.21
label.20:
store %class.support.Array* %_this, %class.support.Array** %flat$16

%_temp.100 = call i8* @malloc(i64 16)
%_temp.101 = bitcast i8* %_temp.100 to %class.support.Array*
%_temp.102 = getelementptr %class.support.Array, %class.support.Array* %_temp.101, i32 0, i32 0
store %dv.support.Array* @_J_dv_13support.Array, %dv.support.Array** %_temp.102
%_temp.99 = load %class.support.Array*, %class.support.Array** %newLengths
%_temp.167 = alloca %class.support.Array*, i32 1
store %class.support.Array* %_temp.99, %class.support.Array** %_temp.167
%_temp.168 = load %class.support.Array*, %class.support.Array** %_temp.167
call void @_J_13support.Array__constructor__a_i32(%class.support.Array* %_temp.101, %class.support.Array* %_temp.168)
store %class.support.Array* %_temp.101, %class.support.Array** %flat$17
%_temp.108 = load %class.support.Array*, %class.support.Array** %flat$17
%_temp.109 = bitcast %class.support.Array* %_temp.108 to %class.java.lang.Object*
%_temp.111 = bitcast %class.java.lang.Object* %_temp.109 to i8*
%_temp.103 = load %class.support.Array*, %class.support.Array** %flat$16
%_result.7 = getelementptr %class.support.Array, %class.support.Array* %_temp.103, i32 0, i32 2
%_temp.104 = load i32, i32* %i
%_temp.110 = sext i32 %_temp.104 to i64
%_elementPtr.7 = getelementptr i8*, i8** %_result.7, i64 %_temp.110
store i8* %_temp.111, i8** %_elementPtr.7
br label %label.22
label.21:
br label %loop.end.2
br label %label.22
label.22:
br label %loop.head.2
loop.end.2:

ret void
}

define %class.java.lang.Object* @_J_13support.Array_5clone_void(%class.support.Array* %_this) {
%flat$18 = alloca %class.java.lang.Object*, i32 1
%_temp.113 = bitcast %class.java.lang.Object* (%class.java.lang.Object*)* @_J_16java.lang.Object_5clone_void to %class.java.lang.Object* (%class.support.Array*)*
%_temp.114 = call %class.java.lang.Object* %_temp.113(%class.support.Array* %_this)
store %class.java.lang.Object* %_temp.114, %class.java.lang.Object** %flat$18
%_temp.115 = load %class.java.lang.Object*, %class.java.lang.Object** %flat$18
ret %class.java.lang.Object* %_temp.115
ret %class.java.lang.Object* null
}

define void @_J_init_13support.Array() {
call void @_J_init_16java.lang.Object()
%_temp.116 = getelementptr %dv.java.lang.Object, %dv.java.lang.Object* @_J_dv_16java.lang.Object, i32 0, i32 1
%_temp.118 = load i1 (%class.java.lang.Object*, %class.java.lang.Object*)*, i1 (%class.java.lang.Object*, %class.java.lang.Object*)** %_temp.116
%_temp.119 = bitcast i1 (%class.java.lang.Object*, %class.java.lang.Object*)* %_temp.118 to i1 (%class.support.Array*, %class.java.lang.Object*)*
%_temp.117 = getelementptr %dv.support.Array, %dv.support.Array* @_J_dv_13support.Array, i32 0, i32 1
store i1 (%class.support.Array*, %class.java.lang.Object*)* %_temp.119, i1 (%class.support.Array*, %class.java.lang.Object*)** %_temp.117
%_temp.120 = getelementptr %dv.java.lang.Object, %dv.java.lang.Object* @_J_dv_16java.lang.Object, i32 0, i32 2
%_temp.122 = load %class.java.lang.Class* (%class.java.lang.Object*)*, %class.java.lang.Class* (%class.java.lang.Object*)** %_temp.120
%_temp.123 = bitcast %class.java.lang.Class* (%class.java.lang.Object*)* %_temp.122 to %class.java.lang.Class* (%class.support.Array*)*
%_temp.121 = getelementptr %dv.support.Array, %dv.support.Array* @_J_dv_13support.Array, i32 0, i32 2
store %class.java.lang.Class* (%class.support.Array*)* %_temp.123, %class.java.lang.Class* (%class.support.Array*)** %_temp.121
%_temp.124 = getelementptr %dv.java.lang.Object, %dv.java.lang.Object* @_J_dv_16java.lang.Object, i32 0, i32 3
%_temp.126 = load i32 (%class.java.lang.Object*)*, i32 (%class.java.lang.Object*)** %_temp.124
%_temp.127 = bitcast i32 (%class.java.lang.Object*)* %_temp.126 to i32 (%class.support.Array*)*
%_temp.125 = getelementptr %dv.support.Array, %dv.support.Array* @_J_dv_13support.Array, i32 0, i32 3
store i32 (%class.support.Array*)* %_temp.127, i32 (%class.support.Array*)** %_temp.125
%_temp.128 = getelementptr %dv.java.lang.Object, %dv.java.lang.Object* @_J_dv_16java.lang.Object, i32 0, i32 4
%_temp.130 = load void (%class.java.lang.Object*)*, void (%class.java.lang.Object*)** %_temp.128
%_temp.131 = bitcast void (%class.java.lang.Object*)* %_temp.130 to void (%class.support.Array*)*
%_temp.129 = getelementptr %dv.support.Array, %dv.support.Array* @_J_dv_13support.Array, i32 0, i32 4
store void (%class.support.Array*)* %_temp.131, void (%class.support.Array*)** %_temp.129
%_temp.132 = getelementptr %dv.java.lang.Object, %dv.java.lang.Object* @_J_dv_16java.lang.Object, i32 0, i32 5
%_temp.134 = load void (%class.java.lang.Object*)*, void (%class.java.lang.Object*)** %_temp.132
%_temp.135 = bitcast void (%class.java.lang.Object*)* %_temp.134 to void (%class.support.Array*)*
%_temp.133 = getelementptr %dv.support.Array, %dv.support.Array* @_J_dv_13support.Array, i32 0, i32 5
store void (%class.support.Array*)* %_temp.135, void (%class.support.Array*)** %_temp.133
%_temp.136 = getelementptr %dv.java.lang.Object, %dv.java.lang.Object* @_J_dv_16java.lang.Object, i32 0, i32 6
%_temp.138 = load %class.java.lang.String* (%class.java.lang.Object*)*, %class.java.lang.String* (%class.java.lang.Object*)** %_temp.136
%_temp.139 = bitcast %class.java.lang.String* (%class.java.lang.Object*)* %_temp.138 to %class.java.lang.String* (%class.support.Array*)*
%_temp.137 = getelementptr %dv.support.Array, %dv.support.Array* @_J_dv_13support.Array, i32 0, i32 6
store %class.java.lang.String* (%class.support.Array*)* %_temp.139, %class.java.lang.String* (%class.support.Array*)** %_temp.137
%_temp.140 = getelementptr %dv.java.lang.Object, %dv.java.lang.Object* @_J_dv_16java.lang.Object, i32 0, i32 7
%_temp.142 = load void (%class.java.lang.Object*, i64)*, void (%class.java.lang.Object*, i64)** %_temp.140
%_temp.143 = bitcast void (%class.java.lang.Object*, i64)* %_temp.142 to void (%class.support.Array*, i64)*
%_temp.141 = getelementptr %dv.support.Array, %dv.support.Array* @_J_dv_13support.Array, i32 0, i32 7
store void (%class.support.Array*, i64)* %_temp.143, void (%class.support.Array*, i64)** %_temp.141
%_temp.144 = getelementptr %dv.java.lang.Object, %dv.java.lang.Object* @_J_dv_16java.lang.Object, i32 0, i32 8
%_temp.146 = load void (%class.java.lang.Object*, i64, i32)*, void (%class.java.lang.Object*, i64, i32)** %_temp.144
%_temp.147 = bitcast void (%class.java.lang.Object*, i64, i32)* %_temp.146 to void (%class.support.Array*, i64, i32)*
%_temp.145 = getelementptr %dv.support.Array, %dv.support.Array* @_J_dv_13support.Array, i32 0, i32 8
store void (%class.support.Array*, i64, i32)* %_temp.147, void (%class.support.Array*, i64, i32)** %_temp.145
%_temp.148 = getelementptr %dv.java.lang.Object, %dv.java.lang.Object* @_J_dv_16java.lang.Object, i32 0, i32 9
%_temp.150 = load void (%class.java.lang.Object*)*, void (%class.java.lang.Object*)** %_temp.148
%_temp.151 = bitcast void (%class.java.lang.Object*)* %_temp.150 to void (%class.support.Array*)*
%_temp.149 = getelementptr %dv.support.Array, %dv.support.Array* @_J_dv_13support.Array, i32 0, i32 9
store void (%class.support.Array*)* %_temp.151, void (%class.support.Array*)** %_temp.149
%_temp.152 = getelementptr %dv.support.Array, %dv.support.Array* @_J_dv_13support.Array, i32 0, i32 10
store %class.java.lang.Object* (%class.support.Array*)* @_J_13support.Array_5clone_void, %class.java.lang.Object* (%class.support.Array*)** %_temp.152
%_temp.153 = getelementptr %dv.java.lang.Object, %dv.java.lang.Object* @_J_dv_16java.lang.Object, i32 0, i32 11
%_temp.155 = load void (%class.java.lang.Object*)*, void (%class.java.lang.Object*)** %_temp.153
%_temp.156 = bitcast void (%class.java.lang.Object*)* %_temp.155 to void (%class.support.Array*)*
%_temp.154 = getelementptr %dv.support.Array, %dv.support.Array* @_J_dv_13support.Array, i32 0, i32 11
store void (%class.support.Array*)* %_temp.156, void (%class.support.Array*)** %_temp.154
ret void
}

