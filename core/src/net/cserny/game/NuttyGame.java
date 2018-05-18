package net.cserny.game;

import com.badlogic.gdx.Game;
import com.badlogic.gdx.assets.AssetManager;

public class NuttyGame extends Game {

	private final AssetManager assetManager = new AssetManager();

	@Override
	public void create() {
		setScreen(new LoadingScreen(this));
	}

	public AssetManager getAssetManager() {
		return assetManager;
	}
}
