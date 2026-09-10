cmake_minimum_required(VERSION 3.22.1)

project("neptools-security")

# Enforce strict symbol hiding and stack hardening flags
set(CMAKE_CXX_FLAGS "${CMAKE_CXX_FLAGS} -O3 -fvisibility=hidden -fvisibility-inlines-hidden -fstack-protector-strong -D_FORTIFY_SOURCE=2 -Wl,-z,relro,-z,now")

add_library(
    neptools-security
    SHARED
    native-lib.cpp
)

# Strip all symbols from final shared library to defeat reverse engineering
set_target_properties(
    neptools-security
    PROPERTIES
    LINK_FLAGS "-Wl,--strip-all -Wl,--exclude-libs,ALL"
)

find_library(
    log-lib
    log
)

target_link_libraries(
    neptools-security
    ${log-lib}
)
