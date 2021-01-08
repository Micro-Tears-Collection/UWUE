#version 110

varying vec2 uv;
varying vec2 uv2;

uniform float ditherW;
uniform float ditherH;

void main (void)
{
    gl_Position = gl_ProjectionMatrix * gl_ModelViewMatrix * gl_Vertex;
	
	uv = gl_Vertex.xy;
	uv2.x = gl_Vertex.x*ditherW;
	uv2.y = (1.-gl_Vertex.y)*ditherH;
}