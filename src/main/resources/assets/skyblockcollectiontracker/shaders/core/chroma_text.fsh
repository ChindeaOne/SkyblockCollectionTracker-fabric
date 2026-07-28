#version 150

in vec4 vertexColor;
in vec2 texCoord0;

layout(std140) uniform SctChromaUniforms {
    float timeOffset;
    int mode; // 0 = chroma, 1 = prefix gradient
    vec4 startColor;
    vec4 endColor;
};

uniform sampler2D Sampler0;

out vec4 fragColor;

float rgb2b(vec3 rgb) {
    return max(max(rgb.r, rgb.g), rgb.b);
}

vec3 hsb2rgb_smooth(vec3 c) {
    vec3 rgb = clamp(
        abs(mod(c.x * 6.0 + vec3(0.0, 4.0, 2.0), 6.0) - 3.0) - 1.0,
        0.0,
        1.0
    );

    rgb = rgb * rgb * (3.0 - 2.0 * rgb);
    return c.z * mix(vec3(1.0), rgb, c.y);
}

void main() {
    vec4 originalColor = texture(Sampler0, texCoord0) * vertexColor;

    if (originalColor.a < 0.1) {
        discard;
    }

    float hue = (gl_FragCoord.x - gl_FragCoord.y) / 600.0 - timeOffset;
    hue = hue - floor(hue);

    vec3 chroma;
    if (mode == 0) {
        chroma = hsb2rgb_smooth(vec3(
            hue,
            0.75,
            rgb2b(originalColor.rgb)
        ));
    } else {
        chroma = mix(startColor.rgb, endColor.rgb, hue) * rgb2b(originalColor.rgb);
    }

    fragColor = vec4(chroma, originalColor.a);
}