#ifndef MTG_C_H
# define MTG_C_H

#include <stdint.h>

# ifdef __cplusplus
extern "C" {
# endif
// Provided functions

__attribute__((import_module("mini_tardis_games"))) void mtg_log(int32_t message_address, int32_t message_len, int32_t level);
__attribute__((import_module("mini_tardis_games"))) int64_t mtg_nano_time(void);
__attribute__((import_module("mini_tardis_games"))) void mtg_save_persistent_data(int32_t data_address, int32_t data_len);
__attribute__((import_module("mini_tardis_games"))) int32_t mtg_get_persistent_data_len(void);
__attribute__((import_module("mini_tardis_games"))) void mtg_get_persistent_data(int32_t data_address);

__attribute__((import_module("mini_tardis_games"))) int32_t mtg_random_i32(void);
__attribute__((import_module("mini_tardis_games"))) void mtg_play_sound(int32_t id_address, int32_t id_len, int32_t category, float volume, float pitch);
__attribute__((import_module("mini_tardis_games"))) _Noreturn void mtg_close_app(void);

__attribute__((import_module("mini_tardis_games"))) int32_t mtg_get_width(void);
__attribute__((import_module("mini_tardis_games"))) int32_t mtg_get_height(void);
__attribute__((import_module("mini_tardis_games"))) int32_t mtg_get_raw(int32_t x, int32_t y);
__attribute__((import_module("mini_tardis_games"))) void mtg_set_raw(int32_t x, int32_t y, int32_t color);
__attribute__((import_module("mini_tardis_games"))) void mtg_set_rgb(int32_t x, int32_t y, int32_t color);
__attribute__((import_module("mini_tardis_games"))) void mtg_set_argb(int32_t x, int32_t y, int32_t color);
__attribute__((import_module("mini_tardis_games"))) void mtg_draw_inbuilt_sprite(int32_t x, int32_t y, int32_t name_address, int32_t name_len);
__attribute__((import_module("mini_tardis_games"))) void mtg_draw_text(int32_t x, int32_t y, int32_t text_address, int32_t text_len, int32_t size, int32_t argb);

# ifdef __cplusplus
}
# endif

// Helper enums

typedef enum mtg_log_level
{
    MTG_LOG_LEVEL_ERROR = 40,
    MTG_LOG_LEVEL_WARN = 30,
    MTG_LOG_LEVEL_INFO = 20,
    MTG_LOG_LEVEL_DEBUG = 10,
    MTG_LOG_LEVEL_TRACE = 0
}
mtg_log_level_t;

typedef enum mtg_sound_category
{
    MTG_SOUND_CATEGORY_MASTER,
    MTG_SOUND_CATEGORY_MUSIC,
    MTG_SOUND_CATEGORY_RECORDS,
    MTG_SOUND_CATEGORY_WEATHER,
    MTG_SOUND_CATEGORY_BLOCKS,
    MTG_SOUND_CATEGORY_HOSTILE,
    MTG_SOUND_CATEGORY_NEUTRAL,
    MTG_SOUND_CATEGORY_PLAYERS,
    MTG_SOUND_CATEGORY_AMBIENT,
    MTG_SOUND_CATEGORY_VOICE
}
mtg_sound_category_t;

typedef enum mtg_click_type
{
    MTG_CLICK_TYPE_LEFT,
    MTG_CLICK_TYPE_RIGHT
}
mtg_click_type_t;

#endif
