/*
 * Copyright 2020 The Android Open Source Project
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

package com.firework.android.exoplayer2.transformer;

import static com.firework.android.exoplayer2.source.SampleStream.FLAG_REQUIRE_FORMAT;
import static com.firework.android.exoplayer2.util.Assertions.checkNotNull;

import androidx.annotation.RequiresApi;
import com.firework.android.exoplayer2.C;
import com.firework.android.exoplayer2.ExoPlaybackException;
import com.firework.android.exoplayer2.Format;
import com.firework.android.exoplayer2.FormatHolder;
import com.firework.android.exoplayer2.decoder.DecoderInputBuffer;
import com.firework.android.exoplayer2.source.SampleStream.ReadDataResult;

@RequiresApi(18)
/* package */ final class TransformerAudioRenderer extends TransformerBaseRenderer {

  private static final String TAG = "TransformerAudioRenderer";

  private final DecoderInputBuffer decoderInputBuffer;

  public TransformerAudioRenderer(
      MuxerWrapper muxerWrapper, TransformerMediaClock mediaClock, Transformation transformation) {
    super(C.TRACK_TYPE_AUDIO, muxerWrapper, mediaClock, transformation);
    decoderInputBuffer =
        new DecoderInputBuffer(DecoderInputBuffer.BUFFER_REPLACEMENT_MODE_DISABLED);
  }

  @Override
  public String getName() {
    return TAG;
  }

  /** Attempts to read the input format and to initialize the {@link SamplePipeline}. */
  @Override
  protected boolean ensureConfigured() throws ExoPlaybackException {
    if (samplePipeline != null) {
      return true;
    }
    FormatHolder formatHolder = getFormatHolder();
    @ReadDataResult
    int result = readSource(formatHolder, decoderInputBuffer, /* readFlags= */ FLAG_REQUIRE_FORMAT);
    if (result != C.RESULT_FORMAT_READ) {
      return false;
    }
    Format inputFormat = checkNotNull(formatHolder.format);
    if ((transformation.audioMimeType != null
            && !transformation.audioMimeType.equals(inputFormat.sampleMimeType))
        || transformation.flattenForSlowMotion) {
      samplePipeline = new AudioSamplePipeline(inputFormat, transformation, getIndex());
    } else {
      samplePipeline = new PassthroughSamplePipeline(inputFormat);
    }
    return true;
  }
}
