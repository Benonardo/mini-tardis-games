#include <stdint.h>

typedef enum click_type { LEFT, RIGHT } click_type;

#ifdef __cplusplus
extern "C" {
#endif

__attribute__((import_module("mini_tardis_games"))) void
mtg_log(int32_t len, int32_t offset, int32_t level);
__attribute__((import_module("mini_tardis_games"))) int32_t mtg_get_width(void);
__attribute__((import_module("mini_tardis_games"))) int32_t
mtg_get_height(void);
__attribute__((import_module("mini_tardis_games"))) void
mtg_set_raw(int32_t x, int32_t y, int32_t color);
__attribute__((import_module("mini_tardis_games"))) int32_t
mtg_random_i32(void);

void mtg_draw(int32_t data_ptr) {
  int32_t width = mtg_get_width();
  int32_t height = mtg_get_height();
  for (int32_t x = 0; x < width; x++) {
    for (int32_t y = 0; y < height; y++) {
      mtg_set_raw(x, y, mtg_random_i32() & 127);
    }
  }
}

void mtg_on_click(int32_t data_ptr, click_type type, int32_t x, int32_t y) {
  mtg_log(type == LEFT ? 4 : 5, (int32_t)(type == LEFT ? "LEFT" : "RIGHT"), 20);
}

int32_t mtg_alloc_data(void) { return -1; }

#ifdef __cplusplus
}
#endif
