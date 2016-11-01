package craftloader.remap;

import com.google.gson.Gson;
import craftloader.TweakClass;
import org.apache.commons.lang3.tuple.Pair;

import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.*;

/**
 * @author canitzp
 */
public class JsonClass {

    public static InputStream classMappingsStream = TweakClass.class.getResourceAsStream("/mappings/ClassMappings.json");

    public static Map<String, String> childClassLocations = new HashMap<>();

    public static List<Pair<String, String[]>> getAllStringMappings(){
        List<Pair<String, String[]>> list = new ArrayList<>();
        Data data = readClassMappings();
        for(Datas datas : data.mappings){
            if(datas.identifierInt == 0 && datas.identifierStrings.length > 0 && datas.name.length() > 0){
                list.add(Pair.of(datas.name, datas.identifierStrings));
                if(datas.moveChildClasses){
                    childClassLocations.put(datas.name, datas.childClassLocation);
                }
            }
        }
        return list;
    }

    public static String getChildClassLocation(String name){
        return childClassLocations.get(name);
    }

    public static boolean shouldMoveChildClasses(String name){
        return childClassLocations.containsKey(name);
    }

    public static Data readClassMappings(){
        return new JsonClass().getData();
    }

    public Data getData(){
        return new Gson().fromJson(new InputStreamReader(classMappingsStream), Data.class);
    }

    /**
     * name: The new name of the obfuscated object
     * identifierType: 0=String, 1=integer, 2=both
     */
    public class Data{
        public List<Datas> mappings;

        @Override
        public String toString(){
            return this.mappings != null ? this.mappings.toString() : super.toString();
        }
    }

    public class Datas{
        public String name;
        public int identifierType;
        public String[] identifierStrings;
        public int identifierInt;
        public boolean moveChildClasses = false;
        public String childClassLocation = null;
        public String childOf = null;

        @Override
        public String toString(){
            return "Name: " + this.name + " IDT: " + this.identifierType + " IDStrings: " + Arrays.toString(this.identifierStrings) + " IDI: " + this.identifierInt;
        }
    }

}
