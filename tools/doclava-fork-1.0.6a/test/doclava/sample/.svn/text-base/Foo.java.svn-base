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

package doclava.sample;

/**
 * A Foo class with documentation.
 *
 */
public class Foo {

  public static int get1() {
    return 1;
  }
  
  public final void nothing() {
    
  }
  
  interface Fuzz {
    public String getFuzzy();
  }
  
  abstract class Bar implements Fuzz {
     public abstract int getBarry();
  }
  
  class Baz extends Bar {
    /**
     * Baz implementation of getBarry, from {@link Fuzz}.
     */
    @Override
    public int getBarry() {
      // TODO Auto-generated method stub
      return 0;
    }
    
    /**
     * Baz implementation of {@link Fuzz#getFuzzy()}
     */
    public String getFuzzy() {
      return null;
    }
  }
}
