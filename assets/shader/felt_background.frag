#ifdef GL_ES
precision highp float;
#endif

uniform vec2 u_resolution;
uniform float u_time;

float hash(vec2 p) {
    p = fract(p * vec2(234.34, 435.345));
    p += dot(p, p + 34.23);
    return fract(p.x * p.y);
}

float noise(vec2 p) {
    vec2 i = floor(p);
    vec2 f = fract(p);
    vec2 u = f * f * (3.0 - 2.0 * f);
    return mix(
        mix(hash(i), hash(i + vec2(1,0)), u.x),
        mix(hash(i + vec2(0,1)), hash(i + vec2(1,1)), u.x),
        u.y
    );
}

float fbm(vec2 p) {
    float v = 0.0, a = 0.5;
    mat2 rot = mat2(cos(0.5), sin(0.5), -sin(0.5), cos(0.5));
    for (int i = 0; i < 5; i++) {
        v += a * noise(p);
        p = rot * p * 2.0 + vec2(1.7, 9.2);
        a *= 0.5;
    }
    return v;
}

void main() {
    // Pixelation — adjust PIXEL_SIZE to taste
    float PIXEL_SIZE = 4.0;
    vec2 uv = floor(gl_FragCoord.xy / PIXEL_SIZE) * PIXEL_SIZE / u_resolution;

    float t = u_time;
    vec2 q = vec2(fbm(uv * 2.0 + vec2(0.0, t * 0.12)),
                  fbm(uv * 2.0 + vec2(5.2, t * 0.12 + 1.3)));
    vec2 r = vec2(fbm(uv * 2.0 + 4.0 * q + vec2(1.7, t * 0.08)),
                  fbm(uv * 2.0 + 4.0 * q + vec2(9.2, t * 0.08 + 2.8)));
    float f = fbm(uv * 3.0 + 4.0 * r + vec2(t * 0.05));
    f = mix(f, fbm((uv + vec2(t * 0.06, -t * 0.04)) * 1.5), 0.3);
    f += fbm(uv * 8.0 + vec2(t * 0.15, t * 0.07)) * 0.06;

    // Vignette
    vec2 vig = uv * 2.0 - 1.0;
    float vignette = clamp(1.0 - dot(vig * 0.6, vig * 0.6), 0.0, 1.0);

    // Green felt palette — swap these for other colors
    vec3 dark  = vec3(0.04, 0.18, 0.07);  // was 0.14
    vec3 mid   = vec3(0.08, 0.36, 0.14);  // was 0.28
    vec3 light = vec3(0.14, 0.52, 0.22);  // was 0.42
    vec3 accent= vec3(0.22, 0.68, 0.18);  // was 0.58

    vec3 col = mix(dark, mid, f);
    col = mix(col, light, f * f * 0.6);
    col += accent * f * f * f * 0.3;
    col *= vignette * 0.6 + 0.4;
    col *= 0.85 + uv.y * 0.3;

    gl_FragColor = vec4(col, 1.0);
}
