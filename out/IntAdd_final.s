	.section	__TEXT,__text,regular,pure_instructions
	.macosx_version_min 10, 11
	.globl	_main
	.p2align	4, 0x90
_main:                                  ## @main
## BB#0:
	pushq	%r15
	pushq	%r14
	pushq	%rbx
	movl	$10, %ebx
	movl	$1, %r15d
	leaq	L_.str(%rip), %r14
	.p2align	4, 0x90
LBB0_1:                                 ## %label.2.i
                                        ## =>This Inner Loop Header: Depth=1
	leal	-1(%r15), %esi
	xorl	%eax, %eax
	movq	%r14, %rdi
	callq	_printf
	xorl	%eax, %eax
	movq	%r14, %rdi
	movl	%ebx, %esi
	callq	_printf
	decl	%ebx
	cmpl	%ebx, %r15d
	leal	1(%r15), %eax
	movl	%eax, %r15d
	jl	LBB0_1
## BB#2:                                ## %_IntAdd_f.exit
	leaq	L_.str(%rip), %rdi
	xorl	%esi, %esi
	xorl	%eax, %eax
	popq	%rbx
	popq	%r14
	popq	%r15
	jmp	_printf                 ## TAILCALL

	.globl	__IntAdd_f
	.p2align	4, 0x90
__IntAdd_f:                             ## @_IntAdd_f
## BB#0:
	pushq	%rbp
	pushq	%r14
	pushq	%rbx
	movl	%esi, %ebx
	movl	%edi, %ebp
	cmpl	%ebx, %ebp
	jge	LBB1_3
## BB#1:                                ## %label.2.preheader
	leaq	L_.str(%rip), %r14
	.p2align	4, 0x90
LBB1_2:                                 ## %label.2
                                        ## =>This Inner Loop Header: Depth=1
	xorl	%eax, %eax
	movq	%r14, %rdi
	movl	%ebp, %esi
	callq	_printf
	xorl	%eax, %eax
	movq	%r14, %rdi
	movl	%ebx, %esi
	callq	_printf
	incl	%ebp
	decl	%ebx
	cmpl	%ebx, %ebp
	jl	LBB1_2
LBB1_3:                                 ## %label.1
	xorl	%eax, %eax
	popq	%rbx
	popq	%r14
	popq	%rbp
	retq

	.globl	__IntAdd_print
	.p2align	4, 0x90
__IntAdd_print:                         ## @_IntAdd_print
## BB#0:
	movl	%edi, %ecx
	leaq	L_.str(%rip), %rdi
	xorl	%eax, %eax
	movl	%ecx, %esi
	jmp	_printf                 ## TAILCALL

	.globl	__IntComparisons_print
	.p2align	4, 0x90
__IntComparisons_print:                 ## @_IntComparisons_print
## BB#0:
	movl	%edi, %ecx
	leaq	L_.str(%rip), %rdi
	xorl	%eax, %eax
	movl	%ecx, %esi
	jmp	_printf                 ## TAILCALL

	.section	__TEXT,__cstring,cstring_literals
L_.str:                                 ## @.str
	.asciz	"%d\n"


.subsections_via_symbols
