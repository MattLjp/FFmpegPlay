#version 300 es
precision mediump float;
uniform sampler2D uTextureUnit;
uniform sampler2D uTextureAlpha;
in vec2 vTexCoord;
out vec4 vFragColor;

void main() {
    vec4 color = texture(uTextureUnit, vTexCoord);
    color.a = texture(uTextureAlpha, vTexCoord).r;
    vFragColor = color;
}
