package com.example.slidepuzzleproj;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ImageView;

public class Splash extends Activity {

    private final int SPLASH_DISPLAY_LENGTH = 3000;
    ImageView logo;
    Animation fromtop;

    @Override
    public void onCreate(Bundle icicle) {
        super.onCreate(icicle);
        setContentView(R.layout.splash);

        logo = (ImageView) findViewById(R.id.splashscreen);

        fromtop = AnimationUtils.loadAnimation(this,R.anim.fromtop);
        logo.setAnimation(fromtop);

        new Handler().postDelayed(new Runnable(){
            @Override
            public void run() {
                Intent mainIntent = new Intent(Splash.this,MenuActivity.class);
                Splash.this.startActivity(mainIntent);
                Splash.this.finish();
            }
        }, SPLASH_DISPLAY_LENGTH);
    }
}
