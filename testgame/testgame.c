#include <stdint.h>
#include <string.h>
#include <stdio.h>
#include <emscripten.h>

typedef enum click_type
{
    LEFT,
    RIGHT
} click_type;

#ifdef __cplusplus
extern "C"
#endif

    void
    mtg_log(int32_t len, int32_t offset, int32_t level) __attribute__((__import_module__("mini_tardis_games")));
int32_t mtg_get_width(void) __attribute__((__import_module__("mini_tardis_games")));
int32_t mtg_get_height(void) __attribute__((__import_module__("mini_tardis_games")));
void mtg_set_raw(int32_t x, int32_t y, int32_t color) __attribute__((__import_module__("mini_tardis_games")));
int32_t mtg_random_i32(void) __attribute__((__import_module__("mini_tardis_games")));

EMSCRIPTEN_KEEPALIVE
void mtg_draw(int32_t data_ptr)
{
    int width = mtg_get_width();
    int height = mtg_get_height();
    for (int32_t x = 0; x < width; x++)
    {
        for (int32_t y = 0; y < height; y++)
        {
            mtg_set_raw(x, y, mtg_random_i32() & 127);
        }
    }
}

EMSCRIPTEN_KEEPALIVE
void mtg_on_click(int32_t data_ptr, click_type type, int32_t x, int32_t y)
{
    const char *type_str = type == LEFT ? "LEFT" : "RIGHT";
    char log_str[32];
    sprintf(log_str, "%s at %d,%d", type_str, x, y);
    mtg_log(strlen(log_str), (int32_t)log_str, 20);
}

EMSCRIPTEN_KEEPALIVE
int32_t mtg_alloc_data(void)
{
    return -1;
}

#ifdef __cplusplus
}
#endif
