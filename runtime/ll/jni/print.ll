; ModuleID = 'print.cpp'
target datalayout = "e-m:o-i64:64-f80:128-n8:16:32:64-S128"
target triple = "x86_64-apple-macosx10.11.0"

%dv.java_lang_String = type opaque
%dv.support_Array = type opaque
%class.java_lang_String = type { %dv.java_lang_String*, %class.support_Array* }
%class.support_Array = type { %class.support_Array*, i32, i8* }

@.str = private unnamed_addr constant [3 x i8] c"%s\00", align 1
@.str.1 = private unnamed_addr constant [4 x i8] c"%s\0A\00", align 1
@.str.2 = private unnamed_addr constant [3 x i8] c"%d\00", align 1
@.str.3 = private unnamed_addr constant [4 x i8] c"%d\0A\00", align 1
@.str.4 = private unnamed_addr constant [5 x i8] c"%lld\00", align 1
@.str.5 = private unnamed_addr constant [6 x i8] c"%lld\0A\00", align 1
@.str.6 = private unnamed_addr constant [3 x i8] c"%f\00", align 1
@.str.7 = private unnamed_addr constant [4 x i8] c"%f\0A\00", align 1

; Function Attrs: ssp uwtable
define void @Java_placeholder_Print_print__Ljava_lang_String_2(%class.java_lang_String* %s) #0 {
  %1 = alloca %class.java_lang_String*, align 8
  store %class.java_lang_String* %s, %class.java_lang_String** %1, align 8
  %2 = load %class.java_lang_String*, %class.java_lang_String** %1, align 8
  call void @_Z15printWithFormatP16java_lang_StringPKc(%class.java_lang_String* %2, i8* getelementptr inbounds ([3 x i8], [3 x i8]* @.str, i32 0, i32 0))
  ret void
}

; Function Attrs: ssp uwtable
define internal void @_Z15printWithFormatP16java_lang_StringPKc(%class.java_lang_String* %s, i8* %format) #0 {
  %1 = alloca %class.java_lang_String*, align 8
  %2 = alloca i8*, align 8
  %data = alloca i64*, align 8
  %len = alloca i32, align 4
  %3 = alloca i8*, align 8
  %i = alloca i32, align 4
  store %class.java_lang_String* %s, %class.java_lang_String** %1, align 8
  store i8* %format, i8** %2, align 8
  %4 = load %class.java_lang_String*, %class.java_lang_String** %1, align 8
  %5 = getelementptr inbounds %class.java_lang_String, %class.java_lang_String* %4, i32 0, i32 1
  %6 = load %class.support_Array*, %class.support_Array** %5, align 8
  %7 = getelementptr inbounds %class.support_Array, %class.support_Array* %6, i32 0, i32 2
  %8 = bitcast i8** %7 to i64*
  store i64* %8, i64** %data, align 8
  %9 = load %class.java_lang_String*, %class.java_lang_String** %1, align 8
  %10 = getelementptr inbounds %class.java_lang_String, %class.java_lang_String* %9, i32 0, i32 1
  %11 = load %class.support_Array*, %class.support_Array** %10, align 8
  %12 = getelementptr inbounds %class.support_Array, %class.support_Array* %11, i32 0, i32 1
  %13 = load i32, i32* %12, align 8
  store i32 %13, i32* %len, align 4
  %14 = load i32, i32* %len, align 4
  %15 = zext i32 %14 to i64
  %16 = call i8* @llvm.stacksave()
  store i8* %16, i8** %3, align 8
  %17 = alloca i8, i64 %15, align 16
  store i32 0, i32* %i, align 4
  br label %18

; <label>:18                                      ; preds = %32, %0
  %19 = load i32, i32* %i, align 4
  %20 = load i32, i32* %len, align 4
  %21 = icmp slt i32 %19, %20
  br i1 %21, label %22, label %35

; <label>:22                                      ; preds = %18
  %23 = load i32, i32* %i, align 4
  %24 = sext i32 %23 to i64
  %25 = load i64*, i64** %data, align 8
  %26 = getelementptr inbounds i64, i64* %25, i64 %24
  %27 = load i64, i64* %26, align 8
  %28 = trunc i64 %27 to i8
  %29 = load i32, i32* %i, align 4
  %30 = sext i32 %29 to i64
  %31 = getelementptr inbounds i8, i8* %17, i64 %30
  store i8 %28, i8* %31, align 1
  br label %32

; <label>:32                                      ; preds = %22
  %33 = load i32, i32* %i, align 4
  %34 = add nsw i32 %33, 1
  store i32 %34, i32* %i, align 4
  br label %18

; <label>:35                                      ; preds = %18
  %36 = load i8*, i8** %2, align 8
  %37 = call i32 (i8*, ...) @printf(i8* %36, i8* %17)
  %38 = load i8*, i8** %3, align 8
  call void @llvm.stackrestore(i8* %38)
  ret void
}

; Function Attrs: ssp uwtable
define void @Java_placeholder_Print_println__Ljava_lang_String_2(%class.java_lang_String* %s) #0 {
  %1 = alloca %class.java_lang_String*, align 8
  store %class.java_lang_String* %s, %class.java_lang_String** %1, align 8
  %2 = load %class.java_lang_String*, %class.java_lang_String** %1, align 8
  call void @_Z15printWithFormatP16java_lang_StringPKc(%class.java_lang_String* %2, i8* getelementptr inbounds ([4 x i8], [4 x i8]* @.str.1, i32 0, i32 0))
  ret void
}

; Function Attrs: ssp uwtable
define void @Java_placeholder_Print_print__I(i32 %n) #0 {
  %1 = alloca i32, align 4
  store i32 %n, i32* %1, align 4
  %2 = load i32, i32* %1, align 4
  %3 = call i32 (i8*, ...) @printf(i8* getelementptr inbounds ([3 x i8], [3 x i8]* @.str.2, i32 0, i32 0), i32 %2)
  ret void
}

declare i32 @printf(i8*, ...) #1

; Function Attrs: ssp uwtable
define void @Java_placeholder_Print_println__I(i32 %n) #0 {
  %1 = alloca i32, align 4
  store i32 %n, i32* %1, align 4
  %2 = load i32, i32* %1, align 4
  %3 = call i32 (i8*, ...) @printf(i8* getelementptr inbounds ([4 x i8], [4 x i8]* @.str.3, i32 0, i32 0), i32 %2)
  ret void
}

; Function Attrs: ssp uwtable
define void @Java_placeholder_Print_print__J(i64 %n) #0 {
  %1 = alloca i64, align 8
  store i64 %n, i64* %1, align 8
  %2 = load i64, i64* %1, align 8
  %3 = call i32 (i8*, ...) @printf(i8* getelementptr inbounds ([5 x i8], [5 x i8]* @.str.4, i32 0, i32 0), i64 %2)
  ret void
}

; Function Attrs: ssp uwtable
define void @Java_placeholder_Print_println__J(i64 %n) #0 {
  %1 = alloca i64, align 8
  store i64 %n, i64* %1, align 8
  %2 = load i64, i64* %1, align 8
  %3 = call i32 (i8*, ...) @printf(i8* getelementptr inbounds ([6 x i8], [6 x i8]* @.str.5, i32 0, i32 0), i64 %2)
  ret void
}

; Function Attrs: ssp uwtable
define void @Java_placeholder_Print_print__F(float %n) #0 {
  %1 = alloca float, align 4
  store float %n, float* %1, align 4
  %2 = load float, float* %1, align 4
  %3 = fpext float %2 to double
  %4 = call i32 (i8*, ...) @printf(i8* getelementptr inbounds ([3 x i8], [3 x i8]* @.str.6, i32 0, i32 0), double %3)
  ret void
}

; Function Attrs: ssp uwtable
define void @Java_placeholder_Print_println__F(float %n) #0 {
  %1 = alloca float, align 4
  store float %n, float* %1, align 4
  %2 = load float, float* %1, align 4
  %3 = fpext float %2 to double
  %4 = call i32 (i8*, ...) @printf(i8* getelementptr inbounds ([4 x i8], [4 x i8]* @.str.7, i32 0, i32 0), double %3)
  ret void
}

; Function Attrs: ssp uwtable
define void @Java_placeholder_Print_print__D(double %n) #0 {
  %1 = alloca double, align 8
  store double %n, double* %1, align 8
  %2 = load double, double* %1, align 8
  %3 = call i32 (i8*, ...) @printf(i8* getelementptr inbounds ([3 x i8], [3 x i8]* @.str.6, i32 0, i32 0), double %2)
  ret void
}

; Function Attrs: ssp uwtable
define void @Java_placeholder_Print_println__D(double %n) #0 {
  %1 = alloca double, align 8
  store double %n, double* %1, align 8
  %2 = load double, double* %1, align 8
  %3 = call i32 (i8*, ...) @printf(i8* getelementptr inbounds ([4 x i8], [4 x i8]* @.str.7, i32 0, i32 0), double %2)
  ret void
}

; Function Attrs: nounwind
declare i8* @llvm.stacksave() #2

; Function Attrs: nounwind
declare void @llvm.stackrestore(i8*) #2

attributes #0 = { ssp uwtable "disable-tail-calls"="false" "less-precise-fpmad"="false" "no-frame-pointer-elim"="true" "no-frame-pointer-elim-non-leaf" "no-infs-fp-math"="false" "no-nans-fp-math"="false" "stack-protector-buffer-size"="8" "target-cpu"="core2" "target-features"="+cx16,+fxsr,+mmx,+sse,+sse2,+sse3,+ssse3" "unsafe-fp-math"="false" "use-soft-float"="false" }
attributes #1 = { "disable-tail-calls"="false" "less-precise-fpmad"="false" "no-frame-pointer-elim"="true" "no-frame-pointer-elim-non-leaf" "no-infs-fp-math"="false" "no-nans-fp-math"="false" "stack-protector-buffer-size"="8" "target-cpu"="core2" "target-features"="+cx16,+fxsr,+mmx,+sse,+sse2,+sse3,+ssse3" "unsafe-fp-math"="false" "use-soft-float"="false" }
attributes #2 = { nounwind }

!llvm.module.flags = !{!0}
!llvm.ident = !{!1}

!0 = !{i32 1, !"PIC Level", i32 2}
!1 = !{!"Apple LLVM version 7.3.0 (clang-703.0.31)"}
