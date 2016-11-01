package de.canitzp.testmod;

import craftloader.Mod;
import craftloader.ModData;
import net.minecraft.client.Minecraft;

/**
 * @author canitzp
 */
@Mod(modid = "testmod", name = "TestMod")
public class TestMod {

    public TestMod(ModData data){
        System.out.println("Hi peeps");
        System.out.println(Minecraft.z().x);
    }

}
