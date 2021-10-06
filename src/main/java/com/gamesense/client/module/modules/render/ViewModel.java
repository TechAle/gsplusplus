package com.gamesense.client.module.modules.render;

import com.gamesense.api.event.events.TransformSideFirstPersonEvent;
import com.gamesense.api.setting.values.BooleanSetting;
import com.gamesense.api.setting.values.DoubleSetting;
import com.gamesense.api.setting.values.IntegerSetting;
import com.gamesense.api.setting.values.ModeSetting;
import com.gamesense.client.module.Category;
import com.gamesense.client.module.Module;
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

    public BooleanSetting cancelEating = registerBoolean("No Eat", false);
    public BooleanSetting EatSection = registerBoolean("Eat Section", false);
    public DoubleSetting xEat = registerDouble("Eat X", 0.0, -2.0, 2.0, () -> EatSection.getValue());
    public DoubleSetting yEat = registerDouble("Eat Y", 0.2, -2.0, 2.0, () -> EatSection.getValue());
    public DoubleSetting zEat = registerDouble("Eat Z", -1.2, -2.0, 2.0, () -> EatSection.getValue());
    public DoubleSetting xScaleEat = registerDouble("Eat X Scale", 1, 0, 3, () -> EatSection.getValue());
    public DoubleSetting yScaleEat = registerDouble("Eat Y Scale", 1, 0, 3, () -> EatSection.getValue());
    public DoubleSetting zScaleEat = registerDouble("Eat Z Scale", 1, 0, 3, () -> EatSection.getValue());
    public IntegerSetting xEatRotate = registerInteger("Eat X Rotate", 0, 0, 180, () -> EatSection.getValue());
    public IntegerSetting yEatRotate = registerInteger("Eat Y Rotate", 0, 0, 180, () -> EatSection.getValue());
    public IntegerSetting zEatRotate = registerInteger("Eat Z Rotate", 0, 0, 180, () -> EatSection.getValue());
    public BooleanSetting cancelStandardBow = registerBoolean("Cancel Standard Bow", true);
    public BooleanSetting hand = registerBoolean("Hand Section", false);
    public DoubleSetting offX = registerDouble("OffhandX", 0.0, -1.0, 1.0, () -> hand.getValue());
    public DoubleSetting offY = registerDouble("OffhandY", 0.0, -1.0, 1.0, () -> hand.getValue());
    public DoubleSetting mainX = registerDouble("MainhandX", 0.0, -1.0, 1.0, () -> hand.getValue());
    public DoubleSetting mainY = registerDouble("MainhandY", 0.0, -1.0, 1.0, () -> hand.getValue());
    BooleanSetting leftSection = registerBoolean("Left Section", false);
    DoubleSetting xLeft = registerDouble("Left X", 0.0, -2.0, 2.0, () -> leftSection.getValue());
    DoubleSetting yLeft = registerDouble("Left Y", 0.2, -2.0, 2.0, () -> leftSection.getValue());
    DoubleSetting zLeft = registerDouble("Left Z", -1.2, -2.0, 2.0, () -> leftSection.getValue());
    DoubleSetting xScaleLeft = registerDouble("Left X Scale", 1, 0, 3, () -> leftSection.getValue());
    DoubleSetting yScaleLeft = registerDouble("Left Y Scale", 1, 0, 3, () -> leftSection.getValue());
    DoubleSetting zScaleLeft = registerDouble("Left Z Scale", 1, 0, 3, () -> leftSection.getValue());
    IntegerSetting xLeftRotate = registerInteger("Left X Rotate", 0, 0, 180, () -> leftSection.getValue());
    IntegerSetting yLeftRotate = registerInteger("Left Y Rotate", 0, 0, 180, () -> leftSection.getValue());
    IntegerSetting zLeftRotate = registerInteger("Left Z Rotate", 0, 0, 180, () -> leftSection.getValue());
    BooleanSetting rightSection = registerBoolean("Right Section", false);
    DoubleSetting xRight = registerDouble("Right X", 0.0, -5.0, 2.0, () -> rightSection.getValue());
    DoubleSetting yRight = registerDouble("Right Y", 0.2, -2.0, 5.0, () -> rightSection.getValue());
    DoubleSetting zRight = registerDouble("Right Z", -1.2, -5.0, 2.0, () -> rightSection.getValue());
    IntegerSetting xRightRotate = registerInteger("Right X Rotate", 0, 0, 360, () -> rightSection.getValue());
    IntegerSetting yRightRotate = registerInteger("Right Y Rotate", 0, 0, 360, () -> rightSection.getValue());
    IntegerSetting zRightRotate = registerInteger("Right Z Rotate", 0, 0, 360, () -> rightSection.getValue());
    DoubleSetting xScaleRight = registerDouble("Right X Scale", 1, 0, 3, () -> rightSection.getValue());
    DoubleSetting yScaleRight = registerDouble("Right Y Scale", 1, 0, 3, () -> rightSection.getValue());
    DoubleSetting zScaleRight = registerDouble("Right Z Scale", 1, 0, 3, () -> rightSection.getValue());
    BooleanSetting fovEnabled = registerBoolean("Enable Fov", false);
    DoubleSetting fov = registerDouble("Item FOV", 130, 70, 200, () -> fovEnabled.getValue());
    BooleanSetting animations = registerBoolean("Animations", false);
    BooleanSetting xLeftAnimation = registerBoolean("X Left Animation", false, () -> animations.getValue());
    BooleanSetting yLeftAnimation = registerBoolean("Y Left Animation", false, () -> animations.getValue());
    BooleanSetting zLeftAnimation = registerBoolean("Z Left Animation", false, () -> animations.getValue());
    BooleanSetting xRightAnimation = registerBoolean("X Right Animation", false, () -> animations.getValue());
    BooleanSetting yRightAnimation = registerBoolean("Y Right Animation", false, () -> animations.getValue());
    BooleanSetting zRightAnimation = registerBoolean("Z Right Animation", false, () -> animations.getValue());



    int xLeftAnimationCount = 0,
        yLeftAnimationCount = 0,
        zLeftAnimationCount = 0,
        xRightAnimationCount = 0,
        yRightAnimationCount = 0,
        zRightAnimationCount = 0;

    @SuppressWarnings("unused")
    @EventHandler
    private final Listener<TransformSideFirstPersonEvent> eventListener = new Listener<>(event -> {
        GlStateManager.popMatrix();
        if (event.getEnumHandSide() == EnumHandSide.RIGHT) {
            GlStateManager.translate(xRight.getValue(), yRight.getValue(), zRight.getValue());
            if (xRightAnimation.getValue())
                glRotatef(++xRightAnimationCount, 1, 0, 0);
            else
                glRotatef(xRightRotate.getValue(), 1, 0, 0);
            if (yRightAnimation.getValue())
                glRotatef(++yRightAnimationCount, 0, 1, 0);
            else
                glRotatef(yRightRotate.getValue(), 0, 1, 0);
            if (zRightAnimation.getValue())
                glRotatef(++zRightAnimationCount, 0, 0, 1);
            else
                glRotatef(zRightRotate.getValue(), 0, 0, 1);
            GlStateManager.scale(xScaleRight.getValue(), yScaleRight.getValue(), zScaleRight.getValue());
        } else if (event.getEnumHandSide() == EnumHandSide.LEFT) {
            GlStateManager.translate(xLeft.getValue(), yLeft.getValue(), zLeft.getValue());
            if (xLeftAnimation.getValue())
                glRotatef(++xLeftAnimationCount, 1, 0, 0);
            else
                glRotatef(xLeftRotate.getValue(), 1, 0, 0);
            if (yLeftAnimation.getValue())
                glRotatef(++yLeftAnimationCount, 0, 1, 0);
            else
                glRotatef(yLeftRotate.getValue(), 0, 1, 0);
            if (zLeftAnimation.getValue())
                glRotatef(++zLeftAnimationCount, 0, 0, 1);
            else
                glRotatef(zLeftRotate.getValue(), 0, 0, 1);
            GlStateManager.scale(xScaleLeft.getValue(), yScaleLeft.getValue(), zScaleLeft.getValue());
        }

        xLeftAnimationCount = xLeftAnimationCount % 360;
        yLeftAnimationCount = yLeftAnimationCount % 360;
        zLeftAnimationCount = zLeftAnimationCount % 360;
        xRightAnimationCount = xRightAnimationCount % 360;
        yRightAnimationCount = yRightAnimationCount % 360;
        zRightAnimationCount = zRightAnimationCount % 360;


    });

    @SuppressWarnings("unused")
    @EventHandler
    private final Listener<EntityViewRenderEvent.FOVModifier> fovModifierListener = new Listener<>(event -> {
        if (fovEnabled.getValue())
            event.setFOV(fov.getValue().floatValue());
    });
}