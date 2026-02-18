package org.celestialworkshop.behemothscataclysm;

import com.github.L_Ender.cataclysm.entity.AnimationMonster.BossMonsters.Ignited_Revenant_Entity;
import com.github.L_Ender.cataclysm.entity.AnimationMonster.BossMonsters.LLibrary_Boss_Monster;
import com.github.L_Ender.cataclysm.entity.Deepling.AbstractDeepling;
import com.github.L_Ender.cataclysm.entity.Deepling.Deepling_Priest_Entity;
import com.github.L_Ender.cataclysm.entity.InternalAnimationMonster.Wadjet_Entity;
import com.github.L_Ender.cataclysm.init.ModEntities;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.common.ForgeMod;
import net.minecraftforge.event.entity.living.LivingEvent;
import net.minecraftforge.event.entity.living.LivingHurtEvent;
import net.minecraftforge.event.entity.living.MobSpawnEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import org.celestialworkshop.behemoths.api.client.animation.InterpolationTypes;
import org.celestialworkshop.behemoths.api.pandemonium.PandemoniumVotingSystem;
import org.celestialworkshop.behemoths.particles.VFXParticleData;
import org.celestialworkshop.behemoths.particles.VFXTypes;

import java.util.List;

@Mod.EventBusSubscriber(modid = BehemothsCataclysm.MODID, bus = Mod.EventBusSubscriber.Bus.FORGE)
public class BMCCommonEvents {

    @SubscribeEvent
    public static void onEntityHurt(LivingHurtEvent event) {
        LivingEntity entity = event.getEntity();
        Level level = entity.level();
        if (!level.isClientSide) {
            Entity attacker = event.getSource().getEntity();
            Entity directEntity = event.getSource().getDirectEntity();

            if (attacker instanceof LLibrary_Boss_Monster boss && !(boss instanceof Ignited_Revenant_Entity)) {
                if (boss.getRandom().nextFloat() < 0.3F) {
                    event.setAmount(event.getAmount() * 1.3F);

                    boss.playSound(BehemothsCataclysm.CATACLYSMIC_CRIT.get(), 1.0f, 0.8f + entity.getRandom().nextFloat() * 0.4f);
                    VFXParticleData.Builder data = new VFXParticleData.Builder()
                            .textureName(BehemothsCataclysm.prefix("cataclysmic_critical_hit"))
                            .type(VFXTypes.FLAT_LOOK).lifetime(15).scale(1.3F);
                    Vec3 pos = entity.getEyePosition();
                    ((ServerLevel) boss.level()).sendParticles(data.build(), pos.x(), pos.y(), pos.z(), 0, 0, 0, 0, 0);
                }
            }
        }
    }

    @SubscribeEvent
    public static void onEntityTick(LivingEvent.LivingTickEvent event) {
        LivingEntity entity = event.getEntity();
        Level level = entity.level();
        if (!level.isClientSide && !entity.isDeadOrDying() && entity.isAlive()) {

            if (entity instanceof Deepling_Priest_Entity priest && PandemoniumVotingSystem.hasPandemoniumCurse(level, BehemothsCataclysm.DEEPLING_ASCENSION)) {
                if (entity.tickCount % 100 == 0) {
                    entity.playSound(BehemothsCataclysm.DEEPLING_PRIEST_HEAL.get(), 1.5f, 1.0F);
                    VFXParticleData.Builder data = new VFXParticleData.Builder()
                            .textureName(BehemothsCataclysm.prefix("deepling_priest_heal"))
                            .type(VFXTypes.FLAT_LOOK).lifetime(15).scale(2F);
                    Vec3 pos = entity.getEyePosition();
                    ((ServerLevel) level).sendParticles(data.build(), pos.x(), pos.y(), pos.z(), 0, 0, 0, 0, 0);

                    List<AbstractDeepling> deeplings = priest.level().getEntitiesOfClass(AbstractDeepling.class, priest.getBoundingBox().inflate(10));
                    for (AbstractDeepling deepling : deeplings) {
                        deepling.heal(2.0F);

                        VFXParticleData.Builder data1 = new VFXParticleData.Builder()
                                .textureName(BehemothsCataclysm.prefix("deepling_priest_heal_effect"))
                                .type(VFXTypes.WALLS).lifetime(10).boundEntity(deepling)
                                .scale(0, 1, InterpolationTypes.EASE_OUT_QUAD)
                                .alpha(1.0F, 0F, InterpolationTypes.EASE_IN_QUAD);
                        Vec3 rel = deepling.position();
                        ((ServerLevel) level).sendParticles(data1.build(), rel.x(), rel.y(), rel.z(), 0, 0, 0, 0, 0);
                    }
                }
            }
        }
    }

    @SubscribeEvent
    public static void onEntitySpawn(MobSpawnEvent.FinalizeSpawn event) {
        Mob entity = event.getEntity();
        ServerLevel level = event.getLevel().getLevel();
        RandomSource random = level.getRandom();
        if (entity.getType() == ModEntities.KOBOLETON.get() && PandemoniumVotingSystem.hasPandemoniumCurse(level, BehemothsCataclysm.KOBOLETON_HORDE)) {
            int extraSpawns = 3 + random.nextInt(4);
            for (int i = 0; i < extraSpawns; i++) {
                EntityType<?> mobType = random.nextFloat() <= 0.15F ? ModEntities.WADJET.get() : ModEntities.KOBOLETON.get();
                if (mobType.create(level) instanceof Mob mob) {
                    mob.moveTo(entity.position());
                    mob.finalizeSpawn(level, event.getDifficulty(), event.getSpawnType(), null, null);
                    level.addFreshEntity(mob);
                }
            }

            if (random.nextFloat() <= 0.15F) {
                Wadjet_Entity wadjet = ModEntities.WADJET.get().create(level);
                wadjet.moveTo(entity.position());
                wadjet.finalizeSpawn(level, event.getDifficulty(), event.getSpawnType(), null, null);
                level.addFreshEntity(wadjet);
                event.setSpawnCancelled(true);
            }
        }

        if (entity instanceof AbstractDeepling) {
            if (PandemoniumVotingSystem.hasPandemoniumCurse(level, BehemothsCataclysm.DEEP_TIDAL_WAVE)) {
                entity.getAttribute(Attributes.MOVEMENT_SPEED).setBaseValue(entity.getAttributeValue(Attributes.MOVEMENT_SPEED) * 1.5);
                entity.getAttribute(ForgeMod.SWIM_SPEED.get()).setBaseValue(entity.getAttributeValue(ForgeMod.SWIM_SPEED.get()) * 1.5);
                int extraSpawns = 1 + random.nextInt(3);
                for (int i = 0; i < extraSpawns; i++) {
                    EntityType<?> mobType = random.nextFloat() <= 0.15F ? ModEntities.DEEPLING_PRIEST.get() : ModEntities.DEEPLING.get();
                    if (mobType.create(level) instanceof Mob mob) {
                        mob.moveTo(entity.position());
                        mob.finalizeSpawn(level, event.getDifficulty(), event.getSpawnType(), null, null);
                        level.addFreshEntity(mob);
                    }
                }
            }

            if (PandemoniumVotingSystem.hasPandemoniumCurse(level, BehemothsCataclysm.DEEPLING_ASCENSION)) {
                if (random.nextFloat() <= 0.15F) {
                    Deepling_Priest_Entity priest = ModEntities.DEEPLING_PRIEST.get().create(level);
                    priest.moveTo(entity.position());
                    priest.finalizeSpawn(level, event.getDifficulty(), event.getSpawnType(), null, null);
                    level.addFreshEntity(priest);
                    event.setSpawnCancelled(true);
                }
            }
        }
    }
}
