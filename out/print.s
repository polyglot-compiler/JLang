	.section	__TEXT,__text,regular,pure_instructions
	.macosx_version_min 10, 11
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

	.section	__TEXT,__cstring,cstring_literals
L_.str:                                 ## @.str
	.asciz	"%d\n"


.subsections_via_symbols
