package craftloader;

import craftloader.remap.Remap;
import craftloader.transformer.ClassNameRemapper;
import craftloader.transformer.minecraft.PhaseTransformer;
import net.minecraft.launchwrapper.ITweaker;
import net.minecraft.launchwrapper.Launch;
import net.minecraft.launchwrapper.LaunchClassLoader;
import net.minecraft.launchwrapper.LogWrapper;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.*;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;

/**
 * @author canitzp
 */
@SuppressWarnings({"unchecked", "ConstantConditions", "ResultOfMethodCallIgnored"})
public class TweakClass implements ITweaker {

    public static Map<String, String> obfuscatingNames = new HashMap<>();

    private List<String> args = new ArrayList<>();
    private Map<String, String> tweaker = new HashMap<>();

    public TweakClass(){
        Launch.classLoader.addClassLoaderExclusion("org.objectweb.asm.");
    }

    @Override
    public void acceptOptions(List<String> args, File gameDir, File assetsDir, String profile){

        for(URL url : Launch.classLoader.getSources()){
            String[] split = url.getFile().split("/");
            if((split[split.length - 3]).equals("versions")){
                try{
                    JarFile jar = new JarFile(url.getFile());
                    obfuscatingNames = Remap.remapClassNames(jar);
                    break;
                } catch(IOException e){
                    e.printStackTrace();
                }
            }
        }

        this.args = new ArrayList<>(args);

        // If they specified a custom version name, pass it to Minecraft
        if(profile != null){
            this.args.add("--version");
            this.args.add("Craftloader");
        }

        // If they specified an assets dir, pass it to Minecraft
        if(assetsDir != null){
            this.args.add("--assetsDir");
            this.args.add(assetsDir.getPath());
        }


        File mods = new File(gameDir, "craftloader");
        mods.mkdirs();
        for(File file : mods.listFiles((dir, name) -> name.endsWith(".jar"))){
            try{
                Launch.classLoader.addURL(file.toURI().toURL());
                JarFile jar = new JarFile(file);
                Enumeration<JarEntry> entries = jar.entries();
                while(entries.hasMoreElements()){
                    JarEntry entry = entries.nextElement();
                    if(entry.getName().endsWith(".class") && !entry.getName().contains("$")){
                        Class clazz = Class.forName(entry.getName().substring(0, entry.getName().length() - 6).replace("/", "."), false, Launch.classLoader);
                        if(clazz.isAnnotationPresent(Mod.class)){
                            ModManagement.addMod(new ModData((Mod) clazz.getAnnotation(Mod.class), clazz));
                        }
                        if(clazz.isAnnotationPresent(Mod.Tweak.class)){
                            tweaker.put(((Mod.Tweak)clazz.getAnnotation(Mod.Tweak.class)).tweakid(), clazz.getName());
                        }
                    }
                }
            } catch(Exception e){
                e.printStackTrace();
            }
        }
        //tweaker.put("CraftloaderPhaseTransformer", PhaseTransformer.class.getName());
        tweaker.put("ClassNameTransformer", ClassNameRemapper.class.getName());
    }

    @Override
    public void injectIntoClassLoader(LaunchClassLoader classLoader) {
        for(Map.Entry<String, String> tweak : tweaker.entrySet()){
            try{
                Class.forName(tweak.getValue(), true, classLoader);
                classLoader.addTransformerExclusion("craftloader.transformer.");
                classLoader.registerTransformer(tweak.getValue());
                //LogWrapper.info("Registering Tweak class '" + tweak.getKey() + "' ('" + tweak.getValue() + "')");
            } catch(ClassNotFoundException e){
                e.printStackTrace();
            }
        }
    }

    @Override
    public String getLaunchTarget() {
        return "net.minecraft.client.main.Main";
    }

    @Override
    public String[] getLaunchArguments() {
        return this.args.toArray(new String[args.size()]);
    }

}
