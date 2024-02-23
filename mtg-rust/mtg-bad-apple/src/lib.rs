mod file;

use mtg_rust::{game_impl, Game, SoundCategory};

const BLACK_LOWEST: i32 = 119;
const BLACK_LOW: i32 = 116;
const BLACK_NORMAL: i32 = 117;
const BLACK_HIGH: i32 = 118;
const WHITE_LOWEST: i32 = 35;
const WHITE_LOW: i32 = 32;
const WHITE_NORMAL: i32 = 33;
const WHITE_HIGH: i32 = 34;

const fn get_raw_color(frame: i32, x: i32, y: i32) -> i32 {
    let pixel = file::get_pixel(frame, x, y);
    match pixel {
        0 => BLACK_LOWEST,
        1 => BLACK_LOW,
        2 => BLACK_NORMAL,
        3 => BLACK_HIGH,
        4 => WHITE_LOWEST,
        5 => WHITE_LOW,
        6 => WHITE_NORMAL,
        _ => WHITE_HIGH,
    }
}

game_impl!(BadApple);

#[derive(Default)]
struct BadApple {
    frame_counter: i32,
}

impl Game for BadApple {
    fn initialize() -> Self
    where
        Self: Sized,
    {
        Default::default()
    }

    fn draw(&mut self, screen: &mtg_rust::Screen, canvas: &mtg_rust::Canvas) {
        let frame = self.frame_counter.clamp(0, file::FRAME_COUNT);

        let canvas_width = canvas.get_width();
        let canvas_height = canvas.get_height();

        for x in 0..file::WIDTH {
            for y in 0..file::HEIGHT {
                let color = get_raw_color(frame, x, y);
                canvas.set_pixel_raw(
                    x * file::WIDTH / canvas_width,
                    y * file::HEIGHT / canvas_height,
                    color,
                );
            }
        }

        self.frame_counter += 1;

        if self.frame_counter >= file::FRAME_COUNT {
            screen.close();
        }
    }

    fn screen_open(&mut self, screen: &mtg_rust::Screen) {
        self.frame_counter = -5;
        screen.play_sound("mini_tardis:bad_apple", SoundCategory::Records, 1.0, 1.0);
    }

    fn screen_close(&mut self, _screen: &mtg_rust::Screen) {
        self.frame_counter = 0;
        // TODO stop sound
    }

    fn on_click(
        &mut self,
        _screen: &mtg_rust::Screen,
        _click_type: mtg_rust::ClickType,
        _x: i32,
        _y: i32,
    ) {
    }
}
