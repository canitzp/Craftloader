package craftloader.remap;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import org.apache.commons.lang3.tuple.Pair;
import org.objectweb.asm.Type;
import org.objectweb.asm.tree.*;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * @author canitzp
 */
public class ClassRemapper{

    public static Map<String, List<String>> stringsToFound = Maps.newHashMap();

    static {
        for(Pair<String, String[]> pair : JsonClass.getAllStringMappings()){
            stringsToFound.put(pair.getKey(), Lists.newArrayList(pair.getValue()));
        }
    }

    public static String remapClassName(ClassNode node){
        List<String> stringsInClass = new ArrayList<>();
        for(MethodNode method : node.methods){
            stringsInClass.addAll(searchStrings(method.instructions));
        }
        for(Map.Entry<String, List<String>> entry : stringsToFound.entrySet()){
            int i = 0;
            for(String s : entry.getValue()){
                for(String s1 : stringsInClass){
                    if(s.equals(s1)){
                        i++;
                    }
                }
            }
            if(i == entry.getValue().size()){
                return entry.getKey();
            }
        }
        return null;
    }

    public static List<String> searchStrings(InsnList insn){
        List<String> found = new ArrayList<>();
        for(AbstractInsnNode node : insn.toArray()){
            if(node instanceof LdcInsnNode){
                Object ldc = ((LdcInsnNode) node).cst;
                if(ldc instanceof String){
                    found.add((String) ldc);
                }
            }
        }
        return found;
    }

    public static int getClassValue(ClassNode cn){
        int i = 0;
        for(MethodNode method : cn.methods){
            i += getValue(method.instructions);
        }
        for(FieldNode field : cn.fields){
            System.out.println(Type.getType(field.desc));
        }
        return i;
    }

    private static long getValue(InsnList insn){
        long i = 0;
        for(AbstractInsnNode node : insn.toArray()){
            if(node instanceof LdcInsnNode){
                Object ldc = ((LdcInsnNode) node).cst;
                if(ldc instanceof Integer){
                    i += (Integer) ldc;
                } else if(ldc instanceof Float){
                    i += (Float) ldc;
                } else if(ldc instanceof Long){
                    i += (Long) ldc;
                } else if(ldc instanceof Double){
                    i += (Double) ldc;
                }
            }
        }
        return i;
    }

}
