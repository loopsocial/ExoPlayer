/*
 * Copyright (C) 2017 The Android Open Source Project
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
package com.firework.android.exoplayer2.ext.rtmp;

import static com.firework.android.exoplayer2.util.Util.castNonNull;

import android.net.Uri;
import androidx.annotation.Nullable;
import com.firework.android.exoplayer2.C;
import com.firework.android.exoplayer2.ExoPlayerLibraryInfo;
import com.firework.android.exoplayer2.upstream.BaseDataSource;
import com.firework.android.exoplayer2.upstream.DataSource;
import com.firework.android.exoplayer2.upstream.DataSpec;
import com.firework.android.exoplayer2.upstream.TransferListener;
import io.antmedia.rtmp_client.RtmpClient;
import io.antmedia.rtmp_client.RtmpClient.RtmpIOException;
import java.io.IOException;

/** A Real-Time Messaging Protocol (RTMP) {@link DataSource}. */
public final class RtmpDataSource extends BaseDataSource {

  static {
    ExoPlayerLibraryInfo.registerModule("goog.exo.rtmp");
  }

  /** {@link DataSource.Factory} for {@link RtmpDataSource} instances. */
  public static final class Factory implements DataSource.Factory {

    @Nullable private TransferListener transferListener;

    /**
     * Sets the {@link TransferListener} that will be used.
     *
     * <p>The default is {@code null}.
     *
     * <p>See {@link DataSource#addTransferListener(TransferListener)}.
     *
     * @param transferListener The listener that will be used.
     * @return This factory.
     */
    public Factory setTransferListener(@Nullable TransferListener transferListener) {
      this.transferListener = transferListener;
      return this;
    }

    @Override
    public RtmpDataSource createDataSource() {
      RtmpDataSource dataSource = new RtmpDataSource();
      if (transferListener != null) {
        dataSource.addTransferListener(transferListener);
      }
      return dataSource;
    }
  }

  @Nullable private RtmpClient rtmpClient;
  @Nullable private Uri uri;

  public RtmpDataSource() {
    super(/* isNetwork= */ true);
  }

  @Override
  public long open(DataSpec dataSpec) throws RtmpIOException {
    transferInitializing(dataSpec);
    rtmpClient = new RtmpClient();
    rtmpClient.open(dataSpec.uri.toString(), false);

    this.uri = dataSpec.uri;
    transferStarted(dataSpec);
    return C.LENGTH_UNSET;
  }

  @Override
  public int read(byte[] buffer, int offset, int length) throws IOException {
    int bytesRead = castNonNull(rtmpClient).read(buffer, offset, length);
    if (bytesRead == -1) {
      return C.RESULT_END_OF_INPUT;
    }
    bytesTransferred(bytesRead);
    return bytesRead;
  }

  @Override
  public void close() {
    if (uri != null) {
      uri = null;
      transferEnded();
    }
    if (rtmpClient != null) {
      rtmpClient.close();
      rtmpClient = null;
    }
  }

  @Override
  @Nullable
  public Uri getUri() {
    return uri;
  }
}
