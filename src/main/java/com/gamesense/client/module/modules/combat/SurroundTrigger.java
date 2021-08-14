package com.gamesense.client.module.modules.combat;


import com.gamesense.api.setting.values.BooleanSetting;
import com.gamesense.api.setting.values.IntegerSetting;
import com.gamesense.api.util.misc.Timer;
import com.gamesense.api.util.world.HoleUtil;
import com.gamesense.client.manager.managers.TotemPopManager;
import com.gamesense.client.module.Category;
import com.gamesense.client.module.Module;
import com.gamesense.client.module.ModuleManager;
import com.sun.org.apache.xpath.internal.operations.Bool;
import net.minecraft.util.math.BlockPos;

@Module.Declaration(name = "Surround Trigger", category = Category.Combat)
public class SurroundTrigger extends Module {

    BooleanSetting SurroundInHole = registerBoolean("Surround in Hole", true);
    IntegerSetting SIHTicks = registerInteger("Hole Delay", 1,0,20);
    BooleanSetting SurroundOnPop = registerBoolean("Surround on Pop", false);

    Surround srnd = ModuleManager.getModule(Surround.class);
    int pops;

    Timer SIHTicksTimer = new Timer();

    @Override
    protected void onEnable() {
        pops = TotemPopManager.INSTANCE.getPops();
    }

    @Override
    public void onUpdate() {
        if (!(HoleUtil.isHole(new BlockPos(mc.player.posX,mc.player.posY-1,mc.player.posZ),true, true).getType().equals("None")) && SurroundInHole.getValue() && SIHTicksTimer.hasReached(SIHTicks.getValue()*50)) {

            if (!srnd.isEnabled()){
                srnd.enable();
            }

        }

        if (TotemPopManager.INSTANCE.getPops() > pops && SurroundOnPop.getValue()) {

            pops = TotemPopManager.INSTANCE.getPops();

            if (!srnd.isEnabled()){
                srnd.enable();
            }

        }
    }
}
