package craftloader.testmod;

import craftloader.Mod;
import craftloader.ModData;
import craftloader.ModManagement;
import org.apache.logging.log4j.Logger;

/**
 * @author canitzp
 */
public class TestMod{

    //public final Logger logger;

    public TestMod(ModData data){
        //this.logger = data.getModLog();
        //this.logger.info("Starting Pre Init of TestMod");
        //this.logger.info("Hi Ellpeck");
    }

    @Mod.Loading(state = ModManagement.LoadingStates.PRE)
    public void preInit(ModData data){

    }

}
