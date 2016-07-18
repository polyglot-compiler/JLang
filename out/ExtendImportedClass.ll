%class.ExtendImportedClass = type opaque
%class.IntLit_c = type opaque
@_J_size_19ExtendImportedClass = global i64 0
@_J_size_8IntLit_c = external global i64
declare i8* @malloc(i64 %size)

define i64 @_ExtendImportedClass_something(%class.ExtendImportedClass* %_this) {
ret i64 12312312312
}

define i64 @_ExtendImportedClass_value(%class.ExtendImportedClass* %_this) {
%flat$0 = alloca i64, i32 1
%_temp.4 = call i64 @_polyglot.ast.IntLit_c_value()

store i64 %_temp.4, i64* %flat$0

%_temp.5 = load i64, i64* %flat$0
ret i64 %_temp.5

}

