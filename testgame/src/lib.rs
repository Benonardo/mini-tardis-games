use std::ops::Deref;

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

#[repr(C)]
#[derive(Clone, Copy, Debug)]
pub enum ClickType {
    Left,
    Right,
}

type Data = Box<Option<ClickType>>;

#[no_mangle]
pub extern "C" fn draw(data_ptr: i32) {
    safe_log(&format!("in draw, pointer is {data_ptr}"), 20);
    let Some(data) = (unsafe { (data_ptr as *mut Data).as_mut() }) else {
        safe_log(&format!("in draw, pointer is {data_ptr}"), 40);
        return;
    };
    let (width, height) = unsafe { (get_width(), get_height()) };
    for x in 0..width {
        for y in 0..height {
            let color = match data.deref().deref() {
                Some(ClickType::Left) => 42,
                Some(ClickType::Right) => 69,
                None => 100,
            };
            unsafe { set_raw(x, y, color) }
        }
    }
}

#[no_mangle]
pub extern "C" fn on_click(data_ptr: i32, r#type: ClickType, x: i32, y: i32) {
    safe_log(&format!("in on_click, pointer is {data_ptr}"), 20);
    let Some(data) = (unsafe { (data_ptr as *mut Data).as_mut() }) else {
        safe_log(&format!("in on_click, pointer is {data_ptr}"), 40);
        return;
    };
    **data = Some(r#type);
}

#[no_mangle]
pub extern "C" fn alloc_data() -> i32 {
    Box::leak(Data::default()) as *mut _ as i32
}
