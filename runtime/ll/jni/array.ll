; ModuleID = 'array.cpp'
target datalayout = "e-m:o-i64:64-f80:128-n8:16:32:64-S128"
target triple = "x86_64-apple-macosx10.11.0"

%dv.support_Array = type opaque
%class.support_Array = type { %dv.support_Array*, i32, i8* }

; Function Attrs: nounwind ssp uwtable
define void @Java_support_Array_Array__I(%class.support_Array* %_this, i32 %len) #0 {
  %1 = alloca %class.support_Array*, align 8
  %2 = alloca i32, align 4
  store %class.support_Array* %_this, %class.support_Array** %1, align 8
  store i32 %len, i32* %2, align 4
  %3 = load i32, i32* %2, align 4
  %4 = load %class.support_Array*, %class.support_Array** %1, align 8
  %5 = getelementptr inbounds %class.support_Array, %class.support_Array* %4, i32 0, i32 1
  store i32 %3, i32* %5, align 8
  %6 = load %class.support_Array*, %class.support_Array** %1, align 8
  %7 = getelementptr inbounds %class.support_Array, %class.support_Array* %6, i32 0, i32 2
  %8 = bitcast i8** %7 to i8*
  %9 = load i32, i32* %2, align 4
  %10 = sext i32 %9 to i64
  %11 = mul i64 %10, 8
  call void @llvm.memset.p0i8.i64(i8* %8, i8 0, i64 %11, i32 8, i1 false)
  ret void
}

; Function Attrs: argmemonly nounwind
declare void @llvm.memset.p0i8.i64(i8* nocapture, i8, i64, i32, i1) #1

attributes #0 = { nounwind ssp uwtable "disable-tail-calls"="false" "less-precise-fpmad"="false" "no-frame-pointer-elim"="true" "no-frame-pointer-elim-non-leaf" "no-infs-fp-math"="false" "no-nans-fp-math"="false" "stack-protector-buffer-size"="8" "target-cpu"="core2" "target-features"="+cx16,+fxsr,+mmx,+sse,+sse2,+sse3,+ssse3" "unsafe-fp-math"="false" "use-soft-float"="false" }
attributes #1 = { argmemonly nounwind }

!llvm.module.flags = !{!0}
!llvm.ident = !{!1}

!0 = !{i32 1, !"PIC Level", i32 2}
!1 = !{!"Apple LLVM version 7.3.0 (clang-703.0.31)"}
