package dev.xdark.ssvm.memory.gc;

import dev.xdark.ssvm.VirtualMachine;
import dev.xdark.ssvm.classloading.ClassLoaderData;
import dev.xdark.ssvm.classloading.ClassLoaders;
import dev.xdark.ssvm.execution.ExecutionContext;
import dev.xdark.ssvm.execution.PanicException;
import dev.xdark.ssvm.memory.allocation.MemoryAddress;
import dev.xdark.ssvm.memory.allocation.MemoryAllocator;
import dev.xdark.ssvm.memory.allocation.MemoryBlock;
import dev.xdark.ssvm.memory.allocation.MemoryData;
import dev.xdark.ssvm.memory.management.MemoryManager;
import dev.xdark.ssvm.mirror.ArrayJavaClass;
import dev.xdark.ssvm.mirror.FieldLayout;
import dev.xdark.ssvm.mirror.InstanceJavaClass;
import dev.xdark.ssvm.mirror.JavaClass;
import dev.xdark.ssvm.mirror.JavaField;
import dev.xdark.ssvm.symbol.VMPrimitives;
import dev.xdark.ssvm.thread.StackFrame;
import dev.xdark.ssvm.thread.ThreadManager;
import dev.xdark.ssvm.thread.VMThread;
import dev.xdark.ssvm.tlc.ThreadLocalStorage;
import dev.xdark.ssvm.value.ArrayValue;
import dev.xdark.ssvm.value.InstanceValue;
import dev.xdark.ssvm.value.ObjectValue;
import dev.xdark.ssvm.value.Value;
import org.objectweb.asm.Type;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

/**
 * Primitive mark and sweep garbage collector.
 *
 * @author xDark
 */
public class MarkAndSweepGarbageCollector implements GarbageCollector {
	protected static final byte MARK_NONE = 0;
	private static final byte MARK_SET = 1;
	private static final byte MARK_REF = 2;
	private final Map<ObjectValue, GCHandle> handles = new HashMap<>();
	private final VirtualMachine vm;

	/**
	 * @param vm VM instance.
	 */
	public MarkAndSweepGarbageCollector(VirtualMachine vm) {
		this.vm = vm;
	}

	@Override
	public int reservedHeaderSize() {
		return 1;
	}

	@Override
	public GCHandle makeHandle(ObjectValue value) {
		return handles.computeIfAbsent(value, k -> {
			setMarkImpl(value, MARK_REF);
			return new SimpleGCHandle() {
				@Override
				protected void deallocate() {
					setMarkImpl(k, MARK_NONE);
					handles.remove(k);
				}
			};
		});
	}

	@Override
	public void invoke() {
		VirtualMachine vm = this.vm;
		MemoryManager memoryManager = vm.getMemoryManager();
		ThreadManager threadManager = vm.getThreadManager();
		threadManager.suspendAll();
		Collection<ObjectValue> allObjects = memoryManager.listObjects();
		ClassLoaders classLoaders = vm.getClassLoaders();
		Collection<InstanceValue> loaderList = classLoaders.getAll();
		for (InstanceValue classLoader : loaderList) {
			setMark(classLoader);
			ClassLoaderData data = classLoaders.getClassLoaderData(classLoader);
			markClassLoaderData(data);
		}
		markClassLoaderData(vm.getBootClassLoaderData());
		VMPrimitives primitives = vm.getPrimitives();
		markClass(primitives.longPrimitive());
		markClass(primitives.doublePrimitive());
		markClass(primitives.intPrimitive());
		markClass(primitives.floatPrimitive());
		markClass(primitives.charPrimitive());
		markClass(primitives.shortPrimitive());
		markClass(primitives.bytePrimitive());
		markClass(primitives.booleanPrimitive());
		for (VMThread thread : threadManager.getThreads()) {
			if (thread.isAlive()) {
				setMark(thread.getOop());
				for (StackFrame frame : thread.getBacktrace()) {
					ExecutionContext ctx = frame.getExecutionContext();
					if (ctx != null) {
						for (Value value : ctx.getStack().view()) {
							tryMark(value);
						}
						for (Value value : ctx.getLocals().getTable()) {
							tryMark(value);
						}
					}
				}
			}
		}
		MemoryAllocator allocator = vm.getMemoryAllocator();
		allObjects.removeIf(x -> {
			if (x.isNull()) {
				return false;
			}
			MemoryBlock block = x.getMemory();
			MemoryData data = block.getData();
			byte mark = data.readByte(0L);
			boolean result = mark == MARK_NONE;
			if (mark != MARK_REF) {
				data.writeByte(0L, MARK_NONE);
			}
			if (result) {
				if (!allocator.freeHeap(block.getAddress())) {
					throw new PanicException("Failed to free heap memory");
				}
			}
			return result;
		});
		threadManager.resumeAll();
	}

	private void markClassLoaderData(ClassLoaderData data) {
		MemoryManager memoryManager = vm.getMemoryManager();
		MemoryAddress address = ThreadLocalStorage.get().memoryAddress();
		for (InstanceJavaClass klass : data.getAll()) {
			markClass(klass);
			if (!klass.shouldBeInitialized()) {
				MemoryBlock memory = klass.getOop().getMemory();
				address.set(memory.getAddress());
				markAllFields(klass.getStaticFieldLayout(), memory.getData(), memoryManager.getStaticOffset(klass), MARK_SET);
			}
		}
	}

	private void markClass(JavaClass klass) {
		InstanceValue oop = klass.getOop();
		setMark(oop, MARK_SET);
		ArrayJavaClass arrayClass = klass.getArrayClass();
		while (arrayClass != null) {
			setMark(arrayClass.getOop(), MARK_SET);
			arrayClass = arrayClass.getArrayClass();
		}
	}

	private void tryMark(Value value) {
		if (value instanceof ObjectValue) {
			setMark((ObjectValue) value, MARK_SET);
		}
	}

	private void setMark(ObjectValue value) {
		setMark(value, MARK_SET);
	}

	private void setMark(ObjectValue value, byte markToSet) {
		if (!value.isNull()) {
			MemoryData data = value.getMemory().getData();
			byte mark = data.readByte(0L);
			if (mark == MARK_REF || mark == MARK_SET) {
				// Already marked/referenced outside of the VM
				return;
			}
			setMarkImpl(value, markToSet);
		}
	}

	private void setMarkImpl(ObjectValue value, byte markToSet) {
		MemoryData data = value.getMemory().getData();
		data.writeByte(0L, markToSet);
		MemoryManager memoryManager = vm.getMemoryManager();
		long offset = memoryManager.valueBaseOffset(value);
		long objectSize = memoryManager.objectSize();
		if (value instanceof ArrayValue) {
			ArrayValue arrayValue = (ArrayValue) value;
			if (!arrayValue.getJavaClass().getComponentType().isPrimitive()) {
				for (int i = 0, j = arrayValue.getLength(); i < j; i++) {
					setMark(memoryManager.getValue(data.readLong(offset + objectSize * (long) i)), markToSet);
				}
			}
		} else {
			InstanceValue instanceValue = (InstanceValue) value;
			InstanceJavaClass klass = instanceValue.getJavaClass();
			markAllFields(klass.getVirtualFieldLayout(), data, offset, markToSet);
		}
	}

	private void markAllFields(FieldLayout layout, MemoryData data, long offset, byte markToSet) {
		MemoryManager memoryManager = vm.getMemoryManager();
		for (JavaField field : layout.getAll()) {
			if (field.getType().getSort() >= Type.ARRAY) {
				setMark(memoryManager.getValue(data.readLong(offset + field.getOffset())), markToSet);
			}
		}
	}
}