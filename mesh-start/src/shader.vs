attribute vec4 a_position;
attribute float a_index;

uniform mat4 u_projTrans;
uniform float u_time;

void main()
{
	float modulation = sin(u_time + a_index);
	gl_Position =  u_projTrans * (a_position * vec4(modulation, modulation, modulation, 1.0));
}
