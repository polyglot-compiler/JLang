; ModuleID = 'interface.cpp'
source_filename = "interface.cpp"
target datalayout = "e-m:o-i64:64-f80:128-n8:16:32:64-S128"
target triple = "x86_64-apple-macosx10.11.0"

%class.java_obj = type { %class.dv* }
%class.dv = type { %class.it*, i8* }
%class.it = type { %class.it*, i8* }

; Function Attrs: ssp uwtable
define i8* @__getInterfaceMethod(i8* %obj, i8* %interface_string, i32 %methodIndex) #0 {
entry:
  %obj.addr = alloca i8*, align 8
  %interface_string.addr = alloca i8*, align 8
  %methodIndex.addr = alloca i32, align 4
  %o = alloca %class.java_obj*, align 8
  %itable = alloca %class.it*, align 8
  store i8* %obj, i8** %obj.addr, align 8
  store i8* %interface_string, i8** %interface_string.addr, align 8
  store i32 %methodIndex, i32* %methodIndex.addr, align 4
  %0 = load i8*, i8** %obj.addr, align 8
  %1 = bitcast i8* %0 to %class.java_obj*
  store %class.java_obj* %1, %class.java_obj** %o, align 8
  %2 = load %class.java_obj*, %class.java_obj** %o, align 8
  %dv = getelementptr inbounds %class.java_obj, %class.java_obj* %2, i32 0, i32 0
  %3 = load %class.dv*, %class.dv** %dv, align 8
  %it = getelementptr inbounds %class.dv, %class.dv* %3, i32 0, i32 0
  %4 = load %class.it*, %class.it** %it, align 8
  store %class.it* %4, %class.it** %itable, align 8
  br label %while.cond

while.cond:                                       ; preds = %if.end, %entry
  %5 = load %class.it*, %class.it** %itable, align 8
  %cmp = icmp ne %class.it* %5, null
  br i1 %cmp, label %while.body, label %while.end

while.body:                                       ; preds = %while.cond
  %6 = load %class.it*, %class.it** %itable, align 8
  %interface_name = getelementptr inbounds %class.it, %class.it* %6, i32 0, i32 1
  %7 = load i8*, i8** %interface_name, align 8
  %8 = load i8*, i8** %interface_string.addr, align 8
  %call = call i32 @strcmp(i8* %7, i8* %8)
  %cmp1 = icmp eq i32 %call, 0
  br i1 %cmp1, label %if.then, label %if.else

if.then:                                          ; preds = %while.body
  %9 = load i32, i32* %methodIndex.addr, align 4
  %idxprom = sext i32 %9 to i64
  %10 = load %class.it*, %class.it** %itable, align 8
  %11 = bitcast %class.it* %10 to i8**
  %arrayidx = getelementptr inbounds i8*, i8** %11, i64 %idxprom
  %12 = load i8*, i8** %arrayidx, align 8
  ret i8* %12

if.else:                                          ; preds = %while.body
  %13 = load %class.it*, %class.it** %itable, align 8
  %next = getelementptr inbounds %class.it, %class.it* %13, i32 0, i32 0
  %14 = load %class.it*, %class.it** %next, align 8
  store %class.it* %14, %class.it** %itable, align 8
  br label %if.end

if.end:                                           ; preds = %if.else
  br label %while.cond

while.end:                                        ; preds = %while.cond
  call void @abort() #3
  unreachable
}

declare i32 @strcmp(i8*, i8*) #1

; Function Attrs: noreturn
declare void @abort() #2

attributes #0 = { ssp uwtable "disable-tail-calls"="false" "less-precise-fpmad"="false" "no-frame-pointer-elim"="true" "no-frame-pointer-elim-non-leaf" "no-infs-fp-math"="false" "no-jump-tables"="false" "no-nans-fp-math"="false" "stack-protector-buffer-size"="8" "target-cpu"="core2" "target-features"="+cx16,+fxsr,+mmx,+sse,+sse2,+sse3,+ssse3,+x87" "unsafe-fp-math"="false" "use-soft-float"="false" }
attributes #1 = { "disable-tail-calls"="false" "less-precise-fpmad"="false" "no-frame-pointer-elim"="true" "no-frame-pointer-elim-non-leaf" "no-infs-fp-math"="false" "no-nans-fp-math"="false" "stack-protector-buffer-size"="8" "target-cpu"="core2" "target-features"="+cx16,+fxsr,+mmx,+sse,+sse2,+sse3,+ssse3,+x87" "unsafe-fp-math"="false" "use-soft-float"="false" }
attributes #2 = { noreturn "disable-tail-calls"="false" "less-precise-fpmad"="false" "no-frame-pointer-elim"="true" "no-frame-pointer-elim-non-leaf" "no-infs-fp-math"="false" "no-nans-fp-math"="false" "stack-protector-buffer-size"="8" "target-cpu"="core2" "target-features"="+cx16,+fxsr,+mmx,+sse,+sse2,+sse3,+ssse3,+x87" "unsafe-fp-math"="false" "use-soft-float"="false" }
attributes #3 = { noreturn }

!llvm.module.flags = !{!0}
!llvm.ident = !{!1}

!0 = !{i32 1, !"PIC Level", i32 2}
!1 = !{!"clang version 3.9.0 (trunk 272188)"}
