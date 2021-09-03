#ifdef VERT
layout(location = 0) in vec3 inPos;

noperspective out vec2 uv;
noperspective out vec2 uv2;

layout(std140) uniform mats
{
	mat4 modelView;
	mat4 project;
};

uniform float ditherW;
uniform float ditherH;

void main()
{
    gl_Position = project * modelView * vec4(inPos, 1.);
	
	uv = inPos.xy;
	uv2.x = inPos.x*ditherW;
	uv2.y = (1.-inPos.y)*ditherH;
}
#endif

#ifdef FRAG
out vec4 fragColor;

noperspective in vec2 uv;
noperspective in vec2 uv2;

uniform sampler2D texUnit0;
uniform sampler2D texUnit1;

void main() 
{
    fragColor = floor(texture2D(texUnit0, uv) * 32. + texture2D(texUnit1, uv2)) / 32.;
}
#endif