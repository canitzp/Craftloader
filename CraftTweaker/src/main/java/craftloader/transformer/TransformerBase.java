package craftloader.transformer;

import com.google.common.collect.Lists;
import craftloader.TweakClass;
import craftloader.remap.ClassRemapper;
import net.minecraft.launchwrapper.IClassTransformer;
import org.objectweb.asm.ClassReader;
import org.objectweb.asm.Type;
import org.objectweb.asm.tree.*;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.*;

import static org.objectweb.asm.Opcodes.*;

/**
 * @author canitzp
 */
public abstract class TransformerBase implements IClassTransformer{

    private static final Map<String, String> names = new HashMap<>(TweakClass.obfuscatingNames);

    private final Map<String, ClassNode> cachedClassNodes = new HashMap<>();

    public String mappedClassName;

    public TransformerBase(String mappedClassName){
        this.mappedClassName = mappedClassName;
    }

    public void setNames(Map<String, String> map){
        names.clear();
        names.putAll(map);
    }

    @Override
    public final byte[] transform(String name, String transformedName, byte[] basicClass){
        if(names.containsKey(name) || mappedClassName.equals(name)){
            if(mappedClassName.equals(names.get(name)) || mappedClassName.equals(name)){
                ClassReader reader = new ClassReader(basicClass);
                ClassNode node = new ClassNode();
                reader.accept(node, ClassReader.EXPAND_FRAMES);
                return transform(node, reader);
            }
        }
        return basicClass;
    }

    public abstract byte[] transform(ClassNode node, ClassReader reader);

    protected String getMappedName(String obfName){
        if(names.containsKey(obfName)){
            return names.get(obfName);
        }
        return null;
    }

    protected String getObfuscatedName(String mappedName){
        if(names.containsValue(mappedName)){
            for(Map.Entry<String, String> entry : names.entrySet()){
                if(entry.getValue().equals(mappedName)){
                    return entry.getKey();
                }
            }
        }
        return null;
    }

    protected ClassNode getNodeFromMapping(String mappedName){
        return getNode(getObfuscatedName(mappedName));
    }

    protected ClassNode getNode(String obfName){
        if(obfName != null){
            if(cachedClassNodes.containsKey(obfName)){
                return cachedClassNodes.get(obfName);
            }
            try{
                ClassReader reader = new ClassReader(TweakClass.class.getClassLoader().getResourceAsStream(obfName + ".class"));
                ClassNode node = new ClassNode();
                reader.accept(node, 0);
                cachedClassNodes.put(obfName, node);
                return node;
            } catch(IOException e){
                e.printStackTrace();
            }
        }
        return null;
    }

    protected MethodNode searchMethod(ClassNode node, Type[] params, Type returnType, List<String> containsStrings, List<String> exceptions){
        List<MethodNode> possibleMethods = new ArrayList<>();
        for(MethodNode method : node.methods){
            Type[] args = Type.getArgumentTypes(method.desc);
            if(args.length == params.length && Arrays.equals(args, params)){
                Type ret = Type.getReturnType(method.desc);
                if(ret.equals(returnType)){
                    possibleMethods.add(method);
                }
            }
        }
        if(possibleMethods.size() > 1){
            for(MethodNode method : possibleMethods){
                InsnList insn = method.instructions;
                List<String> found = ClassRemapper.searchStrings(insn);
                int i = 0;
                for(String contains : containsStrings){
                    if(found.contains(contains)){
                        i++;
                    }
                }
                if(i == containsStrings.size()){
                    return method;
                }
            }
        } else if(possibleMethods.size() == 1){
            return possibleMethods.get(0);
        }
        return null;
    }

    private List<Integer> importantOpcodes = Lists.newArrayList(NEW, INVOKESPECIAL, PUTFIELD, GETFIELD, INVOKEVIRTUAL, INVOKESTATIC, LDC);
    public int getNextImportantOpcode(AbstractInsnNode current){
        int opcode = -1;
        AbstractInsnNode node = current.getNext();
        while(opcode == -1){
            if(!importantOpcodes.contains(node.getOpcode())){
                node = node.getNext();
            } else {
                opcode = node.getOpcode();
            }
        }
        return opcode;
    }

    public void addStaticMethodCall(InsnList list, ClassNode node, MethodNode method, String clazz, String staticMethodName){
        Type[] methodParams = Type.getArgumentTypes(method.desc);
        list.add(new VarInsnNode(ALOAD, 0));
        /*
        for(int i = 1; i >= methodParams.length; i++){
            if(methodParams[i-1].getSort() == Type.OBJECT){
                list.add(new VarInsnNode(ALOAD, i));
            }
        }
        */
        String desc = "(L" + node.name + ";)V";
        System.out.println(desc);
        String[] descs = desc.split("\\)");
        list.add(new MethodInsnNode(INVOKESTATIC, clazz, staticMethodName, descs[0] + ")V", false));
    }

    protected void dump(byte[] data, File classOutput){
        try{
            FileOutputStream fos = new FileOutputStream(classOutput + ".class");
            fos.write(data);
            fos.close();
        } catch(Exception e){
            e.printStackTrace();
        }

    }

}
