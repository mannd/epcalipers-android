package org.epstudios.epcalipers;

import android.os.Bundle;

import com.cuneytayyildiz.onboarder.OnboarderActivity;
import com.cuneytayyildiz.onboarder.OnboarderPage;
import com.cuneytayyildiz.onboarder.utils.OnboarderPageChangeListener;

import java.util.Arrays;
import java.util.List;

/**
 * Copyright (C) 2019 EP Studios, Inc.
 * www.epstudiossoftware.com
 * <p>
 * Created by mannd on 2/26/19.
 * <p>
 * This file is part of epcalipers-android.
 * <p>
 * epcalipers-android is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * <p>
 * epcalipers-android is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * <p>
 * You should have received a copy of the GNU General Public License
 * along with epcalipers-android.  If not, see <http://www.gnu.org/licenses/>.
 */
public class Onboarder extends OnboarderActivity implements OnboarderPageChangeListener {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setSkipButtonTitle(getString(R.string.skip_button_title));
        setFinishButtonTitle(getString(R.string.finish_button_title));
        List<OnboarderPage> pages = Arrays.asList(
                new OnboarderPage.Builder()
                        .title(getString(R.string.move_caliper_label))
                        .backgroundColor(R.color.onboard_page1_color)
                        .imageResourceId(R.drawable.move_caliper)
                        .build(),
                new OnboarderPage.Builder()
                        .title(getString(R.string.single_tap_caliper_label))
                        .backgroundColor(R.color.onboard_page2_color)
                        .imageResourceId(R.drawable.single_tap_caliper)
                        .build(),
                new OnboarderPage.Builder()
                        .title(getString(R.string.double_tap_caliper_label))
                        .imageResourceId(R.drawable.double_tap_caliper)
                        .backgroundColor(R.color.onboard_page3_color)
                        .build(),
                new OnboarderPage.Builder()
                        .title(getString(R.string.zoom_ecg_label))
                        .backgroundColor(R.color.onboard_page4_color)
                        .imageResourceId(R.drawable.zoom_ecg)
                        .build(),
                new OnboarderPage.Builder()
                        .title(getString(R.string.longpress_label))
                        .backgroundColor(R.color.onboard_page5_color)
                        .imageResourceId(R.drawable.longpress)
                        .build()
        );
        setOnboarderPageChangeListener(this);
        initOnboardingPages(pages);
    }

    @Override
    public void onFinishButtonPressed() {
        EPSLog.log("Finish button pressed.");
        finish();
    }

    @Override
    public void onPageChanged(int position) {
        EPSLog.log("Page changed");
    }
}
