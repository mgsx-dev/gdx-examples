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

	// TODO sourceOffsets doesn't really work with source maps smaller than window size : TODO new test cases
	
	private TiledMapLink head;
	private TiledMap map;
	private int offsetX, offsetY;
	private int sourceOffsetX, sourceOffsetY;
	private int xLeft, xRight, xTop, xBottom;
	
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
		this.windowWidth = MathUtils.ceilPositive(maxWindowWidth / tileWidth) + 1;
		this.windowHeight = MathUtils.ceilPositive(maxWindowHeight / tileHeight) + 1;
		this.tileWidth = tileWidth;
		this.tileHeight = tileHeight;
		
		map = new TiledMap();
	}
	
	public void setMap(TiledMapLink map){
		head = map;
	}
	
	public void update(OrthographicCamera camera){
		
		float worldX = camera.position.x - camera.viewportWidth/2; // TODO handle zoom
		float worldY = camera.position.y - camera.viewportHeight/2; // TODO handle zoom
		
		// TODO if deltaX or deltaY != 0 then recopy
		// and then fill missing parts
		
		// maybe use another map and swap : if is from old then copy else fetch from model ...
		
		// remove some tiles (copy map stream)
		int newOffsetX = MathUtils.floor(worldX / tileWidth);
		int newOffsetY = MathUtils.floor(worldY / tileHeight);
		int deltaX = newOffsetX - offsetX;
		int deltaY = newOffsetY - offsetY;
		
		if(deltaX != 0 || deltaY != 0){
			for(MapLayer layer : map.getLayers()){
				TiledMapTileLayer streamLayer = (TiledMapTileLayer)layer;
				for(int ix = 0 ; ix<windowWidth ; ix++){
					int srcX = deltaX > 0 ? ix + deltaX : windowWidth-1-ix + deltaX;
					int dstX = deltaX > 0 ? ix : windowWidth-1-ix;
					for(int iy=0 ; iy<windowHeight ; iy++){
						int srcY = deltaY > 0 ? iy + deltaY : windowHeight-1-iy + deltaY;
						int dstY = deltaY > 0 ? iy : windowHeight-1-iy;
						streamLayer.setCell(dstX, dstY, streamLayer.getCell(srcX, srcY));
					}
				}
			}
			offsetX += deltaX;
			if(deltaX > 0){
				xRight -= deltaX;
			}else if(deltaX < 0){
				xLeft -= deltaX;
			}
			offsetY += deltaY;
			if(deltaY > 0){
				xTop -= deltaY;
			}else if(deltaY < 0){
				xBottom -= deltaY;
			}
		}
		
		// add some tiles (fill map stream)
		
		// TODO fill 4 times in each direction taking half part into account
//		stream(xLeft, windowWidth, xBottom, windowHeight - xTop);
//		stream(xLeft, windowWidth, xBottom, windowHeight - xTop);
		
		
		
		while(xBottom > 0){
			
			xBottom--;
			
			TiledMap sourceMap = head.map;
			
			int dstY = xBottom;
			int srcY = offsetY + xBottom + sourceOffsetY;
			
			if(srcY < 0){
				if(head.bottomMap != null){
					sourceOffsetY += head.sizeY;
					head = head.bottomMap;
					srcY = offsetY + xBottom + sourceOffsetY;
				}else{
					// TODO wrap mode
					if(wrapBottom == TiledMapWrap.Clamp){
						srcY = 0;
					}
				}
			}
			
			streamV(sourceMap, srcY, dstY);
		}
		
		while(xTop < windowHeight){
			
			TiledMap sourceMap = head.map;
			
			int dstY = xTop;
			int srcY = offsetY + xTop + sourceOffsetY;
			
			if(srcY >= head.sizeY){
				if(head.topMap != null){
					sourceOffsetY -= head.sizeY;
					head = head.topMap;
					srcY = offsetY + xTop + sourceOffsetY;
				}else{
					// TODO wrap mode
					if(wrapTop == TiledMapWrap.Clamp){
						srcY = head.sizeY - 1;
					}
				}
			}
			
			streamV(sourceMap, srcY, dstY);
			
			xTop++;
		}
		
		while(xLeft > 0){
			
			xLeft--;
			
			TiledMap sourceMap = head.map;
			
			int dstX = xLeft;
			int srcX = offsetX + xLeft + sourceOffsetX;
			
			if(srcX < 0){
				if(head.leftMap != null){
					sourceOffsetX += head.sizeX;
					head = head.leftMap;
					srcX = offsetX + xLeft + sourceOffsetX;
				}else{
					// TODO wrap mode
					if(wrapLeft == TiledMapWrap.Clamp){
						srcX = 0;
					}else if(wrapLeft == TiledMapWrap.Repeat){
						// TODO not really working
						sourceOffsetX += head.sizeX;
						srcX = offsetX + xLeft + sourceOffsetX+head.sizeX;
					}
				}
			}
			
			streamH(sourceMap, srcX, dstX);
		}
		
		
		while(xRight < windowWidth){
			
			TiledMap sourceMap = head.map;
			
			int dstX = xRight;
			int srcX = offsetX + xRight + sourceOffsetX;
			
			if(srcX >= head.sizeX){
				if(head.rightMap != null){
					sourceOffsetX -= head.sizeX;
					head = head.rightMap;
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
			
			streamH(sourceMap, srcX, dstX);
			
			xRight++;
		}
	}
	
	private void streamH(TiledMap sourceMap, int srcX, int dstX){
		for(MapLayer layer : sourceMap.getLayers()){
			TiledMapTileLayer sourceLayer = (TiledMapTileLayer)layer;
			TiledMapTileLayer streamLayer = getStreamLayer(sourceLayer);;
			
			for(int iy=0 ; iy<windowHeight ; iy++){
				streamLayer.setCell(dstX, iy, sourceLayer.getCell(srcX, iy));
			}
		}
	}
	
	private void streamV(TiledMap sourceMap, int srcY, int dstY){
		for(MapLayer layer : sourceMap.getLayers()){
			TiledMapTileLayer sourceLayer = (TiledMapTileLayer)layer;
			TiledMapTileLayer streamLayer = getStreamLayer(sourceLayer);
			
			for(int ix=0 ; ix<windowWidth ; ix++){
				streamLayer.setCell(ix, dstY, sourceLayer.getCell(ix, srcY));
			}
		}
	}
	
	private TiledMapTileLayer getStreamLayer(TiledMapTileLayer sourceLayer){
		TiledMapTileLayer streamLayer = (TiledMapTileLayer)map.getLayers().get(sourceLayer.getName());
		if(streamLayer == null){
			streamLayer = new TiledMapTileLayer(windowWidth, windowHeight, tileWidth, tileHeight);
			streamLayer.setName(sourceLayer.getName());
			map.getLayers().add(streamLayer);
		}
		return streamLayer;
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
		camera.position.x -= offsetX * tileWidth;
		camera.position.y -= offsetY * tileHeight;
		camera.update();
	}
	
	private void end(Camera camera) {
		camera.position.x += offsetX * tileWidth;
		camera.position.y += offsetY * tileHeight;
		camera.update();
	}

}
