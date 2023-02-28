#version 300 es
precision mediump float;
uniform sampler2D utextureUnit;
in vec2 vTexCoord;
out vec4 vFragColor;

void main() {
    vFragColor = texture(utextureUnit, vTexCoord);
}
