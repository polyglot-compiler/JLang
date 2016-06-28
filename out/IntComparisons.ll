declare void @_IntComparisons_print(i32 %i)

define void @main() {
call void @_IntComparisons_noelse()
ret void
}

define void @_IntComparisons_noelse() {
%flat$$$0 = icmp slt i32 1, 2
br i1 %flat$$$0, label %label.0, label %label.1
label.0:
call void @_IntComparisons_print(i32 1)
br label %label.2
label.1:
br label %label.2
label.2:
%flat$$$1 = icmp slt i32 1, 1
br i1 %flat$$$1, label %label.3, label %label.4
label.3:
call void @_IntComparisons_print(i32 0)
br label %label.5
label.4:
br label %label.5
label.5:
%flat$$$2 = icmp slt i32 1, 0
br i1 %flat$$$2, label %label.6, label %label.7
label.6:
call void @_IntComparisons_print(i32 0)
br label %label.8
label.7:
br label %label.8
label.8:
%flat$$$3 = icmp sgt i32 4, 3
br i1 %flat$$$3, label %label.9, label %label.10
label.9:
call void @_IntComparisons_print(i32 2)
br label %label.11
label.10:
br label %label.11
label.11:
%flat$$$4 = icmp sgt i32 3, 3
br i1 %flat$$$4, label %label.12, label %label.13
label.12:
call void @_IntComparisons_print(i32 0)
br label %label.14
label.13:
br label %label.14
label.14:
%flat$$$5 = icmp sgt i32 3, 4
br i1 %flat$$$5, label %label.15, label %label.16
label.15:
call void @_IntComparisons_print(i32 0)
br label %label.17
label.16:
br label %label.17
label.17:
%flat$$$6 = icmp eq i32 2, 2
br i1 %flat$$$6, label %label.18, label %label.19
label.18:
call void @_IntComparisons_print(i32 3)
br label %label.20
label.19:
br label %label.20
label.20:
%flat$$$7 = icmp eq i32 2, 1
br i1 %flat$$$7, label %label.21, label %label.22
label.21:
call void @_IntComparisons_print(i32 0)
br label %label.23
label.22:
br label %label.23
label.23:
%flat$$$8 = icmp sle i32 1, 2
br i1 %flat$$$8, label %label.24, label %label.25
label.24:
call void @_IntComparisons_print(i32 4)
br label %label.26
label.25:
br label %label.26
label.26:
%flat$$$9 = icmp sle i32 1, 1
br i1 %flat$$$9, label %label.27, label %label.28
label.27:
call void @_IntComparisons_print(i32 5)
br label %label.29
label.28:
br label %label.29
label.29:
%flat$$$10 = icmp sle i32 1, 0
br i1 %flat$$$10, label %label.30, label %label.31
label.30:
call void @_IntComparisons_print(i32 0)
br label %label.32
label.31:
br label %label.32
label.32:
%flat$$$11 = icmp sge i32 3, 4
br i1 %flat$$$11, label %label.33, label %label.34
label.33:
call void @_IntComparisons_print(i32 0)
br label %label.35
label.34:
br label %label.35
label.35:
%flat$$$12 = icmp sge i32 3, 3
br i1 %flat$$$12, label %label.36, label %label.37
label.36:
call void @_IntComparisons_print(i32 6)
br label %label.38
label.37:
br label %label.38
label.38:
%flat$$$13 = icmp sge i32 2, 3
br i1 %flat$$$13, label %label.39, label %label.40
label.39:
call void @_IntComparisons_print(i32 0)
br label %label.41
label.40:
br label %label.41
label.41:
%flat$$$14 = icmp ne i32 2, 1
br i1 %flat$$$14, label %label.42, label %label.43
label.42:
call void @_IntComparisons_print(i32 7)
br label %label.44
label.43:
br label %label.44
label.44:
%flat$$$15 = icmp ne i32 2, 2
br i1 %flat$$$15, label %label.45, label %label.46
label.45:
call void @_IntComparisons_print(i32 0)
br label %label.47
label.46:
br label %label.47
label.47:
ret void
}

