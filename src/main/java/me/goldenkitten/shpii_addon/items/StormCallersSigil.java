package me.goldenkitten.shpii_addon.items;

import me.goldenkitten.shpii_addon.SHPIIAddon;
import me.goldenkitten.shpii_addon.utils.Utils;
import net.fabricmc.fabric.api.item.v1.FabricItemSettings;
import net.minecraft.block.Blocks;
import net.minecraft.client.item.TooltipContext;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.EyeOfEnderEntity;
import net.minecraft.entity.LightningEntity;
import net.minecraft.entity.mob.SkeletonHorseEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.EnderEyeItem;
import net.minecraft.item.ItemStack;
import net.minecraft.item.ItemUsageContext;
import net.minecraft.registry.tag.StructureTags;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
import net.minecraft.text.Text;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Formatting;
import net.minecraft.util.Hand;
import net.minecraft.util.TypedActionResult;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.hit.HitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.RaycastContext;
import net.minecraft.world.World;
import net.minecraft.world.event.GameEvent;
import org.jetbrains.annotations.Nullable;

import java.lang.reflect.Field;
import java.util.List;

public class StormCallersSigil extends EnderEyeItem {
    public StormCallersSigil(FabricItemSettings properties) {
        super(properties);
    }

    @Override
    public ActionResult useOnBlock(ItemUsageContext context) {
        return ActionResult.PASS;
    }

    @Override
    public TypedActionResult<ItemStack> use(World world, PlayerEntity user, Hand hand) {
        ItemStack itemStack = user.getStackInHand(hand);
        BlockHitResult blockHitResult = raycast(world, user, RaycastContext.FluidHandling.NONE);
        if (blockHitResult.getType() == HitResult.Type.BLOCK && world.getBlockState(blockHitResult.getBlockPos()).isOf(Blocks.END_PORTAL_FRAME)) {
            return TypedActionResult.pass(itemStack);
        } else {
            user.setCurrentHand(hand);
            if (world instanceof ServerWorld serverWorld) {
                serverWorld.setWeather(0, 6000, true, true);
                BlockPos blockPos = serverWorld.locateStructure(StructureTags.EYE_OF_ENDER_LOCATED, user.getBlockPos(), 100, false);
                if (blockPos != null) {
                    EyeOfEnderEntity eyeOfEnderEntity = new EyeOfEnderEntity(world, user.getX(), user.getBodyY(0.5D), user.getZ());
                    eyeOfEnderEntity.setItem(itemStack);
                    eyeOfEnderEntity.initTargetPos(blockPos);
                    try {
                        Field field = EyeOfEnderEntity.class.getDeclaredField("dropsItem");
                        field.setAccessible(true); // Allow access to private field
                        field.set(eyeOfEnderEntity, false); // Modify the private field
                    } catch (NoSuchFieldException | IllegalAccessException e) {
                        throw new RuntimeException(e);
                    }
                    world.emitGameEvent(GameEvent.PROJECTILE_SHOOT, eyeOfEnderEntity.getPos(), GameEvent.Emitter.of(user));
                    world.spawnEntity(eyeOfEnderEntity);
                    world.playSound(null, user.getX(), user.getY(), user.getZ(), SoundEvents.ENTITY_ENDER_EYE_LAUNCH, SoundCategory.NEUTRAL, 0.5F, 0.4F / (world.getRandom().nextFloat() * 0.4F + 0.8F));
                    world.syncWorldEvent(null, 1003, user.getBlockPos(), 0);
                    if (itemStack.getItem() == ModItems.STORMCALLERS_SIGIL) {
                        SkeletonHorseEntity entity = EntityType.SKELETON_HORSE.create(world);
                        if (entity != null) {
                            Vec3d offset = new Vec3d(blockPos.getX() - user.getX(), 0, blockPos.getZ() - user.getZ()).normalize().multiply(4.0);
                            double x = user.getX() + offset.x;
                            double y = user.getZ() + offset.z;
                            double z = user.getY() + 8.0;
                            BlockPos pos = Utils.findSafeTeleportPos(serverWorld, new BlockPos((int)x, 70, (int)z), world.getHeight() - 1);
                            Vec3d newPos = new Vec3d(pos.getX(), pos.getY(), pos.getZ());
                            LightningEntity lightning = new LightningEntity(EntityType.LIGHTNING_BOLT, world);
                            lightning.setPosition(newPos);
                            world.spawnEntity(lightning);
                            entity.setPosition(newPos);
                            entity.setGlowing(true);
                            entity.setCustomName(Utils.getLightningMessage("StormCaller"));
                            entity.setTrapped(true);// or use NBT
                            world.spawnEntity(entity);
                            SHPIIAddon.LOGGER.info("Summoned StormCaller!{}, {}, {}", x, y, z);
                        }
                    }
                    if (!user.getAbilities().creativeMode) {
                        itemStack.decrement(1);
                    }

                    //user.incrementStat(Stats.USED.getOrCreateStat(this));
                    user.swingHand(hand, true);
                    return TypedActionResult.success(itemStack);
                }
            }
            return TypedActionResult.consume(itemStack);
        }
    }

    @Override
    public void appendTooltip(ItemStack stack, @Nullable World world, List<Text> tooltip, TooltipContext context) {
        if (stack.getItem() == ModItems.STORMCALLERS_REMNANT) {
            tooltip.add(Text.translatable("itemTooltip." + SHPIIAddon.MOD_ID + ".stormcallers_remnant").formatted(Formatting.GOLD));
        }
        else {
            tooltip.add(Text.translatable("itemTooltip." + SHPIIAddon.MOD_ID + ".stormcallers_sigil").formatted(Formatting.GOLD));
        }
    }
}