#ifdef GL_ES
precision mediump float;
#endif

uniform sampler2D u_texture0;

uniform float u_time;
uniform vec2 u_resolution;

varying vec2 v_texCoords;

float random(vec2 p) {
    return fract(
        sin(dot(p, vec2(12.9898, 78.233))) * 43758.5453
    );
}

void main() {

    vec2 uv = v_texCoords;

    // ------------------------------------------------
    // VERTICAL JITTER
    // ------------------------------------------------

    float jitterTrigger =
    step(0.985, random(vec2(floor(u_time * 12.0), 3.0)));

    uv.y += jitterTrigger * 0.0025;


    // ------------------------------------------------
    // CRT CURVATURE
    // ------------------------------------------------

    vec2 centered = uv * 2.0 - 1.0;

    float curvature = 0.035;

    centered.x *=
    1.0 + centered.y * centered.y * curvature;

    centered.y *=
    1.0 + centered.x * centered.x * curvature;

    uv = centered * 0.5 + 0.5;


    // outside curved CRT
    if (
    uv.x < 0.0 ||
    uv.x > 1.0 ||
    uv.y < 0.0 ||
    uv.y > 1.0
    ) {
        gl_FragColor = vec4(0.0, 0.0, 0.0, 1.0);
        return;
    }


    // ------------------------------------------------
    // OCCASIONAL HORIZONTAL INTERFERENCE
    // ------------------------------------------------

    float glitchTrigger =
    step(0.97, random(vec2(floor(u_time * 4.0), 8.0)));

    float glitchY =
    random(vec2(floor(u_time * 4.0), 17.0));

    float glitchBand =
    step(abs(uv.y - glitchY), 0.015);

    float glitch =
    glitchTrigger * glitchBand;

    uv.x += glitch * 0.012;


    // ------------------------------------------------
    // CHROMATIC ABERRATION
    // ------------------------------------------------

    float distanceFromCenter =
    length(uv - vec2(0.5));

    float aberration =
    0.0015 +
    distanceFromCenter * 0.002;

    float r = texture2D(
        u_texture0,
        uv + vec2(aberration, 0.0)
    ).r;

    float g = texture2D(
        u_texture0,
        uv
    ).g;

    float b = texture2D(
        u_texture0,
        uv - vec2(aberration, 0.0)
    ).b;

    vec3 color = vec3(r, g, b);


    // ------------------------------------------------
    // SCANLINES
    // ------------------------------------------------

    float scanline =
    sin(uv.y * u_resolution.y * 3.14159265);

    scanline =
    scanline * 0.5 + 0.5;

    color *=
    0.91 + scanline * 0.09;


    // ------------------------------------------------
    // VERY SMALL ANALOG NOISE
    // ------------------------------------------------

    float noise =
    random(
        uv * u_resolution +
        vec2(u_time * 100.0)
    );

    color +=
    (noise - 0.5) * 0.018;


    // ------------------------------------------------
    // CRT COLOR CHARACTER
    // ------------------------------------------------

    // slightly stronger saturation
    float luminance =
    dot(color, vec3(0.299, 0.587, 0.114));

    color =
    mix(
        vec3(luminance),
        color,
        1.08
    );

    // extremely slight warm/green CRT tint
    color.r *= 1.02;
    color.g *= 1.01;
    color.b *= 0.97;


    // ------------------------------------------------
    // VIGNETTE
    // ------------------------------------------------

    vec2 vignetteUv =
    uv * (1.0 - uv.yx);

    float vignette =
    vignetteUv.x *
    vignetteUv.y *
    18.0;

    vignette =
    pow(
        clamp(vignette, 0.0, 1.0),
        0.22
    );

    color *= vignette;


    // ------------------------------------------------
    // SMALL BRIGHTNESS / PHOSPHOR BOOST
    // ------------------------------------------------

    color *= 1.04;


    gl_FragColor =
    vec4(color, 1.0);
}
