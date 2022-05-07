package dev.xdark.ssvm.mirror;

import dev.xdark.ssvm.VirtualMachine;
import dev.xdark.ssvm.value.JavaValue;
import dev.xdark.ssvm.value.Value;
import me.coley.cafedude.classfile.ClassFile;
import org.objectweb.asm.ClassReader;
import org.objectweb.asm.tree.ClassNode;

import java.util.List;

/**
 * Class representing Java class of instance values.
 *
 * @apiNote xDark
 */
public interface InstanceJavaClass extends JavaClass {

	/**
	 * Returns VM instance in which this class
	 * was loaded.
	 *
	 * @return VM instance.
	 */
	VirtualMachine getVM();

	/**
	 * Searches for a virtual method by it's name and descriptor recursively.
	 *
	 * @param name
	 * 		Name of the method.
	 * @param desc
	 * 		Descriptor of the method.
	 *
	 * @return class method or {@code null}, if not found.
	 */
	JavaMethod getVirtualMethodRecursively(String name, String desc);

	/**
	 * Searches for an interface method by it's name and descriptor recursively.
	 *
	 * @param name
	 * 		Name of the method.
	 * @param desc
	 * 		Descriptor of the method.
	 *
	 * @return class method or {@code null}, if not found.
	 */
	JavaMethod getInterfaceMethodRecursively(String name, String desc);

	/**
	 * Searches for a virtual method by it's name and descriptor.
	 *
	 * @param name
	 * 		Name of the method.
	 * @param desc
	 * 		Descriptor of the method.
	 *
	 * @return class method or {@code null}, if not found.
	 */
	JavaMethod getVirtualMethod(String name, String desc);

	/**
	 * Searches for a virtual field by it's name and descriptor.
	 *
	 * @param name
	 * 		Name of the field.
	 * @param desc
	 * 		Descriptor of the field.
	 *
	 * @return virtual class field or {@code null}, if not found.
	 */
	JavaField getVirtualField(String name, String desc);

	/**
	 * Searches for virtual field by it's name and descriptor
	 * recursively.
	 *
	 * @param name
	 * 		Name of the field.
	 * @param desc
	 * 		Descriptor of the field.
	 *
	 * @return static class field or {@code null}, if not found.
	 */
	JavaField getVirtualFieldRecursively(String name, String desc);

	/**
	 * Searches for a static field by it's name and descriptor.
	 *
	 * @param name
	 * 		Name of the field.
	 * @param desc
	 * 		Descriptor of the field.
	 *
	 * @return static class field or {@code null}, if not found.
	 */
	JavaField getStaticField(String name, String desc);

	/**
	 * Searches for a static field by it's name and descriptor
	 * recursively.
	 *
	 * @param name
	 * 		Name of the field.
	 * @param desc
	 * 		Descriptor of the field.
	 *
	 * @return static class field or {@code null}, if not found.
	 */
	JavaField getStaticFieldRecursively(String name, String desc);

	/**
	 * Searches for a static method by it's name and descriptor recursively.
	 *
	 * @param name
	 * 		Name of the method.
	 * @param desc
	 * 		Descriptor of the method.
	 *
	 * @return class method or {@code null}, if not found.
	 */
	JavaMethod getStaticMethodRecursively(String name, String desc);

	/**
	 * Searches for a static method by it's name and descriptor.
	 *
	 * @param name
	 * 		Name of the method.
	 * @param desc
	 * 		Descriptor of the method.
	 *
	 * @return class method or {@code null}, if not found.
	 */
	JavaMethod getStaticMethod(String name, String desc);

	/**
	 * Searches for a method by it's name and descriptor.
	 *
	 * @param name
	 * 		Name of the method.
	 * @param desc
	 * 		Descriptor of the method.
	 *
	 * @return class method or {@code null}, if not found.
	 */
	JavaMethod getMethod(String name, String desc);

	/**
	 * Returns static value of a field.
	 *
	 * @param field
	 * 		Field info.
	 *
	 * @return static value of a field or {@code null},
	 * if field was not found.
	 */
	Value getStaticValue(MemberKey field);

	/**
	 * Returns static value of a field.
	 *
	 * @param name
	 * 		Field name.
	 * @param desc
	 * 		Field descriptor.
	 *
	 * @return static value of a field or {@code null},
	 * if field was not found.
	 */
	Value getStaticValue(String name, String desc);

	/**
	 * Sets static value for a field.
	 *
	 * @param field
	 * 		Field info.
	 * @param value
	 * 		New value.
	 *
	 * @return whether the value was changed or not.
	 * This method will return {@code false} if there is no such field.
	 */
	boolean setFieldValue(MemberKey field, Value value);

	/**
	 * Sets static value for a field.
	 *
	 * @param name
	 * 		Field name.
	 * @param desc
	 * 		Field desc.
	 * @param value
	 * 		New value.
	 *
	 * @return whether the value was changed or not.
	 * This method will return {@code false} if there is no such field.
	 */
	boolean setFieldValue(String name, String desc, Value value);

	/**
	 * Searches for field offset.
	 *
	 * @param name
	 * 		Field name.
	 * @param desc
	 * 		Field desc.
	 *
	 * @return field offset or {@code -1L} if not found.
	 */
	long getFieldOffset(String name, String desc);

	/**
	 * Searches for field offset recursively.
	 *
	 * @param name
	 * 		Field name.
	 * @param desc
	 * 		Field desc.
	 *
	 * @return field offset or {@code -1L} if not found.
	 */
	long getFieldOffsetRecursively(String name, String desc);

	/**
	 * Searches for field offset recursively.
	 *
	 * @param name
	 * 		Field name.
	 *
	 * @return field offset or {@code -1L} if not found.
	 */
	long getFieldOffsetRecursively(String name);

	/**
	 * Checks whether virtual field exists.
	 *
	 * @param info
	 * 		Field info.
	 *
	 * @return {@code true} if field exists, {@code false}
	 * otherwise.
	 */
	boolean hasVirtualField(MemberKey info);

	/**
	 * Checks whether virtual field exists.
	 *
	 * @param name
	 * 		Field name.
	 * @param desc
	 * 		Field desc.
	 *
	 * @return {@code true} if field exists, {@code false}
	 * otherwise.
	 */
	boolean hasVirtualField(String name, String desc);

	/**
	 * Returns ASM node.
	 *
	 * @return asm node.
	 */
	ClassNode getNode();

	/**
	 * Returns class source.
	 *
	 * @return class source.
	 */
	ClassReader getClassReader();

	/**
	 * Returns list of all methods.
	 *
	 * @param publicOnly
	 * 		Should only public methods be included.
	 *
	 * @return all methods.
	 */
	List<JavaMethod> getDeclaredMethods(boolean publicOnly);

	/**
	 * Returns list of all constructors.
	 *
	 * @param publicOnly
	 * 		Should only public constructors be included.
	 *
	 * @return all constructors.
	 */
	List<JavaMethod> getDeclaredConstructors(boolean publicOnly);

	/**
	 * Returns list of all fields.
	 *
	 * @param publicOnly
	 * 		Should only public fields be included.
	 *
	 * @return all fields.
	 */
	List<JavaField> getDeclaredFields(boolean publicOnly);

	/**
	 * @return raw class file.
	 */
	ClassFile getRawClassFile();

	/**
	 * Returns virtual method layout.
	 *
	 * @return virtual method layout.
	 */
	MethodLayout getVirtualMethodLayout();

	/**
	 * Returns static method layout.
	 *
	 * @return static method layout.
	 */
	MethodLayout getStaticMethodLayout();

	/**
	 * @return {@code true} if the class should be initialized,
	 * {@code false} otherwise.
	 */
	boolean shouldBeInitialized();

	/**
	 * @return super class without calling initialization.
	 */
	InstanceJavaClass getSuperclassWithoutResolving();

	/**
	 * Called on class linkage.
	 */
	void link();

	/**
	 * Sets oop of the class.
	 *
	 * @param oop
	 * 		Class oop.
	 */
	void setOop(JavaValue<InstanceJavaClass> oop);

	/**
	 * Loads hierarchy of classes without marking them as resolved.
	 */
	void loadNoResolve();

	/**
	 * @param slot
	 * 		Method slot.
	 *
	 * @return method by it's slot.
	 */
	JavaMethod getMethodBySlot(int slot);

	/**
	 * @param slot
	 * 		Field slot.
	 *
	 * @return field by it's slot.
	 */
	JavaField getFieldBySlot(int slot);
}
