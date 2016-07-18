%class.FloatingPoint = type opaque
define void @main() {
%x = alloca float, i32 1
%y = alloca double, i32 1
store float 0.0, float* %x
store float 32.0, float* %x
store double 0.0, double* %y
%_temp.2 = load float, float* %x
%_temp.3 = fpext float %_temp.2 to double
store double %_temp.3, double* %y
%_temp.5 = load double, double* %y
%_temp.10 = alloca double, i32 1
store double %_temp.5, double* %_temp.10
%_temp.6 = load double, double* %y
%_temp.11 = load double, double* %_temp.10
%_temp.7 = fadd double %_temp.11, %_temp.6
store double %_temp.7, double* %y
ret void
}

define double @_FloatingPoint_f(%class.FloatingPoint* %_this, float %f) {
%arg_f = alloca float, i32 1
store float %f, float* %arg_f
%_temp.8 = load float, float* %arg_f
%_temp.9 = fpext float %_temp.8 to double
ret double %_temp.9
}

