////////////////////////////////////////////////////////////////////////////////
//
//  Licensed to the Apache Software Foundation (ASF) under one or more
//  contributor license agreements.  See the NOTICE file distributed with
//  this work for additional information regarding copyright ownership.
//  The ASF licenses this file to You under the Apache License, Version 2.0
//  (the "License"); you may not use this file except in compliance with
//  the License.  You may obtain a copy of the License at
//
//      http://www.apache.org/licenses/LICENSE-2.0
//
//  Unless required by applicable law or agreed to in writing, software
//  distributed under the License is distributed on an "AS IS" BASIS,
//  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
//  See the License for the specific language governing permissions and
//  limitations under the License.
//
////////////////////////////////////////////////////////////////////////////////
package com.adobe.ac.pmd.rules.style;

import com.adobe.ac.pmd.rules.core.AbstractFlexRule;
import com.adobe.ac.pmd.rules.core.AbstractFlexRuleTest;
import com.adobe.ac.pmd.rules.core.ViolationPosition;

public class CopyrightMissingRuleTest extends AbstractFlexRuleTest
{
   @Override
   protected ExpectedViolation[] getExpectedViolatingFiles()
   {
      return new ExpectedViolation[]
      { new ExpectedViolation( "Simple.as", new ViolationPosition[]
       { new ViolationPosition( -1 ) } ),
                  new ExpectedViolation( "MainWithNoCopyright.mxml", new ViolationPosition[]
                  { new ViolationPosition( -1 ) } ) };
   }

   @Override
   protected AbstractFlexRule getRule()
   {
      return new CopyrightMissingRule();
   }
}
