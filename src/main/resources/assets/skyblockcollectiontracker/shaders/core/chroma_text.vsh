#version 150

//? if 26.3
//#extension GL_ARB_separate_shader_objects : require

//~ if 26.3 'in vec3 Position;' -> 'layout(location = 0) in vec3 Position;' as _
//~ if 26.3 'in vec2 UV0;' -> 'layout(location = 1) in vec2 UV0;' as _
//~ if 26.3 'in vec4 Color;' -> 'layout(location = 2) in vec4 Color;' as _
//~ if 26.3 'out vec4 vertexColor;' -> 'layout(location = 0) out vec4 vertexColor;' as _
//~ if 26.3 'out vec2 texCoord0;' -> 'layout(location = 1) out vec2 texCoord0;' as _

in vec3 Position;
in vec2 UV0;
in vec4 Color;

#moj_import <minecraft:dynamictransforms.glsl>
#moj_import <minecraft:projection.glsl>

out vec4 vertexColor;
out vec2 texCoord0;

void main() {
    gl_Position = ProjMat * ModelViewMat * vec4(Position, 1.0);
    vertexColor = Color;
    texCoord0 = UV0;
}