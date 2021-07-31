package com.gamesense.client.module.modules.render;

import com.gamesense.api.event.events.TransformSideFirstPersonEvent;
import com.gamesense.api.setting.values.BooleanSetting;
import com.gamesense.api.setting.values.DoubleSetting;
import com.gamesense.api.setting.values.IntegerSetting;
import com.gamesense.api.setting.values.ModeSetting;
import com.gamesense.client.module.Category;
import com.gamesense.client.module.Module;
import com.gamesense.client.module.ModuleManager;
import com.gamesense.client.module.modules.combat.KillAura;
import me.zero.alpine.listener.EventHandler;
import me.zero.alpine.listener.Listener;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.util.EnumHandSide;
import net.minecraftforge.client.event.EntityViewRenderEvent;

import java.util.Arrays;

import static org.lwjgl.opengl.GL11.glRotatef;
import static org.lwjgl.opengl.GL11.glTranslatef;

/**
 * @author GL_DONT_CARE (Viewmodel Transformations)
 * @author NekoPvP (Item FOV)
 */

@Module.Declaration(name = "ViewModel", category = Category.Render)
public class ViewModel extends Module {

    ModeSetting type = registerMode("Type", Arrays.asList("Value", "FOV", "Both"), "Value");
    public BooleanSetting cancelEating = registerBoolean("No Eat", false);
    DoubleSetting xLeft = registerDouble("Left X", 0.0, -2.0, 2.0);
    DoubleSetting yLeft = registerDouble("Left Y", 0.2, -2.0, 2.0);
    DoubleSetting zLeft = registerDouble("Left Z", -1.2, -2.0, 2.0);
    DoubleSetting xScaleLeft = registerDouble("Left X Scale", 1, 0, 3);
    DoubleSetting yScaleLeft = registerDouble("Left Y Scale", 1, 0, 3);
    DoubleSetting zScaleLeft = registerDouble("Left Z Scale", 1, 0, 3);
    IntegerSetting xLeftRotate = registerInteger("Left X Rotate", 0, 0, 180);
    IntegerSetting yLeftRotate = registerInteger("Left Y Rotate", 0, 0, 180);
    IntegerSetting zLeftRotate = registerInteger("Left Z Rotate", 0, 0, 180);
    public DoubleSetting xRight = registerDouble("Right X", 0.0, -2.0, 2.0);
    public DoubleSetting yRight = registerDouble("Right Y", 0.2, -2.0, 2.0);
    public DoubleSetting zRight = registerDouble("Right Z", -1.2, -2.0, 2.0);
    public IntegerSetting xRightRotate = registerInteger("Right X Rotate", 0, 0, 360);
    public IntegerSetting yRightRotate = registerInteger("Right Y Rotate", 0, 0, 360);
    public IntegerSetting zRightRotate = registerInteger("Right Z Rotate", 0, 0, 360);
    public DoubleSetting xScaleRight = registerDouble("Right X Scale", 1, 0, 3);
    public DoubleSetting yScaleRight = registerDouble("Right Y Scale", 1, 0, 3);
    public DoubleSetting zScaleRight = registerDouble("Right Z Scale", 1, 0, 3);
    DoubleSetting fov = registerDouble("Item FOV", 130, 70, 200);

    KillAura ka = ModuleManager.getModule(KillAura.class);

    @SuppressWarnings("unused")
    @EventHandler
    private final Listener<TransformSideFirstPersonEvent> eventListener = new Listener<>(event -> {
        if (type.getValue().equalsIgnoreCase("Value") || type.getValue().equalsIgnoreCase("Both")) {
            if (event.getEnumHandSide() == EnumHandSide.RIGHT) {
                if (!ka.animation.getValue()){
                    GlStateManager.scale(xScaleRight.getValue(), yScaleRight.getValue(), zScaleRight.getValue());
                }
                glRotatef(xRightRotate.getValue(), 1, 0, 0);
                glRotatef(yRightRotate.getValue(), 0, 1, 0);
                glRotatef(zRightRotate.getValue(), 0, 0, 1);
                if (!ka.animation.getValue()){
                    GlStateManager.translate(xRight.getValue(), yRight.getValue(), zRight.getValue());
                }
            } else if (event.getEnumHandSide() == EnumHandSide.LEFT) {
                GlStateManager.scale(xScaleLeft.getValue(), yScaleLeft.getValue(), zScaleLeft.getValue());
                GlStateManager.translate(xLeft.getValue(), yLeft.getValue(), zLeft.getValue());
                GlStateManager.rotate(xLeftRotate.getValue(), 1, 0, 0);
                GlStateManager.rotate(yLeftRotate.getValue(), 0, 1, 0);
                GlStateManager.rotate(zLeftRotate.getValue(), 0, 0, 1);
            }
        }
    });

    @SuppressWarnings("unused")
    @EventHandler
    private final Listener<EntityViewRenderEvent.FOVModifier> fovModifierListener = new Listener<>(event -> {
        if (type.getValue().equalsIgnoreCase("FOV") || type.getValue().equalsIgnoreCase("Both")) {
            event.setFOV(fov.getValue().floatValue());
        }
    });
}