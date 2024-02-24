package com.benonardo.mini_tardis_games;

import dev.enjarai.minitardis.component.screen.app.ScreenAppType;
import dev.enjarai.minitardis.item.FloppyItem;
import dev.enjarai.minitardis.item.ModItems;
import eu.pb4.mapcanvas.api.core.CanvasColor;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;
import net.fabricmc.fabric.api.resource.ResourceManagerHelper;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.command.argument.IdentifierArgumentType;
import net.minecraft.registry.Registry;
import net.minecraft.resource.ResourceType;
import net.minecraft.server.command.CommandManager;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.nio.file.Path;

public class MiniTardisGames implements ModInitializer {
	public static final String MOD_ID = "mini_tardis_games";
    public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);

	@Override
	public void onInitialize() {
        MiniTardisGames.LOGGER.info(String.valueOf(CanvasColor.BLACK_LOWEST.getRenderColor()));
        MiniTardisGames.LOGGER.info(String.valueOf(CanvasColor.BLACK_LOW.getRenderColor()));
        MiniTardisGames.LOGGER.info(String.valueOf(CanvasColor.BLACK_NORMAL.getRenderColor()));
        MiniTardisGames.LOGGER.info(String.valueOf(CanvasColor.BLACK_HIGH.getRenderColor()));
        MiniTardisGames.LOGGER.info(String.valueOf(CanvasColor.WHITE_LOWEST.getRenderColor()));
        MiniTardisGames.LOGGER.info(String.valueOf(CanvasColor.WHITE_LOW.getRenderColor()));
        MiniTardisGames.LOGGER.info(String.valueOf(CanvasColor.WHITE_NORMAL.getRenderColor()));
        MiniTardisGames.LOGGER.info(String.valueOf(CanvasColor.WHITE_HIGH.getRenderColor()));
		Registry.register(ScreenAppType.REGISTRY, new Identifier(MOD_ID, "custom"), CustomApp.TYPE);

        ResourceManagerHelper.get(ResourceType.SERVER_DATA).registerReloadListener(new GameManager());

		CommandRegistrationCallback.EVENT.register((dispatcher, registryAccess, environment) -> dispatcher.register(CommandManager.literal("minitardisgames").then(CommandManager.literal("install").then(CommandManager.argument("game", IdentifierArgumentType.identifier()).executes(context -> {
            var player = context.getSource().getPlayer();
            if (player == null) return 0;

            var handStack = player.getMainHandStack();
            if (!handStack.isOf(ModItems.FLOPPY)) {
                context.getSource().sendFeedback(() -> Text.of("Please hold a Floppy in your main hand"), false);
                return 0;
            }

            FloppyItem.removeApp(handStack, 0);
            FloppyItem.addApp(handStack, new CustomApp(IdentifierArgumentType.getIdentifier(context, "game")));
            return 1;
        }))).then(CommandManager.literal("list").executes(context -> {
            context.getSource().sendMessage(Text.literal(GameManager.GAMES.toString()));

            return 1;
        }))));
	}

	public static Path getFile(String path) {
		return FabricLoader.getInstance()
				.getModContainer(MOD_ID)
				.flatMap(container -> container.findPath("data/" + MOD_ID + "/" + path))
				.orElseThrow(() -> new IllegalStateException("Couldn't get inbuilt file " + path + ". Corrupt download?"));
	}
}