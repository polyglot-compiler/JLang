%dv.java.lang.String = type {i8*, i1 (%class.java.lang.String*, %class.java.lang.Object*)*, %class.java.lang.Class* (%class.java.lang.String*)*, i32 (%class.java.lang.String*)*, void (%class.java.lang.String*)*, void (%class.java.lang.String*)*, %class.java.lang.String* (%class.java.lang.String*)*, void (%class.java.lang.String*, i64)*, void (%class.java.lang.String*, i64, i32)*, void (%class.java.lang.String*)*, %class.java.lang.Object* (%class.java.lang.String*)*, void (%class.java.lang.String*)*}
%dv.class.support.Array = type {i8*, i1 (%class.class.support.Array*, %class.java.lang.Object*)*, %class.java.lang.Class* (%class.class.support.Array*)*, i32 (%class.class.support.Array*)*, void (%class.class.support.Array*)*, void (%class.class.support.Array*)*, %class.java.lang.String* (%class.class.support.Array*)*, void (%class.class.support.Array*, i64)*, void (%class.class.support.Array*, i64, i32)*, void (%class.class.support.Array*)*, %class.java.lang.Object* (%class.class.support.Array*)*, void (%class.class.support.Array*)*}
%class.support.Array = type {%dv.support.Array*, i32, i8*}
%dv.java.lang.Object = type {i8*, i1 (%class.java.lang.Object*, %class.java.lang.Object*)*, %class.java.lang.Class* (%class.java.lang.Object*)*, i32 (%class.java.lang.Object*)*, void (%class.java.lang.Object*)*, void (%class.java.lang.Object*)*, %class.java.lang.String* (%class.java.lang.Object*)*, void (%class.java.lang.Object*, i64)*, void (%class.java.lang.Object*, i64, i32)*, void (%class.java.lang.Object*)*, %class.java.lang.Object* (%class.java.lang.Object*)*, void (%class.java.lang.Object*)*}
%class.java.lang.Class = type opaque
%class.class.support.Array = type {%dv.class.support.Array*, i32}
%class.java.lang.Object = type {%dv.java.lang.Object*}
%class.java.lang.String = type {%dv.java.lang.String*, %class.support.Array*}
%dv.support.Array = type {i8*, i1 (%class.support.Array*, %class.java.lang.Object*)*, %class.java.lang.Class* (%class.support.Array*)*, i32 (%class.support.Array*)*, void (%class.support.Array*)*, void (%class.support.Array*)*, %class.java.lang.String* (%class.support.Array*)*, void (%class.support.Array*, i64)*, void (%class.support.Array*, i64, i32)*, void (%class.support.Array*)*, %class.java.lang.Object* (%class.support.Array*)*, void (%class.support.Array*)*}
@_J_dv_13support.Array = external global %dv.support.Array
@_J_size_16java.lang.String = global i64 zeroinitializer
@_J_dv_16java.lang.String = global %dv.java.lang.String zeroinitializer
@_J_size_16java.lang.Object = external global i64
@_J_dv_16java.lang.Object = external global %dv.java.lang.Object
%__ctortype = type { i32, void ()*, i8* }
@llvm.global_ctors = appending global [1 x %__ctortype] [%__ctortype { i32 65535, void ()* @_J_init_16java.lang.String, i8* null }]
declare i8* @malloc(i64 %size)

declare void @_J_16java.lang.Object__constructor__void(%class.java.lang.Object* %arg_0)

declare void @_J_13support.Array__constructor__i32(%class.support.Array* %arg_0, i32 %arg_1)

declare void @_J_init_16java.lang.Object()

define void @_J_16java.lang.String__constructor__a_i16(%class.java.lang.String* %_this, %class.support.Array* %value) {
%arg_value = alloca %class.support.Array*, i32 1
store %class.support.Array* %value, %class.support.Array** %arg_value
%_temp.0 = bitcast %class.java.lang.String* %_this to %class.java.lang.Object*
call void @_J_16java.lang.Object__constructor__void(%class.java.lang.Object* %_temp.0)
%_temp.4 = getelementptr %class.java.lang.String, %class.java.lang.String* %_this, i32 0, i32 1
%_temp.3 = load %class.support.Array*, %class.support.Array** %arg_value
store %class.support.Array* %_temp.3, %class.support.Array** %_temp.4
ret void
}

define void @_J_16java.lang.String__constructor__a_i8(%class.java.lang.String* %_this, %class.support.Array* %bytes) {
%flat$7 = alloca i16, i32 1
%flat$5 = alloca %class.support.Array*, i32 1
%flat$6 = alloca i8, i32 1
%flat$3 = alloca i32, i32 1
%flat$4 = alloca i32, i32 1
%flat$1 = alloca i32, i32 1
%flat$2 = alloca %class.support.Array*, i32 1
%i = alloca i32, i32 1
%loop$0 = alloca i1, i32 1
%arg_bytes = alloca %class.support.Array*, i32 1
store %class.support.Array* %bytes, %class.support.Array** %arg_bytes
%_temp.5 = bitcast %class.java.lang.String* %_this to %class.java.lang.Object*
call void @_J_16java.lang.Object__constructor__void(%class.java.lang.Object* %_temp.5)
%_temp.6 = load %class.support.Array*, %class.support.Array** %arg_bytes
%_temp.7 = getelementptr %class.support.Array, %class.support.Array* %_temp.6, i32 0, i32 1
%_temp.8 = load i32, i32* %_temp.7
store i32 %_temp.8, i32* %flat$1
%_temp.10 = load i32, i32* %flat$1
%_mulByEightVar.0 = mul i32 8, %_temp.10
%_addTwoVar.0 = add i32 2, %_mulByEightVar.0
%_sizeCastVar.0 = sext i32 %_addTwoVar.0 to i64
%_temp.94 = alloca i64, i32 1
store i64 %_sizeCastVar.0, i64* %_temp.94
%_temp.95 = load i64, i64* %_temp.94

%_temp.11 = call i8* @malloc(i64 %_temp.95)
%_temp.12 = bitcast i8* %_temp.11 to %class.support.Array*
%_temp.13 = getelementptr %class.support.Array, %class.support.Array* %_temp.12, i32 0, i32 0
store %dv.support.Array* @_J_dv_13support.Array, %dv.support.Array** %_temp.13
%_temp.9 = load i32, i32* %flat$1
%_temp.96 = alloca i32, i32 1
store i32 %_temp.9, i32* %_temp.96
%_temp.97 = load i32, i32* %_temp.96
call void @_J_13support.Array__constructor__i32(%class.support.Array* %_temp.12, i32 %_temp.97)
store %class.support.Array* %_temp.12, %class.support.Array** %flat$2
%_temp.17 = getelementptr %class.java.lang.String, %class.java.lang.String* %_this, i32 0, i32 1
%_temp.16 = load %class.support.Array*, %class.support.Array** %flat$2
store %class.support.Array* %_temp.16, %class.support.Array** %_temp.17
store i32 0, i32* %i
store i32 0, i32* %i
store i1 0, i1* %loop$0
store i1 0, i1* %loop$0
br label %loop.head.0
loop.head.0:
br label %label.6
label.6:
%_temp.20 = load i1, i1* %loop$0
br i1 %_temp.20, label %label.0, label %label.1
label.0:
%_temp.21 = load i32, i32* %i
%_temp.22 = add i32 %_temp.21, 1
store i32 %_temp.22, i32* %flat$3
%_temp.24 = load i32, i32* %flat$3
store i32 %_temp.24, i32* %i

%_NOP.0 = add i64 0, 0
br label %label.2
label.1:
br label %label.2
label.2:
%_temp.25 = load %class.support.Array*, %class.support.Array** %arg_bytes
%_temp.26 = getelementptr %class.support.Array, %class.support.Array* %_temp.25, i32 0, i32 1
%_temp.27 = load i32, i32* %_temp.26
store i32 %_temp.27, i32* %flat$4
%_temp.29 = load i32, i32* %i
%_temp.98 = alloca i32, i32 1
store i32 %_temp.29, i32* %_temp.98
%_temp.30 = load i32, i32* %flat$4
%_temp.99 = load i32, i32* %_temp.98
%_temp.31 = icmp slt i32 %_temp.99, %_temp.30
store i1 %_temp.31, i1* %loop$0
%_temp.32 = load i1, i1* %loop$0
br i1 %_temp.32, label %label.3, label %label.4
label.3:
%_temp.33 = getelementptr %class.java.lang.String, %class.java.lang.String* %_this, i32 0, i32 1
%_temp.34 = load %class.support.Array*, %class.support.Array** %_temp.33
store %class.support.Array* %_temp.34, %class.support.Array** %flat$5
%_temp.35 = load %class.support.Array*, %class.support.Array** %arg_bytes
%_result.0 = getelementptr %class.support.Array, %class.support.Array* %_temp.35, i32 0, i32 2
%_temp.36 = load i32, i32* %i
%_temp.37 = sext i32 %_temp.36 to i64
%_elementPtr.0 = getelementptr i8*, i8** %_result.0, i64 %_temp.37
%_temp.38 = load i8*, i8** %_elementPtr.0
%_temp.39 = ptrtoint i8* %_temp.38 to i8
store i8 %_temp.39, i8* %flat$6
%_temp.40 = load i8, i8* %flat$6
%_temp.41 = sext i8 %_temp.40 to i16
store i16 %_temp.41, i16* %flat$7
%_temp.47 = load i16, i16* %flat$7
%_temp.49 = inttoptr i16 %_temp.47 to i8*
%_temp.42 = load %class.support.Array*, %class.support.Array** %flat$5
%_result.2 = getelementptr %class.support.Array, %class.support.Array* %_temp.42, i32 0, i32 2
%_temp.43 = load i32, i32* %i
%_temp.48 = sext i32 %_temp.43 to i64
%_elementPtr.2 = getelementptr i8*, i8** %_result.2, i64 %_temp.48
store i8* %_temp.49, i8** %_elementPtr.2
br label %label.5
label.4:
br label %loop.end.0
br label %label.5
label.5:
br label %loop.head.0
loop.end.0:

ret void
}

define void @_J_init_16java.lang.String() {
call void @_J_init_16java.lang.Object()
%_temp.50 = getelementptr %dv.java.lang.Object, %dv.java.lang.Object* @_J_dv_16java.lang.Object, i32 0, i32 1
%_temp.52 = load i1 (%class.java.lang.Object*, %class.java.lang.Object*)*, i1 (%class.java.lang.Object*, %class.java.lang.Object*)** %_temp.50
%_temp.53 = bitcast i1 (%class.java.lang.Object*, %class.java.lang.Object*)* %_temp.52 to i1 (%class.java.lang.String*, %class.java.lang.Object*)*
%_temp.51 = getelementptr %dv.java.lang.String, %dv.java.lang.String* @_J_dv_16java.lang.String, i32 0, i32 1
store i1 (%class.java.lang.String*, %class.java.lang.Object*)* %_temp.53, i1 (%class.java.lang.String*, %class.java.lang.Object*)** %_temp.51
%_temp.54 = getelementptr %dv.java.lang.Object, %dv.java.lang.Object* @_J_dv_16java.lang.Object, i32 0, i32 2
%_temp.56 = load %class.java.lang.Class* (%class.java.lang.Object*)*, %class.java.lang.Class* (%class.java.lang.Object*)** %_temp.54
%_temp.57 = bitcast %class.java.lang.Class* (%class.java.lang.Object*)* %_temp.56 to %class.java.lang.Class* (%class.java.lang.String*)*
%_temp.55 = getelementptr %dv.java.lang.String, %dv.java.lang.String* @_J_dv_16java.lang.String, i32 0, i32 2
store %class.java.lang.Class* (%class.java.lang.String*)* %_temp.57, %class.java.lang.Class* (%class.java.lang.String*)** %_temp.55
%_temp.58 = getelementptr %dv.java.lang.Object, %dv.java.lang.Object* @_J_dv_16java.lang.Object, i32 0, i32 3
%_temp.60 = load i32 (%class.java.lang.Object*)*, i32 (%class.java.lang.Object*)** %_temp.58
%_temp.61 = bitcast i32 (%class.java.lang.Object*)* %_temp.60 to i32 (%class.java.lang.String*)*
%_temp.59 = getelementptr %dv.java.lang.String, %dv.java.lang.String* @_J_dv_16java.lang.String, i32 0, i32 3
store i32 (%class.java.lang.String*)* %_temp.61, i32 (%class.java.lang.String*)** %_temp.59
%_temp.62 = getelementptr %dv.java.lang.Object, %dv.java.lang.Object* @_J_dv_16java.lang.Object, i32 0, i32 4
%_temp.64 = load void (%class.java.lang.Object*)*, void (%class.java.lang.Object*)** %_temp.62
%_temp.65 = bitcast void (%class.java.lang.Object*)* %_temp.64 to void (%class.java.lang.String*)*
%_temp.63 = getelementptr %dv.java.lang.String, %dv.java.lang.String* @_J_dv_16java.lang.String, i32 0, i32 4
store void (%class.java.lang.String*)* %_temp.65, void (%class.java.lang.String*)** %_temp.63
%_temp.66 = getelementptr %dv.java.lang.Object, %dv.java.lang.Object* @_J_dv_16java.lang.Object, i32 0, i32 5
%_temp.68 = load void (%class.java.lang.Object*)*, void (%class.java.lang.Object*)** %_temp.66
%_temp.69 = bitcast void (%class.java.lang.Object*)* %_temp.68 to void (%class.java.lang.String*)*
%_temp.67 = getelementptr %dv.java.lang.String, %dv.java.lang.String* @_J_dv_16java.lang.String, i32 0, i32 5
store void (%class.java.lang.String*)* %_temp.69, void (%class.java.lang.String*)** %_temp.67
%_temp.70 = getelementptr %dv.java.lang.Object, %dv.java.lang.Object* @_J_dv_16java.lang.Object, i32 0, i32 6
%_temp.72 = load %class.java.lang.String* (%class.java.lang.Object*)*, %class.java.lang.String* (%class.java.lang.Object*)** %_temp.70
%_temp.73 = bitcast %class.java.lang.String* (%class.java.lang.Object*)* %_temp.72 to %class.java.lang.String* (%class.java.lang.String*)*
%_temp.71 = getelementptr %dv.java.lang.String, %dv.java.lang.String* @_J_dv_16java.lang.String, i32 0, i32 6
store %class.java.lang.String* (%class.java.lang.String*)* %_temp.73, %class.java.lang.String* (%class.java.lang.String*)** %_temp.71
%_temp.74 = getelementptr %dv.java.lang.Object, %dv.java.lang.Object* @_J_dv_16java.lang.Object, i32 0, i32 7
%_temp.76 = load void (%class.java.lang.Object*, i64)*, void (%class.java.lang.Object*, i64)** %_temp.74
%_temp.77 = bitcast void (%class.java.lang.Object*, i64)* %_temp.76 to void (%class.java.lang.String*, i64)*
%_temp.75 = getelementptr %dv.java.lang.String, %dv.java.lang.String* @_J_dv_16java.lang.String, i32 0, i32 7
store void (%class.java.lang.String*, i64)* %_temp.77, void (%class.java.lang.String*, i64)** %_temp.75
%_temp.78 = getelementptr %dv.java.lang.Object, %dv.java.lang.Object* @_J_dv_16java.lang.Object, i32 0, i32 8
%_temp.80 = load void (%class.java.lang.Object*, i64, i32)*, void (%class.java.lang.Object*, i64, i32)** %_temp.78
%_temp.81 = bitcast void (%class.java.lang.Object*, i64, i32)* %_temp.80 to void (%class.java.lang.String*, i64, i32)*
%_temp.79 = getelementptr %dv.java.lang.String, %dv.java.lang.String* @_J_dv_16java.lang.String, i32 0, i32 8
store void (%class.java.lang.String*, i64, i32)* %_temp.81, void (%class.java.lang.String*, i64, i32)** %_temp.79
%_temp.82 = getelementptr %dv.java.lang.Object, %dv.java.lang.Object* @_J_dv_16java.lang.Object, i32 0, i32 9
%_temp.84 = load void (%class.java.lang.Object*)*, void (%class.java.lang.Object*)** %_temp.82
%_temp.85 = bitcast void (%class.java.lang.Object*)* %_temp.84 to void (%class.java.lang.String*)*
%_temp.83 = getelementptr %dv.java.lang.String, %dv.java.lang.String* @_J_dv_16java.lang.String, i32 0, i32 9
store void (%class.java.lang.String*)* %_temp.85, void (%class.java.lang.String*)** %_temp.83
%_temp.86 = getelementptr %dv.java.lang.Object, %dv.java.lang.Object* @_J_dv_16java.lang.Object, i32 0, i32 10
%_temp.88 = load %class.java.lang.Object* (%class.java.lang.Object*)*, %class.java.lang.Object* (%class.java.lang.Object*)** %_temp.86
%_temp.89 = bitcast %class.java.lang.Object* (%class.java.lang.Object*)* %_temp.88 to %class.java.lang.Object* (%class.java.lang.String*)*
%_temp.87 = getelementptr %dv.java.lang.String, %dv.java.lang.String* @_J_dv_16java.lang.String, i32 0, i32 10
store %class.java.lang.Object* (%class.java.lang.String*)* %_temp.89, %class.java.lang.Object* (%class.java.lang.String*)** %_temp.87
%_temp.90 = getelementptr %dv.java.lang.Object, %dv.java.lang.Object* @_J_dv_16java.lang.Object, i32 0, i32 11
%_temp.92 = load void (%class.java.lang.Object*)*, void (%class.java.lang.Object*)** %_temp.90
%_temp.93 = bitcast void (%class.java.lang.Object*)* %_temp.92 to void (%class.java.lang.String*)*
%_temp.91 = getelementptr %dv.java.lang.String, %dv.java.lang.String* @_J_dv_16java.lang.String, i32 0, i32 11
store void (%class.java.lang.String*)* %_temp.93, void (%class.java.lang.String*)** %_temp.91
ret void
}

