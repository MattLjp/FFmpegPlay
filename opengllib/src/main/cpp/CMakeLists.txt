#声明cmake版本号
cmake_minimum_required(VERSION 3.10.2)

# 支持C++11标准和GNU扩展特性
set(CMAKE_CXX_FLAGS "${CMAKE_CXX_FLAGS} -std=gnu++11")

# 1. 定义so库和头文件所在目录，方面后面使用
set(libname opengllib)
set(opengl_lib_dir ${CMAKE_SOURCE_DIR}/../cmakeLibs/${ANDROID_ABI})

#项目名称
project("libname")

# 2. 添加头文件目录
include_directories(
        utils
        render
        drawer
        #        sample
        #        model
        #        egl
        #        looper

        glm
        inc
        inc/assimp
        inc/freetype_2_9_1
        opencv_3_0_0
)

# Creates and names a library, sets it as either STATIC
# or SHARED, and provides the relative paths to its source code.
# You can define multiple libraries, and CMake builds them for you.
# Gradle automatically packages shared libraries with your APK.
file(GLOB src-files
        ${CMAKE_SOURCE_DIR}/*.cpp
        ${CMAKE_SOURCE_DIR}/render/*.cpp
        ${CMAKE_SOURCE_DIR}/utils/*.cpp
        ${CMAKE_SOURCE_DIR}/drawer/*.cpp
        #        ${CMAKE_SOURCE_DIR}/egl/*.cpp
        #        ${CMAKE_SOURCE_DIR}/looper/*.cpp
        )

# 配置目标so库编译信息
add_library( # Sets the name of the library.
        ${libname}

        # Sets the library as a shared library.
        SHARED

        # Provides a relative path to your source file(s).
        ${src-files}
        )

# 查找代码中使用到的系统库
find_library( # Sets the name of the path variable.
        log-lib

        # Specifies the name of the NDK library that
        # you want CMake to locate.
        log)

# 3. 添加ffmpeg相关的so库
add_library(assimp SHARED IMPORTED)
set_target_properties(assimp
        PROPERTIES IMPORTED_LOCATION
        ${opengl_lib_dir}/libassimp.so)


add_library(opencv_java3 SHARED IMPORTED)
set_target_properties(opencv_java3
        PROPERTIES IMPORTED_LOCATION
        ${opengl_lib_dir}/libopencv_java3.so)

add_library(freetype STATIC IMPORTED)
set_target_properties(freetype
        PROPERTIES IMPORTED_LOCATION
        ${opengl_lib_dir}/libfreetype.a)

#ASAN 需要 NDK20 编译，Android P 以上运行
set(third-party-libs
        freetype
        assimp
        opencv_java3)

target_link_libraries( # Specifies the target library.
        # 指定目标库，native-lib 是在上面 add_library 中配置的目标库
        ${libname}

        # Links the target library to the log library
        # included in the NDK.
        ${log-lib}
        ${third-party-libs}
        EGL
        GLESv3
        android
        )

