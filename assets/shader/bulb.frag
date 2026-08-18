#ifdef GL_ES
precision mediump float;
#endif

uniform vec2  u_resolution;     // quad size in px (bulbSize, bulbSize)
uniform float u_on;             // 1.0 on, 0.0 off
uniform float u_time;           // seconds
uniform float u_glowOnly;       // 0.0 = body, 1.0 = glow pass
uniform vec3  u_colorOn;
uniform vec3  u_colorOff;

varying vec2 v_texCoords;

float hash21(vec2 p){
p = fract(p*vec2(123.34, 456.21));
p += dot(p, p+45.32);
return fract(p.x*p.y);
}

void main() {
    // Centered UV in [-0.5, +0.5]
    vec2 uv = v_texCoords - 0.5;

    // Slightly squash vertical to mimic perspective/CRT-ish bulbs (tweak 1.0..1.2)
    uv.y *= 1.08;

    float d = length(uv);

    // Bulb geometry
    float rCore = 0.20;   // hot core
    float rGlass = 0.45;  // glass edge
    float rHalo = 0.75;   // halo extent (glow pass)

    // Body masks
    float glass = smoothstep(rGlass, rGlass - 0.02, d);          // inside glass
    float rim   = smoothstep(rGlass, rGlass - 0.06, d) - glass;  // thin rim band
    float core  = smoothstep(rCore, rCore - 0.06, d);            // hot core

    // Halo (for glow pass)
    float halo  = smoothstep(rHalo, 0.10, d); // wide soft falloff

    // Glass specular highlight (small bright spot offset)
    vec2 hpos = uv - vec2(-0.14, 0.14);
    float h = exp(-dot(hpos,hpos) * 60.0);  // gaussian highlight

    // Micro flicker/noise (very subtle)
    float n = hash21(v_texCoords * 128.0 + u_time);
    float flicker = 1.0 + (n - 0.5) * 0.08;

    vec3 baseOn  = u_colorOn;
    vec3 baseOff = u_colorOff;

    // Blend on/off (off still shows a dim glass body)
    float on = clamp(u_on, 0.0, 1.0);
    vec3 base = mix(baseOff, baseOn, on);

    // BODY pass: glass + rim + highlight + hot core
    vec3 bodyColor =
            base * (0.35 * glass) +
            base * (0.20 * rim) +
            baseOn * (1.25 * core) * on * flicker +
            vec3(1.0) * (0.35 * h) * (0.3 + 0.7 * on);

    float bodyAlpha = clamp(glass + 0.4*rim + 0.25*h, 0.0, 1.0);

    // GLOW pass: halo only (additive)
    vec3 glowColor =
            baseOn * (1.35 * halo) * on * flicker +
            baseOn * (0.55 * core) * on;

    float glowAlpha = clamp(halo * on, 0.0, 1.0);

    // Switch between passes
    vec3  outColor = mix(bodyColor, glowColor, u_glowOnly);
    float outAlpha = mix(bodyAlpha, glowAlpha, u_glowOnly);

    gl_FragColor = vec4(outColor, outAlpha);
}
