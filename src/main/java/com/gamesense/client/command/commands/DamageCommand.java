package com.gamesense.client.command.commands;

import com.gamesense.api.util.misc.MessageBus;
import com.gamesense.client.command.Command;
import com.gamesense.client.module.ModuleManager;
import com.gamesense.client.module.modules.gui.ColorMain;
import com.gamesense.client.module.modules.movement.PlayerTweaks;
import net.minecraft.network.play.client.CPacketPlayer;

@Command.Declaration(name = "Damage", syntax = "damage", alias = {"damage", "dmg", "hurt", "legbreak"})
public class DamageCommand extends Command {

    @Override
    public void onCommand(String command, String[] message) {
        if (mc.player != null) {
            String dmg = message[0];
            int damage = 0;

            ModuleManager.getModule(PlayerTweaks.class).pauseNoFallPacket = true;

            try {
                damage = Integer.parseInt(dmg);
                MessageBus.sendCommandMessage(ModuleManager.getModule(ColorMain.class).getEnabledColor() + "Attempted to deal " + dmg + " damage to the player", true);

                mc.player.connection.sendPacket(new CPacketPlayer.Position(mc.player.posX, mc.player.posY + damage + 2.1, mc.player.posZ, false)); // send the player up
                mc.player.connection.sendPacket(new CPacketPlayer.Position(mc.player.posX, mc.player.posY + 0.05, mc.player.posZ, false));

                mc.player.connection.sendPacket(new CPacketPlayer.Position(mc.player.posX, mc.player.posY - damage - 2.1, mc.player.posZ, false)); // send back down to damage self
                mc.player.connection.sendPacket(new CPacketPlayer.Position(mc.player.posX, mc.player.posY + 0.05, mc.player.posZ, false));

            } catch (NumberFormatException ignored) {
                for (int i = 0; i < 64; i++) {
                    mc.player.connection.sendPacket(new CPacketPlayer.Position(mc.player.posX, mc.player.posY + 0.049, mc.player.posZ, false));
                    mc.player.connection.sendPacket(new CPacketPlayer.Position(mc.player.posX, mc.player.posY, mc.player.posZ, false));
                }
            }


            mc.player.connection.sendPacket(new CPacketPlayer.Position(mc.player.posX, mc.player.posY, mc.player.posZ, true)); // set onGround to true to trigger damage

            // MINECRAFTS FALL DAMAGE CODE
         /* if (!this.world.isRemote && this.fallDistance > 3.0F && onGroundIn) {
                float f = (float)MathHelper.ceil(this.fallDistance - 3.0F);
                if (!state.getBlock().isAir(state, this.world, pos)) {
                    double d0 = Math.min((double)(0.2F + f / 15.0F), 2.5D);
                    int i = (int)(150.0D * d0);
                    if (!state.getBlock().addLandingEffects(state, (WorldServer)this.world, pos, state, this, i)) {
                        ((WorldServer)this.world).spawnParticle(EnumParticleTypes.BLOCK_DUST, this.posX, this.posY, this.posZ, i, 0.0D, 0.0D, 0.0D, 0.15000000596046448D, new int[]{Block.getStateId(state)});
                    }
                }*/
        }
    }
}
