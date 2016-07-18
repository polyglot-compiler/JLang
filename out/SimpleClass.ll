%class.SimpleClass = type opaque
%dv.SimpleClass = type opaque
@_J_size_11SimpleClass = global i64 0
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

