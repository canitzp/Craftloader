package de.canitzp.craftmine;

/**
 * @author canitzp
 */
public class AddonData {

    public Class mainClass;

    public boolean isStartupAddon;

    public AddonData(Class mainClass){
        this.mainClass = mainClass;
        this.isStartupAddon = ((CraftAddon)mainClass.getAnnotation(CraftAddon.class)).startup();
    }

}
