#ifdef GL_ES
precision highp float;
#endif


uniform float time;
uniform vec2 resolution;
uniform sampler2D texture;

#define WAVELENGTH 22.0
#define C 555.0
#define R_CENTER vec2(0, 0)
#define G_CENTER vec2(0, resolution.y/04.0)
#define B_CENTER vec2(0, resolution.y)

float wave(vec2 c, vec2 pos)
{
    float d = distance(c, pos);
    return 1000000.0 * sin((d - time * C) / WAVELENGTH);
}

void main( void )
{
    vec4 centerCol = texture2D(texture, gl_TexCoord[0].xy);
    vec2 pos = gl_FragCoord.xy;
    gl_FragColor = vec4(
    wave(R_CENTER, pos),
    wave(G_CENTER, pos),
    wave(B_CENTER, pos),
    centerCol.a);
}