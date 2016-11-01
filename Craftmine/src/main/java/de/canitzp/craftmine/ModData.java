package de.canitzp.craftmine;

import org.apache.commons.io.FileUtils;

import java.io.File;
import java.io.IOException;
import java.util.List;

/**
 * @author canitzp
 */
public class ModData {

    public Class mainClass;
    public MCModInfo modInfo;

    public ModData(Class mainClass, File modInfoFile){
        this.mainClass = mainClass;
        try {
            this.modInfo = new MCModInfo(modInfoFile);
        } catch (IOException e) {
            e.printStackTrace();

        }
    }

    public static class MCModInfo{

        public String modname;
        public String modid;
        public String moddescription;
        public String modversion;

        public MCModInfo(File file) throws IOException {
            if(file.getName().equals("mcmod.info")){
                List<String> lines = FileUtils.readLines(file);
                if(lines != null && !lines.isEmpty()){
                    for(String line : lines){
                        String[] lineParts = line.split(":");
                        if(lineParts.length == 2){
                            switch (lineParts[0]) {
                                case "modname":
                                    this.modname = lineParts[1];
                                    break;
                                case "modid":
                                    this.modid = lineParts[1];
                                    break;
                                case "moddescription":
                                    this.moddescription = lineParts[1];
                                    break;
                                case "modversion":
                                    this.modversion = lineParts[1];
                                    break;
                            }
                        }
                    }
                }
            } else {
                Craftmine.logger.error("The File '" + file.getName() + "' is no allowed mcmod.info file!");
                this.modname = this.modid = this.moddescription = this.modversion = "unspecified";
            }
        }

    }

}
