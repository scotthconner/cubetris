uniform   mat4 uMVPMatrix;
uniform   mat4 uModelMatrix;   // the board's rotation matrix
uniform   mat4 uMVMatrix;      // the combined model/view matrix for object orientation
uniform   vec3 uLightPos;      // light position
uniform   vec3 uLightPos2;     // the second light position
uniform   vec3 uLightPos3;     // the third light position

attribute vec4 aPosition;      // vertex position
attribute vec3 aNormal;        // vertex normal
attribute vec4 aColor;         // vertex color
attribute vec3 aModelOffset;   // model position vertex offset
attribute vec4 aRotation;      // model rotation from its origin

varying   vec3 vEyeSpacePosition;
varying   vec4 vColor;         // this goes to the fragment shader
varying   vec3 vPosition;      // this goes to the fragment shader
varying   vec3 vNormal;        // this goes to the fragment shader

mat4 rotationMatrix(vec3 axis, float angle) {
    axis = normalize(axis);
    float s = sin(angle);
    float c = cos(angle);
    float oc = 1.0 - c;

    return mat4(oc * axis.x * axis.x + c,           oc * axis.x * axis.y - axis.z * s,  oc * axis.z * axis.x + axis.y * s,  0.0,
                oc * axis.x * axis.y + axis.z * s,  oc * axis.y * axis.y + c,           oc * axis.y * axis.z - axis.x * s,  0.0,
                oc * axis.z * axis.x - axis.y * s,  oc * axis.y * axis.z + axis.x * s,  oc * axis.z * axis.z + c,           0.0,
                0.0,                                0.0,                                0.0,                                1.0);
}

void main() {
    // construct the model matrix of the cube's vertex from the rotation and model offset vectors.
    mat4 modelMatrix = mat4(
        vec4(1.0f, 0.0f, 0.0f, aModelOffset.x),
        vec4(0.0f, 1.0f, 0.0f, aModelOffset.y),
        vec4(0.0f, 0.0f, 1.0f, aModelOffset.z),
        vec4(0.0f, 0.0f, 0.0f, 1.0f)
    );
    float cosa = cos(aRotation.w);
    float omcosa = 1.0f - cosa;
    float sina = sin(aRotation.w);

    mat4 rMatrix = rotationMatrix( vec3(aRotation.x, aRotation.y, aRotation.z), aRotation.w);

    // transform the vertex using the model position.
    vec4 finalPosition = (rMatrix * aPosition) + vec4(aModelOffset, 0.0f);

    // Transform the vertex into eye space.
    vEyeSpacePosition = vec3(uModelMatrix * finalPosition);
    vPosition = vec3(uMVMatrix * finalPosition);

    // Pass through the color.
    vColor = aColor;
    // Transform the normal's orientation into eye space.
    vNormal = vec3(uMVMatrix * vec4(aNormal, 0.0));
    // translate the final position into screen space
    gl_Position = uMVPMatrix * finalPosition;
}