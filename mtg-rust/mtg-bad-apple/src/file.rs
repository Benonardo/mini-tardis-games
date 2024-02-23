const FILE: &[u8] = include_bytes!("bad_apple.bin");

pub(super) const WIDTH: i32 = FILE[0] as i32;
pub(super) const HEIGHT: i32 = FILE[1] as i32;

pub(super) const FRAME_COUNT: i32 = (FILE.len() - 2) as i32 * 2 / (WIDTH * HEIGHT);

pub(super) const fn get_pixel(frame: i32, x: i32, y: i32) -> u8 {
    let index = frame * WIDTH * HEIGHT + x * HEIGHT + y;
    if index % 2 == 0 {
        FILE[2 + index as usize / 2] >> 4
    } else {
        FILE[2 + index as usize / 2] & 0b1111
    }
}
