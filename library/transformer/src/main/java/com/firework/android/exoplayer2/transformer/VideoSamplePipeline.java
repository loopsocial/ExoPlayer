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

package com.firework.android.exoplayer2.transformer;

import static com.firework.android.exoplayer2.util.Assertions.checkNotNull;

import android.content.Context;
import android.media.MediaCodec;
import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import com.firework.android.exoplayer2.C;
import com.firework.android.exoplayer2.ExoPlaybackException;
import com.firework.android.exoplayer2.Format;
import com.firework.android.exoplayer2.PlaybackException;
import com.firework.android.exoplayer2.decoder.DecoderInputBuffer;
import com.google.common.collect.ImmutableMap;
import java.io.IOException;

/**
 * Pipeline to decode video samples, apply transformations on the raw samples, and re-encode them.
 */
@RequiresApi(18)
/* package */ final class VideoSamplePipeline implements SamplePipeline {

  private static final String TAG = "VideoSamplePipeline";

  private final DecoderInputBuffer decoderInputBuffer;
  private final MediaCodecAdapterWrapper decoder;

  private final FrameEditor frameEditor;

  private final MediaCodecAdapterWrapper encoder;
  private final DecoderInputBuffer encoderOutputBuffer;

  private boolean waitingForPopulatedDecoderSurface;

  public VideoSamplePipeline(
      Context context, Format inputFormat, Transformation transformation, int rendererIndex)
      throws ExoPlaybackException {
    decoderInputBuffer =
        new DecoderInputBuffer(DecoderInputBuffer.BUFFER_REPLACEMENT_MODE_DISABLED);
    encoderOutputBuffer =
        new DecoderInputBuffer(DecoderInputBuffer.BUFFER_REPLACEMENT_MODE_DISABLED);

    int outputWidth = inputFormat.width;
    int outputHeight = inputFormat.height;
    if (transformation.outputHeight != Format.NO_VALUE
        && transformation.outputHeight != inputFormat.height) {
      outputWidth = inputFormat.width * transformation.outputHeight / inputFormat.height;
      outputHeight = transformation.outputHeight;
    }

    try {
      encoder =
          MediaCodecAdapterWrapper.createForVideoEncoding(
              new Format.Builder()
                  .setWidth(outputWidth)
                  .setHeight(outputHeight)
                  .setSampleMimeType(
                      transformation.videoMimeType != null
                          ? transformation.videoMimeType
                          : inputFormat.sampleMimeType)
                  .build(),
              ImmutableMap.of());
    } catch (IOException e) {
      // TODO(internal b/192864511): Assign a specific error code.
      throw createRendererException(
          e, rendererIndex, inputFormat, PlaybackException.ERROR_CODE_UNSPECIFIED);
    }
    frameEditor =
        FrameEditor.create(
            context,
            outputWidth,
            outputHeight,
            /* outputSurface= */ checkNotNull(encoder.getInputSurface()));
    try {
      decoder =
          MediaCodecAdapterWrapper.createForVideoDecoding(
              inputFormat, frameEditor.getInputSurface());
    } catch (IOException e) {
      throw createRendererException(
          e, rendererIndex, inputFormat, PlaybackException.ERROR_CODE_DECODER_INIT_FAILED);
    }
  }

  @Override
  public boolean processData() {
    if (decoder.isEnded()) {
      return false;
    }

    if (!frameEditor.hasInputData()) {
      if (!waitingForPopulatedDecoderSurface) {
        if (decoder.getOutputBufferInfo() != null) {
          decoder.releaseOutputBuffer(/* render= */ true);
          waitingForPopulatedDecoderSurface = true;
        }
        if (decoder.isEnded()) {
          encoder.signalEndOfInputStream();
        }
      }
      return false;
    }

    waitingForPopulatedDecoderSurface = false;
    frameEditor.processData();
    return true;
  }

  @Override
  @Nullable
  public DecoderInputBuffer dequeueInputBuffer() {
    return decoder.maybeDequeueInputBuffer(decoderInputBuffer) ? decoderInputBuffer : null;
  }

  @Override
  public void queueInputBuffer() {
    decoder.queueInputBuffer(decoderInputBuffer);
  }

  @Override
  @Nullable
  public Format getOutputFormat() {
    return encoder.getOutputFormat();
  }

  @Override
  public boolean isEnded() {
    return encoder.isEnded();
  }

  @Override
  @Nullable
  public DecoderInputBuffer getOutputBuffer() {
    encoderOutputBuffer.data = encoder.getOutputBuffer();
    if (encoderOutputBuffer.data == null) {
      return null;
    }
    MediaCodec.BufferInfo bufferInfo = checkNotNull(encoder.getOutputBufferInfo());
    encoderOutputBuffer.timeUs = bufferInfo.presentationTimeUs;
    encoderOutputBuffer.setFlags(bufferInfo.flags);
    return encoderOutputBuffer;
  }

  @Override
  public void releaseOutputBuffer() {
    encoder.releaseOutputBuffer();
  }

  @Override
  public void release() {
    frameEditor.release();
    decoder.release();
    encoder.release();
  }

  private static ExoPlaybackException createRendererException(
      Throwable cause, int rendererIndex, Format inputFormat, int errorCode) {
    return ExoPlaybackException.createForRenderer(
        cause,
        TAG,
        rendererIndex,
        inputFormat,
        /* rendererFormatSupport= */ C.FORMAT_HANDLED,
        /* isRecoverable= */ false,
        errorCode);
  }
}
