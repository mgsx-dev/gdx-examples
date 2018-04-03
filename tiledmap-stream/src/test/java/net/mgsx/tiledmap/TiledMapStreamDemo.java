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
	
	private TiledMap map;
	
	@Override
	public void create() {
		camera = new OrthographicCamera();
		renderer = new OrthogonalTiledMapRenderer(null);
		map = new TmxMapLoader().load("map-00-00.tmx");
	}
	
	@Override
	public void render() {
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
		
		float delta = Gdx.graphics.getDeltaTime();
		float speed = 128;
		camera.position.x += dx * speed * delta;
		camera.position.y += dy * speed * delta;
		camera.update();
		
		Gdx.gl.glClearColor(.7f, .9f, 1f, 0f);
		Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);
		
		renderer.setMap(map);
		renderer.setView(camera);
		renderer.render();
	}
	
	@Override
	public void resize(int width, int height) {
		camera.setToOrtho(false, width, height);
	}
}
