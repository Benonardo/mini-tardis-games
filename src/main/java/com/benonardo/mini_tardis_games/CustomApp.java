package com.benonardo.mini_tardis_games;

import com.dylibso.chicory.runtime.Module;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import dev.enjarai.minitardis.block.console.ScreenBlockEntity;
import dev.enjarai.minitardis.canvas.TardisCanvasUtils;
import dev.enjarai.minitardis.component.TardisControl;
import dev.enjarai.minitardis.component.screen.app.AppView;
import dev.enjarai.minitardis.component.screen.app.ScreenApp;
import dev.enjarai.minitardis.component.screen.app.ScreenAppType;
import eu.pb4.mapcanvas.api.core.CanvasColor;
import eu.pb4.mapcanvas.api.core.DrawableCanvas;
import eu.pb4.mapcanvas.api.utils.CanvasUtils;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.ClickType;
import net.minecraft.util.Identifier;
import org.jetbrains.annotations.NotNull;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.zip.GZIPInputStream;

public final class CustomApp implements ScreenApp {

    private static final Identifier INVALID_GAME = new Identifier("mini_tardis_games", "missingno");
    private static final ByteBuffer EMPTY_BUFFER = ByteBuffer.wrap(new byte[0]);
    public static final Codec<CustomApp> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            Identifier.CODEC.fieldOf("app_id").forGetter(CustomApp::getAppId),
            Codec.BYTE_BUFFER.fieldOf("persistent_data").forGetter(CustomApp::getPersistentData)
    ).apply(instance, CustomApp::new));
    public static final ScreenAppType<CustomApp> TYPE = new ScreenAppType<>(CODEC, CustomApp::new, false);
    @NotNull
    private final Identifier appId;
    @NotNull
    private ByteBuffer persistentData;

    public CustomApp(@NotNull Identifier appId, @NotNull ByteBuffer persistentData) {
        this.appId = appId;
        this.persistentData = persistentData;
    }

    public CustomApp(@NotNull Identifier appId) {
        this(appId, EMPTY_BUFFER);
    }

    private CustomApp() {
        this(INVALID_GAME, EMPTY_BUFFER);
    }

    @NotNull
    public Identifier getAppId() {
        return this.appId;
    }

    public void setPersistentData(@NotNull ByteBuffer value) {
        this.persistentData = value;
    }

    @NotNull
    public ByteBuffer getPersistentData() {
        return this.persistentData;
    }

    @Override
    public AppView getView(TardisControl controls) {
        var game = GameManager.GAMES.get(appId);
        if (game == null) {
            return new AppView() {
                @Override
                public void draw(ScreenBlockEntity blockEntity, DrawableCanvas canvas) {
                    var cycle = controls.getTardis().getInteriorWorld().getTime() / 20 % 2;
                    CanvasUtils.draw(canvas, 0, 0, TardisCanvasUtils.getSprite("critical_failure_" + cycle));

                    TardisCanvasUtils.drawCenteredText(canvas, "UNKNOWN GAME", 64, 36, CanvasColor.BRIGHT_RED_HIGH);
                    TardisCanvasUtils.drawCenteredText(canvas, "Insert valid floppy", 64, 46, CanvasColor.RED_HIGH);
                    TardisCanvasUtils.drawCenteredText(canvas, "to start gaming", 64, 54, CanvasColor.RED_HIGH);
                }

                @Override
                public boolean onClick(ScreenBlockEntity blockEntity, ServerPlayerEntity player, ClickType type, int x, int y) {
                    return false;
                }
            };
        } else {
            try (var stream = new GZIPInputStream(new ByteArrayInputStream(game))) {
                return new WasmBackedAppView(this, Module.builder(stream.readAllBytes()).build());
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }
    }

    @Override
    public ScreenAppType<?> getType() {
        return TYPE;
    }

}
