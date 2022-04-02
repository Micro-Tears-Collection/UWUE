#ifdef VERT
layout(location = 0) in vec3 inPos;

#ifdef TEXTURED
layout(location = 1) in vec2 inUV;
smooth out vec2 uv;
uniform vec4 uvOffMul;
#endif

#ifdef VERTEX_COLOR
layout(location = 3) in vec4 inCol;
smooth out vec4 vColor;
#endif

layout(std140) uniform mats
{
	mat4 modelView;
	mat4 project;
};

uniform vec4 clipXY;

void main()
{
    gl_Position = project * modelView * vec4(inPos, 1.);
	gl_ClipDistance[0] = gl_Position.x - clipXY.x;
	gl_ClipDistance[1] = clipXY.z - gl_Position.x;
	gl_ClipDistance[2] = -gl_Position.y - clipXY.y;
	gl_ClipDistance[3] = clipXY.w + gl_Position.y;
	
	#ifdef TEXTURED
	uv = inUV * uvOffMul.zw + uvOffMul.xy;
	#endif
	
	#ifdef VERTEX_COLOR
	vColor = inCol;
	#endif
}
#endif

#ifdef FRAG
out vec4 fragColor;

uniform vec4 color;

#ifdef TEXTURED
smooth in vec2 uv;
uniform sampler2D texUnit0;
#endif

#ifdef VERTEX_COLOR
smooth in vec4 vColor;
#endif

void main() 
{
	vec4 col = color;
	
	#ifdef TEXTURED
	col *= texture(texUnit0, uv);
	#endif
	
	#ifdef VERTEX_COLOR
	col *= vColor;
	#endif
	
    fragColor = col;
}
#endif