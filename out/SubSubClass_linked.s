	.section	__TEXT,__text,regular,pure_instructions
	.macosx_version_min 10, 11
	.globl	__J_init_16java.lang.Object
	.p2align	4, 0x90
__J_init_16java.lang.Object:            ## @_J_init_16java.lang.Object
	.cfi_startproc
## BB#0:
	leaq	__J_16java.lang.Object_7equals_16java.lang.Object(%rip), %rax
	movq	%rax, __J_dv_16java.lang.Object+8(%rip)
	leaq	__J_16java.lang.Object_9getClass_void(%rip), %rax
	movq	%rax, __J_dv_16java.lang.Object+16(%rip)
	leaq	__J_16java.lang.Object_8hashCode_void(%rip), %rax
	movq	%rax, __J_dv_16java.lang.Object+24(%rip)
	leaq	__J_16java.lang.Object_10notifyAll_void(%rip), %rax
	movq	%rax, __J_dv_16java.lang.Object+32(%rip)
	leaq	__J_16java.lang.Object_7notify_void(%rip), %rax
	movq	%rax, __J_dv_16java.lang.Object+40(%rip)
	leaq	__J_16java.lang.Object_9toString_void(%rip), %rax
	movq	%rax, __J_dv_16java.lang.Object+48(%rip)
	leaq	__J_16java.lang.Object_5wait_i64(%rip), %rax
	movq	%rax, __J_dv_16java.lang.Object+56(%rip)
	leaq	__J_16java.lang.Object_5wait_i64_i32(%rip), %rax
	movq	%rax, __J_dv_16java.lang.Object+64(%rip)
	leaq	__J_16java.lang.Object_5wait_void(%rip), %rax
	movq	%rax, __J_dv_16java.lang.Object+72(%rip)
	leaq	__J_16java.lang.Object_6clone_void(%rip), %rax
	movq	%rax, __J_dv_16java.lang.Object+80(%rip)
	leaq	__J_16java.lang.Object_9finalize_void(%rip), %rax
	movq	%rax, __J_dv_16java.lang.Object+88(%rip)
	retq
	.cfi_endproc

	.globl	__J_16java.lang.Object_7equals_16java.lang.Object
	.p2align	4, 0x90
__J_16java.lang.Object_7equals_16java.lang.Object: ## @_J_16java.lang.Object_7equals_16java.lang.Object
	.cfi_startproc
## BB#0:
	movq	%rsi, -8(%rsp)
	xorl	%eax, %eax
	retq
	.cfi_endproc

	.globl	__J_16java.lang.Object_9getClass_void
	.p2align	4, 0x90
__J_16java.lang.Object_9getClass_void:  ## @_J_16java.lang.Object_9getClass_void
	.cfi_startproc
## BB#0:
	xorl	%eax, %eax
	retq
	.cfi_endproc

	.globl	__J_16java.lang.Object_8hashCode_void
	.p2align	4, 0x90
__J_16java.lang.Object_8hashCode_void:  ## @_J_16java.lang.Object_8hashCode_void
	.cfi_startproc
## BB#0:
	movl	%edi, %eax
	retq
	.cfi_endproc

	.globl	__J_16java.lang.Object_10notifyAll_void
	.p2align	4, 0x90
__J_16java.lang.Object_10notifyAll_void: ## @_J_16java.lang.Object_10notifyAll_void
	.cfi_startproc
## BB#0:
	retq
	.cfi_endproc

	.globl	__J_16java.lang.Object_7notify_void
	.p2align	4, 0x90
__J_16java.lang.Object_7notify_void:    ## @_J_16java.lang.Object_7notify_void
	.cfi_startproc
## BB#0:
	retq
	.cfi_endproc

	.globl	__J_16java.lang.Object_9toString_void
	.p2align	4, 0x90
__J_16java.lang.Object_9toString_void:  ## @_J_16java.lang.Object_9toString_void
	.cfi_startproc
## BB#0:
	xorl	%eax, %eax
	retq
	.cfi_endproc

	.globl	__J_16java.lang.Object_5wait_i64
	.p2align	4, 0x90
__J_16java.lang.Object_5wait_i64:       ## @_J_16java.lang.Object_5wait_i64
	.cfi_startproc
## BB#0:
	movq	%rsi, -8(%rsp)
	retq
	.cfi_endproc

	.globl	__J_16java.lang.Object_5wait_i64_i32
	.p2align	4, 0x90
__J_16java.lang.Object_5wait_i64_i32:   ## @_J_16java.lang.Object_5wait_i64_i32
	.cfi_startproc
## BB#0:
	movq	%rsi, -8(%rsp)
	movl	%edx, -12(%rsp)
	retq
	.cfi_endproc

	.globl	__J_16java.lang.Object_5wait_void
	.p2align	4, 0x90
__J_16java.lang.Object_5wait_void:      ## @_J_16java.lang.Object_5wait_void
	.cfi_startproc
## BB#0:
	retq
	.cfi_endproc

	.globl	__J_16java.lang.Object_6clone_void
	.p2align	4, 0x90
__J_16java.lang.Object_6clone_void:     ## @_J_16java.lang.Object_6clone_void
	.cfi_startproc
## BB#0:
	xorl	%eax, %eax
	retq
	.cfi_endproc

	.globl	__J_16java.lang.Object_9finalize_void
	.p2align	4, 0x90
__J_16java.lang.Object_9finalize_void:  ## @_J_16java.lang.Object_9finalize_void
	.cfi_startproc
## BB#0:
	retq
	.cfi_endproc

	.globl	__IntAdd_print
	.p2align	4, 0x90
__IntAdd_print:                         ## @_IntAdd_print
	.cfi_startproc
## BB#0:
	pushq	%rax
Ltmp0:
	.cfi_def_cfa_offset 16
	movl	%edi, %ecx
	leaq	L_.str(%rip), %rdi
	xorl	%eax, %eax
	movl	%ecx, %esi
	callq	_printf
	popq	%rax
	retq
	.cfi_endproc

	.globl	__IntComparisons_print
	.p2align	4, 0x90
__IntComparisons_print:                 ## @_IntComparisons_print
	.cfi_startproc
## BB#0:
	pushq	%rax
Ltmp1:
	.cfi_def_cfa_offset 16
	movl	%edi, %ecx
	leaq	L_.str(%rip), %rdi
	xorl	%eax, %eax
	movl	%ecx, %esi
	callq	_printf
	popq	%rax
	retq
	.cfi_endproc

	.globl	__Conditions_print
	.p2align	4, 0x90
__Conditions_print:                     ## @_Conditions_print
	.cfi_startproc
## BB#0:
	pushq	%rax
Ltmp2:
	.cfi_def_cfa_offset 16
	movl	%edi, %ecx
	leaq	L_.str(%rip), %rdi
	xorl	%eax, %eax
	movl	%ecx, %esi
	callq	_printf
	popq	%rax
	retq
	.cfi_endproc

	.globl	__SimpleClass_print
	.p2align	4, 0x90
__SimpleClass_print:                    ## @_SimpleClass_print
	.cfi_startproc
## BB#0:
	pushq	%rax
Ltmp3:
	.cfi_def_cfa_offset 16
	movl	%edi, %ecx
	leaq	L_.str(%rip), %rdi
	xorl	%eax, %eax
	movl	%ecx, %esi
	callq	_printf
	popq	%rax
	retq
	.cfi_endproc

	.globl	__J_11SimpleClass_5print_i32
	.p2align	4, 0x90
__J_11SimpleClass_5print_i32:           ## @_J_11SimpleClass_5print_i32
	.cfi_startproc
## BB#0:
	pushq	%rax
Ltmp4:
	.cfi_def_cfa_offset 16
	movl	%edi, %ecx
	leaq	L_.str(%rip), %rdi
	xorl	%eax, %eax
	movl	%ecx, %esi
	callq	_printf
	popq	%rax
	retq
	.cfi_endproc

	.globl	__J_init_11SimpleClass
	.p2align	4, 0x90
__J_init_11SimpleClass:                 ## @_J_init_11SimpleClass
	.cfi_startproc
## BB#0:
	pushq	%rax
Ltmp5:
	.cfi_def_cfa_offset 16
	callq	__J_init_16java.lang.Object
	movq	__J_dv_16java.lang.Object+8(%rip), %rax
	movq	%rax, __J_dv_11SimpleClass+8(%rip)
	movq	__J_dv_16java.lang.Object+16(%rip), %rax
	movq	%rax, __J_dv_11SimpleClass+16(%rip)
	movq	__J_dv_16java.lang.Object+24(%rip), %rax
	movq	%rax, __J_dv_11SimpleClass+24(%rip)
	movq	__J_dv_16java.lang.Object+32(%rip), %rax
	movq	%rax, __J_dv_11SimpleClass+32(%rip)
	movq	__J_dv_16java.lang.Object+40(%rip), %rax
	movq	%rax, __J_dv_11SimpleClass+40(%rip)
	movq	__J_dv_16java.lang.Object+48(%rip), %rax
	movq	%rax, __J_dv_11SimpleClass+48(%rip)
	movq	__J_dv_16java.lang.Object+56(%rip), %rax
	movq	%rax, __J_dv_11SimpleClass+56(%rip)
	movq	__J_dv_16java.lang.Object+64(%rip), %rax
	movq	%rax, __J_dv_11SimpleClass+64(%rip)
	movq	__J_dv_16java.lang.Object+72(%rip), %rax
	movq	%rax, __J_dv_11SimpleClass+72(%rip)
	movq	__J_dv_16java.lang.Object+80(%rip), %rax
	movq	%rax, __J_dv_11SimpleClass+80(%rip)
	movq	__J_dv_16java.lang.Object+88(%rip), %rax
	movq	%rax, __J_dv_11SimpleClass+88(%rip)
	leaq	__J_11SimpleClass_6method_void(%rip), %rax
	movq	%rax, __J_dv_11SimpleClass+96(%rip)
	leaq	__J_11SimpleClass_13privateMethod_void(%rip), %rax
	movq	%rax, __J_dv_11SimpleClass+104(%rip)
	popq	%rax
	retq
	.cfi_endproc

	.globl	__J_11SimpleClass_6method_void
	.p2align	4, 0x90
__J_11SimpleClass_6method_void:         ## @_J_11SimpleClass_6method_void
	.cfi_startproc
## BB#0:
	pushq	%rbx
Ltmp6:
	.cfi_def_cfa_offset 16
	subq	$16, %rsp
Ltmp7:
	.cfi_def_cfa_offset 32
Ltmp8:
	.cfi_offset %rbx, -16
	movq	%rdi, %rbx
	movq	(%rbx), %rax
	callq	*104(%rax)
	movl	8(%rbx), %eax
	movl	%eax, 4(%rsp)
	movzwl	12(%rbx), %eax
	movw	%ax, 2(%rsp)
	movl	4(%rsp), %ecx
	movl	%ecx, 8(%rsp)
	movzwl	2(%rsp), %eax
	addl	%ecx, %eax
	movl	%eax, 12(%rsp)
	addq	$16, %rsp
	popq	%rbx
	retq
	.cfi_endproc

	.globl	__J_11SimpleClass_13privateMethod_void
	.p2align	4, 0x90
__J_11SimpleClass_13privateMethod_void: ## @_J_11SimpleClass_13privateMethod_void
	.cfi_startproc
## BB#0:
	movw	$65, 12(%rdi)
	movl	$23, %eax
	retq
	.cfi_endproc

	.globl	__J_init_14UseSimpleClass
	.p2align	4, 0x90
__J_init_14UseSimpleClass:              ## @_J_init_14UseSimpleClass
	.cfi_startproc
## BB#0:
	pushq	%rax
Ltmp9:
	.cfi_def_cfa_offset 16
	callq	__J_init_11SimpleClass
	leaq	__J_dv_11SimpleClass(%rip), %rax
	movq	%rax, __J_dv_14UseSimpleClass(%rip)
	movq	__J_dv_11SimpleClass+8(%rip), %rax
	movq	%rax, __J_dv_14UseSimpleClass+8(%rip)
	movq	__J_dv_11SimpleClass+16(%rip), %rax
	movq	%rax, __J_dv_14UseSimpleClass+16(%rip)
	movq	__J_dv_11SimpleClass+24(%rip), %rax
	movq	%rax, __J_dv_14UseSimpleClass+24(%rip)
	movq	__J_dv_11SimpleClass+32(%rip), %rax
	movq	%rax, __J_dv_14UseSimpleClass+32(%rip)
	movq	__J_dv_11SimpleClass+40(%rip), %rax
	movq	%rax, __J_dv_14UseSimpleClass+40(%rip)
	movq	__J_dv_11SimpleClass+48(%rip), %rax
	movq	%rax, __J_dv_14UseSimpleClass+48(%rip)
	movq	__J_dv_11SimpleClass+56(%rip), %rax
	movq	%rax, __J_dv_14UseSimpleClass+56(%rip)
	movq	__J_dv_11SimpleClass+64(%rip), %rax
	movq	%rax, __J_dv_14UseSimpleClass+64(%rip)
	movq	__J_dv_11SimpleClass+72(%rip), %rax
	movq	%rax, __J_dv_14UseSimpleClass+72(%rip)
	movq	__J_dv_11SimpleClass+80(%rip), %rax
	movq	%rax, __J_dv_14UseSimpleClass+80(%rip)
	movq	__J_dv_11SimpleClass+88(%rip), %rax
	movq	%rax, __J_dv_14UseSimpleClass+88(%rip)
	leaq	__J_14UseSimpleClass_6method_void(%rip), %rax
	movq	%rax, __J_dv_14UseSimpleClass+96(%rip)
	movq	__J_dv_11SimpleClass+104(%rip), %rax
	movq	%rax, __J_dv_14UseSimpleClass+104(%rip)
	leaq	__J_14UseSimpleClass_7method2_void(%rip), %rax
	movq	%rax, __J_dv_14UseSimpleClass+112(%rip)
	popq	%rax
	retq
	.cfi_endproc

	.globl	__J_14UseSimpleClass_6method_void
	.p2align	4, 0x90
__J_14UseSimpleClass_6method_void:      ## @_J_14UseSimpleClass_6method_void
	.cfi_startproc
## BB#0:
	pushq	%rax
Ltmp10:
	.cfi_def_cfa_offset 16
	callq	__J_11SimpleClass_6method_void
	movl	%eax, 4(%rsp)
	incl	%eax
	movl	%eax, (%rsp)
	popq	%rcx
	retq
	.cfi_endproc

	.globl	__J_14UseSimpleClass_7method2_void
	.p2align	4, 0x90
__J_14UseSimpleClass_7method2_void:     ## @_J_14UseSimpleClass_7method2_void
	.cfi_startproc
## BB#0:
	pushq	%rax
Ltmp11:
	.cfi_def_cfa_offset 16
	movq	(%rdi), %rax
	callq	*96(%rax)
	movl	%eax, 4(%rsp)
	incl	%eax
	movl	%eax, (%rsp)
	popq	%rcx
	retq
	.cfi_endproc

	.globl	__J_init_11SubSubClass
	.p2align	4, 0x90
__J_init_11SubSubClass:                 ## @_J_init_11SubSubClass
	.cfi_startproc
## BB#0:
	pushq	%rax
Ltmp12:
	.cfi_def_cfa_offset 16
	callq	__J_init_14UseSimpleClass
	leaq	__J_dv_14UseSimpleClass(%rip), %rax
	movq	%rax, __J_dv_11SubSubClass(%rip)
	movq	__J_dv_14UseSimpleClass+8(%rip), %rax
	movq	%rax, __J_dv_11SubSubClass+8(%rip)
	movq	__J_dv_14UseSimpleClass+16(%rip), %rax
	movq	%rax, __J_dv_11SubSubClass+16(%rip)
	leaq	__J_11SubSubClass_8hashCode_void(%rip), %rax
	movq	%rax, __J_dv_11SubSubClass+24(%rip)
	movq	__J_dv_14UseSimpleClass+32(%rip), %rax
	movq	%rax, __J_dv_11SubSubClass+32(%rip)
	movq	__J_dv_14UseSimpleClass+40(%rip), %rax
	movq	%rax, __J_dv_11SubSubClass+40(%rip)
	movq	__J_dv_14UseSimpleClass+48(%rip), %rax
	movq	%rax, __J_dv_11SubSubClass+48(%rip)
	movq	__J_dv_14UseSimpleClass+56(%rip), %rax
	movq	%rax, __J_dv_11SubSubClass+56(%rip)
	movq	__J_dv_14UseSimpleClass+64(%rip), %rax
	movq	%rax, __J_dv_11SubSubClass+64(%rip)
	movq	__J_dv_14UseSimpleClass+72(%rip), %rax
	movq	%rax, __J_dv_11SubSubClass+72(%rip)
	movq	__J_dv_14UseSimpleClass+80(%rip), %rax
	movq	%rax, __J_dv_11SubSubClass+80(%rip)
	movq	__J_dv_14UseSimpleClass+88(%rip), %rax
	movq	%rax, __J_dv_11SubSubClass+88(%rip)
	movq	__J_dv_14UseSimpleClass+96(%rip), %rax
	movq	%rax, __J_dv_11SubSubClass+96(%rip)
	movq	__J_dv_14UseSimpleClass+104(%rip), %rax
	movq	%rax, __J_dv_11SubSubClass+104(%rip)
	leaq	__J_11SubSubClass_7method2_void(%rip), %rax
	movq	%rax, __J_dv_11SubSubClass+112(%rip)
	popq	%rax
	retq
	.cfi_endproc

	.globl	__J_11SubSubClass_8hashCode_void
	.p2align	4, 0x90
__J_11SubSubClass_8hashCode_void:       ## @_J_11SubSubClass_8hashCode_void
	.cfi_startproc
## BB#0:
	pushq	%rax
Ltmp13:
	.cfi_def_cfa_offset 16
	callq	__J_16java.lang.Object_8hashCode_void
	movl	%eax, (%rsp)
	addl	$4, %eax
	movl	%eax, 4(%rsp)
	popq	%rcx
	retq
	.cfi_endproc

	.globl	__J_11SubSubClass_7method2_void
	.p2align	4, 0x90
__J_11SubSubClass_7method2_void:        ## @_J_11SubSubClass_7method2_void
	.cfi_startproc
## BB#0:
	pushq	%rax
Ltmp14:
	.cfi_def_cfa_offset 16
	callq	__J_14UseSimpleClass_7method2_void
	movl	%eax, (%rsp)
	addl	$-10, %eax
	movl	%eax, 4(%rsp)
	popq	%rcx
	retq
	.cfi_endproc

	.globl	_main
	.p2align	4, 0x90
_main:                                  ## @main
	.cfi_startproc
## BB#0:
	subq	$72, %rsp
Ltmp15:
	.cfi_def_cfa_offset 80
	movq	$0, 8(%rsp)
	movl	$16, %edi
	callq	_malloc
	leaq	__J_dv_11SubSubClass(%rip), %rcx
	movq	%rcx, (%rax)
	movq	%rax, 8(%rsp)
	movl	$65, 8(%rax)
	movq	8(%rsp), %rdi
	movq	(%rdi), %rax
	callq	*96(%rax)
	movl	%eax, 20(%rsp)
	movq	8(%rsp), %rdi
	movq	(%rdi), %rax
	callq	*112(%rax)
	movl	%eax, 16(%rsp)
	movl	20(%rsp), %edi
	movl	%edi, 52(%rsp)
	addl	16(%rsp), %edi
	movl	%edi, 68(%rsp)
	movl	%edi, 48(%rsp)
	callq	__J_11SimpleClass_5print_i32
	movq	$0, 24(%rsp)
	movq	8(%rsp), %rdi
	movq	%rdi, 24(%rsp)
	movq	(%rdi), %rax
	callq	*96(%rax)
	movl	%eax, 60(%rsp)
	movl	%eax, 44(%rsp)
	movl	%eax, %edi
	callq	__J_11SimpleClass_5print_i32
	movq	8(%rsp), %rdi
	movq	(%rdi), %rax
	callq	*24(%rax)
	movl	%eax, 64(%rsp)
	movl	%eax, 40(%rsp)
	movl	%eax, %edi
	callq	__J_11SimpleClass_5print_i32
	movq	8(%rsp), %rdi
	movq	(%rdi), %rax
	callq	*24(%rax)
	movl	%eax, 56(%rsp)
	movl	%eax, 36(%rsp)
	movl	%eax, %edi
	callq	__J_11SimpleClass_5print_i32
	addq	$72, %rsp
	retq
	.cfi_endproc

	.globl	__J_size_16java.lang.Object ## @_J_size_16java.lang.Object
.zerofill __DATA,__common,__J_size_16java.lang.Object,8,3
	.globl	__J_dv_16java.lang.Object ## @_J_dv_16java.lang.Object
.zerofill __DATA,__common,__J_dv_16java.lang.Object,96,4
	.section	__DATA,__mod_init_func,mod_init_funcs
	.p2align	3
	.quad	__J_init_16java.lang.Object
	.quad	__J_init_11SimpleClass
	.quad	__J_init_14UseSimpleClass
	.quad	__J_init_11SubSubClass
	.section	__TEXT,__cstring,cstring_literals
L_.str:                                 ## @.str
	.asciz	"%d\n"

	.globl	__J_size_11SimpleClass  ## @_J_size_11SimpleClass
.zerofill __DATA,__common,__J_size_11SimpleClass,8,3
	.globl	__J_dv_11SimpleClass    ## @_J_dv_11SimpleClass
.zerofill __DATA,__common,__J_dv_11SimpleClass,112,4
	.globl	__J_size_14UseSimpleClass ## @_J_size_14UseSimpleClass
.zerofill __DATA,__common,__J_size_14UseSimpleClass,8,3
	.globl	__J_dv_14UseSimpleClass ## @_J_dv_14UseSimpleClass
.zerofill __DATA,__common,__J_dv_14UseSimpleClass,120,4
	.globl	__J_size_11SubSubClass  ## @_J_size_11SubSubClass
.zerofill __DATA,__common,__J_size_11SubSubClass,8,3
	.globl	__J_dv_11SubSubClass    ## @_J_dv_11SubSubClass
.zerofill __DATA,__common,__J_dv_11SubSubClass,120,4

.subsections_via_symbols
