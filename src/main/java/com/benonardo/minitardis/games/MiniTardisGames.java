package com.benonardo.minitardis.games;

import dev.enjarai.minitardis.component.screen.app.ScreenAppType;
import dev.enjarai.minitardis.item.FloppyItem;
import dev.enjarai.minitardis.item.ModItems;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.registry.Registry;
import net.minecraft.server.command.CommandManager;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.file.Files;
import java.nio.file.Path;

public class MiniTardisGames implements ModInitializer {
	public static final String MOD_ID = "mini-tardis-games";
    public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);

	@Override
	public void onInitialize() {
		Registry.register(ScreenAppType.REGISTRY, new Identifier(MOD_ID, "custom"), CustomApp.TYPE);

		CommandRegistrationCallback.EVENT.register((dispatcher, registryAccess, environment) -> dispatcher.register(CommandManager.literal("testgame").executes(context -> {
            var player = context.getSource().getPlayer();
            if (player == null) return 0;

            var handStack = player.getMainHandStack();
            if (!handStack.isOf(ModItems.FLOPPY)) {
                context.getSource().sendFeedback(() -> Text.of("Please hold a Floppy in your main hand"), false);
                return 0;
            }

            try {
                FloppyItem.removeApp(handStack, 0);
                FloppyItem.addApp(handStack, new CustomApp(ByteBuffer.wrap(Files.readAllBytes(getFile("testgame.wasm.gz")))));
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
            return 1;
        })));
	}

	public static Path getFile(String path) {
		return FabricLoader.getInstance()
				.getModContainer(MOD_ID)
				.flatMap(container -> container.findPath("data/" + MOD_ID + "/" + path))
				.orElseThrow(() -> new IllegalStateException("Couldn't get inbuilt file " + path + ". Corrupt download?"));
	}
}