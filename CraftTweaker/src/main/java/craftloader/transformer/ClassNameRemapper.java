package craftloader.transformer;

import craftloader.TweakClass;
import craftloader.remap.MCRemapper;
import net.minecraft.launchwrapper.IClassNameTransformer;
import net.minecraft.launchwrapper.IClassTransformer;
import org.objectweb.asm.ClassReader;
import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.commons.RemappingClassAdapter;

import java.util.HashMap;
import java.util.Map;

/**
 * @author canitzp
 */
public class ClassNameRemapper implements IClassNameTransformer, IClassTransformer{

    private Map<String, String> obfNames = new HashMap<>(TweakClass.obfuscatingNames);

    @Override
    public String unmapClassName(String name){
        //System.out.println("Ask: " + name);
        if(obfNames.containsValue(name.replace(".", "/"))){
            //System.out.println("Contains: " + name);
            for(Map.Entry<String, String> entry : obfNames.entrySet()){
                if(entry.getValue().equals(name.replace(".", "/"))){
                    return entry.getKey().replace("/", ".");
                }
            }
        } else if(name.contains("$")){
            String[] ary = name.split("\\$");
            String typeName = unmapClassName(ary[0]);
            for(int i = 1; i < ary.length; i++){
                typeName += "$" + ary[i];
            }
            //System.out.println("Inner: " + typeName);
            return typeName;
        }
        return name;
    }

    @Override
    public String remapClassName(String name){
        if(obfNames.containsKey(name)){
            return obfNames.get(name).replace("/", ".");
        }
        return name;
    }

    @Override
    public byte[] transform(String name, String transformedName, byte[] basicClass){
        if(basicClass != null){
            ClassWriter writer = new ClassWriter(ClassWriter.COMPUTE_FRAMES | ClassWriter.COMPUTE_MAXS);
            ClassReader reader = new ClassReader(basicClass);
            MCRemapper.classNames = new HashMap<>(obfNames);
            MCRemapper.remapUnknown = false;
            reader.accept(new RemappingClassAdapter(writer, new MCRemapper()), ClassReader.EXPAND_FRAMES);
            return writer.toByteArray();
        }
        return null;
    }
}
