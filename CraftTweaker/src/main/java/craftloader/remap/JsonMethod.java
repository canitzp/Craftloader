package craftloader.remap;

import com.google.gson.Gson;
import craftloader.TweakClass;

import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * @author canitzp
 */
public class JsonMethod {

    public static InputStream methodMappingsStream = TweakClass.class.getResourceAsStream("/mappings/MethodMapping.json");

    private InputStream methodMappings;

    public JsonMethod(Remap.ClassCollection collection){
        this.methodMappings = TweakClass.class.getResourceAsStream("/mappings/" + collection.mappedName + ".json");
    }

    public static DataList getMethodMappings(Remap.ClassCollection collection){
        return new JsonMethod(collection).run();
    }

    public DataList run(){
        try{
            return new Gson().fromJson(new InputStreamReader(this.methodMappings), DataList.class);
        } catch(Exception e){
            JsonMethod.DataList data = new JsonMethod.DataList();
            JsonMethod.Data datas = new JsonMethod.Data();
            datas.name = "getItemBlock";
            datas.desc = "(Lnet/minecraft/item/Item;)Lnet/minecraft/block/Block;";
            datas.strings = new ArrayList<>();
            data.mappings = Collections.singletonList(datas);
            data.classname = "net/minecraft/block/Block";
            return data;
        }
    }

    public static class DataList{
        public String classname;
        public List<Data> mappings;
    }

    public static class Data{
        public String name;
        public String desc;
        public List<String> strings;
    }

}
