#version 300 es
layout (location = 0) in vec3 a_position;//顶点
layout (location = 1) in vec3 a_normal;//法线
layout (location = 2) in vec2 a_texCoord;//纹理

uniform mat4 u_MVPMatrix;//MVP矩阵
uniform mat4 u_ModelMatrix;//模型矩阵

uniform vec3 lightPos;//光照位置
uniform vec3 uCamera;//摄像机位置

out vec2 vTextureCoord;
//冯氏光照模型(Phong Lighting Model)
//由三种元素光组成，分别是环境光(Ambient Lighting)、散射光(Diffuse Lighting)及镜面光(Specular Lighting)
out vec4 ambient;
out vec4 diffuse;
out vec4 specular;

//定位光光照计算的方法
void pointLight(
in vec3 normal, //法向量
inout vec4 ambient, //环境光最终强度
inout vec4 diffuse, //散射光最终强度
inout vec4 specular, //镜面光最终强度
in vec3 lightLocation, //光源位置
in vec4 lightAmbient, //环境光强度
in vec4 lightDiffuse, //散射光强度
in vec4 lightSpecular  //镜面光强度
) {
    ambient = lightAmbient;            //直接得出环境光的最终强度
    vec3 normalTarget = a_position + normal;    //计算变换后的法向量
    vec3 newNormal = (u_ModelMatrix * vec4(normalTarget, 1)).xyz - (u_ModelMatrix * vec4(a_position, 1)).xyz;
    newNormal = normalize(newNormal);    //对法向量规格化
    //计算从表面点到摄像机的向量
    vec3 eye = normalize(uCamera - (u_ModelMatrix * vec4(a_position, 1)).xyz);
    //计算从表面点到光源位置的向量vp
    vec3 vp = normalize(lightLocation - (u_ModelMatrix * vec4(a_position, 1)).xyz);
    vp = normalize(vp);//格式化vp
    vec3 halfVector = normalize(vp + eye);    //求视线与光线的半向量
    float shininess = 50.0;                //粗糙度，越小越光滑
    float nDotViewPosition = max(0.0, dot(newNormal, vp));    //求法向量与vp的点积与0的最大值
    diffuse = lightDiffuse * nDotViewPosition;                //计算散射光的最终强度
    float nDotViewHalfVector = dot(newNormal, halfVector);    //法线与半向量的点积
    float powerFactor = max(0.0, pow(nDotViewHalfVector, shininess));    //镜面反射光强度因子
    specular = lightSpecular * powerFactor;                //计算镜面光的最终强度
}

void main() {
    gl_Position = u_MVPMatrix * vec4(a_position, 1); //根据总变换矩阵计算此次绘制此顶点位置

    //存放环境光、散射光、镜面反射光的临时变量
    vec4 ambientTemp;
    vec4 diffuseTemp;
    vec4 specularTemp;

    pointLight(normalize(a_normal), ambientTemp, diffuseTemp, specularTemp, lightPos, vec4(0.15, 0.15, 0.15, 1.0), vec4(0.9, 0.9, 0.9, 1.0), vec4(0.4, 0.4, 0.4, 1.0));

    ambient = ambientTemp;
    diffuse = diffuseTemp;
    specular = specularTemp;
    vTextureCoord = a_texCoord;//将接收的纹理坐标传递给片元着色器
}
