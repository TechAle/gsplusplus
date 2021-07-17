package com.gamesense.client.clickgui;

import com.lukflug.panelstudio.widget.ITextFieldKeys;
import org.lwjgl.input.Keyboard;

public class TextFieldKeys implements ITextFieldKeys {
    @Override
    public boolean isBackspaceKey(int scancode) {
        return scancode == Keyboard.KEY_BACK;
    }

    @Override
    public boolean isDeleteKey(int scancode) {
        return scancode == Keyboard.KEY_DELETE;
    }

    @Override
    public boolean isInsertKey(int scancode) {
        return scancode == Keyboard.KEY_INSERT;
    }

    @Override
    public boolean isLeftKey(int scancode) {
        return scancode == Keyboard.KEY_LEFT;
    }

    @Override
    public boolean isRightKey(int scancode) {
        return scancode == Keyboard.KEY_RIGHT;
    }

    @Override
    public boolean isHomeKey(int scancode) {
        return scancode == Keyboard.KEY_HOME;
    }

    @Override
    public boolean isEndKey(int scancode) {
        return scancode == Keyboard.KEY_END;
    }

    @Override
    public boolean isCopyKey(int scancode) {
        return scancode == Keyboard.KEY_C;
    }

    @Override
    public boolean isPasteKey(int scancode) {
        return scancode == Keyboard.KEY_V;
    }

    @Override
    public boolean isCutKey(int scancode) {
        return scancode == Keyboard.KEY_X;
    }

    @Override
    public boolean isAllKey(int scancode) {
        return scancode == Keyboard.KEY_A;
    }
}