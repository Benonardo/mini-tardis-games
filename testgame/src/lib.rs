use glam::{Vec2, Vec3, Vec4};

#[link(wasm_import_module = "mini_tardis_games")]
extern "C" {
    fn mtg_log(offset: i32, len: i32, level: i32);

    // not sure
    //fn mtg_random_i32() -> i32;

    // only when drawing
    fn mtg_get_width() -> i32;
    fn mtg_get_height() -> i32;
    //fn mtg_get_raw(x: i32, y: i32) -> i32;
    //fn mtg_set_raw(x: i32, y: i32, color: i32);
    fn mtg_set_argb(x: i32, y: i32, color: i32);
    //fn mtg_draw_inbuilt_sprite(x: i32, y: i32, name_offset: i32, name_len: i32);
    fn mtg_draw_text(text_offset: i32, text_len: i32, x: i32, y: i32, size: i32, argb: i32);

    fn mtg_nano_time() -> i64;
}

fn log(str: &str, level: i32) {
    unsafe {
        mtg_log(
            (str.as_ptr() as usize)
                .try_into()
                .expect("couldn't cast log str to i32"),
            str.len().try_into().expect("couldn't cast log len to i32"),
            level,
        );
    }
}

/*fn draw_inbuilt_sprite(x: i32, y: i32, name: &str) {
    unsafe {
        mtg_draw_inbuilt_sprite(
            x,
            y,
            (name.as_ptr() as usize)
                .try_into()
                .expect("couldn't cast draw_sprite name to i32"),
            name.len()
                .try_into()
                .expect("couldn't cast draw_sprite name len to i32"),
        );
    }
}

fn play_sound(id: &str, category: SoundCategory, volume: f32, pitch: f32) {
    unsafe {
        mtg_play_sound(
            (id.as_ptr() as usize)
                .try_into()
                .expect("couldn't cast play_sound id to i32"),
            id.len()
                .try_into()
                .expect("couldn't cast play_sound id len to i32"),
            category as i32,
            volume,
            pitch,
        );
    }
}*/

fn draw_text(text: &str, x: i32, y: i32, size: i32, argb: i32) {
    unsafe {
        mtg_draw_text(
            (text.as_ptr() as usize)
                .try_into()
                .expect("could not cast draw_centered_text text to i32"),
            text.len()
                .try_into()
                .expect("could not cast draw_centered_text text len to i32"),
            x,
            y,
            size,
            argb,
        );
    }
}

#[repr(C)]
#[derive(Clone, Copy, Debug)]
pub enum ClickType {
    Left,
    Right,
}

#[repr(C)]
#[derive(Clone, Copy, Debug)]
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

type Data = (i32, i64);

fn get_data<'a>(data_ptr: i32) -> Option<&'a mut Data> {
    unsafe { (data_ptr as *mut Data).as_mut() }
}

fn per_pixel(coord: Vec2, ray_origin: Vec3) -> Vec4 {
    let ray_direction = Vec3::new(coord.x, coord.y, -1.0);
    const RADIUS: f32 = 0.5;

    let a = ray_direction.dot(ray_direction);
    let b = 2.0 * ray_origin.dot(ray_direction);
    let c = ray_origin.dot(ray_origin) - RADIUS * RADIUS;

    let discriminant = b * b - 4.0 * a * c;

    if discriminant < 0.0 {
        return Vec4::new(0.0, 0.0, 0.0, 1.0);
    }

    let closest_t = (-b - discriminant.sqrt()) / (2.0 * a);

    let hit_point = ray_direction * closest_t + ray_origin;
    let normal = hit_point.normalize();

    let light_dir = Vec3::new(-1.0, -1.0, -1.0).normalize();
    let d = normal.dot(-light_dir).max(0.0);

    let mut pixel_color = Vec4::new(1.0, 0.0, 1.0, 1.0) * d;
    pixel_color.w = 1.0;
    pixel_color
}

#[no_mangle]
pub extern "C" fn mtg_draw(data_ptr: i32) {
    let (degrees, last_time) = get_data(data_ptr).expect("no data in draw");
    let radians = (*degrees as f32).to_radians();
    let ray_origin = Vec3::new(radians.sin(), 0.0, radians.cos());
    let (width, height) = unsafe { (mtg_get_width(), mtg_get_height()) };
    for x in 0..width {
        for y in 0..height {
            let coord = Vec2::new(
                x as f32 / width as f32 * 2.0 - 1.0,
                (width - y) as f32 / width as f32 * 2.0 - 1.0,
            );
            let pixel_color = per_pixel(coord, ray_origin);
            let color = ((pixel_color.w * 255.0) as i32) << 24
                | ((pixel_color.x * 255.0) as i32) << 16
                | ((pixel_color.y * 255.0) as i32) << 8
                | ((pixel_color.z * 255.0) as i32);
            unsafe { mtg_set_argb(x, y, color) }
        }
    }
    *degrees += 1;

    let current_time = unsafe { mtg_nano_time() };
    let elapsed = current_time - *last_time;
    let fps = 60.0 * 16.0 / (elapsed / 1000000) as f64;
    draw_text(&format!("{fps:.3}"), 0, 0, 8, 0xFFFFFFFFu32 as i32);
    *last_time = current_time;
}

#[no_mangle]
pub extern "C" fn mtg_on_click(_data_ptr: i32, _type: ClickType, _x: i32, _y: i32) {}

#[no_mangle]
pub extern "C" fn mtg_alloc_data() -> i32 {
    std::panic::set_hook(Box::new(|info| log(&info.to_string(), 40)));
    Box::leak(Box::<Data>::default()) as *mut _ as i32
}
