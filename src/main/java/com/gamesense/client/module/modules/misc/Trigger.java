package com.gamesense.client.module.modules.misc;

import com.gamesense.api.event.events.TotemPopEvent;
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
import jdk.nashorn.internal.ir.Block;
import me.zero.alpine.listener.EventHandler;
import me.zero.alpine.listener.Listener;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.math.AxisAlignedBB;
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
        BlockPos player = new BlockPos(mc.player.posX,mc.player.posY,mc.player.posZ);

        /* Surround */
        if (!HoleUtil.isHole(mc.player.getPosition().down(), false, false).getType().equals(HoleUtil.HoleType.NONE) && SurroundInHole.getValue() && SIHTicksTimer.hasReached(SIHTicks.getValue()*50,true) && !srnd.isEnabled()) {
            // IF mc.player is in hole, enable surround after SIHTicks * 50 and reset the timer, if in hole, it wont turn on repeatedly
            if (!srnd.isEnabled()){
                srnd.enable();
            }

        }

        /* FootConcrete */

        if (NearbyBurrow.getValue() && PlayerUtil.findClosestTarget(NBRadius.getValue(),null) != null && (new AxisAlignedBB(player)).intersects(mc.player.getEntityBoundingBox())) {

            footConc.enable(); // time for some 2b2tpvp.net fun :troll:

        }
    }

    //totem pop event = surround if
    @EventHandler
    public Listener<TotemPopEvent> totemPopEvent = new Listener<>(event -> {
        if (SurroundOnPop.getValue()) {
            EntityPlayer entity = (EntityPlayer) event.getEntity();
            if (mc.player == entity) {
                if (!ModuleManager.isModuleEnabled("Surround")) ModuleManager.getModule(Surround.class).toggle();
            }
        }
    });

}
