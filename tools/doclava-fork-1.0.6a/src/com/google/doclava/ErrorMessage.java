/*
 * Copyright (C) 2010 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.google.doclava;

import com.google.doclava.ErrorCode;

public final class ErrorMessage implements Comparable<ErrorMessage> {
    private ErrorCode error;
    private SourcePositionInfo pos;
    private String msg;

    public ErrorMessage(ErrorCode error, SourcePositionInfo position, String msg) {
      this.error = error;
      this.pos = position;
      this.msg = msg;
    }

    public int compareTo(ErrorMessage other) {
      int r = this.pos.compareTo(other.pos);
      if (r != 0) {
        return r;
      }
      return this.msg.compareTo(other.msg);
    }

    @Override
    public String toString() {
      String whereText = this.pos == null ? "unknown: " : this.pos.toString() + ':';
      return whereText + this.msg;
    }
    
    public ErrorCode getError() {
      return error;
    }
  }