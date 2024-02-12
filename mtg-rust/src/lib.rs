#![cfg_attr(not(feature = "std"), no_std)]

mod ffi;

fn convert_str(str: &str) -> (i32, i32) {
    let address = (str.as_ptr() as usize)
        .try_into()
        .expect("couldn't convert string pointer to i32");
    let len = str
        .len()
        .try_into()
        .expect("couldn't convert string len to i32");
    (address, len)
}

/// A level of importance for the [`log`] function.  
/// Analogous to SLF4J's [`Level`](https://www.javadoc.io/doc/org.slf4j/slf4j-api/latest/org.slf4j/org/slf4j/event/Level.html) enum.
#[repr(i32)]
pub enum LogLevel {
    Error = 40,
    Warn = 30,
    Info = 20,
    Debug = 10,
    Trace = 0,
}

/// Logs a message.  
/// Forwards to the mod's SLF4J [`Logger`](https://www.javadoc.io/doc/org.slf4j/slf4j-api/latest/org.slf4j/org/slf4j/Logger.html)'s [`log`](https://www.javadoc.io/doc/org.slf4j/slf4j-api/latest/org.slf4j/org/slf4j/spi/LoggingEventBuilder.html#log()) method.
pub fn log(message: &str, level: LogLevel) {
    let (message_address, message_len) = convert_str(message);
    unsafe {
        ffi::mtg_log(message_address, message_len, level as i32);
    }
}

/// Returns an arbitrary nanosecond value to measure elapsed time between two calls.  
/// Analogous to Java's [`System.nanoTime()`](https://docs.oracle.com/en/java/javase/17/docs/api/java.base/java/lang/System.html#nanoTime()) method.
pub fn nano_time() -> i64 {
    unsafe { ffi::mtg_nano_time() }
}

/// A category for a certain sound event, used by [`Screen::play_sound`].  
/// Analogous to [the class with the same name in yarn mappings](https://maven.fabricmc.net/docs/yarn-1.20.1+build.10/net/minecraft/sound/SoundCategory.html).
#[repr(i32)]
pub enum SoundCategory {
    Master,
    Music,
    Records,
    Weather,
    Blocks,
    Hostile,
    Neutral,
    Players,
    Ambient,
    Voice,
}

/// A (console) screen that contains methods that can be called from most callbacks.  
/// Analogous to Mini Tardis' [`ScreenBlockEntity`](https://github.com/enjarai/mini-tardis/blob/master/src/main/java/dev/enjarai/minitardis/block/console/ScreenBlockEntity.java) class.
pub struct Screen {
    _dummy: (),
}

impl Screen {
    /// Returns a pseudorandom [`i32`] using the block entities' [`drawRandom`](https://github.com/enjarai/mini-tardis/blob/cd9041c0cd82eb7f92d4e48ea3c24d9a2ec62e24/src/main/java/dev/enjarai/minitardis/block/console/ScreenBlockEntity.java#L48) field.
    pub fn random_i32() -> i32 {
        unsafe { ffi::mtg_random_i32() }
    }

    /// Plays a sound at the screen's position with the specified `category`, `volume` and `pitch`.  
    /// The [`str`] `id` is in the format of an [`Identifier`](https://maven.fabricmc.net/docs/yarn-1.20.1+build.10/net/minecraft/util/Identifier.html) referring to a sound event.
    pub fn play_sound(id: &str, category: SoundCategory, volume: f32, pitch: f32) {
        let (id_address, id_len) = convert_str(id);
        unsafe {
            ffi::mtg_play_sound(id_address, id_len, category as i32, volume, pitch);
        }
    }
}

/// A canvas that can be manipulated via various methods and is available in draw callbacks.  
/// Analogous to Map Canvas API's [`DrawableCanvas`](https://github.com/Patbox/map-canvas-api/blob/master/src/main/java/eu/pb4/mapcanvas/api/core/DrawableCanvas.java) class.
pub struct Canvas {
    _dummy: (),
}

impl Canvas {
    /// Get the width of the canvas, usually 128.  
    /// Analogous to the canvas' [`getWidth`](https://github.com/Patbox/map-canvas-api/blob/2dc8c9ab5ff2c5caa4cc29168b205224e95402ea/src/main/java/eu/pb4/mapcanvas/api/core/DrawableCanvas.java#L34) method.
    pub fn get_width() -> i32 {
        unsafe { ffi::mtg_get_width() }
    }

    /// Get the height of the canvas, usually 96.  
    /// Analogous to the canvas' [`getHeight`](https://github.com/Patbox/map-canvas-api/blob/2dc8c9ab5ff2c5caa4cc29168b205224e95402ea/src/main/java/eu/pb4/mapcanvas/api/core/DrawableCanvas.java#L32) method.
    pub fn get_height() -> i32 {
        unsafe { ffi::mtg_get_height() }
    }

    /// Get the raw color value at the certain `x` and `y` coordinates.  
    /// Analogous to the canvas' [`getRaw`](https://github.com/Patbox/map-canvas-api/blob/2dc8c9ab5ff2c5caa4cc29168b205224e95402ea/src/main/java/eu/pb4/mapcanvas/api/core/DrawableCanvas.java#L28) method.
    pub fn get_raw_color(x: i32, y: i32) -> i32 {
        unsafe { ffi::mtg_get_raw(x, y) }
    }

    /// Set the raw color value at the certain `x` and `y` coordinates.  
    /// Analogous to the canvas' [`setRaw`](https://github.com/Patbox/map-canvas-api/blob/2dc8c9ab5ff2c5caa4cc29168b205224e95402ea/src/main/java/eu/pb4/mapcanvas/api/core/DrawableCanvas.java#L30) method.
    pub fn set_pixel_raw(x: i32, y: i32, color: i32) {
        unsafe {
            ffi::mtg_set_raw(x, y, color);
        }
    }

    /// Set the RGB color value at the certain `x` and `y` coordinates.  
    /// Analogous to calling the canvas' [`set`](https://github.com/Patbox/map-canvas-api/blob/2dc8c9ab5ff2c5caa4cc29168b205224e95402ea/src/main/java/eu/pb4/mapcanvas/api/core/DrawableCanvas.java#L20) method 
    /// with the color argument returned by [`CanvasUtils.findClosestColor`](https://github.com/Patbox/map-canvas-api/blob/b9781dbdd439ff94ff58383ee8ada64928d4bf28/src/main/java/eu/pb4/mapcanvas/api/utils/CanvasUtils.java#L116).
    pub fn set_pixel_rgb(x: i32, y: i32, color: i32) {
        unsafe {
            ffi::mtg_set_rgb(x, y, color);
        }
    }

    /// Set the ARGB color value at the certain `x` and `y` coordinates.  
    /// Analogous to calling the canvas' [`set`](https://github.com/Patbox/map-canvas-api/blob/2dc8c9ab5ff2c5caa4cc29168b205224e95402ea/src/main/java/eu/pb4/mapcanvas/api/core/DrawableCanvas.java#L20) method 
    /// with the color argument returned by [`CanvasUtils.findClosestColorARGB`](https://github.com/Patbox/map-canvas-api/blob/b9781dbdd439ff94ff58383ee8ada64928d4bf28/src/main/java/eu/pb4/mapcanvas/api/utils/CanvasUtils.java#L106).
    pub fn set_pixel_argb(x: i32, y: i32, color: i32) {
        unsafe {
            ffi::mtg_set_argb(x, y, color);
        }
    }

    /// Draw a sprite at the certain `x` and `y` coordinates.  
    /// Analogous to calling the [`CanvasUtils.draw`](https://github.com/Patbox/map-canvas-api/blob/b9781dbdd439ff94ff58383ee8ada64928d4bf28/src/main/java/eu/pb4/mapcanvas/api/utils/CanvasUtils.java#L56) method
    /// with the source argument returned by [`TardisCanvasUtils.getSprite`](https://github.com/enjarai/mini-tardis/blob/cd9041c0cd82eb7f92d4e48ea3c24d9a2ec62e24/src/main/java/dev/enjarai/minitardis/canvas/TardisCanvasUtils.java#L41).
    pub fn draw_inbuilt_sprite(x: i32, y: i32, name: &str) {
        let (name_address, name_len) = convert_str(name);
        unsafe {
            ffi::mtg_draw_inbuilt_sprite(x, y, name_address, name_len);
        }
    }

    /// Draw text at the certain `x` and `y` coordinates.  
    /// Analogous to calling `DefaultFonts.VANILLA.drawText(canvas, text, x, y, size, CanvasUtils.findClosestColorARGB(argb));`.
    pub fn draw_text(x: i32, y: i32, text: &str, size: i32, argb_color: i32) {
        let (text_address, text_len) = convert_str(text);
        unsafe {
            ffi::mtg_draw_text(x, y, text_address, text_len, size, argb_color);
        }
    }
}
