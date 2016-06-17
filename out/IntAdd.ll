define void @main() {
%x = add i32 1, 2
%flat$$$0 = call i32 @f(i32 4, i32 %x)
%flat$$$1 = add i32 %x, %flat$$$0
call void @print(i32 %flat$$$1)
ret void
}

define i32 @_IntAdd_f(i32 %w, i32 %y) {
%x = add i32 %w, %y
ret i32 %x
}



