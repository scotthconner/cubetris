package com.scotthconner.cubetrisrebooted.lib.core;

import android.content.Context;
import android.opengl.GLES20;
import android.util.Log;

import com.scotthconner.cubetrisrebooted.R;
import com.scotthconner.cubetrisrebooted.lib.render.shader.ShaderProgramLibrary;

/**
 * Created by scottc on 2/21/16.
 */
public class GLHelper {
    public static void checkGLError(String logStatement) {
        int error = GLES20.glGetError();

        if (error != GLES20.GL_NO_ERROR) {
            String s = logStatement + " (" + error + ")";
            Log.d("GLHelper::checkGLError", s);
            throw new RuntimeException("GL CHECK ERROR FAIL: " + s);
        }
    }

    public static void loadDefaultShaders(Context cxt) {
        int vertex = ShaderProgramLibrary.getInstance().createShader(cxt, "vertex",
                GLES20.GL_VERTEX_SHADER, R.raw.flat_color_vertex);
        int fragment = ShaderProgramLibrary.getInstance().createShader(cxt, "fragment",
                GLES20.GL_FRAGMENT_SHADER, R.raw.flat_color_fragment);
        ShaderProgramLibrary.getInstance().createProgram("standard", vertex, fragment);

        // simple color shader
        int stdVertex = ShaderProgramLibrary.getInstance().createShader(cxt, "vertex:color",
                GLES20.GL_VERTEX_SHADER, R.raw.vertex_color_vertex);
        int stdFragment = ShaderProgramLibrary.getInstance().createShader(cxt, "fragment:color",
                GLES20.GL_FRAGMENT_SHADER, R.raw.vertex_color_fragment);
        ShaderProgramLibrary.getInstance().createProgram("color", stdVertex, stdFragment);

        // vertex color per pixel light shader
        int colorVertexLightShader = ShaderProgramLibrary.getInstance().createShader(cxt, "vertex:color",
                GLES20.GL_VERTEX_SHADER, R.raw.cube_vertex);
        int colorFragmentLightShader = ShaderProgramLibrary.getInstance().createShader(cxt, "fragment:color",
                GLES20.GL_FRAGMENT_SHADER, R.raw.cube_fragment);
        ShaderProgramLibrary.getInstance().createProgram("color-light", colorVertexLightShader,
                colorFragmentLightShader);

        // texture shader
        int textureVertexShader = ShaderProgramLibrary.getInstance().createShader(cxt, "vertex:texture",
                GLES20.GL_VERTEX_SHADER, R.raw.texture_vertex);
        int textureFragmentShader = ShaderProgramLibrary.getInstance().createShader(cxt, "fragment:texture",
                GLES20.GL_FRAGMENT_SHADER, R.raw.texture_fragment);
        ShaderProgramLibrary.getInstance().createProgram("texture", textureVertexShader,
                textureFragmentShader);

        // point sprite shaders
        int pointSpriteVertexShader = ShaderProgramLibrary.getInstance().createShader(cxt, "vertex:point-sprite",
                GLES20.GL_VERTEX_SHADER, R.raw.point_sprite_vertex);
        int pointSpriteFragmentShader = ShaderProgramLibrary.getInstance().createShader(cxt, "fragment:point-sprite",
                GLES20.GL_FRAGMENT_SHADER, R.raw.point_sprite_fragment);
        ShaderProgramLibrary.getInstance().createProgram("point-sprite", pointSpriteVertexShader,
                pointSpriteFragmentShader);
    }
}
