package net.alterorb.launcher.patcher.impl;

import net.alterorb.launcher.patcher.Patch;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.tree.AbstractInsnNode;
import org.objectweb.asm.tree.ClassNode;
import org.objectweb.asm.tree.FieldInsnNode;
import org.objectweb.asm.tree.InsnList;
import org.objectweb.asm.tree.LdcInsnNode;
import org.objectweb.asm.tree.MethodNode;

import java.util.ListIterator;
import java.util.Objects;

/**
 * Replaces the public key used in the login process.
 */
public class LoginPublicKeyPatch implements Patch {

    private String publicKeyModulusClass;
    private String publicKeyModulusField;
    private String publicKeyModulus;

    private String publicKeyExponentClass;
    private String publicKeyExponentField;
    private String publicKeyExponent;

    @Override
    public boolean applicable(String className) {
        return Objects.equals(className, publicKeyModulusClass) || Objects.equals(className, publicKeyExponentClass);
    }

    @Override
    public byte[] apply(byte[] classData) {
        ClassNode classNode = byteArrayToClassNode(classData);

        for (MethodNode method : classNode.methods) {

            if (Objects.equals(method.name, "<clinit>")) {
                InsnList instructions = method.instructions;
                ListIterator<AbstractInsnNode> iterator = instructions.iterator();

                while (iterator.hasNext()) {
                    AbstractInsnNode abstractInsn = iterator.next();

                    if (abstractInsn.getOpcode() == Opcodes.PUTSTATIC) {
                        FieldInsnNode fieldInsnNode = (FieldInsnNode) abstractInsn;

                        if (Objects.equals(fieldInsnNode.name, publicKeyModulusField) || Objects.equals(fieldInsnNode.name, publicKeyExponentField)) {
                            // removes calls to the string decryptor methods
                            AbstractInsnNode bigIntegerConstructorCall = fieldInsnNode.getPrevious();

                            instructions.remove(bigIntegerConstructorCall.getPrevious());
                            instructions.remove(bigIntegerConstructorCall.getPrevious());
                            LdcInsnNode ldcInsnNode = (LdcInsnNode) bigIntegerConstructorCall.getPrevious();

                            ldcInsnNode.cst = Objects.equals(fieldInsnNode.name, publicKeyModulusField) ? publicKeyModulus : publicKeyExponent;
                        }
                    }
                }
            }
        }
        return classNodeToByteArray(classNode);
    }
}
