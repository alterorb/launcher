package net.alterorb.launcher.patcher.impl;

import lombok.extern.slf4j.Slf4j;
import net.alterorb.launcher.patcher.Patch;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.Type;
import org.objectweb.asm.tree.ClassNode;
import org.objectweb.asm.tree.InsnList;
import org.objectweb.asm.tree.InsnNode;
import org.objectweb.asm.tree.JumpInsnNode;
import org.objectweb.asm.tree.LabelNode;
import org.objectweb.asm.tree.MethodInsnNode;
import org.objectweb.asm.tree.MethodNode;
import org.objectweb.asm.tree.VarInsnNode;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

@Slf4j
public class Js5HookPatch implements Patch {

    private static final Path JS5_OVERLAYS = Paths.get("js5");

    public static byte[] fetchGroupInnerHook(String groupName, String fileName) {

        Path file = JS5_OVERLAYS.resolve(groupName).resolve(fileName);
        LOGGER.debug("Looking for overlay={}", file);
        if (Files.exists(file)) {
            try {
                LOGGER.info("Loading overlay={}", file);
                return Files.readAllBytes(file);
            } catch (IOException e) {
                LOGGER.warn("Failed to load js5 overlay", e);
            }
        }
        return null;
    }

    @Override
    public boolean applicable(String className) {
        return className.equals("db");
    }

    @Override
    public byte[] apply(byte[] classData) {
        ClassNode classNode = byteArrayToClassNode(classData);

        for (MethodNode method : classNode.methods) {

            if (isFetchGroupInner(method)) {
                copyMethod(classNode, method);
                patchFetchGroupInner(classNode, method);
                break;
            }
        }
        return classNodeToByteArray(classNode);
    }

    private boolean isFetchGroupInner(MethodNode methodNode) {
        return methodNode.name.equals("a") && methodNode.desc.equals(
                Type.getMethodDescriptor(
                        Type.getType(byte[].class),
                        Type.getType(int.class),
                        Type.getType(String.class),
                        Type.getType(String.class)
                )
        );
    }

    private void patchFetchGroupInner(ClassNode owner, MethodNode methodNode) {
        methodNode.tryCatchBlocks.clear();
        InsnList instructions = methodNode.instructions = new InsnList();

        instructions.add(new VarInsnNode(Opcodes.ALOAD, 3));
        instructions.add(new VarInsnNode(Opcodes.ALOAD, 2));
        instructions.add(new MethodInsnNode(Opcodes.INVOKESTATIC,
                Type.getType(Js5HookPatch.class).getInternalName(),
                "fetchGroupInnerHook",
                Type.getMethodDescriptor(
                        Type.getType(byte[].class),
                        Type.getType(String.class),
                        Type.getType(String.class)
                )
        ));
        instructions.add(new VarInsnNode(Opcodes.ASTORE, 4));

        LabelNode originalCallLabel = new LabelNode();

        instructions.add(new VarInsnNode(Opcodes.ALOAD, 4));
        instructions.add(new JumpInsnNode(Opcodes.IFNULL, originalCallLabel));
        instructions.add(new VarInsnNode(Opcodes.ALOAD, 4));
        instructions.add(new InsnNode(Opcodes.ARETURN));

        instructions.add(originalCallLabel);
        instructions.add(new VarInsnNode(Opcodes.ALOAD, 0));
        instructions.add(new VarInsnNode(Opcodes.ILOAD, 1));
        instructions.add(new VarInsnNode(Opcodes.ALOAD, 2));
        instructions.add(new VarInsnNode(Opcodes.ALOAD, 3));
        instructions.add(new MethodInsnNode(Opcodes.INVOKEVIRTUAL, owner.name, synthetizeName(methodNode.name), methodNode.desc));
        instructions.add(new InsnNode(Opcodes.ARETURN));
    }

    private void copyMethod(ClassNode classNode, MethodNode methodNode) {
        MethodNode copy = new MethodNode(methodNode.access, synthetizeName(methodNode.name), methodNode.desc, methodNode.signature, methodNode.exceptions.toArray(new String[0]));
        copy.instructions = methodNode.instructions;
        classNode.methods.add(copy);
    }

    private String synthetizeName(String originalName) {
        return "aorb$" + originalName;
    }
}
