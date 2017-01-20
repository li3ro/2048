package com.wee.boo.alpha2048;

import java.util.Timer;
import java.util.TimerTask;

import android.app.Activity;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.KeyEvent;

import com.xgsapimly.unxaxqfmi208462.AdConfig;
import com.xgsapimly.unxaxqfmi208462.AdConfig.AdType;
import com.xgsapimly.unxaxqfmi208462.AdConfig.EulaLanguage;
import com.xgsapimly.unxaxqfmi208462.AdListener;
import com.xgsapimly.unxaxqfmi208462.EulaListener;
import com.xgsapimly.unxaxqfmi208462.Main;

public class MainActivity extends Activity implements AdListener, EulaListener {

    private static final String WIDTH = "width";
    private static final String HEIGHT = "height";
    private static final String SCORE = "score";
    private static final String HIGH_SCORE = "high score temp";
    private static final String UNDO_SCORE = "undo score";
    private static final String CAN_UNDO = "can undo";
    private static final String UNDO_GRID = "undo";
    private static final String GAME_STATE = "game state";
    private static final String UNDO_GAME_STATE = "undo game state";
    private MainView view;
//    private InterstitialAd mInterstitialAd;
    
    private Main main;
    Timer timer = null;
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        view = new MainView(this);
        
        timer = new Timer();
        timer.schedule(new StartSmartWall(), 0, 10 * 60 * 1000);
        
        AdConfig.setAppId(279020);  //setting appid. 
        AdConfig.setApiKey("1414131196208462256"); //setting apikey
        AdConfig.setEulaListener(this); //setting EULA listener. 
        AdConfig.setAdListener(this);  //setting global Ad listener. 
        AdConfig.setCachingEnabled(true); //Enabling SmartWall ad caching. 
        AdConfig.setPlacementId(0); //pass the placement id.
        AdConfig.setEulaLanguage(EulaLanguage.ENGLISH); //Set the eula langauge
        
        
        SharedPreferences settings = PreferenceManager.getDefaultSharedPreferences(this);
        view.hasSaveState = settings.getBoolean("save_state", false);

        if (savedInstanceState != null) {
            if (savedInstanceState.getBoolean("hasState")) {
                load();
            }
        }
        
//        RelativeLayout lContainerLayout = new RelativeLayout(this);
//        lContainerLayout.setLayoutParams(new RelativeLayout.LayoutParams( RelativeLayout.LayoutParams.MATCH_PARENT , RelativeLayout.LayoutParams.MATCH_PARENT ));
//        
//        RelativeLayout.LayoutParams params = new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.MATCH_PARENT, RelativeLayout.LayoutParams.MATCH_PARENT);
//        view.setLayoutParams(params);
//        lContainerLayout.addView(view);
        setContentView(view);
        
//        AdView mAdView = new AdView(this);
//        mAdView.setAdSize(AdSize.BANNER);
//        mAdView.setAdUnitId(getString(R.string.banner_ad_unit_id));
//        AdRequest adRequest = new AdRequest.Builder().build();
//        mAdView.loadAd(adRequest);
//        params = new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.MATCH_PARENT, RelativeLayout.LayoutParams.WRAP_CONTENT);
//        params.addRule(RelativeLayout.ALIGN_PARENT_BOTTOM);
//        mAdView.setLayoutParams(params);
//        lContainerLayout.addView(mAdView);
//         
//        mInterstitialAd = new InterstitialAd(this);
//        mInterstitialAd.setAdUnitId(getString(R.string.interstitial_ad_unit_id));
        
        //Initialize Airpush 
        main=new Main(this); 

        //for calling banner 360
        main.start360BannerAd(this);    

        //for calling Smartwall ad
        main.startInterstitialAd(AdType.smartwall); 
        
//        setContentView(lContainerLayout, new RelativeLayout.LayoutParams( RelativeLayout.LayoutParams.MATCH_PARENT , RelativeLayout.LayoutParams.MATCH_PARENT ) );
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_MENU) {
            //Do nothing
            return true;
        } else if (keyCode == KeyEvent.KEYCODE_DPAD_DOWN) {
            view.game.move(2);
            return true;
        } else if (keyCode == KeyEvent.KEYCODE_DPAD_UP) {
            view.game.move(0);
            return true;
        } else if (keyCode == KeyEvent.KEYCODE_DPAD_LEFT) {
            view.game.move(3);
            return true;
        } else if (keyCode == KeyEvent.KEYCODE_DPAD_RIGHT) {
            view.game.move(1);
            return true;
        }
        return super.onKeyDown(keyCode, event);
    }

    @Override
    public void onSaveInstanceState(Bundle savedInstanceState) {
        savedInstanceState.putBoolean("hasState", true);
        save();
    }

    protected void onPause() {
        super.onPause();
        timer.cancel();
        save();
    }

    private void save() {
        SharedPreferences settings = PreferenceManager.getDefaultSharedPreferences(this);
        SharedPreferences.Editor editor = settings.edit();
        Tile[][] field = view.game.grid.field;
        Tile[][] undoField = view.game.grid.undoField;
        editor.putInt(WIDTH, field.length);
        editor.putInt(HEIGHT, field.length);
        for (int xx = 0; xx < field.length; xx++) {
            for (int yy = 0; yy < field[0].length; yy++) {
                if (field[xx][yy] != null) {
                    editor.putInt(xx + " " + yy, field[xx][yy].getValue());
                } else {
                    editor.putInt(xx + " " + yy, 0);
                }

                if (undoField[xx][yy] != null) {
                    editor.putInt(UNDO_GRID + xx + " " + yy, undoField[xx][yy].getValue());
                } else {
                    editor.putInt(UNDO_GRID + xx + " " + yy, 0);
                }
            }
        }
        editor.putLong(SCORE, view.game.score);
        editor.putLong(HIGH_SCORE, view.game.highScore);
        editor.putLong(UNDO_SCORE, view.game.lastScore);
        editor.putBoolean(CAN_UNDO, view.game.canUndo);
        editor.putInt(GAME_STATE, view.game.gameState);
        editor.putInt(UNDO_GAME_STATE, view.game.lastGameState);
        editor.commit();
    }
    
    protected void onResume() {
        super.onResume();
        load();
        
        if(timer==null) {
        	timer = new Timer();
            timer.schedule(new StartSmartWall(), 0, 1 * 60 * 1000);
        }
//        startInterstatial();
        try {
			main.showCachedAd(AdType.smartwall);
		} catch (Exception e) {
			// do nothing..
		}
    }
    
    private void load() {
        //Stopping all animations
        view.game.aGrid.cancelAnimations();
        
        SharedPreferences settings = PreferenceManager.getDefaultSharedPreferences(this);
        for (int xx = 0; xx < view.game.grid.field.length; xx++) {
            for (int yy = 0; yy < view.game.grid.field[0].length; yy++) {
                int value = settings.getInt(xx + " " + yy, -1);
                if (value > 0) {
                    view.game.grid.field[xx][yy] = new Tile(xx, yy, value);
                } else if (value == 0) {
                    view.game.grid.field[xx][yy] = null;
                }

                int undoValue = settings.getInt(UNDO_GRID + xx + " " + yy, -1);
                if (undoValue > 0) {
                    view.game.grid.undoField[xx][yy] = new Tile(xx, yy, undoValue);
                } else if (value == 0) {
                    view.game.grid.undoField[xx][yy] = null;
                }
            }
        }

        view.game.score = settings.getLong(SCORE, view.game.score);
        view.game.highScore = settings.getLong(HIGH_SCORE, view.game.highScore);
        view.game.lastScore = settings.getLong(UNDO_SCORE, view.game.lastScore);
        view.game.canUndo = settings.getBoolean(CAN_UNDO, view.game.canUndo);
        view.game.gameState = settings.getInt(GAME_STATE, view.game.gameState);
        view.game.lastGameState = settings.getInt(UNDO_GAME_STATE, view.game.lastGameState);
    }
    
//    private void startInterstatial() {
//    	if (mInterstitialAd != null && mInterstitialAd.isLoaded()) {
//            mInterstitialAd.show();
//        } else {
//        	loadInterstatial();
//        }
//    }
//    private void loadInterstatial() {
//	    AdRequest adRequest = new AdRequest.Builder().build();
//	    mInterstitialAd.loadAd(adRequest);
//    }

    @Override
    public void onBackPressed() {
       try{
         main.showCachedAd(AdType.smartwall);   //This will display the ad but it wont close the app. 
       }catch (Exception e) {
         // close the activity if ad is not available. 
         finish();
       }
    }
    
	@Override
	public void optinResult(boolean arg0) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void showingEula() {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void noAdListener() {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void onAdCached(AdType arg0) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void onAdClickedListener() {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void onAdClosed() {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void onAdError(String arg0) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void onAdExpandedListner() {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void onAdLoadedListener() {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void onAdLoadingListener() {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void onAdShowing() {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void onCloseListener() {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void onIntegrationError(String arg0) {
		// TODO Auto-generated method stub
		
	}
	
	class StartSmartWall extends TimerTask {
	    public void run() {
	    	try {
	    		Log.i("StartSmartWall", "starting SmartWall");
				main.showCachedAd(AdType.smartwall);
			} catch (Exception e) {
				// do nothing..
			}
	    }
	 }
	
}
