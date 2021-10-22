package com.gamesense.client.module.modules.movement;

import com.gamesense.api.setting.values.BooleanSetting;
import com.gamesense.api.setting.values.DoubleSetting;
import com.gamesense.api.setting.values.ModeSetting;
import com.gamesense.api.util.world.MotionUtil;
import com.gamesense.client.module.Category;
import com.gamesense.client.module.Module;
import com.gamesense.client.module.ModuleManager;
import net.minecraft.entity.Entity;
import net.minecraft.network.play.client.CPacketPlayer;

import java.util.Arrays;

@Module.Declaration(name = "PhaseWalk", category = Category.Movement)
public class PhaseWalk extends Module {

    ModeSetting bound = registerMode("Bounds", Arrays.asList("Up", "Alternate", "Down", "Zero", "Min", "Forward"), "Min");
    BooleanSetting fast = registerBoolean("Fast", false);
    BooleanSetting vfast = registerBoolean("Very Fast", false);
    BooleanSetting clipCheck = registerBoolean("Clipped Check", false);
    BooleanSetting update = registerBoolean("Update Pos", false);

    @Override
    public void onUpdate() {
        if (collision() && !ModuleManager.getModule(Flight.class).isEnabled())
            packetFly();
    }

    void packetFly() {

        double[] clip = MotionUtil.forward(0.0624);
        double[] vFastDir = MotionUtil.forward(MotionUtil.getMotion(mc.player));

        double motionX = mc.player.motionX;
        double motionZ = mc.player.motionZ;

        if (ModuleManager.getModule(Flight.class).clipped() || !clipCheck.getValue()) {

            if (mc.gameSettings.keyBindSneak.isKeyDown() && mc.player.onGround)
                tp(mc.player.posX + clip[0], mc.player.posY - 0.0624, mc.player.posZ + clip[1], false);
            else
                tp(mc.player.posX + clip[0], mc.player.posY, mc.player.posZ + clip[1], true);

            if (vfast.getValue())
                tp(mc.player.posX + vFastDir[0], mc.player.posY, mc.player.posZ + vFastDir[1], true);

        } else if (fast.getValue() && ModuleManager.getModule(Flight.class).clipped()) {

            mc.player.setVelocity(motionX, mc.player.motionY, motionZ);

        }

    }

    void tp(double x, double y, double z, boolean onGround) {

        mc.player.connection.sendPacket(new CPacketPlayer.Position(x,y,z, onGround));
        ModuleManager.getModule(Flight.class).doBounds(bound.getValue());

        if (update.getValue())
            mc.player.setPosition(x, y, z);

    }

    boolean collision() {

        double[] dir = MotionUtil.forward(0.0624);

        return mc.world.getCollisionBoxes(mc.player, mc.player.getEntityBoundingBox().offset(dir[0], 0, dir[1])).isEmpty();

    }




}
