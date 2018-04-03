package net.mgsx.tiledmap;

import com.badlogic.gdx.Game;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.backends.lwjgl.LwjglApplication;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.maps.tiled.TiledMap;
import com.badlogic.gdx.maps.tiled.TmxMapLoader;
import com.badlogic.gdx.maps.tiled.renderers.OrthogonalTiledMapRenderer;

public class TiledMapStreamDemo extends Game {
	public static void main (String[] arg) {
		new LwjglApplication(new TiledMapStreamDemo());
	}

	private OrthographicCamera camera;
	private OrthogonalTiledMapRenderer renderer;
	private TiledMapStream mapStream;
	
	@Override
	public void create() {
		camera = new OrthographicCamera();
		renderer = new OrthogonalTiledMapRenderer(null);
		
		TiledMap map = new TmxMapLoader().load("map-00-00.tmx");
		
		mapStream = new TiledMapStream(640, 480, 32, 32);
		TiledMapLink link = mapStream.appendMap(map);
		
		// mode loop both ways
//		link.previousMap = link;
//		link.nextMap = link;
		
		// mode clamp all
		mapStream.setWrap(TiledMapWrap.Clamp, TiledMapWrap.Clamp, TiledMapWrap.Clamp, TiledMapWrap.Clamp);
		
		// mode config
		// XXX mapStream.setWrap(TiledMapWrap.Clamp, TiledMapWrap.Repeat, TiledMapWrap.Clamp, TiledMapWrap.Clamp);
	}
	
	@Override
	public void render() {
		float delta = Gdx.graphics.getDeltaTime();
		
		updateCamera(delta);
		
		// stream the map to ensure visibility from camera bounds
		mapStream.update(camera);
		
		Gdx.gl.glClearColor(.7f, .9f, 1f, 0f);
		Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);
		
		// render the map at camera bounds
		mapStream.render(camera, renderer);
	}
	
	private void updateCamera(float delta) {
		float dx=0, dy=0;
		if(Gdx.input.isKeyPressed(Input.Keys.LEFT)){
			dx = -1;
		}
		if(Gdx.input.isKeyPressed(Input.Keys.RIGHT)){
			dx = 1;
		}
		if(Gdx.input.isKeyPressed(Input.Keys.UP)){
			dy = 1;
		}
		if(Gdx.input.isKeyPressed(Input.Keys.DOWN)){
			dy = -1;
		}
		if(Gdx.input.isKeyPressed(Input.Keys.PAGE_UP)){
			camera.zoom *= 1 + delta;
		}
		if(Gdx.input.isKeyPressed(Input.Keys.PAGE_DOWN)){
			camera.zoom /= 1 + delta;
		}
		if(Gdx.input.isKeyPressed(Input.Keys.HOME)){
			camera.zoom = 1;
		}
		
		float speed = 128;
		camera.position.x += dx * speed * delta;
		camera.position.y += dy * speed * delta;
		camera.update();
	}
	
	@Override
	public void resize(int width, int height) {
		camera.setToOrtho(false, width, height);
	}
}
