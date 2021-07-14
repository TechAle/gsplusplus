package com.gamesense.client.module.modules.misc;

import com.gamesense.api.setting.values.BooleanSetting;
import com.gamesense.api.setting.values.IntegerSetting;
import com.gamesense.api.setting.values.ModeSetting;
import com.gamesense.api.util.player.InventoryUtil;
import com.gamesense.api.util.world.BlockUtil;
import com.gamesense.client.module.Category;
import com.gamesense.client.module.Module;
import net.minecraft.item.ItemEnderPearl;
import net.minecraft.network.play.client.CPacketPlayer;
import net.minecraft.util.EnumHand;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.util.math.Vec3d;
import org.lwjgl.Sys;
import org.lwjgl.input.Mouse;

import java.util.Arrays;

/**
 * @author Hoosiers, Doogie13
 * @since 04/08/2021
 */
@Module.Declaration(name = "MCP", category = Category.Misc)
public class MCP extends Module {

    ModeSetting button = registerMode("Button", Arrays.asList("MOUSE3", "MOUSE4", "MOUSE5"), "MOUSE3");
    BooleanSetting clipRotate = registerBoolean("clipRotate", false);
    IntegerSetting pearlPitch = registerInteger("Pitch", 85, -90, 90);

    public void onUpdate() {

        float pearlPitchFloat = pearlPitch.getValue().floatValue();

        int MCPButtonCode;
        if (button.getValue().equalsIgnoreCase("MOUSE3")) {
            MCPButtonCode = 2; //Mouse3 (used for MCF so using this will retard your gameplay)
        } else if (button.getValue().equalsIgnoreCase("MOUSE4")) {
            MCPButtonCode = 3; //Mouse4
        } else if (button.getValue().equalsIgnoreCase("MOUSE5")) {
            MCPButtonCode = 4; //Mouse5
        } else {
            MCPButtonCode = 2; //User Error Protection
        }

        if (Mouse.isButtonDown(MCPButtonCode)) { //We check for button press and don't check for miss :rage:
            int oldSlot = mc.player.inventory.currentItem;

            int pearlSlot = InventoryUtil.findFirstItemSlot(ItemEnderPearl.class, 0, 8);

            if (pearlSlot != -1) {

                mc.player.inventory.currentItem = pearlSlot;

                if (clipRotate.getValue()){

                    //Get old yaw so we can return it back to before as sometimes glitches idk

                    float oldYaw = mc.player.rotationYaw;
                    float oldPitch = mc.player.rotationPitch;

                    //ROUNDING
                    float yawRounded;

                    float yaw = Math.abs(Math.round(mc.player.rotationYaw) % 360);
                    float division = (int) Math.floor(yaw / 45);
                    float remainder = (int) (yaw % 45);
                    if (remainder < 45 / 2) {
                        yawRounded = 45 * division;
                    } else {
                        yawRounded = 45 * (division + 1);
                    }
                    //END OF ROUNDING

                    mc.player.connection.sendPacket(new CPacketPlayer.Rotation(yawRounded, pearlPitchFloat, true)); // rotate for phasing
                    mc.playerController.processRightClick(mc.player, mc.world, EnumHand.MAIN_HAND); // Throw the pearl (thanks TechAle :D)

                    mc.player.connection.sendPacket(new CPacketPlayer.Rotation(oldYaw, oldPitch, true)); // rotate back
                    mc.player.inventory.currentItem = oldSlot; // return to old slot
                }else{ // same as Hoosiers' code previous code
                    mc.playerController.processRightClick(mc.player, mc.world, EnumHand.MAIN_HAND);
                    mc.player.inventory.currentItem = oldSlot;
                }
            }
        }
    }
}