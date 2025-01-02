precision mediump float;
uniform sampler2D uTexture;

varying float vTextureSize;
varying vec2  vTextCoord;
varying vec4  vColor;
varying mat2  vRotationMatrix;

void main()
{
   // calculate the rotated gl_PointCoord
   vec2 rp = (vRotationMatrix * (gl_PointCoord - 0.5f)) + 0.5f;

   // now that we have the rotated gl_PointCoord, calculate the texture coordinate
   vec2 realTextureCoord = vTextCoord + (rp * (32.0f / vTextureSize));

   // set the fragment color
   gl_FragColor = texture2D(uTexture, realTextureCoord) * vColor;
}