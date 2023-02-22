#version 300 es
layout (location = 0) in vec4 vPosition;
layout (location = 1) in vec2 vCoordinate;
uniform mat4 vMatrix;

out vec4 aPos;
out vec4 gPosition;
out vec2 aCoordinate;

void main() {
    gl_Position = vMatrix * vPosition;
    aPos = vPosition;
    gPosition = vMatrix * vPosition;
    aCoordinate = vCoordinate;
}