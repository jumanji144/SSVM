package dev.xdark.ssvm;

import dev.xdark.ssvm.value.Value;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.objectweb.asm.Label;
import org.objectweb.asm.tree.ClassNode;

import java.util.concurrent.ThreadLocalRandom;

import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.objectweb.asm.Opcodes.*;

public class IntComparisonTest {

	private static VirtualMachine vm;

	@BeforeAll
	private static void setup() {
		vm = new VirtualMachine();
	}

	@Test // != 0
	public void test_IFNE() {
		int v = nextInt();
		assertTrue(doIntJump(v, IFNE));
	}

	@Test // == 0
	public void test_IFEQ() {
		assertTrue(doIntJump(0, IFEQ));
	}

	@Test // < 0
	public void test_IFLT() {
		var v = nextInt();
		assertTrue(doIntJump(-v, IFLT));
	}

	@Test // <= 0
	public void test_IFLE() {
		var v = nextInt();
		assertTrue(doIntJump(-v, IFLE));
		assertTrue(doIntJump(0, IFLE));
	}

	@Test // > 0
	public void test_IFGT() {
		var v = nextInt();
		assertTrue(doIntJump(v, IFGT));
	}

	@Test // > 0
	public void test_IFGE() {
		var v = nextInt();
		assertTrue(doIntJump(v, IFGE));
		assertTrue(doIntJump(0, IFGE));
	}

	private static boolean doIntJump(int value, int opcode) {
		var node = new ClassNode();
		node.visit(V11, ACC_PUBLIC, "Test", null, null, null);
		var mv = node.visitMethod(ACC_STATIC, "test", "()Z", null, null);
		var label = new Label();
		mv.visitLdcInsn(value);
		mv.visitJumpInsn(opcode, label);
		mv.visitInsn(ICONST_0);
		mv.visitInsn(IRETURN);
		mv.visitLabel(label);
		mv.visitInsn(ICONST_1);
		mv.visitInsn(IRETURN);
		mv.visitMaxs(1, 0);
		var jc = TestUtil.createClass(vm, node);
		var result = vm.getHelper().invokeStatic(jc, "test", "()Z", new Value[0], new Value[0]);
		return result.getResult().asBoolean();
	}

	private static int nextInt() {
		return ThreadLocalRandom.current().nextInt(1, Integer.MAX_VALUE - 1);
	}
}
