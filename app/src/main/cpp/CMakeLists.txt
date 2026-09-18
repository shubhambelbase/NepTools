cmake_minimum_required(VERSION 3.22.1)

project("neptools-security")

set(CMAKE_CXX_STANDARD 20)
set(CMAKE_CXX_STANDARD_REQUIRED ON)

# Compile-time hardening. Link-time hardening is applied with target_link_options below,
# because passing -Wl,... through CMAKE_CXX_FLAGS also feeds it to the compile steps.
set(CMAKE_CXX_FLAGS "${CMAKE_CXX_FLAGS} -O3 -fvisibility=hidden -fvisibility-inlines-hidden -fstack-protector-strong -D_FORTIFY_SOURCE=2")

add_library(
    neptools-security
    SHARED
    native-lib.cpp
)

# Strip all symbols from the final shared library to raise the cost of reverse engineering.
target_link_options(
    neptools-security
    PRIVATE
    -Wl,-z,relro
    -Wl,-z,now
    -Wl,--strip-all
    -Wl,--exclude-libs,ALL
)

find_library(
    log-lib
    log
)

target_link_libraries(
    neptools-security
    ${log-lib}
)
