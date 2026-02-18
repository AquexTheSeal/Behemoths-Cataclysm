package org.celestialworkshop.behemothscataclysm;

import com.github.L_Ender.cataclysm.init.ModEntities;
import com.mojang.logging.LogUtils;
import net.minecraft.data.DataGenerator;
import net.minecraft.data.PackOutput;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvent;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.common.data.LanguageProvider;
import net.minecraftforge.data.event.GatherDataEvent;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;
import org.celestialworkshop.behemoths.api.pandemonium.PandemoniumCurse;
import org.celestialworkshop.behemoths.registries.BMPandemoniumCurses;
import org.slf4j.Logger;

// The value here should match an entry in the META-INF/mods.toml file
@Mod(BehemothsCataclysm.MODID)
public class BehemothsCataclysm {
    public static final String MODID = "behemothscataclysm";
    public static final Logger LOGGER = LogUtils.getLogger();

    public static final DeferredRegister<PandemoniumCurse> PANDEMONIUM_CURSES = DeferredRegister.create(BMPandemoniumCurses.PANDEMONIUM_CURSES_KEY, BehemothsCataclysm.MODID);
    public static final DeferredRegister<SoundEvent> SOUND_EVENTS = DeferredRegister.create(ForgeRegistries.SOUND_EVENTS, BehemothsCataclysm.MODID);

    public static final RegistryObject<PandemoniumCurse> CATACLYSMIC_PATH = PANDEMONIUM_CURSES.register("cataclysmic_path", () -> new PandemoniumCurse(ModEntities.IGNIS.get()));
    public static final RegistryObject<PandemoniumCurse> KOBOLETON_HORDE = PANDEMONIUM_CURSES.register("koboleton_horde", () -> new PandemoniumCurse(ModEntities.KOBOLETON.get()));
    public static final RegistryObject<PandemoniumCurse> DEEP_TIDAL_WAVE = PANDEMONIUM_CURSES.register("deep_tidal_wave", () -> new PandemoniumCurse(ModEntities.DEEPLING.get()));
    public static final RegistryObject<PandemoniumCurse> DEEPLING_ASCENSION = PANDEMONIUM_CURSES.register("deepling_ascension", () -> new PandemoniumCurse(ModEntities.DEEPLING_PRIEST.get()));

    public static final RegistryObject<SoundEvent> CATACLYSMIC_CRIT = SOUND_EVENTS.register("cataclysmic_critical_hit", () -> SoundEvent.createVariableRangeEvent(prefix("cataclysmic_critical_hit")));
    public static final RegistryObject<SoundEvent> DEEPLING_PRIEST_HEAL = SOUND_EVENTS.register("deepling_priest_heal", () -> SoundEvent.createVariableRangeEvent(prefix("deepling_priest_heal")));

    public BehemothsCataclysm() {
        IEventBus modEventBus = FMLJavaModLoadingContext.get().getModEventBus();
        IEventBus forgeBus = MinecraftForge.EVENT_BUS;
        MinecraftForge.EVENT_BUS.register(this);

        PANDEMONIUM_CURSES.register(modEventBus);
        SOUND_EVENTS.register(modEventBus);

        modEventBus.addListener(this::onGatherData);
    }

    public static class BMCLangProvider extends LanguageProvider {
        public BMCLangProvider(PackOutput output, String locale) {
            super(output, BehemothsCataclysm.MODID, locale);
        }

        @Override
        protected void addTranslations() {
            this.addPandemoniumCurse(CATACLYSMIC_PATH.get(), "Cataclysmic Path", "All Cataclysmic bosses now have a 30% chance to critically strike, dealing 130% damage.");
            this.addPandemoniumCurse(KOBOLETON_HORDE.get(), "Koboleton Horde", "Koboletons now spawn in much larger groups and has a 15% chance to turn into a Wadjet.");
            this.addPandemoniumCurse(DEEP_TIDAL_WAVE.get(), "The Deep Tidal Wave", "Deepling species now spawn with reinforcements and gain increased movement speed.");
            this.addPandemoniumCurse(DEEPLING_ASCENSION.get(), "Deepling Ascension", "Deeplings now gain a 15% chance to spawn as a Deepling Priest and Deepling Priests can now constantly heal nearby Deepling species.");
        }

        private void addPandemoniumCurse(PandemoniumCurse curse, String value, String desc) {
            this.add(curse.getDisplayName().getString(), value);
            this.add(curse.getDescription().getString(), desc);
        }
    }

    public static ResourceLocation prefix(String path) {
        return ResourceLocation.fromNamespaceAndPath(MODID, path);
    }

    public void onGatherData(GatherDataEvent event) {
        DataGenerator generator = event.getGenerator();
        PackOutput packOutput = generator.getPackOutput();

        generator.addProvider(event.includeClient(), new BMCLangProvider(packOutput, "en_us"));
    }
}
