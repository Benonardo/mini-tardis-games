/*
    Modified version of https://github.com/muayyad-alsadi/malloc0/blob/7527c02685e1109434f33c6812d4165ae8edb167/malloc0.h, licensed under the Apache License 2.0
*/

#ifndef size_t
#define size_t unsigned int
#endif

#ifndef _mem_flag_used
#define _mem_alignment 4
#define _size_t_bytes sizeof(size_t)
#define _mem_flag_used 0xbf82583a
#define _mem_flag_free 0xab34d705
#endif

size_t memory_size(void);
size_t memory_grow(int delta);
void free_all(void);
void free_all_mark(void);
void* malloc(size_t n);
void free(void* p);
