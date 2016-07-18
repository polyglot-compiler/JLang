define void @main( %args) {
%arg_args = alloca ,  1
store  %args, * %arg_args
call void @main()
ret void
}

define void @main() {
br label %head.0
head.0:
br label %label.0
label.0:
br label %head.0
end.0:
ret void
}

