package com.benonardo.minitardis.games;

import com.dylibso.chicory.runtime.ExportFunction;
import com.dylibso.chicory.runtime.HostFunction;
import com.dylibso.chicory.runtime.HostImports;
import com.dylibso.chicory.runtime.Module;
import com.dylibso.chicory.wasm.types.Value;
import com.dylibso.chicory.wasm.types.ValueType;
import dev.enjarai.minitardis.block.console.ConsoleScreenBlockEntity;
import dev.enjarai.minitardis.component.screen.app.AppView;
import eu.pb4.mapcanvas.api.core.DrawableCanvas;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.ClickType;
import org.jetbrains.annotations.Nullable;
import org.slf4j.event.Level;

import java.util.List;

public final class WasmBackedAppView implements AppView {

    private final ExportFunction draw;
    private final ExportFunction onClick;
    @Nullable
    private DrawableCanvas canvas = null;
    @Nullable
    private ConsoleScreenBlockEntity blockEntity;

    public WasmBackedAppView(Module module) {
        var functions = this.new BuiltinFunctions();
        var instance = module.instantiate(new HostImports(functions.ALL));
        this.draw = instance.export("draw");
        this.onClick = instance.export("on_click");
    }

    @Override
    public void draw(ConsoleScreenBlockEntity blockEntity, DrawableCanvas canvas) {
        this.canvas = canvas;
        this.blockEntity = blockEntity;
        try {
            draw.apply();
        } catch (Exception e) {
            MiniTardisGames.LOGGER.error("WASM draw", e);
        }
        this.canvas = null;
        this.blockEntity = null;
    }

    @Override
    public boolean onClick(ConsoleScreenBlockEntity blockEntity, ServerPlayerEntity player, ClickType type, int x, int y) {
        this.blockEntity = blockEntity;
        try {
            onClick.apply(Value.i32(type.ordinal()), Value.i32(x), Value.i32(y));
        } catch (Exception e) {
            MiniTardisGames.LOGGER.error("WASM on_click", e);
        }
        this.blockEntity = null;
        return false;
    }

    private final class BuiltinFunctions {

        public final HostFunction log = new HostFunction(
                (instance, args) -> {
                    var len = args[0].asInt();
                    var offset = args[1].asInt();
                    var message = instance.memory().readString(offset, len);
                    var level = args[2].asInt();
                    MiniTardisGames.LOGGER.atLevel(Level.intToLevel(level)).log(message);
                    return Value.EMPTY_VALUES;
                },
                "mini_tardis_games",
                "log",
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
                "random_i32",
                List.of(ValueType.I32, ValueType.I32, ValueType.I32),
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
                "get_width",
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
                "get_height",
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
                "get_raw",
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
                "set_raw",
                List.of(ValueType.I32, ValueType.I32, ValueType.I32),
                List.of()
        );
        public final HostFunction[] ALL = {log, randomI32, getWidth, getHeight, getRaw, setRaw};

        private BuiltinFunctions() {
        }

    }
}
