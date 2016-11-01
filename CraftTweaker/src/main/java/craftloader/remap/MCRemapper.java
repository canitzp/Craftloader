package craftloader.remap;

import org.apache.commons.lang3.math.NumberUtils;
import org.objectweb.asm.Type;
import org.objectweb.asm.commons.Remapper;

import java.util.HashMap;
import java.util.Map;

/**
 * @author canitzp
 */
public class MCRemapper extends Remapper{

    /**
     * Map<obfuscated class name, real class name>
     */
    public static HashMap<String, String> classNames = new HashMap<>();

    public static Map<String, Remap.ClassCollection> classCollections = new HashMap<>();

    public static boolean remapUnknown = true;

    @Override
    public String map(String typeName){
        if(typeName.contains("/")){
            return super.map(typeName);
        }

        //assigend classes
        if(classNames.containsKey(typeName)){
            return classNames.get(typeName);
        }

        /*
        //child classes
        if(classCollections.containsKey(typeName)){
            ClassNode node = classCollections.get(typeName).classNode;
            if(classNames.containsKey(node.superName)){
                String superClassName = classNames.get(node.superName);
                if(JsonClass.shouldMoveChildClasses(superClassName)){
                    return JsonClass.getChildClassLocation(superClassName) + typeName;
                }
            }
        }
        */

        //inner classes
        if(typeName.contains("$")){
            String[] names = typeName.split("\\$");
            if(classNames.containsKey(names[0])){
                names[0] = classNames.get(names[0]);
            }
            typeName = names[0];
            for(int i = 1; i < names.length; i++){
                String inner = names[i];
                if(remapUnknown && NumberUtils.isNumber(inner)){
                    inner = "inner_" + inner;
                }
                typeName += "$" + inner;
            }
            return typeName;
        }

        //everything else (obfuscated)
        return remapUnknown ? "net/minecraft/class_" + super.map(typeName) : super.map(typeName);
    }

    @Override
    public String mapMethodName(String owner, String name, String desc) {
        if(classNames.containsKey(owner) && classCollections.containsKey(owner)){
            Remap.ClassCollection collection = classCollections.get(owner);
            desc = getMappedDesc(desc);
            JsonMethod.DataList dataList = JsonMethod.getMethodMappings(collection);
            if(dataList.classname.equals(collection.mappedName)){
                for(JsonMethod.Data data : dataList.mappings) {
                    if (getMappedDesc(desc).equals(data.desc)) {
                        return data.name;
                    }
                }
            }
        }
        return super.mapMethodName(owner, name, desc);
    }

    private String getMappedDesc(String desc){
        Type[] types = Type.getArgumentTypes(desc);
        String descs = "(";
        for(Type type : types){
            if(type.getDescriptor().length() == 1 || type.getDescriptor().contains("/")){
                descs += type.getDescriptor();
            } else {
                String s = type.getDescriptor().substring(1).replace(";", "");
                if(classNames.containsKey(s)){
                    descs += "L" + classNames.get(s) + ";";
                }
            }
        }
        descs += ")";
        if(desc.split("\\)").length == 2){
            Type returnDesc = Type.getReturnType(desc);
            if(returnDesc.getDescriptor().length() == 1 || returnDesc.getDescriptor().contains("/")){
                descs += returnDesc.getDescriptor();
            } else {
                String s = returnDesc.getDescriptor().substring(1).replace(";", "");
                if(classNames.containsKey(s)){
                    descs += "L" + classNames.get(s) + ";";
                }
            }
        }
        return descs;
    }
}
