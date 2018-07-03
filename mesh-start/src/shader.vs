attribute vec4 a_position;
attribute vec2 a_index;

uniform mat4 u_projTrans;
uniform float u_time;

varying float v_distance;

void main()
{
	float modulation = sin(u_time + a_index.x);
	vec3 pos = vec3(sin(u_time * 0.5 + a_index.y) * 2.0 * modulation, modulation, modulation);
	v_distance = length(pos) * 0.5;
	gl_Position =  u_projTrans * (a_position * vec4(pos, 1.0));
}
