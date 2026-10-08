package deborn.easycushionplacing.mixin;

import com.llamalad7.mixinextras.injector.ModifyReturnValue;
import com.llamalad7.mixinextras.sugar.Local;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.tags.ItemTags;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.decoration.Cushion;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.entity.EntityTypeTest;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(Cushion.class)
public class StackCushionsMixin {
    @Inject(
        method = "interact(Lnet/minecraft/world/entity/player/Player;Lnet/minecraft/world/InteractionHand;Lnet/minecraft/world/phys/Vec3;)Lnet/minecraft/world/InteractionResult;",
        at = @At("HEAD"),
        cancellable = true
    )
    private void stackCushion(
        CallbackInfoReturnable<InteractionResult> cir, 
        @Local(argsOnly = true, name = "player") Player player, 
        @Local(argsOnly = true, name = "hand") InteractionHand hand) {
        
        Cushion self = (Cushion) (Object) this;
        ItemStack heldStack = player.getItemInHand(hand);

        if (!player.isSecondaryUseActive() || !heldStack.is(ItemTags.CUSHIONS) || self.isVehicle())
            return;

        Vec3 pos = self.position();
        BlockHitResult hitResult = new BlockHitResult(pos, Direction.UP, BlockPos.containing(pos), false);
        cir.setReturnValue(heldStack.useOn(new UseOnContext(player, hand, hitResult)));
    }
}