package dev.xdark.ssvm.execution;

import dev.xdark.ssvm.thread.ThreadRegion;
import dev.xdark.ssvm.thread.SimpleThreadStorage;
import dev.xdark.ssvm.value.TopValue;
import dev.xdark.ssvm.value.Value;
import lombok.RequiredArgsConstructor;

import java.util.Arrays;
import java.util.List;
import java.util.Objects;

/**
 * Method execution stack.
 *
 * @author xDark
 */
@RequiredArgsConstructor
public final class Stack implements AutoCloseable {

	private final ThreadRegion stack;
	private int cursor;

	/**
	 * @param maxSize
	 * 		The maximum size of the stack.
	 */
	public Stack(int maxSize) {
		stack = SimpleThreadStorage.threadPush(maxSize);
	}

	/**
	 * Pushes value onto the stack.
	 *
	 * @param value
	 * 		Value to push.
	 */
	public void push(Value value) {
		checkValue(value);
		if (value.isWide()) {
			throw new IllegalStateException("Must use pushWide instead");
		}
		stack.set(cursor++, value);
	}

	/**
	 * Pushes wide value onto the stack.
	 * Inserts TOP after.
	 *
	 * @param value
	 * 		Value to push.
	 */
	public void pushWide(Value value) {
		checkValue(value);
		if (!value.isWide()) {
			throw new IllegalStateException("Must use push instead");
		}
		ThreadRegion stack = this.stack;
		int cursor = this.cursor;
		stack.set(cursor++, value);
		stack.set(cursor++, TopValue.INSTANCE);
		this.cursor = cursor;
	}

	/**
	 * Pushes generic value onto the stack.
	 * If the value is wide, TOP will also be pushed.
	 *
	 * @param value
	 * 		Value to push.
	 */
	public void pushGeneric(Value value) {
		if (value.isWide()) {
			pushWide(value);
		} else {
			push(value);
		}
	}

	/**
	 * Pops value off the stack.
	 *
	 * @param <V>
	 * 		Type of the value.
	 *
	 * @return value popped off the stack.
	 */
	public <V extends Value> V pop() {
		return (V) stack.get(--cursor);
	}

	/**
	 * Pops wide value off the stack.
	 *
	 * @param <V>
	 * 		Type of the value.
	 *
	 * @return wide value popped off the stack.
	 *
	 * @throws IllegalStateException
	 * 		If wide value does not occupy two slots.
	 */
	public <V extends Value> V popWide() {
		Value top = pop();
		if (top != TopValue.INSTANCE) {
			throw new IllegalStateException("Expected to pop TOP value, but got: " + top);
		}
		return pop();
	}

	/**
	 * Pops generic value off the stack.
	 *
	 * @param <V>
	 * 		Type of the value.
	 *
	 * @return generic value popped off the stack.
	 */
	public <V extends Value> V popGeneric() {
		Value top = pop();
		if (top == TopValue.INSTANCE) {
			return pop();
		}
		return (V) top;
	}

	/**
	 * Peeks value from the stack.
	 *
	 * @param <V>
	 * 		Type of the value.
	 *
	 * @return value peeked from the stack.
	 */
	public <V extends Value> V peek() {
		return (V) stack.get(cursor - 1);
	}

	/**
	 * Polls value from the stack.
	 * @param <V>
	 *     		Value tpye.
	 * @return tail value of the stack or {@code null},
	 * if stack is empty.
	 */
	public <V extends Value> V poll() {
		if (cursor == 0) return null;
		return (V) stack.get(--cursor);
	}

	/**
	 * Duplicates value on the stack.
	 */
	public void dup() {
		ThreadRegion stack = this.stack;
		int cursor = this.cursor;
		stack.set(this.cursor++, stack.get(cursor - 1));
	}

	/**
	 * Duplicate the top operand stack value and insert two values down.
	 */
	public void dupx1() {
		Value v1 = pop();
		Value v2 = pop();
		push(v1);
		push(v2);
		push(v1);
	}

	/**
	 * Duplicate the top operand stack value
	 * and insert two or three values down.
	 */
	public void dupx2() {
		Value v1 = pop();
		Value v2 = popGeneric();
		if (v2.isWide()) {
			push(v1);
			pushWide(v2);
			push(v1);
		} else {
			Value v3 = pop();
			push(v1);
			push(v3);
			push(v2);
			push(v1);
		}
	}

	/**
	 * Duplicate the top one or two operand stack values.
	 */
	public void dup2() {
		Value v = popGeneric();
		if (v.isWide()) {
			pushWide(v);
			pushWide(v);
		} else {
			Value v2 = pop();
			push(v2);
			push(v);
			push(v2);
			push(v);
		}
	}

	/**
	 * Duplicate the top one or two operand stack values
	 * and insert two or three values down.
	 */
	public void dup2x1() {
		Value v = popGeneric();
		if (v.isWide()) {
			Value v2 = pop();
			pushWide(v);
			push(v2);
			pushWide(v);
		} else {
			Value v2 = pop();
			Value v3 = pop();
			push(v2);
			push(v);
			push(v3);
			push(v2);
			push(v);
		}
	}

	/**
	 * Duplicate the top one or two operand stack values
	 * and insert two, three, or four values down.
	 */
	public void dup2x2() {
		Value v1 = popGeneric();
		Value v2 = popGeneric();
		if (v1.isWide()) {
			if (v2.isWide()) {
				pushWide(v1);
				pushWide(v2);
				pushWide(v1);
			} else {
				Value v3 = pop();
				pushWide(v1);
				push(v3);
				push(v2);
				pushWide(v1);
			}
		} else {
			Value v3 = popGeneric();
			//noinspection IfStatementWithIdenticalBranches
			if (v3.isWide()) {
				push(v2);
				push(v1);
				pushWide(v3);
				push(v2);
				push(v1);
			} else {
				Value v4 = pop();
				push(v2);
				push(v1);
				push(v4);
				push(v3);
				push(v2);
				push(v1);
			}
		}
	}

	/**
	 * Swap the top two operand stack values.
	 */
	public void swap() {
		ThreadRegion stack = this.stack;
		int cursor = this.cursor;
		Value v1 = stack.get(cursor - 1);
		Value v2 = stack.get(cursor - 2);
		stack.set(cursor - 1, v2);
		stack.set(cursor - 2, v1);
	}

	/**
	 * Returns whether the stack is empty.
	 *
	 * @return {@code true} if stack is empty,
	 * {@code false} otherwise.
	 */
	public boolean isEmpty() {
		return cursor == 0;
	}

	/**
	 * Resets stack.
	 */
	public void clear() {
		cursor = 0;
	}

	/**
	 * @return current cursor position.
	 */
	public int position() {
		return cursor;
	}

	/**
	 * Gets value on the stack by an index.
	 *
	 * @param index
	 * 		Value position.
	 *
	 * @return value at the specific position.
	 */
	public Value getAt(int index) {
		return stack.get(index);
	}

	/**
	 * Returns stack content as a list view.
	 *
	 * @return stack content as a list view.
	 */
	public List<Value> view() {
		return Arrays.asList(Arrays.copyOf(stack.unwrap(), cursor));
	}

	/**
	 * Deallocates internal stack.
	 */
	public void deallocate() {
		stack.close();
	}

	@Override
	public void close() {
		deallocate();
	}

	@Override
	public boolean equals(Object o) {
		if (o == this) return true;
		if (!(o instanceof Stack)) return false;
		Stack other = (Stack) o;
		int cursor = this.cursor;
		if (cursor != other.cursor) return false;
		for (int i = 0; i < cursor; i++) {
			if (!Objects.equals(getAt(i), other.getAt(i))) {
				return false;
			}
		}
		return true;
	}
	
	@Override
	public int hashCode() {
		int result = 1;
		int cursor = this.cursor;
		ThreadRegion stack = this.stack;
		for (int i = 0; i < cursor; i++) {
			result *= 31;
			result += Objects.hashCode(stack.get(i).hashCode());
		}
		return result;
	}

	@Override
	public String toString() {
		return "Stack{" +
				"stack=" + Arrays.toString(stack.unwrap()) +
				", cursor=" + cursor +
				'}';
	}

	private static void checkValue(Value value) {
		if (Objects.requireNonNull(value, "value").isVoid()) {
			throw new IllegalStateException("Cannot push void value");
		}
	}
}
