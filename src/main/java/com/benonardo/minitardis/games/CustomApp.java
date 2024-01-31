package com.benonardo.minitardis.games;

import com.dylibso.chicory.runtime.HostImports;
import com.dylibso.chicory.runtime.Module;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import dev.enjarai.minitardis.component.TardisControl;
import dev.enjarai.minitardis.component.screen.app.AppView;
import dev.enjarai.minitardis.component.screen.app.ScreenApp;
import dev.enjarai.minitardis.component.screen.app.ScreenAppType;

import java.nio.ByteBuffer;

public record CustomApp(ByteBuffer wasm) implements ScreenApp {

    private static final ByteBuffer EMPTY = ByteBuffer.wrap(new byte[]{});
    public static final Codec<CustomApp> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            Codec.BYTE_BUFFER.fieldOf("wasm").forGetter(CustomApp::wasm)
    ).apply(instance, CustomApp::new));
    public static final ScreenAppType<CustomApp> TYPE = new ScreenAppType<>(CODEC, CustomApp::new, false);

    private CustomApp() {
        this(EMPTY);
    }

    @Override
    public AppView getView(TardisControl controls) {
        return new WasmBackedAppView(Module.builder(wasm).build());
    }

    @Override
    public ScreenAppType<?> getType() {
        return TYPE;
    }
}
