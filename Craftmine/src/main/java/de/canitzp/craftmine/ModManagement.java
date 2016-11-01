package de.canitzp.craftmine;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

/**
 * @author canitzp
 */
public class ModManagement {

    public static List<ModData> loadedMods = new ArrayList<>();
    public static List<AddonData> loadedAddons = new ArrayList<>();

    public static void registerMod(ModData mod){
        if(!loadedMods.contains(mod)){
            loadedMods.add(mod);
        } else {
            Craftmine.logger.error("The mod '" + mod.modInfo.modname + "' tried to register twice!");
        }
    }

    public static void registerMod(Class mainClass, File modInfo){
        registerMod(new ModData(mainClass, modInfo));
    }

    public static void registerAddon(AddonData addonData){
        loadedAddons.add(addonData);
    }

}
