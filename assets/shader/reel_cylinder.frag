#ifdef GL_ES
precision mediump float;
#endif

varying vec2 v_texCoords;
uniform sampler2D u_texture;

// tweakables
uniform float u_curvature;      // 0.0 .. ~0.5
uniform float u_edgeDarken;     // 0.0 .. ~0.7
uniform float u_highlight;      // 0.0 .. ~0.4
uniform float u_highlightPos;   // 0..1 across X
uniform float u_highlightWidth; // ~0.05..0.25

void main() {
    vec2 uv = v_texCoords;

    // y in [-1..1]
    float ny = (uv.y - 0.5) * 2.0;

    // cylinder-ish warp: compress x more near top/bottom
    float bend = u_curvature * (ny * ny);
    float fromCenter = uv.x - 0.5;
    uv.x = 0.5 + fromCenter / (1.0 + bend);

    vec4 col = texture2D(u_texture, uv);

    // darken toward top/bottom to sell depth
    float edge = clamp(abs(ny), 0.0, 1.0);
    float shade = 1.0 - u_edgeDarken * (edge * edge);
    col.rgb *= shade;

    // fake specular highlight band
    float h = exp(-pow((uv.x - u_highlightPos) / u_highlightWidth, 2.0));
    col.rgb += u_highlight * h;

    gl_FragColor = col;
}
