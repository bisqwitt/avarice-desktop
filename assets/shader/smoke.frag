#ifdef GL_ES
precision mediump float;
#endif

uniform float u_time;
uniform vec2 u_mouse;
uniform vec2 u_resolution;
uniform float u_pixelSize;
uniform vec3 u_tint;

void main() {
    vec2 snapped = floor(gl_FragCoord.xy / u_pixelSize) * u_pixelSize;

    vec2 p = (snapped - u_resolution.xy)
        / min(u_resolution.x, u_resolution.y);

    for(int i = 1; i < 5; i++) {
        p += sin(
            p.yx * vec2(1.6, 1.1) * float(i + 11)
            + u_time * float(i) * vec2(3.4, 0.5) / 10.0
        ) * 0.1;
    }

    float c = (abs(sin(p.y) + sin(p.x))) * 0.5;
    gl_FragColor = vec4(c * u_tint, 1.0);
}
