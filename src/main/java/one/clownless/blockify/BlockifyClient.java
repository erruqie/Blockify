package one.clownless.blockify;

import de.guntram.mcmod.crowdintranslate.CrowdinTranslate;
import net.fabricmc.api.ClientModInitializer;

public class BlockifyClient implements ClientModInitializer {
    @Override
    public void onInitializeClient() {
        CrowdinTranslate.downloadTranslations("blockify", "blockify");
    }
}
