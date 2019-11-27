package InstructionWriter;

public enum Instructions {
    nop, bipush, ipush, pop, pop2, popn, dup, dup2, loadc, loada, snew,
    iload, dload, aload, iaload, daload, aaload,
    istore, dstore, astore, iastore, dastore, aastore,
    iadd, dadd, isub, dsub, imul, dmul, idiv, ddiv,
    ineg, dneg, icmp, dcmp,
    i2d, d2i, i2c,
    jmp, je, jne, jl, jge, jg, jle,
    call, ret, iret, dret, aret,
    iprint, dprint, cprint, sprint, printl, iscan, dscan, cscan
}
