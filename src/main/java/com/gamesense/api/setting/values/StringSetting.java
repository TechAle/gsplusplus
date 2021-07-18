package com.gamesense.api.setting.values;

import com.gamesense.api.setting.Setting;
import com.gamesense.client.module.Module;

public class StringSetting extends Setting<String> {

    private String text;

    public StringSetting(String name, Module parent, String text) {
        super(text, name, parent);
        this.text = text;
    }

    public String getText() {
        return text;
    }

    public void setText(String text) {
        this.text = text;
    }
}