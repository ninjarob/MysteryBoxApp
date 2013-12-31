package com.selin.mys.MysteryBox.screen.global;

import com.selin.mys.MysteryBox.GLGame;
import com.selin.mys.MysteryBox.game.BaseGameScene;
import org.andengine.entity.scene.background.Background;
import org.andengine.util.color.Color;

public class TipsScene extends BaseGameScene {


	public void setImageFileName(String imageFileName, int width, int height) {

	}

	public TipsScene(GLGame game) {
		super(game);
	}

	@Override
	public void initResources() {
		//scrollingImageView.initResources();
	}

	@Override
	public void initScene() {
		//scrollingImageView.initScene();

		this.setBackground(new Background(Color.WHITE));
	}
}
