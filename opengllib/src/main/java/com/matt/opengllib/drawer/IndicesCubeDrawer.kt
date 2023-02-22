package com.matt.opengllib.drawer

import android.opengl.GLES30
import com.matt.opengllib.utils.ShaderUtils
import java.nio.ByteBuffer
import java.nio.ByteOrder

/**
 * Created by Liaojp on 2023/2/1
 */
class IndicesCubeDrawer : IDrawer {
    private val vertexShaderCode =
        """
        #version 300 es
        layout (location = 0) in vec4 vPosition;
        layout (location = 1) in vec4 aColor;
        out vec4 vColor;
        void main() { 
             gl_Position  = vPosition;
             gl_PointSize = 10.0;
             vColor = aColor;
        }
        
        """.trimIndent()
    private val fragmentShaderCode =
        """
        #version 300 es
        precision mediump float;
        in vec4 vColor;
        out vec4 fragColor;
        void main() { 
             fragColor = vColor;
        }
        
        """.trimIndent()

    private val POSITION_COMPONENT_SIZE = 3
    private val COLOR_COMPONENT_SIZE = 4


    private val vertexBuffer by lazy {
        //分配内存空间,每个浮点型占4字节空间
        ByteBuffer.allocateDirect(vertexPoints.size * 4)
            .order(ByteOrder.nativeOrder())
            .asFloatBuffer().also {
                //传入指定的坐标数据
                it.put(vertexPoints)
                it.position(0)
            }
    }
    private val colorBuffer by lazy {
        //分配内存空间,每个浮点型占4字节空间
        ByteBuffer.allocateDirect(colors.size * 4)
            .order(ByteOrder.nativeOrder())
            .asFloatBuffer().also {
                //传入指定的坐标数据
                it.put(colors)
                it.position(0)
            }
    }
    private val indicesBuffer by lazy {
        //分配内存空间,每个Short占2字节空间
        ByteBuffer.allocateDirect(indices.size * 2)
            .order(ByteOrder.nativeOrder())
            .asShortBuffer().also {
                //传入指定的坐标数据
                it.put(indices)
                it.position(0)
            }
    }

    /**
     * 点的坐标
     */
    private val vertexPoints = floatArrayOf(
        //正面矩形
        0.25f, 0.25f, 0.0f,  //V0
        -0.75f, 0.25f, 0.0f, //V1
        -0.75f, -0.75f, 0.0f, //V2
        0.25f, -0.75f, 0.0f, //V3

        //背面矩形
        0.75f, -0.25f, 0.0f, //V4
        0.75f, 0.75f, 0.0f, //V5
        -0.25f, 0.75f, 0.0f, //V6
        -0.25f, -0.25f, 0.0f //V7
    )

    /**
     * 定义索引
     */
    private val indices = shortArrayOf(
        //背面
        5, 6, 7, 5, 7, 4,
        //左侧
        6, 1, 2, 6, 2, 7,
        //底部
        4, 7, 2, 4, 2, 3,
        //顶面
        5, 6, 7, 5, 7, 4,
        //右侧
        5, 0, 3, 5, 3, 4,
        //正面
        0, 1, 2, 0, 2, 3
    )

    /**
     * 立方体的顶点颜色
     */
    private val colors = floatArrayOf(
        0.3f, 0.4f, 0.5f, 1f,   //V0
        0.3f, 0.4f, 0.5f, 1f,   //V1
        0.3f, 0.4f, 0.5f, 1f,   //V2
        0.3f, 0.4f, 0.5f, 1f,   //V3
        0.6f, 0.5f, 0.4f, 1f,   //V4
        0.6f, 0.5f, 0.4f, 1f,   //V5
        0.6f, 0.5f, 0.4f, 1f,   //V6
        0.6f, 0.5f, 0.4f, 1f    //V7
    )

    // VertexBufferObject Ids
    private val vaoIds = IntArray(1)
    private val vboIds = IntArray(3)
    private var programId = 0

    override fun create() {
        //编译
        val vertexShaderId = ShaderUtils.loadVertexShader(vertexShaderCode)
        val fragmentShaderId = ShaderUtils.loadFragmentShader(fragmentShaderCode)
        //链接程序片段
        programId = ShaderUtils.createProgram(vertexShaderId, fragmentShaderId)
        GLES30.glUseProgram(programId)

        //生成1个缓冲ID
        GLES30.glGenVertexArrays(1, vaoIds, 0)
        //绑定VAO
        GLES30.glBindVertexArray(vaoIds[0])
        //生成3个缓冲ID
        GLES30.glGenBuffers(3, vboIds, 0)
        //绑定到顶点坐标数据缓冲
        GLES30.glBindBuffer(GLES30.GL_ARRAY_BUFFER, vboIds[0])
        //向顶点坐标数据缓冲送入数据
        GLES30.glBufferData(
            GLES30.GL_ARRAY_BUFFER, 4 * vertexPoints.size,
            vertexBuffer, GLES30.GL_STATIC_DRAW
        )

        //将顶点位置数据送入渲染管线
        GLES30.glVertexAttribPointer(
            0, POSITION_COMPONENT_SIZE,
            GLES30.GL_FLOAT, false, 0, 0
        )
        //启用顶点位置属性
        GLES30.glEnableVertexAttribArray(0)

        //绑定到顶点坐标数据缓冲
        GLES30.glBindBuffer(GLES30.GL_ARRAY_BUFFER, vboIds[1])
        //向顶点坐标数据缓冲送入数据
        GLES30.glBufferData(
            GLES30.GL_ARRAY_BUFFER, 4 * colors.size,
            colorBuffer, GLES30.GL_STATIC_DRAW
        )

        //将顶点位置数据送入渲染管线
        GLES30.glVertexAttribPointer(
            1, COLOR_COMPONENT_SIZE,
            GLES30.GL_FLOAT, false, 0, 0
        )
        //启用顶点位置属性
        GLES30.glEnableVertexAttribArray(1)
        GLES30.glBindBuffer(GLES30.GL_ARRAY_BUFFER, 0)

        //绑定到顶点坐标数据缓冲
        GLES30.glBindBuffer(GLES30.GL_ELEMENT_ARRAY_BUFFER, vboIds[2])
        //向顶点坐标数据缓冲送入数据
        GLES30.glBufferData(
            GLES30.GL_ELEMENT_ARRAY_BUFFER,
            2 * indices.size,
            indicesBuffer,
            GLES30.GL_STATIC_DRAW
        )

    }

    override fun draw() {
        //绑定VAO
        GLES30.glBindVertexArray(vaoIds[0])
        GLES30.glDrawElements(GLES30.GL_TRIANGLES, indices.size, GLES30.GL_UNSIGNED_SHORT, 0)
        GLES30.glBindVertexArray(0)
    }

    override fun release() {
        //禁止顶点数组的句柄
        GLES30.glDisableVertexAttribArray(0)
        GLES30.glDisableVertexAttribArray(1)
        GLES30.glBindVertexArray(0)
        GLES30.glBindBuffer(GLES30.GL_ARRAY_BUFFER, 0)
        GLES30.glBindBuffer(GLES30.GL_ELEMENT_ARRAY_BUFFER, 0)

        GLES30.glDeleteProgram(programId)
    }

}