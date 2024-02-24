package com.benonardo.mini_tardis_games;

import net.fabricmc.fabric.api.resource.SimpleSynchronousResourceReloadListener;
import net.minecraft.resource.ResourceManager;
import net.minecraft.util.Identifier;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

public class GameManager implements SimpleSynchronousResourceReloadListener {

    private static final Identifier ID = new Identifier("mini_tardis_games", "game_manager");

    public static final Map<Identifier, byte[]> GAMES = new HashMap<>();

    @Override
    public Identifier getFabricId() {
        return ID;
    }

    @Override
    public void reload(ResourceManager manager) {
        GAMES.clear();
        var games = manager.findResources("mini_tardis_games", identifier -> identifier.getPath().endsWith(".wasm.gz"));
        for (var game : games.entrySet()) {
            try (var stream = game.getValue().getInputStream()) {
                GAMES.put(game.getKey().withPath(game.getKey().getPath().replace("mini_tardis_games/", "").replace(".wasm.gz", "")), stream.readAllBytes());
            } catch (IOException e) {
                MiniTardisGames.LOGGER.error("error while reloading games", e);
            }
        }
    }
}
