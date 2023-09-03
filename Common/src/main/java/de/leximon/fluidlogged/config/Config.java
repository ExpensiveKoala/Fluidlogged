package de.leximon.fluidlogged.config;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.reflect.TypeToken;
import de.leximon.fluidlogged.config.controller.BlockPredicateController;
import de.leximon.fluidlogged.platform.services.Services;
import dev.isxander.yacl3.api.ConfigCategory;
import dev.isxander.yacl3.api.ListOption;
import dev.isxander.yacl3.api.Option;
import dev.isxander.yacl3.api.YetAnotherConfigLib;
import dev.isxander.yacl3.api.controller.BooleanControllerBuilder;
import dev.isxander.yacl3.api.controller.StringControllerBuilder;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.minecraft.world.level.block.state.BlockState;

import java.io.*;
import java.util.List;

public class Config {

    public static final String CONFIG_FILE_NAME = "fluidlogged.json";
    private static final Gson GSON = new GsonBuilder()
            .setPrettyPrinting()
            .create();

    private static final BlockPredicateList fluidloggableBlocks = new BlockPredicateList(ConfigDefaults.FLUIDLOGGABLE_BLOCKS);

    private static boolean fluidPermeabilityEnabled = ConfigDefaults.FLUID_PASSAGE_ENABLED;
    private static final BlockPredicateList fluidPermeableBlocks = new BlockPredicateList(ConfigDefaults.FLUIDLOGGABLE_BLOCKS);



    public static boolean isFluidloggable(BlockState block) {
        return fluidloggableBlocks.contains(block);
    }

    public static boolean isFluidPermeabilityEnabled() {
        return fluidPermeabilityEnabled;
    }

    public static boolean isFluidPermeable(BlockState block) {
        return fluidPermeableBlocks.contains(block);
    }

    public static void invalidateCaches() {
        fluidloggableBlocks.invalidateCache();
        fluidPermeableBlocks.invalidateCache();
    }

    public static void save() {
        File file = Services.PLATFORM.getConfigFile();

        JsonObject obj = new JsonObject();

        obj.add("fluidloggable_blocks", GSON.toJsonTree(fluidloggableBlocks.getBlocks()));

        JsonObject fluidPassage = new JsonObject();
        fluidPassage.addProperty("enabled", fluidPermeabilityEnabled);
        fluidPassage.add("fluid_permeable_blocks", GSON.toJsonTree(fluidPermeableBlocks.getBlocks()));
        obj.add("fluid_permeability", fluidPassage);

        try (FileWriter writer = new FileWriter(file)) {
            GSON.toJson(obj, writer);
        } catch (IOException e) {
            throw new RuntimeException("Failed to save config " + CONFIG_FILE_NAME, e);
        }
    }

    public static void load() {
        File file = Services.PLATFORM.getConfigFile();
        if (!file.exists())
            return;

        try (FileReader reader = new FileReader(file)) {
            JsonObject obj = GSON.fromJson(reader, JsonObject.class);

            if (obj.has("fluidloggable_blocks")) {
                JsonElement element = obj.get("fluidloggable_blocks");
                List<String> blocks = GSON.fromJson(element, new TypeToken<>() {});
                fluidloggableBlocks.setBlocks(blocks);
            }

            if (obj.has("fluid_permeability")) {
                JsonObject fluidPassage = obj.getAsJsonObject("fluid_permeability");

                if (fluidPassage.has("enabled"))
                    fluidPermeabilityEnabled = fluidPassage.get("enabled").getAsBoolean();

                if (fluidPassage.has("fluid_permeable_blocks")) {
                    JsonElement element = fluidPassage.get("fluid_permeable_blocks");
                    List<String> blocks = GSON.fromJson(element, new TypeToken<>() {});
                    fluidPermeableBlocks.setBlocks(blocks);
                }
            }
        } catch (IOException e) {
            throw new RuntimeException("Failed to load config " + CONFIG_FILE_NAME, e);
        }

    }

    public static Screen createConfigScreen(Screen parent) {
        return YetAnotherConfigLib.createBuilder()
                .title(Component.translatable("fluidlogged.config"))
                .category(ConfigCategory.createBuilder()
                        .name(Component.translatable("fluidlogged.config.general"))
                        .group(ListOption.<String>createBuilder()
                                .name(Component.translatable("fluidlogged.config.general.fluidloggable_blocks"))
                                .binding(ConfigDefaults.FLUIDLOGGABLE_BLOCKS, fluidloggableBlocks::getBlocks, fluidloggableBlocks::setBlocks)
                                .customController(BlockPredicateController::new)
                                .initial("")
                                .build()
                        )
                        .build()
                )
                .category(ConfigCategory.createBuilder()
                        .name(Component.translatable("fluidlogged.config.fluid_permeability"))
                        .option(Option.<Boolean>createBuilder()
                                .name(Component.translatable("fluidlogged.config.fluid_permeability.enabled"))
                                .controller(option -> BooleanControllerBuilder.create(option)
                                        .coloured(true)
                                        .yesNoFormatter()
                                )
                                .binding(ConfigDefaults.FLUID_PASSAGE_ENABLED, () -> fluidPermeabilityEnabled, value -> fluidPermeabilityEnabled = value)
                                .build()
                        )
                        .group(ListOption.<String>createBuilder()
                                .name(Component.translatable("fluidlogged.config.fluid_permeability.fluid_permeable_blocks"))
                                .binding(ConfigDefaults.FLUIDPASSABLE_BLOCKS, fluidPermeableBlocks::getBlocks, fluidPermeableBlocks::setBlocks)
                                .customController(BlockPredicateController::new)
                                .initial("")
                                .build()
                        )
                        .build()
                )
                .save(Config::save)
                .build()
                .generateScreen(parent);
    }
}
