//LIGHT for light
//NORMALMAP for normal map
//vec3 inTangent, sampler2D normalMap

//SPECULAR for specularity
//vec3 specular, float roughness
//SPECULARMAP for specular map
//sampler2D specularMap

//ROUGHNESSMAP for roughness
//sampler2D  roughnessMap

//EMISSIONMAP

#ifdef LIGHT
struct Light {
	vec4 pos;
	vec4 col;
	vec4 spotDirCutoff;
};

layout(std140) uniform lights
{
	vec4 ambientLight;
	Light lightsData[MAX_LIGHTS];
};
#endif

#ifdef VERT
layout(location = 0) in vec3 inPos;
layout(location = 1) in vec2 inUV;
layout(location = 2) in vec4 inNormal;
#ifdef NORMALMAP
layout(location = 3) in vec4 inTangent;
#endif

smooth out vec2 fragUV;
smooth out vec3 fragPos;
smooth out vec3 fragPosOrig;
smooth out vec3 fragNorm;

#ifdef LIGHT
smooth out vec3[MAX_LIGHTS] lightsDirs;
smooth out vec3[MAX_LIGHTS] lightsSpotDirs;
#endif

smooth out vec4 fogOut;
smooth out float fogExp;

layout(std140) uniform mats 
{
	mat4 modelView;
	mat4 project;
};

layout(std140) uniform fog 
{
	vec4 fogColor; //+linear / density
	vec2 fogEndScale;
};

uniform vec2 uvOffset;

void main()
{
	vec4 pos = modelView * vec4(inPos, 1.);
	vec3 norm = normalize((modelView * inNormal).xyz);
	
    gl_Position = project * pos;
	fragUV = inUV + uvOffset;
	
	fogOut.rgb = fogColor.rgb;
	fogOut.a = mix(
		pos.z * fogEndScale.y + fogEndScale.x,
		pos.z * fogEndScale.x * 1.4427,
		fogColor.a
	);
	fogExp = fogColor.a;
	
	#ifdef LIGHT
	#ifdef NORMALMAP
	vec3 T = normalize(inTangent.xyz - dot(inTangent.xyz, inNormal.xyz) * inNormal.xyz);
	T = normalize((modelView * vec4(T, 0.)).xyz);
	vec3 B = cross(norm, T) * inTangent.w;
	
	mat3 TBN = transpose(mat3(T, B, norm));
	#else
	mat3 TBN = mat3(vec3(1., 0., 0.), vec3(0., 1., 0.), vec3(0., 0., 1.));
	#endif
	
	for(int i=0; i<MAX_LIGHTS; i++) {
		Light light = lightsData[i];
		
		if(light.pos.w > 0.5) {
			//Point light
			lightsDirs[i] = TBN * (light.pos.xyz - pos.xyz);
			lightsSpotDirs[i] = TBN * light.spotDirCutoff.xyz;
		} else {
			lightsDirs[i] = normalize(TBN * -light.pos.xyz) * sqrt(100000.);
		}
	}
	
	fragPos.xyz = TBN * pos.xyz;
	#else
	fragPos = pos.xyz;
	#endif
	fragPosOrig = pos.xyz;
	fragNorm = norm;
}
#endif

#ifdef FRAG
out vec4 fragColor;

smooth in vec2 fragUV;
smooth in vec3 fragPos;
smooth in vec3 fragPosOrig;
smooth in vec3 fragNorm;

smooth in vec4 fogOut;
smooth in float fogExp;

uniform sampler2D albedoMap;
#ifdef NORMALMAP
uniform sampler2D normalMap;
#endif
#ifdef SPECULARMAP
uniform sampler2D specularMap;
#endif
#ifdef ROUGHNESSMAP
uniform sampler2D roughnessMap;
#endif
#ifdef PARALLAXMAP
uniform sampler2D parallaxMap;
#endif
#ifdef EMISSIONMAP
uniform sampler2D emissionMap;
#endif

uniform float alphaThreshold;
#ifdef SPECULAR
uniform float roughness;
uniform vec3 specular;
#endif

const float SRGB_GAMMA = 1.0 / 2.2;
const float SRGB_INVERSE_GAMMA = 2.2;
const float SRGB_ALPHA = 0.055;
const float SRGB_ALPHAPONE = 1.055;

// Converts a single linear channel to srgb
float linear_to_srgb(float channel) {
    if(channel <= 0.0031308)
        return 12.92 * channel;
    else
        return SRGB_ALPHAPONE * pow(channel, 1.0/2.4) - SRGB_ALPHA;
}

// Converts a single srgb channel to rgb
float srgb_to_linear(float channel) {
    if (channel <= 0.04045)
        return channel / 12.92;
    else
        return pow((channel + SRGB_ALPHA) / SRGB_ALPHAPONE, 2.4);
}

// Converts a linear rgb color to a srgb color (exact, not approximated)
vec3 rgb_to_srgb(vec3 rgb) {
    return vec3(
        linear_to_srgb(rgb.r),
        linear_to_srgb(rgb.g),
        linear_to_srgb(rgb.b)
    );
}

// Converts a srgb color to a linear rgb color (exact, not approximated)
vec3 srgb_to_rgb(vec3 srgb) {
    return vec3(
        srgb_to_linear(srgb.r),
        srgb_to_linear(srgb.g),
        srgb_to_linear(srgb.b)
    );
}

#ifdef LIGHT

smooth in vec3[MAX_LIGHTS] lightsDirs;
smooth in vec3[MAX_LIGHTS] lightsSpotDirs;

float spotLight(vec3 lightVec, vec3 spot, float spotCutoff) {
	float spotDot = dot(-lightVec, spot);
	float oneMcutoff = 1.0 - spotCutoff;
	return clamp((spotDot - 1.0 + oneMcutoff) / oneMcutoff, 0.0, 1.0);
}

#ifdef SPECULAR
float DistributionGGX(vec3 N, vec3 H, float roughness)
{
    float roughness2     = roughness * roughness;
    float NdotH  = max(dot(N, H), 0.0);
    float NdotH2 = NdotH*NdotH;
	
    float nom    = roughness2;
    float denom  = (NdotH2 * (roughness2 - 1.0) + 1.0);
    denom        = 3.1415926535 * denom * denom;
	
    return nom / denom;
}
#endif

vec3 calcLight(int i, float roughness, vec3 specular, vec3 norm, vec3 normalizedPos) {
	vec3 lightVec = lightsDirs[i];
	
	float dist = length(lightVec);
	float invDist = 1.0 / dist;
	
	lightVec *= invDist;
	
	//Diffuse
	float NdotV = max(dot(norm, lightVec), 0.) * 100000. * invDist * invDist;
	vec3 radiance = lightsData[i].col.rgb * NdotV;
	
	//Spot
	float spotCutoff = lightsData[i].spotDirCutoff.w;
	if(spotCutoff >= 0.) radiance *= spotLight(lightVec, normalize(lightsSpotDirs[i]), spotCutoff);
	
	vec3 lightCol = radiance;
	
	#ifdef SPECULAR
	vec3 halfVec = normalize(lightVec - normalizedPos);
	lightCol += DistributionGGX(norm, halfVec, roughness) * radiance * specular;
	#endif
	
	return lightCol;
}

#endif

#ifdef PARALLAXMAP
vec2 parallaxMapping(vec2 texCoords, vec3 viewDir) { 
    // number of depth layers
    const float minLayers = 8;
    const float maxLayers = 32;
	float heightScale = 0.02;
    float numLayers = mix(maxLayers, minLayers, abs(dot(vec3(0.0, 0.0, 1.0), viewDir)));  
    // calculate the size of each layer
    float layerDepth = 1.0 / numLayers;
    // depth of current layer
    float currentLayerDepth = 0.0;
    // the amount to shift the texture coordinates per layer (from vector P)
    vec2 P = viewDir.xy / viewDir.z * heightScale; 
    vec2 deltaTexCoords = P / numLayers;
  
    // get initial values
    vec2  currentTexCoords     = texCoords;
    float currentDepthMapValue = texture2D(parallaxMap, currentTexCoords).r;
      
    while(currentLayerDepth < currentDepthMapValue) {
        // shift texture coordinates along direction of P
        currentTexCoords -= deltaTexCoords;
        // get depthmap value at current texture coordinates
        currentDepthMapValue = texture2D(parallaxMap, currentTexCoords).r;  
        // get depth of next layer
        currentLayerDepth += layerDepth; 
    }
    
    // get texture coordinates before collision (reverse operations)
    vec2 prevTexCoords = currentTexCoords + deltaTexCoords;

    // get depth after and before collision for linear interpolation
    float afterDepth  = currentDepthMapValue - currentLayerDepth;
    float beforeDepth = texture2D(parallaxMap, prevTexCoords).r - currentLayerDepth + layerDepth;
 
    // interpolation of texture coordinates
    float weight = afterDepth / (afterDepth - beforeDepth);
    vec2 finalTexCoords = prevTexCoords * weight + currentTexCoords * (1.0 - weight);

    return finalTexCoords;
}
#endif

void main()
{
	#ifdef PARALLAXMAP
	vec2 uv = parallaxMapping(fragUV, normalize(fragPos) * vec3(1., -1., -1.));
	#else
	vec2 uv = fragUV;
	#endif
	
	vec4 tex = texture2D(albedoMap, uv);
	tex.rgb = srgb_to_rgb(tex.rgb);
	
	if(tex.a <= alphaThreshold) discard;
	
	#ifdef LIGHT
	vec3 lightsSumm = ambientLight.rgb;
	
	#ifdef NORMALMAP
	vec3 norm = texture2D(normalMap, uv).xyz * 2.0 - vec3(1.0);
	norm.z = sqrt(1. - dot(norm.xy, norm.xy));
	#else
	vec3 norm = normalize(fragNorm);
	#endif
	
	vec3 normalizedPos = normalize(fragPos);
	
	#ifdef SPECULAR
	float roughnessVal = roughness;
	#ifdef ROUGHNESSMAP
	roughnessVal *= texture2D(roughnessMap, uv).x;
	#endif
	
	vec3 specularVal = specular;
	#ifdef SPECULARMAP
	specularVal *= texture2D(specularMap, uv).x;
	#endif
	
	#else
	float roughnessVal = 1.0;
	vec3 specularVal = vec3(0.);
	#endif
	
	for(int i=0; i<MAX_LIGHTS; i++) {
		lightsSumm += calcLight(i, roughnessVal, specularVal, norm, normalizedPos);
	}
	
	//lightsSumm += vec3(0.01) * 100000. / ((fragPosOrig.y + 275.)*(fragPosOrig.y + 275.)) * max(0., 1. - abs(fragNorm.y));
	
	tex.rgb = max(tex.rgb * lightsSumm, 0.0);
	#endif
	
	#ifdef EMISSIONMAP
	tex.rgb += srgb_to_rgb(texture2D(emissionMap, uv).rgb);
	#endif
	
	float fog;
	if(fogExp > 0.5) fog = exp2(fogOut.a);
	else fog = fogOut.a;
	
	tex.rgb = rgb_to_srgb(mix(fogOut.rgb, tex.rgb, clamp(fog, 0., 1.)));
	
    fragColor = tex;
}
#endif