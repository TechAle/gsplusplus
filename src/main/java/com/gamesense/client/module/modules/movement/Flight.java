package com.gamesense.client.module.modules.movement;

import com.gamesense.api.event.events.PlayerMoveEvent;
import com.gamesense.api.setting.values.DoubleSetting;
import com.gamesense.api.setting.values.IntegerSetting;
import com.gamesense.api.setting.values.ModeSetting;
import com.gamesense.api.util.player.PhaseUtil;
import com.gamesense.api.util.player.PlayerUtil;
import com.gamesense.api.util.world.MotionUtil;
import com.gamesense.client.module.Category;
import com.gamesense.client.module.Module;
import me.zero.alpine.listener.EventHandler;
import me.zero.alpine.listener.Listener;
import net.minecraft.network.play.client.CPacketPlayer;

import java.util.Arrays;

@Module.Declaration(name = "Flight", category = Category.Movement)
public class Flight extends Module {

    float flyspeed;
    boolean bounded;

    public ModeSetting mode = registerMode("Mode", Arrays.asList("Vanilla", "Static", "Packet"), "Static");

    // Packet settings
    DoubleSetting packetFactor = registerDouble("Packet Factor", 1, 0, 5, () -> mode.getValue().equalsIgnoreCase("Packet"));
    ModeSetting bound = registerMode("Bounds", Arrays.asList("Up", "Alternate", "Down", "Zero", "Min", "Forward"), "Up", () -> mode.getValue().equalsIgnoreCase("Packet"));
    ModeSetting antiKick = registerMode("AntiKick", Arrays.asList("None", "Down", "Bounce"), "Bounce", () -> mode.getValue().equalsIgnoreCase("Packet"));
    IntegerSetting packets = registerInteger("Packets", 1,1,25, () -> mode.getValue().equalsIgnoreCase("Packet"));

    // Other settings
    DoubleSetting speed = registerDouble("Speed", 2, 0, 10, () -> !mode.getValue().equalsIgnoreCase("Packet"));
    DoubleSetting ySpeed = registerDouble("Y Speed", 1, 0, 10, () -> !mode.getValue().equalsIgnoreCase("Packet"));
    DoubleSetting glideSpeed = registerDouble("Glide Speed", 0, -10, 10, () -> !mode.getValue().equalsIgnoreCase("Packet"));

    @EventHandler
    private final Listener<PlayerMoveEvent> playerMoveEventListener = new Listener<>(event -> {

        if (!PlayerUtil.nullCheck())
            return;

        if (mode.getValue().equalsIgnoreCase("Static")) {
            if (mc.gameSettings.keyBindJump.isKeyDown()) {

                event.setY(ySpeed.getValue());

            } else if (mc.gameSettings.keyBindSneak.isKeyDown()) {

                event.setY(-ySpeed.getValue());

            } else {

                event.setY(-glideSpeed.getValue());

            }

            if (MotionUtil.isMoving(mc.player)) {
                MotionUtil.setSpeed(mc.player, speed.getValue());
            } else {

                event.setX(0);
                event.setZ(0);

            }
        } else if (mode.getValue().equalsIgnoreCase("Vanilla")) {

            mc.player.capabilities.setFlySpeed(flyspeed * speed.getValue().floatValue());
            mc.player.capabilities.isFlying = true;

        } else if (mode.getValue().equalsIgnoreCase("Packet")) {

            /* PACKET */
            event.setY(0);

            double x = mc.player.posX;
            double y = mc.player.posY;
            double z = mc.player.posZ;

            if (mc.gameSettings.keyBindSneak.isKeyDown() && !mc.gameSettings.keyBindJump.isKeyDown()) {

                y -= 0.0624;

                bounded = true;

            }
            if (mc.gameSettings.keyBindJump.isKeyDown()) {

                y += 0.0624;

                bounded = true;

            }
            if (mc.gameSettings.keyBindForward.isKeyDown() || mc.gameSettings.keyBindBack.isKeyDown() || mc.gameSettings.keyBindLeft.isKeyDown() || mc.gameSettings.keyBindRight.isKeyDown()) {

                double[] dir = MotionUtil.forward(clipped() ? 0.0624 : packetFactor.getValue() == 0 ? 0.624 : 0.0624 * packetFactor.getValue());

                x += dir[0];
                z += dir[1];

                bounded = true;

            }


            if (!antiKick.getValue().equalsIgnoreCase("None") && mc.player.ticksExisted % 4 == 0) {

                y -= 0.01;
                bounded = true;

            } else if (antiKick.getValue().equalsIgnoreCase("Bounce") && mc.player.ticksExisted % 4 == 2) {

                y += 0.01;
                bounded = true;

            } else if (antiKick.getValue().equalsIgnoreCase("None")) {

                event.setY(0);
                bounded = true;

            }

            for (int i = 0; i < packets.getValue(); i++) {
                mc.player.connection.sendPacket(new CPacketPlayer.Position(x, y, z, false));
                PhaseUtil.doBounds(bound.getValue());
            }

            /* END OF PACKET */

        }

    });


    @Override
    protected void onEnable() {

        // This does not fix but, avoid a spam in the console -TechAle
        if (mc.world == null || mc.player == null)
            return;

        flyspeed = mc.player.capabilities.getFlySpeed();

    }

    @Override
    protected void onDisable() {
        mc.player.capabilities.setFlySpeed(flyspeed);
        mc.player.capabilities.isFlying = false;
        mc.player.motionX = mc.player.motionY = mc.player.motionZ = 0;
        mc.player.noClip = false;
    }

    public boolean clipped() {

        return !(mc.world.getCollisionBoxes(mc.player, mc.player.getEntityBoundingBox()).isEmpty());

    }

}
