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

    // CRT scanlines — more visible than the felt
    float scan = sin(uv.y * u_resolution.y * 1.5) * 0.06;

    // Subtle pixel grain
    float grain = hash(uv * u_resolution + floor(u_time * 30.0)) * 0.07;

    // Slow background pulse — screen feels "alive"
    float pulse = noise(vec2(uv.x * 2.0, u_time * 0.3)) * 0.04;

    // Dark teal/black screen color
    vec3 screenColor = vec3(0.04, 0.09, 0.07);
    screenColor += grain + scan + pulse;

    // Inner vignette — bright center, dark edges like a CRT
//    float v = uv.x * (1.0 - uv.x) * uv.y * (1.0 - uv.y);
//    float vignette = clamp(pow(v * 16.0, 0.5), 0.0, 1.0);
//    screenColor *= mix(0.5, 1.1, vignette);

    // Screen edge glow — slight bright rim
//    float rim = 1.0 - vignette;
//    screenColor += rim * vec3(0.02, 0.06, 0.04) * 0.4;

    gl_FragColor = vec4(screenColor, 1.0);
}
