precision mediump float;
uniform vec3  uLightPos;         // light position
uniform vec3  uLightPos2;        // second light pos
uniform vec3  uLightPos3;        // third light pos
uniform float uAmbientFactor;    // clamped ambient light factor

uniform float uLightAttenuation;  // attenuation for distance
uniform float uLightAttenuation2; // attention for distance second light
uniform float uLightAttenuation3; // attention for distance second light

uniform float uLightDimmer;
uniform float uLightDimmer2;
uniform float uLightDimmer3;

varying vec3 vEyeSpacePosition;
varying vec3 vPosition;          // interpolated position for this fragment
varying vec4 vColor;
varying vec3 vNormal;
void main() {
    // LIGHT 1
    // calculate distance between light and vertex for attenuation
    float distance = length(uLightPos - vPosition);
    // calculate lighting direction from light to vertex
    vec3 lightVector = normalize(uLightPos - vPosition);
    // calculate dot product between light angle and vertex normal to get cosine, and clamp
    float diffuse = max(dot(vNormal, lightVector), uAmbientFactor);
    // attenuate the light based on distance (inverse distance)
    diffuse = uLightDimmer * diffuse * (1.0 / (1.0 + (uLightAttenuation * distance * distance)));

    // LIGHT 2
    // calculate distance between light and vertex for attenuation
    float distance2 = length(uLightPos2 - vPosition);
    // calculate lighting direction from light to vertex
    vec3 lightVector2 = normalize(uLightPos2 - vPosition);
    // calculate dot product between light angle and vertex normal to get cosine, and clamp
    float diffuse2 = max(dot(vNormal, lightVector2), uAmbientFactor);
    // attenuate the light based on distance (inverse distance)
    diffuse2 = uLightDimmer2 * diffuse2 * (1.0 / (1.0 + (uLightAttenuation2 * distance2 * distance2)));

    // LIGHT 3
    // calculate distance between light and vertex for attenuation
    float distance3 = length(uLightPos3 - vPosition);
    // calculate lighting direction from light to vertex
    vec3 lightVector3 = normalize(uLightPos3 - vPosition);
    // calculate dot product between light angle and vertex normal to get cosine, and clamp
    float diffuse3 = max(dot(vNormal, lightVector3), uAmbientFactor);
    // attenuate the light based on distance (inverse distance)
    diffuse3 = uLightDimmer3 * diffuse3 * (1.0 / (1.0 + (uLightAttenuation3 * distance3 * distance3)));

    gl_FragColor = vColor * min(1.0f, diffuse + diffuse2 + diffuse3);

    // the color is equal to the z depth of the fragment
    if ( vEyeSpacePosition.z <= 2.501f || vEyeSpacePosition.x < -9.0f || vEyeSpacePosition.x > 9.0f || vEyeSpacePosition.y <= -0.51f) {
        gl_FragColor *= 0.60f;
    }

}