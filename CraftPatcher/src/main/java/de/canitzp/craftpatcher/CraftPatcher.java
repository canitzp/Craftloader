package de.canitzp.craftpatcher;

import de.canitzp.craftmine.CraftAddon;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 * @author canitzp
 */
@CraftAddon(name = "CraftPatcher", startup = true)
public class CraftPatcher {

    public static final Logger logger = LogManager.getLogger("CraftPatcher");

    public CraftPatcher(){
        logger.info("Start CraftPatcher");
    }

}
