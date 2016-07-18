%class.UseSimpleClass = type opaque
%dv.UseSimpleClass = type opaque
%class.SimpleClass = type opaque
%dv.SimpleClass = type opaque
@_J_size_14UseSimpleClass = global i64 0
@_J_size_11SimpleClass = external global i64
declare i8* @malloc(i64 %size)

define i32 @_UseSimpleClass_method(%class.UseSimpleClass* %_this) {
%flat$1 = alloca i32, i32 1
%flat$0 = alloca i32, i32 1
%_temp.0 = call i32 @_SimpleClass_method()

store i32 %_temp.0, i32* %flat$0

%_temp.1 = load i32, i32* %flat$0
%_temp.2 = add i32 %_temp.1, 1

store i32 %_temp.2, i32* %flat$1

%_temp.3 = load i32, i32* %flat$1
ret i32 %_temp.3

}

define i32 @_UseSimpleClass_method2(%class.UseSimpleClass* %_this) {
%flat$3 = alloca i32, i32 1
%flat$2 = alloca i32, i32 1
%_temp.4 = call i32 @_SimpleClass_method()

store i32 %_temp.4, i32* %flat$2

%_temp.5 = load i32, i32* %flat$2
%_temp.6 = add i32 %_temp.5, 1

store i32 %_temp.6, i32* %flat$3

%_temp.7 = load i32, i32* %flat$3
ret i32 %_temp.7

}

