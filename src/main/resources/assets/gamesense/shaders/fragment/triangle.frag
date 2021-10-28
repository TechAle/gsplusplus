#ifdef GL_ES
precision mediump float;
#endif

uniform float time;
uniform vec2 resolution;
uniform sampler2D texture;


vec2 hash(vec2 p){
    vec2 q = vec2(dot(p,vec2(143.18,115.1)),
    dot(p,vec2(153.23,156.52)));

    return fract(sin(q)*134.165)*sin(time*0.5+q)+.5;
}



vec3 voronoi(vec2 uv){
    vec2 id = floor(uv);
    vec2 f = fract(uv);

    vec2 a = vec2(0);
    for(int i=-2;i<=2;i++){
        for(int j=-2;j<=2;j++){
            vec2 g = vec2(i,j);
            vec2 o = hash(id+g);

            vec2 d = g - f + o;
            float w = pow(smoothstep(1.2,0.,length(d)),40.);
            a+=vec2(o.x*w,w);
        }
    }
    return (a.x/a.y)*vec3((f+id)/9.*abs(sin(time*0.6))+.3,.8);
}

void main( void ) {
    vec4 centerCol = texture2D(texture, gl_TexCoord[0].xy);
if(centerCol.a == 0.0) {
    gl_FragColor = vec4(centerCol.rgb, 0);
} else {
    vec2 uv = ( gl_FragCoord.xy/ resolution.xy );
    uv.x*=resolution.x/resolution.y;
    vec3 col = vec3(1.0);

    //float c = voronoi(uv*25.);
    vec3 cc = voronoi(uv*18.);
    col = vec3(cc);
    gl_FragColor = vec4(col, centerCol.a );}

}