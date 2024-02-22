package com.benonardo.minitardis.games;

import com.dylibso.chicory.runtime.*;
import com.dylibso.chicory.runtime.Module;
import com.dylibso.chicory.wasm.exceptions.ChicoryException;
import com.dylibso.chicory.wasm.types.Value;
import com.dylibso.chicory.wasm.types.ValueType;
import dev.enjarai.minitardis.block.console.ScreenBlockEntity;
import dev.enjarai.minitardis.canvas.TardisCanvasUtils;
import dev.enjarai.minitardis.component.screen.app.AppView;
import eu.pb4.mapcanvas.api.core.DrawableCanvas;
import eu.pb4.mapcanvas.api.font.DefaultFonts;
import eu.pb4.mapcanvas.api.utils.CanvasUtils;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvent;
import net.minecraft.util.ClickType;
import net.minecraft.util.Identifier;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.slf4j.event.Level;

import java.nio.ByteBuffer;
import java.util.List;

public final class WasmBackedAppView implements AppView {

    @NotNull
    private final CustomApp app;
    @NotNull
    private final ExportFunction draw;
    @NotNull
    private final ExportFunction onClick;
    @Nullable
    private final ExportFunction drawBackground;
    @Nullable
    private final ExportFunction screenTick;
    @Nullable
    private final ExportFunction screenOpen;
    @Nullable
    private final ExportFunction screenClose;
    private final int dataPtr;
    private final Instance instance;
    @Nullable
    private DrawableCanvas canvas = null;
    @Nullable
    private ScreenBlockEntity blockEntity;

    public WasmBackedAppView(@NotNull CustomApp app, @NotNull Module module) {
        this.app = app;
        var functions = this.new BuiltinFunctions();
        instance = module.instantiate(new HostImports(functions.all));
        this.draw = instance.export("mtg_draw");
        this.onClick = instance.export("mtg_on_click");
        var allocResult = instance.export("mtg_alloc_data").apply();
        if (allocResult.length != 1) {
            throw new RuntimeException("alloc_data() returned multiple values");
        }
        this.dataPtr = allocResult[0].asInt();

        var drawBackground = (ExportFunction)null;
        try {
            drawBackground = instance.export("mtg_draw_background");
        } catch (ChicoryException ignored) {}
        this.drawBackground = drawBackground;
        var screenTick = (ExportFunction)null;
        try {
            screenTick = instance.export("mtg_screen_tick");
        } catch (ChicoryException ignored) {}
        this.screenTick = screenTick;
        var screenOpen = (ExportFunction)null;
        try {
            screenOpen = instance.export("mtg_screen_open");
        } catch (ChicoryException ignored) {}
        this.screenOpen = screenOpen;
        var screenClose = (ExportFunction)null;
        try {
            screenClose = instance.export("mtg_screen_close");
        } catch (ChicoryException ignored) {}
        this.screenClose = screenClose;
    }

    private void handleThrowable(ScreenBlockEntity blockEntity, Throwable throwable, String function) {
        var temp = throwable;
        boolean shouldSuppress;
        do {
            shouldSuppress = temp instanceof AppClosedException;
            temp = temp.getCause();
        } while (!shouldSuppress && temp != null);

        if (!shouldSuppress) {
            MiniTardisGames.LOGGER.error("WASM " + function, throwable);
            blockEntity.closeApp();
        }
    }

    @Override
    public synchronized void draw(ScreenBlockEntity blockEntity, DrawableCanvas canvas) {
        this.canvas = canvas;
        this.blockEntity = blockEntity;
        try {
            draw.apply(Value.i32(dataPtr));
        } catch (Throwable throwable) {
            handleThrowable(blockEntity, throwable, "draw");
        }
        this.canvas = null;
        this.blockEntity = null;
    }

    @Override
    public synchronized boolean onClick(ScreenBlockEntity blockEntity, ServerPlayerEntity player, ClickType type, int x, int y) {
        this.blockEntity = blockEntity;
        try {
            onClick.apply(Value.i32(dataPtr), Value.i32(type.ordinal()), Value.i32(x), Value.i32(y));
        } catch (Throwable throwable) {
            handleThrowable(blockEntity, throwable, "on_click");
        }
        this.blockEntity = null;
        return false;
    }

    @Override
    public synchronized void drawBackground(ScreenBlockEntity blockEntity, DrawableCanvas canvas) {
        if (this.drawBackground == null) {
            AppView.super.drawBackground(blockEntity, canvas);
        } else {
            this.canvas = canvas;
            this.blockEntity = blockEntity;
            try {
                drawBackground.apply(Value.i32(dataPtr));
            } catch (Throwable throwable) {
                handleThrowable(blockEntity, throwable, "draw_background");
            }
            this.canvas = null;
            this.blockEntity = null;
        }
    }

    @Override
    public synchronized void screenTick(ScreenBlockEntity blockEntity) {
        if (this.screenTick == null) {
            AppView.super.screenTick(blockEntity);
        } else {
            this.blockEntity = blockEntity;
            try {
                screenTick.apply(Value.i32(dataPtr));
            } catch (Throwable throwable) {
                handleThrowable(blockEntity, throwable, "screen_tick");
            }
            this.blockEntity = null;
        }
    }

    @Override
    public synchronized void screenOpen(ScreenBlockEntity blockEntity) {
        if (this.screenOpen == null) {
            AppView.super.screenOpen(blockEntity);
        } else {
            this.blockEntity = blockEntity;
            try {
                screenOpen.apply(Value.i32(dataPtr));
            } catch (Throwable throwable) {
                handleThrowable(blockEntity, throwable, "screen_open");
            }
            this.blockEntity = null;
        }
    }

    @Override
    public synchronized void screenClose(ScreenBlockEntity blockEntity) {
        if (this.screenClose == null) {
            AppView.super.screenOpen(blockEntity);
        } else {
            this.blockEntity = blockEntity;
            try {
                screenClose.apply(Value.i32(dataPtr));
            } catch (Throwable throwable) {
                handleThrowable(blockEntity, throwable, "screen_close");
            }
            this.blockEntity = null;
        }
    }

    private final class BuiltinFunctions {

        public final HostFunction log = new HostFunction(
                (instance, args) -> {
                    var messageAddress = args[0].asInt();
                    var messageLen = args[1].asInt();
                    var message = instance.memory().readString(messageAddress, messageLen);
                    var level = args[2].asInt();
                    MiniTardisGames.LOGGER.atLevel(Level.intToLevel(level)).log(message);
                    return Value.EMPTY_VALUES;
                },
                "mini_tardis_games",
                "mtg_log",
                List.of(ValueType.I32, ValueType.I32, ValueType.I32),
                List.of()
        );
        public final HostFunction randomI32 = new HostFunction(
                (instance, args) -> {
                    if (blockEntity == null) {
                        throw new IllegalStateException("Called random_i32() while not currently in a context???");
                    }
                    return new Value[]{Value.i32(blockEntity.drawRandom.nextInt())};
                },
                "mini_tardis_games",
                "mtg_random_i32",
                List.of(),
                List.of()
        );
        public final HostFunction getWidth = new HostFunction(
                (instance, args) -> {
                    if (canvas == null) {
                        throw new IllegalStateException("Called get_width() while not currently drawing");
                    }
                    return new Value[]{Value.i32(canvas.getWidth())};
                },
                "mini_tardis_games",
                "mtg_get_width",
                List.of(),
                List.of(ValueType.I32)
        );
        public final HostFunction getHeight = new HostFunction(
                (instance, args) -> {
                    if (canvas == null) {
                        throw new IllegalStateException("Called get_height() while not currently drawing");
                    }
                    return new Value[]{Value.i32(canvas.getHeight())};
                },
                "mini_tardis_games",
                "mtg_get_height",
                List.of(),
                List.of(ValueType.I32)
        );
        public final HostFunction getRaw = new HostFunction(
                (instance, args) -> {
                    if (canvas == null) {
                        throw new IllegalStateException("Called get_raw(x, y) while not currently drawing");
                    }
                    var x = args[0].asInt();
                    var y = args[1].asInt();
                    return new Value[]{Value.i32(canvas.getRaw(x, y))};
                },
                "mini_tardis_games",
                "mtg_get_raw",
                List.of(ValueType.I32, ValueType.I32),
                List.of(ValueType.I32)
        );
        public final HostFunction setRaw = new HostFunction(
                (instance, args) -> {
                    if (canvas == null) {
                        throw new IllegalStateException("Called set_raw(x, y, color) while not currently drawing");
                    }
                    var x = args[0].asInt();
                    var y = args[1].asInt();
                    var color = args[2].asInt();
                    if (color < Byte.MIN_VALUE || color > Byte.MAX_VALUE) {
                        throw new IllegalArgumentException("Raw color " + color + " is out of bounds for byte");
                    }
                    canvas.setRaw(x, y, (byte) color);
                    return Value.EMPTY_VALUES;
                },
                "mini_tardis_games",
                "mtg_set_raw",
                List.of(ValueType.I32, ValueType.I32, ValueType.I32),
                List.of()
        );
        public final HostFunction setRgb = new HostFunction(
                (instance, args) -> {
                    if (canvas == null) {
                        throw new IllegalStateException("Called set_rgb(x, y, color) while not currently drawing");
                    }
                    var x = args[0].asInt();
                    var y = args[1].asInt();
                    var color = args[2].asInt();
                    canvas.set(x, y, CanvasUtils.findClosestColor(color));
                    return Value.EMPTY_VALUES;
                },
                "mini_tardis_games",
                "mtg_set_rgb",
                List.of(ValueType.I32, ValueType.I32, ValueType.I32),
                List.of()
        );
        public final HostFunction setArgb = new HostFunction(
                (instance, args) -> {
                    if (canvas == null) {
                        throw new IllegalStateException("Called set_argb(x, y, color) while not currently drawing");
                    }
                    var x = args[0].asInt();
                    var y = args[1].asInt();
                    var color = args[2].asInt();
                    canvas.set(x, y, CanvasUtils.findClosestColorARGB(color));
                    return Value.EMPTY_VALUES;
                },
                "mini_tardis_games",
                "mtg_set_argb",
                List.of(ValueType.I32, ValueType.I32, ValueType.I32),
                List.of()
        );
        public final HostFunction drawInbuiltSprite = new HostFunction(
                (instance, args) -> {
                    if (canvas == null) {
                        throw new IllegalStateException("Called draw_inbuilt_sprite(x, y, name_address, name_len) while not currently drawing");
                    }
                    var x = args[0].asInt();
                    var y = args[1].asInt();
                    var nameAddress = args[2].asInt();
                    var nameLen = args[3].asInt();
                    var name = instance.memory().readString(nameAddress, nameLen);
                    CanvasUtils.draw(canvas, x, y, TardisCanvasUtils.getSprite(name));
                    return Value.EMPTY_VALUES;
                },
                "mini_tardis_games",
                "mtg_draw_inbuilt_sprite",
                List.of(ValueType.I32, ValueType.I32, ValueType.I32, ValueType.I32),
                List.of()
        );
        public final HostFunction drawText = new HostFunction(
                (instance, args) -> {
                    if (canvas == null) {
                        throw new IllegalStateException("Called draw_text(x, y, text_address, tex_len, size, argb) while not currently drawing");
                    }
                    var x = args[0].asInt();
                    var y = args[1].asInt();
                    var textAddress = args[2].asInt();
                    var textLen = args[3].asInt();
                    var text = instance.memory().readString(textAddress, textLen);
                    var size = args[4].asInt();
                    var argb = args[5].asInt();
                    DefaultFonts.VANILLA.drawText(canvas, text, x, y, size, CanvasUtils.findClosestColorARGB(argb));
                    return Value.EMPTY_VALUES;
                },
                "mini_tardis_games",
                "mtg_draw_text",
                List.of(ValueType.I32, ValueType.I32, ValueType.I32, ValueType.I32, ValueType.I32, ValueType.I32),
                List.of()
        );
        public final HostFunction playSound = new HostFunction(
                (instance, args) -> {
                    if (blockEntity == null || blockEntity.getWorld() == null) {
                        throw new IllegalStateException("Called play_sound(id_address, id_len, category, volume, pitch) while not currently in a context???");
                    }
                    var idAddress = args[0].asInt();
                    var idLen = args[1].asInt();
                    var id = instance.memory().readString(idAddress, idLen);
                    var category = SoundCategory.values()[args[2].asInt()];
                    var volume = args[3].asFloat();
                    var pitch = args[4].asFloat();
                    blockEntity.getWorld().playSound(null, blockEntity.getPos(), SoundEvent.of(new Identifier(id)), category, volume, pitch);
                    return Value.EMPTY_VALUES;
                },
                "mini_tardis_games",
                "mtg_play_sound",
                List.of(ValueType.I32, ValueType.I32, ValueType.I32, ValueType.F32, ValueType.F32),
                List.of()
        );
        public final HostFunction closeApp = new HostFunction(
                (instance, args) -> {
                    if (blockEntity == null) {
                        throw new IllegalStateException("Called close_app() while not currently in a context???");
                    }
                    blockEntity.closeApp();
                    throw new AppClosedException();
                },
                "mini_tardis_games",
                "mtg_close_app",
                List.of(),
                List.of(ValueType.I64)
        );
        public final HostFunction nanoTime = new HostFunction(
                (instance, args) -> new Value[]{Value.i64(System.nanoTime())},
                "mini_tardis_games",
                "mtg_nano_time",
                List.of(),
                List.of(ValueType.I64)
        );
        public final HostFunction savePersistentData = new HostFunction(
                (instance, args) -> {
                    var dataAddress = args[0].asInt();
                    var dataLen = args[1].asInt();
                    var data = instance.memory().readBytes(dataAddress, dataLen);
                    app.setPersistentData(ByteBuffer.wrap(data));
                    return Value.EMPTY_VALUES;
                },
                "mini_tardis_games",
                "mtg_save_persistent_data",
                List.of(ValueType.I32, ValueType.I32),
                List.of()
        );
        public final HostFunction getPersistentDataLen = new HostFunction(
                (instance, args) -> new Value[]{Value.i32(app.getPersistentData().limit())},
                "mini_tardis_games",
                "mtg_get_persistent_data_len",
                List.of(),
                List.of(ValueType.I32)
        );
        public final HostFunction getPersistentData = new HostFunction(
                (instance, args) -> {
                    var dataAddress = args[0].asInt();
                    instance.memory().write(dataAddress, app.getPersistentData().array());
                    return Value.EMPTY_VALUES;
                },
                "mini_tardis_games",
                "mtg_get_persistent_data",
                List.of(ValueType.I32),
                List.of()
        );
        public final HostFunction[] all = {
                log, randomI32, getWidth, getHeight, getRaw, setRaw, setRgb, setArgb,
                drawInbuiltSprite, drawText, playSound, closeApp, nanoTime, savePersistentData, getPersistentDataLen,
                getPersistentData
        };

        private BuiltinFunctions() {
        }

    }

    private static class AppClosedException extends RuntimeException {

    }
}
