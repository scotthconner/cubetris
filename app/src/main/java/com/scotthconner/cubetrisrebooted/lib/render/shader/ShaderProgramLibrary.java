package com.scotthconner.cubetrisrebooted.lib.render.shader;

import android.content.Context;
import android.opengl.GLES20;
import android.util.Log;

import com.scotthconner.cubetrisrebooted.lib.core.AndroidUtils;

import java.util.HashMap;

/**
 * Created by scottc on 12/26/15.
 */
public class ShaderProgramLibrary {
    private static ShaderProgramLibrary ourInstance = null;

    // holds the named programs
    private HashMap<String, Integer> shaders;
    private HashMap<String, Integer> programs;

    /**
     * createShader
     *
     * Creates a named program, compiles the shaders, and caches the whole thing
     *
     * @param cxt the application's activity context
     * @param name the name you want to call this shader by
     * @param resourceId the file that contains the shader code
     * @return integer handle for shader code.
     */
    public int createShader(Context cxt, String name, int type, int resourceId) {
        String shaderCode = AndroidUtils.readRawTextFile(cxt, resourceId);
        int shaderId = loadShader(type, shaderCode);
        shaders.put(name, shaderId);
        Log.d("ShaderProgramLibrary", "loaded shader: " + name + " id: " + shaderId);

        return shaderId;
    }

    public int getShader(String name) {
        return shaders.get(name);
    }

    public int getProgram(String name) {
        return programs.get(name).intValue();
    }

    /**
     * createProgram
     *
     * Takes a name, a vertex and a fragment shader ID, previously loaded with
     * createShader, and will generate a program that can be used to render geometry
     *
     * @param name the name of the rendering program
     * @param vertexShader the ID of the vertex shader, previously loaded from createShader
     * @param fragmentShader the ID of the fragment  shader, previous loaded from createShader
     * @return the ID of the program, used for linking.
     */
    public int createProgram(String name, int vertexShader, int fragmentShader) {
        Log.d("ShaderProgramLibrary", "Starting program load: " + name);
        int program = GLES20.glCreateProgram();

        Log.d("ShaderProgramLibrary", "Program is valid: " + GLES20.glIsProgram(program));
        Log.d("ShaderProgramLibrary", "VertexShader is valid: " + GLES20.glIsShader(vertexShader));
        GLES20.glAttachShader(program, vertexShader);
        Log.d("ShaderProgramLibrary", "FragmentShader is valid: " + GLES20.glIsShader(fragmentShader));
        GLES20.glAttachShader(program, fragmentShader);
        GLES20.glLinkProgram(program);

        // Get the link status.
        final int[] linkStatus = new int[1];
        GLES20.glGetProgramiv(program, GLES20.GL_LINK_STATUS, linkStatus, 0);

        // If the link failed, delete the program.
        if (linkStatus[0] == 0)
        {
            Log.e("ShaderProgramLibrary", "Error compiling program: " + GLES20.glGetProgramInfoLog(program));
            throw new RuntimeException("Couldn't link program");
        }

        programs.put(name, program);
        Log.d("ShaderProgramLibrary", "loaded program: " + name + " id: " + program + " is valid: " +
                GLES20.glIsProgram(program));
        return program;
    }

    private int loadShader(int type, String shaderCode){
        // create a vertex shader type (GLES20.GL_VERTEX_SHADER)
        // or a fragment shader type (GLES20.GL_FRAGMENT_SHADER)
        int shader = GLES20.glCreateShader(type);

        // add the source code to the shader and compile it
        GLES20.glShaderSource(shader, shaderCode);
        GLES20.glCompileShader(shader);

        // Get the compilation status.
        final int[] compileStatus = new int[1];
        GLES20.glGetShaderiv(shader, GLES20.GL_COMPILE_STATUS, compileStatus, 0);
        if (compileStatus[0] == 0)  {
            Log.e("ShaderProgramLibrary", "Problem with Shader Code: " + GLES20.glGetShaderInfoLog(shader));
            Log.e("ShaderProgramLibrary", "Error compiling shader: " + GLES20.glGetShaderInfoLog(shader));
            throw new RuntimeException("couldn't compile shader");
        }

        return shader;
    }

    public static ShaderProgramLibrary getInstance() {
        if (null == ourInstance) {
            ourInstance = new ShaderProgramLibrary();
        }
        return ourInstance;
    }

    private ShaderProgramLibrary() {
        shaders = new HashMap<String, Integer>();
        programs = new HashMap<String, Integer>();
        Log.d("ShaderProgramLibrary", "initialized");
    }
}
