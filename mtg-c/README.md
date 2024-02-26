# How to compile this
`clang -O -c -target wasm32 -nostdlib -Wall -pesrc/counter.c`  
`clang -O -c -target wasm32 -nostdlib -Wall src/malloc0.c` (could issue a warning regarding the `malloc` definition, ignore it)  
`wasm-ld --no-entry -o counter-c.wasm --import-undefined -O counter.o malloc0.o`  
`wasm-opt -O -o counter-c.wasm counter-c.wasm` (optional)  