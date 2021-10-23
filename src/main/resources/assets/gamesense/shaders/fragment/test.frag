#ifdef GL_ES
precision mediump float;
#endif

uniform float time;
uniform vec2 resolution;

void main() {

    vec4 centerCol = texture2D(texture, gl_TexCoord[0].xy);

    vec2 uv2 = 2. * gl_FragCoord.xy / resolution.xy - 1.;
    vec2 uvs = uv2 * resolution.xy / max(resolution.x, resolution.y);

    vec2 point1 = vec2(0.0, 0.0);
    vec2 point2 = vec2(resolution.x, resolution.y);
    vec4 color1 = vec4(0,0,1,1);
    vec4 color2 = vec4(0,1,1,1);

    point2.x += (sin(time / 2.4) - 1.) * 100.0;
    point2.y += (cos(time / 1.) - 1.) * 100.0;


    point1.x += (sin(time) + 1.) * 100.0;
    point1.y += (cos(time) + 1.) * 100.0;

    float distance1 = sqrt( pow(point1.x - gl_FragCoord.x, 2.0) + pow(point1.y - gl_FragCoord.y, 2.0));
    float distance2 = sqrt( pow(point2.x - gl_FragCoord.x, 2.0) + pow(point2.y - gl_FragCoord.y, 2.0));

    distance1 = sin(distance1 / 20.0) + 1.0;
    distance2 = sin(distance2 / 20.0) + 1.0;

    float total = distance1 + distance2;

    vec4 ret = color1 * (distance2 / total);
    ret += color2 * (distance1 / total);

    gl_FragColor = ret;
}