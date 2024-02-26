#include "mtg-c.h"
#include "malloc0.h"

#include <stdbool.h>

typedef struct counter 
{
    uint64_t count;
    char* cached_str;
    uint8_t cached_digits;
} 
counter_t;

static void to_string(uint64_t value, char** string, uint8_t* digits)
{
    {
        uint64_t temp = value;
        *digits = 0;
        do {
            (*digits)++;
            temp /= 10;
        } while (temp != 0);
    }

    if (*digits != 1 || **string != '0')
    {
        free(*string);
    }
    *string = malloc(*digits);

    for (uint8_t digit = 0; digit < *digits; digit++)
    {
        (*string)[*digits - digit - 1] = '0' + value % 10;
        value /= 10;
    }
}

__attribute__((export_name("mtg_alloc_data"))) int32_t mtg_alloc_data(void)
{
    counter_t* counter = malloc(sizeof(counter_t));
    int32_t len = mtg_get_persistent_data_len();
    if (len == 0)
    {
        counter->count = 0;
        counter->cached_str = "0";
        counter->cached_digits = 1;
    }
    else if (len == sizeof(uint64_t))
    {
        mtg_get_persistent_data((int32_t)&counter->count);
        to_string(counter->count, &counter->cached_str, &counter->cached_digits);
    }
    else
    {
        const char error[] = "couldn't convert counter persistent data to count";
        mtg_log((int32_t)error, (int32_t)sizeof(error), MTG_LOG_LEVEL_ERROR);
        mtg_close_app();
    }
    return (int32_t)counter;
}

__attribute__((export_name("mtg_draw"))) void mtg_draw(int32_t data_ptr) 
{
    counter_t* counter = (counter_t*)data_ptr;
    mtg_draw_text(0, 0, (int32_t)counter->cached_str, (int32_t)counter->cached_digits, 96 / counter->cached_digits, 0xFFFFFFFF);
}

__attribute__((export_name("mtg_on_click"))) int32_t mtg_on_click(int32_t data_ptr, int32_t type, int32_t x, int32_t y)
{
    counter_t* counter = (counter_t*)data_ptr;
    counter->count++;
    to_string(counter->count, &counter->cached_str, &counter->cached_digits);

    return true;
}

__attribute__((export_name("mtg_screen_close"))) void mtg_screen_close(int32_t data_ptr)
{
    counter_t* counter = (counter_t*)data_ptr;
    mtg_save_persistent_data((int32_t)&counter->count, sizeof(counter->count));
}
