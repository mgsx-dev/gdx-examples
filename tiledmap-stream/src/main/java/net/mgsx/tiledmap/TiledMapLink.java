package net.mgsx.tiledmap;

import com.badlogic.gdx.maps.tiled.TiledMap;

public class TiledMapLink {
	public TiledMap map;
	public TiledMapLink nextMap, previousMap;
	public int sizeX;
}
