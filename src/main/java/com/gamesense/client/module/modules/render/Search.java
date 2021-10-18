package com.gamesense.client.module.modules.render;

import com.gamesense.api.event.events.RenderEvent;
import com.gamesense.api.setting.values.IntegerSetting;
import com.gamesense.api.setting.values.ModeSetting;
import com.gamesense.api.util.render.GSColor;
import com.gamesense.api.util.render.RenderUtil;
import com.gamesense.api.util.world.GeometryMasks;
import com.gamesense.client.module.Category;
import com.gamesense.client.module.Module;
import net.minecraft.block.Block;
import net.minecraft.init.Blocks;
import net.minecraft.util.math.BlockPos;

import java.util.*;

@Module.Declaration(name = "Search", category = Category.Render)
public class Search extends Module {

    ModeSetting renderType = registerMode("Render", Arrays.asList("Outline", "Fill", "Both"), "Both");
    IntegerSetting fill = registerInteger("Fill Alpha", 128,0,255);
    IntegerSetting outline = registerInteger("Outline Alpha", 255,0,255, () -> !renderType.getValue().equalsIgnoreCase("Fill"));
    IntegerSetting width = registerInteger("Width", 1, 1, 10, () -> !renderType.getValue().equalsIgnoreCase("Fill"));

    public static Search INSTANCE;
    public static Block[] shulkCollection = new Block[]{
            Block.getBlockById(219),
            Block.getBlockById(220),
            Block.getBlockById(221),
            Block.getBlockById(222),
            Block.getBlockById(223),
            Block.getBlockById(224),
            Block.getBlockById(225)};
    private static List<Integer> blocks = new ArrayList<>();

    public Search() {
        INSTANCE = this;
    }

    public static List<Integer> getBlocks() {

        return blocks;

    }

    public static boolean addCollection(Block[] collection) {

        try {

            for (Block block : collection)
                addBlock(block);

            return true;
        }
        catch (NullPointerException ignored) {
            return false;
        }
    }

    public static boolean removeCollection(Block[] collection) {

        try {

            for (Block block : collection)
                removeBlock(block);

            return true;
        }
        catch (NullPointerException ignored) {
            return false;
        }
    }

    public static boolean addBlock(Block block) {

        try {
            ArrayList<Integer> instances = new ArrayList<>(); // we remove all existing instances and add back

            for (int i = 0; i < blocks.size(); i++) {
                if (blocks.get(i).equals(Block.getIdFromBlock(block)))
                    instances.add(i);
            }

            if (instances.size() >= 1) {
                blocks.subList(1, instances.size() + 1).clear();
            }

            blocks.add(Block.getIdFromBlock(block));

            return true;

        } catch (NullPointerException ignored) {
            return false;
        }
    }

    public static boolean removeBlock(Block block) {

        try {
            ArrayList<Integer> instances = new ArrayList<>();

            for (int i = 0; i < blocks.size(); i++) {
                if (blocks.get(i).equals(Block.getIdFromBlock(block)))
                    instances.add(i);
            }

            if (instances.size() >= 1) {
                blocks.subList(1, instances.size() + 1).clear();
            }
            return true;
        } catch (NullPointerException ignored) {
            return false;
        }

    }

    @Override
    public void onWorldRender(RenderEvent event) {
        mc.world.loadedTileEntityList.forEach(tileEntity -> {
            if (getBlocks().contains(Block.getIdFromBlock(tileEntity.getBlockType()))) {

                BlockPos blockPos = tileEntity.getPos();

                switch (renderType.getValue()) {
                    case "Outline": {
                        RenderUtil.drawBoundingBox(blockPos, 1, width.getValue(), getColour(tileEntity.getBlockType(), true));
                        break;
                    }
                    case "Fill": {
                        RenderUtil.drawBox(blockPos, 1, getColour(tileEntity.getBlockType(), false), GeometryMasks.Quad.ALL);
                        break;
                    }
                    default: {
                        RenderUtil.drawBox(blockPos, 1, getColour(tileEntity.getBlockType(),false), GeometryMasks.Quad.ALL);
                        RenderUtil.drawBoundingBox(blockPos, 1, width.getValue(), getColour(tileEntity.getBlockType(), true));
                        break;
                    }
                }
            }

        });

    }

    public static Block[] getCollection(String name) {

        switch (name) { // this will be more useful when more collections are added, rn its just shulkers for testing

            case "shulker":
            case "shulk":
            case "shulkerbox":
                return shulkCollection;

        }

        return null;

    }

    public static String colList() {

        ArrayList<String> l = new ArrayList<>();
        StringBuilder out = new StringBuilder();

        // DECLARE COLLECTIONS HERE ALSO
        l.add("Shulker");

        for (int i = 0; i < l.size(); i++)
            if (!(blocks.size() - 1 == i)) {
                out.append(blocks.get(i)).append(", ");
            } else
                out.append(blocks.get(i));

        return out.toString();
    }

    public static String getList() {

        StringBuilder list = new StringBuilder();

        for (int i = 0; i < blocks.size(); i++) {
            if (!(blocks.size() - 1 == i)) {
                list.append(blocks.get(i)).append(", ");
            } else
                list.append(blocks.get(i));
        }

        return list.toString();

    }

    GSColor getColour(Block block, boolean out) { // this is lifted from lambda search

        if (block == Blocks.PORTAL) {
            return new GSColor(82, 49, 153);
        } else {
            int colorValue = block.blockMapColor.colorValue;
            return new GSColor((colorValue >> 16), (colorValue >> 8 & 255), (colorValue & 255), out ? outline.getValue() : fill.getValue());
        }

    }

}
