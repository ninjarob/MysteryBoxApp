package com.selin.mys.MysteryBox.screen.global;

import android.app.AlertDialog;
import android.app.Dialog;
import android.bluetooth.BluetoothAdapter;
import android.content.DialogInterface;
import android.content.Intent;
import android.util.SparseArray;
import android.view.KeyEvent;
import android.widget.Toast;
import com.selin.mys.MysteryBox.GLGame;
import com.selin.mys.MysteryBox.game.BaseGameScene;
import com.selin.mys.MysteryBox.utils.GameConstants;
import org.andengine.engine.camera.Camera;
import org.andengine.engine.options.EngineOptions;
import org.andengine.engine.options.ScreenOrientation;
import org.andengine.engine.options.resolutionpolicy.RatioResolutionPolicy;
import org.andengine.entity.scene.Scene;
import org.andengine.entity.scene.background.AutoParallaxBackground;
import org.andengine.entity.scene.background.Background;
import org.andengine.entity.scene.background.ParallaxBackground;
import org.andengine.entity.sprite.Sprite;
import org.andengine.entity.text.Text;
import org.andengine.extension.multiplayer.protocol.adt.message.IMessage;
import org.andengine.extension.multiplayer.protocol.adt.message.server.ServerMessage;
import org.andengine.extension.multiplayer.protocol.client.connector.BluetoothSocketConnectionServerConnector;
import org.andengine.extension.multiplayer.protocol.client.connector.ServerConnector;
import org.andengine.extension.multiplayer.protocol.exception.BluetoothException;
import org.andengine.extension.multiplayer.protocol.server.BluetoothSocketServer;
import org.andengine.extension.multiplayer.protocol.server.connector.BluetoothSocketConnectionClientConnector;
import org.andengine.extension.multiplayer.protocol.server.connector.ClientConnector;
import org.andengine.extension.multiplayer.protocol.shared.BluetoothSocketConnection;
import org.andengine.extension.multiplayer.protocol.util.MessagePool;
import org.andengine.input.touch.TouchEvent;
import org.andengine.opengl.font.Font;
import org.andengine.opengl.font.FontFactory;
import org.andengine.opengl.texture.ITexture;
import org.andengine.opengl.texture.TextureOptions;
import org.andengine.opengl.texture.atlas.bitmap.BitmapTextureAtlas;
import org.andengine.opengl.texture.atlas.bitmap.BitmapTextureAtlasTextureRegionFactory;
import org.andengine.opengl.texture.region.ITextureRegion;
import org.andengine.opengl.texture.region.TextureRegionFactory;
import org.andengine.opengl.vbo.VertexBufferObjectManager;
import org.andengine.util.debug.Debug;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;

public class GameLoadingScene extends BaseGameScene implements PropertyChangeListener {

    /** Create your own unique UUID at: http://www.uuidgenerator.com/ */
    private static final String EXAMPLE_UUID = "6D2DF50E-06EF-C21C-7DB0-345099A5F64E";

    private static final int CAMERA_WIDTH = 720;
    private static final int CAMERA_HEIGHT = 480;

    private static final short FLAG_MESSAGE_SERVER_ADD_FACE = 1;
    private static final short FLAG_MESSAGE_SERVER_MOVE_FACE = FLAG_MESSAGE_SERVER_ADD_FACE + 1;

    private static final int DIALOG_CHOOSE_SERVER_OR_CLIENT_ID = 0;
    private static final int DIALOG_SHOW_SERVER_IP_ID = DIALOG_CHOOSE_SERVER_OR_CLIENT_ID + 1;

    private static final int REQUESTCODE_BLUETOOTH_ENABLE = 0;
    private static final int REQUESTCODE_BLUETOOTH_CONNECT = REQUESTCODE_BLUETOOTH_ENABLE + 1;

    // ===========================================================
    // Fields
    // ===========================================================

    //private BitmapTextureAtlas mBitmapTextureAtlas;
    private ITextureRegion mFaceTextureRegion;

    private int mFaceIDCounter;
    private final SparseArray<Sprite> mFaces = new SparseArray<Sprite>();

    private String mServerMACAddress;
    private BluetoothSocketServer<BluetoothSocketConnectionClientConnector> mBluetoothSocketServer;
    private ServerConnector<BluetoothSocketConnection> mServerConnector;

    private final MessagePool<IMessage> mMessagePool = new MessagePool<IMessage>();

    private BluetoothAdapter mBluetoothAdapter;

    @Override
    public void propertyChange(PropertyChangeEvent event) {
    }

    // ===========================================================
    // Fields
    // ===========================================================
    AutoParallaxBackground autoParallaxBackground;
    VertexBufferObjectManager vertexBufferObjectManager;

    private BitmapTextureAtlas mAutoParallaxBackgroundTexture;

    private ITextureRegion mParallaxLayerBack;
    private ITextureRegion mParallaxLayerMid;
    private ITextureRegion mParallaxLayerFront;

    private Font mFont;
    private Text statusText;
    private BitmapTextureAtlas mBitmapTextureAtlas;
    private ITextureRegion buttonsTextureRegion;
    private ITextureRegion backButtonTextureRegion;


    public GameLoadingScene(GLGame game) {
        super(game);
        this.initMessagePool();
    }

    private void initMessagePool() {
        //this.mMessagePool.registerMessage(FLAG_MESSAGE_SERVER_ADD_FACE, AddFaceServerMessage.class);
        //this.mMessagePool.registerMessage(FLAG_MESSAGE_SERVER_MOVE_FACE, MoveFaceServerMessage.class);

        this.mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        this.mServerMACAddress = BluetoothAdapter.getDefaultAdapter().getAddress();
        if (this.mBluetoothAdapter == null) {
            Toast.makeText(game, "Bluetooth is not available!", Toast.LENGTH_LONG).show();
            game.finish();
            return;
        } else {
            if (this.mBluetoothAdapter.isEnabled()) {
                game.showDialog(DIALOG_CHOOSE_SERVER_OR_CLIENT_ID);
            } else {
                final Intent enableIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
                game.startActivityForResult(enableIntent, REQUESTCODE_BLUETOOTH_ENABLE);
            }
        }
    }

    protected Dialog onCreateDialog(final int pID) {
        switch(pID) {
            case DIALOG_SHOW_SERVER_IP_ID:
                return new AlertDialog.Builder(game)
                        .setIcon(android.R.drawable.ic_dialog_info)
                        .setTitle("Server-Details")
                        .setCancelable(false)
                        .setMessage("The Name of your Server is:\n" + BluetoothAdapter.getDefaultAdapter().getName() + "\n" + "The MACAddress of your Server is:\n" + this.mServerMACAddress)
                        .setPositiveButton(android.R.string.ok, null)
                        .create();
            case DIALOG_CHOOSE_SERVER_OR_CLIENT_ID:
                return new AlertDialog.Builder(game)
                        .setIcon(android.R.drawable.ic_dialog_info)
                        .setTitle("Be Server or Client ...")
                        .setCancelable(false)
                        .setPositiveButton("Client", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(final DialogInterface pDialog, final int pWhich) {
                                //final Intent intent = new Intent(game, BluetoothListDevicesActivity.class);
                                //game.startActivityForResult(intent, REQUESTCODE_BLUETOOTH_CONNECT);
                            }
                        })

                        .create();
            default:
                return null;
        }
    }

    @Override
    public void initResources() {
        //FONT
        FontFactory.setAssetBasePath("font/");
        final ITexture fontTexture = new BitmapTextureAtlas(game.getTextureManager(), 256, 256, TextureOptions.BILINEAR);
        this.mFont = FontFactory.createFromAsset(game.getFontManager(), fontTexture, game.getAssets(), "Droid.ttf", 16, true, android.graphics.Color.WHITE);
        this.mFont.load();

        BitmapTextureAtlasTextureRegionFactory.setAssetBasePath("gfx/");
        //BACKGROUND
        this.mAutoParallaxBackgroundTexture = new BitmapTextureAtlas(game.getTextureManager(), 800, 1200);
        this.mParallaxLayerFront = BitmapTextureAtlasTextureRegionFactory.createFromAsset(this.mAutoParallaxBackgroundTexture, game, GameConstants.PARALLAX_BACKGROUND_FRONT_FILE, 0, 0);
        this.mParallaxLayerBack = BitmapTextureAtlasTextureRegionFactory.createFromAsset(this.mAutoParallaxBackgroundTexture, game, GameConstants.BACKGROUND_IMAGE_FILE, 0, 188);  //plus the height of the front
        this.mParallaxLayerMid = BitmapTextureAtlasTextureRegionFactory.createFromAsset(this.mAutoParallaxBackgroundTexture, game, GameConstants.PARALLAX_BACKGROUND_MID_FILE, 0, 989);   //plus the height of the front and the back
        this.mAutoParallaxBackgroundTexture.load();

        //BACK BUTTON
        this.mBitmapTextureAtlas = new BitmapTextureAtlas(game.getTextureManager(), 129, 193, TextureOptions.BILINEAR);
        this.buttonsTextureRegion = BitmapTextureAtlasTextureRegionFactory.createFromAsset(mBitmapTextureAtlas, game, "buttons.png", 0, 0);
        this.backButtonTextureRegion = TextureRegionFactory.extractFromTexture(mBitmapTextureAtlas, 64, 64, 64, 64);
        this.mBitmapTextureAtlas.load();
    }

    @Override
    public void initScene() {
        //BACKGROUND
        autoParallaxBackground = new AutoParallaxBackground(0, 0, 0, 5);
        vertexBufferObjectManager = game.getVertexBufferObjectManager();
        autoParallaxBackground.attachParallaxEntity(new ParallaxBackground.ParallaxEntity(0.0f, new Sprite(0, 0, this.mParallaxLayerBack, vertexBufferObjectManager)));
        autoParallaxBackground.attachParallaxEntity(new ParallaxBackground.ParallaxEntity(-5.0f, new Sprite(0, 80, this.mParallaxLayerMid, vertexBufferObjectManager)));
        autoParallaxBackground.attachParallaxEntity(new ParallaxBackground.ParallaxEntity(-10.0f, new Sprite(0, GLGame.CAMERA_HEIGHT - this.mParallaxLayerFront.getHeight(), this.mParallaxLayerFront, vertexBufferObjectManager)));
        setBackground(autoParallaxBackground);

        //FONT
//        statusText = new Text(50, 180, this.mFont,
//                "Status: " + sfb.getStatus(),
//                new TextOptions(HorizontalAlign.LEFT), vertexBufferObjectManager);
//        attachChild(statusText);

        //BACK BUTTON
        final Sprite backButton = new Sprite(10, GLGame.CAMERA_HEIGHT-74, backButtonTextureRegion, game.getVertexBufferObjectManager()) {
            @Override
            public boolean onAreaTouched(final TouchEvent pSceneTouchEvent, final float pTouchAreaLocalX, final float pTouchAreaLocalY) {
                game.setNewScene(new MainMenuScene(game));
                return true;
            }
        };
        attachChild(backButton);
        registerTouchArea(backButton);
        setTouchAreaBindingOnActionDownEnabled(true);
    }











    public EngineOptions onCreateEngineOptions() {
        final Camera camera = new Camera(0, 0, CAMERA_WIDTH, CAMERA_HEIGHT);

        return new EngineOptions(true, ScreenOrientation.LANDSCAPE_FIXED, new RatioResolutionPolicy(CAMERA_WIDTH, CAMERA_HEIGHT), camera);
    }

    protected void onDestroy() {
        if(this.mBluetoothSocketServer != null) {
            //try {
                //this.mBluetoothSocketServer.sendBroadcastServerMessage(new ConnectionCloseServerMessage());
            //} catch (final IOException e) {
            //    Debug.e(e);
           // }
            this.mBluetoothSocketServer.terminate();
        }

        if(this.mServerConnector != null) {
            this.mServerConnector.terminate();
        }

        //game.onDestroy();
    }

    public boolean onKeyUp(final int pKeyCode, final KeyEvent pEvent) {
        switch(pKeyCode) {
            case KeyEvent.KEYCODE_BACK:
                game.finish();
                return true;
        }
        return game.onKeyUp(pKeyCode, pEvent);
    }

    public void onCreateResources() {
        BitmapTextureAtlasTextureRegionFactory.setAssetBasePath("gfx/");

        //this.mBitmapTextureAtlas = new BitmapTextureAtlas(this.getTextureManager(), 32, 32, TextureOptions.BILINEAR);
        //this.mFaceTextureRegion = BitmapTextureAtlasTextureRegionFactory.createFromAsset(this.mBitmapTextureAtlas, this, "face_box.png", 0, 0);
        this.mBitmapTextureAtlas.load();
    }

    public Scene onCreateScene() {
        //this.mEngine.registerUpdateHandler(new FPSLogger());

        final Scene scene = new Scene();
        scene.setBackground(new Background(0.09804f, 0.6274f, 0.8784f));

		/* We allow only the server to actively send around messages. */
//        if(MultiplayerBluetoothExample.this.mBluetoothSocketServer != null) {
//            scene.setOnSceneTouchListener(new IOnSceneTouchListener() {
//                @Override
//                public boolean onSceneTouchEvent(final Scene pScene, final TouchEvent pSceneTouchEvent) {
//                    if(pSceneTouchEvent.isActionDown()) {
//                        try {
//                            final AddFaceServerMessage addFaceServerMessage = (AddFaceServerMessage) MultiplayerBluetoothExample.this.mMessagePool.obtainMessage(FLAG_MESSAGE_SERVER_ADD_FACE);
//                            addFaceServerMessage.set(MultiplayerBluetoothExample.this.mFaceIDCounter++, pSceneTouchEvent.getX(), pSceneTouchEvent.getY());
//
//                            MultiplayerBluetoothExample.this.mBluetoothSocketServer.sendBroadcastServerMessage(addFaceServerMessage);
//
//                            MultiplayerBluetoothExample.this.mMessagePool.recycleMessage(addFaceServerMessage);
//                        } catch (final IOException e) {
//                            Debug.e(e);
//                        }
//                        return true;
//                    } else {
//                        return false;
//                    }
//                }
//            });

//            scene.setOnAreaTouchListener(new IOnAreaTouchListener() {
//                @Override
//                public boolean onAreaTouched(final TouchEvent pSceneTouchEvent, final ITouchArea pTouchArea, final float pTouchAreaLocalX, final float pTouchAreaLocalY) {
//                    try {
//                        final Sprite face = (Sprite)pTouchArea;
//                        final Integer faceID = (Integer)face.getUserData();
//
//                        final MoveFaceServerMessage moveFaceServerMessage = (MoveFaceServerMessage) MultiplayerBluetoothExample.this.mMessagePool.obtainMessage(FLAG_MESSAGE_SERVER_MOVE_FACE);
//                        moveFaceServerMessage.set(faceID, pSceneTouchEvent.getX(), pSceneTouchEvent.getY());
//
//                        MultiplayerBluetoothExample.this.mBluetoothSocketServer.sendBroadcastServerMessage(moveFaceServerMessage);
//
//                        MultiplayerBluetoothExample.this.mMessagePool.recycleMessage(moveFaceServerMessage);
//                    } catch (final IOException e) {
//                        Debug.e(e);
//                        return false;
//                    }
//                    return true;
//                }
//            });
//
//            scene.setTouchAreaBindingOnActionDownEnabled(true);
//        }
//
//        return scene;
        return null;
    }

    protected void onActivityResult(final int pRequestCode, final int pResultCode, final Intent pData) {
//        switch(pRequestCode) {
//            case REQUESTCODE_BLUETOOTH_ENABLE:
//                this.showDialog(DIALOG_CHOOSE_SERVER_OR_CLIENT_ID);
//                break;
//            case REQUESTCODE_BLUETOOTH_CONNECT:
//                this.mServerMACAddress = pData.getExtras().getString(BluetoothListDevicesActivity.EXTRA_DEVICE_ADDRESS);
//                this.initClient();
//                break;
//            default:
//                super.onActivityResult(pRequestCode, pResultCode, pData);
//        }
    }

    // ===========================================================
    // Methods
    // ===========================================================

    public void addFace(final int pID, final float pX, final float pY) {
//        final Scene scene = this.mEngine.getScene();
//		/* Create the face and add it to the scene. */
//        final Sprite face = new Sprite(0, 0, this.mFaceTextureRegion, this.getVertexBufferObjectManager());
//        face.setPosition(pX - face.getWidth() * 0.5f, pY - face.getHeight() * 0.5f);
//        face.setUserData(pID);
//        this.mFaces.put(pID, face);
//        scene.registerTouchArea(face);
//        scene.attachChild(face);
    }

    public void moveFace(final int pID, final float pX, final float pY) {
		/* Find and move the face. */
        final Sprite face = this.mFaces.get(pID);
        face.setPosition(pX - face.getWidth() * 0.5f, pY - face.getHeight() * 0.5f);
    }

    private void initServerAndClient() {
        this.initServer();

		/* Wait some time after the server has been started, so it actually can start up. */
        try {
            Thread.sleep(500);
        } catch (final Throwable t) {
            Debug.e(t);
        }

        this.initClient();
    }

    private void initServer() {
        this.mServerMACAddress = BluetoothAdapter.getDefaultAdapter().getAddress();
        try {
            this.mBluetoothSocketServer = new BluetoothSocketServer<BluetoothSocketConnectionClientConnector>(EXAMPLE_UUID, new ExampleClientConnectorListener(), new ExampleServerStateListener()) {
                @Override
                protected BluetoothSocketConnectionClientConnector newClientConnector(final BluetoothSocketConnection pBluetoothSocketConnection) throws IOException {
                    try {
                        return new BluetoothSocketConnectionClientConnector(pBluetoothSocketConnection);
                    } catch (final BluetoothException e) {
                        Debug.e(e);
						/* Actually cannot happen. */
                        return null;
                    }
                }
            };
        } catch (final BluetoothException e) {
            Debug.e(e);
        }

        this.mBluetoothSocketServer.start();
    }

    private void initClient() {
//        try {
//            this.mServerConnector = new BluetoothSocketConnectionServerConnector(new BluetoothSocketConnection(this.mBluetoothAdapter, this.mServerMACAddress, EXAMPLE_UUID), new ExampleServerConnectorListener());
//
//            this.mServerConnector.registerServerMessage(FLAG_MESSAGE_SERVER_CONNECTION_CLOSE, ConnectionCloseServerMessage.class, new IServerMessageHandler<BluetoothSocketConnection>() {
//                @Override
//                public void onHandleMessage(final ServerConnector<BluetoothSocketConnection> pServerConnector, final IServerMessage pServerMessage) throws IOException {
//                    GameLoadingScene.this.finish();
//                }
//            });
//
//            this.mServerConnector.registerServerMessage(FLAG_MESSAGE_SERVER_ADD_FACE, AddFaceServerMessage.class, new IServerMessageHandler<BluetoothSocketConnection>() {
//                @Override
//                public void onHandleMessage(final ServerConnector<BluetoothSocketConnection> pServerConnector, final IServerMessage pServerMessage) throws IOException {
//                    final AddFaceServerMessage addFaceServerMessage = (AddFaceServerMessage)pServerMessage;
//                    GameLoadingScene.this.addFace(addFaceServerMessage.mID, addFaceServerMessage.mX, addFaceServerMessage.mY);
//                }
//            });
//
//            this.mServerConnector.registerServerMessage(FLAG_MESSAGE_SERVER_MOVE_FACE, MoveFaceServerMessage.class, new IServerMessageHandler<BluetoothSocketConnection>() {
//                @Override
//                public void onHandleMessage(final ServerConnector<BluetoothSocketConnection> pServerConnector, final IServerMessage pServerMessage) throws IOException {
//                    final MoveFaceServerMessage moveFaceServerMessage = (MoveFaceServerMessage)pServerMessage;
//                    GameLoadingScene.this.moveFace(moveFaceServerMessage.mID, moveFaceServerMessage.mX, moveFaceServerMessage.mY);
//                }
//            });
//
//            this.mServerConnector.getConnection().start();
//        } catch (final Throwable t) {
//            Debug.e(t);
//        }
    }

    private void log(final String pMessage) {
        Debug.d(pMessage);
    }

    private void toast(final String pMessage) {
//        this.log(pMessage);
//        this.runOnUiThread(new Runnable() {
//            @Override
//            public void run() {
//                Toast.makeText(GameLoadingScene.this, pMessage, Toast.LENGTH_SHORT).show();
//            }
//        });
    }

    // ===========================================================
    // Inner and Anonymous Classes
    // ===========================================================

    public static class AddFaceServerMessage extends ServerMessage {
        private int mID;
        private float mX;
        private float mY;

        public AddFaceServerMessage() {

        }

        public AddFaceServerMessage(final int pID, final float pX, final float pY) {
            this.mID = pID;
            this.mX = pX;
            this.mY = pY;
        }

        public void set(final int pID, final float pX, final float pY) {
            this.mID = pID;
            this.mX = pX;
            this.mY = pY;
        }

        @Override
        public short getFlag() {
            return FLAG_MESSAGE_SERVER_ADD_FACE;
        }

        @Override
        protected void onReadTransmissionData(final DataInputStream pDataInputStream) throws IOException {
            this.mID = pDataInputStream.readInt();
            this.mX = pDataInputStream.readFloat();
            this.mY = pDataInputStream.readFloat();
        }

        @Override
        protected void onWriteTransmissionData(final DataOutputStream pDataOutputStream) throws IOException {
            pDataOutputStream.writeInt(this.mID);
            pDataOutputStream.writeFloat(this.mX);
            pDataOutputStream.writeFloat(this.mY);
        }
    }

    public static class MoveFaceServerMessage extends ServerMessage {
        private int mID;
        private float mX;
        private float mY;

        public MoveFaceServerMessage() {

        }

        public MoveFaceServerMessage(final int pID, final float pX, final float pY) {
            this.mID = pID;
            this.mX = pX;
            this.mY = pY;
        }

        public void set(final int pID, final float pX, final float pY) {
            this.mID = pID;
            this.mX = pX;
            this.mY = pY;
        }

        @Override
        public short getFlag() {
            return FLAG_MESSAGE_SERVER_MOVE_FACE;
        }

        @Override
        protected void onReadTransmissionData(final DataInputStream pDataInputStream) throws IOException {
            this.mID = pDataInputStream.readInt();
            this.mX = pDataInputStream.readFloat();
            this.mY = pDataInputStream.readFloat();
        }

        @Override
        protected void onWriteTransmissionData(final DataOutputStream pDataOutputStream) throws IOException {
            pDataOutputStream.writeInt(this.mID);
            pDataOutputStream.writeFloat(this.mX);
            pDataOutputStream.writeFloat(this.mY);
        }
    }

    private class ExampleServerConnectorListener implements BluetoothSocketConnectionServerConnector.IBluetoothSocketConnectionServerConnectorListener {
        @Override
        public void onStarted(final ServerConnector<BluetoothSocketConnection> pConnector) {
            GameLoadingScene.this.toast("CLIENT: Connected to server.");
        }

        @Override
        public void onTerminated(final ServerConnector<BluetoothSocketConnection> pConnector) {
            GameLoadingScene.this.toast("CLIENT: Disconnected from Server...");
            //GameLoadingScene.this.finish();
        }
    }

    private class ExampleServerStateListener implements BluetoothSocketServer.IBluetoothSocketServerListener<BluetoothSocketConnectionClientConnector> {
        @Override
        public void onStarted(final BluetoothSocketServer<BluetoothSocketConnectionClientConnector> pBluetoothSocketServer) {
            GameLoadingScene.this.toast("SERVER: Started.");
        }

        @Override
        public void onTerminated(final BluetoothSocketServer<BluetoothSocketConnectionClientConnector> pBluetoothSocketServer) {
            GameLoadingScene.this.toast("SERVER: Terminated.");
        }

        @Override
        public void onException(final BluetoothSocketServer<BluetoothSocketConnectionClientConnector> pBluetoothSocketServer, final Throwable pThrowable) {
            Debug.e(pThrowable);
            GameLoadingScene.this.toast("SERVER: Exception: " + pThrowable);
        }
    }

    private class ExampleClientConnectorListener implements BluetoothSocketConnectionClientConnector.IBluetoothSocketConnectionClientConnectorListener {
        @Override
        public void onStarted(final ClientConnector<BluetoothSocketConnection> pConnector) {
            GameLoadingScene.this.toast("SERVER: Client connected: " + pConnector.getConnection().getBluetoothSocket().getRemoteDevice().getAddress());
        }

        @Override
        public void onTerminated(final ClientConnector<BluetoothSocketConnection> pConnector) {
            GameLoadingScene.this.toast("SERVER: Client disconnected: " + pConnector.getConnection().getBluetoothSocket().getRemoteDevice().getAddress());
        }
    }

}
