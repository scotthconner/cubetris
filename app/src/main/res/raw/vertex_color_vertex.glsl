uniform   mat4 uMVPMatrix;
uniform   mat4 uModelMatrix;
attribute vec3 aPosition;
attribute vec4 aColor;
varying   vec4 vColor; // this goes to the shader
varying   vec4 vPosition; // shader
void main() {
    vColor = aColor;
    vPosition = uModelMatrix * vec4(aPosition, 1.0f);
    gl_Position = uMVPMatrix * vec4(aPosition, 1.0f);
}