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

package doclava;

import com.google.doclava.ErrorCode;
import com.google.doclava.ErrorMessage;
import com.google.doclava.ErrorReport;
import com.google.doclava.Errors;
import com.google.doclava.apicheck.ApiCheck;

import junit.framework.TestCase;

import java.util.Iterator;

public class ApiCheckTest extends TestCase {
  /**
   * Clear all errors and make sure all future errors will be recorded.
   */
  public void setUp() {
    Errors.clearErrors();
    for (ErrorCode error : Errors.ERRORS) {
      Errors.setErrorLevel(error.getCode(), Errors.ERROR);
    }
  }
  
  public void testEquivalentApi() {
    String[] args = { "test/api/medium.xml", "test/api/medium.xml" };
    ApiCheck apiCheck = new ApiCheck();
    ErrorReport report = apiCheck.checkApi(args);
    assertEquals(report.getErrors().size(), 0);
  }
  
  public void testMethodReturnTypeChanged() {
    String[] args = { "test/api/return-type-changed-1.xml", "test/api/return-type-changed-2.xml" };
    ApiCheck apiCheck = new ApiCheck();
    ErrorReport report = apiCheck.checkApi(args);
    assertEquals(1, report.getErrors().size());
    assertEquals(Errors.CHANGED_TYPE, report.getErrors().iterator().next().getError());
  }
  
  public void testMethodParameterChanged() {
    String[] args = { "test/api/parameter-changed-1.xml", "test/api/parameter-changed-2.xml" };
    ApiCheck apiCheck = new ApiCheck();
    ErrorReport report = apiCheck.checkApi(args);
    assertEquals(2, report.getErrors().size());
    
    Iterator<ErrorMessage> errors = report.getErrors().iterator();
    ErrorMessage m1 = errors.next();
    ErrorMessage m2 = errors.next();
    assertNotSame(m1.getError(), m2.getError());
    assertTrue(m1.getError().equals(Errors.ADDED_METHOD) || m1.getError().equals(Errors.REMOVED_METHOD));
    assertTrue(m2.getError().equals(Errors.ADDED_METHOD) || m2.getError().equals(Errors.REMOVED_METHOD));
  }
  
  public void testConstructorParameterChanged() {
    String[] args = { "test/api/parameter-changed-1.xml", "test/api/parameter-changed-3.xml" };
    ApiCheck apiCheck = new ApiCheck();
    ErrorReport report = apiCheck.checkApi(args);
    assertEquals(2, report.getErrors().size());
    Iterator<ErrorMessage> errors = report.getErrors().iterator();
    ErrorMessage m1 = errors.next();
    ErrorMessage m2 = errors.next();
    assertNotSame(m1.getError(), m2.getError());
    assertTrue(m1.getError().equals(Errors.ADDED_METHOD) || m1.getError().equals(Errors.REMOVED_METHOD));
    assertTrue(m2.getError().equals(Errors.ADDED_METHOD) || m2.getError().equals(Errors.REMOVED_METHOD));
  }
  
  public void testAddedMethod() {
    String[] args = { "test/api/simple.xml", "test/api/added-method.xml" };
    ApiCheck apiCheck = new ApiCheck();
    ErrorReport report = apiCheck.checkApi(args);
    assertEquals(1, report.getErrors().size());
    assertEquals(Errors.ADDED_METHOD, report.getErrors().iterator().next().getError());
  }
  
  public void testRemovedMethod() {
    String[] args = { "test/api/added-method.xml", "test/api/simple.xml" };
    ApiCheck apiCheck = new ApiCheck();
    ErrorReport report = apiCheck.checkApi(args);
    assertEquals(1, report.getErrors().size());
    assertEquals(Errors.REMOVED_METHOD, report.getErrors().iterator().next().getError());
  }
  
  public void testAddedConstructor() {
    String[] args = { "test/api/simple.xml", "test/api/added-constructor.xml" };
    ApiCheck apiCheck = new ApiCheck();
    ErrorReport report = apiCheck.checkApi(args);
    assertEquals(1, report.getErrors().size());
    assertEquals(Errors.ADDED_METHOD, report.getErrors().iterator().next().getError());
  }
  
  public void testRemovedConstructor() {
    String[] args = { "test/api/added-constructor.xml", "test/api/simple.xml" };
    ApiCheck apiCheck = new ApiCheck();
    ErrorReport report = apiCheck.checkApi(args);
    assertEquals(1, report.getErrors().size());
    assertEquals(Errors.REMOVED_METHOD, report.getErrors().iterator().next().getError());
  }
  
  public void testAddedClass() {
    String[] args = { "test/api/simple.xml", "test/api/add-class.xml" };
    ApiCheck apiCheck = new ApiCheck();
    ErrorReport report = apiCheck.checkApi(args);
    assertEquals(1, report.getErrors().size());
    assertEquals(Errors.ADDED_CLASS, report.getErrors().iterator().next().getError());
  }
  
  public void testRemovedClass() {
    String[] args = { "test/api/add-class.xml", "test/api/simple.xml" };
    ApiCheck apiCheck = new ApiCheck();
    ErrorReport report = apiCheck.checkApi(args);
    assertEquals(1, report.getErrors().size());
    assertEquals(Errors.REMOVED_CLASS, report.getErrors().iterator().next().getError());
  }
  
  public void testChangedSuper() {
    String[] args = { "test/api/simple.xml", "test/api/changed-super.xml" };
    ApiCheck apiCheck = new ApiCheck();
    ErrorReport report = apiCheck.checkApi(args);
    assertEquals(1, report.getErrors().size());
    assertEquals(Errors.CHANGED_SUPERCLASS, report.getErrors().iterator().next().getError());
  }
  
  public void testAddedInterface() {
    String[] args = { "test/api/removed-interface.xml", "test/api/medium.xml" };
    ApiCheck apiCheck = new ApiCheck();
    ErrorReport report = apiCheck.checkApi(args);
    assertEquals(1, report.getErrors().size());
    assertEquals(Errors.ADDED_INTERFACE, report.getErrors().iterator().next().getError());
  }
  
  public void testRemovedInterface() {
    String[] args = { "test/api/medium.xml", "test/api/removed-interface.xml" };
    ApiCheck apiCheck = new ApiCheck();
    ErrorReport report = apiCheck.checkApi(args);
    assertEquals(1, report.getErrors().size());
    assertEquals(Errors.REMOVED_INTERFACE, report.getErrors().iterator().next().getError());
  }
  
  public void testChangedAbstractClass() {
    String[] args = { "test/api/medium.xml", "test/api/changed-abstract.xml" };
    ApiCheck apiCheck = new ApiCheck();
    ErrorReport report = apiCheck.checkApi(args);
    assertEquals(1, report.getErrors().size());
    assertEquals(Errors.CHANGED_ABSTRACT, report.getErrors().iterator().next().getError());
  }
  
  public void testChangedAbstractClass2() {
    String[] args = { "test/api/changed-abstract.xml", "test/api/medium.xml" };
    ApiCheck apiCheck = new ApiCheck();
    ErrorReport report = apiCheck.checkApi(args);
    assertEquals(1, report.getErrors().size());
    assertEquals(Errors.CHANGED_ABSTRACT, report.getErrors().iterator().next().getError());
  }
  
  public void testChangedAbstractMethod() {
    String[] args = { "test/api/medium.xml", "test/api/changed-abstract2.xml" };
    ApiCheck apiCheck = new ApiCheck();
    ErrorReport report = apiCheck.checkApi(args);
    assertEquals(1, report.getErrors().size());
    assertEquals(Errors.CHANGED_ABSTRACT, report.getErrors().iterator().next().getError());
  }
  
  public void testChangedAbstractMethod2() {
    String[] args = { "test/api/changed-abstract2.xml", "test/api/medium.xml" };
    ApiCheck apiCheck = new ApiCheck();
    ErrorReport report = apiCheck.checkApi(args);
    assertEquals(1, report.getErrors().size());
    assertEquals(Errors.CHANGED_ABSTRACT, report.getErrors().iterator().next().getError());
  }
  
  public void testAddedPackage() {
    String[] args = { "test/api/medium.xml", "test/api/added-package.xml" };
    ApiCheck apiCheck = new ApiCheck();
    ErrorReport report = apiCheck.checkApi(args);
    assertEquals(1, report.getErrors().size());
    assertEquals(Errors.ADDED_PACKAGE, report.getErrors().iterator().next().getError());
  }
  
  public void testRemovedPackage() {
    String[] args = { "test/api/added-package.xml", "test/api/medium.xml" };
    ApiCheck apiCheck = new ApiCheck();
    ErrorReport report = apiCheck.checkApi(args);
    assertEquals(1, report.getErrors().size());
    assertEquals(Errors.REMOVED_PACKAGE, report.getErrors().iterator().next().getError());
  }
    
  public void testChangedValue() {
    String[] args = { "test/api/constants.xml", "test/api/changed-value.xml" };
    ApiCheck apiCheck = new ApiCheck();
    ErrorReport report = apiCheck.checkApi(args);
    assertEquals(1, report.getErrors().size());
    assertEquals(Errors.CHANGED_VALUE, report.getErrors().iterator().next().getError());
  }
  
  public void testChangedValue2() {
    String[] args = { "test/api/constants.xml", "test/api/changed-value2.xml" };
    ApiCheck apiCheck = new ApiCheck();
    ErrorReport report = apiCheck.checkApi(args);
    assertEquals(1, report.getErrors().size());
    assertEquals(Errors.CHANGED_VALUE, report.getErrors().iterator().next().getError());
  }
  
  public void testChangedType() {
    String[] args = { "test/api/constants.xml", "test/api/changed-type.xml" };
    ApiCheck apiCheck = new ApiCheck();
    ErrorReport report = apiCheck.checkApi(args);
    assertEquals(1, report.getErrors().size());
    assertEquals(Errors.CHANGED_TYPE, report.getErrors().iterator().next().getError());
  }
  
  public void testChangedFinalField() {
    String[] args = { "test/api/constants.xml", "test/api/changed-final.xml" };
    ApiCheck apiCheck = new ApiCheck();
    ErrorReport report = apiCheck.checkApi(args);
    assertEquals(1, report.getErrors().size());
    assertEquals(Errors.CHANGED_FINAL, report.getErrors().iterator().next().getError());
  }
  
  public void testChangedFinalMethod() {
    String[] args = { "test/api/constants.xml", "test/api/changed-final2.xml" };
    ApiCheck apiCheck = new ApiCheck();
    ErrorReport report = apiCheck.checkApi(args);
    assertEquals(1, report.getErrors().size());
    assertEquals(Errors.CHANGED_FINAL, report.getErrors().iterator().next().getError());
  }
  
  public void testChangedFinalClass() {
    String[] args = { "test/api/constants.xml", "test/api/changed-final3.xml" };
    ApiCheck apiCheck = new ApiCheck();
    ErrorReport report = apiCheck.checkApi(args);
    assertEquals(1, report.getErrors().size());
    assertEquals(Errors.CHANGED_FINAL, report.getErrors().iterator().next().getError());
  }
  
  public void testChangedFinalClass2() {
    String[] args = { "test/api/changed-final3.xml", "test/api/constants.xml" };
    ApiCheck apiCheck = new ApiCheck();
    ErrorReport report = apiCheck.checkApi(args);
    assertEquals(1, report.getErrors().size());
    assertEquals(Errors.CHANGED_FINAL, report.getErrors().iterator().next().getError());
  }
  
  public void testAddedField() {
    String[] args = { "test/api/constants.xml", "test/api/added-field.xml" };
    ApiCheck apiCheck = new ApiCheck();
    ErrorReport report = apiCheck.checkApi(args);
    assertEquals(1, report.getErrors().size());
    assertEquals(Errors.ADDED_FIELD, report.getErrors().iterator().next().getError());
  }
  
  public void testRemovedField() {
    String[] args = { "test/api/added-field.xml", "test/api/constants.xml" };
    ApiCheck apiCheck = new ApiCheck();
    ErrorReport report = apiCheck.checkApi(args);
    assertEquals(1, report.getErrors().size());
    assertEquals(Errors.REMOVED_FIELD, report.getErrors().iterator().next().getError());
  }
  
  public void testChangedStaticMethod() {
    String[] args = { "test/api/constants.xml", "test/api/changed-static.xml" };
    ApiCheck apiCheck = new ApiCheck();
    ErrorReport report = apiCheck.checkApi(args);
    assertEquals(1, report.getErrors().size());
    assertEquals(Errors.CHANGED_STATIC, report.getErrors().iterator().next().getError());
  }
  
  public void testChangedStaticClass() {
    String[] args = { "test/api/constants.xml", "test/api/changed-static2.xml" };
    ApiCheck apiCheck = new ApiCheck();
    ErrorReport report = apiCheck.checkApi(args);
    assertEquals(1, report.getErrors().size());
    assertEquals(Errors.CHANGED_STATIC, report.getErrors().iterator().next().getError());
  }
  
  public void testChangedStaticField() {
    String[] args = { "test/api/constants.xml", "test/api/changed-static3.xml" };
    ApiCheck apiCheck = new ApiCheck();
    ErrorReport report = apiCheck.checkApi(args);
    assertEquals(1, report.getErrors().size());
    assertEquals(Errors.CHANGED_STATIC, report.getErrors().iterator().next().getError());
  }
  
  public void testChangedTransient() {
    String[] args = { "test/api/constants.xml", "test/api/changed-transient.xml" };
    ApiCheck apiCheck = new ApiCheck();
    ErrorReport report = apiCheck.checkApi(args);
    assertEquals(1, report.getErrors().size());
    assertEquals(Errors.CHANGED_TRANSIENT, report.getErrors().iterator().next().getError());
  }
  
  /*public void testChangedSynchronized() {
    String[] args = { "test/api/constants.xml", "test/api/changed-synchronized.xml" };
    ApiCheck apiCheck = new ApiCheck();
    ErrorReport report = apiCheck.checkApi(args);
    assertEquals(1, report.getErrors().size());
    assertEquals(Errors.CHANGED_SYNCHRONIZED, report.getErrors().iterator().next().getError());
  }*/

  public void testChangedVolatile() {
    String[] args = { "test/api/constants.xml", "test/api/changed-volatile.xml" };
    ApiCheck apiCheck = new ApiCheck();
    ErrorReport report = apiCheck.checkApi(args);
    assertEquals(1, report.getErrors().size());
    assertEquals(Errors.CHANGED_VOLATILE, report.getErrors().iterator().next().getError());
  }
  
  public void testChangedNative() {
    String[] args = { "test/api/constants.xml", "test/api/changed-native.xml" };
    ApiCheck apiCheck = new ApiCheck();
    ErrorReport report = apiCheck.checkApi(args);
    assertEquals(1, report.getErrors().size());
    assertEquals(Errors.CHANGED_NATIVE, report.getErrors().iterator().next().getError());
  }
  
  public void testChangedScopeMethod() {
    String[] args = { "test/api/constants.xml", "test/api/changed-scope.xml" };
    ApiCheck apiCheck = new ApiCheck();
    ErrorReport report = apiCheck.checkApi(args);
    assertEquals(1, report.getErrors().size());
    assertEquals(Errors.CHANGED_SCOPE, report.getErrors().iterator().next().getError());
  }

  public void testChangedScopeClass() {
    String[] args = { "test/api/changed-scope.xml", "test/api/constants.xml" };
    ApiCheck apiCheck = new ApiCheck();
    ErrorReport report = apiCheck.checkApi(args);
    assertEquals(1, report.getErrors().size());
    assertEquals(Errors.CHANGED_SCOPE, report.getErrors().iterator().next().getError());
  }
  
  public void testChangedScopeClass2() {
    String[] args = { "test/api/constants.xml", "test/api/changed-scope2.xml" };
    ApiCheck apiCheck = new ApiCheck();
    ErrorReport report = apiCheck.checkApi(args);
    assertEquals(1, report.getErrors().size());
    assertEquals(Errors.CHANGED_SCOPE, report.getErrors().iterator().next().getError());
  }
  
  public void testChangedScopeField() {
    String[] args = { "test/api/constants.xml", "test/api/changed-scope3.xml" };
    ApiCheck apiCheck = new ApiCheck();
    ErrorReport report = apiCheck.checkApi(args);
    assertEquals(1, report.getErrors().size());
    assertEquals(Errors.CHANGED_SCOPE, report.getErrors().iterator().next().getError());
  }
  
  public void testChangedConstructorScope() {
    String[] args = { "test/api/constants.xml", "test/api/changed-scope4.xml" };
    ApiCheck apiCheck = new ApiCheck();
    ErrorReport report = apiCheck.checkApi(args);
    assertEquals(1, report.getErrors().size());
    assertEquals(Errors.CHANGED_SCOPE, report.getErrors().iterator().next().getError());
  }
  
  public void testChangedMethodThrows() {
    String[] args = { "test/api/throws.xml", "test/api/removed-exception.xml" };
    ApiCheck apiCheck = new ApiCheck();
    ErrorReport report = apiCheck.checkApi(args);
    assertEquals(1, report.getErrors().size());
    assertEquals(Errors.CHANGED_THROWS, report.getErrors().iterator().next().getError());
  }
  
  public void testChangedMethodThrows2() {
    String[] args = { "test/api/removed-exception.xml", "test/api/throws.xml" };
    ApiCheck apiCheck = new ApiCheck();
    ErrorReport report = apiCheck.checkApi(args);
    assertEquals(1, report.getErrors().size());
    assertEquals(Errors.CHANGED_THROWS, report.getErrors().iterator().next().getError());
  }
  
  public void testChangedConstructorThrows() {
    String[] args = { "test/api/throws.xml", "test/api/added-exception.xml" };
    ApiCheck apiCheck = new ApiCheck();
    ErrorReport report = apiCheck.checkApi(args);
    assertEquals(1, report.getErrors().size());
    assertEquals(Errors.CHANGED_THROWS, report.getErrors().iterator().next().getError());
  }
  
  public void testChangedConstructorThrows2() {
    String[] args = { "test/api/added-exception.xml", "test/api/throws.xml" };
    ApiCheck apiCheck = new ApiCheck();
    ErrorReport report = apiCheck.checkApi(args);
    assertEquals(1, report.getErrors().size());
    assertEquals(Errors.CHANGED_THROWS, report.getErrors().iterator().next().getError());
  }
  
  public void testChangedMethodDeprecated() {
    String[] args = { "test/api/constants.xml", "test/api/changed-deprecated.xml" };
    ApiCheck apiCheck = new ApiCheck();
    ErrorReport report = apiCheck.checkApi(args);
    assertEquals(1, report.getErrors().size());
    assertEquals(Errors.CHANGED_DEPRECATED, report.getErrors().iterator().next().getError());
  }
  
  public void testChangedConstructorDeprecated() {
    String[] args = { "test/api/constants.xml", "test/api/changed-deprecated2.xml" };
    ApiCheck apiCheck = new ApiCheck();
    ErrorReport report = apiCheck.checkApi(args);
    assertEquals(1, report.getErrors().size());
    assertEquals(Errors.CHANGED_DEPRECATED, report.getErrors().iterator().next().getError());
  }
  
  public void testChangedFieldDeprecated() {
    String[] args = { "test/api/constants.xml", "test/api/changed-deprecated3.xml" };
    ApiCheck apiCheck = new ApiCheck();
    ErrorReport report = apiCheck.checkApi(args);
    assertEquals(1, report.getErrors().size());
    assertEquals(Errors.CHANGED_DEPRECATED, report.getErrors().iterator().next().getError());
  }
  
  public void testChangedClassToInterface() {
    String[] args = { "test/api/changed-class-info2.xml", "test/api/changed-class-info.xml" };
    ApiCheck apiCheck = new ApiCheck();
    ErrorReport report = apiCheck.checkApi(args);
    assertEquals(1, report.getErrors().size());
    assertEquals(Errors.CHANGED_CLASS, report.getErrors().iterator().next().getError());
  }
  
  public void testChangedInterfaceToClass() {
    String[] args = { "test/api/changed-class-info.xml", "test/api/changed-class-info2.xml" };
    ApiCheck apiCheck = new ApiCheck();
    ErrorReport report = apiCheck.checkApi(args);
    assertEquals(1, report.getErrors().size());
    assertEquals(Errors.CHANGED_CLASS, report.getErrors().iterator().next().getError());
  }
}
