package net.alterorb.launcher.patcher.impl;

import net.alterorb.launcher.patcher.Patch;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.tree.AbstractInsnNode;
import org.objectweb.asm.tree.ClassNode;
import org.objectweb.asm.tree.InsnList;
import org.objectweb.asm.tree.MethodInsnNode;
import org.objectweb.asm.tree.MethodNode;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Objects;

/**
 * Patches the MouseInput listener to make right click work on jdk9+.
 */
public class MouseInputPatch implements Patch {

    private static final Logger LOGGER = LoggerFactory.getLogger(MouseInputPatch.class);
    private String mouseListenerClass;

    @Override
    public boolean applicable(String className) {
        return Objects.equals(className, mouseListenerClass);
    }

    @Override
    public byte[] apply(byte[] classData) {
        ClassNode classNode = byteArrayToClassNode(classData);

        for (MethodNode methodNode : classNode.methods) {

            if (Objects.equals(methodNode.name, "mousePressed")) {
                InsnList instructions = methodNode.instructions;

                for (AbstractInsnNode abstractInsn : instructions) {
                    if (abstractInsn.getOpcode() != Opcodes.INVOKEVIRTUAL) {
                        continue;
                    }
                    MethodInsnNode methodInsn = (MethodInsnNode) abstractInsn;

                    if (!methodInsn.name.equals("isMetaDown")) {
                        continue;
                    }
                    LOGGER.debug("Replaced instruction");
                    MethodInsnNode replacementFieldInsn = new MethodInsnNode(Opcodes.INVOKESTATIC, "javax/swing/SwingUtilities", "isRightMouseButton", "(Ljava/awt/event/MouseEvent;)Z", false);
                    instructions.set(methodInsn, replacementFieldInsn);
                    break;
                }
                LOGGER.debug("Patched mousePressed method");
                break;
            }
        }
        return classNodeToByteArray(classNode);
    }
}
