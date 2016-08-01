	.section	__TEXT,__text,regular,pure_instructions
	.macosx_version_min 10, 11
	.globl	__J_init_14UseSimpleClass
	.p2align	4, 0x90
__J_init_14UseSimpleClass:              ## @_J_init_14UseSimpleClass
	.cfi_startproc
## BB#0:
	pushq	%rax
Ltmp0:
	.cfi_def_cfa_offset 16
	callq	__J_init_11SimpleClass
	leaq	__J_dv_11SimpleClass(%rip), %rax
	movq	%rax, __J_dv_14UseSimpleClass(%rip)
	leaq	__UseSimpleClass_method(%rip), %rax
	movq	%rax, __J_dv_14UseSimpleClass+8(%rip)
	movq	__J_dv_11SimpleClass+16(%rip), %rax
	movq	%rax, __J_dv_14UseSimpleClass+16(%rip)
	leaq	__UseSimpleClass_method2(%rip), %rax
	movq	%rax, __J_dv_14UseSimpleClass+24(%rip)
	popq	%rax
	retq
	.cfi_endproc

	.globl	__UseSimpleClass_method
	.p2align	4, 0x90
__UseSimpleClass_method:                ## @_UseSimpleClass_method
	.cfi_startproc
## BB#0:
	pushq	%rax
Ltmp1:
	.cfi_def_cfa_offset 16
	movq	(%rdi), %rax
	movq	(%rax), %rax
	callq	*8(%rax)
	movl	%eax, 4(%rsp)
	incl	%eax
	movl	%eax, (%rsp)
	popq	%rcx
	retq
	.cfi_endproc

	.globl	__UseSimpleClass_method2
	.p2align	4, 0x90
__UseSimpleClass_method2:               ## @_UseSimpleClass_method2
	.cfi_startproc
## BB#0:
	pushq	%rax
Ltmp2:
	.cfi_def_cfa_offset 16
	movq	(%rdi), %rax
	callq	*8(%rax)
	movl	%eax, 4(%rsp)
	incl	%eax
	movl	%eax, (%rsp)
	popq	%rcx
	retq
	.cfi_endproc

	.globl	_main
	.p2align	4, 0x90
_main:                                  ## @main
	.cfi_startproc
## BB#0:
	subq	$24, %rsp
Ltmp3:
	.cfi_def_cfa_offset 32
	movq	$0, 8(%rsp)
	movl	$16, %edi
	callq	_malloc
	leaq	__J_dv_14UseSimpleClass(%rip), %rcx
	movq	%rcx, (%rax)
	movq	%rax, 8(%rsp)
	movl	$65, 8(%rax)
	movq	8(%rsp), %rdi
	movq	(%rdi), %rax
	callq	*8(%rax)
	movl	%eax, 20(%rsp)
	movl	%eax, 16(%rsp)
	movl	%eax, %edi
	callq	__SimpleClass_print
	addq	$24, %rsp
	retq
	.cfi_endproc

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
	pushq	%rbx
Ltmp4:
	.cfi_def_cfa_offset 16
	subq	$16, %rsp
Ltmp5:
	.cfi_def_cfa_offset 32
Ltmp6:
	.cfi_offset %rbx, -16
	movq	%rdi, %rbx
	movq	(%rbx), %rax
	callq	*16(%rax)
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

	.globl	__SimpleClass_privateMethod
	.p2align	4, 0x90
__SimpleClass_privateMethod:            ## @_SimpleClass_privateMethod
	.cfi_startproc
## BB#0:
	movw	$65, 12(%rdi)
	movl	$23, %eax
	retq
	.cfi_endproc

	.globl	__IntAdd_print
	.p2align	4, 0x90
__IntAdd_print:                         ## @_IntAdd_print
	.cfi_startproc
## BB#0:
	pushq	%rax
Ltmp7:
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
Ltmp8:
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
Ltmp9:
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
Ltmp10:
	.cfi_def_cfa_offset 16
	movl	%edi, %ecx
	leaq	L_.str(%rip), %rdi
	xorl	%eax, %eax
	movl	%ecx, %esi
	callq	_printf
	popq	%rax
	retq
	.cfi_endproc

	.globl	__J_size_14UseSimpleClass ## @_J_size_14UseSimpleClass
.zerofill __DATA,__common,__J_size_14UseSimpleClass,8,3
	.globl	__J_dv_14UseSimpleClass ## @_J_dv_14UseSimpleClass
.zerofill __DATA,__common,__J_dv_14UseSimpleClass,32,4
	.section	__DATA,__mod_init_func,mod_init_funcs
	.p2align	3
	.quad	__J_init_14UseSimpleClass
	.quad	__J_init_11SimpleClass
	.globl	__J_size_11SimpleClass  ## @_J_size_11SimpleClass
.zerofill __DATA,__common,__J_size_11SimpleClass,8,3
	.globl	__J_dv_11SimpleClass    ## @_J_dv_11SimpleClass
.zerofill __DATA,__common,__J_dv_11SimpleClass,24,4
	.section	__TEXT,__cstring,cstring_literals
L_.str:                                 ## @.str
	.asciz	"%d\n"


.subsections_via_symbols
