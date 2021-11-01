package com.gamesense.api.util.player.social;

import com.gamesense.client.module.modules.combat.Friends;
import com.gamesense.client.module.modules.combat.PistonCrystal;

import java.util.ArrayList;
import java.util.stream.Collectors;

public class SocialManager {

    private static final ArrayList<Friend> friends = new ArrayList<>();
    private static final ArrayList<Enemy> enemies = new ArrayList<>();
    private static final ArrayList<SpecialNames> SpecialNames = new ArrayList<>();

    public static ArrayList<Friend> getFriends() {
        return friends;
    }

    public static ArrayList<Enemy> getEnemies() {
        return enemies;
    }

    public static ArrayList<SpecialNames> getSpecialNames() {
        return SpecialNames;
    }

    public static ArrayList<String> getFriendsByName() {
        ArrayList<String> friendNames = new ArrayList<>();

        getFriends().forEach(friend -> friendNames.add(friend.getName()));
        return friendNames;
    }

    public static ArrayList<String> getEnemiesByName() {
        ArrayList<String> enemyNames = new ArrayList<>();

        getEnemies().forEach(enemy -> enemyNames.add(enemy.getName()));
        return enemyNames;
    }

    public static boolean isFriend(String name) {

        for (Friend friend : getFriends()) {
            if (friend.getName().equalsIgnoreCase(name) && Friends.INSTANCE.isEnabled()) {
                return true;
            }
        }

        return false;
    }

    public static boolean isEnemy(String name) {

        for (Enemy enemy : getEnemies()) {
            if (enemy.getName().equalsIgnoreCase(name)) {
                return true;
            }
        }

        return false;
    }

    public static boolean isSpecial(String name) {

        for (SpecialNames enemy : getSpecialNames()) {
            if (enemy.getName().equalsIgnoreCase(name)) {
                return true;
            }
        }

        return false;
    }

    public static Friend getFriend(String name) {

        for(Friend friend : getFriends())
            if (friend.getName().equalsIgnoreCase(name))
                return friend;
        return null;
    }

    public static Enemy getEnemy(String name) {

        for(Enemy enemy : getEnemies())
            if (enemy.getName().equalsIgnoreCase(name))
                return enemy;
        return null;

    }

    public static SpecialNames getSpecialNames(String name) {
        for(SpecialNames specialNames : getSpecialNames())
            if (specialNames.getName().equalsIgnoreCase(name))
                return specialNames;
        return null;
    }

    public static ArrayList<String> getSpecialNamesString() {
        ArrayList<String> out = new ArrayList<>();
        try {
            getSpecialNames().forEach(name -> out.add(name.getName()));
        }catch (OutOfMemoryError ignored) {

        }
        return out;
    }

    public static void addFriend(String name) {
        getFriends().add(new Friend(name));
    }

    public static void delFriend(String name) {
        getFriends().remove(getFriend(name));
    }

    public static void addEnemy(String name) {
        getEnemies().add(new Enemy(name));
    }

    public static void delEnemy(String name) {
        getEnemies().remove(getEnemy(name));
    }

    public static void delSpecial(String name) {
        getSpecialNames().remove(getSpecialNames(name));
    }

    public static void addSpecialName(String name) {
        getSpecialNames().add(new SpecialNames(name));
    }

    public static void removeSpecialName(String name) {
        getSpecialNames().remove(getSpecialNames(name));
    }
}