uniform   mat4 uMVPMatrix;
attribute vec4 aPosition;
attribute vec2 aTextCoord;
varying   vec2 vTextCoord;
void main() {
    vTextCoord = aTextCoord;
    gl_Position = uMVPMatrix * aPosition;
}