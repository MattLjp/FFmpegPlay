#version 300 es

precision mediump float;
uniform sampler2D vTexture;
uniform int vChangeType;
uniform vec3 vChangeColor;
uniform int vIsHalf;
uniform float uXY;

in vec4 aPos;
in vec4 gPosition;
in vec2 aCoordinate;
out vec4 vFragColor;

void modifyColor(vec4 color) {
    color.r = max(min(color.r, 1.0), 0.0);
    color.g = max(min(color.g, 1.0), 0.0);
    color.b = max(min(color.b, 1.0), 0.0);
    color.a = max(min(color.a, 1.0), 0.0);
}

void main() {
    vec4 nColor = texture(vTexture, aCoordinate);
    if (aPos.x > 0.0 || vIsHalf == 0) {
        if (vChangeType == 1) {
            float c = nColor.r * vChangeColor.r + nColor.g * vChangeColor.g + nColor.b * vChangeColor.b;
            vFragColor = vec4(c, c, c, nColor.a);
        } else if (vChangeType == 2) {
            vec4 deltaColor = nColor + vec4(vChangeColor, 0.0);
            modifyColor(deltaColor);
            vFragColor = deltaColor;
        } else if (vChangeType == 3) {
            nColor += texture(vTexture, vec2(aCoordinate.x - vChangeColor.r, aCoordinate.y - vChangeColor.r));
            nColor += texture(vTexture, vec2(aCoordinate.x - vChangeColor.r, aCoordinate.y + vChangeColor.r));
            nColor += texture(vTexture, vec2(aCoordinate.x + vChangeColor.r, aCoordinate.y - vChangeColor.r));
            nColor += texture(vTexture, vec2(aCoordinate.x + vChangeColor.r, aCoordinate.y + vChangeColor.r));
            nColor += texture(vTexture, vec2(aCoordinate.x - vChangeColor.g, aCoordinate.y - vChangeColor.g));
            nColor += texture(vTexture, vec2(aCoordinate.x - vChangeColor.g, aCoordinate.y + vChangeColor.g));
            nColor += texture(vTexture, vec2(aCoordinate.x + vChangeColor.g, aCoordinate.y - vChangeColor.g));
            nColor += texture(vTexture, vec2(aCoordinate.x + vChangeColor.g, aCoordinate.y + vChangeColor.g));
            nColor += texture(vTexture, vec2(aCoordinate.x - vChangeColor.b, aCoordinate.y - vChangeColor.b));
            nColor += texture(vTexture, vec2(aCoordinate.x - vChangeColor.b, aCoordinate.y + vChangeColor.b));
            nColor += texture(vTexture, vec2(aCoordinate.x + vChangeColor.b, aCoordinate.y - vChangeColor.b));
            nColor += texture(vTexture, vec2(aCoordinate.x + vChangeColor.b, aCoordinate.y + vChangeColor.b));
            nColor /= 13.0;
            vFragColor = nColor;
        } else if (vChangeType == 4) {
            float dis = distance(vec2(gPosition.x, gPosition.y / uXY), vec2(vChangeColor.r, vChangeColor.g));
            if (dis < vChangeColor.b) {
                nColor = texture(vTexture, vec2(aCoordinate.x / 2.0 + 0.25, aCoordinate.y / 2.0 + 0.25));
            }
            vFragColor = nColor;
        } else {
            vFragColor = nColor;
        }
    } else {
        vFragColor = nColor;
    }
}