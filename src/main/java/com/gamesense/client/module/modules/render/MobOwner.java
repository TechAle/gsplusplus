package com.gamesense.client.module.modules.render;

import com.gamesense.api.util.render.GSColor;
import com.gamesense.api.util.render.RenderUtil;
import com.gamesense.client.module.Category;
import com.gamesense.client.module.Module;
import net.minecraft.entity.Entity;
import net.minecraft.entity.IEntityOwnable;
import net.minecraft.entity.passive.AbstractHorse;

import java.awt.*;
import java.util.Objects;

@Module.Declaration(name = "MobOwner", category = Category.Render)
public class MobOwner extends Module {

    @Override
    public void onUpdate() {
        for (Entity e : mc.world.loadedEntityList) {
            if (e instanceof IEntityOwnable) {
                if (!(e instanceof AbstractHorse)){
                    try {
                        RenderUtil.drawNametag(e, new String[]{Objects.requireNonNull(((IEntityOwnable) e).getOwner()).getName() + ""}, new GSColor(Color.WHITE), 1);
                    } catch (NullPointerException ignored) {}

                } else {

                    StringBuilder string = new StringBuilder();

                    string.append("Name: ");
                    string.append(e.getCustomNameTag());
                    string.append(", Owner: ");
                    string.append(((Objects.requireNonNull(((IEntityOwnable) e).getOwner())).getName()));
                    string.append(", Speed: ");
                    string.append(((AbstractHorse) e).getAIMoveSpeed());

                    RenderUtil.drawNametag(e, new String[]{string.toString()}, new GSColor(Color.WHITE), 1);

                }
            }
        }
    }
}
