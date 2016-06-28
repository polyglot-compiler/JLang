; ModuleID = 'IntAdd.ll'
source_filename = "IntAdd.ll"

declare void @_IntAdd_print(i32)

define void @main() {
label.3:
  %"flat$$$0" = tail call i32 @_IntAdd_f(i32 4, i32 3)
  %"flat$$$1" = add i32 %"flat$$$0", 3
  tail call void @_IntAdd_print(i32 %"flat$$$1")
  tail call void @_IntAdd_print(i32 1)
  %"flat$$$3" = tail call i32 @_IntAdd_fib(i32 30)
  tail call void @_IntAdd_print(i32 %"flat$$$3")
  ret void
}

; Function Attrs: norecurse nounwind readnone
define i32 @_IntAdd_f(i32 %w, i32 %y) #0 {
  %x = add i32 %y, %w
  ret i32 %x
}

; Function Attrs: nounwind readnone
define i32 @_IntAdd_fib(i32 %x) #1 {
  %"flat$$$4" = icmp slt i32 %x, 2
  br i1 %"flat$$$4", label %label.6, label %label.8

label.6:                                          ; preds = %0
  ret i32 %x

label.8:                                          ; preds = %0
  %"flat$$$5" = add i32 %x, -1
  %"flat$$$6" = tail call i32 @_IntAdd_fib(i32 %"flat$$$5")
  %"flat$$$7" = add i32 %x, -2
  %"flat$$$8" = tail call i32 @_IntAdd_fib(i32 %"flat$$$7")
  %"flat$$$9" = add i32 %"flat$$$8", %"flat$$$6"
  ret i32 %"flat$$$9"
}

define void @_IntAdd_nonssa(i32 %y) {
  %factor = mul i32 %y, 6
  %temp.9 = add i32 %factor, 3
  tail call void @_IntAdd_print(i32 %temp.9)
  ret void
}

attributes #0 = { norecurse nounwind readnone }
attributes #1 = { nounwind readnone }
