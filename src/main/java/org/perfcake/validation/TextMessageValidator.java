/*
 * -----------------------------------------------------------------------\
 * PerfCake
 *  
 * Copyright (C) 2010 - 2013 the original author or authors.
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
 * -----------------------------------------------------------------------/
 */
package org.perfcake.validation;

import java.util.HashMap;

import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import org.perfcake.message.Message;

/**
 * 
 * @author Lucie Fabriková <lucie.fabrikova@gmail.com>
 * @author Martin Večeřa <marvenec@gmail.com>
 */
public class TextMessageValidator implements MessageValidator {

   private static final Logger log = Logger.getLogger(TextMessageValidator.class);

   private String expectedOutput = "";

   @Override
   public boolean isValid(final Message message) {
      final String trimmedLinesOfPayload = StringUtil.trimLines(message.getPayload().toString());
      final String resultPayload = StringUtil.trim(trimmedLinesOfPayload);

      if (!resultPayload.matches(expectedOutput)) {
         if (log.isEnabledFor(Level.ERROR)) {
            // TODO log more verbose description at levels like warn/info/debug
            if (log.isInfoEnabled()) {
               log.info(String.format("Message payload '%s' is not equal to the expected payload '%s'.", message.getPayload().toString(), expectedOutput));
            }
            return false;
         }
      }

      return true;
   }

   @Override
   public void setAssertions(final String validationRule) {
      expectedOutput = StringUtil.trim(StringUtil.trimLines(validationRule));
   }

}
