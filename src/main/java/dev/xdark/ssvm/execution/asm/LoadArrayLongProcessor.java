package dev.xdark.ssvm.execution.asm;

import dev.xdark.ssvm.execution.ExecutionContext;
import dev.xdark.ssvm.execution.InstructionProcessor;
import dev.xdark.ssvm.execution.Result;
import dev.xdark.ssvm.execution.Stack;
import dev.xdark.ssvm.jit.JitHelper;
import dev.xdark.ssvm.value.Value;
import org.objectweb.asm.tree.AbstractInsnNode;

/**
 * Loads long from an array.
 *
 * @author xDark
 */
public final class LoadArrayLongProcessor implements InstructionProcessor<AbstractInsnNode> {

	@Override
	public Result execute(AbstractInsnNode insn, ExecutionContext ctx) {
		Stack stack = ctx.getStack();
		Value index = stack.pop();
		Value array = stack.pop();
		stack.pushWide(JitHelper.arrayLoadLong(array, index, ctx));
		return Result.CONTINUE;
	}
}
