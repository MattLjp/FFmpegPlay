#version 300 es
//OpenGL ES3.0外部纹理扩展
#extension GL_OES_EGL_image_external_essl3: require
precision mediump float;
uniform samplerExternalOES yuvTexSampler;
uniform int vChangeType;
uniform vec3 vChangeData;
in vec2 yuvTexCoords;
out vec4 vFragColor;
void modifyColor(vec4 color) {
    color.r = max(min(color.r, 1.0), 0.0);
    color.g = max(min(color.g, 1.0), 0.0);
    color.b = max(min(color.b, 1.0), 0.0);
    color.a = max(min(color.a, 1.0), 0.0);
}

void main() {
    vec4 vCameraColor = texture(yuvTexSampler, yuvTexCoords);
    if (vChangeType == 1) {
        float c = vCameraColor.r * vChangeData.r + vCameraColor.g * vChangeData.g + vCameraColor.b * vChangeData.b;
        vFragColor = vec4(c, c, c, vCameraColor.a);
    } else if (vChangeType == 2) {
        vec4 deltaColor = vCameraColor + vec4(vChangeData, 0.0);
        modifyColor(deltaColor);
        vFragColor = deltaColor;
    } else {
        vFragColor = vCameraColor;
    }
}