/*
 * Copyright (C) 2020 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.firework.android.exoplayer2.source;

import static com.firework.android.exoplayer2.robolectric.RobolectricUtil.runMainLooperUntil;
import static com.google.common.truth.Truth.assertThat;

import android.net.Uri;
import androidx.test.core.app.ApplicationProvider;
import androidx.test.ext.junit.runners.AndroidJUnit4;
import com.firework.android.exoplayer2.source.BundledExtractorsAdapter;
import com.firework.android.exoplayer2.source.MediaParserExtractorAdapter;
import com.firework.android.exoplayer2.source.MediaPeriod;
import com.firework.android.exoplayer2.source.MediaSourceEventListener;
import com.firework.android.exoplayer2.source.ProgressiveMediaExtractor;
import com.firework.android.exoplayer2.source.ProgressiveMediaPeriod;
import com.firework.android.exoplayer2.source.ProgressiveMediaSource;
import com.firework.android.exoplayer2.C;
import com.firework.android.exoplayer2.analytics.PlayerId;
import com.firework.android.exoplayer2.drm.DrmSessionEventListener;
import com.firework.android.exoplayer2.drm.DrmSessionManager;
import com.firework.android.exoplayer2.extractor.mp4.Mp4Extractor;
import com.firework.android.exoplayer2.source.MediaSource.MediaPeriodId;
import com.firework.android.exoplayer2.upstream.AssetDataSource;
import com.firework.android.exoplayer2.upstream.DefaultAllocator;
import com.firework.android.exoplayer2.upstream.DefaultLoadErrorHandlingPolicy;
import java.util.concurrent.TimeoutException;
import java.util.concurrent.atomic.AtomicBoolean;
import org.junit.Test;
import org.junit.runner.RunWith;

/** Unit test for {@link ProgressiveMediaPeriod}. */
@RunWith(AndroidJUnit4.class)
public final class ProgressiveMediaPeriodTest {

  @Test
  public void prepareUsingBundledExtractors_updatesSourceInfoBeforeOnPreparedCallback()
      throws TimeoutException {
    testExtractorsUpdatesSourceInfoBeforeOnPreparedCallback(
        new BundledExtractorsAdapter(Mp4Extractor.FACTORY));
  }

  @Test
  public void prepareUsingMediaParser_updatesSourceInfoBeforeOnPreparedCallback()
      throws TimeoutException {
    testExtractorsUpdatesSourceInfoBeforeOnPreparedCallback(
        new MediaParserExtractorAdapter(PlayerId.UNSET));
  }

  private static void testExtractorsUpdatesSourceInfoBeforeOnPreparedCallback(
      ProgressiveMediaExtractor extractor) throws TimeoutException {
    AtomicBoolean sourceInfoRefreshCalled = new AtomicBoolean(false);
    ProgressiveMediaPeriod.Listener sourceInfoRefreshListener =
        (durationUs, isSeekable, isLive) -> sourceInfoRefreshCalled.set(true);
    MediaPeriodId mediaPeriodId = new MediaPeriodId(/* periodUid= */ new Object());
    ProgressiveMediaPeriod mediaPeriod =
        new ProgressiveMediaPeriod(
            Uri.parse("asset://android_asset/media/mp4/sample.mp4"),
            new AssetDataSource(ApplicationProvider.getApplicationContext()),
            extractor,
            DrmSessionManager.DRM_UNSUPPORTED,
            new DrmSessionEventListener.EventDispatcher()
                .withParameters(/* windowIndex= */ 0, mediaPeriodId),
            new DefaultLoadErrorHandlingPolicy(),
            new MediaSourceEventListener.EventDispatcher()
                .withParameters(/* windowIndex= */ 0, mediaPeriodId, /* mediaTimeOffsetMs= */ 0),
            sourceInfoRefreshListener,
            new DefaultAllocator(/* trimOnReset= */ true, C.DEFAULT_BUFFER_SEGMENT_SIZE),
            /* customCacheKey= */ null,
            ProgressiveMediaSource.DEFAULT_LOADING_CHECK_INTERVAL_BYTES);

    AtomicBoolean prepareCallbackCalled = new AtomicBoolean(false);
    AtomicBoolean sourceInfoRefreshCalledBeforeOnPrepared = new AtomicBoolean(false);
    mediaPeriod.prepare(
        new MediaPeriod.Callback() {
          @Override
          public void onPrepared(MediaPeriod mediaPeriod) {
            sourceInfoRefreshCalledBeforeOnPrepared.set(sourceInfoRefreshCalled.get());
            prepareCallbackCalled.set(true);
          }

          @Override
          public void onContinueLoadingRequested(MediaPeriod source) {
            source.continueLoading(/* positionUs= */ 0);
          }
        },
        /* positionUs= */ 0);
    runMainLooperUntil(prepareCallbackCalled::get);
    mediaPeriod.release();

    assertThat(sourceInfoRefreshCalledBeforeOnPrepared.get()).isTrue();
  }
}
