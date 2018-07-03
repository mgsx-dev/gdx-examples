package net.mgsx.gdx.example;

import com.badlogic.gdx.ApplicationAdapter;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.backends.lwjgl.LwjglApplication;
import com.badlogic.gdx.graphics.Camera;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.Mesh;
import com.badlogic.gdx.graphics.PerspectiveCamera;
import com.badlogic.gdx.graphics.VertexAttribute;
import com.badlogic.gdx.graphics.VertexAttributes;
import com.badlogic.gdx.graphics.g3d.utils.CameraInputController;
import com.badlogic.gdx.graphics.glutils.ShaderProgram;
import com.badlogic.gdx.math.Vector3;

public class MeshStart extends ApplicationAdapter
{
	public static void main (String[] arg) {
		new LwjglApplication(new MeshStart());
	}

	protected Camera camera;
	private CameraInputController cameraControl;
	private float time;
	private Mesh mesh;
	private ShaderProgram shader;
	
	@Override
	public void create () 
	{
		// create camera and controller
		camera = new PerspectiveCamera(45f, 640, 480);
		camera.position.set(2, 5, -10);
		camera.up.set(Vector3.Y);
		camera.lookAt(Vector3.Zero);
		camera.near = .1f;
		camera.far = 15f * (float) Math.sqrt(2);
		camera.update();
		
		cameraControl = new CameraInputController(camera);
		cameraControl.autoUpdate = true;
		
		// create models
		VertexAttributes attributes = new VertexAttributes(
				VertexAttribute.Position(),
				new VertexAttribute(VertexAttributes.Usage.Generic, 2, "a_index")
				);
		
		int DIM = 20;
		int numVertices = DIM * DIM * DIM;
		int numIndices = 0;
		int i=0;
		float scale = 1f / (DIM - 1);
		
		int stride = attributes.vertexSize / 4;
		float [] vertices = new float[numVertices * stride];
		
		for(int z=0 ; z<DIM ; z++){
			for(int y=0 ; y<DIM ; y++){
				for(int x=0 ; x<DIM ; x++){
					vertices[i+0] = x * scale;
					vertices[i+1] = y * scale;
					vertices[i+2] = z * scale;
					vertices[i+3] = ((z * DIM + y) * DIM + x);
					vertices[i+4] = (x + y + z) * scale;
					i+=stride;
				}
			}
		}
		
		mesh = new Mesh(true, numVertices, numIndices, attributes);
		
		mesh.setVertices(vertices);
		
		shader = new ShaderProgram(Gdx.files.classpath("shader.vs"), Gdx.files.classpath("shader.fs"));
		
		Gdx.input.setInputProcessor(cameraControl);
	}
	
	@Override
	public void render () 
	{
		float deltaTime = Gdx.graphics.getDeltaTime();
		updateScene(deltaTime);
		drawScene();
	}
	
	protected void updateScene(float deltaTime)
	{
		time += deltaTime;
		// update camera
		cameraControl.update();
	}
	
	protected void drawScene()
	{
		Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT | GL20.GL_DEPTH_BUFFER_BIT);
		
		shader.begin();
		shader.setUniformMatrix("u_projTrans", camera.combined);
		shader.setUniformf("u_time", time);
		mesh.render(shader, GL20.GL_POINTS);
		shader.end();
	}
	
	@Override
	public void dispose () {
		// dispose shaders and meshes
		mesh.dispose(); 
		shader.dispose();
	}
}
