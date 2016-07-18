%class.ExtendConditions = type opaque
%class.Conditions = type opaque
@_J_size_16ExtendConditions = global i64 0
@_J_size_10Conditions = external global i64
declare i8* @malloc(i64 %size)

define void @_ExtendConditions_h(%class.ExtendConditions* %_this) {
call void @_tests.Conditions_h()

ret void
}

define void @_ExtendConditions_i(%class.ExtendConditions* %_this) {
ret void
}

