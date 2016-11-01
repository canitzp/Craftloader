package craftloader.transformer;

import net.minecraft.client.Minecraft;
import net.minecraft.launchwrapper.Launch;

/**
 * @author canitzp
 */
public class TweakHook{

    public static void preInit(Minecraft minecraft){
        System.out.println("Start Minecraft Test");
        System.out.println(TweakHook.class.getClassLoader().getClass().getName());
        try{
            Class.forName("net.minecraft.client.Minecraft");
        } catch(ClassNotFoundException e){
            e.printStackTrace();
            try{
                Class.forName("ben");
            } catch(ClassNotFoundException e1){
                e1.printStackTrace();
            }
        }
        try {
            Launch.classLoader.findClass("net.minecraft.client.Minecraft");
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        }
    }

}
