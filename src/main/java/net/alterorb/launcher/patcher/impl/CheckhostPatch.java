package net.alterorb.launcher.patcher.impl;

import net.alterorb.launcher.patcher.Patch;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.tree.ClassNode;
import org.objectweb.asm.tree.InsnList;
import org.objectweb.asm.tree.InsnNode;
import org.objectweb.asm.tree.MethodNode;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Objects;

/**
 * Patches the gameshell's checkhost method so it accepts any domain instead of restricting to localhost/jagex domains.
 */
public class CheckhostPatch implements Patch {

    private static final Logger LOGGER = LoggerFactory.getLogger(CheckhostPatch.class);

    private String gameshellClass;
    private String checkhostMethod;
    private String checkhostMethodDesc;

    @Override
    public boolean applicable(String className) {
        return Objects.equals(className, gameshellClass);
    }

    @Override
    public byte[] apply(byte[] classData) {
        ClassNode classNode = byteArrayToClassNode(classData);

        for (MethodNode methodNode : classNode.methods) {

            if (Objects.equals(methodNode.name, checkhostMethod) && Objects.equals(methodNode.desc, checkhostMethodDesc)) {
                InsnList insnList = new InsnList();
                insnList.add(new InsnNode(Opcodes.ICONST_1));
                insnList.add(new InsnNode(Opcodes.IRETURN));

                methodNode.instructions = insnList;
                methodNode.tryCatchBlocks.clear();
                LOGGER.debug("Patched checkhost method");
                break;
            }
        }
        return classNodeToByteArray(classNode);
    }
}
