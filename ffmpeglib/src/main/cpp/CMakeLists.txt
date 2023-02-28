#声明cmake版本号
cmake_minimum_required(VERSION 3.10.2)

# 支持C++11标准和GNU扩展特性
set(CMAKE_CXX_FLAGS "${CMAKE_CXX_FLAGS} -std=gnu++11")

# 1. 定义so库和头文件所在目录，方面后面使用
set(libname ffmpeglib)
set(ffmpeg_lib_dir ${CMAKE_SOURCE_DIR}/../cmakeLibs/${ANDROID_ABI})

#项目名称
project("libname")

# 2. 添加头文件目录
include_directories(
        ffmpeg/include
        glm
        inc
        inc/assimp
        inc/freetype_2_9_1
        opencv_3_0_0

        utils
        media/player
        media/decoder
        media/decoder/audio
        media/decoder/video
        media/render
        media/render/audio
        media/render/video
        media/encoder
        media/encoder/audio
        media/encoder/video
        media/muxer

        opengl
        opengl/egl
        opengl/drawer
        opengl/drawer/proxy
)

# 3. 添加ffmpeg相关的so库
add_library(avutil
        SHARED
        IMPORTED)
set_target_properties(avutil
        PROPERTIES IMPORTED_LOCATION
        ${ffmpeg_lib_dir}/libavutil.so)

add_library(swresample
        SHARED
        IMPORTED)
set_target_properties(swresample
        PROPERTIES IMPORTED_LOCATION
        ${ffmpeg_lib_dir}/libswresample.so)

add_library(avcodec
        SHARED
        IMPORTED)
set_target_properties(avcodec
        PROPERTIES IMPORTED_LOCATION
        ${ffmpeg_lib_dir}/libavcodec.so)

add_library(avfilter
        SHARED
        IMPORTED)
set_target_properties(avfilter
        PROPERTIES IMPORTED_LOCATION
        ${ffmpeg_lib_dir}/libavfilter.so)

add_library(swscale
        SHARED
        IMPORTED)
set_target_properties(swscale
        PROPERTIES IMPORTED_LOCATION
        ${ffmpeg_lib_dir}/libswscale.so)

add_library(avformat
        SHARED
        IMPORTED)
set_target_properties(avformat
        PROPERTIES IMPORTED_LOCATION
        ${ffmpeg_lib_dir}/libavformat.so)

add_library(assimp SHARED IMPORTED)
set_target_properties(assimp
        PROPERTIES IMPORTED_LOCATION
        ${ffmpeg_lib_dir}/libassimp.so)


add_library(opencv_java3 SHARED IMPORTED)
set_target_properties(opencv_java3
        PROPERTIES IMPORTED_LOCATION
        ${ffmpeg_lib_dir}/libopencv_java3.so)

add_library(freetype STATIC IMPORTED)
set_target_properties(freetype
        PROPERTIES IMPORTED_LOCATION
        ${ffmpeg_lib_dir}/libfreetype.a)

# 查找代码中使用到的系统库
find_library( # Sets the name of the path variable.
        log-lib

        # Specifies the name of the NDK library that
        # you want CMake to locate.
        log)

file(GLOB src-files
        # 工具
        ${CMAKE_SOURCE_DIR}/utils/*.c

        # 播放器
        ${CMAKE_SOURCE_DIR}/media/player/*.cpp

        # 解码器
        ${CMAKE_SOURCE_DIR}/media/decoder/*.cpp
        ${CMAKE_SOURCE_DIR}/media/decoder/video/*.cpp
        ${CMAKE_SOURCE_DIR}/media/decoder/audio/*.cpp

        # 编码器
        ${CMAKE_SOURCE_DIR}/media/encoder/*.cpp
        ${CMAKE_SOURCE_DIR}/media/encoder/video/*.cpp
        ${CMAKE_SOURCE_DIR}/media/encoder/audio/*.cpp

        # 打包器
        ${CMAKE_SOURCE_DIR}/media/muxer/*.cpp

        # 渲染器
        ${CMAKE_SOURCE_DIR}/media/render/video/*.cpp
        ${CMAKE_SOURCE_DIR}/media/render/audio/*.cpp

        #Opengl
        ${CMAKE_SOURCE_DIR}/opengl/*.cpp
        ${CMAKE_SOURCE_DIR}/opengl/egl/*.cpp
        ${CMAKE_SOURCE_DIR}/opengl/drawer/*.cpp
        ${CMAKE_SOURCE_DIR}/opengl/drawer/proxy/*.cpp
        )

# 配置目标so库编译信息
add_library( # Sets the name of the library.
        ${libname}

        # Sets the library as a shared library.
        SHARED

        # Provides a relative path to your source file(s).
        native-lib.cpp

        ${src-files}
        )


set(third-party-libs
        avutil
        swresample
        avcodec
        avfilter
        swscale
        avformat
        freetype
        assimp
        opencv_java3
        )

# 指定编译目标库时，cmake要链接的库
target_link_libraries(

        # 指定目标库，native-lib 是在上面 add_library 中配置的目标库
        ${libname}

        # 连接 FFmpeg 相关的库
        ${third-party-libs}

        android
        # 打开opensl es支持
        EGL
        GLESv3
        OpenSLES
        # Links the target library to the log library
        # included in the NDK.
        ${log-lib}
)
