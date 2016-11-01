package de.canitzp.craftmine;

import net.minecraft.launchwrapper.ITweaker;
import net.minecraft.launchwrapper.Launch;
import net.minecraft.launchwrapper.LaunchClassLoader;
import org.apache.commons.io.FileUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.*;
import java.lang.reflect.InvocationTargetException;
import java.net.URL;
import java.util.*;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;

/**
 * @author canitzp
 */
public class Craftmine implements ITweaker{

    public static final Logger logger = LogManager.getLogger("Craftmine");
    public static Map<String, String> obfuscatingNames = new HashMap<>();

    private List<String> args = new ArrayList<>();
    private List<Class> tweaker = new ArrayList<>();

    public Craftmine(){
        for(URL url : Launch.classLoader.getSources()){
            String[] split = url.getFile().split("/");
            if((split[split.length - 3]).equals("versions")){
                try{
                    JarFile jar = new JarFile(url.getFile());
                    //obfuscatingNames = Remap.remapClassNames(jar);
                    break;
                } catch(IOException e){
                    e.printStackTrace();
                }
            }
        }
    }

    @Override
    public void acceptOptions(List<String> args, File gameDir, File assetsDir, String profile) {
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

        try {
            analizeMods(gameDir);
        } catch (IOException | ClassNotFoundException e) {
            e.printStackTrace();
        }
        for(AddonData addonData : ModManagement.loadedAddons){
            if(addonData.isStartupAddon){
                System.out.println(addonData.mainClass.getName());
                try {
                    Class c = Class.forName(addonData.mainClass.getName(), true, Launch.classLoader);
                    c.newInstance();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }
    }

    @Override
    public void injectIntoClassLoader(LaunchClassLoader classLoader) {
        for(Class tweak : tweaker){
            classLoader.registerTransformer(tweak.getName());
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

    @SuppressWarnings({"ConstantConditions", "ReflectionForUnavailableAnnotation", "unchecked"})
    public void analizeMods(File gameDir) throws IOException, ClassNotFoundException {
        // ModMainClass, mcmod.info
        HashMap<Class, File> legitMods = new HashMap<>();
        // ModLoader addon
        List<Class> loaderAddon = new ArrayList<>();

        List<JarFile> modsToAnalyze = new ArrayList<>();
        //Analyze the modloader addons like the CraftTweaker
        File loaderExtensions = new File(gameDir, "craftextensions");
        loaderExtensions.mkdirs();
        for(File file : loaderExtensions.listFiles((dir, name) -> name.endsWith(".jar"))){
            Launch.classLoader.addURL(file.toURI().toURL());
            modsToAnalyze.add(new JarFile(file));
        }
        //Analyze the mods
        File mods = new File(gameDir, "mods");
        mods.mkdirs();
        for(File file : mods.listFiles((dir, name) -> name.endsWith(".jar"))){
            Launch.classLoader.addURL(file.toURI().toURL());
            modsToAnalyze.add(new JarFile(file));
        }

        for(JarFile file : modsToAnalyze){
            boolean flag = false;
            File modInfo = null;
            Class mainModClass = null;
            Enumeration<JarEntry> entries = file.entries();
            while(entries.hasMoreElements()) {
                JarEntry entry = entries.nextElement();
                if(entry.getName().endsWith(".class")){
                    Class clazz = Class.forName(entry.getName().substring(0, entry.getName().length() - 6).replace("/", "."), false, Launch.classLoader);
                    if(clazz.isAnnotationPresent(Mod.class)){
                        if(mainModClass != null){
                            logger.error("Found a second Main mod class! Overriding the old one! Old: " + mainModClass.getName() + "  New: " + clazz.getName());
                        }
                        mainModClass = clazz;
                    } else if(clazz.isAnnotationPresent(CraftAddon.class)){
                        flag = true;
                        logger.info("Found a Craftloader extension. " + clazz.getName());
                        loaderAddon.add(clazz);
                    } else if(clazz.isAnnotationPresent(CraftTweaker.class) && clazz.isAssignableFrom(ITweaker.class)){
                        flag = true;
                        tweaker.add(clazz);
                    }
                } else if(entry.getName().endsWith(".info")){
                    FileUtils.copyInputStreamToFile(file.getInputStream(entry), modInfo);
                }
            }
            if(!flag && mainModClass != null){
                if(modInfo != null){
                    legitMods.put(mainModClass, modInfo);
                } else {
                    logger.error("Found a Mod without a mcmod.info file! The mod didn't get loaded! " + file.getName() + "  " + mainModClass.getName());
                }
            } else {
                logger.info("Found a non mod java file. Adding it anyway. File name: " + file.getName());
            }

            for(Map.Entry<Class, File> mod : legitMods.entrySet()){
                ModManagement.registerMod(mod.getKey(), mod.getValue());
            }
            for(Class clazz : loaderAddon){
                ModManagement.registerAddon(new AddonData(clazz));
            }
        }
    }

}
