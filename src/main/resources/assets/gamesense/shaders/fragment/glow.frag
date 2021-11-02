#version 120

uniform sampler2D texture;
uniform vec2 texelSize;
uniform vec2 resolution;

uniform vec3 color;

uniform float radius;
uniform float divider;
uniform float maxSample;
uniform float time;

uniform float rainbowSpeed;

float random (in vec2 uv) {
    return fract(sin(dot(uv.xy,
    vec2(12.9898,78.233)))*
    43758.5453123);
}


float noise (in vec2 uv) {
    vec2 i = floor(uv);
    vec2 f = fract(uv);

    float a = random(i);
    float b = random(i + vec2(1.0, 0.0));
    float c = random(i + vec2(0.0, 1.0));
    float d = random(i + vec2(1.0, 1.0));

    vec2 u = f * f * (3.0 - 2.0 * f);

    return mix(a, b, u.x) +
    (c - a)* u.y * (1.0 - u.x) +
    (d - b) * u.x * u.y;
}


float fbm (in vec2 uv) {
    float value = 0.0;
    float amplitude = .5;
    float frequency = 0.;

    for (int i = 0; i < 6; i++) {
        value += amplitude * noise(uv);
        uv *= 2.;
        amplitude *= .5;
    }
    return value;
}


void main() {
    vec4 centerCol = texture2D(texture, gl_TexCoord[0].xy);

     if(centerCol.a != 0) {
         gl_FragColor = vec4(centerCol.rgb, 0);
     } else {

         float alpha = 0;
         vec2 uv = gl_FragCoord.xy/resolution.xy;

         uv.x *= resolution.x/resolution.y;

         vec3 col = 0.5 * .5 + cos(time + 6. * fbm(uv * atan(3.0))+ vec3(0, 23, 21));

         col += fbm(uv * atan(3.0));


         for (float x = -radius; x < radius; x++) {
             for (float y = -radius; y < radius; y++) {

                 vec4 currentColor = texture2D(texture, gl_TexCoord[0].xy + vec2(texelSize.x * x, texelSize.y * y));

                 if (currentColor.a != 0) {
                     alpha += divider > 0 ? max(0, (maxSample - distance(vec2(x, y), vec2(0))) / divider) : 1;
                     if (rainbowSpeed > -1.0) {
                         gl_FragColor = vec4(col, 1.0);
                     } else {
                         gl_FragColor = vec4(color.xyz, 1.0);
                     }
                 }
             }
         }
     }
}