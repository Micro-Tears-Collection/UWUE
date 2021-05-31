#ifdef VERT
layout(location = 0) in vec3 inPos;
layout(location = 1) in vec2 inUV;
layout(location = 2) in vec3 inNormal;

smooth out vec2 uv;
smooth out vec3 normal;

layout(std140) uniform Mats
{
	mat4 modelViewMatrix;
	mat4 projectMatrix;
//	mat3 normalMatrix;
};

uniform vec2 uvOffset;


void main()
{
    gl_Position = projectMatrix * modelViewMatrix * vec4(inPos, 1.);
    //normal = normalMatrix * inNormal;
	normal = inNormal;
	
	uv = inUV + uvOffset;
}
#endif

#ifdef FRAG
out vec4 fragColor;

smooth in vec2 uv;
smooth in vec3 normal;

uniform sampler2D texUnit0;

void main() 
{
    fragColor = texture2D(texUnit0, uv);
}
#endif