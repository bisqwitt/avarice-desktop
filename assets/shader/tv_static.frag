#ifdef GL_ES
precision mediump float;
#endif

varying vec2 v_texCoords;

uniform float u_time;
uniform vec2 u_resolution;

float hash(vec2 p) {
p = fract(p * vec2(443.897, 441.423));
p += dot(p, p + 19.19);
return fract(p.x * p.y);
}

float noise(vec2 p) {
vec2 i = floor(p);
vec2 f = fract(p);
f = f * f * (3.0 - 2.0 * f);
return mix(
        mix(hash(i), hash(i + vec2(1,0)), f.x),
        mix(hash(i + vec2(0,1)), hash(i + vec2(1,1)), f.x),
        f.y
);
}

void main() {
    vec2 uv = v_texCoords;

    // Felt fiber texture — two octaves of high-freq noise
    float felt = noise(uv * 40.0) * 0.5
                 + noise(uv * 80.0) * 0.3
                 + noise(uv * 160.0) * 0.2;

    // Colors
    vec3 green = vec3(0.05, 0.22, 0.08);
    vec3 felt3  = mix(green, vec3(0.08, 0.30, 0.12), felt);
    vec3 gold   = vec3(1.00, 0.85, 0.25);

    vec3 col = felt3;

    // Subtle scanline so it still reads as a screen
    float scan = sin((uv.y + u_time * 0.04) * 60.0) * 0.015;
    col += scan;

    // Vignette
    float v = uv.x * (1.0 - uv.x) * uv.y * (1.0 - uv.y);
    col *= clamp(pow(v * 16.0, 0.3), 0.0, 1.0);

    gl_FragColor = vec4(col, 1.0);
}
