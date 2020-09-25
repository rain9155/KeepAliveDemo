# Sets the minimum version of CMake required to build your native library.
# This ensures that a certain set of CMake features is available to
# your build.
cmake_minimum_required(VERSION 3.4.1)

find_library( # Defines the name of the path variable that stores the
        # location of the NDK library.
        log

        # Specifies the name of the NDK library that
        # CMake needs to locate.
        log )

set(CMAKE_LIBRARY_OUTPUT_DIRECTORY ${PROJECT_SOURCE_DIR}/libs/${ANDROID_ABI})

# Specifies a library name, specifies whether the library is STATIC or
# SHARED, and provides relative paths to the source code. You can
# define multiple libraries by adding multiple add_library() commands,
# and CMake builds them for you. When you build your app, Gradle
# automatically packages shared libraries with your APK.
add_library( # Specifies the name of the library.
             daemon

             # Sets the library as a shared library.
             SHARED

             # Provides a relative path to your source file(s).
             src/main/jni/daemon.c )


# Links your native library against one or more other native libraries.
target_link_libraries( # Specifies the target library.
                       daemon

                       # Links the log library to the target library.
                       ${log} )

set(CMAKE_RUNTIME_OUTPUT_DIRECTORY ${PROJECT_SOURCE_DIR}/src/main/assets/${ANDROID_ABI})

add_executable(
        supervisor
        src/main/jni/supervisor.c )

target_link_libraries(
        supervisor
        ${log} )




