package pers.lonestar.pixelcanvas.activity;

import android.content.Intent;

import com.daimajia.androidanimations.library.Techniques;
import com.viksaa.sssplash.lib.activity.AwesomeSplash;
import com.viksaa.sssplash.lib.cnst.Flags;
import com.viksaa.sssplash.lib.model.ConfigSplash;

import pers.lonestar.pixelcanvas.R;

public class SplashActivity extends AwesomeSplash {

    @Override
    public void initSplash(ConfigSplash configSplash) {
        /* you don't have to override every property */

        //Customize Circular Reveal
        configSplash.setBackgroundColor(R.color.backgroundColor); //any color you want form colors.xml
        configSplash.setAnimCircularRevealDuration(700); //int ms
        configSplash.setRevealFlagX(Flags.REVEAL_RIGHT);  //or Flags.REVEAL_LEFT
        configSplash.setRevealFlagY(Flags.REVEAL_BOTTOM); //or Flags.REVEAL_TOP

        //Choose LOGO OR PATH; if you don't provide String value for path it's logo by default

        //Customize Logo
        configSplash.setLogoSplash(R.drawable.ic_launcher); //or any other drawable
        configSplash.setAnimLogoSplashDuration(700); //int ms
        configSplash.setAnimLogoSplashTechnique(Techniques.FadeInUp); //choose one form Techniques (ref: https://github.com/daimajia/AndroidViewAnimations)

        //Customize Title
        configSplash.setTitleSplash("像素画板");
        configSplash.setTitleTextColor(R.color.colorFont);
        configSplash.setTitleTextSize(30f); //float value
        configSplash.setAnimTitleDuration(700);
        configSplash.setAnimTitleTechnique(Techniques.FadeIn);
        configSplash.setTitleFont("fonts/zpix.ttf"); //provide string to your font located in assets/fonts/
    }

    @Override
    public void animationsFinished() {
        Intent intent = new Intent(this, LoginActivity.class);
        startActivity(intent);
        finish();
    }
}
