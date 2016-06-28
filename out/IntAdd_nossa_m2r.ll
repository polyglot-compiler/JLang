; ModuleID = 'IntAdd.ll'
source_filename = "IntAdd.ll"

declare void @_IntAdd_print(i32)

define void @main() {
  %x = add i32 1, 2
  %"flat$$$0" = call i32 @_IntAdd_f(i32 4, i32 %x)
  %"flat$$$1" = add i32 %x, %"flat$$$0"
  call void @_IntAdd_print(i32 %"flat$$$1")
  %"flat$$$2" = icmp eq i32 %x, 3
  br i1 %"flat$$$2", label %label.3, label %label.4

label.3:                                          ; preds = %0
  call void @_IntAdd_print(i32 1)
  br label %label.5

label.4:                                          ; preds = %0
  br i1 false, label %label.0, label %label.1

label.0:                                          ; preds = %label.4
  call void @_IntAdd_print(i32 0)
  br label %label.2

label.1:                                          ; preds = %label.4
  call void @_IntAdd_print(i32 123)
  br label %label.2

label.2:                                          ; preds = %label.1, %label.0
  br label %label.5

label.5:                                          ; preds = %label.2, %label.3
  %"flat$$$3" = call i32 @_IntAdd_fib(i32 30)
  call void @_IntAdd_print(i32 %"flat$$$3")
  ret void
}

define i32 @_IntAdd_f(i32 %w, i32 %y) {
  %x = add i32 %w, %y
  ret i32 %x
}

define i32 @_IntAdd_fib(i32 %x) {
  %"flat$$$4" = icmp sle i32 %x, 1
  br i1 %"flat$$$4", label %label.6, label %label.7

label.6:                                          ; preds = %0
  ret i32 %x
                                                  ; No predecessors!
  br label %label.8

label.7:                                          ; preds = %0
  br label %label.8

label.8:                                          ; preds = %label.7, %1
  %"flat$$$5" = sub i32 %x, 1
  %"flat$$$6" = call i32 @_IntAdd_fib(i32 %"flat$$$5")
  %"flat$$$7" = sub i32 %x, 2
  %"flat$$$8" = call i32 @_IntAdd_fib(i32 %"flat$$$7")
  %"flat$$$9" = add i32 %"flat$$$6", %"flat$$$8"
  ret i32 %"flat$$$9"
}

define void @_IntAdd_nonssa(i32 %y) {
  %temp.1 = add i32 %y, %y
  %temp.4 = add i32 %temp.1, %y
  %temp.6 = add i32 %temp.4, 3
  %temp.9 = add i32 %temp.4, %temp.6
  call void @_IntAdd_print(i32 %temp.9)
  ret void
}
