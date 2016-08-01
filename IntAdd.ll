%class.IntAdd = type {%dv.IntAdd*}
%dv.IntAdd = type {i8*}
@_J_size_6IntAdd = global i64 0
@_J_dv_6IntAdd = global %dv.IntAdd zeroinitializer
declare void @_IntAdd_print(i32 %i)

declare i8* @malloc(i64 %size)

define void @_IntAdd_main() {
%x = alloca i32, i32 1
%flat$1 = alloca i32, i32 1
%flat$2 = alloca i1, i32 1
%flat$0 = alloca i32, i32 1
store i32 0, i32* %x
%_temp.1 = add i32 1, 2
store i32 %_temp.1, i32* %x

%_temp.2 = load i32, i32* %x
%_temp.20 = alloca i32, i32 1
store i32 %_temp.2, i32* %_temp.20
%_temp.21 = load i32, i32* %_temp.20
%_temp.3 = call i32 @_IntAdd_f(i32 4, i32 %_temp.21)

store i32 %_temp.3, i32* %flat$0

%_temp.4 = load i32, i32* %x
%_temp.22 = alloca i32, i32 1
store i32 %_temp.4, i32* %_temp.22
%_temp.5 = load i32, i32* %flat$0
%_temp.23 = load i32, i32* %_temp.22
%_temp.6 = add i32 %_temp.23, %_temp.5

store i32 %_temp.6, i32* %flat$1

%_temp.7 = load i32, i32* %flat$1
%_temp.24 = alloca i32, i32 1
store i32 %_temp.7, i32* %_temp.24
%_temp.25 = load i32, i32* %_temp.24
call void @_IntAdd_print(i32 %_temp.25)

%_temp.9 = load i32, i32* %x
%_temp.10 = icmp eq i32 %_temp.9, 3

store i1 %_temp.10, i1* %flat$2

%_temp.11 = load i1, i1* %flat$2
br i1 %_temp.11, label %label.3, label %label.4

label.3:
call void @_IntAdd_print(i32 1)


br label %label.5
label.4:
br label %label.1
label.0:
call void @_IntAdd_print(i32 0)


br label %label.2
label.1:
call void @_IntAdd_print(i32 123)


br label %label.2
label.2:


br label %label.5
label.5:

ret void
}

define i32 @_IntAdd_f(i32 %w, i32 %y) {
%x = alloca i32, i32 1
%arg_w = alloca i32, i32 1
store i32 %w, i32* %arg_w
%arg_y = alloca i32, i32 1
store i32 %y, i32* %arg_y
store i32 0, i32* %x
%_temp.16 = load i32, i32* %arg_w
%_temp.26 = alloca i32, i32 1
store i32 %_temp.16, i32* %_temp.26
%_temp.17 = load i32, i32* %arg_y
%_temp.27 = load i32, i32* %_temp.26
%_temp.18 = add i32 %_temp.27, %_temp.17

store i32 %_temp.18, i32* %x

%_temp.19 = load i32, i32* %x
ret i32 %_temp.19

}

define void @_J_init_6IntAdd() {
ret void
}

