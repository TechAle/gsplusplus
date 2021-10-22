package com.gamesense.client.module.modules.movement;

import com.gamesense.api.setting.values.BooleanSetting;
import com.gamesense.api.setting.values.DoubleSetting;
import com.gamesense.api.util.world.MotionUtil;
import com.gamesense.client.module.Category;
import com.gamesense.client.module.Module;
import com.gamesense.client.module.ModuleManager;
import net.minecraft.network.play.client.CPacketPlayer;

@Module.Declaration(name = "PhaseWalk", category = Category.Movement)
public class PhaseWalk extends Module {

    BooleanSetting fast = registerBoolean("Fast", false);
    BooleanSetting vfast = registerBoolean("Very Fast", false);
    DoubleSetting vfastSpeed = registerDouble("Speed", 0.2873,0,0.3);
    BooleanSetting clipCheck = registerBoolean("Clipped Check", false);
    BooleanSetting update = registerBoolean("Update Pos", false);

    @Override
    public void onUpdate() {
        if (mc.player.collidedHorizontally && !ModuleManager.getModule(Flight.class).isEnabled())
            pfly();
    }

    void pfly() {

        double[] clip = MotionUtil.forward(0.0624);
        double[] motion = MotionUtil.forward(MotionUtil.getBaseMoveSpeed());
        double[] vfastdir = MotionUtil.forward(vfastSpeed.getValue() == 0 ? 0.2873 : vfastSpeed.getValue());

        if (ModuleManager.getModule(Flight.class).clipped() || !clipCheck.getValue()) {

            if (mc.gameSettings.keyBindSneak.isKeyDown() && mc.player.onGround)
                tp(mc.player.posX + clip[0], mc.player.posY - 0.0624, mc.player.posZ + clip[1], false);
            else
                tp(mc.player.posX + clip[0], mc.player.posY, mc.player.posZ + clip[1], true);

            if (vfast.getValue())
                tp(mc.player.posX + vfastdir[0], mc.player.posY, mc.player.posZ + vfastdir[1], true);

        } else if (fast.getValue() && ModuleManager.getModule(Flight.class).clipped()) {

            mc.player.setVelocity(motion[0], mc.player.motionY, motion[1]);

        }

    }

    void tp(double x, double y, double z, boolean onGround) {

        mc.player.connection.sendPacket(new CPacketPlayer.Position(x,y,z, onGround));
        mc.player.connection.sendPacket(new CPacketPlayer.Position(x, mc.player.posY - 69420, z, true));

        if (update.getValue())
            mc.player.setPosition(x, y, z);

    }

}
