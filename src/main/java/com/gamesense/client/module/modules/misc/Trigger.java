package com.gamesense.client.module.modules.misc;


import com.gamesense.api.setting.values.BooleanSetting;
import com.gamesense.api.setting.values.DoubleSetting;
import com.gamesense.api.setting.values.IntegerSetting;
import com.gamesense.api.util.misc.MessageBus;
import com.gamesense.api.util.misc.Timer;
import com.gamesense.api.util.player.PlayerUtil;
import com.gamesense.api.util.world.HoleUtil;
import com.gamesense.client.manager.managers.TotemPopManager;
import com.gamesense.client.module.Category;
import com.gamesense.client.module.Module;
import com.gamesense.client.module.ModuleManager;
import com.gamesense.client.module.modules.combat.FootConcrete;
import com.gamesense.client.module.modules.combat.Surround;
import net.minecraft.util.math.BlockPos;

@Module.Declaration(name = "Trigger", category = Category.Misc)
public class Trigger extends Module {

    BooleanSetting SurroundSettings = registerBoolean("Surround Settings", false);

    BooleanSetting SurroundInHole = registerBoolean("Surround in Hole", true, () -> SurroundSettings.getValue());
    IntegerSetting SIHTicks = registerInteger("Hole Delay", 1,0,20,() -> SurroundInHole.getValue() && SurroundSettings.getValue());
    BooleanSetting SurroundOnPop = registerBoolean("Surround on Pop", false, () -> SurroundSettings.getValue());

    BooleanSetting FootConcSettings = registerBoolean("Foot Concrete Settings", false);

    BooleanSetting NearbyBurrow = registerBoolean("Auto Clutch Burrow", true, () -> FootConcSettings.getValue());
    DoubleSetting NBRadius = registerDouble("Scan Radius", 2,0,10, () -> FootConcSettings.getValue() && NearbyBurrow.getValue()); // if you use 10 you deserve the death sentence
    BooleanSetting CCRot = registerBoolean("No Rotate On .cc", true);

    Surround srnd = ModuleManager.getModule(Surround.class);
    FootConcrete footConc = ModuleManager.getModule(FootConcrete.class);

    int pops;

    public boolean cc;

    Timer SIHTicksTimer = new Timer();

    @Override
    protected void onEnable() {
        pops = TotemPopManager.INSTANCE.getPops();
    }

    @Override
    public void onUpdate() {

        /* Surround */
        if (!(HoleUtil.isHole(new BlockPos(mc.player.posX,mc.player.posY-1,mc.player.posZ),true, true).getType().equals(HoleUtil.HoleType.NONE)) && SurroundInHole.getValue() && SIHTicksTimer.hasReached(SIHTicks.getValue()*50,true) && !srnd.isEnabled()) {
            // IF mc.player is in hole, enable surround after SIHTicks * 50 and reset the timer, if in hole, it wont turn on repeatedly
            if (!srnd.isEnabled()){
                srnd.enable();
            }

        }

        if (TotemPopManager.INSTANCE.getPops() > pops && SurroundOnPop.getValue() && srnd.isEnabled()) {
            // IF we pop whilst surround is disabled, we enable surround
            pops = TotemPopManager.INSTANCE.getPops();

            if (!srnd.isEnabled()){
                srnd.enable();
            }

        }

        /* FootConcrete */

        if (NearbyBurrow.getValue() && PlayerUtil.findClosestTarget(NBRadius.getValue(),null) != null) {

            footConc.enable(); // time for some 2b2tpvp.net fun :troll:

        }

        try {
            if ((CCRot.getValue() && (mc.serverName.equalsIgnoreCase("crystalpvp.cc") || (mc.serverName.equalsIgnoreCase("us.crystalpvp.cc")))) == true) {

                cc = true;


            } else {

                cc = false;

            }
        } finally {

            MessageBus.sendClientPrefixMessage("Null exception caught from Trigger > 'No Rotate On .cc'");

        }



    }
}
