precision mediump float;
varying vec4 vColor;
varying vec4 vPosition;

float zPlane = 8.00f; // ROOM_RADIUS * 0.75f - some stuff;

void main() {
    // throw anything away that is essentially the front facing wall
    if (vPosition.z > zPlane) {
        discard;
    }

    gl_FragColor = vColor;
}