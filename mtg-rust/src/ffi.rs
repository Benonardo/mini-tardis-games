#[link(wasm_import_module = "mini_tardis_games")]
extern "C" {
    pub(super) fn mtg_log(message_address: i32, message_len: i32, level: i32);
    pub(super) fn mtg_nano_time() -> i64;
    pub(super) fn mtg_save_persistent_data(data_address: i32, data_len: i32);
    pub(super) fn mtg_get_persistent_data_len() -> i32;
    pub(super) fn mtg_get_persistent_data(data_address: i32);

    pub(super) fn mtg_random_i32() -> i32;
    pub(super) fn mtg_play_sound(id_address: i32, id_len: i32, category: i32, volume: f32, pitch: f32);
    pub(super) fn mtg_close_app() -> !;

    pub(super) fn mtg_get_width() -> i32;
    pub(super) fn mtg_get_height() -> i32;
    pub(super) fn mtg_get_raw(x: i32, y: i32) -> i32;
    pub(super) fn mtg_set_raw(x: i32, y: i32, color: i32);
    pub(super) fn mtg_set_rgb(x: i32, y: i32, color: i32);
    pub(super) fn mtg_set_argb(x: i32, y: i32, color: i32);
    pub(super) fn mtg_draw_inbuilt_sprite(x: i32, y: i32, name_address: i32, name_len: i32);
    pub(super) fn mtg_draw_text(x: i32, y: i32, text_address: i32, text_len: i32, size: i32, argb: i32);
}