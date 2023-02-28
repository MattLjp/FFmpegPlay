#version 300 es
precision mediump float;
out vec4 outColor;
in vec2 vTextureCoord;
//3个光照颜色
in vec4 ambient;
in vec4 diffuse;
in vec4 specular;
// alpha值
uniform float alpha;
uniform sampler2D sTexture;//为简单说明，暂时只支持1个纹理
void main() {
    //将计算出的颜色给此片元
    vec4 finalColor = texture(sTexture, vTextureCoord);
    finalColor.a *= alpha;
    //给此片元颜色值
    outColor = finalColor * ambient + finalColor * specular + finalColor * diffuse;
}
