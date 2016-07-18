%class.ExtendEmptyClass = type opaque
%class.EmptyClass = type opaque
@_J_size_16ExtendEmptyClass = global i64 0
@_J_size_10EmptyClass = external global i64
declare i8* @malloc(i64 %size)

define void @main( %args) {
%arg_args = alloca , i32 1
store  %args, * %arg_args
ret void
}

