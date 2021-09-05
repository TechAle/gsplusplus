package com.gamesense.api.util.misc;

import org.lwjgl.input.Keyboard;

public class KeyBoardClass {

    public static int getKeyFromChar(char character) {
        character = Character.toUpperCase(character);
        switch (character) {
            case 'A':
                return Keyboard.KEY_A;
            case 'B':
                return Keyboard.KEY_B;
            case 'C':
                return Keyboard.KEY_C;
            case 'D':
                return Keyboard.KEY_D;
            case 'E':
                return Keyboard.KEY_E;
            case 'F':
                return Keyboard.KEY_F;
            case 'G':
                return Keyboard.KEY_G;
            case 'H':
                return Keyboard.KEY_H;
            case 'I':
                return Keyboard.KEY_I;
            case 'L':
                return Keyboard.KEY_L;
            case 'M':
                return Keyboard.KEY_M;
            case 'N':
                return Keyboard.KEY_N;
            case 'O':
                return Keyboard.KEY_O;
            case 'P':
                return Keyboard.KEY_P;
            case 'Q':
                return Keyboard.KEY_Q;
            case 'R':
                return Keyboard.KEY_R;
            case 'S':
                return Keyboard.KEY_S;
            case 'T':
                return Keyboard.KEY_T;
            case 'U':
                return Keyboard.KEY_U;
            case 'V':
                return Keyboard.KEY_V;
            case 'W':
                return Keyboard.KEY_W;
            case 'X':
                return Keyboard.KEY_X;
            case 'Y':
                return Keyboard.KEY_Y;
            case 'Z':
                return Keyboard.KEY_Z;
            case '0':
                return Keyboard.KEY_0;
            case '1':
                return Keyboard.KEY_1;
            case '2':
                return Keyboard.KEY_2;
            case '3':
                return Keyboard.KEY_3;
            case '4':
                return Keyboard.KEY_4;
            case '5':
                return Keyboard.KEY_5;
            case '6':
                return Keyboard.KEY_6;
            case '7':
                return Keyboard.KEY_7;
            case '8':
                return Keyboard.KEY_8;
            case '9':
                return Keyboard.KEY_9;
            case '.':
                return Keyboard.KEY_COMMA;
            case ',':
                return Keyboard.KEY_COLON;
            case '\\':
                return Keyboard.KEY_BACKSLASH;
            case '\'':
                return Keyboard.KEY_APOSTROPHE;
            case '-':
                return Keyboard.KEY_MINUS;
        }
        return 0;
    }

}
