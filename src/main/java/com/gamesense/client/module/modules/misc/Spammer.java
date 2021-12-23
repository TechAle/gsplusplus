package com.gamesense.client.module.modules.misc;

import com.gamesense.api.setting.values.BooleanSetting;
import com.gamesense.api.setting.values.IntegerSetting;
import com.gamesense.api.setting.values.StringSetting;
import com.gamesense.api.util.misc.MessageBus;
import com.gamesense.client.GameSense;
import com.gamesense.client.module.Category;
import com.gamesense.client.module.Module;

@Module.Declaration(name = "Spammer", category = Category.Misc)
public class Spammer extends Module {

    StringSetting string = registerString("Message", "$VER on top!");
    BooleanSetting green = registerBoolean("Green Text", true);
    BooleanSetting fancy = registerBoolean("Fancy", false);
    BooleanSetting bypass = registerBoolean("Bypass", true);
    IntegerSetting delay = registerInteger("Speed", 10, 1, 20);

    int i = 0;

    String str;

    @Override
    public void onUpdate() {

        str = string.getValue()
                .replace("$VER", GameSense.MODID + " " + GameSense.MODVER)
                .replace("$NAME", mc.player.getName());

        if (green.getValue())
            str = "> " + str;

        if (fancy.getValue())
            str = toUnicode(str);

        if (bypass.getValue())
            str = str + " " + (int) (Math.random() * 10000);



        if (i == delay.getValue()) {
            MessageBus.sendServerMessage(str);
            i = 0;
        } else
            i++;
    }

    private String toUnicode(String s) {
        return s.toLowerCase()
                .replace("a", "\u1d00")
                .replace("b", "\u0299")
                .replace("c", "\u1d04")
                .replace("d", "\u1d05")
                .replace("e", "\u1d07")
                .replace("f", "\ua730")
                .replace("g", "\u0262")
                .replace("h", "\u029c")
                .replace("i", "\u026a")
                .replace("j", "\u1d0a")
                .replace("k", "\u1d0b")
                .replace("l", "\u029f")
                .replace("m", "\u1d0d")
                .replace("n", "\u0274")
                .replace("o", "\u1d0f")
                .replace("p", "\u1d18")
                .replace("q", "\u01eb")
                .replace("r", "\u0280")
                .replace("s", "\ua731")
                .replace("t", "\u1d1b")
                .replace("u", "\u1d1c")
                .replace("v", "\u1d20")
                .replace("w", "\u1d21")
                .replace("x", "\u02e3")
                .replace("y", "\u028f")
                .replace("z", "\u1d22");
    }

}
