const FILE: &[u8] = include_bytes!("bad_apple.bin");

pub(super) const WIDTH: u32 = FILE[0] as u32;
pub(super) const HEIGHT: u32 = FILE[1] as u32;

#[allow(clippy::cast_possible_truncation, clippy::cast_possible_wrap)]
pub(super) const FRAME_COUNT: i32 = (FILE.len() - 2) as i32 * 2 / (WIDTH * HEIGHT) as i32;

#[allow(clippy::cast_sign_loss)]
pub(super) const fn get_pixel(frame: u32, x: u32, y: u32) -> u8 {
    let index = frame * WIDTH * HEIGHT + x * HEIGHT + y;
    if index % 2 == 0 {
        FILE[2 + index as usize / 2] >> 4
    } else {
        FILE[2 + index as usize / 2] & 0b1111
    }
}
