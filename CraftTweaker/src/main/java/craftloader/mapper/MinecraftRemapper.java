package craftloader.mapper;

import craftloader.remap.ClassRemapper;
import craftloader.remap.MCRemapper;
import craftloader.remap.Remap;
import org.objectweb.asm.ClassReader;
import org.objectweb.asm.commons.RemappingClassAdapter;
import org.objectweb.asm.tree.ClassNode;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.*;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;

/**
 * @author canitzp
 */
public class MinecraftRemapper{

    public static boolean DEBUG = false;

    public static final List<Remap.ClassCollection> collectionList = new ArrayList<>();
    public static final Map<String, byte[]> uncollectedFiles = new HashMap<>();

    public static void remapRuntime(){
        for(URL url : ((URLClassLoader) MinecraftRemapper.class.getClassLoader()).getURLs()){
            String[] split = url.getFile().split("/");
            if((split[split.length - 3]).equals("versions")){
                try{
                    remapMinecraft(new JarFile(url.getFile()));
                } catch(IOException | InstantiationException | IllegalAccessException e){
                    e.printStackTrace();
                }
            }
        }
    }

    public static void remapDevEnv(){
        try{
            remapMinecraft(new JarFile(new File("P:\\Craftloader\\library\\client.jar")));
            Remap.createOutputJar(new File("P:\\Craftloader\\library\\client_remapped.jar"), collectionList, uncollectedFiles);
        } catch(IOException | InstantiationException | IllegalAccessException e){
            e.printStackTrace();
        }
    }

    public static void remapMinecraft(JarFile jar) throws IOException, IllegalAccessException, InstantiationException{
        Enumeration<JarEntry> e = jar.entries();
        while(e.hasMoreElements()){
            JarEntry entry = e.nextElement();
            if(entry.getName().endsWith(".class")){
                Remap.ClassCollection collection = getClassCollection(jar.getInputStream(entry));
                collectionList.add(collection);
                String mapped = ClassRemapper.remapClassName(collection.classNode);
                if(mapped != null){
                    collection.mappedName = mapped;
                    MCRemapper.classNames.put(collection.classNode.name, mapped);
                    MCRemapper.classCollections.put(collection.classNode.name, collection);
                }
            } else {
                uncollectedFiles.put(entry.getName(), Remap.getFileFromZip(entry, jar));
            }
        }
        for(Remap.ClassCollection classCollection : collectionList){
            ClassNode cn = new ClassNode();
            classCollection.classReader.accept(new RemappingClassAdapter(cn, new MCRemapper()), ClassReader.EXPAND_FRAMES);
            classCollection.classNode = cn;
            classCollection.mappedName = cn.name;
        }

        if(DEBUG){
            for(Remap.ClassCollection collection : collectionList){
                Remap.dumpClassWrite(collection.classNode);
            }
        }
    }

    public static Remap.ClassCollection getClassCollection(InputStream stream) throws IOException{
        ClassNode cn = new ClassNode();
        ClassReader cr = new ClassReader(stream);
        cr.accept(cn, 0);
        return new Remap.ClassCollection(cn, cr, cn.name, cr.getClassName().contains("/") || cr.getClassName().contains(".") ? cr.getClassName() : null);
    }

}
