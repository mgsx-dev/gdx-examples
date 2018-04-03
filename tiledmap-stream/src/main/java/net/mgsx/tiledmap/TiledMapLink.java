package net.mgsx.tiledmap;

import com.badlogic.gdx.maps.tiled.TiledMap;

public class TiledMapLink {
	public TiledMap map;
	public TiledMapLink rightMap, leftMap, topMap, bottomMap;
	public int sizeX, sizeY;
	public TiledMapLink(TiledMap map) {
		super();
		this.map = map;
		sizeX = map.getProperties().get("width", Integer.class);
		sizeY = map.getProperties().get("height", Integer.class);
	}
	
}
