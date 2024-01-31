#[link(wasm_import_module = "mini_tardis_games")]
extern "C" {
    fn log(len: i32, offset: i32, level: i32);

    // not sure
    fn random_i32() -> i32;

    // only when drawing
    fn get_width() -> i32;
    fn get_height() -> i32;
    fn get_raw(x: i32, y: i32) -> i32;
    fn set_raw(x: i32, y: i32, color: i32);
}

fn safe_log(str: &str, level: i32) {
    unsafe { log(str.len() as i32, str.as_ptr() as usize as i32, level) }
}

#[no_mangle]
pub extern "C" fn draw() {
    let (width, height) = unsafe { (get_width(), get_height()) };
    for x in 0..width {
        for y in 0..height {
            unsafe { set_raw(x, y, random_i32() & 1) }
        }
    }
}

#[repr(C)]
#[derive(Clone, Copy, Debug)]
pub enum ClickType {
    Left,
    Right,
}

#[no_mangle]
pub extern "C" fn on_click(r#type: ClickType, x: i32, y: i32) {
    
}
