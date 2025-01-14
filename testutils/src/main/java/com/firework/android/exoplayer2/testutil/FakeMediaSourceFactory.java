/*
 * Copyright 2021 The Android Open Source Project
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
package com.firework.android.exoplayer2.testutil;

import androidx.annotation.Nullable;
import com.firework.android.exoplayer2.C;
import com.firework.android.exoplayer2.MediaItem;
import com.firework.android.exoplayer2.drm.DrmSessionManager;
import com.firework.android.exoplayer2.drm.DrmSessionManagerProvider;
import com.firework.android.exoplayer2.source.MediaSource;
import com.firework.android.exoplayer2.source.MediaSourceFactory;
import com.firework.android.exoplayer2.source.ads.AdPlaybackState;
import com.firework.android.exoplayer2.testutil.FakeTimeline.TimelineWindowDefinition;
import com.firework.android.exoplayer2.upstream.HttpDataSource;
import com.firework.android.exoplayer2.upstream.LoadErrorHandlingPolicy;
import com.firework.android.exoplayer2.util.Util;

/** Fake {@link MediaSourceFactory} that creates a {@link FakeMediaSource}. */
public class FakeMediaSourceFactory implements MediaSourceFactory {

  /** The window UID used by media sources that are created by the factory. */
  public static final Object DEFAULT_WINDOW_UID = new Object();

  @Override
  public MediaSourceFactory setDrmSessionManagerProvider(
      @Nullable DrmSessionManagerProvider drmSessionManagerProvider) {
    throw new UnsupportedOperationException();
  }

  @Deprecated
  @Override
  public MediaSourceFactory setDrmSessionManager(@Nullable DrmSessionManager drmSessionManager) {
    throw new UnsupportedOperationException();
  }

  @Deprecated
  @Override
  public MediaSourceFactory setDrmHttpDataSourceFactory(
      @Nullable HttpDataSource.Factory drmHttpDataSourceFactory) {
    throw new UnsupportedOperationException();
  }

  @Deprecated
  @Override
  public MediaSourceFactory setDrmUserAgent(@Nullable String userAgent) {
    throw new UnsupportedOperationException();
  }

  @Override
  public MediaSourceFactory setLoadErrorHandlingPolicy(
      @Nullable LoadErrorHandlingPolicy loadErrorHandlingPolicy) {
    throw new UnsupportedOperationException();
  }

  @Override
  public int[] getSupportedTypes() {
    return new int[] {C.TYPE_OTHER};
  }

  @Override
  public MediaSource createMediaSource(MediaItem mediaItem) {
    TimelineWindowDefinition timelineWindowDefinition =
        new TimelineWindowDefinition(
            /* periodCount= */ 1,
            /* id= */ DEFAULT_WINDOW_UID,
            /* isSeekable= */ true,
            /* isDynamic= */ false,
            /* isLive= */ false,
            /* isPlaceholder= */ false,
            /* durationUs= */ 1000 * C.MICROS_PER_SECOND,
            /* defaultPositionUs= */ 2 * C.MICROS_PER_SECOND,
            /* windowOffsetInFirstPeriodUs= */ Util.msToUs(123456789),
            AdPlaybackState.NONE,
            mediaItem);
    return new FakeMediaSource(new FakeTimeline(timelineWindowDefinition));
  }
}
