uniform  mat4  uMVPMatrix;
uniform  float uTexturePointSize;
//uniform  vec2  uTextCoord;

attribute float aPointSize;
attribute vec4  aPosition;
attribute vec4  aColor;
attribute float aTextureRotation;
attribute vec2  aTextCoord;

varying vec4  vColor;
varying vec2  vTextCoord;
varying float vTextureSize;
varying mat2  vRotationMatrix;

mat2 rotationMatrix(float rotationAngle) {
    float cosa = cos(rotationAngle);
    float sina = sin(rotationAngle);

    return mat2(
        vec2(cosa, -sina),
        vec2(sina, cosa)
    );
}

void main() {
    // pass in the varying colors, texture coordinates, and point sizes
    vColor = aColor;
    vTextCoord = aTextCoord;

    // assumes square texture
    vTextureSize = uTexturePointSize;

    // pass through the texture rotation
    vRotationMatrix = rotationMatrix(aTextureRotation);

    gl_PointSize = aPointSize;
    gl_Position =  uMVPMatrix * aPosition;
}