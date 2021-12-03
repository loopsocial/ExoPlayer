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

import androidx.annotation.Nullable;

/** A media transformation configuration. */
/* package */ final class Transformation {

  public final boolean removeAudio;
  public final boolean removeVideo;
  public final boolean flattenForSlowMotion;
  public final int outputHeight;
  public final String containerMimeType;
  @Nullable public final String audioMimeType;
  @Nullable public final String videoMimeType;

  public Transformation(
      boolean removeAudio,
      boolean removeVideo,
      boolean flattenForSlowMotion,
      int outputHeight,
      String containerMimeType,
      @Nullable String audioMimeType,
      @Nullable String videoMimeType) {
    this.removeAudio = removeAudio;
    this.removeVideo = removeVideo;
    this.flattenForSlowMotion = flattenForSlowMotion;
    this.outputHeight = outputHeight;
    this.containerMimeType = containerMimeType;
    this.audioMimeType = audioMimeType;
    this.videoMimeType = videoMimeType;
  }
}
