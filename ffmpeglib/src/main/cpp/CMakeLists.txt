#声明cmake版本号
cmake_minimum_required(VERSION 3.10.2)

# 支持C++11标准和GNU扩展特性
set(CMAKE_CXX_FLAGS "${CMAKE_CXX_FLAGS} -std=gnu++11")

# 1. 定义so库和头文件所在目录，方面后面使用
set(libname ffmpeglib)
set(ffmpeg_lib_dir ${CMAKE_SOURCE_DIR}/../cmakeLibs/${ANDROID_ABI})
set(ffmpeg_head_dir ${CMAKE_SOURCE_DIR}/ffmpeg)

#项目名称
project("libname")

# 2. 添加头文件目录
include_directories(
        ${ffmpeg_head_dir}/include
        ${CMAKE_SOURCE_DIR}/utils
        ${CMAKE_SOURCE_DIR}/media/player
        ${CMAKE_SOURCE_DIR}/media/decoder
        ${CMAKE_SOURCE_DIR}/media/decoder/audio
        ${CMAKE_SOURCE_DIR}/media/decoder/video
        ${CMAKE_SOURCE_DIR}/media/render
        ${CMAKE_SOURCE_DIR}/media/render/audio
        ${CMAKE_SOURCE_DIR}/media/render/video
        ${CMAKE_SOURCE_DIR}/media/encoder
        ${CMAKE_SOURCE_DIR}/media/encoder/audio
        ${CMAKE_SOURCE_DIR}/media/encoder/video
        ${CMAKE_SOURCE_DIR}/media/muxer
        ${CMAKE_SOURCE_DIR}/media/synthesizer
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


# 查找代码中使用到的系统库
find_library( # Sets the name of the path variable.
        log-lib

        # Specifies the name of the NDK library that
        # you want CMake to locate.
        log)


# 配置目标so库编译信息
add_library( # Sets the name of the library.
        ${libname}

        # Sets the library as a shared library.
        SHARED

        # Provides a relative path to your source file(s).
        native-lib.cpp

        # 工具
        ${CMAKE_SOURCE_DIR}/utils/timer.c

        # 播放器
        ${CMAKE_SOURCE_DIR}/media/player/player.cpp

        # 解码器
        ${CMAKE_SOURCE_DIR}/media/decoder/base_decoder.cpp
        ${CMAKE_SOURCE_DIR}/media/decoder/video/video_decoder.cpp
        ${CMAKE_SOURCE_DIR}/media/decoder/audio/audio_decoder.cpp

        # 编码器
        ${CMAKE_SOURCE_DIR}/media/encoder/base_encoder.cpp
        ${CMAKE_SOURCE_DIR}/media/encoder/video/video_encoder.cpp
        ${CMAKE_SOURCE_DIR}/media/encoder/audio/audio_encoder.cpp

        #        ${CMAKE_SOURCE_DIR}/media/const.h


        # 打包器
        ${CMAKE_SOURCE_DIR}/media/muxer/ff_repack.cpp
        ${CMAKE_SOURCE_DIR}/media/muxer/mp4_muxer.cpp

        # 渲染器
        ${CMAKE_SOURCE_DIR}/media/render/video/native_render.cpp
        #                ${CMAKE_SOURCE_DIR}/media/render/video/opengl_render/opengl_render.cpp
        ${CMAKE_SOURCE_DIR}/media/render/audio/opensl_render.cpp

        #        # OpenGL EGL核心
        #        ${CMAKE_SOURCE_DIR}/opengl/egl/egl_core.cpp
        #        ${CMAKE_SOURCE_DIR}/opengl/egl/egl_surface.cpp
        #
        #        # OpenGL绘制器
        #        ${CMAKE_SOURCE_DIR}/opengl/drawer/drawer.cpp
        #        ${CMAKE_SOURCE_DIR}/opengl/drawer/video_drawer.cpp
        #
        #        # OpenGL绘制代理器
        #        ${CMAKE_SOURCE_DIR}/opengl/drawer/proxy/drawer_proxy.cpp
        #        ${CMAKE_SOURCE_DIR}/opengl/drawer/proxy/def_drawer_proxy_impl.cpp
        #
        #        #合成器
        #                ${CMAKE_SOURCE_DIR}/media/synthesizer/synthesizer.cpp
        )


# 指定编译目标库时，cmake要链接的库
target_link_libraries(

        # 指定目标库，native-lib 是在上面 add_library 中配置的目标库
        ${libname}

        # 4. 连接 FFmpeg 相关的库
        avutil
        swresample
        avcodec
        avfilter
        swscale
        avformat

        -landroid
        # 打开opensl es支持
        OpenSLES
        -lEGL
        -lGLESv2
        # Links the target library to the log library
        # included in the NDK.
        ${log-lib}
)
