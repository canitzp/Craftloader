package craftloader.transformer.minecraft;

import com.google.common.collect.Lists;
import craftloader.Mod;
import craftloader.transformer.TransformerBase;
import net.minecraft.client.main.Main;
import net.minecraft.launchwrapper.Launch;
import org.objectweb.asm.ClassReader;
import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.Type;
import org.objectweb.asm.tree.*;

import java.io.File;
import java.util.Arrays;

/**
 * @author canitzp
 */
public class PhaseTransformer extends TransformerBase{

    public PhaseTransformer(){
        super("net/minecraft/client/Minecraft");
    }

    @Override
    public byte[] transform(ClassNode node, ClassReader reader){
        MethodNode method = searchMethod(node, new Type[0], Type.VOID_TYPE, Lists.newArrayList("LWJGL Version: {}"), Lists.newArrayList("org/lwjgl/LWJGLException"));
        if(method != null){
            InsnList oldList = method.instructions;
            InsnList newList = new InsnList();
            for(AbstractInsnNode insnNode : oldList.toArray()){
                if(insnNode instanceof MethodInsnNode && insnNode.getOpcode() == Opcodes.INVOKEVIRTUAL){
                    MethodInsnNode methodInsnNode = (MethodInsnNode) insnNode;
                    if(methodInsnNode.owner.equals(node.name)){
                        Type ret = Type.getReturnType(methodInsnNode.desc);
                        if(ret.equals(Type.VOID_TYPE)){
                            if(Arrays.equals(Type.getArgumentTypes(methodInsnNode.desc), new Type[0])){
                                if(getNextImportantOpcode(methodInsnNode) == Opcodes.NEW){
                                    //addStaticMethodCall(newList, node, method, "craftloader/transformer/TweakHook", "preInit");
                                    newList.add(insnNode);
                                } else {
                                    newList.add(insnNode);
                                }
                            } else {
                                newList.add(insnNode);
                            }
                        } else {
                            newList.add(insnNode);
                        }
                    } else {
                        newList.add(insnNode);
                    }
                } else {
                    newList.add(insnNode);
                }
            }
            method.instructions = newList;
        }
        ClassWriter writer = new ClassWriter(ClassWriter.COMPUTE_FRAMES | ClassWriter.COMPUTE_MAXS);
        node.accept(writer);
        dump(writer.toByteArray(),new File(Launch.minecraftHome, this.mappedClassName.replace("/", "_")));
        return writer.toByteArray();
    }

}
