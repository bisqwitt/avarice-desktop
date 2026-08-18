#ifdef GL_ES
precision mediump float;
precision mediump int;
#endif

varying vec4 v_color;
varying vec2 v_texCoords;

uniform float u_time;       // from LibGDX

const vec3 mixColor1 = vec3(0.2078, 0.39607, 0.30196);
const vec3 mixColor2 = vec3(0.2078, 0.39607, 0.30196);

#define NUM_OCTAVES 5

float random(in vec2 st)
{
    return fract(sin(dot(st.xy, vec2(12.9898, 78.233))) * 43758.5453123);
}

float hash(vec2 p) {
    p = fract(p * vec2(123.34, 456.21));
    p += dot(p, p + 45.32);
    return fract(p.x * p.y);
}

float noise(in vec2 st)
{
    vec2 i = floor(st);
    vec2 f = fract(st);

    float a = hash(i + vec2(0.0, 0.0));
    float b = hash(i + vec2(1.0, 0.0));
    float c = hash(i + vec2(0.0, 1.0));
    float d = hash(i + vec2(1.0, 1.0));

    vec2 u = f * f * (3.0 - 2.0 * f);
    return mix(a, b, u.x) + (c - a) * u.y * (1.0 - u.x) + (d - b) * u.x * u.y;
}

float fbm(in vec2 st)
{
    float v = 0.0;
    float a = 0.5;
    for (int i = 0; i < NUM_OCTAVES; i++)
    {
        v += a * noise(st);
        st = st * 2.0;
        a *= 0.5;
    }
    return v;
}

void main()
{
    // Base UV
    vec2 st = v_texCoords * 200.0;

    // ------------- PIXELATION -------------
    // u_pixelSize is in UV units; e.g. 1.0/64.0 → 64 blocks across
    float blocks = 384.0;              // tweak this
    vec2 stPix = floor(st * blocks) / blocks;
    st = stPix;
    // --------------------------------------

    vec3 color = mixColor1;

    vec2 q = vec2(0.0);
    q.x = fbm(st + vec2(0.0));
    q.y = fbm(st + vec2(1.0));

    vec2 r = vec2(0.0);
    r.x = fbm(st + (4.0 * q) + vec2(1.7, 9.2) + (0.15 * u_time));
    r.y = fbm(st + (4.0 * q) + vec2(8.3, 2.8) + (0.12 * u_time));

    color = mix(color, mixColor1, clamp(length(q), 0.0, 1.0));
    color = mix(color, mixColor2, clamp(length(r), 0.0, 1.0));

    float f = fbm(st + 4.0 * r);

    float coef = (f * f * f + (0.6 * f * f) + (0.5 * f));
    //coef = coef * 0.5 + 0.5;
    color *= coef;

    gl_FragColor = vec4(color, 1.0) * v_color;
}
