package craftloader;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author canitzp
 */
@SuppressWarnings("unchecked")
public class ModManagement{

    public static List<String> loadedMods = new ArrayList<>();
    public static Map<String, ModData> modInstances = new HashMap<>();

    public static void addMod(ModData data){
        try{
            Object modInstance = data.getModClass().getConstructor(ModData.class).newInstance(data);
            data.setModInstance(modInstance);
            loadedMods.add(data.getModid());
            modInstances.put(data.getModid(), data);
        } catch(Exception e){
            e.printStackTrace();
        }
    }

    public static void postLoadingEvent(LoadingStates state){
        for(ModData data : modInstances.values()){
            Class clazz = data.getModClass();
            for(Method method : clazz.getDeclaredMethods()){
                if(method.isAnnotationPresent(Mod.Loading.class)){
                    if(state.equals(method.getAnnotation(Mod.Loading.class).state())){
                        if(method.getParameterCount() == 1 && method.getParameterTypes()[0].equals(ModData.class)){
                            try{
                                method.invoke(data.getModInstance(), data);
                                return;
                            } catch(IllegalAccessException | InvocationTargetException e){
                                e.printStackTrace();
                            }
                        }
                    }
                }
            }
        }
    }

    public enum LoadingStates{
        PRE,
        INIT,
        POST
    }

}
