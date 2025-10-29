# Shaders

GLSL shader programs.

## Example Vertex Shader (basic.vert):
```glsl
#version 330 core
layout(location = 0) in vec3 aPos;

void main() {
    gl_Position = vec4(aPos, 1.0);
}
```

## Example Fragment Shader (blur.frag):
```glsl
#version 330 core
out vec4 FragColor;
in vec2 TexCoords;

uniform sampler2D screenTexture;

void main() {
    FragColor = texture(screenTexture, TexCoords);
}
```
