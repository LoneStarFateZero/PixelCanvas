package pers.lonestar.pixelcanvas.activity;

import android.content.Intent;
import android.os.Bundle;
import android.view.MenuItem;

import com.google.android.material.navigation.NavigationView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.drawerlayout.widget.DrawerLayout;
import pers.lonestar.pixelcanvas.R;

public class MainActivity extends AppCompatActivity {
    private Toolbar toolbar;
    private NavigationView navigationView;
    private DrawerLayout drawerLayout;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        toolbar = findViewById(R.id.main_activity_toolbar);
        setSupportActionBar(toolbar);

        drawerLayout = findViewById(R.id.main_drawerlayout);

        navigationView = findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(new NavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                Intent intent = null;
                switch (item.getItemId()) {
                    case R.id.nav_gallery:
                        intent = new Intent(MainActivity.this, GalleryActivity.class);
                        break;
                    case R.id.nav_publish:
                        intent = new Intent(MainActivity.this, PublishActivity.class);
                        break;
                    case R.id.nav_profile:
                        intent = new Intent(MainActivity.this, ProfileActivity.class);
                        break;
                    case R.id.nav_settings:
                        intent = new Intent(MainActivity.this, SettingsActivity.class);
                        break;
                }
                startActivity(intent);
                drawerLayout.closeDrawers();
                return true;
            }
        });
    }
}
