package com.selin.mys.MysteryBox.screen.global;

import com.selin.mys.MysteryBox.GLGame;
import com.selin.mys.MysteryBox.game.BaseGameScene;
import org.andengine.engine.camera.hud.HUD;
import org.andengine.extension.physics.box2d.PhysicsWorld;
import org.andengine.opengl.font.Font;
import org.andengine.opengl.font.FontFactory;
import org.andengine.opengl.texture.ITexture;
import org.andengine.opengl.texture.TextureOptions;
import org.andengine.opengl.texture.atlas.bitmap.BitmapTextureAtlas;
import org.andengine.opengl.texture.atlas.bitmap.BitmapTextureAtlasTextureRegionFactory;

public class GameScene extends BaseGameScene {

    // ===========================================================
    // Fields
    // ===========================================================
    public GLGame game;
    private PhysicsWorld physicsWorld;
    private HUD hud;

    public GameScene(GLGame game) {
        super(game);

        this.game = game;
    }

    @Override
    public void initResources() {
        //FONT
        FontFactory.setAssetBasePath("font/");
        final ITexture fontTexture = new BitmapTextureAtlas(game.getTextureManager(), 256, 256, TextureOptions.BILINEAR);
        Font mFont = FontFactory.createFromAsset(game.getFontManager(), fontTexture, game.getAssets(), "Droid.ttf", 16, true, android.graphics.Color.WHITE);
        mFont.load();

        //init background resource
        BitmapTextureAtlasTextureRegionFactory.setAssetBasePath("gfx/");

    }

    @Override
    public void initScene() {

        //BACKGROUND
//        Sprite backgroundSprite = new Sprite(0, 0, gameSceneInfo.getMapBackground(), gameSceneInfo.getVertexBufferObjectManager());
//        backgroundSprite.setScale(2.4f);
//        SpriteBackground normalBackground = new SpriteBackground(0, 0, 0, backgroundSprite);
//        setBackground(normalBackground);
//
//        //physics
//        this.physicsWorld = new FixedStepPhysicsWorld(60, new Vector2(0, 0), false, 8, 8);
//
//        //boundaries
//        final Rectangle ground = new Rectangle(-1*gameSceneInfo.getSystemRadius(), gameSceneInfo.getSystemRadius(), 2*gameSceneInfo.getSystemRadius(), 2, gameSceneInfo.getVertexBufferObjectManager());
//        final Rectangle roof = new Rectangle(-1*gameSceneInfo.getSystemRadius(), -1*gameSceneInfo.getSystemRadius(), 2*gameSceneInfo.getSystemRadius(), 2, gameSceneInfo.getVertexBufferObjectManager());
//        final Rectangle leftWall = new Rectangle(-1*gameSceneInfo.getSystemRadius(), -gameSceneInfo.getSystemRadius(), 2, 2*gameSceneInfo.getSystemRadius(), gameSceneInfo.getVertexBufferObjectManager());
//        final Rectangle rightWall = new Rectangle(gameSceneInfo.getSystemRadius(), -gameSceneInfo.getSystemRadius(), 2, 2*gameSceneInfo.getSystemRadius(), gameSceneInfo.getVertexBufferObjectManager());
//
//        PhysicsFactory.createBoxBody(physicsWorld, ground, BodyDef.BodyType.StaticBody, GameConstants.WALL_FIXTURE_DEF);
//        PhysicsFactory.createBoxBody(physicsWorld, roof, BodyDef.BodyType.StaticBody, GameConstants.WALL_FIXTURE_DEF);
//        PhysicsFactory.createBoxBody(physicsWorld, leftWall, BodyDef.BodyType.StaticBody, GameConstants.WALL_FIXTURE_DEF);
//        PhysicsFactory.createBoxBody(physicsWorld, rightWall, BodyDef.BodyType.StaticBody, GameConstants.WALL_FIXTURE_DEF);
//        attachChild(ground);
//        attachChild(roof);
//        attachChild(leftWall);
//        attachChild(rightWall);
//
//        for (CelestialBody sb : gameSceneInfo.getCelestialBodies()) {
//            sb.setScene(this);
//            sb.initForScene(physicsWorld);
//        }
//
//        //listeners
//        setOnAreaTouchListener(this);
//        setOnSceneTouchListener(this);
//        hud.setOnAreaTouchListener(this);
//
//        for (GameObject o : gameObjects) {
//            o.initForScene(physicsWorld);
//        }
//
//        registerUpdateHandler(physicsWorld);
//        registerUpdateHandler(guh);
//
//        setTouchAreaBindingOnActionDownEnabled(true);
    }


    // ===========================================================
    // Methods
    // ===========================================================

    public void back() {
        super.back();
        game.setNewScene(new MainMenuScene(game));
    }

}
