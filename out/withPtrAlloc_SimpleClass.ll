%class.SimpleClass = type {%dv.SimpleClass*, i32}
%dv.SimpleClass = type {i8*, i32 (%class.SimpleClass*)*, i32 (%class.SimpleClass*)*}

@_J_size_11SimpleClass = global i64 0
@_J_dv_11SimpleClass = global %dv.SimpleClass zeroinitializer

declare void @_SimpleClass_print(i32 %i)
declare i8* @malloc(i64)

define void @main() {
  %s = alloca %class.SimpleClass*, i32 1
  store %class.SimpleClass* null, %class.SimpleClass** %s
  %_temp.1 = call i8* @malloc(i64 8)
  %_temp.2 = bitcast i8* %_temp.1 to %class.SimpleClass*
  store %class.SimpleClass* %_temp.2, %class.SimpleClass** %s
  ret void
}

define i32 @_SimpleClass_method(%class.SimpleClass* %_this) {
  ret i32 3
}

define i32 @_SimpleClass_privateMethod(%class.SimpleClass* %_this) {
  ret i32 23
}

define void @_J_init_11SimpleClass() {
  %_temp.3 = getelementptr %dv.SimpleClass, %dv.SimpleClass* @_J_dv_11SimpleClass, i32 0, i32 1
  store i32 (%class.SimpleClass*)* @_SimpleClass_method, i32 (%class.SimpleClass*)** %_temp.3
  %_temp.4 = getelementptr %dv.SimpleClass, %dv.SimpleClass* @_J_dv_11SimpleClass, i32 0, i32 2
  store i32 (%class.SimpleClass*)* @_SimpleClass_privateMethod, i32 (%class.SimpleClass*)** %_temp.4
  ret void
}

