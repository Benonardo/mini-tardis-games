use glam::{Vec2, Vec3, Vec4};
use mtg_rust::{Canvas, ClickType, Game, LogLevel, Screen};

mtg_rust::game_impl!(RayTracer);

#[derive(Default)]
pub struct RayTracer {
    degrees: u16,
}

const RADIUS: f32 = 0.5;

fn per_pixel(coord: Vec2, ray_origin: Vec3) -> Vec4 {
    let ray_direction = Vec3::new(coord.x, coord.y, -1.0);

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

impl Game for RayTracer {
    #[allow(clippy::cast_possible_truncation, clippy::cast_precision_loss)]
    fn draw(&mut self, _screen: &Screen, canvas: &Canvas) {
        let radians = f32::from(self.degrees).to_radians();
        let ray_origin = Vec3::new(radians.sin(), 0.0, radians.cos());
        let width = canvas.get_width();
        let height = canvas.get_height();
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
                canvas.set_pixel_argb(x, y, color);
            }
        }
    }

    fn on_click(&mut self, _screen: &Screen, _click_type: ClickType, _x: i32, _y: i32) {}

    fn draw_background(&mut self, _screen: &Screen, _canvas: &Canvas) {}

    fn screen_tick(&mut self, _screen: &Screen) {
        self.degrees += 1;
        if self.degrees >= 360 {
            self.degrees = 0;
        }
    }

    fn screen_open(&mut self, _screen: &Screen) {
        mtg_rust::log("opened ray tracer", LogLevel::Info);
    }

    fn screen_close(&mut self, _screen: &Screen) {
        mtg_rust::log("closed ray tracer", LogLevel::Info);
    }
}
