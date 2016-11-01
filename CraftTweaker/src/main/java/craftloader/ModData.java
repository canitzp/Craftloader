package craftloader;

import net.minecraft.launchwrapper.LogWrapper;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 * @author canitzp
 */
public class ModData{

    private Class modClass;
    private Object modInstance;

    private Logger modLog;
    private String modid, modName;

    public ModData(Mod annotation, Class modClass){
        this.modid = annotation.modid();
        this.modName = annotation.name();
        this.modClass = modClass;
        this.modLog = null;
    }

    public Object getModInstance(){
        return modInstance;
    }

    public void setModInstance(Object modInstance){
        this.modInstance = modInstance;
    }

    public Logger getModLog(){
        return this.modLog;
    }

    public Class getModClass(){
        return modClass;
    }

    public String getModid(){
        return modid;
    }

    public String getModName(){
        return modName;
    }
}
