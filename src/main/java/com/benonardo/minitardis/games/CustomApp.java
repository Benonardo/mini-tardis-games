package com.benonardo.minitardis.games;

import com.dylibso.chicory.runtime.Module;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import dev.enjarai.minitardis.component.TardisControl;
import dev.enjarai.minitardis.component.screen.app.AppView;
import dev.enjarai.minitardis.component.screen.app.ScreenApp;
import dev.enjarai.minitardis.component.screen.app.ScreenAppType;
import org.jetbrains.annotations.NotNull;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.zip.GZIPInputStream;

public final class CustomApp implements ScreenApp {

    private static final ByteBuffer EMPTY_BUFFER = ByteBuffer.wrap(new byte[0]);
    public static final Codec<CustomApp> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            Codec.BYTE_BUFFER.fieldOf("wasm").forGetter(CustomApp::getWASM),
            Codec.BYTE_BUFFER.fieldOf("persistent_data").forGetter(CustomApp::getPersistentData)
    ).apply(instance, CustomApp::new));
    public static final ScreenAppType<CustomApp> TYPE = new ScreenAppType<>(CODEC, CustomApp::new, false);
    @NotNull
    private final ByteBuffer wasm;
    @NotNull
    private ByteBuffer persistentData;

    public CustomApp(@NotNull ByteBuffer wasm, @NotNull ByteBuffer persistentData) {
        this.wasm = wasm;
        this.persistentData = persistentData;
    }

    public CustomApp(@NotNull ByteBuffer wasm) {
        this(wasm, EMPTY_BUFFER);
    }

    private CustomApp() {
        this(EMPTY_BUFFER, EMPTY_BUFFER);
    }

    public ByteBuffer getWASM() {
        return this.wasm;
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
        try (var stream = new GZIPInputStream(new ByteArrayInputStream(wasm.array()))) {
            return new WasmBackedAppView(this, Module.builder(stream.readAllBytes()).build());
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public ScreenAppType<?> getType() {
        return TYPE;
    }

}
