package com.matt.ffmpeglib.drawer

import android.Manifest
import android.content.Context
import android.content.Context.CAMERA_SERVICE
import android.content.pm.PackageManager
import android.graphics.SurfaceTexture
import android.hardware.camera2.*
import android.media.MediaRecorder
import android.opengl.GLES11Ext
import android.opengl.GLES30
import android.opengl.GLSurfaceView
import android.os.Handler
import android.os.HandlerThread
import android.util.Log
import android.util.Size
import android.view.Surface
import android.widget.Toast
import androidx.core.app.ActivityCompat
import com.matt.ffmpeglib.R
import com.matt.ffmpeglib.utils.ResReadUtils
import com.matt.ffmpeglib.utils.ShaderUtils
import com.matt.ffmpeglib.utils.TextureUtils
import java.nio.ByteBuffer
import java.nio.ByteOrder


/**
 * Created by Liaojp on 2023/2/6.
 */
class CameraDrawer(private val context: Context, private val glSurfaceView: GLSurfaceView) : IDrawer {

    private val TAG = "CameraSurfaceRenderer"

    /**
     * 顶点坐标
     * (x,y)
     */
    private var POSITION_VERTEX = floatArrayOf(
        0f, 0f,  //顶点坐标V0
        1f, 1f,  //顶点坐标V1
        -1f, 1f,  //顶点坐标V2
        -1f, -1f,  //顶点坐标V3
        1f, -1f //顶点坐标V4
    )


    /**
     * 纹理坐标
     * (s,t)
     */
    private val TEX_VERTEX = floatArrayOf(
        0.5f, 0.5f,  //纹理坐标V0
        1f, 1f,  //纹理坐标V1
        0f, 1f,  //纹理坐标V2
        0f, 0.0f,  //纹理坐标V3
        1f, 0.0f //纹理坐标V4
    )

    /**
     * 索引
     */
    private val VERTEX_INDEX = shortArrayOf(
        0, 1, 2,  //V0,V1,V2 三个顶点组成一个三角形
        0, 2, 3,  //V0,V2,V3 三个顶点组成一个三角形
        0, 3, 4,  //V0,V3,V4 三个顶点组成一个三角形
        0, 4, 1 //V0,V4,V1 三个顶点组成一个三角形
    )

    private val vertexBuffer by lazy {
        //分配内存空间,每个浮点型占4字节空间
        ByteBuffer.allocateDirect(POSITION_VERTEX.size * 4)
            .order(ByteOrder.nativeOrder())
            .asFloatBuffer().also {
                //传入指定的坐标数据
                it.put(POSITION_VERTEX)
                it.position(0)
            }
    }

    private val texVertexBuffer by lazy {
        //分配内存空间,每个浮点型占4字节空间
        ByteBuffer.allocateDirect(TEX_VERTEX.size * 4)
            .order(ByteOrder.nativeOrder())
            .asFloatBuffer().also {
                //传入指定的坐标数据
                it.put(TEX_VERTEX)
                it.position(0)
            }
    }
    private val vertexIndexBuffer by lazy {
        //分配内存空间,每个Short占2字节空间
        ByteBuffer.allocateDirect(VERTEX_INDEX.size * 2)
            .order(ByteOrder.nativeOrder())
            .asShortBuffer().also {
                //传入指定的坐标数据
                it.put(VERTEX_INDEX)
                it.position(0)
            }
    }
    private var programId = 0
    private var textureId = 0

    private var shootWidth = 0
    private var shootHeight = 0

    private var mMatrix = FloatArray(16)

    //用于绘制到屏幕上的变换矩阵
    private val previewMatrix = FloatArray(16)

    //用于绘制拍照缩放的矩阵
    private val shootMatrix = FloatArray(16)


    /**
     * 相机实例
     */
    private var cameraDevice: CameraDevice? = null
    private var mSurfaceTexture: SurfaceTexture? = null
    private var mCameraManager: CameraManager? = null
    private var mThread: HandlerThread? = null
    private var mHandler: Handler? = null
    private var mVideoSize: Size? = null
    private var mPreviewSize: Size? = null
    private var cameraId = CameraCharacteristics.LENS_FACING_BACK
    private var mPreviewSession: CameraCaptureSession? = null
    private var isShoot = false
    private var imageBuffer: ByteBuffer? = null
    var callback: ((ByteBuffer, Int, Int) -> Unit) = { _, _, _ -> }

    /**
     * 矩阵索引
     */
    private var uTextureMatrixHandler = 0
    private var uTextureSamplerHandler = 0
    private var vChangeTypeHandler = 0
    private var vChangeDataHandler = 0

    private var type = 0
    private var data = floatArrayOf(0.0f, 0.0f, 0.0f)

    private val fFrame = IntArray(1)
    private val fTexture = IntArray(1)

    fun setData(type: Int, data: FloatArray) {
        this.type = type
        this.data = data
    }

    override fun create() {
        //编译
        val vertexShaderId =
            ShaderUtils.loadVertexShader(ResReadUtils.readResource(context, R.raw.camera_vertex_shader))
        val fragmentShaderId =
            ShaderUtils.loadFragmentShader(ResReadUtils.readResource(context, R.raw.camera_fragment_shader))
        //链接程序片段
        programId = ShaderUtils.createProgram(vertexShaderId, fragmentShaderId)

        //获取Shader中定义的变量在program中的位置
        uTextureMatrixHandler = GLES30.glGetUniformLocation(programId, "uTextureMatrix")
        uTextureSamplerHandler = GLES30.glGetUniformLocation(programId, "yuvTexSampler")
        vChangeTypeHandler = GLES30.glGetUniformLocation(programId, "vChangeType")
        vChangeDataHandler = GLES30.glGetUniformLocation(programId, "vChangeData")

        //使用程序片段
        GLES30.glUseProgram(programId)

        //加载纹理
        textureId = TextureUtils.createTextureOesID()
        startBackgroundThread()
        initCamera2()
        createEnvi()
    }

    override fun setWorldSize(worldW: Int, worldH: Int) {
        shootWidth = worldW
        shootHeight = worldH
    }

    override fun draw() {
        if (isShoot) {
            isShoot = false
            GLES30.glBindFramebuffer(GLES30.GL_FRAMEBUFFER, fFrame[0])
            //为FrameBuffer挂载Texture[0]来存储颜色
            GLES30.glFramebufferTexture2D(
                GLES30.GL_FRAMEBUFFER, GLES30.GL_COLOR_ATTACHMENT0,
                GLES30.GL_TEXTURE_2D, fTexture[0], 0
            )
//            mMatrix = shootMatrix
            drawPreview()

            GLES30.glReadPixels(
                0, 0, shootWidth, shootHeight,
                GLES30.GL_RGBA, GLES30.GL_UNSIGNED_BYTE, imageBuffer
            )
            callback.invoke(imageBuffer!!, shootWidth, shootHeight)
            GLES30.glBindFramebuffer(GLES30.GL_FRAMEBUFFER, 0)
        } else {
//            mMatrix = shootMatrix
            drawPreview()
        }
    }

    private fun drawPreview(){
        GLES30.glClear(GLES30.GL_COLOR_BUFFER_BIT)

        //使用程序片段
        GLES30.glUseProgram(programId)

        // 更新纹理
        mSurfaceTexture?.updateTexImage()
        mSurfaceTexture?.getTransformMatrix(previewMatrix)

        //将纹理矩阵传给片段着色器
        GLES30.glUniformMatrix4fv(uTextureMatrixHandler, 1, false, previewMatrix, 0)

        //激活纹理单元0
        GLES30.glActiveTexture(GLES30.GL_TEXTURE0)
        //绑定外部纹理到纹理单元0
        GLES30.glBindTexture(GLES11Ext.GL_TEXTURE_EXTERNAL_OES, textureId)
        //将此纹理单元床位片段着色器的uTextureSampler外部纹理采样器
        GLES30.glUniform1i(uTextureSamplerHandler, 0)


        GLES30.glUniform1i(vChangeTypeHandler, type)
        GLES30.glUniform3fv(vChangeDataHandler, 1, data, 0)

        GLES30.glEnableVertexAttribArray(0)
        GLES30.glVertexAttribPointer(0, 2, GLES30.GL_FLOAT, false, 0, vertexBuffer)

        GLES30.glEnableVertexAttribArray(1)
        GLES30.glVertexAttribPointer(1, 2, GLES30.GL_FLOAT, false, 0, texVertexBuffer)

        // 绘制
        GLES30.glDrawElements(GLES30.GL_TRIANGLES, VERTEX_INDEX.size, GLES30.GL_UNSIGNED_SHORT, vertexIndexBuffer)

    }


    override fun release() {
        deleteEnvi()
        GLES30.glDisableVertexAttribArray(0)
        GLES30.glDisableVertexAttribArray(1)
        GLES30.glDeleteProgram(programId)
        closeCamera()
        stopBackgroundThread()
    }


    private fun initCamera2() {
        mCameraManager = context.getSystemService(CAMERA_SERVICE) as CameraManager
        val characteristics = mCameraManager!!.getCameraCharacteristics(cameraId.toString())
        val map = characteristics.get(CameraCharacteristics.SCALER_STREAM_CONFIGURATION_MAP)
        //获取可用的录制视频的尺寸
        val videoSizes = map!!.getOutputSizes(MediaRecorder::class.java)
        mVideoSize = videoSizes[0]
        //获取可用的用于渲染图像的尺寸
        val previewSizes = map.getOutputSizes(SurfaceTexture::class.java)
        mPreviewSize = previewSizes[0]


        runCatching {
            if (ActivityCompat.checkSelfPermission(
                    context,
                    Manifest.permission.CAMERA
                ) != PackageManager.PERMISSION_GRANTED
            ) {
                Toast.makeText(context, "缺少权限", Toast.LENGTH_SHORT).show()
                return
            }
            mCameraManager!!.openCamera(cameraId.toString(), object : CameraDevice.StateCallback() {
                override fun onOpened(camera: CameraDevice) {
                    cameraDevice = camera
                    createCameraCaptureSession()
                }

                override fun onDisconnected(camera: CameraDevice) {
                    cameraDevice?.close()
                    cameraDevice = null
                }

                override fun onError(camera: CameraDevice, error: Int) {
                    Log.e(TAG, "cameraManager打开摄像头失败")
                }
            }, mHandler)
        }.onFailure {
            Log.e(TAG, "cameraManager访问摄像头失败")
        }
    }

    private fun createCameraCaptureSession() {
        runCatching {
            val builder = cameraDevice!!.createCaptureRequest(CameraDevice.TEMPLATE_PREVIEW)
            //根据纹理ID创建SurfaceTexture
            mSurfaceTexture = SurfaceTexture(textureId)
            mSurfaceTexture!!.setOnFrameAvailableListener { glSurfaceView.requestRender() }
            //给SurfaceTexture设置缓冲区的大小，这里就是我们预览的尺寸
            mSurfaceTexture!!.setDefaultBufferSize(mPreviewSize!!.width, mPreviewSize!!.height);
            val surface = Surface(mSurfaceTexture)
            builder.addTarget(surface)

            val sessionPreviewCallback = object : CameraCaptureSession.StateCallback() {
                override fun onConfigured(session: CameraCaptureSession) {
                    mPreviewSession = session
                    try {
                        // 设置自动对焦
                        builder.set(CaptureRequest.CONTROL_AF_MODE, CaptureRequest.CONTROL_AF_MODE_CONTINUOUS_VIDEO)
                        //开始预览
                        session.setRepeatingRequest(builder.build(), null, mHandler)
                    } catch (e: CameraAccessException) {
                        e.printStackTrace()
                    }
                }

                override fun onConfigureFailed(session: CameraCaptureSession) {}
            }
            cameraDevice!!.createCaptureSession(listOf(surface), sessionPreviewCallback, mHandler)
        }.onFailure {
            Log.e(TAG, "cameraManager访问摄像头失败")
        }
    }

    fun takePhoto() {
        isShoot = true
    }

    fun closeCamera() {
        mPreviewSession?.close()
        mPreviewSession = null
        cameraDevice!!.close()
        cameraDevice = null
    }

    private fun startBackgroundThread() {
        mThread = HandlerThread("camera2")
        mThread!!.start()
        mHandler = Handler(mThread!!.looper)
    }

    private fun stopBackgroundThread() {
        mThread?.quitSafely()
        mThread = null
        mHandler = null
    }

    private fun createEnvi() {
        //生成Frame Buffer
        GLES30.glGenFramebuffers(1, fFrame, 0)
        GLES30.glGenTextures(1, fTexture, 0)
        GLES30.glBindTexture(GLES30.GL_TEXTURE_2D, fTexture[0])
        GLES30.glTexImage2D(
            GLES30.GL_TEXTURE_2D, 0, GLES30.GL_RGBA, shootWidth, shootHeight, 0,
            GLES30.GL_RGBA, GLES30.GL_UNSIGNED_BYTE, null
        )
        TextureUtils.setTexParameter()
        // 取消绑定纹理
        GLES30.glBindTexture(GLES30.GL_TEXTURE_2D, 0)
        imageBuffer = ByteBuffer.allocate(shootWidth * shootHeight * 4)
    }

    private fun deleteEnvi() {
        GLES30.glDeleteTextures(1, fTexture, 0)
        GLES30.glDeleteFramebuffers(1, fFrame, 0)
    }
}