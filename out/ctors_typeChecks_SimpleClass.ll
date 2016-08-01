%class.SimpleClass = type {%dv.SimpleClass*, i32}
%dv.SimpleClass = type {i8*, i8*, i8*}
@_J_size_11SimpleClass = global i64 0
@_J_dv_11SimpleClass = global %dv.SimpleClass zeroinitializer

declare void @_SimpleClass_print(i32 %i)
declare i8* @malloc(i64 %size)

define void @main() {
ret void
}

define i32 @_SimpleClass_method(%class.SimpleClass* %_this) {
ret i32 3
}

define i32 @_SimpleClass_privateMethod(%class.SimpleClass* %_this) {
ret i32 23
}

define void @_J_init_11SimpleClass() {
%_temp.0 = getelementptr %dv.SimpleClass, %dv.SimpleClass* @_J_dv_11SimpleClass, i32 0, i32 0
%cast1 = bitcast i32 (%class.SimpleClass*)* @_SimpleClass_method to i8*
store i8* %cast1, i8** %_temp.0

%_temp.1 = getelementptr %dv.SimpleClass, %dv.SimpleClass* @_J_dv_11SimpleClass, i32 0, i32 1
%cast2 = bitcast i32 (%class.SimpleClass*)* @_SimpleClass_privateMethod to i8*
store i8* %cast2, i8** %_temp.1
ret void
}

