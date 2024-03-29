package jp.co.miosys.aitec.utils;

/**
 * Created by DatNT on 8/30/2017.
 */
import android.opengl.GLES11Ext;
import android.opengl.GLES20;

import org.webrtc.GlShader;
import org.webrtc.GlTextureFrameBuffer;
import org.webrtc.GlUtil;
import org.webrtc.RendererCommon;
import org.webrtc.ThreadUtils;

import java.nio.ByteBuffer;
import java.nio.FloatBuffer;

/**
 * Class for converting OES textures to a YUV ByteBuffer. It should be constructed on a thread with
 * an active EGL context, and only be used from that thread.
 */
public class YuvConverter {
    // Vertex coordinates in Normalized Device Coordinates, i.e.
    // (-1, -1) is bottom-left and (1, 1) is top-right.
    private static final FloatBuffer DEVICE_RECTANGLE = GlUtil.createFloatBuffer(new float[] {
            -1.0f, -1.0f, // Bottom left.
            1.0f, -1.0f, // Bottom right.
            -1.0f, 1.0f, // Top left.
            1.0f, 1.0f, // Top right.
    });
    // Texture coordinates - (0, 0) is bottom-left and (1, 1) is top-right.
    private static final FloatBuffer TEXTURE_RECTANGLE = GlUtil.createFloatBuffer(new float[] {
            0.0f, 0.0f, // Bottom left.
            1.0f, 0.0f, // Bottom right.
            0.0f, 1.0f, // Top left.
            1.0f, 1.0f // Top right.
    });
    // clang-format off
    private static final String VERTEX_SHADER =
            "varying vec2 interp_tc;\n"
                    + "attribute vec4 in_pos;\n"
                    + "attribute vec4 in_tc;\n"
                    + "\n"
                    + "uniform mat4 texMatrix;\n"
                    + "\n"
                    + "void main() {\n"
                    + "    gl_Position = in_pos;\n"
                    + "    interp_tc = (texMatrix * in_tc).xy;\n"
                    + "}\n";
    private static final String FRAGMENT_SHADER =
            "#extension GL_OES_EGL_image_external : require\n"
                    + "precision mediump float;\n"
                    + "varying vec2 interp_tc;\n"
                    + "\n"
                    + "uniform samplerExternalOES oesTex;\n"
                    // Difference in texture coordinate corresponding to one
                    // sub-pixel in the x direction.
                    + "uniform vec2 xUnit;\n"
                    // Color conversion coefficients, including constant term
                    + "uniform vec4 coeffs;\n"
                    + "\n"
                    + "void main() {\n"
                    // Since the alpha read from the texture is always 1, this could
                    // be written as a mat4 x vec4 multiply. However, that seems to
                    // give a worse framerate, possibly because the additional
                    // multiplies by 1.0 consume resources. TODO(nisse): Could also
                    // try to do it as a vec3 x mat3x4, followed by an add in of a
                    // constant vector.
                    + "  gl_FragColor.r = coeffs.a + dot(coeffs.rgb,\n"
                    + "      texture2D(oesTex, interp_tc - 1.5 * xUnit).rgb);\n"
                    + "  gl_FragColor.g = coeffs.a + dot(coeffs.rgb,\n"
                    + "      texture2D(oesTex, interp_tc - 0.5 * xUnit).rgb);\n"
                    + "  gl_FragColor.b = coeffs.a + dot(coeffs.rgb,\n"
                    + "      texture2D(oesTex, interp_tc + 0.5 * xUnit).rgb);\n"
                    + "  gl_FragColor.a = coeffs.a + dot(coeffs.rgb,\n"
                    + "      texture2D(oesTex, interp_tc + 1.5 * xUnit).rgb);\n"
                    + "}\n";
    // clang-format on
    private final GlTextureFrameBuffer textureFrameBuffer;
    private final GlShader shader;
    private final int texMatrixLoc;
    private final int xUnitLoc;
    private final int coeffsLoc;
    private final ThreadUtils.ThreadChecker threadChecker = new ThreadUtils.ThreadChecker();
    private boolean released = false;
    /**
     * This class should be constructed on a thread that has an active EGL context.
     */
    public YuvConverter() {
        threadChecker.checkIsOnValidThread();
        textureFrameBuffer = new GlTextureFrameBuffer(GLES20.GL_RGBA);
        shader = new GlShader(VERTEX_SHADER, FRAGMENT_SHADER);
        shader.useProgram();
        texMatrixLoc = shader.getUniformLocation("texMatrix");
        xUnitLoc = shader.getUniformLocation("xUnit");
        coeffsLoc = shader.getUniformLocation("coeffs");
        GLES20.glUniform1i(shader.getUniformLocation("oesTex"), 0);
        GlUtil.checkNoGLES2Error("Initialize fragment shader uniform values.");
        // Initialize vertex shader attributes.
        shader.setVertexAttribArray("in_pos", 2, DEVICE_RECTANGLE);
        // If the width is not a multiple of 4 pixels, the texture
        // will be scaled up slightly and clipped at the right border.
        shader.setVertexAttribArray("in_tc", 2, TEXTURE_RECTANGLE);
    }
    public int convert(ByteBuffer buf, int width, int height, int stride, int srcTextureId,
                        float[] transformMatrix) {
        threadChecker.checkIsOnValidThread();
        if (released) {
            return -1;
        }
        // We draw into a buffer laid out like
        //
        //    +---------+
        //    |         |
        //    |  Y      |
        //    |         |
        //    |         |
        //    +----+----+
        //    | U  | V  |
        //    |    |    |
        //    +----+----+
        //
        // In memory, we use the same stride for all of Y, U and V. The
        // U data starts at offset |height| * |stride| from the Y data,
        // and the V data starts at at offset |stride/2| from the U
        // data, with rows of U and V data alternating.
        //
        // Now, it would have made sense to allocate a pixel buffer with
        // a single byte per pixel (EGL10.EGL_COLOR_BUFFER_TYPE,
        // EGL10.EGL_LUMINANCE_BUFFER,), but that seems to be
        // unsupported by devices. So do the following hack: Allocate an
        // RGBA buffer, of width |stride|/4. To render each of these
        // large pixels, sample the texture at 4 different x coordinates
        // and store the results in the four components.
        //
        // Since the V data needs to start on a boundary of such a
        // larger pixel, it is not sufficient that |stride| is even, it
        // has to be a multiple of 8 pixels.
        if (stride % 8 != 0) {
            //return -1;    //  [2017.10.05] VMio modify for accept even stride
        }
        if (stride < width) {
            return -1;
        }
        int y_width = (width + 3) / 4;
        int uv_width = (width + 7) / 8;
        int uv_height = (height + 1) / 2;
        int total_height = height + uv_height;
        int size = stride * total_height;
        if (buf.capacity() < size) {
            return -1;
        }
        // Produce a frame buffer starting at top-left corner, not
        // bottom-left.
        transformMatrix =
                RendererCommon.multiplyMatrices(transformMatrix, RendererCommon.verticalFlipMatrix());
        final int frameBufferWidth = stride / 4;
        final int frameBufferHeight = total_height;
        textureFrameBuffer.setSize(frameBufferWidth, frameBufferHeight);
        // Bind our framebuffer.
        GLES20.glBindFramebuffer(GLES20.GL_FRAMEBUFFER, textureFrameBuffer.getFrameBufferId());
        GlUtil.checkNoGLES2Error("glBindFramebuffer");
        GLES20.glActiveTexture(GLES20.GL_TEXTURE0);
        GLES20.glBindTexture(GLES11Ext.GL_TEXTURE_EXTERNAL_OES, srcTextureId);
        GLES20.glUniformMatrix4fv(texMatrixLoc, 1, false, transformMatrix, 0);
        // Draw Y
        GLES20.glViewport(0, 0, y_width, height);
        // Matrix * (1;0;0;0) / width. Note that opengl uses column major order.
        GLES20.glUniform2f(xUnitLoc, transformMatrix[0] / width, transformMatrix[1] / width);
        // Y'UV444 to RGB888, see
        // https://en.wikipedia.org/wiki/YUV#Y.27UV444_to_RGB888_conversion.
        // We use the ITU-R coefficients for U and V */
        GLES20.glUniform4f(coeffsLoc, 0.299f, 0.587f, 0.114f, 0.0f);
        GLES20.glDrawArrays(GLES20.GL_TRIANGLE_STRIP, 0, 4);
        // Draw U
        GLES20.glViewport(0, height, uv_width, uv_height);
        // Matrix * (1;0;0;0) / (width / 2). Note that opengl uses column major order.
        GLES20.glUniform2f(
                xUnitLoc, 2.0f * transformMatrix[0] / width, 2.0f * transformMatrix[1] / width);
        GLES20.glUniform4f(coeffsLoc, -0.169f, -0.331f, 0.499f, 0.5f);
        GLES20.glDrawArrays(GLES20.GL_TRIANGLE_STRIP, 0, 4);
        // Draw V
        // [2017.10.05] VMio modify for keep memory inbound
        // GLES20.glViewport(stride / 8, height, uv_width, uv_height);
        GLES20.glViewport((int)Math.floor(stride / 8), height, uv_width, uv_height);
        GLES20.glUniform4f(coeffsLoc, 0.499f, -0.418f, -0.0813f, 0.5f);
        GLES20.glDrawArrays(GLES20.GL_TRIANGLE_STRIP, 0, 4);
        GLES20.glReadPixels(
                0, 0, frameBufferWidth, frameBufferHeight, GLES20.GL_RGBA, GLES20.GL_UNSIGNED_BYTE, buf);
        GlUtil.checkNoGLES2Error("YuvConverter.convert");
        // Restore normal framebuffer.
        GLES20.glBindFramebuffer(GLES20.GL_FRAMEBUFFER, 0);
        GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, 0);
        // Unbind texture. Reportedly needed on some devices to get
        // the texture updated from the camera.
        GLES20.glBindTexture(GLES11Ext.GL_TEXTURE_EXTERNAL_OES, 0);
        return 0;
    }
    public void release() {
        threadChecker.checkIsOnValidThread();
        released = true;
        shader.release();
        textureFrameBuffer.release();
    }
}