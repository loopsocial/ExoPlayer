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
package com.firework.android.exoplayer2.audio;

import static com.google.common.truth.Truth.assertThat;

import androidx.test.ext.junit.runners.AndroidJUnit4;
import com.firework.android.exoplayer2.C;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.List;
import org.junit.Test;
import org.junit.runner.RunWith;

/** Unit tests for {@link OpusUtil}. */
@RunWith(AndroidJUnit4.class)
public final class OpusUtilTest {

  private static final byte[] HEADER =
      new byte[] {79, 112, 117, 115, 72, 101, 97, 100, 0, 2, 1, 56, 0, 0, -69, -128, 0, 0, 0};

  private static final int HEADER_PRE_SKIP_SAMPLES = 14337;
  private static final byte[] HEADER_PRE_SKIP_BYTES =
      buildNativeOrderByteArray(sampleCountToNanoseconds(HEADER_PRE_SKIP_SAMPLES));

  private static final int DEFAULT_SEEK_PRE_ROLL_SAMPLES = 3840;
  private static final byte[] DEFAULT_SEEK_PRE_ROLL_BYTES =
      buildNativeOrderByteArray(sampleCountToNanoseconds(DEFAULT_SEEK_PRE_ROLL_SAMPLES));

  @Test
  public void buildInitializationData() {
    List<byte[]> initializationData = OpusUtil.buildInitializationData(HEADER);
    assertThat(initializationData).hasSize(3);
    assertThat(initializationData.get(0)).isEqualTo(HEADER);
    assertThat(initializationData.get(1)).isEqualTo(HEADER_PRE_SKIP_BYTES);
    assertThat(initializationData.get(2)).isEqualTo(DEFAULT_SEEK_PRE_ROLL_BYTES);
  }

  @Test
  public void getChannelCount() {
    int channelCount = OpusUtil.getChannelCount(HEADER);
    assertThat(channelCount).isEqualTo(2);
  }

  private static long sampleCountToNanoseconds(long sampleCount) {
    return (sampleCount * C.NANOS_PER_SECOND) / OpusUtil.SAMPLE_RATE;
  }

  private static byte[] buildNativeOrderByteArray(long value) {
    return ByteBuffer.allocate(8).order(ByteOrder.nativeOrder()).putLong(value).array();
  }
}
