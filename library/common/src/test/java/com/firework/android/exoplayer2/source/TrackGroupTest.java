/*
 * Copyright (C) 2018 The Android Open Source Project
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

import static com.google.common.truth.Truth.assertThat;

import androidx.test.ext.junit.runners.AndroidJUnit4;
import com.firework.android.exoplayer2.Format;
import com.firework.android.exoplayer2.util.MimeTypes;
import org.junit.Test;
import org.junit.runner.RunWith;

/** Unit test for {@link TrackGroup}. */
@RunWith(AndroidJUnit4.class)
public final class TrackGroupTest {

  @Test
  public void roundTripViaBundle_ofTrackGroup_yieldsEqualInstance() {
    Format.Builder formatBuilder = new Format.Builder();
    Format format1 = formatBuilder.setSampleMimeType(MimeTypes.VIDEO_H264).build();
    Format format2 = formatBuilder.setSampleMimeType(MimeTypes.AUDIO_AAC).build();

    TrackGroup trackGroupToBundle = new TrackGroup(format1, format2);

    TrackGroup trackGroupFromBundle = TrackGroup.CREATOR.fromBundle(trackGroupToBundle.toBundle());

    assertThat(trackGroupFromBundle).isEqualTo(trackGroupToBundle);
  }
}
