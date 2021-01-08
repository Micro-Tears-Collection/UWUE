#version 110

varying vec2 uv;
varying vec2 uv2;

uniform sampler2D texUnit0;
uniform sampler2D texUnit1;
void main (void) 
{
    gl_FragColor = floor(texture2D(texUnit0, uv) * 32. + texture2D(texUnit1, uv2)) / 32.;
}
