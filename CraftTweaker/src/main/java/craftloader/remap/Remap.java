package craftloader.remap;

import craftloader.mapper.MinecraftRemapper;
import org.apache.commons.io.FileUtils;
import org.objectweb.asm.ClassReader;
import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.tree.ClassNode;

import javax.swing.*;
import java.io.*;
import java.util.*;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;
import java.util.jar.JarOutputStream;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

/**
 * @author canitzp
 */
public class Remap{

    public static File mappingsDir;

    public JarFile jarFileToDeobfuscate;
    public File jarFileOutput;
    public static File reobfuscatedDump;

    public List<ClassCollection> finishedClasses = new ArrayList<>();
    private Map<String, byte[]> uncollectedFiles = new HashMap<>();

    public Remap(File jarFile, File dumpFolder){
        try {
            mappingsDir = new File(dumpFolder.getParentFile(), "mappings");
            this.jarFileToDeobfuscate = new JarFile(jarFile);
            this.jarFileOutput = new File(jarFile.getParentFile(), "minecraft_remapped.jar");
            this.reobfuscatedDump = dumpFolder;
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void runDeobfuscation(){
        runDeobfuscation(null);
    }

    public void runDeobfuscation(JProgressBar bar){
        /*
        if(bar != null){
            bar.setBorderPainted(true);
            bar.setString("Analize Jar");
            bar.setStringPainted(true);
        }
        List<ClassCollection> classes = createNodes(this.jarFileToDeobfuscate, getClassEntries());
        if(bar != null){
            bar.setValue(1);
            bar.setString("Remap Jar - 1");
        }
        List<String> obfs = new ArrayList<>();
        classes = remapClasses(classes);
        if(bar != null){
            bar.setValue(2);
            bar.setString("Remap Jar - 2");
        }
        for(ClassCollection collection : classes){
            obfs.add(collection.obfuscatedName);
            if(collection.mappedName != null){
                MCRemapper.classNames.put(collection.obfuscatedName, collection.mappedName);
            }
            MCRemapper.classCollections.put(collection.obfuscatedName, collection);
        }
        List<ClassCollection> toRemove = new ArrayList<>();
        for(ClassCollection collection : classes){
            ClassNode newNode = new ClassNode();
            collection.classReader.accept(new RemappingClassAdapter(newNode, new MCRemapper()), ClassReader.EXPAND_FRAMES);
            collection.classNode = newNode;
        }
        if(bar != null){
            bar.setValue(3);
            bar.setString("Dump Classes");
        }
        for(ClassCollection collection : classes){
            if(!collection.classNode.name.contains("/")){
                toRemove.add(collection);
            }
        }
        classes.removeAll(toRemove);
        for(ClassCollection classCollection : classes){
            dumpClassWrite(classCollection.classNode);
        }
        finishedClasses.addAll(classes);
        if(bar != null){
            bar.setValue(4);
            bar.setString("Finished");
        }
        */
    }

    private List<JarEntry> getClassEntries(){
        List<JarEntry> entries = new ArrayList<>();
        try {
            FileUtils.deleteDirectory(this.reobfuscatedDump);
            Enumeration<JarEntry> e = this.jarFileToDeobfuscate.entries();
            while(e.hasMoreElements()){
                JarEntry entry = e.nextElement();
                if(entry.getName().endsWith(".class")){
                    entries.add(entry);
                } else {
                    uncollectedFiles.put(entry.getName(), getFileFromZip(entry, this.jarFileToDeobfuscate));
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return entries;
    }

    private static List<ClassCollection> createNodes(JarFile jar, List<JarEntry> entries){
        List<ClassCollection> list = new ArrayList<>();
        for(JarEntry entry : entries){
            try {
                ClassReader reader = new ClassReader(jar.getInputStream(entry));
                ClassNode node = new ClassNode();
                reader.accept(node, ClassReader.EXPAND_FRAMES);
                String mapping = reader.getClassName().contains("/") || reader.getClassName().contains(".") ? reader.getClassName() : null;
                list.add(new ClassCollection(node, reader, reader.getClassName(), mapping));
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return list;
    }

    private static List<ClassCollection> remapClasses(List<ClassCollection> classes){
        for(ClassCollection collection : classes){
            collection.mappedName = ClassRemapper.remapClassName(collection.classNode);
        }
        return classes;
    }

    public static void createOutputJar(File outFile, List<ClassCollection> finishedClasses, Map<String, byte[]> uncollectedFiles){
        try {
            JarOutputStream out = new JarOutputStream(new FileOutputStream(outFile));
            for(ClassCollection collection : finishedClasses){
                String name = collection.mappedName != null ? collection.mappedName : "net/minecraft/class_" + collection.obfuscatedName;
                ZipEntry zipEntry = new ZipEntry(name + ".class");
                out.putNextEntry(zipEntry);
                ClassWriter writer = new ClassWriter(0);
                ClassNode node = collection.classNode;
                node.accept(writer);
                out.write(writer.toByteArray());
            }
            for(Map.Entry<String, byte[]> entry : uncollectedFiles.entrySet()){
                ZipEntry zipEntry = new ZipEntry(entry.getKey());
                out.putNextEntry(zipEntry);
                out.write(entry.getValue());
            }
            out.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static Map<String, String> remapClassNames(JarFile minecraftFile){
        Map<String, String> map = new HashMap<>();
        List<JarEntry> jarEntries = new ArrayList<>();
        Enumeration<JarEntry> entries = minecraftFile.entries();
        while(entries.hasMoreElements()){
            JarEntry entry = entries.nextElement();
            if(entry.getName().endsWith(".class")){
                jarEntries.add(entry);
            }
        }
        List<ClassCollection> collection = remapClasses(createNodes(minecraftFile, jarEntries));
        for(ClassCollection collections : collection){
            if(collections.mappedName != null){
                map.put(collections.obfuscatedName, collections.mappedName);
            }
        }
        return map;
    }

    public static byte[] getFileFromZip(ZipEntry entry, ZipFile zipFile) {
        byte[] buffer = null;
        if (entry != null) {
            try {
                InputStream stream = zipFile.getInputStream(entry);
                int pos = 0;
                buffer = new byte[(int)entry.getSize()];
                while (true) {
                    int read = stream.read(buffer, pos, Math.min(1024, (int)entry.getSize() - pos));
                    pos += read;
                    if (read < 1) break;
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return buffer;
    }

    @Deprecated
    private static File mcClientFile = new File("P:\\Craftloader\\library\\client.jar");
    @Deprecated
    private static File mcClientFileLinux = new File("/media/canitzp/TransportPlatte/Craftloader/library/client.jar");
    @Deprecated
    public static File projectPath = new File("P:\\Craftloader\\dump");
    @Deprecated
    public static File projectPathLinux = new File("/media/canitzp/TransportPlatte/Craftloader/dump");

    public static void main(String[] args) throws IOException{
        /*
        Remap remap = new Remap(mcClientFile, projectPath);
        remap.runDeobfuscation();
        remap.createOutputJar();
        */
        MinecraftRemapper.remapDevEnv();
    }

    public static void dumpClassWrite(String fileName, byte[] classToWrite){
        try{
            File f = new File(projectPath, fileName.replace(".", "/") + ".class");
            f.getParentFile().mkdirs();
            f.createNewFile();
            BufferedOutputStream bos = new BufferedOutputStream(new FileOutputStream(f));
            bos.write(classToWrite);
            bos.close();
        } catch(IOException e){
            e.printStackTrace();
        }
    }

    public static void dumpClassWrite(ClassNode node){
        ClassWriter writer = new ClassWriter(0);
        node.accept(writer);
        dumpClassWrite(node.name, writer.toByteArray());
    }

    public static class Remapper{
        public String className;
        public ClassNode classNode;
        public ClassReader classReader;
        public Remapper(String className, ClassNode classNode, ClassReader classReader){
            this.className = className;
            this.classNode = classNode;
            this.classReader = classReader;
        }
    }

    public static class ClassCollection{
        public ClassNode classNode;
        public ClassReader classReader;
        public String obfuscatedName, mappedName;
        public ClassCollection(ClassNode node, ClassReader reader, String obfuscatedName, String mappedName){
            this.classNode = node;
            this.classReader = reader;
            this.obfuscatedName = obfuscatedName;
            this.mappedName = mappedName;
        }
        @Override
        public String toString() {
            return "Class{name=" + this.mappedName + ", obfname=" + this.obfuscatedName + ", Node=" + this.classNode + ", Reader=" + this.classReader + "}";
        }
    }


}
