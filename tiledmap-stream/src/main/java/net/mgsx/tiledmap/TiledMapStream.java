package net.mgsx.tiledmap;

import com.badlogic.gdx.graphics.Camera;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.maps.MapLayer;
import com.badlogic.gdx.maps.tiled.TiledMap;
import com.badlogic.gdx.maps.tiled.TiledMapTileLayer;
import com.badlogic.gdx.maps.tiled.TiledMapTileLayer.Cell;
import com.badlogic.gdx.maps.tiled.renderers.OrthogonalTiledMapRenderer;
import com.badlogic.gdx.math.MathUtils;

/**
 * Implementation with only one cycling map.
 * 
 * TODO create an alternate implementation with 4 cycling maps.
 * 
 * @author mgsx
 *
 */
public class TiledMapStream
{

	private TiledMapLink head, tail;
	private TiledMap map;
	private int offsetX;
	private int sourceOffsetX;
	private int xLeft, xRight;
	
	private TiledMapWrap wrapLeft = TiledMapWrap.None;
	private TiledMapWrap wrapRight = TiledMapWrap.None;
	private TiledMapWrap wrapTop = TiledMapWrap.None;
	private TiledMapWrap wrapBottom = TiledMapWrap.None;
	
	int windowWidth, windowHeight, tileWidth, tileHeight;
	
	/**
	 * 
	 * @param maxWindowSize max window size is the max visible size in world coordinates.
	 * this should include any zoom changes ...
	 */
	public TiledMapStream(float maxWindowWidth, float maxWindowHeight, int tileWidth, int tileHeight) 
	{
		this.windowWidth = MathUtils.ceilPositive(maxWindowWidth / tileWidth);
		this.windowHeight = MathUtils.ceilPositive(maxWindowHeight / tileHeight);
		this.tileWidth = tileWidth;
		this.tileHeight = tileHeight;
		
		map = new TiledMap();
	}
	
	public TiledMapLink appendMap(TiledMap map){
		TiledMapLink mapLink = new TiledMapLink();
		mapLink.map = map;
		mapLink.sizeX = map.getProperties().get("width", Integer.class);
		mapLink.previousMap = tail;
		if(tail != null){
			tail.nextMap = mapLink;
		}
		tail = mapLink;
		if(head == null){
			head = tail;
		}
		return mapLink;
	}
	
	public void update(OrthographicCamera camera){
		
		float worldX = camera.position.x - camera.viewportWidth/2; // TODO handle zoom
		
		// TODO if deltaX or deltaY != 0 then recopy
		// and then fill missing parts
		
		// maybe use another map and swap : if is from old then copy else fetch from model ...
		
		// remove some tiles (copy map stream)
		int newOffsetX = MathUtils.floor(worldX / tileWidth);
		int deltaX = newOffsetX - offsetX;
		
		if(deltaX != 0){
			for(MapLayer layer : map.getLayers()){
				TiledMapTileLayer streamLayer = (TiledMapTileLayer)layer;
				for(int ix = 0 ; ix<windowWidth ; ix++){
					int srcX = deltaX > 0 ? ix + deltaX : windowWidth-1-ix + deltaX;
					int dstX = deltaX > 0 ? ix : windowWidth-1-ix;
					for(int iy=0 ; iy<windowHeight ; iy++){
						streamLayer.setCell(dstX, iy, streamLayer.getCell(srcX, iy));
					}
				}
			}
			offsetX += deltaX;
			if(deltaX > 0){
				xRight -= deltaX;
			}else{
				xLeft -= deltaX;
			}
		}
		
		// add some tiles (fill map stream)
		while(xLeft > 0){
			
			xLeft--;
			
			TiledMap sourceMap = head.map;
			
			int dstX = xLeft;
			int srcX = offsetX + xLeft + sourceOffsetX;
			
			if(srcX < 0){
				if(head.previousMap != null){
					sourceOffsetX += head.sizeX;
					head = head.previousMap;
					srcX = offsetX + xLeft + sourceOffsetX;
				}else{
					// TODO wrap mode
					if(wrapRight == TiledMapWrap.Clamp){
						srcX = 0;
					}else if(wrapRight == TiledMapWrap.Repeat){
						// TODO not really working
						sourceOffsetX += head.sizeX;
						srcX = offsetX + xLeft + sourceOffsetX+head.sizeX;
					}
				}
			}
			
			for(MapLayer layer : sourceMap.getLayers()){
				TiledMapTileLayer sourceLayer = (TiledMapTileLayer)layer;
				TiledMapTileLayer streamLayer = (TiledMapTileLayer)map.getLayers().get(layer.getName());
				if(streamLayer == null){
					streamLayer = new TiledMapTileLayer(windowWidth, windowHeight, tileWidth, tileHeight);
					streamLayer.setName(sourceLayer.getName());
					map.getLayers().add(streamLayer);
				}
				
				for(int iy=0 ; iy<windowHeight ; iy++){
					streamLayer.setCell(dstX, iy, sourceLayer.getCell(srcX, iy));
				}
			}
		}
		
		
		while(xRight < windowWidth){
			
			TiledMap sourceMap = head.map;
			
			int dstX = xRight;
			int srcX = offsetX + xRight + sourceOffsetX;
			
			if(srcX >= head.sizeX){
				if(head.nextMap != null){
					sourceOffsetX -= head.sizeX;
					head = head.nextMap;
					srcX = offsetX + xRight + sourceOffsetX;
				}else{
					// TODO wrap mode
					if(wrapRight == TiledMapWrap.Clamp){
						srcX = head.sizeX - 1;
					}else if(wrapRight == TiledMapWrap.Repeat){
						sourceOffsetX -= head.sizeX;
						srcX = offsetX + xRight + sourceOffsetX;
					}
				}
			}
			
			for(MapLayer layer : sourceMap.getLayers()){
				TiledMapTileLayer sourceLayer = (TiledMapTileLayer)layer;
				TiledMapTileLayer streamLayer = (TiledMapTileLayer)map.getLayers().get(layer.getName());
				if(streamLayer == null){
					streamLayer = new TiledMapTileLayer(windowWidth, windowHeight, tileWidth, tileHeight);
					streamLayer.setName(sourceLayer.getName());
					map.getLayers().add(streamLayer);
				}
				
				for(int iy=0 ; iy<windowHeight ; iy++){
					streamLayer.setCell(dstX, iy, sourceLayer.getCell(srcX, iy));
				}
			}
			
			xRight++;
		}
	}

	public TiledMapTileLayer getTileLayer(String name) {
		return (TiledMapTileLayer)map.getLayers().get(name);
	}

	/**
	 * Get cell from a map stream layer
	 * @param layer
	 * @param ix
	 * @param iy
	 * @return
	 */
	public Cell getCell(TiledMapTileLayer layer, int ix, int iy) {
		return layer.getCell(ix - offsetX, iy);
	}

	public void setWrap(TiledMapWrap wrapLeft, TiledMapWrap wrapRight, TiledMapWrap wrapTop, TiledMapWrap wrapBottom) {
		this.wrapLeft = wrapLeft;
		this.wrapRight = wrapRight;
		this.wrapTop = wrapTop;
		this.wrapBottom = wrapBottom;
	}

	// TODO how to offer same behavior as TMR (set view, render, render layers ...)
	public void render(OrthographicCamera camera, OrthogonalTiledMapRenderer renderer) {
		renderer.setMap(map);
		begin(camera);
		renderer.setView(camera);
		renderer.render();
		end(camera);
	}
	
	private void begin(Camera camera) {
		camera.position.x -= offsetX * 32;
		camera.update();
	}
	
	private void end(Camera camera) {
		camera.position.x += offsetX * 32;
		camera.update();
	}

}
