; ModuleID = 'llvm-link'
source_filename = "llvm-link"

@.str = private unnamed_addr constant [4 x i8] c"%d\0A\00", align 1

define void @main() {
  call void @_IntComparisons_noelse()
  ret void
}

define void @_IntComparisons_noelse() {
  %"flat$$$0" = icmp slt i32 1, 2
  br i1 %"flat$$$0", label %label.0, label %label.1

label.0:                                          ; preds = %0
  call void @_IntComparisons_print(i32 1)
  br label %label.2

label.1:                                          ; preds = %0
  br label %label.2

label.2:                                          ; preds = %label.1, %label.0
  %"flat$$$1" = icmp slt i32 1, 1
  br i1 %"flat$$$1", label %label.3, label %label.4

label.3:                                          ; preds = %label.2
  call void @_IntComparisons_print(i32 0)
  br label %label.5

label.4:                                          ; preds = %label.2
  br label %label.5

label.5:                                          ; preds = %label.4, %label.3
  %"flat$$$2" = icmp slt i32 1, 0
  br i1 %"flat$$$2", label %label.6, label %label.7

label.6:                                          ; preds = %label.5
  call void @_IntComparisons_print(i32 0)
  br label %label.8

label.7:                                          ; preds = %label.5
  br label %label.8

label.8:                                          ; preds = %label.7, %label.6
  %"flat$$$3" = icmp sgt i32 4, 3
  br i1 %"flat$$$3", label %label.9, label %label.10

label.9:                                          ; preds = %label.8
  call void @_IntComparisons_print(i32 2)
  br label %label.11

label.10:                                         ; preds = %label.8
  br label %label.11

label.11:                                         ; preds = %label.10, %label.9
  %"flat$$$4" = icmp sgt i32 3, 3
  br i1 %"flat$$$4", label %label.12, label %label.13

label.12:                                         ; preds = %label.11
  call void @_IntComparisons_print(i32 0)
  br label %label.14

label.13:                                         ; preds = %label.11
  br label %label.14

label.14:                                         ; preds = %label.13, %label.12
  %"flat$$$5" = icmp sgt i32 3, 4
  br i1 %"flat$$$5", label %label.15, label %label.16

label.15:                                         ; preds = %label.14
  call void @_IntComparisons_print(i32 0)
  br label %label.17

label.16:                                         ; preds = %label.14
  br label %label.17

label.17:                                         ; preds = %label.16, %label.15
  %"flat$$$6" = icmp eq i32 2, 2
  br i1 %"flat$$$6", label %label.18, label %label.19

label.18:                                         ; preds = %label.17
  call void @_IntComparisons_print(i32 3)
  br label %label.20

label.19:                                         ; preds = %label.17
  br label %label.20

label.20:                                         ; preds = %label.19, %label.18
  %"flat$$$7" = icmp eq i32 2, 1
  br i1 %"flat$$$7", label %label.21, label %label.22

label.21:                                         ; preds = %label.20
  call void @_IntComparisons_print(i32 0)
  br label %label.23

label.22:                                         ; preds = %label.20
  br label %label.23

label.23:                                         ; preds = %label.22, %label.21
  %"flat$$$8" = icmp sle i32 1, 2
  br i1 %"flat$$$8", label %label.24, label %label.25

label.24:                                         ; preds = %label.23
  call void @_IntComparisons_print(i32 4)
  br label %label.26

label.25:                                         ; preds = %label.23
  br label %label.26

label.26:                                         ; preds = %label.25, %label.24
  %"flat$$$9" = icmp sle i32 1, 1
  br i1 %"flat$$$9", label %label.27, label %label.28

label.27:                                         ; preds = %label.26
  call void @_IntComparisons_print(i32 5)
  br label %label.29

label.28:                                         ; preds = %label.26
  br label %label.29

label.29:                                         ; preds = %label.28, %label.27
  %"flat$$$10" = icmp sle i32 1, 0
  br i1 %"flat$$$10", label %label.30, label %label.31

label.30:                                         ; preds = %label.29
  call void @_IntComparisons_print(i32 0)
  br label %label.32

label.31:                                         ; preds = %label.29
  br label %label.32

label.32:                                         ; preds = %label.31, %label.30
  %"flat$$$11" = icmp sge i32 3, 4
  br i1 %"flat$$$11", label %label.33, label %label.34

label.33:                                         ; preds = %label.32
  call void @_IntComparisons_print(i32 0)
  br label %label.35

label.34:                                         ; preds = %label.32
  br label %label.35

label.35:                                         ; preds = %label.34, %label.33
  %"flat$$$12" = icmp sge i32 3, 3
  br i1 %"flat$$$12", label %label.36, label %label.37

label.36:                                         ; preds = %label.35
  call void @_IntComparisons_print(i32 6)
  br label %label.38

label.37:                                         ; preds = %label.35
  br label %label.38

label.38:                                         ; preds = %label.37, %label.36
  %"flat$$$13" = icmp sge i32 2, 3
  br i1 %"flat$$$13", label %label.39, label %label.40

label.39:                                         ; preds = %label.38
  call void @_IntComparisons_print(i32 0)
  br label %label.41

label.40:                                         ; preds = %label.38
  br label %label.41

label.41:                                         ; preds = %label.40, %label.39
  %"flat$$$14" = icmp ne i32 2, 1
  br i1 %"flat$$$14", label %label.42, label %label.43

label.42:                                         ; preds = %label.41
  call void @_IntComparisons_print(i32 7)
  br label %label.44

label.43:                                         ; preds = %label.41
  br label %label.44

label.44:                                         ; preds = %label.43, %label.42
  %"flat$$$15" = icmp ne i32 2, 2
  br i1 %"flat$$$15", label %label.45, label %label.46

label.45:                                         ; preds = %label.44
  call void @_IntComparisons_print(i32 0)
  br label %label.47

label.46:                                         ; preds = %label.44
  br label %label.47

label.47:                                         ; preds = %label.46, %label.45
  ret void
}

define void @_IntAdd_print(i32 %x) {
  %call = call i32 (i8*, ...) @printf(i8* getelementptr inbounds ([4 x i8], [4 x i8]* @.str, i32 0, i32 0), i32 %x)
  ret void
}

declare i32 @printf(i8*, ...)

define void @_IntComparisons_print(i32 %x) {
  %call = call i32 (i8*, ...) @printf(i8* getelementptr inbounds ([4 x i8], [4 x i8]* @.str, i32 0, i32 0), i32 %x)
  ret void
}
