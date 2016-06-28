define i32 @main() {
Test:
;  %cond = icmp eq i32 1, 2
  br i1 0, label %IfEqual.0, label %IfUnequal
IfEqual.0:
  ret i32 1
IfUnequal:
  ret i32 23
}
