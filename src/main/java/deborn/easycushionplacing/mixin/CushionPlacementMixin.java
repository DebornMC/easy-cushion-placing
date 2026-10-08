package deborn.easycushionplacing.mixin;

import com.llamalad7.mixinextras.sugar.Local;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.decoration.Cushion;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.CushionItem;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyVariable;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(CushionItem.class)
public class CushionPlacementMixin {
    @ModifyVariable(
        method = "useOn(Lnet/minecraft/world/item/context/UseOnContext;)Lnet/minecraft/world/InteractionResult;",
        at = @At("STORE"),
        name = "entityPos")
    private Vec3 adjustPlacementPosition(
        Vec3 entityPos,
        @Local(name = "recalculatedContext") UseOnContext recalculatedContext) {
        Player player = recalculatedContext.getPlayer();
        if (player == null || !player.isSecondaryUseActive())
            return entityPos;

        Vec3 clicked = recalculatedContext.getClickLocation();
        Cushion target = recalculatedContext.getLevel().getEntitiesOfClass(
            Cushion.class,
            AABB.ofSize(entityPos, 0.1, 0.1, 0.1),
            e -> e.position().equals(clicked)).stream().findAny().orElse(null);

        return target == null ? entityPos : target.position().add(0, target.getBbHeight(), 0);
    }

    @Inject(
        method = "useOn(Lnet/minecraft/world/item/context/UseOnContext;)Lnet/minecraft/world/InteractionResult;",
        at = @At(
            value = "INVOKE",
            target = "Lnet/minecraft/server/level/ServerLevel;addFreshEntity(Lnet/minecraft/world/entity/Entity;)Z"
        )
    )
    private void adjustEntityPosition(
        CallbackInfoReturnable<InteractionResult> cir,
        @Local(name = "cushion") Cushion cushion,
        @Local(name = "entityPos") Vec3 entityPos,
        @Local(name = "placeContext") BlockPlaceContext placeContext) {
        Player player = placeContext.getPlayer();
        if (player != null && player.isSecondaryUseActive())
            cushion.snapTo(entityPos, cushion.getYRot(), cushion.getXRot());
    }
}