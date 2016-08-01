%class.SimpleClass = type {%dv.SimpleClass*, i32, i16}
%dv.java.lang.Object = type {i8*, i1 (%class.java.lang.Object*, %class.java.lang.Object*)*, %class.java.lang.Class* (%class.java.lang.Object*)*, i32 (%class.java.lang.Object*)*, void (%class.java.lang.Object*)*, void (%class.java.lang.Object*)*, %class.java.lang.String* (%class.java.lang.Object*)*, void (%class.java.lang.Object*, i64)*, void (%class.java.lang.Object*, i64, i32)*, void (%class.java.lang.Object*)*, %class.java.lang.Object* (%class.java.lang.Object*)*, void (%class.java.lang.Object*)*}
%dv.SimpleClass = type {i8*, i1 (%class.SimpleClass*, %class.java.lang.Object*)*, %class.java.lang.Class* (%class.SimpleClass*)*, i32 (%class.SimpleClass*)*, void (%class.SimpleClass*)*, void (%class.SimpleClass*)*, %class.java.lang.String* (%class.SimpleClass*)*, void (%class.SimpleClass*, i64)*, void (%class.SimpleClass*, i64, i32)*, void (%class.SimpleClass*)*, %class.java.lang.Object* (%class.SimpleClass*)*, void (%class.SimpleClass*)*, i32 (%class.SimpleClass*)*, i32 (%class.SimpleClass*)*}
%class.java.lang.Class = type opaque
%dv.UseSimpleClass = type {i8*, i1 (%class.UseSimpleClass*, %class.java.lang.Object*)*, %class.java.lang.Class* (%class.UseSimpleClass*)*, i32 (%class.UseSimpleClass*)*, void (%class.UseSimpleClass*)*, void (%class.UseSimpleClass*)*, %class.java.lang.String* (%class.UseSimpleClass*)*, void (%class.UseSimpleClass*, i64)*, void (%class.UseSimpleClass*, i64, i32)*, void (%class.UseSimpleClass*)*, %class.java.lang.Object* (%class.UseSimpleClass*)*, void (%class.UseSimpleClass*)*, i32 (%class.UseSimpleClass*)*, i32 (%class.UseSimpleClass*)*, i32 (%class.UseSimpleClass*)*}
%class.UseSimpleClass = type {%dv.UseSimpleClass*, i32, i16}
%class.java.lang.Object = type {%dv.java.lang.Object*}
%class.java.lang.String = type opaque
@_J_size_14UseSimpleClass = global i64 0
@_J_dv_14UseSimpleClass = global %dv.UseSimpleClass zeroinitializer
@_J_size_11SimpleClass = external global i64
@_J_dv_11SimpleClass = external global %dv.SimpleClass
@_J_size_16java.lang.Object = external global i64
@_J_dv_16java.lang.Object = external global %dv.java.lang.Object
%__ctortype = type { i32, void ()*, i8* }
@llvm.global_ctors = appending global [1 x %__ctortype] [%__ctortype { i32 65535, void ()* @_J_init_14UseSimpleClass, i8* null }]
declare i8* @malloc(i64 %size)

declare i32 @_J_11SimpleClass_6method_void(%class.SimpleClass* %arg_0)

declare void @_J_init_11SimpleClass()

define i32 @_J_14UseSimpleClass_6method_void(%class.UseSimpleClass* %_this) {
%flat$3 = alloca i32, i32 1
%flat$4 = alloca i32, i32 1
%_temp.64 = bitcast i32 (%class.SimpleClass*)* @_J_11SimpleClass_6method_void to i32 (%class.UseSimpleClass*)*
%_temp.65 = call i32 %_temp.64(%class.UseSimpleClass* %_this)
store i32 %_temp.65, i32* %flat$3
%_temp.66 = load i32, i32* %flat$3
%_temp.67 = add i32 %_temp.66, 1
store i32 %_temp.67, i32* %flat$4
%_temp.68 = load i32, i32* %flat$4
ret i32 %_temp.68
}

define i32 @_J_14UseSimpleClass_7method2_void(%class.UseSimpleClass* %_this) {
%flat$5 = alloca i32, i32 1
%flat$6 = alloca i32, i32 1
%_dvDoublePtrResult.1 = getelementptr %class.UseSimpleClass, %class.UseSimpleClass* %_this, i32 0, i32 0
%_dvPtrValue.1 = load %dv.UseSimpleClass*, %dv.UseSimpleClass** %_dvDoublePtrResult.1
%_temp.69 = getelementptr %dv.UseSimpleClass, %dv.UseSimpleClass* %_dvPtrValue.1, i32 0, i32 12
%_temp.70 = load i32 (%class.UseSimpleClass*)*, i32 (%class.UseSimpleClass*)** %_temp.69
%_temp.71 = call i32 %_temp.70(%class.UseSimpleClass* %_this)
store i32 %_temp.71, i32* %flat$5
%_temp.72 = load i32, i32* %flat$5
%_temp.73 = add i32 %_temp.72, 1
store i32 %_temp.73, i32* %flat$6
%_temp.74 = load i32, i32* %flat$6
ret i32 %_temp.74
}

define void @_J_init_14UseSimpleClass() {
call void @_J_init_11SimpleClass()
%_temp.75 = getelementptr %dv.UseSimpleClass, %dv.UseSimpleClass* @_J_dv_14UseSimpleClass, i32 0, i32 0
%_temp.76 = bitcast %dv.SimpleClass* @_J_dv_11SimpleClass to i8*
store i8* %_temp.76, i8** %_temp.75
%_temp.77 = getelementptr %dv.SimpleClass, %dv.SimpleClass* @_J_dv_11SimpleClass, i32 0, i32 1
%_temp.79 = load i1 (%class.SimpleClass*, %class.java.lang.Object*)*, i1 (%class.SimpleClass*, %class.java.lang.Object*)** %_temp.77
%_temp.80 = bitcast i1 (%class.SimpleClass*, %class.java.lang.Object*)* %_temp.79 to i1 (%class.UseSimpleClass*, %class.java.lang.Object*)*
%_temp.78 = getelementptr %dv.UseSimpleClass, %dv.UseSimpleClass* @_J_dv_14UseSimpleClass, i32 0, i32 1
store i1 (%class.UseSimpleClass*, %class.java.lang.Object*)* %_temp.80, i1 (%class.UseSimpleClass*, %class.java.lang.Object*)** %_temp.78
%_temp.81 = getelementptr %dv.SimpleClass, %dv.SimpleClass* @_J_dv_11SimpleClass, i32 0, i32 2
%_temp.83 = load %class.java.lang.Class* (%class.SimpleClass*)*, %class.java.lang.Class* (%class.SimpleClass*)** %_temp.81
%_temp.84 = bitcast %class.java.lang.Class* (%class.SimpleClass*)* %_temp.83 to %class.java.lang.Class* (%class.UseSimpleClass*)*
%_temp.82 = getelementptr %dv.UseSimpleClass, %dv.UseSimpleClass* @_J_dv_14UseSimpleClass, i32 0, i32 2
store %class.java.lang.Class* (%class.UseSimpleClass*)* %_temp.84, %class.java.lang.Class* (%class.UseSimpleClass*)** %_temp.82
%_temp.85 = getelementptr %dv.SimpleClass, %dv.SimpleClass* @_J_dv_11SimpleClass, i32 0, i32 3
%_temp.87 = load i32 (%class.SimpleClass*)*, i32 (%class.SimpleClass*)** %_temp.85
%_temp.88 = bitcast i32 (%class.SimpleClass*)* %_temp.87 to i32 (%class.UseSimpleClass*)*
%_temp.86 = getelementptr %dv.UseSimpleClass, %dv.UseSimpleClass* @_J_dv_14UseSimpleClass, i32 0, i32 3
store i32 (%class.UseSimpleClass*)* %_temp.88, i32 (%class.UseSimpleClass*)** %_temp.86
%_temp.89 = getelementptr %dv.SimpleClass, %dv.SimpleClass* @_J_dv_11SimpleClass, i32 0, i32 4
%_temp.91 = load void (%class.SimpleClass*)*, void (%class.SimpleClass*)** %_temp.89
%_temp.92 = bitcast void (%class.SimpleClass*)* %_temp.91 to void (%class.UseSimpleClass*)*
%_temp.90 = getelementptr %dv.UseSimpleClass, %dv.UseSimpleClass* @_J_dv_14UseSimpleClass, i32 0, i32 4
store void (%class.UseSimpleClass*)* %_temp.92, void (%class.UseSimpleClass*)** %_temp.90
%_temp.93 = getelementptr %dv.SimpleClass, %dv.SimpleClass* @_J_dv_11SimpleClass, i32 0, i32 5
%_temp.95 = load void (%class.SimpleClass*)*, void (%class.SimpleClass*)** %_temp.93
%_temp.96 = bitcast void (%class.SimpleClass*)* %_temp.95 to void (%class.UseSimpleClass*)*
%_temp.94 = getelementptr %dv.UseSimpleClass, %dv.UseSimpleClass* @_J_dv_14UseSimpleClass, i32 0, i32 5
store void (%class.UseSimpleClass*)* %_temp.96, void (%class.UseSimpleClass*)** %_temp.94
%_temp.97 = getelementptr %dv.SimpleClass, %dv.SimpleClass* @_J_dv_11SimpleClass, i32 0, i32 6
%_temp.99 = load %class.java.lang.String* (%class.SimpleClass*)*, %class.java.lang.String* (%class.SimpleClass*)** %_temp.97
%_temp.100 = bitcast %class.java.lang.String* (%class.SimpleClass*)* %_temp.99 to %class.java.lang.String* (%class.UseSimpleClass*)*
%_temp.98 = getelementptr %dv.UseSimpleClass, %dv.UseSimpleClass* @_J_dv_14UseSimpleClass, i32 0, i32 6
store %class.java.lang.String* (%class.UseSimpleClass*)* %_temp.100, %class.java.lang.String* (%class.UseSimpleClass*)** %_temp.98
%_temp.101 = getelementptr %dv.SimpleClass, %dv.SimpleClass* @_J_dv_11SimpleClass, i32 0, i32 7
%_temp.103 = load void (%class.SimpleClass*, i64)*, void (%class.SimpleClass*, i64)** %_temp.101
%_temp.104 = bitcast void (%class.SimpleClass*, i64)* %_temp.103 to void (%class.UseSimpleClass*, i64)*
%_temp.102 = getelementptr %dv.UseSimpleClass, %dv.UseSimpleClass* @_J_dv_14UseSimpleClass, i32 0, i32 7
store void (%class.UseSimpleClass*, i64)* %_temp.104, void (%class.UseSimpleClass*, i64)** %_temp.102
%_temp.105 = getelementptr %dv.SimpleClass, %dv.SimpleClass* @_J_dv_11SimpleClass, i32 0, i32 8
%_temp.107 = load void (%class.SimpleClass*, i64, i32)*, void (%class.SimpleClass*, i64, i32)** %_temp.105
%_temp.108 = bitcast void (%class.SimpleClass*, i64, i32)* %_temp.107 to void (%class.UseSimpleClass*, i64, i32)*
%_temp.106 = getelementptr %dv.UseSimpleClass, %dv.UseSimpleClass* @_J_dv_14UseSimpleClass, i32 0, i32 8
store void (%class.UseSimpleClass*, i64, i32)* %_temp.108, void (%class.UseSimpleClass*, i64, i32)** %_temp.106
%_temp.109 = getelementptr %dv.SimpleClass, %dv.SimpleClass* @_J_dv_11SimpleClass, i32 0, i32 9
%_temp.111 = load void (%class.SimpleClass*)*, void (%class.SimpleClass*)** %_temp.109
%_temp.112 = bitcast void (%class.SimpleClass*)* %_temp.111 to void (%class.UseSimpleClass*)*
%_temp.110 = getelementptr %dv.UseSimpleClass, %dv.UseSimpleClass* @_J_dv_14UseSimpleClass, i32 0, i32 9
store void (%class.UseSimpleClass*)* %_temp.112, void (%class.UseSimpleClass*)** %_temp.110
%_temp.113 = getelementptr %dv.SimpleClass, %dv.SimpleClass* @_J_dv_11SimpleClass, i32 0, i32 10
%_temp.115 = load %class.java.lang.Object* (%class.SimpleClass*)*, %class.java.lang.Object* (%class.SimpleClass*)** %_temp.113
%_temp.116 = bitcast %class.java.lang.Object* (%class.SimpleClass*)* %_temp.115 to %class.java.lang.Object* (%class.UseSimpleClass*)*
%_temp.114 = getelementptr %dv.UseSimpleClass, %dv.UseSimpleClass* @_J_dv_14UseSimpleClass, i32 0, i32 10
store %class.java.lang.Object* (%class.UseSimpleClass*)* %_temp.116, %class.java.lang.Object* (%class.UseSimpleClass*)** %_temp.114
%_temp.117 = getelementptr %dv.SimpleClass, %dv.SimpleClass* @_J_dv_11SimpleClass, i32 0, i32 11
%_temp.119 = load void (%class.SimpleClass*)*, void (%class.SimpleClass*)** %_temp.117
%_temp.120 = bitcast void (%class.SimpleClass*)* %_temp.119 to void (%class.UseSimpleClass*)*
%_temp.118 = getelementptr %dv.UseSimpleClass, %dv.UseSimpleClass* @_J_dv_14UseSimpleClass, i32 0, i32 11
store void (%class.UseSimpleClass*)* %_temp.120, void (%class.UseSimpleClass*)** %_temp.118
%_temp.121 = getelementptr %dv.UseSimpleClass, %dv.UseSimpleClass* @_J_dv_14UseSimpleClass, i32 0, i32 12
store i32 (%class.UseSimpleClass*)* @_J_14UseSimpleClass_6method_void, i32 (%class.UseSimpleClass*)** %_temp.121
%_temp.122 = getelementptr %dv.SimpleClass, %dv.SimpleClass* @_J_dv_11SimpleClass, i32 0, i32 13
%_temp.124 = load i32 (%class.SimpleClass*)*, i32 (%class.SimpleClass*)** %_temp.122
%_temp.125 = bitcast i32 (%class.SimpleClass*)* %_temp.124 to i32 (%class.UseSimpleClass*)*
%_temp.123 = getelementptr %dv.UseSimpleClass, %dv.UseSimpleClass* @_J_dv_14UseSimpleClass, i32 0, i32 13
store i32 (%class.UseSimpleClass*)* %_temp.125, i32 (%class.UseSimpleClass*)** %_temp.123
%_temp.126 = getelementptr %dv.UseSimpleClass, %dv.UseSimpleClass* @_J_dv_14UseSimpleClass, i32 0, i32 14
store i32 (%class.UseSimpleClass*)* @_J_14UseSimpleClass_7method2_void, i32 (%class.UseSimpleClass*)** %_temp.126
ret void
}

