package com.gamesense.client.module.modules.movement;

import com.gamesense.api.setting.values.BooleanSetting;
import com.gamesense.api.util.world.MotionUtil;
import com.gamesense.client.module.Category;
import com.gamesense.client.module.Module;
import com.gamesense.client.module.ModuleManager;
import net.minecraft.network.play.client.CPacketPlayer;

@Module.Declaration(name = "PhaseWalk", category = Category.Movement)
public class PhaseWalk extends Module {

    BooleanSetting update = registerBoolean("Update Pos", false);

    @Override
    public void onUpdate() {
        if (mc.player.collidedHorizontally)
            pfly();
    }

    void pfly() {

        double[] dir = MotionUtil.forward(0.0624);


        mc.player.connection.sendPacket(new CPacketPlayer.Position(mc.player.posX + dir[0],mc.player.posY,mc.player.posZ + dir[1], true));
        mc.player.connection.sendPacket(new CPacketPlayer.Position(mc.player.posX + dir[0],mc.player.posY - 69420,mc.player.posZ + dir[1], true));

        ModuleManager.getModule(Flight.class).tpid += 2;

        if (update.getValue())
            mc.player.setPosition(mc.player.posX + dir[0],mc.player.posY,mc.player.posZ + dir[1]);

    }

}
