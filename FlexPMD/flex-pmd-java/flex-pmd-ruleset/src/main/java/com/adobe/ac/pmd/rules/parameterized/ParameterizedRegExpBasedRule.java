/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.adobe.ac.pmd.rules.parameterized;

import java.util.LinkedHashMap;
import java.util.Map;

import net.sourceforge.pmd.PropertyDescriptor;
import net.sourceforge.pmd.properties.StringProperty;

import com.adobe.ac.pmd.rules.core.AbstractRegexpBasedRule;
import com.adobe.ac.pmd.rules.core.ViolationPriority;

/**
 * @author xagnetti
 */
public class ParameterizedRegExpBasedRule extends AbstractRegexpBasedRule
{
   public static final String PROPERTY_NAME = "expression";

   /*
    * (non-Javadoc)
    * @see com.adobe.ac.pmd.rules.core.AbstractFlexRule#getDefaultPriority()
    */
   @Override
   protected ViolationPriority getDefaultPriority()
   {
      return ViolationPriority.NORMAL;
   }

   /*
    * (non-Javadoc)
    * @see com.adobe.ac.pmd.rules.core.AbstractRegexpBasedRule#getRegexp()
    */
   @Override
   protected String getRegexp()
   {
      return getStringProperty( propertyDescriptorFor( PROPERTY_NAME ) );
   }

   /*
    * (non-Javadoc)
    * @see
    * com.adobe.ac.pmd.rules.core.AbstractFlexRule#isConcernedByTheCurrentFile()
    */
   @Override
   protected boolean isConcernedByTheCurrentFile()
   {
      return true;
   }

   /*
    * (non-Javadoc)
    * @seecom.adobe.ac.pmd.rules.core.AbstractRegexpBasedRule#
    * isViolationDetectedOnThisMatchingLine(java.lang.String)
    */
   @Override
   protected boolean isViolationDetectedOnThisMatchingLine( final String line )
   {
      return true;
   }

   /*
    * (non-Javadoc)
    * @see net.sourceforge.pmd.CommonAbstractRule#propertiesByName()
    */
   @Override
   protected final Map< String, PropertyDescriptor > propertiesByName()
   {
      final Map< String, PropertyDescriptor > properties = new LinkedHashMap< String, PropertyDescriptor >();

      properties.put( PROPERTY_NAME,
                      new StringProperty( PROPERTY_NAME, "", "", properties.size() ) );

      return properties;
   }
}
