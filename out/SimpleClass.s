	.section	__TEXT,__text,regular,pure_instructions
	.macosx_version_min 10, 11
	.globl	__J_init_11SimpleClass
	.p2align	4, 0x90
__J_init_11SimpleClass:                 ## @_J_init_11SimpleClass
	.cfi_startproc
## BB#0:
	leaq	__SimpleClass_method(%rip), %rax
	movq	%rax, __J_dv_11SimpleClass+8(%rip)
	leaq	__SimpleClass_privateMethod(%rip), %rax
	movq	%rax, __J_dv_11SimpleClass+16(%rip)
	retq
	.cfi_endproc

	.globl	__SimpleClass_method
	.p2align	4, 0x90
__SimpleClass_method:                   ## @_SimpleClass_method
	.cfi_startproc
## BB#0:
	movl	8(%rdi), %eax
	movl	%eax, -12(%rsp)
	movzwl	12(%rdi), %eax
	movw	%ax, -14(%rsp)
	movl	-12(%rsp), %ecx
	movl	%ecx, -8(%rsp)
	movzwl	-14(%rsp), %eax
	addl	%ecx, %eax
	movl	%eax, -4(%rsp)
	retq
	.cfi_endproc

	.globl	__SimpleClass_privateMethod
	.p2align	4, 0x90
__SimpleClass_privateMethod:            ## @_SimpleClass_privateMethod
	.cfi_startproc
## BB#0:
	movl	$23, %eax
	retq
	.cfi_endproc

	.globl	_main
	.p2align	4, 0x90
_main:                                  ## @main
	.cfi_startproc
## BB#0:
	subq	$40, %rsp
Ltmp0:
	.cfi_def_cfa_offset 48
	movq	$0, 8(%rsp)
	movl	$16, %edi
	callq	_malloc
	leaq	__J_dv_11SimpleClass(%rip), %rcx
	movq	%rcx, (%rax)
	movq	%rax, 8(%rsp)
	movl	$12, 8(%rax)
	movq	8(%rsp), %rax
	movw	$65, 12(%rax)
	movq	8(%rsp), %rdi
	movq	(%rdi), %rax
	callq	*8(%rax)
	movl	%eax, 20(%rsp)
	movq	8(%rsp), %rdi
	movq	(%rdi), %rax
	callq	*16(%rax)
	movl	%eax, 24(%rsp)
	movl	20(%rsp), %edi
	movl	%edi, 32(%rsp)
	addl	24(%rsp), %edi
	movl	%edi, 36(%rsp)
	movl	%edi, 28(%rsp)
	callq	__SimpleClass_print
	addq	$40, %rsp
	retq
	.cfi_endproc

	.globl	__IntAdd_print
	.p2align	4, 0x90
__IntAdd_print:                         ## @_IntAdd_print
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

	.globl	__IntComparisons_print
	.p2align	4, 0x90
__IntComparisons_print:                 ## @_IntComparisons_print
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

	.globl	__Conditions_print
	.p2align	4, 0x90
__Conditions_print:                     ## @_Conditions_print
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

	.globl	__SimpleClass_print
	.p2align	4, 0x90
__SimpleClass_print:                    ## @_SimpleClass_print
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

	.globl	__J_size_11SimpleClass  ## @_J_size_11SimpleClass
.zerofill __DATA,__common,__J_size_11SimpleClass,8,3
	.globl	__J_dv_11SimpleClass    ## @_J_dv_11SimpleClass
.zerofill __DATA,__common,__J_dv_11SimpleClass,24,4
	.section	__DATA,__mod_init_func,mod_init_funcs
	.p2align	3
	.quad	__J_init_11SimpleClass
	.section	__TEXT,__cstring,cstring_literals
L_.str:                                 ## @.str
	.asciz	"%d\n"


.subsections_via_symbols
