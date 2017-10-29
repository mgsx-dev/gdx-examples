package net.mgsx.gdx.example;

import com.badlogic.gdx.ApplicationAdapter;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.InputAdapter;
import com.badlogic.gdx.InputMultiplexer;
import com.badlogic.gdx.InputProcessor;
import com.badlogic.gdx.backends.lwjgl.LwjglApplication;
import com.badlogic.gdx.graphics.Camera;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.PerspectiveCamera;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.Texture.TextureFilter;
import com.badlogic.gdx.graphics.VertexAttribute;
import com.badlogic.gdx.graphics.VertexAttributes;
import com.badlogic.gdx.graphics.g3d.Environment;
import com.badlogic.gdx.graphics.g3d.Material;
import com.badlogic.gdx.graphics.g3d.Model;
import com.badlogic.gdx.graphics.g3d.ModelBatch;
import com.badlogic.gdx.graphics.g3d.ModelInstance;
import com.badlogic.gdx.graphics.g3d.attributes.ColorAttribute;
import com.badlogic.gdx.graphics.g3d.attributes.FloatAttribute;
import com.badlogic.gdx.graphics.g3d.attributes.TextureAttribute;
import com.badlogic.gdx.graphics.g3d.environment.DirectionalLight;
import com.badlogic.gdx.graphics.g3d.utils.CameraInputController;
import com.badlogic.gdx.graphics.g3d.utils.ModelBuilder;
import com.badlogic.gdx.math.Intersector;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.math.collision.Ray;
import com.badlogic.gdx.utils.Array;

public class G3DStart extends ApplicationAdapter
{
	public static void main (String[] arg) {
		new LwjglApplication(new G3DStart());
	}

	private static class Ball
	{
		ModelInstance instance;
		Vector3 position = new Vector3();
		
		public void setColor(Color color) {
			instance.materials.first().get(ColorAttribute.class, ColorAttribute.Diffuse).color.set(color);
		}
	}
	
	protected ModelBatch batch;
	protected Camera camera;
	private CameraInputController cameraControl;
	protected Array<Ball> balls = new Array<Ball>();
	protected Environment environment;
	private float time;
	private final float ballRadius = 1;
	private Model backgroundModel, ballModel;
	private ModelInstance backgroundInstance;
	
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
		VertexAttributes attributes = new VertexAttributes(VertexAttribute.Position(), VertexAttribute.Normal());
		VertexAttributes bgAttributes = new VertexAttributes(VertexAttribute.Position(), VertexAttribute.Normal(), VertexAttribute.TexCoords(0));
		
		ModelBuilder mb = new ModelBuilder();
		Material backgroundMaterial = new Material(ColorAttribute.createDiffuse(.1f,.1f,.1f,.1f));
		Texture backgroundTexture = new Texture(Gdx.files.internal("stone2.png"));
		backgroundTexture.setFilter(TextureFilter.Linear, TextureFilter.Linear);
		backgroundMaterial.set(TextureAttribute.createDiffuse(backgroundTexture));
		backgroundMaterial.set(TextureAttribute.createSpecular(backgroundTexture));
		backgroundMaterial.set(ColorAttribute.createSpecular(Color.WHITE));
		backgroundMaterial.set(FloatAttribute.createShininess(12f));
		
		backgroundModel = mb.createBox(10, 2, 10, backgroundMaterial , bgAttributes.getMask());
		backgroundInstance = new ModelInstance(backgroundModel);
		
		Material ballMaterial = new Material(ColorAttribute.createDiffuse(Color.RED));
		ballModel = mb.createSphere(ballRadius, ballRadius, ballRadius, 5, 4, ballMaterial , attributes.getMask());
		
		// create environment
		environment = createEnvironment();
		
		Gdx.gl.glClearColor(0, 0, 0, 0);
		
		// create utilities
		batch = new ModelBatch();
		
		// register inputs
		InputProcessor ballPicker = new InputAdapter(){
			@Override
			public boolean touchDown(int screenX, int screenY, int pointer, int button) {
				pickup(camera.getPickRay(screenX, screenY));
				return super.touchDown(screenX, screenY, pointer, button);
			}
		};
		Gdx.input.setInputProcessor(new InputMultiplexer(ballPicker, cameraControl));
		
	}
	
	protected Environment createEnvironment()
	{
		Environment environment = new Environment();
		environment.set(new ColorAttribute(ColorAttribute.AmbientLight, Color.GRAY));
		environment.set(new ColorAttribute(ColorAttribute.Reflection, Color.WHITE));
		//environment.set(ColorAttribute.c);
		
		environment.set(new ColorAttribute(ColorAttribute.Fog, new Color(.5f, .6f, .9f, 1)));
		
		DirectionalLight light = new DirectionalLight();
		light.color.set(Color.WHITE);
		light.direction.set(.1f, -1, .3f).nor();
		
		environment.add(light);
		
		return environment;
	}
	
	private void pickup(Ray ray){
		// find nearest ball under ray
		Ball nearestBall = null;
		float minDistance = 0;
		for(Ball ball : balls){
			Vector3 intersection = new Vector3();
			if(Intersector.intersectRaySphere(ray, ball.position, ballRadius/2, intersection)){
				float dst = intersection.dst(camera.position);
				if(nearestBall == null || dst < minDistance){
					nearestBall = ball;
					minDistance = dst;
				}
			}
		}
		// colorize the ball if any
		if(nearestBall != null){
			nearestBall.setColor(Color.DARK_GRAY);
		}
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
		// udpdate ball emitter
		time -= deltaTime;
		if(time <= 0){
			Ball ball = new Ball();
			ball.instance = new ModelInstance(ballModel);
			ball.position.x = MathUtils.lerp(-5, 5, MathUtils.random());
			ball.position.y = 5;
			ball.position.z = MathUtils.lerp(-5, 5, MathUtils.random());
			ball.setColor(Color.RED);
			
			balls.add(ball);
			
			time += 1f; // next ball in 1 second
		}
		
		// update balls
		for(int i=0 ; i<balls.size ;){
			Ball ball = balls.get(i);
			ball.position.y -= deltaTime * 1;
			if(balls.get(i).position.y < 0){
				balls.removeIndex(i);
			}else{
				ball.instance.transform.setTranslation(ball.position);
				i++;
			}
		}
		
		// update camera
		cameraControl.update();
	}
	
	protected void drawScene()
	{
		Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT | GL20.GL_DEPTH_BUFFER_BIT);
		
		batch.begin(camera);
		batch.render(backgroundInstance, environment);
		for(Ball ball : balls) {
			batch.render(ball.instance, environment);
		}
		batch.end();
	}
	
	@Override
	public void dispose () {
		// dispose shaders and meshes
		batch.dispose(); 
		backgroundModel.dispose();
		ballModel.dispose();
	}
}
