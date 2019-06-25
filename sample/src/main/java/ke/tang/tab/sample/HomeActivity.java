package ke.tang.tab.sample;

import android.os.Bundle;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import ke.tang.tab.Tab;
import ke.tang.tab.TabLayout;

public class HomeActivity extends AppCompatActivity {
    private TabLayout mTab;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);
    }

    @Override
    protected void onPostCreate(@Nullable Bundle savedInstanceState) {
        super.onPostCreate(savedInstanceState);
        mTab = findViewById(R.id.tab);
        final Tab tab1 = mTab.newTab();
        tab1.setText("唐珂1");
        mTab.addTab(tab1);
        final Tab tab2 = mTab.newTab();
        tab2.setText("唐珂2");
        mTab.addTab(tab2);
    }
}
