use mtg_rust::{game_impl, Game};

game_impl!(Counter);

struct Counter {
    count: u64,
    cached_str: String,
}

impl Game for Counter {
    fn initialize() -> Self
    where
        Self: Sized,
    {
        let data = mtg_rust::get_persistent_data();
        let count = if data.is_empty() {
            0
        } else {
            let data: [u8; 8] = data
                .try_into()
                .expect("couldn't convert counter persistent data to count");
            u64::from_ne_bytes(data)
        };
        Self {
            count,
            cached_str: count.to_string(),
        }
    }

    fn draw(&mut self, _screen: &mtg_rust::Screen, canvas: &mtg_rust::Canvas) {
        canvas.draw_text(
            0,
            0,
            &self.cached_str,
            96 / self.cached_str.len() as i32,
            0xFFFFFFFFu32 as i32,
        );
    }

    fn on_click(
        &mut self,
        _screen: &mtg_rust::Screen,
        _click_type: mtg_rust::ClickType,
        _x: i32,
        _y: i32,
    ) {
        self.count += 1;
        self.cached_str = self.count.to_string();
    }

    fn screen_close(&mut self, _screen: &mtg_rust::Screen) {
        mtg_rust::save_persistent_data(&self.count.to_ne_bytes())
    }
}
