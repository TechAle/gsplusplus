package com.gamesense.client.module.modules.movement;

import com.gamesense.api.setting.values.BooleanSetting;
import com.gamesense.api.setting.values.DoubleSetting;
import com.gamesense.api.setting.values.IntegerSetting;
import com.gamesense.client.module.Category;
import com.gamesense.client.module.Module;
import com.sun.org.apache.xpath.internal.operations.Bool;
import net.minecraft.network.play.client.CPacketPlayer;
import net.minecraft.util.math.Vec3d;

@Module.Declaration(name = "PacketFly", category = Category.Movement)
public class PacketFly extends Module {

    BooleanSetting yPause = registerBoolean("yPause",true);
    BooleanSetting antiKick = registerBoolean("antiKick", true);
    IntegerSetting antiKickTimer = registerInteger("antiKickTimer", 4, 1, 10);

    @Override
    public void onUpdate() {

        mc.player.setVelocity(0, 0, 0);

        if (mc.gameSettings.keyBindSneak.isKeyDown() && !mc.gameSettings.keyBindJump.isKeyDown()) {
            mc.player.connection.sendPacket(new CPacketPlayer.PositionRotation(mc.player.posX + mc.player.motionX, mc.player.posY - 0.0624, mc.player.posZ + mc.player.motionZ, mc.player.rotationYaw, mc.player.rotationPitch, false));
            extremeTP();
        }
        if (mc.gameSettings.keyBindJump.isKeyDown() || mc.player.posY < -1 && yPause.getValue()) {
            mc.player.connection.sendPacket(new CPacketPlayer.PositionRotation(mc.player.posX + mc.player.motionX, mc.player.posY + 0.0624, mc.player.posZ + mc.player.motionZ, mc.player.rotationYaw, mc.player.rotationPitch, false));
            extremeTP();
        }
        if (mc.gameSettings.keyBindForward.isKeyDown() || mc.gameSettings.keyBindBack.isKeyDown() || mc.gameSettings.keyBindLeft.isKeyDown() || mc.gameSettings.keyBindRight.isKeyDown()) {
            Vec3d dir = mc.player.getLookVec().normalize();
            mc.player.setPosition(mc.player.posX + (dir.x * 0.0624), mc.player.posY, mc.player.posZ + (dir.z * 0.0624));
        }
        if (antiKick.getValue() && mc.player.ticksExisted % antiKickTimer.getValue() == 0) {
            mc.player.motionY -= 0.1;
        }
    }

    public void extremeTP() {
        if (!mc.player.onGround) {
            mc.player.connection.sendPacket(new CPacketPlayer.PositionRotation(mc.player.posX + mc.player.motionX, mc.player.posY - 42069, mc.player.posZ + mc.player.motionZ, mc.player.rotationYaw, mc.player.rotationPitch, true));
        }
    }
}
